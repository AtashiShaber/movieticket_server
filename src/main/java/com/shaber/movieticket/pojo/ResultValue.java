package com.shaber.movieticket.pojo;

public class ResultValue<T> {
    private boolean result;
    private String message;
    private T t;

    public ResultValue() {
    }

    public ResultValue(boolean result, String message, T t) {
        this.result = result;
        this.message = message;
        this.t = t;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getT() {
        return t;
    }

    public void setT(T t) {
        this.t = t;
    }

    @Override
    public String toString() {
        return "ResultValue{" +
                "result=" + result +
                ", message='" + message + '\'' +
                ", t=" + t +
                '}';
    }
}
