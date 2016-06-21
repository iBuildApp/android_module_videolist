package com.ibuildapp.romanblack.VideoPlugin.viewholders.main;

import android.view.View;

import com.bumptech.glide.Glide;
import com.ibuildapp.romanblack.VideoPlugin.api.youtubeapi.YouTubeApi;
import com.ibuildapp.romanblack.VideoPlugin.api.youtubeapi.YouTubeUtils;
import com.ibuildapp.romanblack.VideoPlugin.api.youtubeapi.model.YouTubeResponse;
import com.ibuildapp.romanblack.VideoPlugin.model.VideoItem;
import com.ibuildapp.romanblack.VideoPlugin.utils.DateUtils;
import com.ibuildapp.romanblack.VideoPlugin.utils.SerializableUtils;
import com.ibuildapp.romanblack.VideoPlugin.utils.Statics;
import com.ibuildapp.romanblack.VideoPlugin.utils.rx.RxUtils;
import com.ibuildapp.romanblack.VideoPlugin.utils.rx.SimpleSubscriber;

import java.io.File;
import java.util.ArrayList;

import rx.schedulers.Schedulers;


public class YouTubeViewHolder extends MainViewHolder{
    private YouTubeApi api;

    public YouTubeViewHolder(View v, YouTubeApi youTubeApi) {
        super(v);
        this.api = youTubeApi;
    }

    @Override
    public void postView(final int position, final ArrayList<VideoItem> items) {
        final VideoItem currentItem = items.get(position);

        final String agoString = DateUtils.getAgoDateWithAgo(postTime.getContext(), items.get(position).getCreationLong());
        postTime.setText(agoString);
        durationLayout.setVisibility(View.VISIBLE);
        durationText.setText(currentItem.getXmlDuration());

       // Glide.with(durationLayout.getContext()).load(currentItem.getCoverUrl()).dontAnimate().into(thumbImageView);
       /// final String filePath = Statics.getCachePath() + File.separator + String.valueOf(currentItem.getId());
       /* if (fileExists(filePath)){
            YouTubeResponse response = SerializableUtils.readSerializable(filePath);
            currentItem.setResponse(response);
        }*/

        if (currentItem.getResponse() == null) {
            thumbImageView.setImageBitmap(null);

            String videoId = YouTubeUtils.getVideoId(currentItem.getUrl());
            api.getVideoInfo(videoId).subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.newThread())
                    .subscribe(new SimpleSubscriber<YouTubeResponse>() {

                        @Override
                        public void onError(Throwable e) {
                            super.onError(e);
                        }

                        @Override
                        public void onNext(YouTubeResponse youTubeResponse) {
                            currentItem.setResponse(youTubeResponse);
                            //SerializableUtils.saveSerializable(youTubeResponse, filePath);
                            onYouTubeDataLoad(youTubeResponse);
                        }
                    });
        } else
            onYouTubeDataLoad(currentItem.getResponse());
    }

    private boolean fileExists(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }

    private void onYouTubeDataLoad(YouTubeResponse response) {
        final String url = response.getItems()[0].getSnippet().getThumbnails().getHigh().getUrl();

        durationLayout.post(new Runnable() {
            @Override
            public void run() {
                if (!url.equals(""))
                    Glide.with(durationLayout.getContext()).load(url).dontAnimate().into(thumbImageView);
            }
        });
    }
}
