package com.test.mvvm.proxy;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.test.mvvm.bean.NewsDetailInfo;
import com.test.mvvm.bean.Response;
import com.test.mvvm.viewmodel.NewsViewModel;
import com.test.mvvm.viewmodel.NewsViewModel2;
import com.zzhoujay.richtext.RichText;

/**
 * 注意，这种MVVM架构有它的局限性，不适合非Activity和Fragment的场景，比如全局悬浮窗，就不适合。
 */
public class NewsDetailContent extends TextDetailContent {
    private static final String TAG = NewsDetailContent.class.getSimpleName();

    private NewsDetailInfo mInfo;

    public NewsDetailContent(FragmentActivity context) {
        super(context);
    }

    @Override
    public void onDestoryView() {
        super.onDestoryView();
    }

    public void requestData(final String newsId) {
        if (!(mContext instanceof FragmentActivity)) {
            Toast.makeText(mContext, "mContext need be Activity", Toast.LENGTH_SHORT).show();
            return;
        }
        FragmentActivity activity = (FragmentActivity) mContext;

        //ViewModel使用的两种方式
        requestNewsDetailInfo(activity, newsId);
        //requestNewsDetailInfo2(activity, newsId);
    }

    /**
     * 第一种使用方式
     * @param activity
     * @param newsId
     */
    private void requestNewsDetailInfo(FragmentActivity activity, String newsId){
        ViewModelProviders.of(activity).get(NewsViewModel.class)
                .requestNewsDetailInfo(newsId)
                .observe(activity, new Observer<Response<NewsDetailInfo>>() {
                    @Override
                    public void onChanged(@Nullable Response<NewsDetailInfo> response) {
                        switch (response.status) {
                            case SUCCESS:

                                //成功
                                showNewsDetail(response.data);

                                break;
                            case ERROR:

                                //错误
                                Log.e(TAG, "错误:" + response.error.getMessage());

                                break;
                            case LOADING:
                                break;
                        }
                    }
                });
    }

    /**
     * 第二种使用方式
     * @param activity
     * @param newsId
     */
    private void requestNewsDetailInfo2(FragmentActivity activity, String newsId){
        ViewModelProviders.of(activity).get(NewsViewModel2.class)
                .requestNewsDetailInfo(newsId);
        ViewModelProviders.of(activity).get(NewsViewModel2.class).getNewsDetailInfoLiveData().observe(activity, new Observer<Response<NewsDetailInfo>>() {
            @Override
            public void onChanged(@Nullable Response<NewsDetailInfo> response) {
                switch (response.status) {
                    case SUCCESS:

                        //成功
                        showNewsDetail(response.data);

                        break;
                    case ERROR:

                        //错误
                        Log.e(TAG, "错误:" + response.error.getMessage());

                        break;
                    case LOADING:
                        break;
                }
            }
        });
    }

    private void showNewsDetail(NewsDetailInfo info) {
        mInfo = info;
        if (info != null) {
            if (info.getBody() != null) {
                RichText.from(info.getBody()).into(mTextView);
            }
        }
    }

    public NewsDetailInfo getNewsDetailInfo() {
        return mInfo;
    }
}
