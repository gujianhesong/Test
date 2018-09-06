package com.test.mvvm.bean;

/**
 * Created by hesong-os on 2018/9/6.
 */

public class ListResult<T> {

    private T data;
    private int page;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }
}
