package com.shaber.movieticket.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketInfo {
    private String tid;
    private String cname;
    private String srname;
    private String mname;
    private String tseat;
    private int tstatus;
}
