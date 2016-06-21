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
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.Secure;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.appbuilder.sdk.android.AppBuilderModuleMainAppCompat;
import com.appbuilder.sdk.android.StartUpActivity;
import com.appbuilder.sdk.android.Utils;
import com.appbuilder.sdk.android.Widget;
import com.appbuilder.sdk.android.authorization.Authorization;
import com.appbuilder.sdk.android.authorization.entities.User;
import com.flurry.android.FlurryAgent;
import com.ibuildapp.romanblack.VideoPlugin.adapters.MainAdapter;
import com.ibuildapp.romanblack.VideoPlugin.callbacks.OnAuthListener;
import com.ibuildapp.romanblack.VideoPlugin.callbacks.OnCommentPushedListener;
import com.ibuildapp.romanblack.VideoPlugin.callbacks.OnPostListener;
import com.ibuildapp.romanblack.VideoPlugin.callbacks.SharePressedListener;
import com.ibuildapp.romanblack.VideoPlugin.model.CommentItem;
import com.ibuildapp.romanblack.VideoPlugin.model.VideoItem;
import com.ibuildapp.romanblack.VideoPlugin.utils.EntityParser;
import com.ibuildapp.romanblack.VideoPlugin.utils.ShareUtils;
import com.ibuildapp.romanblack.VideoPlugin.utils.Statics;
import com.ibuildapp.romanblack.VideoPlugin.utils.VideoPluginConstants;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * Main module class. Module entry point.
 * Represents video list, video stream widgets.
 */
@StartUpActivity(moduleName = "Video")
public class VideoPlugin extends AppBuilderModuleMainAppCompat implements
        View.OnClickListener, SharePressedListener,
        OnPostListener, OnAuthListener, OnCommentPushedListener {

    private final int INITIALIZATION_FAILED = 0;
    private final int LOADING_ABORTED = 1;
    private final int SHOW_PROGRESS_DIALOG = 2;
    private final int HIDE_PROGRESS_DIALOG = 3;
    private final int SHOW_MEDIA_LIST = 4;
    private final int REFRESH_LIST = 5;
    private final int PING = 6;
    private final int COLORS_RECEIVED = 8;

    private boolean destroyed = false;
    private String cachePath = "";
    public static String userID = null;
    private Widget widget = null;
    private LinearLayout mainLayout = null;
    private TextView homeBtn = null;
    private RecyclerView listView = null;
    private ProgressDialog progressDialog = null;
    private ArrayList<VideoItem> items = new ArrayList<>();

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case INITIALIZATION_FAILED: {
                    Toast.makeText(VideoPlugin.this,
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
                case SHOW_MEDIA_LIST: {
                    showMediaList();
                }
                break;
                case PING: {
                    ping();
                }
                break;
                case COLORS_RECEIVED: {
                    colorsReceived();
                }
                break;
            }
        }
    };
    private int sharePosition;

    @Override
    public void create() {

        setContentView(R.layout.video_plugin_main);

        // topbar initialization
        setTopBarTitle(getString(R.string.romanblack_video_main_capture));

        setTopBarTitleColor(Color.parseColor("#000000"));

        setTopBarLeftButtonTextAndColor(getResources().getString(R.string.common_home_upper), Color.parseColor("#000000"), true, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        try {
            String packageName = getPackageName();
            String lastPart = packageName.substring(packageName.lastIndexOf(".") + 1);
            String uId = lastPart.substring(lastPart.indexOf("u") + 1, lastPart.indexOf("p"));
            userID = uId;
        } catch (Throwable thr) {
            thr.printStackTrace();
        }

        Intent currentIntent = getIntent();
        Bundle store = currentIntent.getExtras();
        widget = (Widget) store.getSerializable("Widget");
        if (widget == null) {
            handler.sendEmptyMessage(INITIALIZATION_FAILED);
            return;
        }

        if (widget.getPluginXmlData().length() == 0) {
            if (currentIntent.getStringExtra("WidgetFile").length() == 0) {
                handler.sendEmptyMessageDelayed(INITIALIZATION_FAILED, 3000);
                return;
            }
        }

        if (!TextUtils.isEmpty(widget.getTitle())) {
            setTopBarTitle(widget.getTitle());
        } else {
            setTopBarTitle(getResources().getString(R.string.romanblack_video_main_capture));
        }

        Statics.isOnline = Utils.networkAvailable(VideoPlugin.this);

        homeBtn = (TextView) findViewById(R.id.video_plugin_email_sign_up_main_home);
        listView = (RecyclerView) findViewById(R.id.video_plugin_main_list_view);
        mainLayout = (LinearLayout) findViewById(R.id.video_plugin_main_layout);

        cachePath = widget.getCachePath() + "/video-" + widget.getOrder();
        File cache = new File(this.cachePath);
        if (!cache.exists()) {
            cache.mkdirs();
        }

        Statics.setCachePath(cachePath);

        handler.sendEmptyMessage(PING);
        handler.sendEmptyMessage(SHOW_PROGRESS_DIALOG);

        new Thread() {
            @Override
            public void run() {
                EntityParser parser;

                if (widget.getPluginXmlData() != null) {
                    if (widget.getPluginXmlData().length() > 0) {
                        parser = new EntityParser(widget.getPluginXmlData());
                    } else {
                        String xmlData = readXmlFromFile(getIntent().getStringExtra("WidgetFile"));
                        parser = new EntityParser(xmlData);
                    }
                } else {
                    String xmlData = readXmlFromFile(getIntent().getStringExtra("WidgetFile"));
                    parser = new EntityParser(xmlData);
                }

                parser.parse();
                items = parser.getItems();
                Statics.APP_ID = parser.getAppId();
                Statics.APP_NAME = parser.getAppName();
                Statics.MODULE_ID = parser.getModuleId();
                Statics.sharingOn = parser.getSharingOn();
                Statics.commentsOn = parser.getCommentsOn();
                Statics.likesOn = parser.getLikesOn();

                Statics.color1 = parser.getColor1();
                Statics.color2 = parser.getColor2();
                Statics.color3 = parser.getColor3();
                Statics.color4 = parser.getColor4();
                Statics.color5 = parser.getColor5();
                Statics.isLight = parser.isLight();

                handler.sendEmptyMessage(COLORS_RECEIVED);

                for (int i = 0; i < items.size(); i++)
                    items.get(i).setTextColor(widget.getTextColor());

                handler.sendEmptyMessage(SHOW_MEDIA_LIST);
            }
        }.start();

        Statics.onAuthListeners.add(this);
        Statics.onPostListeners.add(this);
    }


    @Override
    public void destroy() {
        destroyed = true;

        try {
            Statics.onAuthListeners.remove(this);
            Statics.onPostListeners.remove(this);
            Statics.onCommentPushedListeners.remove(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VideoPluginConstants.FACEBOOK_AUTH_SHARE) {
            if (resultCode == RESULT_OK) {
                if (Authorization.isAuthorized(Authorization.AUTHORIZATION_TYPE_FACEBOOK)) {
                        ShareUtils.shareFacebook(this, items.get(sharePosition));
                    onAuth();
                }
            }
        } else if (requestCode == VideoPluginConstants.TWITTER_AUTH) {
            if (resultCode == RESULT_OK) {
                if (Authorization.getAuthorizedUser(Authorization.AUTHORIZATION_TYPE_TWITTER) != null) {
                    ShareUtils.shareTwitter(this, items.get(sharePosition));
                }
            }
        } else if ( requestCode == VideoPluginConstants.SHARING_FACEBOOK ) {
            if ( resultCode == RESULT_OK )
                Toast.makeText(VideoPlugin.this, getString(R.string.directoryplugin_facebook_posted_success), Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(VideoPlugin.this, getString(R.string.directoryplugin_facebook_posted_error), Toast.LENGTH_SHORT).show();
        } else if ( requestCode == VideoPluginConstants.SHARING_TWITTER ) {
            if ( resultCode == RESULT_OK )
                Toast.makeText(VideoPlugin.this, getString(R.string.directoryplugin_twitter_posted_success), Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(VideoPlugin.this, getString(R.string.directoryplugin_twitter_posted_error), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void resume() {
        handler.sendEmptyMessage(REFRESH_LIST);
        if (userID != null && userID.equals("186589")) {
            FlurryAgent.endTimedEvent("VideoPlugin");
        }
    }

    /**
     * Shows media list after parsing.
     */
    private void showMediaList() {
        if (items.isEmpty()) {
            return;
        }

        MainAdapter adapter = new MainAdapter(this, items, widget);
        adapter.setCachePath(cachePath);
        adapter.setSharePressedListener(this);
        listView.setLayoutManager(new LinearLayoutManager(this));
        listView.setAdapter(adapter);

        handler.sendEmptyMessage(HIDE_PROGRESS_DIALOG);
    }

    /**
     * Polls service when module is open.
     */
    private void ping() {
        new Thread(new Runnable() {
            public void run() {
                HttpParams params = new BasicHttpParams();
                params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION,
                        HttpVersion.HTTP_1_1);
                HttpClient httpClient = new DefaultHttpClient(params);

                try {
                    StringBuilder sb = new StringBuilder();
                    sb.append(Statics.BASE_URL);

                    sb.append("/");

                    HttpPost httpPost = new HttpPost(sb.toString());

                    String UID = Utils.md5(Secure.getString(getContentResolver(), Secure.ANDROID_ID));

                    MultipartEntity multipartEntity = new MultipartEntity();
                    multipartEntity.addPart("action", new StringBody("ping", Charset.forName("UTF-8")));
                    multipartEntity.addPart("platform", new StringBody("android", Charset.forName("UTF-8")));
                    multipartEntity.addPart("app_id", new StringBody(Statics.APP_ID, Charset.forName("UTF-8")));
                    multipartEntity.addPart("module_id", new StringBody(Statics.MODULE_ID, Charset.forName("UTF-8")));
                    multipartEntity.addPart("device", new StringBody(UID, Charset.forName("UTF-8")));

                    if (Authorization.getAuthorizedUser() != null) {
                        multipartEntity.addPart("account_id", new StringBody(Authorization.getAuthorizedUser().getAccountId(), Charset.forName("UTF-8")));
                        if (Authorization.getAuthorizedUser().getAccountType() == User.ACCOUNT_TYPES.FACEBOOK) {
                            multipartEntity.addPart("account_type", new StringBody("facebook", Charset.forName("UTF-8")));
                        } else if (Authorization.getAuthorizedUser().getAccountType() == User.ACCOUNT_TYPES.TWITTER) {
                            multipartEntity.addPart("account_type", new StringBody("twitter", Charset.forName("UTF-8")));
                        } else {
                            multipartEntity.addPart("account_type", new StringBody("ibuildapp", Charset.forName("UTF-8")));
                        }
                    }

                    httpPost.setEntity(multipartEntity);

                    String resp = httpClient.execute(httpPost, new BasicResponseHandler());

                    Log.d("", "");

                } catch (Exception e) {
                    Log.d("", "");
                }

                if (!destroyed) {
                    handler.sendEmptyMessageDelayed(PING, 30000);
                }
            }
        }).start();
    }


    private void colorsReceived() {
        setTopBarBackgroundColor(Statics.color1);
        mainLayout.setBackgroundColor(Statics.color1);
    }

    private void showProgressDialog() {
        if (progressDialog == null || progressDialog.isShowing()) {
            return;
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
        if (arg0 == homeBtn) {
            finish();
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.activity_open_scale, R.anim.activity_close_translate);
    }

    public void onSharePressed(int position) {
        sharePosition = position;
        ShareUtils.onSharePressed(this, items.get(position));
    }

    public void onPost() {
        handler.removeMessages(PING);
        handler.sendEmptyMessage(PING);
    }

    public void onAuth() {
        handler.removeMessages(PING);
        handler.sendEmptyMessage(PING);
    }

    public void onCommentPushed(CommentItem item) {
        if (item != null) {
            if (item.getReplyId() != 0) {
                return;
            }

            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).getId() == item.getTrackId()) {
                    items.get(i).setTotalComments(
                            items.get(i).getTotalComments() + 1);
                }
            }

            handler.sendEmptyMessage(REFRESH_LIST);
        }
    }

    public void onCommentsUpdate(VideoItem item, CommentItem commentItem, int count, int newCommentsCount, ArrayList<CommentItem> comments) {
        for (int i = 0; i < items.size(); i++) {
            VideoItem tmpItem = items.get(i);

            if (item.getId() == tmpItem.getId()) {
                tmpItem.setTotalComments(count);

                handler.sendEmptyMessageDelayed(REFRESH_LIST, 100);

                return;
            }
        }
    }
}