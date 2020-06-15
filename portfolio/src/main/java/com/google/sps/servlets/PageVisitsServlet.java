package com.google.sps.servlets;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/visit")
public class PageVisitsServlet extends HttpServlet {
  private final PageVisitsProcessor processor = new PageVisitsProcessor();

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    processor.incrementPageVisit();

    response.setContentType("text/html");
    response.getWriter().println("200 OK");
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // TODO: Process request max-days data
    String chartDataJson = processor.getDataJson(request);

    response.setContentType("application/json");
    response.getWriter().println(chartDataJson);
  }
}
