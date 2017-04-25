package com.ibuildapp.romanblack.VideoPlugin.viewholders;


import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ibuildapp.romanblack.VideoPlugin.R;
import com.ibuildapp.romanblack.VideoPlugin.api.ibaapi.model.CommentsData;
import com.ibuildapp.romanblack.VideoPlugin.details.VideoDetailsActivity;
import com.ibuildapp.romanblack.VideoPlugin.model.VideoItem;
import com.ibuildapp.romanblack.VideoPlugin.utils.ShareUtils;
import com.ibuildapp.romanblack.VideoPlugin.utils.Statics;


public class DetailsInfoHeaderViewHolder extends DetailsInfoViewHolder{

    private final View bottomSeparator;
    public View resizeLayout;
    public ImageView resizeImage;

    public TextView titleText;
    public TextView descriptionText;

    public View bottomLayout;
    public ImageView shareImage;
    public View shareLayout;
    public TextView shareText;

    public ImageView likeImage;
    public View likeLayout;
    public TextView likeText;

    public View topSeparator;
    public TextView comments;

    public LinearLayout noCommentsLayout;
    public TextView noCommentsTextView;


    public DetailsInfoHeaderViewHolder(View itemView, VideoDetailsActivity context) {
        super(itemView, context);

        titleText = (TextView) itemView.findViewById(R.id.video_plugin_details_info_header_title_text);
        descriptionText = (TextView) itemView.findViewById(R.id.video_plugin_details_info_header_description);

        bottomLayout = itemView.findViewById(R.id.video_plugin_details_info_header_bottom_layout);
        shareLayout = itemView.findViewById(R.id.video_plugin_details_info_header_share_layout);
        shareImage = (ImageView) itemView.findViewById(R.id.video_plugin_details_info_header_share_image);
        shareText = (TextView) itemView.findViewById(R.id.video_plugin_details_info_header_share_caption);

        likeLayout = itemView.findViewById(R.id.video_plugin_details_info_header_like_layout);
        likeImage = (ImageView) itemView.findViewById(R.id.video_plugin_details_info_header_like_image);
        likeText = (TextView) itemView.findViewById(R.id.video_plugin_details_info_header_like_caption);

        topSeparator = itemView.findViewById(R.id.video_plugin_details_info_header_top_separator);
        bottomSeparator = itemView.findViewById(R.id.video_plugin_details_info_header_bottom_separator);
        comments = (TextView) itemView.findViewById(R.id.video_plugin_details_info_header_comments_text);

        resizeLayout = itemView.findViewById(R.id.video_plugin_details_info_header_resize_layout);
        resizeImage = (ImageView) itemView.findViewById(R.id.video_plugin_details_info_header_resize_image);

        noCommentsLayout = (LinearLayout) itemView.findViewById(R.id.video_plugin_details_info_item_no_comments_layout);
        noCommentsTextView = (TextView) itemView.findViewById(R.id.video_plugin_details_info_item_no_comments);
    }

    @Override
    public void bind(final VideoItem item, CommentsData data, int position) {
        titleText.setTextColor(Statics.color3);
        titleText.setText(item.getTitle());

        descriptionText.setTextColor(Statics.color3);
        descriptionText.setText(item.getDescription());

        shareText.setTextColor(Statics.color3);
        shareImage.setImageBitmap(Statics.appyColorFilterForResource(shareImage.getContext(), R.drawable.video_list_share,
                Statics.color3, PorterDuff.Mode.MULTIPLY));

        likeImage.setImageBitmap(Statics.appyColorFilterForResource(likeImage.getContext(), R.drawable.video_plugin_like,
                Statics.color3, PorterDuff.Mode.MULTIPLY));

        likeText.setTextColor(Statics.color3);
        likeText.setText(String.valueOf(item.getLikesCount()));

        noCommentsTextView.setTextColor(Statics.color4);
        if ("on".equals(Statics.sharingOn))
            shareLayout.setVisibility(View.VISIBLE);
        else shareLayout.setVisibility(View.GONE);

        if ("on".equals(Statics.likesOn))
            likeLayout.setVisibility(View.VISIBLE);
        else likeLayout.setVisibility(View.GONE);

        shareLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShareUtils.onSharePressed(context, item);
            }
        });

        if (!Statics.isLight) {
            topSeparator.setBackgroundColor(Color.parseColor("#4DFFFFFF"));
            bottomSeparator.setBackgroundColor(Color.parseColor("#4DFFFFFF"));
        } else {
            topSeparator.setBackgroundColor(Color.parseColor("#4D000000"));
            bottomSeparator.setBackgroundColor(Color.parseColor("#4D000000"));
        }

        comments.setTextColor(Statics.color4);

        if (data.getData().size() == 0)
            noCommentsLayout.setVisibility(View.VISIBLE);
        else
            noCommentsLayout.setVisibility(View.GONE);

        if(Statics.commentsOn.equals("off"))
        {
            noCommentsLayout.setVisibility(View.GONE);
            comments.setVisibility(View.GONE);
            topSeparator.setVisibility(View.GONE);
            bottomSeparator.setVisibility(View.GONE);
        }   else
        {
            comments.setVisibility(View.VISIBLE);
            topSeparator.setVisibility(View.VISIBLE);
            bottomSeparator.setVisibility(View.VISIBLE);
        }
    }
}
