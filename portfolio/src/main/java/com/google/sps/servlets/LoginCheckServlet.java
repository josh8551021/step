package com.google.sps.servlets;


import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/login-check")
public class LoginCheckServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();

    String userEmail;
    boolean isLoggedIn = userService.isUserLoggedIn();

    if (isLoggedIn) {
      userEmail = userService.getCurrentUser().getEmail();
    } else {
      userEmail = "";
    }

    UserData userData = new UserData(userEmail, isLoggedIn);

    Gson gson = new Gson();
    String responseString = gson.toJson(userData);

    response.setContentType("application/json");
    response.getWriter().println(responseString);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.sendRedirect("/");
  }
}
