package com.domain;

public class User {
    private String name;
    private Integer pass;

    public User() {
    }

    public User(String name, Integer pass) {
        this.name = name;
        this.pass = pass;
    }

    public String toString() {
        return "User{name='" + this.name + '\'' + ", pass=" + this.pass + '}';
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPass() {
        return this.pass;
    }

    public void setPass(Integer pass) {
        this.pass = pass;
    }
}