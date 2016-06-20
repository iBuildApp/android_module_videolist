package com.ibuildapp.romanblack.VideoPlugin.viewholders.replies;


import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ibuildapp.romanblack.VideoPlugin.R;
import com.ibuildapp.romanblack.VideoPlugin.api.ibaapi.model.CommentData;
import com.ibuildapp.romanblack.VideoPlugin.utils.DateUtils;
import com.ibuildapp.romanblack.VideoPlugin.utils.Statics;
import com.restfb.util.StringUtils;

public class RepliesHeaderViewHolder extends RepliesViewHolder{

    public View bottomSeparator;
    public View topSeparator;
    public ImageView avatar;
    public TextView author;
    public TextView message;

    public TextView date;
    public TextView comments;

    public LinearLayout noCommentsLayout;
    public TextView noCommentsTextView;

    public RepliesHeaderViewHolder(View itemView) {
        super(itemView);

        avatar = (ImageView) itemView.findViewById(R.id.video_plugin_replies_header_avatar);
        author = (TextView) itemView.findViewById(R.id.video_plugin_replies_header_author);
        message = (TextView) itemView.findViewById(R.id.video_plugin_replies_header_message);

        topSeparator = itemView.findViewById(R.id.video_plugin_replies_header_top_separator);
        date = (TextView) itemView.findViewById(R.id.video_plugin_replies_header_date);
        comments = (TextView) itemView.findViewById(R.id.video_plugin_replies_header_comments);
        bottomSeparator = itemView.findViewById(R.id.video_plugin_replies_header_bottom_separator);

        noCommentsLayout = (LinearLayout) itemView.findViewById(R.id.video_plugin_replies_header_no_comments_layout);
        noCommentsTextView = (TextView) itemView.findViewById(R.id.video_plugin_replies_header_no_comments);
    }

    @Override
    public void bind(CommentData data) {
        if (StringUtils.isBlank(data.getAvatar()))
            avatar.setImageResource(R.drawable.video_plugin_profile_avatar);
        else Glide.with(avatar.getContext()).load(data.getAvatar()).dontAnimate().into(avatar);

        author.setTextColor(Statics.color3);
        author.setText(data.getUsername());

        message.setTextColor(Statics.color4);
        message.setText(data.getText());

        if (!Statics.isLight) {
            topSeparator.setBackgroundColor(Color.parseColor("#4DFFFFFF"));
            bottomSeparator.setBackgroundColor(Color.parseColor("#4DFFFFFF"));
        } else {
            topSeparator.setBackgroundColor(Color.parseColor("#4D000000"));
            bottomSeparator.setBackgroundColor(Color.parseColor("#4D000000"));
        }

        date.setTextColor(Statics.color4);
        date.setText(DateUtils.getAgoDate(date.getContext(), Long.valueOf(data.getCreate())));

        comments.setTextColor(Statics.color3);

        Integer repliesCount = data.getTotal_comments();

        comments.setText(comments.getResources().getQuantityString(R.plurals.inverseNumberOfComments,
                repliesCount, repliesCount));

        comments.setTextColor(Statics.color4);

        noCommentsTextView.setTextColor(Statics.color4);

        if (data.getTotal_comments() == 0)
            noCommentsLayout.setVisibility(View.VISIBLE);
        else noCommentsLayout.setVisibility(View.GONE);
    }

}
