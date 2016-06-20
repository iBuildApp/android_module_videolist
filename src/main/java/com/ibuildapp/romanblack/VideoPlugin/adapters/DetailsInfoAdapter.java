package com.ibuildapp.romanblack.VideoPlugin.adapters;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.PorterDuff;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import com.ibuildapp.romanblack.VideoPlugin.R;
import com.ibuildapp.romanblack.VideoPlugin.api.ibaapi.model.CommentData;
import com.ibuildapp.romanblack.VideoPlugin.api.ibaapi.model.CommentsData;
import com.ibuildapp.romanblack.VideoPlugin.callbacks.FBLikePressedListener;
import com.ibuildapp.romanblack.VideoPlugin.details.VideoDetailsActivity;
import com.ibuildapp.romanblack.VideoPlugin.model.VideoItem;
import com.ibuildapp.romanblack.VideoPlugin.utils.Statics;
import com.ibuildapp.romanblack.VideoPlugin.viewholders.DetailsInfoHeaderViewHolder;
import com.ibuildapp.romanblack.VideoPlugin.viewholders.DetailsInfoItemViewHolder;
import com.ibuildapp.romanblack.VideoPlugin.viewholders.DetailsInfoViewHolder;

public class DetailsInfoAdapter extends RecyclerView.Adapter<DetailsInfoViewHolder>{

    private final boolean isVimeo;
    private CommentsData data;
    private VideoItem item;
    private FBLikePressedListener listener;
    private VideoDetailsActivity context;

    private int lastAnimatedPosition = -1;
    private boolean animationsLocked = false;

    public DetailsInfoAdapter(VideoDetailsActivity context, CommentsData data, VideoItem item, boolean isVimeo){
        this.context = context;
        this.data = data;
        this.item = item;
        this.isVimeo = isVimeo;
    }

    @Override
    public DetailsInfoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0){
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_plugin_details_info_header, parent, false);
            return new DetailsInfoHeaderViewHolder(v, context);
        }else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_plugin_details_info_item, parent, false);
            return new DetailsInfoItemViewHolder(v, context);
        }
    }

    @Override
    public void onBindViewHolder(DetailsInfoViewHolder holder, int position) {
        runEnterAnimation(holder.itemView, position);
        if (position == 0) {
            ((DetailsInfoHeaderViewHolder) holder).likeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onLikePressed();
                }
            });

            if(!isVimeo || android.os.Build.VERSION.SDK_INT <= 18) {
                ((DetailsInfoHeaderViewHolder) holder).resizeLayout.setVisibility(View.GONE);
            } else {
                ((DetailsInfoHeaderViewHolder) holder).resizeLayout.setVisibility(View.VISIBLE);
                ((DetailsInfoHeaderViewHolder) holder).resizeImage.setImageBitmap(Statics.appyColorFilterForResource(context, R.drawable.video_plugin_expand,
                        Statics.color3, PorterDuff.Mode.MULTIPLY));
                ((DetailsInfoHeaderViewHolder) holder).resizeLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        context.onVimeoResizePressed();
                    }
                });
            }
        }

        holder.bind(item, data, position);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return 0;
        else return 1;
    }

    @Override
    public int getItemCount() {
        return data.getData().size() + 1;
    }

    public void setListener(FBLikePressedListener listener) {
        this.listener = listener;
    }

    public void addNewItem(CommentsData commentsData) {
        data.getData().add(0, commentsData.getData().get(0));
        this.notifyItemInserted(1);
        this.notifyItemChanged(0);
    }

    public CommentData getItemByPosition(int position) {
        return data.getData().get(position-1);
    }

    private void runEnterAnimation(View view, int position) {
        if (animationsLocked) return;

        if (android.os.Build.VERSION.SDK_INT <  12)
            return;


        if (position > lastAnimatedPosition) {
            lastAnimatedPosition = position;

            int height = context.getResources().getDisplayMetrics().heightPixels;
            view.setTranslationY(height);

            view.animate()
                    .translationY(0)
                    .setStartDelay(300 + 100 * (position))
                    .setDuration(500)
                    .setInterpolator(new DecelerateInterpolator(2.f))
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            animationsLocked = true;
                        }
                    })
                    .start();
        }
    }
}
