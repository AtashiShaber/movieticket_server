package com.shaber.movieticket.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private String uid;
    private String uname;
    private String uphone;
    private BigDecimal umoney;
}
