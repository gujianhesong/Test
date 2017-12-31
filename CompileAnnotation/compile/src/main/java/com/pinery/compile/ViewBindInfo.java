package com.pinery.compile;

import javax.lang.model.type.TypeMirror;

/**
 * Created by Administrator on 2017/12/31 0031.
 */

public class ViewBindInfo {

    public int viewId;
    public String viewName;
    public TypeMirror typeMirror;

    public ViewBindInfo(int viewId, String viewName, TypeMirror typeMirror){
        this.viewId = viewId;
        this.viewName = viewName;
        this.typeMirror = typeMirror;
    }

}
