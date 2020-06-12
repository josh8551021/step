package com.google.sps.servlets;

class UserData {
    private boolean isLoggedIn = false;
    private String email;

    UserData(String email, boolean isLoggedIn) {
        this.isLoggedIn = isLoggedIn;
        this.email = email;
    }
}
