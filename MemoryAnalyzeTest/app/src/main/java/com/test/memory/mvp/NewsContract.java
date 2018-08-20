package com.test.memory.mvp;

import com.test.memory.bean.NewsInfo;
import java.util.List;

/**
 * @author hesong
 * @time 2018/1/17
 * @desc
 */

public interface NewsContract {

  interface View extends IView {
    void updateList(boolean isRefresh, List<NewsInfo> data);

    void error(Throwable throwable);
  }

  interface Presenter extends IPresenter<View> {
    void loadData(int page);
  }

}
