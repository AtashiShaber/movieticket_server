package com.shaber.movieticket.vo.pagequery;

import lombok.Data;

@Data
public class CinemaPageQueryVO extends PageQueryVO {
    private String cname;
    private String caddress;
    private String ccall;
}
