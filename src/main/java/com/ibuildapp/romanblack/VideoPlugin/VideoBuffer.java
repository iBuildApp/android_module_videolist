/****************************************************************************
*                                                                           *
*  Copyright (C) 2014-2015 iBuildApp, Inc. ( http://ibuildapp.com )         *
*                                                                           *
*  This file is part of iBuildApp.                                          *
*                                                                           *
*  This Source Code Form is subject to the terms of the iBuildApp License.  *
*  You can obtain one at http://ibuildapp.com/license/                      *
*                                                                           *
****************************************************************************/
package com.ibuildapp.romanblack.VideoPlugin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.appbuilder.sdk.android.AppBuilderModuleMain;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnBufferingUpdateListener;
import io.vov.vitamio.MediaPlayer.OnInfoListener;
import io.vov.vitamio.widget.VideoView;

import java.io.Serializable;
import java.util.List;

/**
 * This activity represents custom video player that plays m3u8 strem videos.
 */
public class VideoBuffer extends AppBuilderModuleMain implements OnInfoListener, OnBufferingUpdateListener {

    Integer position;
    List<VideoItem> items;
    LinearLayout controls;
    ProgressBar pb;
    TextView downloadRateView, loadRateView;
    ImageView buttonPlay;
    private String path;
    private Uri uri;
    private VideoView mVideoView;
    private boolean isStart;

    @Override
    public void destroy() {
        mVideoView.stop();
        super.destroy();


    }

    @Override
    public void stop() {
        super.stop();
    }

    @Override
    public void pause() {
        super.pause();
    }

    @Override
    public void resume() {
        super.resume();
    }

    @Override
    public void restart() {
        super.restart();
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void create() {
        hideTopBar();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.romanblack_videobuffer);
        mVideoView = (VideoView) findViewById(R.id.buffer);
        pb = (ProgressBar) findViewById(R.id.probar);

        Intent it = getIntent();
        Bundle bundles = it.getExtras();

        position = (Integer) bundles.get("position");

        items = (List<VideoItem>) bundles.get("items");
        path = items.get(position).getUrl();

        // title
        if (!TextUtils.isEmpty(items.get(position).getTitle()))
            setTopBarTitle(items.get(position).getTitle());
        else
            setTopBarTitle("");

        downloadRateView = (TextView) findViewById(R.id.download_rate);
        loadRateView = (TextView) findViewById(R.id.load_rate);
        uri = Uri.parse(path);
        mVideoView.setVideoURI(uri);
        mVideoView.requestFocus();
        mVideoView.setOnInfoListener(this);
        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mVideoView.setVideoURI(uri);
                mVideoView.requestFocus();
            }
        });
        controls = (LinearLayout) findViewById(R.id.romanblack_video_playerweb_contolpanel);
        controls.setVisibility(View.INVISIBLE);
        mVideoView.setOnBufferingUpdateListener(this);
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.setPlaybackSpeed(1.0f);
            }
        });
        mVideoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (controls.getVisibility() == View.INVISIBLE) {
                    controls.setVisibility(View.VISIBLE);
                } else {
                    controls.setVisibility(View.INVISIBLE);
                }
            }
        });
        View view = findViewById(R.id.video_surface);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (controls.getVisibility() == View.INVISIBLE) {
                    controls.setVisibility(View.VISIBLE);
                } else {
                    controls.setVisibility(View.INVISIBLE);
                }
            }
        });

        buttonPlay = (ImageView) findViewById(R.id.romanblack_video_playerweb_btn_play);
        buttonPlay.setImageResource(R.drawable.romanblack_video_play);
        ImageView buttonPrev = (ImageView) findViewById(R.id.romanblack_video_playerweb_btn_prev);
        ImageView buttonNext = (ImageView) findViewById(R.id.romanblack_video_playerweb_btn_next);

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mVideoView.stop();
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putSerializable("items", (Serializable) items);
                bundle.putInt("position", position);
                intent.putExtras(bundle);
                setResult(Statics.PLAY_NEXT_VIDEO, intent);
                finish();
            }
        });

        buttonPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mVideoView.stop();
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putSerializable("items", (Serializable) items);
                bundle.putInt("position", position);
                intent.putExtras(bundle);
                setResult(Statics.PLAY_PREV_VIDEO, intent);
                finish();
            }
        });

        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mVideoView.isPlaying()) {
                    mVideoView.pause();
                    buttonPlay.setImageResource(R.drawable.romanblack_video_play);
                } else {
                    mVideoView.start();
                    buttonPlay.setImageResource(R.drawable.romanblack_video_pause);
                }
            }
        });
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                if (mVideoView.isPlaying()) {
                    mVideoView.pause();
                    isStart = true;
                    pb.setVisibility(View.VISIBLE);
                    downloadRateView.setVisibility(View.VISIBLE);
                    loadRateView.setVisibility(View.VISIBLE);

                }
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                if (isStart) {
                    mVideoView.start();
                    pb.setVisibility(View.GONE);
                    downloadRateView.setVisibility(View.GONE);
                    loadRateView.setVisibility(View.GONE);
                    buttonPlay.setImageResource(R.drawable.romanblack_video_pause);
                }
                break;
            case MediaPlayer.MEDIA_INFO_DOWNLOAD_RATE_CHANGED:
                downloadRateView.setText("" + extra + "kb/s" + "  ");
                break;
        }
        return true;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        loadRateView.setText(percent + "%");
    }
}