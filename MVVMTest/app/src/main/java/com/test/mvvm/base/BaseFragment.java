package com.test.mvvm.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class BaseFragment extends Fragment{

  protected View rootView;

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {

    if (rootView == null) {
      rootView = inflater.inflate(getLayoutId(), container, false);

      initViews(rootView, savedInstanceState);

      initData();
    }

    ViewGroup parent = (ViewGroup) rootView.getParent();
    if (parent != null) {
      parent.removeView(rootView);
    }

    return rootView;
  }

  protected abstract int getLayoutId();

  protected abstract void initViews(View view, Bundle savedInstanceState);

  protected abstract void initData();
}