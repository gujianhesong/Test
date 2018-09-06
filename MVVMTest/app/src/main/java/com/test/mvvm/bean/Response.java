package com.test.mvvm.bean;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class Response<T> {

    public enum Status {
        LOADING,
        SUCCESS,
        ERROR,
        NO_NETWORK
    }

    public final Status status;

    @Nullable
    public final T data;

    @Nullable
    public final Throwable error;

    private Response(Status status, @Nullable T data, @Nullable Throwable error) {
        this.status = status;
        this.data = data;
        this.error = error;
    }

    public boolean isSuccess(){
        return status == Status.SUCCESS;
    }

    public boolean isError(){
        return status == Status.ERROR;
    }

    public boolean isLoading(){
        return status == Status.LOADING;
    }

    public static Response loading() {
        return new Response(Status.LOADING, null, null);
    }

    public static <T> Response<T> success(@NonNull T data) {
        return new Response(Status.SUCCESS, data, null);
    }

    public static Response error(@NonNull Throwable error) {
        return new Response(Status.ERROR, null, error);
    }
    public static Response noNetwork() {
        return new Response(Status.NO_NETWORK, null, null);
    }

}
