package com.ibuildapp.romanblack.VideoPlugin.viewholders;


import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.ibuildapp.romanblack.VideoPlugin.api.ibaapi.model.CommentsData;
import com.ibuildapp.romanblack.VideoPlugin.details.VideoDetailsActivity;
import com.ibuildapp.romanblack.VideoPlugin.model.VideoItem;

public abstract class DetailsInfoViewHolder extends RecyclerView.ViewHolder {
    public VideoDetailsActivity context;
    public DetailsInfoViewHolder(View itemView, VideoDetailsActivity context) {
        super(itemView);

        this.context = context;
    }

    public abstract void bind(VideoItem item, CommentsData data, int position);
}
