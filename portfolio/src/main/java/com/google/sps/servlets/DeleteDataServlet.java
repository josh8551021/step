package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet("/delete-data")
public class DeleteDataServlet extends HttpServlet {

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    List<Entity> allCommentEntities = DataServlet.getCommentEntities(datastore);
    List<Key> commentEntityKeys = allCommentEntities.stream().map(Entity::getKey).collect(Collectors.toList());

    datastore.delete(commentEntityKeys);
  }
}
