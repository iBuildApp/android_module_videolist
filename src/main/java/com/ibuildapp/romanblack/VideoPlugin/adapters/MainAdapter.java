package com.ibuildapp.romanblack.VideoPlugin.adapters;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

import com.appbuilder.sdk.android.Widget;
import com.appbuilder.sdk.android.tools.NetworkUtils;
import com.ibuildapp.romanblack.VideoPlugin.R;
import com.ibuildapp.romanblack.VideoPlugin.api.vimeoapi.VimeoApi;
import com.ibuildapp.romanblack.VideoPlugin.api.youtubeapi.YouTubeApi;
import com.ibuildapp.romanblack.VideoPlugin.callbacks.BtnShareListener;
import com.ibuildapp.romanblack.VideoPlugin.callbacks.SharePressedListener;
import com.ibuildapp.romanblack.VideoPlugin.details.VideoDetailsActivity;
import com.ibuildapp.romanblack.VideoPlugin.model.VideoItem;
import com.ibuildapp.romanblack.VideoPlugin.model.VideoType;
import com.ibuildapp.romanblack.VideoPlugin.utils.Statics;
import com.ibuildapp.romanblack.VideoPlugin.viewholders.main.DefaultViewHolder;
import com.ibuildapp.romanblack.VideoPlugin.viewholders.main.MainViewHolder;
import com.ibuildapp.romanblack.VideoPlugin.viewholders.main.UploadViewHolder;
import com.ibuildapp.romanblack.VideoPlugin.viewholders.main.VimeoViewHolder;
import com.ibuildapp.romanblack.VideoPlugin.viewholders.main.YouTubeViewHolder;

import java.util.ArrayList;

public class MainAdapter extends RecyclerView.Adapter<MainViewHolder>{

    private ArrayList<VideoItem> items = null;
    private SharePressedListener sharePressedListener = null;
    private YouTubeApi youTubeApi;
    private VimeoApi vimeoApi;

    private Widget widget = null;
    private Activity ctx = null;
    private String cachePath;

    private int lastAnimatedPosition = -1;
    private boolean animationsLocked = false;

    public MainAdapter(Activity ctx, ArrayList<VideoItem> items, Widget widget) {
        this.items = items;
        this.ctx = ctx;
        this.widget = widget;

        youTubeApi = new YouTubeApi();
        vimeoApi = new VimeoApi();
    }

    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_plugin_list_item, parent, false);

        VideoType type = VideoType.values()[viewType];
        switch (type){
            case YOUTUBE:
                return new YouTubeViewHolder(v, youTubeApi);
            case VIMEO:
                return new VimeoViewHolder(v, vimeoApi);
            case UPLOAD:
                return new UploadViewHolder(v);
            default:
                return new DefaultViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(MainViewHolder holder, int position) {
        runEnterAnimation(holder.itemView, position);

        holder.prefetchView();

        holder.shareImageView.setImageBitmap(Statics.appyColorFilterForResource(ctx, R.drawable.video_list_share,
                Statics.color3, PorterDuff.Mode.MULTIPLY));
        holder.shareButton.setOnClickListener(new BtnShareListener(position, getSharePressedListener()));

        if (Statics.sharingOn.equalsIgnoreCase("off"))
            holder.shareButton.setVisibility(View.GONE);
        else holder.shareButton.setVisibility(View.VISIBLE);

        holder.itemView.setOnClickListener(new PlayerStartListener(position));

        holder.titleTextView.setText(items.get(position).getTitle());

        if (!Statics.isLight)
            holder.divider.setBackgroundColor(Color.parseColor("#4DFFFFFF"));
        else holder.divider.setBackgroundColor(Color.parseColor("#4D000000"));
        holder.postView(position, items);
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getVideoType().ordinal();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public SharePressedListener getSharePressedListener() {
        return sharePressedListener;
    }

    public void setSharePressedListener(SharePressedListener sharePressedListener) {
        this.sharePressedListener = sharePressedListener;
    }

    public void setCachePath(String cachePath) {
        this.cachePath = cachePath;
    }


    public class PlayerStartListener implements View.OnClickListener {

        private int position = 0;

        public PlayerStartListener(int position) {
            this.position = position;
        }

        public void onClick(View arg0) {
            if (NetworkUtils.isOnline(ctx)) {
                Intent it = new Intent(ctx, VideoDetailsActivity.class);
                it.putExtra("items", items);
                it.putExtra("position", position);
                it.putExtra("cachePath", cachePath);
                it.putExtra("Widget", widget);
                ctx.startActivity(it);
                ctx.overridePendingTransition(R.anim.activity_open_translate, R.anim.activity_close_scale);
            }else{
                Toast.makeText(ctx, R.string.romanblack_video_alert_no_internet, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void runEnterAnimation(View view, int position) {
        if (animationsLocked) return;

        if (android.os.Build.VERSION.SDK_INT <  12)
            return;

        int height = ctx.getResources().getDisplayMetrics().heightPixels;
        if (position > lastAnimatedPosition) {
            lastAnimatedPosition = position;

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
