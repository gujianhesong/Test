package com.test.memory.base;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;
import com.github.jdsjlzx.interfaces.OnItemClickListener;
import com.github.jdsjlzx.interfaces.OnItemLongClickListener;
import com.github.jdsjlzx.interfaces.OnLoadMoreListener;
import com.github.jdsjlzx.interfaces.OnNetWorkErrorListener;
import com.github.jdsjlzx.interfaces.OnRefreshListener;
import com.github.jdsjlzx.recyclerview.LRecyclerView;
import com.github.jdsjlzx.recyclerview.LRecyclerViewAdapter;
import com.test.memory.R;
import com.test.memory.adapter.BaseAdapter;
import com.test.memory.mvp.IPresenter;
import com.test.memory.widget.RecycleViewDivider;

/**
 * 列表页面Fragment
 * Created by gujian on 2018-08-12.
 */

public abstract class BaseListFragment<T extends IPresenter> extends BaseLazyFragment<T>
    implements OnRefreshListener, OnLoadMoreListener {

  protected LRecyclerView mRecyclerView;

  protected BaseAdapter mAdapter;
  private LRecyclerViewAdapter mLRecyclerViewAdapter;

  protected Context mContext;

  protected int getLayoutId() {
    return R.layout.fragment_base_list;
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    if(mAdapter != null){
      mAdapter.onViewCreate();
    }
  }

  @Override public void onDestroyView() {
    super.onDestroyView();

    if(mAdapter != null){
      mAdapter.onViewDestory();
    }
  }

  @Override protected void initViews(View view, Bundle savedInstanceState) {
    mContext = view.getContext();

    mRecyclerView = view.findViewById(R.id.swipe_target);
    mRecyclerView.setHasFixedSize(true);

    setLayoutManager(mRecyclerView);

    mAdapter = generateAdapter();
    mLRecyclerViewAdapter = new LRecyclerViewAdapter(mAdapter);
    mRecyclerView.setAdapter(mLRecyclerViewAdapter);
    mRecyclerView.setOnRefreshListener(this);
    mRecyclerView.setOnLoadMoreListener(this);
  }

  public void notifyCompleteRefresh(int refreshCount) {
    mRecyclerView.refreshComplete(refreshCount);
    mLRecyclerViewAdapter.notifyDataSetChanged();
  }

  public void showErrorMessage(boolean isRefresh, String message) {
    if (mContext instanceof Activity) {
      Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }

    if (isRefresh) {
      mRecyclerView.refreshComplete(0);
    } else {
      mRecyclerView.refreshComplete(0);
      mRecyclerView.setOnNetWorkErrorListener(new OnNetWorkErrorListener() {
        @Override public void reload() {
          onLoadMore();
        }
      });
    }
  }

  protected void setLayoutManager(LRecyclerView recyclerView) {
    LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
    recyclerView.setLayoutManager(layoutManager);

    mRecyclerView.addItemDecoration(new RecycleViewDivider(mContext));
  }

  public void setOnItemClickListener(OnItemClickListener listener) {
    if (mLRecyclerViewAdapter != null) {
      mLRecyclerViewAdapter.setOnItemClickListener(listener);
    }
  }

  public void setOnItemLongClickListener(OnItemLongClickListener listener) {
    if (mLRecyclerViewAdapter != null) {
      mLRecyclerViewAdapter.setOnItemLongClickListener(listener);
    }
  }

  protected void showToast(String message) {
    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
  }

  protected abstract BaseAdapter generateAdapter();

  public abstract void onLoadMore();

  public abstract void onRefresh();
}
