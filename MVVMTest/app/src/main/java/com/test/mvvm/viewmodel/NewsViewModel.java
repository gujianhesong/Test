package com.test.mvvm.viewmodel;

import android.app.Application;
import android.arch.core.util.Function;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.support.annotation.NonNull;

import com.test.mvvm.api.NewsApi;
import com.test.mvvm.api.NewsUtils;
import com.test.mvvm.api.RetrofitClient;
import com.test.mvvm.bean.NewsDetailInfo;
import com.test.mvvm.bean.NewsInfo;
import com.test.mvvm.bean.ListResult;
import com.test.mvvm.bean.Response;
import com.test.mvvm.util.RetryWithDelayFunc;

import org.reactivestreams.Subscription;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

public class NewsViewModel extends AndroidViewModel {
    private LiveData<Response<ListResult<List<NewsInfo>>>> mNewsInfoLiveData;
    private LiveData<Response<NewsDetailInfo>> mNewsDetailInfoLiveData;
    private CompositeDisposable mDisposable = new CompositeDisposable();

    private static final String HEAD_LINE_NEWS = "T1348647909107";
    private static final String NEWS_URL = "https://c.3g.163.com/";
    private static final String HTML_IMG_TEMPLATE = "<img src=\"http\" />";
    // 递增页码
    private static final int INCREASE_PAGE = 20;

    public NewsViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<Response<ListResult<List<NewsInfo>>>> requestNewsInfo(String newsType, int page) {
        requestNewsInfoInner(newsType, page);

        return mNewsInfoLiveData;
    }

    private void requestNewsInfoInner(String newsType, int page) {

        MutableLiveData<HashMap<String, Object>> requestParams = new MutableLiveData<>();
        HashMap<String, Object> map = new HashMap<>();
        map.put("newsType", newsType);
        map.put("page", page);
        requestParams.setValue(map);

        mNewsInfoLiveData = Transformations.switchMap(requestParams, new Function<HashMap<String, Object>, LiveData<Response<ListResult<List<NewsInfo>>>>>() {

            @Override
            public LiveData<Response<ListResult<List<NewsInfo>>>> apply(HashMap<String, Object> map) {
                final MutableLiveData<Response<ListResult<List<NewsInfo>>>> applyData = new MutableLiveData<>();

                final String newsType = (String) map.get("newsType");
                final int page = (int) map.get("page");

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
                                applyData.setValue(Response.loading());
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

                                applyData.setValue(Response.success(result));
                            }
                        }, new Consumer<Throwable>() {
                            @Override public void accept(@io.reactivex.annotations.NonNull Throwable throwable) throws Exception {
                                applyData.setValue(Response.error(throwable));
                            }
                        }));

                return applyData;
            }
        });
    }

    public LiveData<Response<NewsDetailInfo>> requestNewsDetailInfo(String newsId) {
        requestNewsDetailInfoInner(newsId);

        return mNewsDetailInfoLiveData;
    }

    private void requestNewsDetailInfoInner(String newsId) {

        MutableLiveData<String> requestParams = new MutableLiveData<>();
        requestParams.setValue(newsId);

        mNewsDetailInfoLiveData = Transformations.switchMap(requestParams, new Function<String, LiveData<Response<NewsDetailInfo>>>() {

            @Override
            public LiveData<Response<NewsDetailInfo>> apply(final String newsId) {
                final MutableLiveData<Response<NewsDetailInfo>> applyData = new MutableLiveData<>();

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
                                applyData.setValue(Response.loading());
                            }
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<NewsDetailInfo>() {
                            @Override public void accept(@io.reactivex.annotations.NonNull NewsDetailInfo bean) throws Exception {
                                applyData.setValue(Response.success(bean));
                            }
                        }, new Consumer<Throwable>() {
                            @Override public void accept(@io.reactivex.annotations.NonNull Throwable throwable) throws Exception {
                                applyData.setValue(Response.error(throwable));
                            }
                        }));

                return applyData;
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        if (mDisposable != null) {
            mDisposable.clear();
        }
    }
}
