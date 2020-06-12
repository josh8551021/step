package com.google.sps.servlets;


import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

        UserData userData;

        UserService userService = UserServiceFactory.getUserService();
        if (userService.isUserLoggedIn()) {
            String userEmail = userService.getCurrentUser().getEmail();
            userData = new UserData(userEmail);

            Gson gson = new Gson();
            String responseString = gson.toJson(userData);

            response.getWriter().println(responseString);
        } else {
            userData = new UserData("josh8551021@gmail.com");

            Gson gson = new Gson();
            String responseString = gson.toJson(userData);

            response.getWriter().println(responseString);
        }
    }
}
