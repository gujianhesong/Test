package com.test.mvvm.ext;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.support.annotation.NonNull;

/**
 * Created by hesong-os on 2018/9/13.
 */

public class MyLifecycleOwner implements LifecycleOwner {

    private Lifecycle lifecycle = new MyLifecycle();

    private static class Holder{
        private static MyLifecycleOwner instance = new MyLifecycleOwner();
    }

    public static MyLifecycleOwner getInstance(){
        return Holder.instance;
    }

    private MyLifecycleOwner(){
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return lifecycle;
    }

    private static class MyLifecycle extends Lifecycle{

        @Override
        public void addObserver(@NonNull LifecycleObserver observer) {

        }

        @Override
        public void removeObserver(@NonNull LifecycleObserver observer) {

        }

        @NonNull
        @Override
        public State getCurrentState() {
            return null;
        }
    }
}
