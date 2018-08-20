package com.test.memory.mvp;

import com.test.memory.bean.NewsDetailInfo;

public interface NewsDetailContract {

  interface View extends IView {
    void showNewsDetail(NewsDetailInfo info);

    void error(Throwable throwable);
  }

  interface Presenter extends IPresenter<View> {
    void requestNewsDetailInfo(String newsId);
  }
}
