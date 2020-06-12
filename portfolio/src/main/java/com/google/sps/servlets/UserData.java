package com.google.sps.servlets;

class UserData {
    private boolean isLoggedIn = false;
    private String email;

    UserData(String email) {
        if (!email.isEmpty()) {
            this.isLoggedIn = true;
        }

        this.email = email;
    }
}
