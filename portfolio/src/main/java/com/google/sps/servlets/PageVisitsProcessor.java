package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.gson.Gson;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PageVisitsProcessor {

  private static final int MAX_DAYS = 30;
  private static final String VISITS_TODAY = "Visits Today";

  // Define constants for "Visits Today" entity properties
  private static final String VISITS = "visits";
  private static final String DATE = "date";
  private static final String DAY_OF_WEEK = "dayOfWeek";
  private static final String TIMESTAMP = "timestamp";

  // Define constants for chart options (an enum might be a better choice)
  private static final int CHART_BY_DATE = 0;
  private static final int CHART_BY_WEEKDAY = 1;

  final DatastoreService datastore;

  PageVisitsProcessor() {
    this.datastore = DatastoreServiceFactory.getDatastoreService();
  }

  public String getDataJson(HttpServletRequest request) {
    int choice = processGetRequest(request);
    String chartDataJson;

    if (choice == CHART_BY_WEEKDAY) {
      chartDataJson = getDayOfWeekDataJson();
    } else {
      chartDataJson = getDateDataJson();
    }

    return chartDataJson;
  }

  public String getDayOfWeekDataJson() {
    List<Entity> visitEntities = extractEntitiesByDate();
    Map<Integer, Long> visitsByWeekday = mapVisitsPerWeekDay(visitEntities);

    Gson gson = new Gson();
    return gson.toJson(visitsByWeekday);
  }

  public String getDateDataJson() {
    List<Entity> visitEntities = extractEntitiesByDate();
    Map<LocalDate, Long> visitsByDate = mapVisitsPerDay(visitEntities);

    Gson gson = new Gson();
    return gson.toJson(visitsByDate);
  }

  public int processGetRequest(HttpServletRequest request) {
    String chartChoice = request.getParameter("chart-choice");
    int chartChoiceInt = CHART_BY_DATE;
    try {
      chartChoiceInt = Integer.parseInt(chartChoice);
    } catch(NumberFormatException e) {
      System.err.println("Could not convert to int: " + chartChoice);
    }

    return chartChoiceInt;
  }

  private Map<Integer, Long> mapVisitsPerWeekDay(List<Entity> visitEntities) {

    Map<Integer, Long> visitsByDayOfWeek = new HashMap<>();
    visitsByDayOfWeek.put(Calendar.SUNDAY, 0L);
    visitsByDayOfWeek.put(Calendar.MONDAY, 0L);
    visitsByDayOfWeek.put(Calendar.TUESDAY, 0L);
    visitsByDayOfWeek.put(Calendar.WEDNESDAY, 0L);
    visitsByDayOfWeek.put(Calendar.THURSDAY, 0L);
    visitsByDayOfWeek.put(Calendar.FRIDAY, 0L);
    visitsByDayOfWeek.put(Calendar.SATURDAY, 0L);

    visitEntities.forEach(entity -> {
      int dayOfWeek = Integer.parseInt(entity.getProperty(DAY_OF_WEEK).toString());
      long addedVisits = (long) entity.getProperty(VISITS);
      visitsByDayOfWeek.computeIfPresent(dayOfWeek, (day, visits) -> visits + addedVisits);
    });
    return visitsByDayOfWeek;
  }

  private Map<LocalDate, Long> mapVisitsPerDay(List<Entity> visitEntities) {
    Map<LocalDate, Long> visitsPerDayMapping = new LinkedHashMap<>();
    visitEntities.forEach(entity -> {
      LocalDate date = LocalDate.parse((String) entity.getProperty(DATE));
      long numberOfVisits = (long) entity.getProperty(VISITS);
      visitsPerDayMapping.put(date, numberOfVisits);
    });
    return visitsPerDayMapping;
  }

  private List<Entity> extractEntitiesByDate() {
    Query query = new Query(VISITS_TODAY).addSort(
        TIMESTAMP, Query.SortDirection.ASCENDING);
    return datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
  }

  public void incrementPageVisit() {
    LocalDate today = LocalDate.now(ZoneOffset.UTC);
    Entity todayVisitsEntity = getPageVisitsEntity(today);
    long visitsToday = (long) todayVisitsEntity.getProperty(VISITS);
    todayVisitsEntity.setProperty(VISITS, visitsToday + 1L);

    datastore.put(todayVisitsEntity);
  }

  private Entity createVisitsEntity() {
    long timestamp = System.currentTimeMillis();
    Calendar calendar = Calendar.getInstance();
    LocalDate today = LocalDate.now(ZoneOffset.UTC);
    Entity visitsEntity = new Entity(VISITS_TODAY);

    visitsEntity.setProperty(DATE, today.toString());
    visitsEntity.setProperty(DAY_OF_WEEK, calendar.get(Calendar.DAY_OF_WEEK));
    visitsEntity.setProperty(VISITS, 1L);
    visitsEntity.setProperty(TIMESTAMP, timestamp);

    return visitsEntity;
  }

  private Entity getPageVisitsEntity(LocalDate date) {
    Query.Filter filter = new Query.FilterPredicate(
        DATE,
        Query.FilterOperator.EQUAL,
        date.toString());

    Query query = new Query(VISITS_TODAY).setFilter(filter);
    PreparedQuery preparedQuery = datastore.prepare(query);

    Entity entity;
    try {
      entity = preparedQuery.asSingleEntity();
    } catch (PreparedQuery.TooManyResultsException e) {
      // Collapse into single entity per day.
      System.err.println("More than none entity returned for a given date.");
      entity = collapseDateEntities(preparedQuery);
    }

    // No results returned.
    if (entity == null) {
      entity = createVisitsEntity();
    }

    return entity;
  }

  private Entity collapseDateEntities(PreparedQuery preparedQuery) {
    List<Entity> entities = preparedQuery.asList(FetchOptions.Builder.withDefaults());

    // Fold entries into single element.
    return entities.stream().reduce((entity1, entity2) -> {
      long summedVisits = (long) entity1.getProperty(VISITS) +
          (long) entity2.getProperty(VISITS);
      entity1.setProperty(VISITS, summedVisits);

      Key entity2Key = entity2.getKey();
      datastore.delete(entity2Key);
      return entity1;
    }).orElse(entities.get(0));
  }
}
