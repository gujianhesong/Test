package com.test.mvvm.callback;

import java.util.List;

/**
 * Created by gujian on 2018-08-12.
 */

public interface OnListDataCallback<T> {
  void onSuccess(List<T> t);

  void onError(Throwable throwable);
}
