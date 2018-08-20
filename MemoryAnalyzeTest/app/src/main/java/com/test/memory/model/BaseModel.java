package com.test.memory.model;

import com.test.memory.api.RetrofitClient;

public abstract class BaseModel {

  public <T> T getApiService(String url, Class<T> cl) {
    return RetrofitClient.getInstance().getApiService(url, cl);
  }
}
