package com.ibuildapp.romanblack.VideoPlugin.details.fragments;


import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ibuildapp.romanblack.VideoPlugin.R;
import com.ibuildapp.romanblack.VideoPlugin.details.InnerFullscreenActivity;
import com.ibuildapp.romanblack.VideoPlugin.model.VideoItem;
import com.ibuildapp.romanblack.VideoPlugin.utils.VideoPluginConstants;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class InnerVideoFragment extends Fragment implements
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener,
        SurfaceHolder.Callback {

    private final int SHOW_SURFACE = 6;
    private final int VIDEOPLAYER_ERROR = 7;
    private final int SHOW_CONTROLS = 8;
    private final int UPDATE_SEEK_BAR = 9;
    private final int CHECK_CONTROLS_STATE = 10;
    private final int VIDEO_PLAYER_START = 12;
    private final int HIDE_CONTROLS = 14;
    private final int UPDATE_CONTROLS_STATE = 15;
    private PLAYER_STATES playerState = PLAYER_STATES.STATE_STOP;
    private boolean playerPrepared = false;
    private boolean surfaceCreated = false;
    private boolean isTouchSeekBar = false;
    private boolean playIsActive = false;
    private int videoCurrentPos = 0;
    private int btnActive = 0;
    private int videoWidth = 0;
    private int videoHeight = 0;
    private String playingUrl = "";
    private VideoItem videoItem = null;
    private LinearLayout surfaceLayout = null;
    private LinearLayout controlsLayout = null;
    private View playButton = null;
    private View playImage;
    private View pauseImage;
    private SurfaceView surfaceView = null;
    private SurfaceHolder surfaceHolder = null;
    private View progressLayout;
    private SeekBar seekBar = null;
    private TextView durationPositiveTextView = null;
    private TextView durationNegativeTextView = null;
    private View resizeLayout;
    private ImageView resizeImage;
    private int seekToPosition;
    private boolean fullscreen;
    private MediaPlayer mediaPlayer = null;
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_SURFACE: {
                    showSurface();
                }
                break;
                case VIDEOPLAYER_ERROR: {
                }
                break;
                case SHOW_CONTROLS: {
                    showControls();
                }
                break;
                case UPDATE_SEEK_BAR: {
                    updateSeekBar();
                }
                break;
                case VIDEO_PLAYER_START: {
                    playVideo();
                }
                break;
                case CHECK_CONTROLS_STATE: {
                    if (playerState == PLAYER_STATES.STATE_PLAY) {
                        checkControlsState();
                    }
                }
                break;
                case HIDE_CONTROLS: {
                    hideControls();
                }
                break;
                case UPDATE_CONTROLS_STATE: {
                    btnActive = 3;
                }
                break;
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.video_plugin_details_inner, container, false);

        Bundle args = getArguments();
        videoItem = (VideoItem) args.getSerializable(VideoPluginConstants.ITEM);
        seekToPosition = args.getInt(VideoPluginConstants.SEEK_POSITION, 0);
        fullscreen = args.getBoolean(VideoPluginConstants.FULLSCREEN, false);

        videoCurrentPos = seekToPosition;

        playButton = rootView.findViewById(R.id.video_plugin_details_inner_play_layout);
        playImage = rootView.findViewById(R.id.video_plugin_details_inner_play);
        pauseImage = rootView.findViewById(R.id.video_plugin_details_inner_pause);

        progressLayout = rootView.findViewById(R.id.video_plugin_details_inner_progress_layout);

        resizeLayout = rootView.findViewById(R.id.video_plugin_details_inner_resize_layout);
        resizeImage = (ImageView) rootView.findViewById(R.id.video_plugin_details_inner_resize_image);
        surfaceView = (SurfaceView) rootView.findViewById(R.id.video_plugin_details_inner_surface);
        surfaceLayout = (LinearLayout) rootView.findViewById(R.id.video_plugin_details_inner_surface_layout);

        controlsLayout = (LinearLayout) rootView.findViewById(R.id.video_plugin_details_inner_controls_layout);
        seekBar = (SeekBar) rootView.findViewById(R.id.video_plugin_details_inner_seek_bar);

        durationPositiveTextView = (TextView) rootView.findViewById(R.id.video_plugin_details_inner_duration);
        durationNegativeTextView = (TextView) rootView.findViewById(R.id.video_plugin_details_inner_negative_duration);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playIsActive) {
                    if (playerState == PLAYER_STATES.STATE_PLAY)
                        pause();
                    else if (playerState == PLAYER_STATES.STATE_PAUSE
                            || playerState == PLAYER_STATES.STATE_STOP)
                        play();
                }
            }
        });

        if (!fullscreen)
            resizeImage.setImageResource(R.drawable.video_plugin_expand);
        else resizeImage.setImageResource(R.drawable.video_plugin_colapse);

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                handler.sendEmptyMessage(SHOW_CONTROLS);
                handler.sendEmptyMessage(UPDATE_CONTROLS_STATE);
                handler.sendEmptyMessageDelayed(CHECK_CONTROLS_STATE, 300);
                return false;
            }
        });

        TelephonyManager telephonyManager = (TelephonyManager) getActivity().getApplicationContext()
                .getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                try {
                    switch (state) {
                        case TelephonyManager.CALL_STATE_IDLE: {
                        }
                        break;
                        case TelephonyManager.CALL_STATE_OFFHOOK: {
                        }
                        break;
                        case TelephonyManager.CALL_STATE_RINGING: {
                            if (InnerVideoFragment.this.playerState == PLAYER_STATES.STATE_PLAY) {
                                playImage.setVisibility(View.INVISIBLE);
                                pauseImage.setVisibility(View.VISIBLE);
                                mediaPlayer.pause();
                                InnerVideoFragment.this.playerState = PLAYER_STATES.STATE_PAUSE;
                            }
                            Log.d("DEBUG", "RINGING");
                        }
                        break;
                    }

                } catch (NullPointerException nPEx) {
                    Log.d("", "");
                }

            }
        }, PhoneStateListener.LISTEN_CALL_STATE);

        seekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                btnActive = 100;
                isTouchSeekBar = true;

                videoCurrentPos = (mediaPlayer.getDuration() / 100) * seekBar.getProgress();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    mediaPlayer.seekTo(videoCurrentPos);
                    isTouchSeekBar = false;
                    btnActive = 3;
                }
                return false;
            }
        });

        resizeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!fullscreen) {
                    if (playerState == PLAYER_STATES.STATE_PLAY)
                        pause();

                    Intent intent = new Intent(getActivity(), InnerFullscreenActivity.class);
                    intent.putExtra(VideoPluginConstants.ITEM, videoItem);
                    videoCurrentPos = (mediaPlayer.getDuration() / 100) * seekBar.getProgress();
                    intent.putExtra(VideoPluginConstants.SEEK_POSITION, videoCurrentPos);
                    intent.putExtra(VideoPluginConstants.FULLSCREEN, true);
                    getActivity().startActivityForResult(intent, VideoPluginConstants.FULLSCREEN_REQUEST_CODE);
                }else
                    getActivity().onBackPressed();
            }
        });
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);

        playingUrl = videoItem.getUrl();

        handler.sendEmptyMessage(SHOW_SURFACE);

        playVideo();

        return rootView;
    }

    public void pause() {
        playImage.setVisibility(View.VISIBLE);
        pauseImage.setVisibility(View.INVISIBLE);
        mediaPlayer.pause();
        playerState = PLAYER_STATES.STATE_PAUSE;
    }

    public void play() {
        try {
            playImage.setVisibility(View.INVISIBLE);
            pauseImage.setVisibility(View.VISIBLE);
            mediaPlayer.start();
            playerState = PLAYER_STATES.STATE_PLAY;
            handler.sendEmptyMessage(CHECK_CONTROLS_STATE);
            handler.sendEmptyMessage(UPDATE_SEEK_BAR);
        } catch (Exception e) {
            Log.d("", "");
        }
    }

    public int getCurrentDuration(){
        try {
            return (mediaPlayer.getDuration() / 100) * seekBar.getProgress();
        }catch (Throwable e){
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        prepareSurfaceLayoutParams();
    }

    /**
     * Checks if need to hide the player controls and hides them if need.
     */
    private void checkControlsState() {
        if (btnActive > 0) {
            btnActive--;
            handler.sendEmptyMessageDelayed(CHECK_CONTROLS_STATE, 1000);
        } else {
            handler.sendEmptyMessageDelayed(HIDE_CONTROLS, 1000);
        }
    }

    /**
     * Hides the player controls.
     */
    private void hideControls() {
        controlsLayout.setVisibility(View.INVISIBLE);
        playButton.setVisibility(View.INVISIBLE);
    }

    /**
     * Shows the player controls.
     */
    private void showControls() {
        controlsLayout.setVisibility(View.VISIBLE);
        playButton.setVisibility(View.VISIBLE);
    }

    /**
     * Shows surface layout and hides others.
     */
    private void showSurface() {
        surfaceLayout.setVisibility(View.VISIBLE);
        controlsLayout.setVisibility(View.VISIBLE);
    }

    /**
     * Prepares and starts custom video player.
     */
    private void playVideo() {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(playingUrl);
            mediaPlayer.prepareAsync();
        } catch (IOException iOEx) {
            Log.d("", "");
        }
    }

    /**
     * Updates SeekBar state when video is playing.
     */
    private void updateSeekBar() {
        if (mediaPlayer != null) {
            try {
                if (playerState == PLAYER_STATES.STATE_PLAY && !isTouchSeekBar) {
                    seekBar.setProgress((int) (((float) mediaPlayer.getCurrentPosition() / mediaPlayer.getDuration()) * 100));

                    int duration = mediaPlayer.getDuration();
                    int posit = mediaPlayer.getCurrentPosition();

                    Log.d("", "");

                    SimpleDateFormat sdf = new SimpleDateFormat("mm:ss", Locale.getDefault());
                    durationPositiveTextView.setText(sdf.format(new Date(posit)));
                    durationNegativeTextView.setText(sdf.format(new Date(duration - posit)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (playerState == PLAYER_STATES.STATE_PLAY) {
                handler.sendEmptyMessageDelayed(UPDATE_SEEK_BAR, 300);
            }
        }
    }

    private void prepareSurfaceLayoutParams() {
        int screenWidth = InnerVideoFragment.this.getActivity().getWindowManager().getDefaultDisplay().getWidth();
        int screenHeight = InnerVideoFragment.this.getActivity().getWindowManager().getDefaultDisplay().getHeight() -
                (int) (50 * getResources().getDisplayMetrics().density);
        int playerWidth = videoWidth;
        int playerHeight = videoHeight;

        if (videoWidth > videoHeight) {
            playerWidth = screenWidth;
            playerHeight = (int) (((float) screenWidth / (float) videoWidth) * (float) videoHeight);
            if (playerHeight > screenHeight) {
                playerHeight = screenHeight;
                playerWidth = (int) (((float) screenHeight / (float) videoHeight) * (float) videoWidth);
            }
        } else {
            playerHeight = screenHeight;
            playerWidth = (int) (((float) screenHeight / (float) videoHeight) * (float) videoWidth);
            if (playerWidth > screenWidth) {
                playerWidth = screenWidth;
                playerHeight = (int) (((float) screenWidth / (float) videoWidth)
                        * (float) videoHeight);
            }
        }

        android.view.ViewGroup.LayoutParams lp = surfaceView.getLayoutParams();
        lp.width = playerWidth;
        lp.height = playerHeight;
        surfaceView.setLayoutParams(lp);
    }

    private void hideProgressDialog() {
        progressLayout.setVisibility(View.GONE);
    }

    public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
        handler.sendEmptyMessage(VIDEOPLAYER_ERROR);
        return true;
    }

    public void onCompletion(MediaPlayer arg0) {
        playerState = PLAYER_STATES.STATE_STOP;
        playImage.setVisibility(View.VISIBLE);
        pauseImage.setVisibility(View.INVISIBLE);
        arg0.seekTo(videoCurrentPos);

        handler.sendEmptyMessage(SHOW_CONTROLS);

    }

    public void onPrepared(MediaPlayer mp) {
        hideProgressDialog();

        videoWidth = mp.getVideoWidth();
        videoHeight = mp.getVideoHeight();

        prepareSurfaceLayoutParams();

        handler.sendEmptyMessage(UPDATE_SEEK_BAR);

        playerPrepared = true;

        if (surfaceCreated) {
            if (playerState == PLAYER_STATES.STATE_PLAY) {
                btnActive = 5;
                handler.sendEmptyMessage(CHECK_CONTROLS_STATE);
            }
            playIsActive = true;
            mp.seekTo(videoCurrentPos);
        }
    }

    public void surfaceCreated(SurfaceHolder arg0) {
        mediaPlayer.setDisplay(surfaceHolder);

        surfaceCreated = true;

        if (playerPrepared) {
            if (playerState == PLAYER_STATES.STATE_PLAY) {
                btnActive = 5;
                handler.sendEmptyMessage(CHECK_CONTROLS_STATE);
            }

            playIsActive = true;
            mediaPlayer.seekTo(videoCurrentPos);
        }
    }

    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
    }

    public void surfaceDestroyed(SurfaceHolder arg0) {
    }

    public void stop() {
        try {
            mediaPlayer.stop();
            mediaPlayer.release();
        } catch (Exception ex) {
        }
    }

    private enum PLAYER_STATES {

        STATE_PLAY, STATE_STOP, STATE_PAUSE
    }
}
