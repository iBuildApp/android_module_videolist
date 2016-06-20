package com.ibuildapp.romanblack.VideoPlugin.viewholders.replies;


import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ibuildapp.romanblack.VideoPlugin.R;
import com.ibuildapp.romanblack.VideoPlugin.api.ibaapi.model.CommentData;
import com.ibuildapp.romanblack.VideoPlugin.utils.DateUtils;
import com.ibuildapp.romanblack.VideoPlugin.utils.Statics;
import com.restfb.util.StringUtils;

public class RepliesItemViewHolder extends RepliesViewHolder{

    public ImageView avatar;
    public TextView author;
    public TextView message;

    public View separator;
    public TextView date;

    public RepliesItemViewHolder(View itemView) {
        super(itemView);
        avatar = (ImageView) itemView.findViewById(R.id.video_plugin_replies_item_avatar);
        author = (TextView) itemView.findViewById(R.id.video_plugin_replies_item_author);
        message = (TextView) itemView.findViewById(R.id.video_plugin_replies_item_message);

        separator = itemView.findViewById(R.id.video_plugin_replies_item_separator);
        date = (TextView) itemView.findViewById(R.id.video_plugin_replies_item_date);
    }

    @Override
    public void bind(CommentData currentData) {

        if (StringUtils.isBlank(currentData.getAvatar()))
            avatar.setImageResource(R.drawable.video_plugin_profile_avatar);
        else Glide.with(avatar.getContext()).load(currentData.getAvatar()).dontAnimate().into(avatar);

        author.setTextColor(Statics.color3);
        author.setText(currentData.getUsername());

        message.setTextColor(Statics.color4);
        message.setText(currentData.getText());

        if (!Statics.isLight)
            separator.setBackgroundColor(Color.parseColor("#4DFFFFFF"));
        else separator.setBackgroundColor(Color.parseColor("#4D000000"));

        date.setTextColor(Statics.color4);
        date.setText(DateUtils.getAgoDate(date.getContext(), Long.valueOf(currentData.getCreate())));
    }
}
