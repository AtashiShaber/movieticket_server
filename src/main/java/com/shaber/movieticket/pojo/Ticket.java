package com.shaber.movieticket.pojo;

public class Ticket {
    private String tid;
    private String sid;
    private String tseat;
    private int tstatus;

    public Ticket() {
    }

    public Ticket(String sid, String tseat) {
        this.sid = sid;
        this.tseat = tseat;
    }

    public Ticket(String tid, String sid, String tseat, int tstatus) {
        this.tid = tid;
        this.sid = sid;
        this.tseat = tseat;
        this.tstatus = tstatus;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getTseat() {
        return tseat;
    }

    public void setTseat(String tseat) {
        this.tseat = tseat;
    }

    public int getTstatus() {
        return tstatus;
    }

    public void setTstatus(int tstatus) {
        this.tstatus = tstatus;
    }

    @Override
    public String toString() {
        return "ticket{" +
                "tid='" + tid + '\'' +
                ", sid='" + sid + '\'' +
                ", tseat='" + tseat + '\'' +
                ", tstatus=" + tstatus +
                '}';
    }
}
