package com.ibuildapp.romanblack.VideoPlugin.viewholders.replies;


import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.ibuildapp.romanblack.VideoPlugin.api.ibaapi.model.CommentData;

public abstract class RepliesViewHolder extends RecyclerView.ViewHolder{
    public RepliesViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void bind(CommentData data);
}
