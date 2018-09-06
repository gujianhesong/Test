package com.test.mvvm.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import com.test.mvvm.R;
import com.test.mvvm.proxy.NewsDetailContent;

public class NewsDetailActivity extends AppCompatActivity {

  private static final String KEY_NEWS_ID = "news_id";
  private static final String KEY_NEWS_TITLE = "news_title";

  Toolbar toolbar;
  NestedScrollView nest;

  private NewsDetailContent detailContent;

  String newsId;
  String title;

  public static void open(Context context, String newsId, String newsTitle) {
    Intent intent = new Intent(context, NewsDetailActivity.class);
    intent.putExtra(KEY_NEWS_ID, newsId);
    intent.putExtra(KEY_NEWS_TITLE, newsTitle);
    context.startActivity(intent);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_news_detail);

    newsId = getIntent().getStringExtra(KEY_NEWS_ID);
    title = getIntent().getStringExtra(KEY_NEWS_TITLE);

    detailContent = new NewsDetailContent(this);

    initView();

    requestData();
  }

  private void initView() {
    toolbar = (Toolbar) findViewById(R.id.toolbar);
    nest = (NestedScrollView) findViewById(R.id.nest);

    toolbar.setTitle(title);
    setSupportActionBar(toolbar);
    ActionBar actionBar = getSupportActionBar();
    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        NewsDetailActivity.this.onBackPressed();
      }
    });
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true);
    }

    nest.removeAllViews();
    nest.addView(detailContent.getView());
  }

  private void requestData() {
    detailContent.requestData(newsId);
  }
}
