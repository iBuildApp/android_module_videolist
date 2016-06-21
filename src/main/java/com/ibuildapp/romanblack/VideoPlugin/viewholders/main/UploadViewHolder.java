package com.ibuildapp.romanblack.VideoPlugin.viewholders.main;

import android.view.View;

import com.bumptech.glide.Glide;
import com.ibuildapp.romanblack.VideoPlugin.model.VideoItem;
import com.ibuildapp.romanblack.VideoPlugin.utils.DateUtils;

import java.util.ArrayList;


public class UploadViewHolder extends MainViewHolder{
    public UploadViewHolder(View v) {
        super(v);
    }

    @Override
    public void postView(int position, final ArrayList<VideoItem> items) {
        final VideoItem currentItem = items.get(position);
        Glide.with(thumbImageView.getContext()).load(currentItem.getCoverUrl()).dontAnimate().into(thumbImageView);

        final String agoString = DateUtils.getAgoDateWithAgo(postTime.getContext(), items.get(position).getCreationLong());

        postTime.setText(agoString);
        durationLayout.setVisibility(View.VISIBLE);
        durationText.setText(currentItem.getXmlDuration());
    }
}
