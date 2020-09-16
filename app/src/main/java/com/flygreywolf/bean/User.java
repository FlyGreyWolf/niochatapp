package com.flygreywolf.bean;

public class User {

    private String host;

    public User() {
    }

    public User(String host) {
        this.host = host;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public String toString() {
        return "User{" +
                "host='" + host + '\'' +
                '}';
    }
}
