package com.google.sps.servlets;

class UserData {
    private final boolean isLoggedIn;
    private final String email;

    UserData(String email, boolean isLoggedIn) {
        this.isLoggedIn = isLoggedIn;
        this.email = email;
    }
}
