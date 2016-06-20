package com.ibuildapp.romanblack.VideoPlugin.api.vimeoapi;


import com.ibuildapp.romanblack.VideoPlugin.api.vimeoapi.model.VimeoResponse;

import rx.Observable;

public class VimeoApi {
    private VimeoApiRetrofit innerApi;

    public VimeoApi(){
        innerApi = new VimeoApiService().getVimeoApi();
    }

    public Observable<VimeoResponse> getVideoInfo(String videoUrl){
        return innerApi.getVideoInfo(videoUrl);
    }
}
