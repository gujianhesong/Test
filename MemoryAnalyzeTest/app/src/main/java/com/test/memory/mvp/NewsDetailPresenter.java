package com.test.memory.mvp;

import com.test.memory.bean.NewsDetailInfo;
import com.test.memory.callback.OnDataCallback;
import com.test.memory.model.NewsModel;

public class NewsDetailPresenter extends BaseRxJavaPresenter<NewsDetailContract.View>
    implements NewsDetailContract.Presenter {

  private NewsModel model;

  public NewsDetailPresenter() {
    model = new NewsModel();
  }

  @Override public void requestNewsDetailInfo(final String newsId) {
    addDisposable(model.loadData(newsId, new OnDataCallback<NewsDetailInfo>() {
      @Override public void onSuccess(NewsDetailInfo info) {
        if (mView != null) {
          mView.showNewsDetail(info);
        }
      }

      @Override public void onError(Throwable throwable) {
        if (mView != null) {
          mView.error(throwable);
        }
      }
    }));
  }

  @Override public void onStart() {

  }
}
