package com.ibuildapp.romanblack.VideoPlugin.viewholders;


import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ibuildapp.romanblack.VideoPlugin.R;
import com.ibuildapp.romanblack.VideoPlugin.api.ibaapi.model.CommentData;
import com.ibuildapp.romanblack.VideoPlugin.api.ibaapi.model.CommentsData;
import com.ibuildapp.romanblack.VideoPlugin.details.VideoDetailsActivity;
import com.ibuildapp.romanblack.VideoPlugin.model.VideoItem;
import com.ibuildapp.romanblack.VideoPlugin.utils.DateUtils;
import com.ibuildapp.romanblack.VideoPlugin.utils.Statics;
import com.restfb.util.StringUtils;

public class DetailsInfoItemViewHolder extends DetailsInfoViewHolder{

    public ImageView avatar;
    public TextView author;
    public TextView message;

    public View separator;
    public TextView date;
    public TextView comments;

    public DetailsInfoItemViewHolder(View itemView, VideoDetailsActivity context) {
        super(itemView, context);

        avatar = (ImageView) itemView.findViewById(R.id.video_plugin_details_info_item_avatar);
        author = (TextView) itemView.findViewById(R.id.video_plugin_details_info_item_author);
        message = (TextView) itemView.findViewById(R.id.video_plugin_details_info_item_message);

        separator = itemView.findViewById(R.id.video_plugin_details_info_item_separator);
        date = (TextView) itemView.findViewById(R.id.video_plugin_details_info_item_date);
        comments = (TextView) itemView.findViewById(R.id.video_plugin_details_info_item_comments);
    }

    @Override
    public void bind(VideoItem item, CommentsData data, final int position) {
        final CommentData currentData = data.getData().get(position-1);

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

        comments.setTextColor(Statics.color5);

        Integer repliesCount = currentData.getTotal_comments();

        if (repliesCount == 0)
            comments.setText(comments.getResources().getString(R.string.video_plugin_add_comment));
        else
        comments.setText(comments.getResources().getQuantityString(R.plurals.numberOfComments,
                repliesCount, repliesCount));

        comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.launchCommentToCommentsActivity(currentData, position);
            }
        });
    }
}
