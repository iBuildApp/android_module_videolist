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
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.Secure;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import com.appbuilder.sdk.android.AppBuilderModuleMain;
import com.appbuilder.sdk.android.DialogSharing;
import com.appbuilder.sdk.android.Utils;
import com.appbuilder.sdk.android.Widget;
import com.appbuilder.sdk.android.authorization.Authorization;
import com.appbuilder.sdk.android.authorization.FacebookAuthorizationActivity;
import com.appbuilder.sdk.android.authorization.entities.User;
import com.flurry.android.FlurryAgent;
import com.ibuildapp.romanblack.VideoPlugin.callbacks.OnAuthListener;
import com.ibuildapp.romanblack.VideoPlugin.callbacks.OnCommentPushedListener;
import com.ibuildapp.romanblack.VideoPlugin.callbacks.OnPostListener;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main module class. Module entry point.
 * Represents video list, video stream widgets.
 */
public class VideoPlugin extends AppBuilderModuleMain implements
        OnItemClickListener, View.OnClickListener,
        MediaAdapter.FBLikePressedListener, MediaAdapter.SharePressedListener,
        OnPostListener, OnAuthListener, OnCommentPushedListener {

    public static final int VIDEO_PLAYER = 10002;
    private final int INITIALIZATION_FAILED = 0;
    private final int LOADING_ABORTED = 1;
    private final int SHOW_PROGRESS_DIALOG = 2;
    private final int HIDE_PROGRESS_DIALOG = 3;
    private final int SHOW_MEDIA_LIST = 4;
    private final int REFRESH_LIST = 5;
    private final int PING = 6;
    private final int GET_OG_LIKES = 7;
    private final int COLORS_RECIEVED = 8;
    private final int FACEBOOK_AUTH = 10000;
    private final int TWITTER_AUTH = 10001;
    private final int SHARING_FACEBOOK = 10002;
    private final int SHARING_TWITTER = 10003;
    private ACTIONS action = ACTIONS.ACTION_NONE;
    private boolean destroyed = false;
    private boolean needMenu = false;
    private int likePosition = 0;
    private int sharingPosition = 0;
    private String cachePath = "";
    public static String userID = null;
    private Widget widget = null;
    private MediaAdapter adapter = null;
    private LinearLayout mainLayout = null;
    private TextView homeBtn = null;
    private ListView listView = null;
    private ProgressDialog progressDialog = null;
    private ArrayList<VideoItem> items = new ArrayList<VideoItem>();
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
                case REFRESH_LIST: {
                    refreshList();
                }
                break;
                case PING: {
                    ping();
                }
                break;
                case GET_OG_LIKES: {
                    getOgLikes();
                }
                break;
                case COLORS_RECIEVED: {
                    colorsRecieved();
                }
                break;
            }
        }
    };

    @Override
    public void create() {

        Log.d("", "");
        setContentView(R.layout.romanblack_video_main);

        // topbar initialization
        setTopBarTitle(getString(R.string.romanblack_video_main_capture));
        boolean showSideBar = ((Boolean) getIntent().getExtras().getSerializable("showSideBar")).booleanValue();
        if (!showSideBar) {
            setTopBarLeftButtonText(getResources().getString(R.string.common_home_upper), true, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
        }

        try {
            String packageName = getPackageName();
            String lastPart = packageName.substring(packageName.lastIndexOf(".") + 1);
            String uId = lastPart.substring(lastPart.indexOf("u") + 1, lastPart.indexOf("p"));
            userID = uId;
        } catch (Throwable thr) {
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

        if (Utils.networkAvailable(VideoPlugin.this))
            Statics.isOnline = true;
        else
            Statics.isOnline = false;

        homeBtn = (TextView) findViewById(R.id.romanblack_fanwall_main_home);
        listView = (ListView) findViewById(R.id.romanblack_video_main_listview);
        mainLayout = (LinearLayout) findViewById(R.id.romanblack_video_main_layout);

        cachePath = widget.getCachePath() + "/video-" + widget.getOrder();
        File cache = new File(this.cachePath);
        if (!cache.exists()) {
            cache.mkdirs();
        }

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

                Statics.color1 = parser.getColor1();
                Statics.color2 = parser.getColor2();
                Statics.color3 = parser.getColor3();
                Statics.color4 = parser.getColor4();
                Statics.color5 = parser.getColor5();

                handler.sendEmptyMessage(COLORS_RECIEVED);

                for (int i = 0; i < items.size(); i++) {
                    items.get(i).setTextColor(widget.getTextColor());
                }

                // зачем то почистил каш директорию
                File dir = new File(cachePath);
                String[] files = dir.list();
                if (files != null) {
                    for (int i = 0; i < files.length; i++) {
                        String filename = files[i];
                        boolean fl = false;

                        if (fl == false) {
                            File file = new File(cachePath + "/" + filename);
                            file.delete();
                        }
                    }
                }

                handler.sendEmptyMessage(SHOW_MEDIA_LIST);
            }
        }.start();

        Statics.onAuthListeners.add(this);
        Statics.onPostListeners.add(this);
    }

    @Override
    public void start() {
    }

    @Override
    public void destroy() {
        destroyed = true;

        try {
            Statics.onAuthListeners.remove(this);
            Statics.onPostListeners.remove(this);
            Statics.onCommentPushedListeners.remove(this);
        } catch (Exception ex) {
        }
    }

    /**
     * Starts playing video with given position.
     * @param position video position
     */
    void startPlayer(int position) {
        if (userID != null && userID.equals("186589")) {
            Map<String, String> maps = new HashMap<String, String>();
            maps.put("Watch", items.get(position).getTitle());
            FlurryAgent.logEvent("VideoPlugin", maps, true);
        }

        if (items.get(position).getUrl().contains("youtube.com")) {
            this.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://www.youtube.com")).setData(Uri.parse(items.get(position).getUrl())));
            return;
        }
        if (items.get(position).getUrl().contains("vimeo.com")) {
            this.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(items.get(position).getUrl())));
            return;
        }

        if (items.get(position).getUrl().contains("m3u8")
                || items.get(position).getUrl().contains("mp4")) {
            Intent it = new Intent(this, VideoBuffer.class);
            it.putExtra("position", position);
            it.putExtra("items", items);
            it.putExtra("Widget", widget);
            this.startActivityForResult(it, VideoPlugin.VIDEO_PLAYER);
            return;
        }

        Intent it = new Intent(this, PlayerWebActivity.class);
        it.putExtra("position", position);
        it.putExtra("items", items);
        it.putExtra("Widget", widget);
        this.startActivity(it);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FACEBOOK_AUTH) {
            if (resultCode == RESULT_OK) {
                if (Authorization.isAuthorized(Authorization.AUTHORIZATION_TYPE_FACEBOOK)) {
                    if (action == ACTIONS.FACEBOOK_LIKE) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    boolean res = FacebookAuthorizationActivity.like(items.get(likePosition).getUrl());
                                    if ( res )
                                    {
                                        items.get(likePosition).setLikesCount(items.get(likePosition).getLikesCount() + 1);
                                        items.get(likePosition).setLiked(true);

                                        handler.sendEmptyMessage(REFRESH_LIST);
                                    }
                                } catch (FacebookAuthorizationActivity.FacebookNotAuthorizedException e) {

                                } catch (FacebookAuthorizationActivity.FacebookAlreadyLiked facebookAlreadyLiked) {
                                    items.get(likePosition).setLiked(true);
                                    handler.sendEmptyMessage(REFRESH_LIST);
                                }
                            }
                        }).start();
                    } else if (action == ACTIONS.FACEBOOK_SHARE) {
                        shareFacebook(sharingPosition);
                    }
                    onAuth();
                }
            }
        } else if (requestCode == TWITTER_AUTH) {
            if (resultCode == RESULT_OK) {
                if (Authorization.getAuthorizedUser(Authorization.AUTHORIZATION_TYPE_TWITTER) != null) {
                    shareTwitter(sharingPosition);
                }
            }
        } else if (requestCode == VIDEO_PLAYER) {
            Intent intent = data;
            try {
                Bundle bundle = intent.getExtras();
                List<VideoItem> items = (List<VideoItem>) bundle.getSerializable("items");
                int position = bundle.getInt("position");
                if (userID != null && userID.equals("186589")) {
                    FlurryAgent.endTimedEvent("VideoPlugin");
                }
                if (resultCode == Statics.PLAY_NEXT_VIDEO) {
                    if (position + 1 >= items.size()) {
                        position = 0;
                    } else {
                        position++;
                    }
                    startPlayer(position);
                } else if (resultCode == Statics.PLAY_PREV_VIDEO) {
                    if (position - 1 < 0) {
                        position = items.size() - 1;
                    } else {
                        position--;
                    }
                    startPlayer(position);
                }
            } catch (Exception e) {
            }
        } else if ( requestCode == SHARING_FACEBOOK )
        {
            if ( resultCode == RESULT_OK )
                Toast.makeText(VideoPlugin.this, getString(R.string.directoryplugin_facebook_posted_success), Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(VideoPlugin.this, getString(R.string.directoryplugin_facebook_posted_error), Toast.LENGTH_SHORT).show();
        } else if ( requestCode == SHARING_TWITTER )
        {
            if ( resultCode == RESULT_OK )
                Toast.makeText(VideoPlugin.this, getString(R.string.directoryplugin_twitter_posted_success), Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(VideoPlugin.this, getString(R.string.directoryplugin_twitter_posted_error), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * This menu contains share via Facebook, Twitter, Email, SMS buttons.
     * Also it contains "cancel" button.
     * @param menu
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        menu.add("Facebook").setOnMenuItemClickListener(new OnMenuItemClickListener() {
//            public boolean onMenuItemClick(MenuItem arg0) {
//                if (Authorization.isAuthorized(Authorization.AUTHORIZATION_TYPE_FACEBOOK)) {
//                    shareFacebook(sharingPosition);
//                } else {
//                    action = ACTIONS.FACEBOOK_SHARE;
//                    Authorization.authorize(VideoPlugin.this, FACEBOOK_AUTH, Authorization.AUTHORIZATION_TYPE_FACEBOOK);
//                }
//                needMenu = false;
//                return true;
//            }
//        });
//        menu.add("Twitter").setOnMenuItemClickListener(new OnMenuItemClickListener() {
//            public boolean onMenuItemClick(MenuItem arg0) {
//                if (Authorization.getAuthorizedUser(Authorization.AUTHORIZATION_TYPE_TWITTER) != null) {
//                    shareTwitter(sharingPosition);
//                } else {
//                    Authorization.authorize(VideoPlugin.this, TWITTER_AUTH, Authorization.AUTHORIZATION_TYPE_TWITTER);
//                }
//                needMenu = false;
//                return true;
//            }
//        });
//        menu.add("Email").setOnMenuItemClickListener(new OnMenuItemClickListener() {
//            public boolean onMenuItemClick(MenuItem arg0) {
//                String text = getResources().getString(R.string.romanblack_video_sharingsms_first_part) + " "
//                        + items.get(sharingPosition).getUrl()
//                        + " " + getResources().getString(R.string.romanblack_video_sharingsms_second_part) + " "
//                        + Statics.APP_NAME + " "
//                        + getResources().getString(R.string.romanblack_video_sharingsms_third_part)
//                        + Statics.APP_NAME + " "
//                        + getResources().getString(R.string.romanblack_video_sharingsms_fourth_part)
//                        + " "
//                        + "http://ibuildapp.com/projects.php?action=info&projectid=" + Statics.APP_ID;
//
//                Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
//                emailIntent.setType("text/html");
//                emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, Html.fromHtml(text));
//                startActivity(emailIntent);
//
//                needMenu = false;
//
//                return true;
//            }
//        });
//        menu.add("SMS").setOnMenuItemClickListener(new OnMenuItemClickListener() {
//            public boolean onMenuItemClick(MenuItem arg0) {
//                String text = getResources().getString(R.string.romanblack_video_sharingsms_first_part) + " "
//                        + items.get(sharingPosition).getUrl()
//                        + " " + getResources().getString(R.string.romanblack_video_sharingsms_second_part) + " "
//                        + Statics.APP_NAME + " "
//                        + getResources().getString(R.string.romanblack_video_sharingsms_third_part)
//                        + Statics.APP_NAME + " "
//                        + getResources().getString(R.string.romanblack_video_sharingsms_fourth_part)
//                        + " "
//                        + "http://ibuildapp.com/projects.php?action=info&projectid=" + Statics.APP_ID;
//
//                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:"));
//                intent.putExtra("sms_body", text);
//                startActivity(intent);
//
//                needMenu = false;
//
//                return true;
//            }
//        });
//        menu.add(getString(R.string.romanblack_video_cancel)).setOnMenuItemClickListener(new OnMenuItemClickListener() {
//            public boolean onMenuItemClick(MenuItem arg0) {
//                needMenu = false;
//
//                return true;
//            }
//        });

        return false;
    }

    @Override
    public void resume() {
        handler.sendEmptyMessage(REFRESH_LIST);
        if (userID != null && userID.equals("186589")) {
            FlurryAgent.endTimedEvent("VideoPlugin");
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return needMenu;
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        needMenu = false;
    }

    /**
     * Shows media list after parsing.
     */
    private void showMediaList() {
        if (items.isEmpty()) {
            return;
        }

        adapter = new MediaAdapter(this, items, widget);
        adapter.setCachePath(cachePath);
        adapter.setfBLikePressedListener(this);
        adapter.setSharePressedListener(this);
        listView.setAdapter(adapter);

        Statics.onCommentPushedListeners.add(this);

        listView.setOnItemClickListener(this);

        handler.sendEmptyMessage(GET_OG_LIKES);

        new Thread(new Runnable() {
            public void run() {
                HashMap<String, String> commentCounts =
                        JSONParser.getVideoCommentsCount();

                for (int i = 0; i < items.size(); i++) {
                    int count = 0;

                    try {
                        String key = items.get(i).getId() + "";

                        count = Integer.parseInt(commentCounts.get(key));
                    } catch (Exception ex) {
                        Log.d("", "");
                    }

                    items.get(i).setTotalComments(count);
                }

                handler.sendEmptyMessage(REFRESH_LIST);

                HashMap<String, String> likesCounts =
                        JSONParser.getVideoLikesCount(items);

                if ( likesCounts != null )
                {
                    for (int i = 0; i < items.size(); i++) {
                        try {
                            String iCountS =
                                    likesCounts.get(items.get(i).getUrl());
                            items.get(i).setLikesCount(Integer.parseInt(iCountS));
                        } catch (Exception ex) {
                            Log.d("", "");
                        }
                    }
                }
                handler.sendEmptyMessage(REFRESH_LIST);
            }
        }).start();

        handler.sendEmptyMessage(HIDE_PROGRESS_DIALOG);
    }

    /**
     * Refreshes media list.
     */
    private void refreshList() {
        try {
            adapter.notifyDataSetChanged();
        } catch (Exception ex) {
        }
    }

    /**
     * Prepares videos open graph likes.
     */
    private void getOgLikes() {
        if (Authorization.isAuthorized(Authorization.AUTHORIZATION_TYPE_FACEBOOK)) {
            new Thread(new Runnable() {
                public void run() {
                    ArrayList<String> urls = JSONParser.getUserOgLikes();

                    for (int i = 0; i < items.size(); i++) {
                        for (int j = 0; j < urls.size(); j++) {
                            if (items.get(i).getUrl().equalsIgnoreCase(urls.get(j))) {
                                items.get(i).setLiked(true);

                                break;
                            }
                        }
                    }

                    handler.sendEmptyMessage(REFRESH_LIST);
                }
            }).start();
        }
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

    /**
     * Calling when module colors was recieved.
     */
    private void colorsRecieved() {
        mainLayout.setBackgroundColor(Statics.color1);
        listView.setDivider(new ColorDrawable(Color.parseColor(Statics.color1 == android.R.color.white ? "#66000000" : "#33000000")));
        listView.setDividerHeight(1);
    }

    /**
     * Starts share on facebook activity.
     * @param position video position
     */
    private void shareFacebook(int position) {
        Intent it = new Intent(this, SharingActivity.class);
        it.putExtra("type", "facebook");
        it.putExtra("link", items.get(position).getUrl());
        it.putExtra("item", items.get(position));
        startActivityForResult(it, SHARING_FACEBOOK);

    }

    /**
     * Starts share on twitter activity.
     * @param position video position
     */
    private void shareTwitter(int position) {
        Intent it = new Intent(this, SharingActivity.class);
        it.putExtra("type", "twitter");
        it.putExtra("link", items.get(position).getUrl());
        it.putExtra("item", items.get(position));
        startActivityForResult(it, SHARING_TWITTER);
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

    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
    }

    public void onClick(View arg0) {
        if (arg0 == homeBtn) {
            finish();
        }
    }

    public void onLikePressed(final int position) {
        if (Authorization.isAuthorized(Authorization.AUTHORIZATION_TYPE_FACEBOOK)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        boolean res = FacebookAuthorizationActivity.like(items.get(position).getUrl());
                        if ( res )
                        {
                            items.get(position).setLikesCount(items.get(position).getLikesCount() + 1);
                            items.get(position).setLiked(true);

                            handler.sendEmptyMessage(REFRESH_LIST);
                        }
                    } catch (FacebookAuthorizationActivity.FacebookNotAuthorizedException e) {
                        action = ACTIONS.FACEBOOK_LIKE;
                        likePosition = position;
                        Authorization.authorize(VideoPlugin.this, FACEBOOK_AUTH, Authorization.AUTHORIZATION_TYPE_FACEBOOK);
                    } catch (FacebookAuthorizationActivity.FacebookAlreadyLiked facebookAlreadyLiked) {
                        items.get(position).setLiked(true);
                        handler.sendEmptyMessage(REFRESH_LIST);
                    }
                }
            }).start();
        } else {
            action = ACTIONS.FACEBOOK_LIKE;
            likePosition = position;

            Authorization.authorize(this, FACEBOOK_AUTH, Authorization.AUTHORIZATION_TYPE_FACEBOOK);
        }
    }

    public void onSharePressed(int position) {
        sharingPosition = position;

        needMenu = true;

        showDialogSharing(new DialogSharing.Configuration.Builder()
                        .setFacebookSharingClickListener(new DialogSharing.Item.OnClickListener() {
                            @Override
                            public void onClick() {
                                if (Authorization.isAuthorized(Authorization.AUTHORIZATION_TYPE_FACEBOOK)) {
                                    shareFacebook(sharingPosition);
                                } else {
                                    action = ACTIONS.FACEBOOK_SHARE;
                                    Authorization.authorize(VideoPlugin.this, FACEBOOK_AUTH, Authorization.AUTHORIZATION_TYPE_FACEBOOK);
                                }
                                needMenu = false;
                            }
                        })
                        .setTwitterSharingClickListener(new DialogSharing.Item.OnClickListener() {
                            @Override
                            public void onClick() {
                                if (Authorization.getAuthorizedUser(Authorization.AUTHORIZATION_TYPE_TWITTER) != null) {
                                    shareTwitter(sharingPosition);
                                } else {
                                    Authorization.authorize(VideoPlugin.this, TWITTER_AUTH, Authorization.AUTHORIZATION_TYPE_TWITTER);
                                }
                                needMenu = false;
                            }
                        })
                        .setEmailSharingClickListener(new DialogSharing.Item.OnClickListener() {
                            @Override
                            public void onClick() {
                                String text = getResources().getString(R.string.romanblack_video_sharingsms_first_part) + " "
                                        + items.get(sharingPosition).getUrl()
                                        + " " + getResources().getString(R.string.romanblack_video_sharingsms_second_part) + " "
                                        + Statics.APP_NAME + " "
                                        + getResources().getString(R.string.romanblack_video_sharingsms_third_part)
                                        + Statics.APP_NAME + " "
                                        + getResources().getString(R.string.romanblack_video_sharingsms_fourth_part)
                                        + " "
                                        + "http://ibuildapp.com/projects.php?action=info&projectid=" + Statics.APP_ID;

                                Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                                emailIntent.setType("text/html");
                                emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, Html.fromHtml(text));
                                startActivity(emailIntent);

                                needMenu = false;
                            }
                        })
                        .setSmsSharingClickListener(new DialogSharing.Item.OnClickListener() {
                            @Override
                            public void onClick() {
                                String text = getResources().getString(R.string.romanblack_video_sharingsms_first_part) + " "
                                        + items.get(sharingPosition).getUrl()
                                        + " " + getResources().getString(R.string.romanblack_video_sharingsms_second_part) + " "
                                        + Statics.APP_NAME + " "
                                        + getResources().getString(R.string.romanblack_video_sharingsms_third_part)
                                        + Statics.APP_NAME + " "
                                        + getResources().getString(R.string.romanblack_video_sharingsms_fourth_part)
                                        + " "
                                        + "http://ibuildapp.com/projects.php?action=info&projectid=" + Statics.APP_ID;

                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:"));
                                intent.putExtra("sms_body", text);
                                startActivity(intent);

                                needMenu = false;
                            }
                        })
                        .build()
        );

//        openOptionsMenu();
    }

    public void onPost() {
        handler.removeMessages(PING);
        handler.sendEmptyMessage(PING);
    }

    public void onAuth() {
        handler.removeMessages(PING);
        handler.sendEmptyMessage(PING);
        handler.sendEmptyMessage(GET_OG_LIKES);
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

    private enum ACTIONS {

        FACEBOOK_LIKE, FACEBOOK_SHARE, ACTION_NONE
    }
}
