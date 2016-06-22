package com.ibuildapp.romanblack.VideoPlugin.details;


import android.content.Intent;
import android.os.Bundle;

import com.appbuilder.sdk.android.AppBuilderModuleMainAppCompat;
import com.ibuildapp.romanblack.VideoPlugin.R;
import com.ibuildapp.romanblack.VideoPlugin.details.fragments.InnerVideoFragment;
import com.ibuildapp.romanblack.VideoPlugin.model.VideoItem;
import com.ibuildapp.romanblack.VideoPlugin.utils.VideoPluginConstants;

public class InnerFullscreenActivity extends AppBuilderModuleMainAppCompat {

    private InnerVideoFragment fragment;

    @Override
    public void create() {
        setContentView(R.layout.video_plugin_fullscreen);
        hideTopBar();

        fragment = new InnerVideoFragment();

        Intent intent = getIntent();
        VideoItem videoItem = (VideoItem) intent.getSerializableExtra(VideoPluginConstants.ITEM);
        int seekToPosition = intent.getIntExtra(VideoPluginConstants.SEEK_POSITION, 0);
        boolean fullscreen = intent.getBooleanExtra(VideoPluginConstants.FULLSCREEN, false);

        Bundle bundle = new Bundle();
        bundle.putSerializable(VideoPluginConstants.ITEM,  videoItem);
        bundle.putSerializable(VideoPluginConstants.SEEK_POSITION,  seekToPosition);
        bundle.putSerializable(VideoPluginConstants.FULLSCREEN, fullscreen);
        fragment.setArguments(bundle);

        android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.video_plugin_fullscreen_fragment_container, fragment);
        transaction.commit();
    }

    @Override
    public void finish() {
        Intent intent = new Intent();
        intent.putExtra(VideoPluginConstants.SEEK_POSITION, fragment.getCurrentDuration());
        setResult(RESULT_OK, intent);

        if (fragment != null)
            fragment.stop();

        super.finish();
    }

}
