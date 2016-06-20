package com.ibuildapp.romanblack.VideoPlugin.api.ibaapi;


import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class IbaApiService {
    private  String baseUrl  =  "";

    public IbaApiService(String domain){
        baseUrl = domain;
        if(!baseUrl.contains("http"))
            baseUrl = "http://"+baseUrl;

        baseUrl+='/';
    }

    public  IbaApiRetrofit getIbaApiRetrofit(){

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();

        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient)
                .build().create(IbaApiRetrofit.class);
    }
}
