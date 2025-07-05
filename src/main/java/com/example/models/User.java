package com.example.models;

import java.time.LocalDate;
import java.util.Date;

public class User {
    public String fName;
    public String lName;
    public String email;
    public Date createdAt;

    public User(String fName, String lName, String email, LocalDate createdAt) {
        this.fName = fName;
        this.lName = lName;
        this.email = email;
        this.createdAt = new Date();
    }
    public String getfName() {
        return fName;
    }
    public void setfName(String fName) {
        this.fName = fName;
    }
    public String getlName() {
        return lName;
    }
    public void setlName(String lName) {
        this.lName = lName;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public Date getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

}
