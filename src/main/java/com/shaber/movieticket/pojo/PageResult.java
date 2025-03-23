package com.shaber.movieticket.pojo;

import java.util.List;

public class PageResult<T> {
    private int total;
    private List<T> value;

    public PageResult() {
    }

    public PageResult(int total, List<T> value) {
        this.total = total;
        this.value = value;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<T> getValue() {
        return value;
    }

    public void setValue(List<T> value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "PageResult{" +
                "total=" + total +
                ", value=" + value +
                '}';
    }
}
