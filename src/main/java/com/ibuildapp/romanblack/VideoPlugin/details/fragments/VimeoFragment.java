package com.ibuildapp.romanblack.VideoPlugin.details.fragments;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.ibuildapp.romanblack.VideoPlugin.R;
import com.ibuildapp.romanblack.VideoPlugin.api.vimeoapi.VimeoApi;
import com.ibuildapp.romanblack.VideoPlugin.api.vimeoapi.model.VimeoResponse;
import com.ibuildapp.romanblack.VideoPlugin.model.VideoItem;
import com.ibuildapp.romanblack.VideoPlugin.utils.VideoPluginConstants;
import com.ibuildapp.romanblack.VideoPlugin.utils.rx.RxUtils;
import com.ibuildapp.romanblack.VideoPlugin.utils.rx.SimpleSubscriber;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class VimeoFragment extends Fragment {

    private WebView webView;
    private ImageView imageStub;
    private VideoItem currentItem;
    private View root;

    private VimeoApi api;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.video_plugin_details_vimeo, container, false);

        webView = (WebView) rootView.findViewById(R.id.video_plugin_details_vimeo_web_view);
        imageStub = (ImageView) rootView.findViewById(R.id.video_plugin_details_vimeo_image_stub);
        root = rootView.findViewById(R.id.video_plugin_details_vimeo_image_frame);

        if (android.os.Build.VERSION.SDK_INT <= 18) {
            root.setVisibility(View.VISIBLE);
            webView.setVisibility(View.GONE);
        }else {
            root.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);
        }

        Bundle args = getArguments();
        currentItem = (VideoItem) args.getSerializable(VideoPluginConstants.ITEM);

        if (currentItem != null)
            new VimeoApi().getVideoInfo(currentItem.getUrl())
                .compose(RxUtils.<VimeoResponse>applyCustomSchedulers(Schedulers.io(), AndroidSchedulers.mainThread()))
                .subscribe(new SimpleSubscriber<VimeoResponse>() {

                    @Override
                    public void onNext(VimeoResponse response) {
                        initData(response);
                    }
                });

        return rootView;
    }

    private void initData(final VimeoResponse response) {
        if (android.os.Build.VERSION.SDK_INT <= 18) {
            Glide.with(this).load(currentItem.getCoverUrl()).dontAnimate().into(imageStub);
            root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String urlRequest = "https://player.vimeo.com/video/" + response.getVideoId();
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(urlRequest));
                    startActivity(i);
                }
            });
        }else {
            webView.setWebChromeClient(new WebChromeClient());
            webView.getSettings().setPluginState(WebSettings.PluginState.ON);
            webView.getSettings().setPluginState(WebSettings.PluginState.ON_DEMAND);
            webView.setWebViewClient(new WebViewClient());
            webView.getSettings().setJavaScriptEnabled(true);
            Integer height = getResources().getDisplayMetrics().widthPixels * 9;
            height = height / 16;
            height = (int) (height / getResources().getDisplayMetrics().density);
            height -= 10;

            String data = "<iframe src=https://player.vimeo.com/video/" + response.getVideoId() + "?api=1&amp;player_id=player_1 width=100% height=" + height + " frameborder=\"0\" webkitallowfullscreen=\"\" mozallowfullscreen=\"\" allowfullscreen=\"\"></iframe>";
            webView.loadData(data, "text/html", "utf-8");
        }
    }
}
