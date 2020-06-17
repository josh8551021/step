package com.google.sps.servlets;

public class Comment {
  private final String userEmail;
  private final String message;

  Comment(String message, String userEmail) {
    this.message = message;
    this.userEmail = userEmail;
  }
}
