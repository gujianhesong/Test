package com.test.memory.util;

import android.util.Log;
import io.reactivex.Flowable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import java.util.concurrent.TimeUnit;

/**
 * Created by gujian on 2018-08-12.
 */

public class RetryWithDelayFunc implements Function<Flowable<? extends Throwable>, Flowable<?>> {
  private final int maxRetries;
  private final int retryDelayMillis;
  private int retryCount;

  public RetryWithDelayFunc() {
    this(4, 6000);
  }

  public RetryWithDelayFunc(int maxRetries, int retryDelayMillis) {
    this.maxRetries = maxRetries;
    this.retryDelayMillis = retryDelayMillis;
  }

  @Override public Flowable<?> apply(@NonNull Flowable<? extends Throwable> observable)
      throws Exception {
    return observable.flatMap(new Function<Throwable, Flowable<?>>() {
      @Override public Flowable<?> apply(Throwable throwable) {
        if (++retryCount <= maxRetries) {
          // When this Observable calls onNext, the original Observable will be retried (i.e. re-subscribed).
          Log.i("RetryWithDelayFunc", "Get error, it will try after "
              + retryDelayMillis
              + " millisecond, retry count "
              + retryCount);
          return Flowable.timer(retryDelayMillis, TimeUnit.MILLISECONDS);
        }
        // Max retries hit. Just pass the error along.
        return Flowable.error(throwable);
      }
    });
  }
}
