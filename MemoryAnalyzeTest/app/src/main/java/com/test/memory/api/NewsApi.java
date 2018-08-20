package com.test.memory.api;

import com.test.memory.bean.NewsDetailInfo;
import com.test.memory.bean.NewsInfo;
import io.reactivex.Flowable;
import java.util.List;
import java.util.Map;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;

public interface NewsApi {

  // 避免出现 HTTP 403 Forbidden，参考：http://stackoverflow.com/questions/13670692/403-forbidden-with-java-but-not-web-browser
  static final String AVOID_HTTP403_FORBIDDEN =
      "User-Agent: Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11";

  /**
   * 获取新闻列表
   * eg: http://c.m.163.com/nc/article/headline/T1348647909107/60-20.html
   * http://c.m.163.com/nc/article/list/T1348647909107/60-20.html
   *
   * @param type 新闻类型
   * @param id 新闻ID
   * @param startPage 起始页码
   */
  @Headers(AVOID_HTTP403_FORBIDDEN) @GET("nc/article/{type}/{id}/{startPage}-20.html")
  Flowable<Map<String, List<NewsInfo>>> getNewsList(@Path("type") String type,
      @Path("id") String id, @Path("startPage") int startPage);

  /**
   * 获取新闻详情
   * eg: http://c.3g.163.com/nc/article/BV56RVG600011229/full.html
   *
   * @param newsId 专题ID
   */
  @Headers(AVOID_HTTP403_FORBIDDEN) @GET("nc/article/{newsId}/full.html")
  Flowable<Map<String, NewsDetailInfo>> getNewsDetail(@Path("newsId") String newsId);
}
