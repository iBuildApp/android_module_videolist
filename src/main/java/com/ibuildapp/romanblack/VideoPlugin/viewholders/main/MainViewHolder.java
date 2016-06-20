package com.ibuildapp.romanblack.VideoPlugin.viewholders.main;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ibuildapp.romanblack.VideoPlugin.R;
import com.ibuildapp.romanblack.VideoPlugin.model.VideoItem;
import com.ibuildapp.romanblack.VideoPlugin.utils.Statics;

import java.util.ArrayList;

public abstract class MainViewHolder extends RecyclerView.ViewHolder {
    public final ImageView thumbImageView;
    public final View itemView;
    public final TextView titleTextView;
    public final LinearLayout shareButton;
    public final View bottomLayout;
    public final TextView shareText;
    public final ImageView shareImageView;
    public final TextView postTime;
    public final View durationLayout;
    public final TextView durationText;
    public final View divider;

    public  MainViewHolder(View itemView){
        super(itemView);
        this.itemView = itemView;

        thumbImageView = (ImageView) itemView.findViewById(R.id.video_plugin_main_item_image);
        shareImageView = (ImageView) itemView.findViewById(R.id.video_plugin_main_list_share_image);
        titleTextView = (TextView) itemView.findViewById(R.id.video_plugin_main_item_title);
        shareButton = (LinearLayout) itemView.findViewById(R.id.video_plugin_main_item_share_layout);

        bottomLayout = itemView.findViewById(R.id.video_plugin_main_item_bottom_layout);
        shareText = (TextView)itemView.findViewById(R.id.video_plugin_main_list_share_caption);
        postTime = (TextView)itemView.findViewById(R.id.video_plugin_main_item_post_time);
        durationLayout = itemView.findViewById(R.id.video_plugin_main_item_duration);
        durationText = (TextView) itemView.findViewById(R.id.video_plugin_main_item_duration_text);
        divider = itemView.findViewById(R.id.video_plugin_main_item_divider);
    }

    public void prefetchView(){
        titleTextView.setTextColor(Statics.color3);

        postTime.setTextColor(Statics.color3);
        shareText.setTextColor(Statics.color3);
        itemView.setBackgroundColor(Statics.color1);
    }


    public abstract void postView(int position, ArrayList<VideoItem> items);
}
