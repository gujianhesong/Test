package com.pinery.bind_lib;

import android.app.Activity;

/**
 * Created by Administrator on 2017/12/31 0031.
 */

public class BindHelper {

    /**
     * 绑定方法
     * @param activity
     */
    public static void bind(Activity activity) {
        try {
            Class<?> viewBinderClazz = Class.forName(activity.getClass().getCanonicalName() + "_ViewBinder");
            ViewBinder viewBinder = (ViewBinder) viewBinderClazz.newInstance();
            viewBinder.bind(activity);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

    }

}
