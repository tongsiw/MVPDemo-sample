package com.siw.basemvp.net;


import com.siw.basemvp.constants.Constants;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by hpw on 16/11/2.
 *
 */

public class RxService {
    private static class OkHttpClientClazz{
        private static final int TIMEOUT_READ = 20;
        private static final int TIMEOUT_CONNECTION = 10;
        private static final HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
        private static final CacheInterceptor cacheInterceptor = new CacheInterceptor();
        private static final  OkHttpClient okHttpClient = new OkHttpClient.Builder()
                //SSL证书
//            .sslSocketFactory(TrustManagers.getSafeOkHttpClient(CommonUtils.getContext(), R.raw.demo,"password","alias"))//需要验证证书
//            .hostnameVerifier(TrustManagers.getHostnameVerifier())
                .sslSocketFactory(TrustManagers.getUnsafeOkHttpClient())//这是信任所有证书
                .hostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)
                //打印日志
                .addInterceptor(interceptor)
                //设置Cache
                .addNetworkInterceptor(cacheInterceptor)//缓存方面需要加入这个拦截器
                .addInterceptor(cacheInterceptor)
                .cache(HttpCache.getCache())
                //time out
                .connectTimeout(TIMEOUT_CONNECTION, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_READ, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_READ, TimeUnit.SECONDS)
                //失败重连
                .retryOnConnectionFailure(true)
                .build();
    }


    public static <T> T createApi(Class<T> clazz) {
        return createApi(clazz, Constants.BaseUrl);
    }

    public static <T> T createApi(Class<T> clazz, String url) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .client(OkHttpClientClazz.okHttpClient)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(clazz);
    }
}
