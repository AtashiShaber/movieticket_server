package com.shaber.movieticket.resp;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

import java.util.Collections;

@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS) // 添加类型元数据
public class RV<T> {
    private int code;
    private boolean success;
    private String message;
    private T data;

    protected RV() {

    }

    protected RV(int code, boolean success, String message, T data) {
        this.code = code;
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static RV<?> success(String message) {
        return new RV<Object>(2000, true, message, Collections.EMPTY_MAP);
    }

    public static <T> RV<T> success(String message, T data) {
        return new RV<T>(2000, true, message, data);
    }

    public static <T> RV<T> noData(int code, String message, T data) {
        return new RV<T>(code, false, message, data);
    }

    public static RV<?> fail(String message) {
        return new RV<Object>(4000, false, message, Collections.EMPTY_MAP);
    }

    public static RV<?> customFail(String message, int code) { return new RV<Object>(code, false, message, Collections.EMPTY_MAP); }
}
