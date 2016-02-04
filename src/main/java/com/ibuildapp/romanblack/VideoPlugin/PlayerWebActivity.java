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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.appbuilder.sdk.android.AppBuilderModuleMain;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * This activity represents custom videoplayer.
 */
public class PlayerWebActivity extends AppBuilderModuleMain implements
        OnClickListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener,
        Callback {

    private enum PLAYER_STATES {

        STATE_PLAY, STATE_STOP, STATE_PAUSE
    }
    private final int NEED_INTERNET_CONNECTION = 0;
    private final int INITIALIZATION_FAILED = 1;
    private final int LOADING_ABORTED = 2;
    private final int SHOW_PROGRESS_DIALOG = 3;
    private final int HIDE_PROGRESS_DIALOG = 4;
    private final int SHOW_WEB = 5;
    private final int SHOW_SURFACE = 6;
    private final int VIDEOPLAYER_ERROR = 7;
    private final int SHOW_CONTROLS = 8;
    private final int UPDATE_SEEK_BAR = 9;
    private final int CHECK_CONTROLS_STATE = 10;
    private final int RESOLVE_YOUTUBE_URL = 11;
    private final int VIDEO_PLAYER_START = 12;
    private final int SHOW_YOUTUBE = 13;
    private final int HIDE_CONTROLS = 14;
    private final int UPDATE_CONTROLS_STATE = 15;
    private PLAYER_STATES playerState = PLAYER_STATES.STATE_STOP;
    private boolean playerPrepared = false;
    private boolean surfaceCreated = false;
    private boolean linkResolved = false;
    private boolean isTouchSeekBar = false;
    private boolean playIsActive = false;
    private int position = 0;
    private int videoCurrentPos = 0;
    private int btnActive = 0;
    private int videoWidth = 0;
    private int videoHeight = 0;
    private String playingUrl = "";
    private String youTubeHTML = "";
    private VideoItem videoItem = null;
    private ProgressDialog progressDialog = null;
    private TextView homeImageView = null;
    private LinearLayout webLayout = null;
    private WebView webView = null;
    private LinearLayout surfaceLayout = null;
    private LinearLayout controlsLayout = null;
    private ImageView btnPlayImageView = null;
    private ImageView btnPrevImageView = null;
    private ImageView btnNextImageView = null;
    private SurfaceView surfaceView = null;
    private SurfaceHolder surfaceHolder = null;
    private SeekBar seekBar = null;
    private TextView durationPositiveTextView = null;
    private TextView durationNegativeTextView = null;
    private TelephonyManager telephonyManager = null;
    private MediaPlayer mediaPlayer = null;
    private ArrayList<VideoItem> items = new ArrayList<VideoItem>();

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case NEED_INTERNET_CONNECTION: {
                    Toast.makeText(PlayerWebActivity.this,
                            getResources().getString(
                            R.string.romanblack_video_alert_no_internet),
                            Toast.LENGTH_LONG).show();

                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            finish();
                        }
                    }, 2000);
                }
                break;
                case INITIALIZATION_FAILED: {
                    Toast.makeText(PlayerWebActivity.this,
                            getResources().getString(R.string.romanblack_video_alert_cannot_init),
                            Toast.LENGTH_LONG).show();
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            finish();
                        }
                    }, 2000);
                }
                break;
                case LOADING_ABORTED: {
                    closeActivity();
                }
                break;
                case SHOW_PROGRESS_DIALOG: {
                    showProgressDialog();
                }
                break;
                case HIDE_PROGRESS_DIALOG: {
                    hideProgressDialog();
                }
                break;
                case SHOW_WEB: {
                    showWeb();
                }
                break;
                case SHOW_SURFACE: {
                    showSurface();
                }
                break;
                case VIDEOPLAYER_ERROR: {
                    Toast.makeText(PlayerWebActivity.this,
                            getResources().getString(R.string.romanblack_video_alert_link_not_valid),
                            Toast.LENGTH_LONG).show();

                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            finish();
                        }
                    }, 2000);
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
                case RESOLVE_YOUTUBE_URL: {
                    resolveYouTubeUrl();
                }
                break;
                case VIDEO_PLAYER_START: {
                    playVideo();
                }
                break;
                case SHOW_YOUTUBE: {
                    showYouTube();
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

    @Override
    public void create() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.romanblack_video_playerweb);

        Intent currentIntent = getIntent();
        items = (ArrayList<VideoItem>) currentIntent.getSerializableExtra("items");

        if (items == null) {
            handler.sendEmptyMessage(INITIALIZATION_FAILED);
            return;
        }

        if (items.isEmpty()) {
            handler.sendEmptyMessage(INITIALIZATION_FAILED);
            return;
        }

        position = currentIntent.getIntExtra("position", 0);

        try {
            videoItem = items.get(position);
        } catch (IndexOutOfBoundsException iOOBEx) {
            handler.sendEmptyMessage(INITIALIZATION_FAILED);
            return;
        }

        if (videoItem == null) {
            handler.sendEmptyMessage(INITIALIZATION_FAILED);
            return;
        }

        homeImageView = (TextView) findViewById(R.id.romanblack_fanwall_main_home);
        homeImageView.setOnClickListener(this);

        if (!TextUtils.isEmpty(items.get(position).getTitle())) {
            setTopBarTitle(items.get(position).getTitle());
        } else {
            setTopBarTitle("");
        }

        swipeBlock();
        setTopBarLeftButtonText(getResources().getString(R.string.common_back_upper), true, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        webLayout = (LinearLayout) findViewById(R.id.romanblack_video_playerweb_webviewlayout);

        webView = (WebView) findViewById(R.id.romanblack_video_playerweb_webview);
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
        webView.getSettings().setSupportMultipleWindows(false);
        webView.getSettings().setSupportZoom(false);
        webView.getSettings().setUseWideViewPort(true);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                super.onShowCustomView(view, callback);

                Log.d("", "");
            }

            @Override
            public boolean onCreateWindow(WebView view, boolean dialog, boolean userGesture, Message resultMsg) {
                Log.d("", "");

                return super.onCreateWindow(view, dialog, userGesture, resultMsg);
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                Log.d("", "");

                super.onProgressChanged(view, newProgress);
            }
        });
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

                handler.sendEmptyMessage(SHOW_PROGRESS_DIALOG);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                if (url.equals("end")) {
                    handler.sendEmptyMessage(LOADING_ABORTED);

                    return;
                }

                handler.sendEmptyMessage(HIDE_PROGRESS_DIALOG);
            }
        });

        btnPlayImageView = (ImageView) findViewById(R.id.romanblack_video_playerweb_btn_play);
        btnPlayImageView.setOnClickListener(this);

        btnNextImageView = (ImageView) findViewById(R.id.romanblack_video_playerweb_btn_next);
        btnNextImageView.setOnClickListener(this);

        btnPrevImageView = (ImageView) findViewById(R.id.romanblack_video_playerweb_btn_prev);
        btnPrevImageView.setOnClickListener(this);

        surfaceLayout = (LinearLayout) findViewById(R.id.romanblack_video_playerweb_surfacelayout);

        surfaceView = (SurfaceView) findViewById(R.id.romanblack_video_playerweb_surface);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                handler.sendEmptyMessage(SHOW_CONTROLS);
                handler.sendEmptyMessage(UPDATE_CONTROLS_STATE);
                handler.sendEmptyMessageDelayed(CHECK_CONTROLS_STATE, 300);
                return false;
            }
        });

        controlsLayout = (LinearLayout) findViewById(R.id.romanblack_video_playerweb_controlslayout);

        telephonyManager = (TelephonyManager) getApplicationContext()
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
                            Log.d("DEBUG", "OFFHOOK");
                        }
                        break;
                        case TelephonyManager.CALL_STATE_RINGING: {
                            if (PlayerWebActivity.this.playerState == PLAYER_STATES.STATE_PLAY) {
                                btnPlayImageView.setImageResource(R.drawable.romanblack_video_pause);
                                mediaPlayer.pause();
                                PlayerWebActivity.this.playerState = PLAYER_STATES.STATE_PAUSE;
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

        seekBar = (SeekBar) findViewById(R.id.romanblack_video_playerweb_seekbar);
        seekBar.setOnTouchListener(new OnTouchListener() {
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
        durationPositiveTextView = (TextView) findViewById(R.id.romanblack_video_playerweb_duration);
        durationNegativeTextView = (TextView) findViewById(R.id.romanblack_video_playerweb_negative_duration);

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);

        if (videoItem.getUrl().contains("youtube")) {
            handler.sendEmptyMessage(SHOW_WEB);

            handler.sendEmptyMessage(RESOLVE_YOUTUBE_URL);
        } else if (videoItem.getUrl().contains("vimeo")) {
            handler.sendEmptyMessage(SHOW_WEB);

            webView.loadDataWithBaseURL("",
                    prepareVimeoHTML(videoItem.getUrl(), 100, 100), "text/html",
                    "utf-8", "");
        } else {
            linkResolved = true;
            playingUrl = videoItem.getUrl();

            handler.sendEmptyMessage(SHOW_SURFACE);
            
            playVideo();
        }
    }

    @Override
    public void destroy() {
    }

    /**
     * Stops playing video before activity will be destroyed.
     */
    @Override
    public void onBackPressed() {
        try {
            mediaPlayer.stop();
            mediaPlayer.release();
        } catch (Exception ex) {
        }

        super.onBackPressed();
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
    }

    /**
     * Shows the player controls.
     */
    private void showControls() {
        controlsLayout.setVisibility(View.VISIBLE);
    }

    /**
     * Shows web layout and hides others.
     */
    private void showWeb() {
        webLayout.setVisibility(View.VISIBLE);
        surfaceLayout.setVisibility(View.INVISIBLE);
        controlsLayout.setVisibility(View.INVISIBLE);
    }

    /**
     * Shows surface layout and hides others.
     */
    private void showSurface() {
        webLayout.setVisibility(View.INVISIBLE);
        surfaceLayout.setVisibility(View.VISIBLE);
        controlsLayout.setVisibility(View.VISIBLE);
    }

    /**
     * Prepares and starts custom video player.
     */
    private void playVideo() {
        try {
            handler.sendEmptyMessage(SHOW_PROGRESS_DIALOG);
            mediaPlayer.reset();
            mediaPlayer.setDataSource(playingUrl);
            mediaPlayer.prepareAsync();
        } catch (IOException iOEx) {
            Log.d("", "");
        }
    }

    /**
     * Resolves YouTube stream URL by video YouTube ID.
     */
    private void resolveYouTubeUrl() {
        new Thread(new Runnable() {
            public void run() {

                try {
                    Uri youTubeUri = Uri.parse(items.get(position).getUrl());
                    String youTubeId = youTubeUri.getQueryParameter("v");

                    Log.d("", "");

                    HttpClient lClient = new DefaultHttpClient();

                    HttpGet lGetMethod = new HttpGet("http://www.youtube.com/oembed?url="
                            + URLEncoder.encode(items.get(position).getUrl())
                            + "&format=json");

                    HttpResponse lResp = null;

                    lResp = lClient.execute(lGetMethod);

                    ByteArrayOutputStream lBOS = new ByteArrayOutputStream();
                    String lInfoStr = null;

                    lResp.getEntity().writeTo(lBOS);
                    lInfoStr = lBOS.toString("UTF-8");

                    Log.d("", "");

                    JSONObject obj = new JSONObject(lInfoStr);
                    String html = obj.getString("html");

                    Document doc = Jsoup.parse(html);
                    Element iFrame = doc.select("iframe").first();

                    Attributes iFrameAttrs = iFrame.attributes();
                    int videoHeight = Integer.parseInt(iFrameAttrs.get("height"));
                    int videoWidth = Integer.parseInt(iFrameAttrs.get("width"));
                    int displayHeight = getResources().getDisplayMetrics().heightPixels;
                    int displayWidth = getResources().getDisplayMetrics().widthPixels;
                    float widthKoef = displayWidth / videoWidth;

                    iFrameAttrs.remove("height");
                    iFrameAttrs.remove("width");

                    int heightRes = (int) (videoHeight * widthKoef);
                    int widthRes = (int) (videoWidth * widthKoef);

                    iFrameAttrs.put("height", heightRes + "");
                    iFrameAttrs.put("width", widthRes + "");

                    youTubeHTML = doc.outerHtml();

                    Log.d("", "");

                    handler.sendEmptyMessage(SHOW_YOUTUBE);

                    if (surfaceCreated) {
                        handler.sendEmptyMessage(VIDEO_PLAYER_START);
                    }

                } catch (Exception e) {
                    Log.d("", "");
                }
            }
        }).start();
    }

    /**
     * Returns supported fallback ID.
     * @param pOldId
     * @return int value of ID
     */
    public static int getSupportedFallbackId(int pOldId) {
        final int lSupportedFormatIds[] = {13, //3GPP (MPEG-4 encoded) Low quality 
            17, //3GPP (MPEG-4 encoded) Medium quality 
            18, //MP4  (H.264 encoded) Normal quality
            22, //MP4  (H.264 encoded) High quality
            37 //MP4  (H.264 encoded) High quality
        };
        int lFallbackId = pOldId;
        for (int i = lSupportedFormatIds.length - 1; i >= 0; i--) {
            if (pOldId == lSupportedFormatIds[i] && i > 0) {
                lFallbackId = lSupportedFormatIds[i - 1];
            }
        }
        return lFallbackId;
    }

    /**
     * Prepares vimeo video HTML page.
     * @param vimeoUrl vimeo video URL
     * @param height needed video height
     * @param width needed video width
     * @return prepared HTML page
     */
    private String prepareVimeoHTML(String vimeoUrl, int height, int width) {
        StringBuilder sb = new StringBuilder();

        Uri vimeoUri = Uri.parse(vimeoUrl);
        String vimeoId = vimeoUri.getPathSegments().get(0);

        sb.append("<html>");
        sb.append("<head>");
        sb.append("<body style=\"margin:0\" bgcolor=\"black\">");
        sb.append("<iframe src=\"");
        sb.append("http://player.vimeo.com/video/");
        sb.append(vimeoId);
        sb.append("\" width=\"100%");
        sb.append("\" height=\"100%");
        sb.append("\" frameborder=\"0\" webkitAllowFullScreen mozallowfullscreen allowFullScreen>");
        sb.append("</iframe>");
        sb.append("</body>");
        sb.append("</html>");

        return sb.toString();
    }

    /**
     * Loads YouTube HTML page to WebView.
     */
    private void showYouTube() {
        webView.loadDataWithBaseURL("",
                youTubeHTML, "text/html",
                "utf-8", "");
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

                    SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
                    durationPositiveTextView.setText(sdf.format(new Date(posit)));
                    durationNegativeTextView.setText("-" + sdf.format(new Date(duration - posit)));

                }
                if (playerState == PLAYER_STATES.STATE_PAUSE && !isTouchSeekBar) {
                    seekBar.setProgress((int) (((float) videoCurrentPos / mediaPlayer.getDuration()) * 100));
                }
            } catch (Exception e) {
            }

            if (playerState == PLAYER_STATES.STATE_PLAY) {
                handler.sendEmptyMessageDelayed(UPDATE_SEEK_BAR, 300);
            }
        }
    }
    
    private void prepareSurfaceLayoutParams(){
        int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
        int screenHeight = getWindowManager().getDefaultDisplay().getHeight() - 
                (int)(50 * getResources().getDisplayMetrics().density);
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

    private void showProgressDialog() {
        try {
            if (progressDialog.isShowing()) {
                return;
            }
        } catch (NullPointerException nPEx) {
        }

        progressDialog = ProgressDialog.show(this, null, getString(R.string.romanblack_video_loading), true);
        progressDialog.setCancelable(true);
        progressDialog.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                handler.sendEmptyMessage(LOADING_ABORTED);
            }
        });
    }

    private void hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    private void closeActivity() {
        hideProgressDialog();
        finish();
    }

    public void onClick(View arg0) {
        try {
            if (arg0 == homeImageView) {
                finish();
            } else if (arg0 == btnPlayImageView) {
                if (playIsActive) {
                    if (playerState == PLAYER_STATES.STATE_PLAY) {
                        btnPlayImageView.setImageResource(R.drawable.romanblack_video_play);
                        mediaPlayer.pause();
                        playerState = PLAYER_STATES.STATE_PAUSE;
                    } else if (playerState == PLAYER_STATES.STATE_PAUSE) {
                        try {
                            btnPlayImageView.setImageResource(R.drawable.romanblack_video_pause);
                            mediaPlayer.start();
                            playerState = PLAYER_STATES.STATE_PLAY;
                            handler.sendEmptyMessage(CHECK_CONTROLS_STATE);
                            handler.sendEmptyMessage(UPDATE_SEEK_BAR);
                        } catch (Exception e) {
                            Log.d("", "");
                        }
                    } else if (playerState == PLAYER_STATES.STATE_STOP) {
                        try {
                            btnPlayImageView.setImageResource(R.drawable.romanblack_video_pause);
                            mediaPlayer.start();
                            playerState = PLAYER_STATES.STATE_PLAY;
                            handler.sendEmptyMessage(CHECK_CONTROLS_STATE);
                            handler.sendEmptyMessage(UPDATE_SEEK_BAR);
                        } catch (Exception e) {
                            Log.d("", "");
                        }
                    }
                }
            } else if (arg0 == btnNextImageView) {
                if (playIsActive) {
                    int videoDuration = mediaPlayer.getDuration();
                    videoCurrentPos = mediaPlayer.getCurrentPosition() + 5000;
                    if (videoCurrentPos > videoDuration - 256) {
                        videoCurrentPos = videoDuration - 256;
                    }
                    mediaPlayer.seekTo(videoCurrentPos);
                    if (playerState == PLAYER_STATES.STATE_PAUSE) {
                        handler.sendEmptyMessage(UPDATE_SEEK_BAR);
                    }
                }
            } else if (arg0 == btnPrevImageView) {
                if (playIsActive) {
                    videoCurrentPos = mediaPlayer.getCurrentPosition() - 5000;
                    if (videoCurrentPos < 0) {
                        videoCurrentPos = 0;
                    }
                    mediaPlayer.seekTo(videoCurrentPos);
                    if (playerState == PLAYER_STATES.STATE_PAUSE) {
                        handler.sendEmptyMessage(UPDATE_SEEK_BAR);
                    }
                }
            }
        } catch (NullPointerException nPEx) {
        }
    }

    public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
        handler.sendEmptyMessage(HIDE_PROGRESS_DIALOG);

        handler.sendEmptyMessage(VIDEOPLAYER_ERROR);

        return true;
    }

    public void onCompletion(MediaPlayer arg0) {
        videoCurrentPos = 0;
        playerState = PLAYER_STATES.STATE_STOP;
        btnPlayImageView.setImageResource(R.drawable.romanblack_video_play);
        arg0.seekTo(videoCurrentPos);

        handler.sendEmptyMessage(SHOW_CONTROLS);
    }

    public void onPrepared(MediaPlayer mp) {
        handler.sendEmptyMessage(HIDE_PROGRESS_DIALOG);

        videoWidth = mp.getVideoWidth();
        videoHeight = mp.getVideoHeight();
        
        prepareSurfaceLayoutParams();

        handler.sendEmptyMessage(UPDATE_SEEK_BAR);
        
        playerPrepared = true;
        
        if(surfaceCreated){
            if (playerState == PLAYER_STATES.STATE_PLAY) {
                btnActive = 5;
                handler.sendEmptyMessage(CHECK_CONTROLS_STATE);
            } else {
                Toast.makeText(PlayerWebActivity.this, R.string.romanblack_video_alert_press_play,
                        Toast.LENGTH_LONG).show();
            }
            playIsActive = true;
            mp.seekTo(videoCurrentPos);
        }
    }

    public void surfaceCreated(SurfaceHolder arg0) {
        mediaPlayer.setDisplay(surfaceHolder);
        
        surfaceCreated = true;
        
        if(playerPrepared){
            if (playerState == PLAYER_STATES.STATE_PLAY) {
                btnActive = 5;
                handler.sendEmptyMessage(CHECK_CONTROLS_STATE);
            } else {
                Toast.makeText(PlayerWebActivity.this, R.string.romanblack_video_alert_press_play,
                        Toast.LENGTH_LONG).show();
            }
            playIsActive = true;
            mediaPlayer.seekTo(videoCurrentPos);
        }
    }

    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
    }

    public void surfaceDestroyed(SurfaceHolder arg0) {
    }
}
