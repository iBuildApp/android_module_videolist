package com.ibuildapp.romanblack.VideoPlugin.details;


import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.appbuilder.sdk.android.AppBuilderModuleMainAppCompat;
import com.ibuildapp.romanblack.VideoPlugin.R;
import com.ibuildapp.romanblack.VideoPlugin.details.fragments.InnerVideoFragment;
import com.ibuildapp.romanblack.VideoPlugin.model.VideoItem;
import com.ibuildapp.romanblack.VideoPlugin.utils.VideoPluginConstants;

public class VimeoFullscreenActivity extends AppBuilderModuleMainAppCompat {

    private WebView webView;
    private VideoItem videoItem;

    @Override
    public void create() {
        setContentView(R.layout.video_plugin_details_vimeo);
        hideTopBar();

        Intent intent = getIntent();
        videoItem = (VideoItem) intent.getSerializableExtra(VideoPluginConstants.ITEM);
        webView = (WebView) findViewById(R.id.video_plugin_details_vimeo_web_view);

        webView.setWebChromeClient(new WebChromeClient());
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        webView.getSettings().setPluginState(WebSettings.PluginState.ON_DEMAND);
        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);


        String data = "<iframe src=https://player.vimeo.com/video/" + videoItem.getVimeoResponse().getVideoId() + "?api=1&amp;player_id=player_1 width=100% height=95% frameborder=\"0\" webkitallowfullscreen=\"\" mozallowfullscreen=\"\" allowfullscreen=\"1\"></iframe>";
        webView.loadData(data, "text/html", "utf-8");
    }

    @Override
    public void finish() {
        webView.destroy();
        super.finish();
    }

}
