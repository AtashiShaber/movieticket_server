package com.shaber.movieticket.vo.pagequery;

import lombok.Data;

import java.time.LocalDate;

@Data
public class MoviePageQueryVO extends PageQueryVO{
    private String mname;
    private String mactor;
    private int mstatus = -1;
    private LocalDate mstarttime;
    private LocalDate mendtime;
}
