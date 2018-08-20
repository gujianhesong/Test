package com.test.memory.mvp;

import com.test.memory.bean.NewsInfo;
import com.test.memory.callback.OnListDataCallback;
import com.test.memory.model.NewsModel;
import java.util.List;

/**
 * Created by gujian on 2018-08-11.
 */
public class NewsPresenter extends BaseRxJavaPresenter<NewsContract.View>
    implements NewsContract.Presenter {

  private NewsModel model;

  public NewsPresenter() {
    model = new NewsModel();
  }

  @Override public void onStart() {
  }

  @Override public void loadData(final int page) {

    addDisposable(model.loadData(page, new OnListDataCallback<NewsInfo>() {
      @Override public void onSuccess(List<NewsInfo> list) {
        if(mView != null){
          mView.updateList(page == 0, list);
        }
      }

      @Override public void onError(Throwable throwable) {
        if(mView != null){
          mView.error(throwable);
        }
      }
    }));
  }

  public void setNewsType(String newsType) {
    if(model != null){
      model.setNewsType(newsType);
    }
  }

}
