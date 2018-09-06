package com.test.mvvm.callback;

/**
 * Created by gujian on 2018-08-12.
 */

public interface OnDataCallback<T> {
  void onSuccess(T t);

  void onError(Throwable throwable);
}
