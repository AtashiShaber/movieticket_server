package com.shaber.movieticket.vo.pagequery;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ScreeningPageQueryVO extends PageQueryVO{
    private String mnmae;
    private String srname;
    private String cname;
    private LocalDate sday;
    private String stime;
}
