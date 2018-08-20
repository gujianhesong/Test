package com.test.memory.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.test.memory.R;
import com.test.memory.activity.NewsDetailActivity;
import com.test.memory.bean.NewsInfo;
import java.util.List;

public class NewsAdapter extends BaseAdapter<NewsInfo, NewsAdapter.ViewHolder> {

  public NewsAdapter(Context context, List<NewsInfo> infoList) {
    super(context, infoList);
  }

  @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.news_item, parent, false));
  }

  @Override public void onBindViewHolder(final ViewHolder holder, int position) {

    final NewsInfo itemInfo = mList.get(position);

    holder.tvTitle.setText(itemInfo.getTitle());
    holder.tvSource.setText(itemInfo.getSource());
    holder.tvTime.setText(itemInfo.getPtime());

    if (itemInfo.getImgsrc() != null) {
      Glide.with(mContext).load(itemInfo.getImgsrc()).into(holder.ivImage);
    }
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {

    ImageView ivImage;
    TextView tvTitle;
    TextView tvSource;
    TextView tvTime;

    ViewHolder(View itemView) {
      super(itemView);

      ivImage = itemView.findViewById(R.id.iv_image);
      tvTitle = itemView.findViewById(R.id.tv_title);
      tvSource = itemView.findViewById(R.id.tv_source);
      tvTime = itemView.findViewById(R.id.tv_time);
    }
  }
}
