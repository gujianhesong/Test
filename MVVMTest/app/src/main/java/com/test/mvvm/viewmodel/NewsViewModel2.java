package com.test.mvvm.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import com.test.mvvm.api.NewsApi;
import com.test.mvvm.api.NewsUtils;
import com.test.mvvm.api.RetrofitClient;
import com.test.mvvm.bean.ListResult;
import com.test.mvvm.bean.NewsDetailInfo;
import com.test.mvvm.bean.NewsInfo;
import com.test.mvvm.bean.Response;
import com.test.mvvm.util.RetryWithDelayFunc;

import org.reactivestreams.Subscription;

import java.util.List;
import java.util.Map;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

public class NewsViewModel2 extends AndroidViewModel {
    private MutableLiveData<Response<ListResult<List<NewsInfo>>>> mNewsInfoLiveData = new MutableLiveData<>();
    private MutableLiveData<Response<NewsDetailInfo>> mNewsDetailInfoLiveData = new MutableLiveData<>();
    private CompositeDisposable mDisposable = new CompositeDisposable();

    private static final String HEAD_LINE_NEWS = "T1348647909107";
    private static final String NEWS_URL = "https://c.3g.163.com/";
    private static final String HTML_IMG_TEMPLATE = "<img src=\"http\" />";
    // 递增页码
    private static final int INCREASE_PAGE = 20;

    public NewsViewModel2(@NonNull Application application) {
        super(application);
    }

    public void requestNewsInfo(final String newsType, final int page) {
        String type;
        if (newsType.equals(HEAD_LINE_NEWS)) {
            type = "headline";
        } else {
            type = "list";
        }

        mDisposable.add(RetrofitClient.getInstance().getApiService(NEWS_URL, NewsApi.class).getNewsList(type, newsType, page * INCREASE_PAGE)
                .retryWhen(new RetryWithDelayFunc())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(new Consumer<Subscription>() {
                    @Override
                    public void accept(@io.reactivex.annotations.NonNull Subscription subscription) throws Exception {
                        mNewsInfoLiveData.setValue(Response.loading());
                    }
                })
                .flatMap(new io.reactivex.functions.Function<Map<String, List<NewsInfo>>, Flowable<NewsInfo>>() {
                    @Override
                    public Flowable<NewsInfo> apply(@io.reactivex.annotations.NonNull Map<String, List<NewsInfo>> newsListMap)
                            throws Exception {
                        return Flowable.fromIterable(newsListMap.get(newsType));
                    }
                })
                .filter(new Predicate<NewsInfo>() {
                    @Override public boolean test(@io.reactivex.annotations.NonNull NewsInfo info) throws Exception {
                        if (NewsUtils.isAbNews(info) || NewsUtils.isNewsPhotoSet(info.getSkipType())) {
                            return false;
                        }

                        return true;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .toList()
                .subscribe(new Consumer<List<NewsInfo>>() {
                    @Override public void accept(@io.reactivex.annotations.NonNull List<NewsInfo> newsList) throws Exception {
                        ListResult<List<NewsInfo>> result = new ListResult<List<NewsInfo>>();
                        result.setData(newsList);
                        result.setPage(page);

                        mNewsInfoLiveData.setValue(Response.success(result));
                    }
                }, new Consumer<Throwable>() {
                    @Override public void accept(@io.reactivex.annotations.NonNull Throwable throwable) throws Exception {
                        mNewsInfoLiveData.setValue(Response.error(throwable));
                    }
                }));

    }

    public void requestNewsDetailInfo(final String newsId) {
        mDisposable.add(RetrofitClient.getInstance().getApiService(NEWS_URL, NewsApi.class).getNewsDetail(newsId)
                .retryWhen(new RetryWithDelayFunc())
                .flatMap(new io.reactivex.functions.Function<Map<String, NewsDetailInfo>, Flowable<NewsDetailInfo>>() {
                    @Override
                    public Flowable<NewsDetailInfo> apply(Map<String, NewsDetailInfo> newsDetailMap) {
                        return Flowable.just(newsDetailMap.get(newsId));
                    }
                })
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(new Consumer<Subscription>() {
                    @Override
                    public void accept(@io.reactivex.annotations.NonNull Subscription subscription) throws Exception {
                        mNewsDetailInfoLiveData.setValue(Response.loading());
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<NewsDetailInfo>() {
                    @Override public void accept(@io.reactivex.annotations.NonNull NewsDetailInfo bean) throws Exception {
                        mNewsDetailInfoLiveData.setValue(Response.success(bean));
                    }
                }, new Consumer<Throwable>() {
                    @Override public void accept(@io.reactivex.annotations.NonNull Throwable throwable) throws Exception {
                        mNewsDetailInfoLiveData.setValue(Response.error(throwable));
                    }
                }));
    }

    public LiveData<Response<ListResult<List<NewsInfo>>>> getNewsInfoLiveData() {
        return mNewsInfoLiveData;
    }

    public LiveData<Response<NewsDetailInfo>> getNewsDetailInfoLiveData() {
        return mNewsDetailInfoLiveData;
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        if (mDisposable != null) {
            mDisposable.clear();
        }
    }
}
