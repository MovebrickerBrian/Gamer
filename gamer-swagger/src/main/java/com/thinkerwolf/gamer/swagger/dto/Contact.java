package com.thinkerwolf.gamer.swagger.dto;

public class Contact {
    private final String name;
    private final String url;
    private final String email;

    public Contact(String name, String url, String email) {
        this.name = name;
        this.url = url;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getEmail() {
        return email;
    }
}
