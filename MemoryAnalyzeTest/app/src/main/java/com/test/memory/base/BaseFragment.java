package com.test.memory.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.test.memory.mvp.IPresenter;
import com.test.memory.mvp.IView;

public abstract class BaseFragment<T extends IPresenter> extends Fragment implements IView {

  protected View rootView;

  protected T mPresenter;

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

    mPresenter = createPresenter();
    if (mPresenter != null) {
      mPresenter.attachView(this);
    }

    if (mPresenter != null) {
      mPresenter.onStart();
    }

    return rootView;
  }

  @Override public void onDestroyView() {
    super.onDestroyView();

    if (mPresenter != null) {
      mPresenter.detachView();
      mPresenter = null;
    }
  }

  protected abstract T createPresenter();

  protected abstract int getLayoutId();

  protected abstract void initViews(View view, Bundle savedInstanceState);

  protected abstract void initData();
}