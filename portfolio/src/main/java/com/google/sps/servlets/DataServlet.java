// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  private static final int DEFAULT_MESSAGES = 10;
  private static final int MAX_MESSAGES = 50;

  DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    int numComments = getNumComments(request);
    if (numComments == -1) {
      numComments = DEFAULT_MESSAGES;
    }

    numComments = Math.min(numComments, MAX_MESSAGES);
    List<String> messages = getCommentEntities(datastore, numComments).stream().map(
        entity -> entity.getProperty("text").toString()).collect(Collectors.toList());

    response.setContentType("text/html;");
    response.getWriter().println(convertToJson(messages));
  }

  private int getNumComments(HttpServletRequest request) {
    String numCommentsString = request.getParameter("num-comments");
    int numComments;
    try {
      numComments = Integer.parseInt(numCommentsString);
    } catch (NumberFormatException e) {
      System.err.println("Could not convert to int: " + numCommentsString);
      return -1;
    }
    return numComments;
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String commentText = request.getParameter("comment-input");
    Entity commentEntity = createCommentEntity(commentText);

    //datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity);

    response.sendRedirect("/index.html");
  }

  private String convertToJson(List<String> messages) {
    Gson gson = new Gson();
    return gson.toJson(messages);
  }

  private Entity createCommentEntity(String commentText) {
    long timestamp = System.currentTimeMillis();

    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("text", commentText);
    commentEntity.setProperty("timestamp", timestamp);
    
    return commentEntity;
  }

  protected static List<Entity> getCommentEntities(DatastoreService datastore) {
    Query query = new Query("Comment").addSort("timestamp",
        Query.SortDirection.ASCENDING);
    return datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
  }

  protected static List<Entity> getCommentEntities(DatastoreService datastore, int numComments) {
    Query query = new Query("Comment").addSort("timestamp",
        Query.SortDirection.ASCENDING);
    return datastore.prepare(query).asList(FetchOptions.Builder.withLimit(numComments));
  }
}
