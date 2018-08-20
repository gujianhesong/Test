package com.test.memory.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseAdapter<T, VH extends RecyclerView.ViewHolder>
    extends RecyclerView.Adapter<VH> {
  protected List<T> mList;
  private List<T> backList;
  protected List<T> emptyList = new ArrayList<>();
  protected Context mContext;

  public BaseAdapter(Context context, List<T> infoList) {
    this.mContext = context;
    this.mList = infoList;
  }

  public List<T> getData() {
    return mList;
  }

  @Override public int getItemCount() {
    return mList != null ? mList.size() : 0;
  }

  public void onViewCreate(){
    if(mList == emptyList){
      mList = backList;
      notifyDataSetChanged();
    }
  }

  public void onViewDestory(){
    backList = mList;
    mList = emptyList;
    notifyDataSetChanged();
  }

}
