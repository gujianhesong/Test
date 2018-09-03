package com.test.memory.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.github.jdsjlzx.interfaces.OnItemClickListener;
import com.test.memory.activity.NewsDetailActivity;
import com.test.memory.adapter.BaseAdapter;
import com.test.memory.adapter.NewsAdapter;
import com.test.memory.base.BaseListFragment;
import com.test.memory.bean.NewsInfo;
import com.test.memory.common.ControInfos;
import com.test.memory.mvp.NewsContract;
import com.test.memory.mvp.NewsPresenter;
import com.test.memory.util.NetWorkUtil;
import com.test.memory.util.ReflectUtil;
import com.test.memory.widget.LeakAnimView;

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

  private LeakAnimView animView;

  public static NewsListFragment newInstance() {
    return new NewsListFragment();
  }

  @Override protected NewsPresenter createPresenter() {
    NewsPresenter presenter = new NewsPresenter();
    presenter.setNewsType(mNewsType);
    return presenter;
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    if(ControInfos.testMemory){
      //如果测试内存泄露问题，则执行
      animView = new LeakAnimView(view.getContext());
      RelativeLayout parent = (RelativeLayout) view;
      RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);
      params.addRule(RelativeLayout.CENTER_IN_PARENT);
      parent.addView(animView, params);
      animView.start();
    }

  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();

    if(ControInfos.testMemory){
      //如果测试内存泄露问题，则执行
      if(animView != null && animView.getParent() != null){
        Log.e("NewsListFragment", "remove pre, animView parent : " + animView.getParent());
        RelativeLayout parent = (RelativeLayout) animView.getParent();
        animView.cancel();
        parent.removeView(animView);

        //这里通过反射主动调用RelativeLayout的sortChildren方法，达到清除animView被RelativeLayout$DependencyGraph$Node持有引用的问题
        if(ControInfos.testMemoryOptimize){
          ReflectUtil.invokeMethod(parent.getClass().getName(), "sortChildren", parent, null, new Object[]{});
        }

        Log.e("NewsListFragment", "remove post, animView parent : " + animView.getParent());
        Log.e("NewsListFragment", "remove post, parent size : " + parent.getChildCount());

        animView = null;
      }
    }
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