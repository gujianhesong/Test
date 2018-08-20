package com.test.memory.fragment;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import com.github.jdsjlzx.interfaces.OnItemClickListener;
import com.github.jdsjlzx.interfaces.OnItemLongClickListener;
import com.test.memory.activity.NewsDetailActivity;
import com.test.memory.adapter.BaseAdapter;
import com.test.memory.adapter.NewsAdapter;
import com.test.memory.base.BaseListFragment;
import com.test.memory.bean.NewsInfo;
import com.test.memory.mvp.NewsContract;
import com.test.memory.mvp.NewsPresenter;
import com.test.memory.util.NetWorkUtil;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gujian on 2018-08-12.
 */

public class NewsListFragment extends BaseListFragment<NewsPresenter>
    implements NewsContract.View {

  private List<NewsInfo> mDatas = new ArrayList<>();
  private int mPage;
  private boolean mFirstRefresh = true;
  private int mCurPage;
  private String mNewsType;

  public static NewsListFragment newInstance() {
    return new NewsListFragment();
  }

  @Override protected NewsPresenter createPresenter() {
    NewsPresenter presenter = new NewsPresenter();
    presenter.setNewsType(mNewsType);
    return presenter;
  }

  @Override protected void initViews(View view, Bundle savedInstanceState) {
    super.initViews(view, savedInstanceState);

    setOnItemClickListener(new OnItemClickListener() {
      @Override public void onItemClick(View view, int position) {

        NewsInfo info = mDatas.get(position);
        if (info != null) {
          NewsDetailActivity.open(mContext, info.getPostid(), info.getTitle());
        }
      }
    });
  }

  @Override protected void initData() {
  }

  @Override protected void onLazyLoad() {
    if (mDatas.isEmpty()) {
      mRecyclerView.forceToRefresh();
    } else {
    }
  }

  @Override protected BaseAdapter<NewsInfo, NewsAdapter.ViewHolder> generateAdapter() {
    NewsAdapter adapter = new NewsAdapter(mContext, mDatas);
    return adapter;
  }

  @Override public void onRefresh() {
    if (!NetWorkUtil.isNetWorkAvailable(mContext)) {
      showToast("网络不可用");
      return;
    }

    mCurPage = 0;
    if(mPresenter != null){
      mPresenter.loadData(mCurPage);
    }else{
      Log.e("TAG", "isFragmentViewCreated :" + isFragmentViewCreated);
    }
  }

  @Override public void onLoadMore() {
    if (!NetWorkUtil.isNetWorkAvailable(mContext)) {
      showToast("网络不可用");
      return;
    }

    if(mPresenter != null){
      mPresenter.loadData(mCurPage);
    }else {
      Log.e("TAG", "isFragmentViewCreated :" + isFragmentViewCreated);
    }
  }

  @Override public void updateList(boolean isRefresh, List<NewsInfo> list) {
    if (isRefresh) {
      mDatas.clear();
    }

    if (list != null && list.size() > 0) {
      mCurPage++;
      mDatas.addAll(list);
      notifyCompleteRefresh(list.size());
    } else {
      notifyCompleteRefresh(mDatas.size());
    }
  }

  @Override public void error(Throwable throwable) {
    showToast(throwable.getMessage());
  }

  public NewsListFragment setNewsType(String newsType) {
    mNewsType = newsType;
    return this;
  }
}