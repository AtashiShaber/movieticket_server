package com.shaber.movieticket.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public class Movie {
    private String mid;
    private String mname;
    private String mp;
    private String mactor;
    private int mstatus;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate mstarttime;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate mendtime;

    public Movie() {
    }

    public Movie(String mid, String mname, String mp, String mactor, int mstatus, LocalDate mstarttime, LocalDate mendtime) {
        this.mid = mid;
        this.mname = mname;
        this.mp = mp;
        this.mactor = mactor;
        this.mstatus = mstatus;
        this.mstarttime = mstarttime;
        this.mendtime = mendtime;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getMname() {
        return mname;
    }

    public void setMname(String mname) {
        this.mname = mname;
    }

    public String getMp() {
        return mp;
    }

    public void setMp(String mp) {
        this.mp = mp;
    }

    public String getMactor() {
        return mactor;
    }

    public void setMactor(String mactor) {
        this.mactor = mactor;
    }

    public int getMstatus() {
        return mstatus;
    }

    public void setMstatus(int mstatus) {
        this.mstatus = mstatus;
    }

    public LocalDate getMstarttime() {
        return mstarttime;
    }

    public void setMstarttime(LocalDate mstarttime) {
        this.mstarttime = mstarttime;
    }

    public LocalDate getMendtime() {
        return mendtime;
    }

    public void setMendtime(LocalDate mendtime) {
        this.mendtime = mendtime;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "mid='" + mid + '\'' +
                ", mname='" + mname + '\'' +
                ", mp='" + mp + '\'' +
                ", mactor='" + mactor + '\'' +
                ", mstatus=" + mstatus +
                ", mstarttime=" + mstarttime +
                ", mendtime=" + mendtime +
                '}';
    }
}
