package com.test.memory.proxy;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.widget.NestedScrollView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public abstract class TextDetailContent {

  protected Context mContext;
  protected TextView mTextView;
  protected NestedScrollView mScrollView;

  public TextDetailContent(Context context) {
    mContext = context;

    initView();
  }

  private void initView() {

    initContentView();
  }

  public void showInView(ViewGroup parent) {
    if (!isInActivity()) {
      if (mScrollView != null) {
        parent.addView(mScrollView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT));
      }
    } else {
      parent.addView(mTextView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.MATCH_PARENT));
    }
  }

  public void onDestoryView() {
    if (!isInActivity()) {
      if (mScrollView != null && mScrollView.getParent() != null) {
        ((ViewGroup) mScrollView.getParent()).removeView(mScrollView);
      }
    } else {
      if (mTextView != null && mTextView.getParent() != null) {
        ((ViewGroup) mTextView.getParent()).removeView(mTextView);
      }
    }

    if (mTextView != null) {
      mTextView.setText("");
    }
  }

  public View getView() {
    if (!isInActivity()) {
      return mScrollView;
    } else {
      return mTextView;
    }
  }

  /**
   * 初始化WebView
   */
  private void initContentView() {
    //创建TextView
    int padding = dp2px(16);
    mTextView = new TextView(mContext);
    mTextView.setTextSize(16);
    mTextView.setPadding(padding, padding, padding, padding);
    mTextView.setTextColor(Color.DKGRAY);

    if (!isInActivity()) {
      mScrollView.removeAllViews();
      mScrollView.addView(mTextView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.MATCH_PARENT));
    }
  }

  private boolean isInActivity() {
    return mContext instanceof Activity;
  }

  /**
   * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
   */
  private int dp2px(float dpValue) {
    final float scale = mContext.getResources().getDisplayMetrics().density;
    return (int) (dpValue * scale + 0.5f);
  }
}
