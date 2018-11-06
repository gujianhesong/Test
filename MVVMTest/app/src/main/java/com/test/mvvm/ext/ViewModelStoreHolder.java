package com.test.mvvm.ext;

import android.arch.lifecycle.ViewModelStore;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by hesong-os on 2018/9/18.
 */

public class ViewModelStoreHolder {

    private static class InstanceHolder {
        private static ViewModelStoreHolder instance = new ViewModelStoreHolder();
    }

    public static ViewModelStoreHolder getInstance() {
        return InstanceHolder.instance;
    }

    private ViewModelStoreHolder() {
    }

    private HashMap<String, ViewModelStore> map = new HashMap<>();
    private static final String DEFAULT_TAG = "default_tag";

    public ViewModelStore getDefault() {
        return get(DEFAULT_TAG);
    }

    public ViewModelStore get(String tag) {
        if (TextUtils.isEmpty(tag)) {
            tag = DEFAULT_TAG;
        }
        ViewModelStore viewModelStore = map.get(tag);
        if (viewModelStore == null) {
            viewModelStore = new ViewModelStore();
            map.put(tag, viewModelStore);
        }
        return viewModelStore;
    }

    public void removeDefault() {
        remove(DEFAULT_TAG);
    }

    public void remove(String tag) {
        if (TextUtils.isEmpty(tag)) {
            tag = DEFAULT_TAG;
        }
        ViewModelStore viewModelStore = map.remove(tag);
        if (viewModelStore != null) {
            viewModelStore.clear();
        }
    }

    public void clear() {
        Set<Map.Entry<String, ViewModelStore>> set = map.entrySet();
        for (Map.Entry<String, ViewModelStore> item : set) {
            if (item.getValue() != null) {
                item.getValue().clear();
            }
        }
        map.clear();
    }

}
