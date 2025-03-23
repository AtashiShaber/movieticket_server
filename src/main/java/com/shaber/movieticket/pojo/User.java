package com.shaber.movieticket.pojo;

import java.math.BigDecimal;

public class User {
    private String uid;
    private String uname;
    private String upwd;
    private BigDecimal umoney;
    private String uphone;

    public User() {
    }

    public User(String uid, String uname, String upwd, BigDecimal umoney, String uphone) {
        this.uid = uid;
        this.uname = uname;
        this.upwd = upwd;
        this.umoney = umoney;
        this.uphone = uphone;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

    public String getUpwd() {
        return upwd;
    }

    public void setUpwd(String upwd) {
        this.upwd = upwd;
    }

    public BigDecimal getUmoney() {
        return umoney;
    }

    public void setUmoney(BigDecimal umoney) {
        this.umoney = umoney;
    }

    public String getUphone() {
        return uphone;
    }

    public void setUphone(String uphone) {
        this.uphone = uphone;
    }

    @Override
    public String toString() {
        return "User{" +
                "uid='" + uid + '\'' +
                ", uname='" + uname + '\'' +
                ", upwd='" + upwd + '\'' +
                ", umoney=" + umoney +
                ", uphone='" + uphone + '\'' +
                '}';
    }
}
