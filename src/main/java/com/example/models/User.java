package com.example.models;

import java.time.LocalDate;
import java.util.Date;

public class User {
    private String fName;
    private String lName;
    private String hashedPass;
    private String email;
    private Date createdAt;
    private double balance;

    public User(String fName, String lName, String hashedPass, String email, LocalDate createdAt, double balance) {
        this.fName = fName;
        this.lName = lName;
        this.hashedPass = hashedPass;
        this.email = email;
        this.createdAt = new Date();
        this.balance = balance;
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

    public String getHashedPass() {
        return hashedPass;
    }
    public void setHashedPass(String hashedPass) {
        this.hashedPass = hashedPass;
    }

    public Date getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public double getAccountBalance() {
        return balance;
    }
    public void setAccountBalance(double accountBalance) {
        this.balance = accountBalance;
    }
}
