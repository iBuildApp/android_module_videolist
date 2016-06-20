package com.ibuildapp.romanblack.VideoPlugin.details;


import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.appbuilder.sdk.android.AppBuilderModuleMainAppCompat;
import com.appbuilder.sdk.android.Widget;
import com.appbuilder.sdk.android.authorization.Authorization;
import com.ibuildapp.romanblack.VideoPlugin.AuthorizationActivity;
import com.ibuildapp.romanblack.VideoPlugin.R;
import com.ibuildapp.romanblack.VideoPlugin.api.ibaapi.model.CommentData;
import com.ibuildapp.romanblack.VideoPlugin.details.fragments.DetailsInfoFragment;
import com.ibuildapp.romanblack.VideoPlugin.details.fragments.InnerVideoFragment;
import com.ibuildapp.romanblack.VideoPlugin.details.fragments.VimeoFragment;
import com.ibuildapp.romanblack.VideoPlugin.details.fragments.YouTubeFragment;
import com.ibuildapp.romanblack.VideoPlugin.model.VideoItem;
import com.ibuildapp.romanblack.VideoPlugin.model.VideoType;
import com.ibuildapp.romanblack.VideoPlugin.replies.RepliesActivity;
import com.ibuildapp.romanblack.VideoPlugin.utils.ShareUtils;
import com.ibuildapp.romanblack.VideoPlugin.utils.Statics;
import com.ibuildapp.romanblack.VideoPlugin.utils.VideoPluginConstants;

import java.util.ArrayList;

public class VideoDetailsActivity extends AppBuilderModuleMainAppCompat{

    private ArrayList<VideoItem> items;
    private int position;
    private VideoItem currentItem;
    private Widget widget;
    private Fragment fragment;
    private DetailsInfoFragment infoFragment;
    private boolean fullScreenStarted = false;

    @Override
    public void create() {
        setContentView(R.layout.video_plugin_details);
        Intent currentIntent = getIntent();
        items = (ArrayList<VideoItem>) currentIntent.getSerializableExtra(VideoPluginConstants.ITEMS);
        position = currentIntent.getIntExtra(VideoPluginConstants.POSITION, 0);

        currentItem = items.get(position);
        setTopBarTitle(getString(R.string.romanblack_video_main_capture));

        setTopBarTitleColor(Color.parseColor("#000000"));

        setTopBarLeftButtonTextAndColor(getResources().getString(R.string.common_back_upper), Color.parseColor("#000000"), true, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        widget = (Widget) currentIntent.getSerializableExtra(VideoPluginConstants.WIDGET);

        if (!TextUtils.isEmpty(widget.getTitle())) {
            setTopBarTitle(widget.getTitle());
        } else {
            setTopBarTitle(getResources().getString(R.string.romanblack_video_main_capture));
        }
        setTopBarBackgroundColor(Statics.color1);

        boolean isVimeo = false;
        if (currentItem.getVideoType().equals(VideoType.VIMEO)) {
            fragment = new VimeoFragment();
            isVimeo = true;
        } else if (currentItem.getVideoType().equals(VideoType.YOUTUBE))
             fragment = new YouTubeFragment();
        else fragment = new InnerVideoFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable(VideoPluginConstants.ITEM,  currentItem);
        fragment.setArguments(bundle);

        bundle.putBoolean(VideoPluginConstants.VIMEO, isVimeo);
        infoFragment = new DetailsInfoFragment();
        infoFragment.setArguments(bundle);

        android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.video_plugin_details_fragment_container, fragment);
        transaction.replace(R.id.video_plugin_details_info_fragment_container, infoFragment);
        transaction.commit();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        fullScreenStarted = newConfig.orientation != Configuration.ORIENTATION_PORTRAIT;
    }

    @Override
    public void onBackPressed() {
        if (fullScreenStarted){
            if (fragment instanceof YouTubeFragment) {
                ((YouTubeFragment) fragment).goToPortrait();
            }
        }else super.onBackPressed();
    }

    @Override
    public void finish() {
        if (fragment instanceof InnerVideoFragment)
            ((InnerVideoFragment) fragment).stop();

        super.finish();
        overridePendingTransition(R.anim.activity_open_scale, R.anim.activity_close_translate);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VideoPluginConstants.FULLSCREEN_REQUEST_CODE){
            int newDuration = data.getIntExtra(VideoPluginConstants.SEEK_POSITION, ((InnerVideoFragment)fragment).getCurrentDuration());
            fragment = new InnerVideoFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable(VideoPluginConstants.ITEM,  currentItem);
            bundle.putSerializable(VideoPluginConstants.SEEK_POSITION,  newDuration);
            bundle.putSerializable(VideoPluginConstants.FULLSCREEN, false);
            fragment.setArguments(bundle);

            android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.video_plugin_details_fragment_container, fragment);
            transaction.commit();
        }else if (requestCode == VideoPluginConstants.FACEBOOK_AUTH_LIKE) {
            if (resultCode == RESULT_OK) {
                infoFragment.likePressed();
            }
        }else if (requestCode == VideoPluginConstants.FACEBOOK_AUTH_SHARE) {
            if (resultCode == RESULT_OK) {
                ShareUtils.shareFacebook(this, currentItem);
            }
        } else if (requestCode == VideoPluginConstants.TWITTER_AUTH) {
            if (resultCode == RESULT_OK) {
                if (Authorization.getAuthorizedUser(Authorization.AUTHORIZATION_TYPE_TWITTER) != null) {
                    ShareUtils.shareTwitter(this, currentItem);
                }
            }
        } else if ( requestCode == VideoPluginConstants.SHARING_FACEBOOK ) {
            if ( resultCode == RESULT_OK )
                Toast.makeText(VideoDetailsActivity.this, getString(R.string.directoryplugin_facebook_posted_success), Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(VideoDetailsActivity.this, getString(R.string.directoryplugin_facebook_posted_error), Toast.LENGTH_SHORT).show();
        } else if ( requestCode == VideoPluginConstants.SHARING_TWITTER ) {
            if ( resultCode == RESULT_OK )
                Toast.makeText(VideoDetailsActivity.this, getString(R.string.directoryplugin_twitter_posted_success), Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(VideoDetailsActivity.this, getString(R.string.directoryplugin_twitter_posted_error), Toast.LENGTH_SHORT).show();
        }else if (requestCode == VideoPluginConstants.AUTHORIZATION_ACTIVITY && resultCode == RESULT_OK) {
            infoFragment.postMessage();
        }else if (requestCode == VideoPluginConstants.REPLIES && data != null){
            CommentData returnComment = (CommentData) data.getSerializableExtra(VideoPluginConstants.ITEM);
            infoFragment.onCommentCountChange(returnComment.getTotal_comments(), position);
        }
    }

    public void authorize(){
        Intent it = new Intent(this, AuthorizationActivity.class);
        it.putExtra("Widget", widget);
        startActivityForResult(it, VideoPluginConstants.AUTHORIZATION_ACTIVITY);
        overridePendingTransition(R.anim.activity_open_translate, R.anim.activity_close_scale);
    }

    public void launchCommentToCommentsActivity(CommentData comment, int position) {
        this.position = position;

        Intent it = new Intent(this, RepliesActivity.class);
        it.putExtra(VideoPluginConstants.ITEM, currentItem);
        it.putExtra(VideoPluginConstants.ITEM_2, comment);
        startActivityForResult(it, VideoPluginConstants.REPLIES);
        overridePendingTransition(R.anim.activity_open_translate, R.anim.activity_close_scale);
    }

    public void onVimeoResizePressed() {
        Intent it = new Intent(this, VimeoFullscreenActivity.class);
        it.putExtra(VideoPluginConstants.ITEM, currentItem);
        startActivity(it);
        overridePendingTransition(R.anim.activity_open_translate, R.anim.activity_close_scale);
    }
}
