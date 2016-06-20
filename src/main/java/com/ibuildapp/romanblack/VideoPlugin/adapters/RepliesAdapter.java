package com.ibuildapp.romanblack.VideoPlugin.adapters;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import com.ibuildapp.romanblack.VideoPlugin.R;
import com.ibuildapp.romanblack.VideoPlugin.api.ibaapi.model.CommentData;
import com.ibuildapp.romanblack.VideoPlugin.api.ibaapi.model.CommentsData;
import com.ibuildapp.romanblack.VideoPlugin.replies.RepliesActivity;
import com.ibuildapp.romanblack.VideoPlugin.viewholders.replies.RepliesHeaderViewHolder;
import com.ibuildapp.romanblack.VideoPlugin.viewholders.replies.RepliesItemViewHolder;
import com.ibuildapp.romanblack.VideoPlugin.viewholders.replies.RepliesViewHolder;

public class RepliesAdapter extends RecyclerView.Adapter<RepliesViewHolder>{

    private CommentsData replies;
    private CommentData parent;
    private RepliesActivity context;

    private int lastAnimatedPosition = -1;
    private boolean animationsLocked = false;

    public RepliesAdapter(RepliesActivity context, CommentsData data, CommentData item){
        this.context = context;
        this.replies = data;
        this.parent = item;
    }

    @Override
    public RepliesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0){
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_plugin_replies_header, parent, false);
            return new RepliesHeaderViewHolder(v);
        }else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_plugin_replies_item, parent, false);
            return new RepliesItemViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(RepliesViewHolder holder, int position) {
        runEnterAnimation(holder.itemView, position);

        CommentData currentData = position == 0?parent:replies.getData().get(position-1);
        holder.bind(currentData);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return 0;
        else return 1;
    }

    @Override
    public int getItemCount() {
        return replies.getData().size() + 1;
    }

    public void addNewItem(CommentsData commentsData) {
        replies.getData().add(0, commentsData.getData().get(0));
        parent.setTotal_comments(replies.getData().size());
        this.notifyItemInserted(1);
        this.notifyItemChanged(0);
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
                    .setStartDelay(100 * position)
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
