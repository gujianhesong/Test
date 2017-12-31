package com.pinery.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 作用于View的注解，如@BindView(R.id.text) TextView tvText
 *
 * @Retention(RetentionPolicy.CLASS) 表示生命周期到类的编译时期
 * @Target(ElementType.FIELD) 表示注解作用在字段上
 *
 * Created by Administrator on 2017/12/31 0031.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface BindView {

    @android.support.annotation.IdRes
    int value();
}
