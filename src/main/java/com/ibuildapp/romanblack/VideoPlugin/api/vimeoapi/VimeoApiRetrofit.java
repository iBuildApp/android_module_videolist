package com.ibuildapp.romanblack.VideoPlugin.api.vimeoapi;


import com.ibuildapp.romanblack.VideoPlugin.api.vimeoapi.model.VimeoResponse;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

public interface VimeoApiRetrofit {
    @GET("/api/oembed.json")
    Observable<VimeoResponse> getVideoInfo(@Query("url") String videoUrl);
}
