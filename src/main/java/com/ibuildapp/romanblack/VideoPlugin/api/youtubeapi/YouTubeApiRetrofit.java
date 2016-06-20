package com.ibuildapp.romanblack.VideoPlugin.api.youtubeapi;


import com.ibuildapp.romanblack.VideoPlugin.api.youtubeapi.model.YouTubeResponse;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

public interface YouTubeApiRetrofit {

    @GET("/youtube/v3/videos")
    Observable<YouTubeResponse> getVideoInfo(@Query("id") String videoId,
                                             @Query("key") String key,
                                             @Query("part") String part);
}
