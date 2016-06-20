package com.ibuildapp.romanblack.VideoPlugin.api.youtubeapi;


import com.ibuildapp.romanblack.VideoPlugin.api.youtubeapi.model.YouTubeResponse;

import rx.Observable;

public class YouTubeApi {
    public static final String YOUTUBE_KEY = "AIzaSyBysCzHNvYFGV9e2lTMg6k3faN8BwsDLpA";
    public static final String YOUTUBE_PART = "snippet,contentDetails";

    private YouTubeApiRetrofit innerApi;

    public YouTubeApi(){
        innerApi = new YouTubeApiService().getYouTubeApi();
    }

    public Observable<YouTubeResponse> getVideoInfo(String videoId){
        return innerApi.getVideoInfo(videoId, YOUTUBE_KEY, YOUTUBE_PART);
    }
}
