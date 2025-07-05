package com.example.csc325capstone;

import java.util.Date;

public class User {
    private String fName;
    private String lName;
    private String email;
    private Date createdAt;

    public User(String fName, String lName, String email, Date createdAt) {
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
