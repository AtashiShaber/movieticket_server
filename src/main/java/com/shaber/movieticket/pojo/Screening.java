package com.shaber.movieticket.pojo;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Screening {
    private String sid;
    private String mid;
    private String srid;
    private LocalDate sday;
    private String stime;
    private BigDecimal sprice;

    public Screening() {
    }

    public Screening(String sid, String mid, String srid, LocalDate sday, String stime, BigDecimal sprice) {
        this.sid = sid;
        this.mid = mid;
        this.srid = srid;
        this.sday = sday;
        this.stime = stime;
        this.sprice = sprice;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getSrid() {
        return srid;
    }

    public void setSrid(String srid) {
        this.srid = srid;
    }

    public LocalDate getSday() {
        return sday;
    }

    public void setSday(LocalDate sday) {
        this.sday = sday;
    }

    public String getStime() {
        return stime;
    }

    public void setStime(String stime) {
        this.stime = stime;
    }

    public BigDecimal getSprice() {
        return sprice;
    }

    public void setSprice(BigDecimal sprice) {
        this.sprice = sprice;
    }

    @Override
    public String toString() {
        return "screening{" +
                "sid='" + sid + '\'' +
                ", mid='" + mid + '\'' +
                ", srid='" + srid + '\'' +
                ", sday=" + sday +
                ", stime='" + stime + '\'' +
                ", sprice=" + sprice +
                '}';
    }
}
