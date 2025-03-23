package com.shaber.movieticket.resp;

import com.shaber.movieticket.dto.AdminDto;
import lombok.Data;

@Data
public class AuthAdminResponse {
    private String uuid;
    private AdminDto adminDto;
    private long expires;
}
