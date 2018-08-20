package com.test.memory.model;

import com.test.memory.api.NewsApi;
import com.test.memory.api.NewsUtils;
import com.test.memory.bean.NewsDetailInfo;
import com.test.memory.bean.NewsInfo;
import com.test.memory.callback.OnDataCallback;
import com.test.memory.callback.OnListDataCallback;
import com.test.memory.util.RetryWithDelayFunc;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import java.util.List;
import java.util.Map;

public class NewsModel extends BaseModel {
  private static final String HEAD_LINE_NEWS = "T1348647909107";
  private static final String NEWS_URL = "https://c.3g.163.com/";
  private static final String HTML_IMG_TEMPLATE = "<img src=\"http\" />";
  // 递增页码
  private static final int INCREASE_PAGE = 20;

  private String mNewsType;

  public void setNewsType(String newsType) {
    mNewsType = newsType;
  }

  public Disposable loadData(final int page, final OnListDataCallback<NewsInfo> callback) {
    String type;
    if (mNewsType.equals(HEAD_LINE_NEWS)) {
      type = "headline";
    } else {
      type = "list";
    }

    return getApiService(NEWS_URL, NewsApi.class).getNewsList(type, mNewsType, page * INCREASE_PAGE)
        .retryWhen(new RetryWithDelayFunc())
        .subscribeOn(Schedulers.io())
        .flatMap(new Function<Map<String, List<NewsInfo>>, Flowable<NewsInfo>>() {
          @Override
          public Flowable<NewsInfo> apply(@NonNull Map<String, List<NewsInfo>> newsListMap)
              throws Exception {
            return Flowable.fromIterable(newsListMap.get(mNewsType));
          }
        })
        .filter(new Predicate<NewsInfo>() {
          @Override public boolean test(@NonNull NewsInfo info) throws Exception {
            if (NewsUtils.isAbNews(info) || NewsUtils.isNewsPhotoSet(info.getSkipType())) {
              return false;
            }

            return true;
          }
        })
        .observeOn(AndroidSchedulers.mainThread())
        .toList()
        .subscribe(new Consumer<List<NewsInfo>>() {
          @Override public void accept(@NonNull List<NewsInfo> newsList) throws Exception {
            if (callback != null) {
              callback.onSuccess(newsList);
            }
          }
        }, new Consumer<Throwable>() {
          @Override public void accept(@NonNull Throwable throwable) throws Exception {
            if (callback != null) {
              callback.onError(throwable);
            }
          }
        });
  }

  public Disposable loadData(final String newsId, final OnDataCallback<NewsDetailInfo> callback) {
    return getApiService(NEWS_URL, NewsApi.class).getNewsDetail(newsId)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .flatMap(new Function<Map<String, NewsDetailInfo>, Flowable<NewsDetailInfo>>() {
          @Override
          public Flowable<NewsDetailInfo> apply(Map<String, NewsDetailInfo> newsDetailMap) {
            return Flowable.just(newsDetailMap.get(newsId));
          }
        })
        .doOnNext(new Consumer<NewsDetailInfo>() {
          @Override public void accept(@NonNull NewsDetailInfo newsDetailInfo) throws Exception {
            _handleRichTextWithImg(newsDetailInfo);
          }
        })
        .subscribe(new Consumer<NewsDetailInfo>() {
          @Override public void accept(@NonNull NewsDetailInfo info) throws Exception {
            if (callback != null) {
              callback.onSuccess(info);
            }
          }
        }, new Consumer<Throwable>() {
          @Override public void accept(@NonNull Throwable throwable) throws Exception {
            if (callback != null) {
              callback.onError(throwable);
            }
          }
        });
  }

  /**
   * 处理富文本包含图片的情况
   *
   * @param newsDetailBean 原始数据
   */
  private void _handleRichTextWithImg(NewsDetailInfo newsDetailBean) {
    if (newsDetailBean.getImg() != null && newsDetailBean.getImg().size() > 0) {
      String body = newsDetailBean.getBody();
      for (NewsDetailInfo.ImgEntity imgEntity : newsDetailBean.getImg()) {
        String ref = imgEntity.getRef();
        String src = imgEntity.getSrc();
        String img = HTML_IMG_TEMPLATE.replace("http", src);
        body = body.replaceAll(ref, img);
      }
      newsDetailBean.setBody(body);
    }
  }
}
