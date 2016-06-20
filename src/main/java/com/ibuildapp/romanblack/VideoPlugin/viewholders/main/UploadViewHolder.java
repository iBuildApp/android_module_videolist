package com.ibuildapp.romanblack.VideoPlugin.viewholders.main;

import android.view.View;

import com.appbuilder.sdk.android.tools.NetworkUtils;
import com.bumptech.glide.Glide;
import com.ibuildapp.romanblack.VideoPlugin.model.VideoItem;
import com.ibuildapp.romanblack.VideoPlugin.utils.DateUtils;

import java.util.ArrayList;

import wseemann.media.FFmpegMediaMetadataRetriever;


public class UploadViewHolder extends MainViewHolder{
    public UploadViewHolder(View v) {
        super(v);

    }

    @Override
    public void postView(int position, final ArrayList<VideoItem> items) {
        final VideoItem currentItem = items.get(position);
        Glide.with(thumbImageView.getContext()).load(currentItem.getCoverUrl()).dontAnimate().into(thumbImageView);

        if (currentItem.getUploadDuration() == null)
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (NetworkUtils.isOnline(thumbImageView.getContext())) {
                        FFmpegMediaMetadataRetriever mFFmpegMediaMetadataRetriever = new FFmpegMediaMetadataRetriever();
                        mFFmpegMediaMetadataRetriever.setDataSource(currentItem.getUrl());
                        String mVideoDuration = mFFmpegMediaMetadataRetriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION);
                        long mTimeInMilliseconds = Long.parseLong(mVideoDuration);
                        final String durationString = DateUtils.parseStringDurationFromLong(mTimeInMilliseconds);

                        currentItem.setUploadDuration(durationString);
                        thumbImageView.post(new Runnable() {
                            @Override
                            public void run() {
                                durationLayout.setVisibility(View.VISIBLE);
                                durationText.setText(currentItem.getUploadDuration());
                            }
                        });
                    }
                }
            }).start();
        else {
            durationLayout.setVisibility(View.VISIBLE);
            durationText.setText(currentItem.getUploadDuration());
        }
    }
}
