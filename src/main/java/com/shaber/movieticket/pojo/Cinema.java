package com.shaber.movieticket.pojo;

public class Cinema {
    private String cid;
    private String cname;
    private String caddress;
    private String ccall;

    public Cinema() {
    }

    public Cinema(String cname, String caddress, String ccall) {
        this.cname = cname;
        this.caddress = caddress;
        this.ccall = ccall;
    }

    public Cinema(String cid, String cname, String caddress, String ccall) {
        this.cid = cid;
        this.cname = cname;
        this.caddress = caddress;
        this.ccall = ccall;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getCname() {
        return cname;
    }

    public void setCname(String cname) {
        this.cname = cname;
    }

    public String getCaddress() {
        return caddress;
    }

    public void setCaddress(String caddress) {
        this.caddress = caddress;
    }

    public String getCcall() {
        return ccall;
    }

    public void setCcall(String ccall) {
        this.ccall = ccall;
    }

    @Override
    public String toString() {
        return "Cinema{" +
                "cid='" + cid + '\'' +
                ", cname='" + cname + '\'' +
                ", caddress='" + caddress + '\'' +
                ", ccall='" + ccall + '\'' +
                '}';
    }
}
