package com.ibuildapp.romanblack.VideoPlugin.api.ibaapi;


import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class IbaApiService {
    private  String baseUrl  =  "";

    public IbaApiService(String domain){
        baseUrl = domain;
  /*      if(!baseUrl.contains("http"))
        {
            baseUrl = "http://"+baseUrl;
        }*/
    /*    else
        {
            if(!baseUrl.contains("https"))
            {
                String tmp = "http";
                 baseUrl = baseUrl.replaceAll(tmp,"https");
            }
        }
*/
        baseUrl+='/';
    }

    public  IbaApiRetrofit getIbaApiRetrofit(){

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .writeTimeout(2000000, TimeUnit.MILLISECONDS )
                .readTimeout(2000000, TimeUnit.MILLISECONDS )
                .connectTimeout(2000000, TimeUnit.MILLISECONDS )
                .build();

        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient)
                .build().create(IbaApiRetrofit.class);
    }
}
