package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.List;

@WebServlet("/visit")
public class PageVisitsServlet extends HttpServlet {

  private static final String VISITS_TODAY = "Visits Today";

  private static final String VISITS = "visits";
  private static final String DATE = "date";

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    incrementPageVisit(datastore);
  }

  private void incrementPageVisit(DatastoreService datastore) {
    LocalDate today = LocalDate.now(ZoneOffset.UTC);
    Entity todayVisitsEntity = getPageVisitsEntity(datastore, today);
    long visitsToday = (long) todayVisitsEntity.getProperty(VISITS);
    todayVisitsEntity.setProperty(VISITS, visitsToday + 1);

    datastore.put(todayVisitsEntity);
  }

  private Entity createVisitsEntity() {
    long timestamp = System.currentTimeMillis();
    Calendar calendar = Calendar.getInstance();
    LocalDate today = LocalDate.now(ZoneOffset.UTC);
    Entity visitsEntity = new Entity(VISITS_TODAY);

    visitsEntity.setProperty(DATE, today.toString());
    visitsEntity.setProperty("dayOfWeek", calendar.get(Calendar.DAY_OF_WEEK));
    visitsEntity.setProperty(VISITS, 1);
    visitsEntity.setProperty("timestamp", timestamp);

    return visitsEntity;
  }

  private Entity getPageVisitsEntity(DatastoreService datastore, LocalDate date) {
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
      entity = collapseDateEntities(preparedQuery, datastore);
    }

    // No results returned.
    if (entity == null) {
      entity = createVisitsEntity();
    }

    return entity;
  }

  private Entity collapseDateEntities(PreparedQuery preparedQuery, DatastoreService datastore) {
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
