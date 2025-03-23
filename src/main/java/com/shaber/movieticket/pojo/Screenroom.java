package com.shaber.movieticket.pojo;

public class Screenroom {
    private String srid;
    private String cid;
    private String srname;

    public Screenroom() {
    }

    public Screenroom(String cid, String srname) {
        this.cid = cid;
        this.srname = srname;
    }

    public Screenroom(String srid, String cid, String srname) {
        this.srid = srid;
        this.cid = cid;
        this.srname = srname;
    }

    public String getSrid() {
        return srid;
    }

    public void setSrid(String srid) {
        this.srid = srid;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getSrname() {
        return srname;
    }

    public void setSrname(String srname) {
        this.srname = srname;
    }

    @Override
    public String toString() {
        return "screenroom{" +
                "srid='" + srid + '\'' +
                ", cid='" + cid + '\'' +
                ", srname='" + srname + '\'' +
                '}';
    }
}
