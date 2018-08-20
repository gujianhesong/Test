package com.test.memory.proxy;

import android.content.Context;
import com.test.memory.bean.NewsDetailInfo;
import com.test.memory.mvp.NewsDetailContract;
import com.test.memory.mvp.NewsDetailPresenter;
import com.zzhoujay.richtext.RichText;

public class NewsDetailContent extends TextDetailContent implements NewsDetailContract.View {

  private NewsDetailPresenter mPresenter;
  private NewsDetailInfo mInfo;

  public NewsDetailContent(Context context) {
    super(context);

    mPresenter = new NewsDetailPresenter();
    mPresenter.attachView(this);
  }

  @Override public void onDestoryView() {
    super.onDestoryView();
    mPresenter.detachView();
  }

  public void requestData(String newsId) {
    mPresenter.requestNewsDetailInfo(newsId);
  }

  @Override public void showNewsDetail(NewsDetailInfo info) {
    mInfo = info;
    if (info != null) {
      if (info.getBody() != null) {
        RichText.from(info.getBody()).into(mTextView);
      }
    }
  }

  @Override public void error(Throwable throwable) {

  }

  public NewsDetailInfo getNewsDetailInfo() {
    return mInfo;
  }
}
