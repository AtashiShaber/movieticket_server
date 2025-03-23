package com.shaber.movieticket.pojo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Order {
    private String oid;
    private String uid;
    private String tid;
    private LocalDateTime otime;
    private BigDecimal oprice;
    private int ostatus;

    public Order() {
    }

    public Order(String oid, String uid, String tid, LocalDateTime otime, BigDecimal oprice, int ostatus) {
        this.oid = oid;
        this.uid = uid;
        this.tid = tid;
        this.otime = otime;
        this.oprice = oprice;
        this.ostatus = ostatus;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public LocalDateTime getOtime() {
        return otime;
    }

    public void setOtime(LocalDateTime otime) {
        this.otime = otime;
    }

    public BigDecimal getOprice() {
        return oprice;
    }

    public void setOprice(BigDecimal oprice) {
        this.oprice = oprice;
    }

    public int getOstatus() {
        return ostatus;
    }

    public void setOstatus(int ostatus) {
        this.ostatus = ostatus;
    }

    @Override
    public String toString() {
        return "Order{" +
                "oid='" + oid + '\'' +
                ", uid='" + uid + '\'' +
                ", tid='" + tid + '\'' +
                ", otime=" + otime +
                ", oprice=" + oprice +
                ", ostatus=" + ostatus +
                '}';
    }
}
