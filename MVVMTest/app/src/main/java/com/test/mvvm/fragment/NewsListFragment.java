package com.test.mvvm.fragment;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.github.jdsjlzx.interfaces.OnItemClickListener;
import com.test.mvvm.activity.NewsDetailActivity;
import com.test.mvvm.adapter.BaseAdapter;
import com.test.mvvm.adapter.NewsAdapter;
import com.test.mvvm.base.BaseListFragment;
import com.test.mvvm.bean.ListResult;
import com.test.mvvm.bean.NewsInfo;
import com.test.mvvm.bean.Response;
import com.test.mvvm.ext.MyLifecycleOwner;
import com.test.mvvm.ext.ViewModelProvidersExt;
import com.test.mvvm.util.NetWorkUtil;
import com.test.mvvm.viewmodel.NewsViewModel;
import com.test.mvvm.viewmodel.NewsViewModel2;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gujian on 2018-08-12.
 */

public class NewsListFragment extends BaseListFragment {
    private static final String TAG = NewsListFragment.class.getSimpleName();

    private List<NewsInfo> mDatas = new ArrayList<>();
    private int mCurPage;
    private String mNewsType;

    public static NewsListFragment newInstance() {
        return new NewsListFragment();
    }

    @Override
    protected void initViews(View view, Bundle savedInstanceState) {
        super.initViews(view, savedInstanceState);

        setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                NewsInfo info = mDatas.get(position);
                if (info != null) {
                    NewsDetailActivity.open(mContext, info.getPostid(), info.getTitle());
                }
            }
        });
    }

    @Override
    protected void initData() {
    }

    @Override
    protected void onLazyLoad() {
        if (mDatas.isEmpty()) {
            mRecyclerView.forceToRefresh();
        } else {
        }
    }

    @Override
    protected BaseAdapter<NewsInfo, NewsAdapter.ViewHolder> generateAdapter() {
        NewsAdapter adapter = new NewsAdapter(mContext, mDatas);
        return adapter;
    }

    @Override
    public void onRefresh() {
        if (!NetWorkUtil.isNetWorkAvailable(mContext)) {
            showToast("网络不可用");
            return;
        }

        mCurPage = 0;

        requestNewsInfo(mCurPage);
    }

    @Override
    public void onLoadMore() {
        if (!NetWorkUtil.isNetWorkAvailable(mContext)) {
            showToast("网络不可用");
            return;
        }

        //ViewModel使用的两种方式
        requestNewsInfo(mCurPage);
        //requestNewsInfo2(mCurPage);
    }

    /**
     * 第一种使用方式
     * @param page
     */
    private void requestNewsInfo(int page) {
        ViewModelProvidersExt.of(this).get(NewsViewModel.class)
                .requestNewsInfo(mNewsType, page)
                .observe(MyLifecycleOwner.getInstance(), new Observer<Response<ListResult<List<NewsInfo>>>>() {
                    @Override
                    public void onChanged(@Nullable Response<ListResult<List<NewsInfo>>> response) {
                        switch (response.status) {
                            case SUCCESS:

                                //成功
                                updateList(response.data);

                                break;
                            case ERROR:

                                //错误
                                Log.e(TAG, "错误:" + response.error.getMessage());
                                showToast(response.error.getMessage());

                                break;
                            case LOADING:
                                break;
                        }
                    }
                });
    }

    /**
     * 第二种使用方式
     * @param page
     */
    private void requestNewsInfo2(int page) {
        ViewModelProviders.of(this).get(NewsViewModel2.class)
                .requestNewsInfo(mNewsType, page);
        ViewModelProviders.of(this).get(NewsViewModel2.class).getNewsInfoLiveData().observe(this, new Observer<Response<ListResult<List<NewsInfo>>>>() {
            @Override
            public void onChanged(@Nullable Response<ListResult<List<NewsInfo>>> response) {
                switch (response.status) {
                    case SUCCESS:

                        //成功
                        updateList(response.data);

                        break;
                    case ERROR:

                        //错误
                        Log.e(TAG, "错误:" + response.error.getMessage());
                        showToast(response.error.getMessage());

                        break;
                    case LOADING:
                        break;
                }
            }
        });
    }

    private void updateList(ListResult<List<NewsInfo>> result) {
        if (result.getPage() == 0) {
            mDatas.clear();
        }

        List<NewsInfo> list = result.getData();
        if (list != null && list.size() > 0) {
            mCurPage++;
            mDatas.addAll(list);
            notifyCompleteRefresh(list.size());
        } else {
            notifyCompleteRefresh(mDatas.size());
        }
    }

    public NewsListFragment setNewsType(String newsType) {
        mNewsType = newsType;
        return this;
    }
}