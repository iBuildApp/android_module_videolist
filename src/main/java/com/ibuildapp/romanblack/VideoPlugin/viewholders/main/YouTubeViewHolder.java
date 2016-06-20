package com.ibuildapp.romanblack.VideoPlugin.viewholders.main;

import android.view.View;

import com.bumptech.glide.Glide;
import com.ibuildapp.romanblack.VideoPlugin.R;
import com.ibuildapp.romanblack.VideoPlugin.api.youtubeapi.YouTubeApi;
import com.ibuildapp.romanblack.VideoPlugin.api.youtubeapi.YouTubeUtils;
import com.ibuildapp.romanblack.VideoPlugin.api.youtubeapi.model.ContentDetails;
import com.ibuildapp.romanblack.VideoPlugin.api.youtubeapi.model.Snippet;
import com.ibuildapp.romanblack.VideoPlugin.api.youtubeapi.model.YouTubeResponse;
import com.ibuildapp.romanblack.VideoPlugin.model.VideoItem;
import com.ibuildapp.romanblack.VideoPlugin.utils.DateUtils;
import com.ibuildapp.romanblack.VideoPlugin.utils.rx.RxUtils;
import com.ibuildapp.romanblack.VideoPlugin.utils.rx.SimpleSubscriber;

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

        if (currentItem.getResponse() == null) {
            thumbImageView.setImageBitmap(null);

            String videoId = YouTubeUtils.getVideoId(currentItem.getUrl());
            api.getVideoInfo(videoId)
                    .compose(RxUtils.<YouTubeResponse>applyCustomSchedulers(Schedulers.io(), Schedulers.computation()))
                    .subscribe(new SimpleSubscriber<YouTubeResponse>() {

                        @Override
                        public void onNext(YouTubeResponse youTubeResponse) {
                            currentItem.setResponse(youTubeResponse);
                            onYouTubeDataLoad(youTubeResponse);
                        }
                    });
        } else
            onYouTubeDataLoad(currentItem.getResponse());
    }

    private void onYouTubeDataLoad(YouTubeResponse response) {
        final String url = response.getItems()[0].getSnippet().getThumbnails().getHigh().getUrl();
        Snippet snippet = response.getItems()[0].getSnippet();
        snippet.setPublishedDate(DateUtils.parsYouTubeDate(snippet.getPublishedAt()));
        final String agoString = DateUtils.getAgoDateWithAgo(durationLayout.getContext(), snippet.getPublishedDate().getTime());

        ContentDetails details = response.getItems()[0].getContentDetails();
        final String parsedDuration = DateUtils.parseYouTubeDuration(details.getDuration());
        details.setParsedDuration(parsedDuration);

        durationLayout.post(new Runnable() {
            @Override
            public void run() {
                if (!url.equals(""))
                    Glide.with(durationLayout.getContext()).load(url).dontAnimate().into(thumbImageView);

                postTime.setText(agoString);
                durationText.setText(parsedDuration);
            }
        });
    }
}
