package com.test.memory.mvp;

/**
 * Presenter基类
 */
public interface IPresenter<T extends IView>{

    void attachView(T view);

    void detachView();

    void onStart();
}