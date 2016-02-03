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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import com.appbuilder.sdk.android.AppBuilderModuleMain;
import com.appbuilder.sdk.android.DialogSharing;
import com.appbuilder.sdk.android.Utils;
import com.appbuilder.sdk.android.Widget;
import com.appbuilder.sdk.android.authorization.Authorization;
import com.appbuilder.sdk.android.authorization.FacebookAuthorizationActivity;
import com.ibuildapp.romanblack.VideoPlugin.callbacks.OnCommentPushedListener;
import org.apache.http.util.ByteArrayBuffer;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;

/**
 * This activity represents video preview page.
 */
public class VideoPlayer extends AppBuilderModuleMain implements OnClickListener,
        OnCommentPushedListener {

    private final int INITIALIZATION_FAILED = 0;
    private final int LOADING_ABORTED = 1;
    private final int SHOW_PROGRESS_DIALOG = 2;
    private final int HIDE_PROGRESS_DIALOG = 3;
    private final int SHOW_COMMENTS_LIST = 4;
    private final int NEED_INTERNET_CONNECTION = 5;
    private final int REFRESH_LIST = 6;
    private final int HIDE_LIKE_BUTTON = 7;
    private final int UPDATE_LIKE_COUNTER = 8;
    private final int FACEBOOK_AUTH = 10000;
    private final int TWITTER_AUTH = 10001;
    private final int AUTHORIZATION_ACTIVITY = 10002;
    private final int SEND_COMMENT_ACTIVITY = 10003;
    private ACTIONS action = ACTIONS.ACTION_NONE;
    private boolean needMenu = false;
    private int position = 0;
    private String cachePath = "";
    private VideoItem item = null;
    private Widget widget = null;
    private CommentsAdapter adapter = null;
    private Intent actionIntent = null;
    private LinearLayout mainLayout = null;
    private TextView videoTitleTextView = null;
    private ListView listView = null;
    private RelativeLayout videoPreview = null;
    private ImageView videoPreviewImageView = null;
    private ProgressDialog progressDialog = null;
    private View headerView = null;
    private ImageView shareButton = null;
    private ImageView postCommentButton = null;
    private TextView likesCountTextView = null;
    private LinearLayout likeButton = null;
    private LinearLayout bottomPanel = null;
    private ArrayList<VideoItem> items = null;
    private ArrayList<CommentItem> comments = null;
    private View likeLayout;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case INITIALIZATION_FAILED: {
                    Toast.makeText(VideoPlayer.this,
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
                case SHOW_COMMENTS_LIST: {
                    showCommentsList();
                }
                break;
                case NEED_INTERNET_CONNECTION: {
                    Toast.makeText(VideoPlayer.this,
                            getResources().getString(
                            R.string.romanblack_video_alert_no_internet),
                            Toast.LENGTH_LONG).show();
                }
                break;
                case REFRESH_LIST: {
                    refreshList();
                }
                break;
                case HIDE_LIKE_BUTTON: {
                    hideLikeButton();
                }
                break;
                case UPDATE_LIKE_COUNTER: {
                    updateLikeCounter();
                }
                break;
            }
        }
    };

    @Override
    public void create() {
        setContentView(R.layout.romanblack_video_player);

        Intent currentIntent = getIntent();
        items = (ArrayList<VideoItem>) currentIntent.getSerializableExtra("items");

        if (items == null || items.isEmpty()) {
            handler.sendEmptyMessage(INITIALIZATION_FAILED);
            return;
        }

        position = currentIntent.getIntExtra("position", 0);
        try {
            item = items.get(position);
        } catch (IndexOutOfBoundsException iOOBEx) {
            handler.sendEmptyMessage(INITIALIZATION_FAILED);
            return;
        }

        if (item == null) {
            handler.sendEmptyMessage(INITIALIZATION_FAILED);
            return;
        }

        if (Utils.networkAvailable(VideoPlayer.this)){
            Statics.isOnline = true;
        } else {
            Statics.isOnline = false;
        }

        widget = (Widget) currentIntent.getSerializableExtra("Widget");
        cachePath = currentIntent.getStringExtra("cachePath");

        mainLayout = (LinearLayout) findViewById(R.id.romanblack_video_player_main);
        mainLayout.setBackgroundColor(Statics.color1);

        if (!TextUtils.isEmpty(item.getTitle()))
            setTopBarTitle(item.getTitle());
        else
            setTopBarTitle(getString(R.string.romanblack_video_player_capture));

        swipeBlock();
        setTopBarLeftButtonText(getResources().getString(R.string.common_back_upper), true, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        headerView = LayoutInflater.from(this).inflate(R.layout.romanblack_video_player_comments_header, null);
        headerView.setBackgroundColor(Statics.color1);

        videoTitleTextView = (TextView) headerView.findViewById(R.id.romanblack_video_player_video_title);
        videoTitleTextView.setText(item.getTitle());

        likeLayout = headerView.findViewById(R.id.video_detail_like_layout);
        if (Statics.likesOn.equalsIgnoreCase("on")) {
            likeLayout.setVisibility(View.VISIBLE);
        }else likeLayout.setVisibility(View.INVISIBLE);

        likesCountTextView = (TextView) headerView.findViewById(R.id.romanblack_video_player_comments_header_likes_count);
        likesCountTextView.setText(item.getLikesCount() + "");

        likeButton = (LinearLayout) headerView.findViewById(R.id.romanblack_video_player_comments_header_like_btn);
        likeButton.setOnClickListener(this);

        listView = (ListView) findViewById(R.id.romanblack_video_player_listview);
        listView.setCacheColorHint(Color.parseColor("#41464b"));
        listView.setHeaderDividersEnabled(true);
        listView.addHeaderView(headerView, null, true);
        listView.setDivider(new ColorDrawable(Color.parseColor(Statics.color1 == android.R.color.white ? "#66000000" : "#33000000")));
        listView.setDividerHeight(1);

        videoPreview = (RelativeLayout) headerView.findViewById(R.id.romanblack_video_player_preview);
        videoPreview.setOnClickListener(this);

        videoPreviewImageView = (ImageView) headerView.findViewById(R.id.romanblack_video_player_preview_img);
        ImageDownloadTask idt = new ImageDownloadTask();
        idt.execute(item);

        shareButton = (ImageView) findViewById(R.id.romanblack_video_player_share_btn);
        try {
            shareButton.setColorFilter(bottomBarDesign.leftButtonDesign.textColor);
        } catch (NullPointerException nPEx) {
        }
        shareButton.setOnClickListener(this);

        postCommentButton = (ImageView) findViewById(R.id.romanblack_video_player_comment_btn);
        try {
            postCommentButton.setColorFilter(bottomBarDesign.leftButtonDesign.textColor);
        } catch (NullPointerException nPEx) {
        }
        postCommentButton.setOnClickListener(this);
        if (Statics.commentsOn.equals("on"))
            postCommentButton.setVisibility(View.VISIBLE);
        else postCommentButton.setVisibility(View.GONE);

        bottomPanel = (LinearLayout) findViewById(R.id.romanblack_video_player_bottom_panel);

        if (!Statics.isOnline) {
            shareButton.setAlpha(100);
            postCommentButton.setAlpha(100);
            ImageView likeImage = (ImageView) headerView.findViewById(R.id.romanblack_video_player_comments_header_like_image);
            likeImage.setAlpha(100);
            TextView likeCaption = (TextView) headerView.findViewById(R.id.romanblack_video_player_comments_header_like_caption);
            likeCaption.setTextColor(Color.parseColor("#9bffffff"));
            likeButton.getBackground().setAlpha(100);
        }

        if (item.isLiked()) {
            handler.sendEmptyMessage(HIDE_LIKE_BUTTON);
        }

        if (Statics.sharingOn.equalsIgnoreCase("off")
                && Statics.commentsOn.equalsIgnoreCase("off")) {
            bottomPanel.setVisibility(View.GONE);
        } else if (Statics.sharingOn.equalsIgnoreCase("off")) {
            shareButton.setVisibility(View.INVISIBLE);
        } else if (Statics.commentsOn.equalsIgnoreCase("off")) {
            postCommentButton.setVisibility(View.INVISIBLE);
        }

        handler.sendEmptyMessage(SHOW_PROGRESS_DIALOG);

        new Thread(new Runnable() {
            public void run() {

                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo ni = cm.getActiveNetworkInfo();

                boolean isOnline = false;

                if (ni != null) {
                    isOnline = ni.isConnectedOrConnecting();
                }

                if (isOnline) {
                    String commentsUrl = Statics.BASE_URL + "/getcomments/"
                            + com.appbuilder.sdk.android.Statics.appId + "/" + Statics.MODULE_ID + "/"
                            + item.getId() + "/0/" 
                            + com.appbuilder.sdk.android.Statics.appId + "/"
                            + com.appbuilder.sdk.android.Statics.appToken;

                    comments = JSONParser.parseCommentsUrl(commentsUrl);
                } else {
                    try {
                        FileInputStream fis = new FileInputStream(
                                cachePath + "/" + "ca-" + item.getId() + "-0");
                        ObjectInputStream ois = new ObjectInputStream(fis);
                        comments = (ArrayList<CommentItem>) ois.readObject();
                        ois.close();
                        fis.close();
                    } catch (FileNotFoundException fNFEx) {
                        Log.d("", "");
                    } catch (IOException iOEx) {
                        Log.d("", "");
                    } catch (ClassNotFoundException cNFEx) {
                        Log.d("", "");
                    }
                }
                
                if(comments != null){
                    Collections.reverse(comments);
                }

                handler.sendEmptyMessage(SHOW_COMMENTS_LIST);
            }
        }).start();

        Statics.onCommentPushedListeners.add(this);
    }

    @Override
    public void destroy() {
        Statics.onCommentPushedListeners.remove(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FACEBOOK_AUTH) {
            if (resultCode == RESULT_OK) {
                if (Authorization.getAuthorizedUser(Authorization.AUTHORIZATION_TYPE_FACEBOOK) != null) {
                    if (action == ACTIONS.FACEBOOK_LIKE) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    boolean res = FacebookAuthorizationActivity.like(items.get(position).getUrl());
                                    if ( res )
                                    {
                                        handler.sendEmptyMessage(UPDATE_LIKE_COUNTER);
                                        handler.sendEmptyMessage(HIDE_LIKE_BUTTON);
                                    }
                                } catch (FacebookAuthorizationActivity.FacebookNotAuthorizedException e) {
                                } catch (FacebookAuthorizationActivity.FacebookAlreadyLiked facebookAlreadyLiked) {
                                    handler.sendEmptyMessage(HIDE_LIKE_BUTTON);
                                }
                            }
                        }).start();
                    } else if (action == ACTIONS.FACEBOOK_SHARE) {
                        shareFacebook();
                    }
                }
            }
        } else if (requestCode == TWITTER_AUTH) {
            if (resultCode == RESULT_OK) {
                if (Authorization.getAuthorizedUser(Authorization.AUTHORIZATION_TYPE_TWITTER) != null) {
                    shareTwitter();
                }
            }
        } else if (requestCode == AUTHORIZATION_ACTIVITY) {
            if (resultCode == RESULT_OK) {

                if (action == ACTIONS.SEND_MESSAGE) {
                    startActivityForResult(actionIntent, SEND_COMMENT_ACTIVITY);
                }
            } else {
            }
        } else if (requestCode == SEND_COMMENT_ACTIVITY) {
            if (resultCode == RESULT_OK) {
            }
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
//        menu.add("Facebook").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//            public boolean onMenuItemClick(MenuItem arg0) {
//                if (Authorization.getAuthorizedUser(Authorization.AUTHORIZATION_TYPE_FACEBOOK) != null) {
//                    shareFacebook();
//                } else {
//                    action = ACTIONS.FACEBOOK_SHARE;
//                    Authorization.authorize(VideoPlayer.this, FACEBOOK_AUTH, Authorization.AUTHORIZATION_TYPE_FACEBOOK);
//                }
//
//                needMenu = false;
//
//                return true;
//            }
//        });
//        menu.add("Twitter").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//            public boolean onMenuItemClick(MenuItem arg0) {
//                if (Authorization.getAuthorizedUser(Authorization.AUTHORIZATION_TYPE_TWITTER) != null) {
//                    shareTwitter();
//                } else {
//                    Authorization.authorize(VideoPlayer.this, TWITTER_AUTH, Authorization.AUTHORIZATION_TYPE_TWITTER);
//                }
//
//                needMenu = false;
//
//                return true;
//            }
//        });
//        menu.add("Email").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//            public boolean onMenuItemClick(MenuItem arg0) {
//                String text = getResources().getString(R.string.romanblack_video_sharingsms_first_part) + " " + item.getUrl()
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
//        menu.add("SMS").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//            public boolean onMenuItemClick(MenuItem arg0) {
//                String text = getResources().getString(R.string.romanblack_video_sharingsms_first_part) + " " + item.getUrl()
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
//        menu.add(getString(R.string.romanblack_video_cancel)).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//            public boolean onMenuItemClick(MenuItem arg0) {
//                needMenu = false;
//
//                return true;
//            }
//        });

        return false;
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
     * Updates video likes counter.
     */
    private void updateLikeCounter() {
        item.setLikesCount(item.getLikesCount() + 1);
        likesCountTextView.setText(item.getLikesCount() + "");
    }

    /**
     * Hides like button after user liked the video.
     */
    private void hideLikeButton() {
        ImageView likeImage = (ImageView) headerView.findViewById(R.id.romanblack_video_player_comments_header_like_image);
        likeImage.setAlpha(100);
        TextView likeCaption = (TextView) headerView.findViewById(R.id.romanblack_video_player_comments_header_like_caption);
        likeCaption.setTextColor(Color.parseColor("#9bffffff"));
        likeButton.getBackground().setAlpha(100);
    }

    /**
     * Sets the video thumbnail image after it was loaded.
     */
    private void setThumb() {
        if (videoPreviewImageView != null) {
            if (item.getCoverPath().length() > 0) {

                Bitmap bitmap = null;

                try {
                    bitmap = decodeImageFile(item.getCoverPath());
                } catch (Exception e) {
                    Log.d("", "");
                }

                if (bitmap != null) {
                    BitmapDrawable bDrw = new BitmapDrawable(bitmap);
                    videoPreviewImageView.setImageDrawable(bDrw);
                }
            }
        }
    }

    /**
     * Decodes image file to bitmap from device external storage.
     * @param imagePath image file path
     * @return decoded image bitmap
     */
    private Bitmap decodeImageFile(String imagePath) {
        try {
            int imageWidth = getResources().getDisplayMetrics().widthPixels;
            int imageHeight = (int) (150 * getResources().getDisplayMetrics().density);

            File file = new File(imagePath);
            //Decode image size
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(file), null, opts);

            //Find the correct scale value. It should be the power of 2.
            int width = opts.outWidth, height = opts.outHeight;
            int scale = 1;
            while (true) {
                if (width / 2 < imageWidth || height / 2 < imageHeight) {
                    break;
                }
                width /= 2;
                height /= 2;
                scale *= 2;
            }

            //Decode with inSampleSize
            opts = new BitmapFactory.Options();
            opts.inSampleSize = scale;

            Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(file), null, opts);

            return bitmap;
        } catch (Exception e) {
            Log.d("", "");
        }

        return null;
    }

    /**
     * Refreshes comments list.
     */
    private void refreshList() {
        adapter.notifyDataSetChanged();
    }

    /**
     * Shows comments list if it is not empty.
     */
    private void showCommentsList() {
        if (comments == null) {
            comments = new ArrayList<CommentItem>();
        }

        adapter = new CommentsAdapter(this, comments, item, widget);
        adapter.setCachePath(cachePath);
        listView.setAdapter(adapter);

        handler.sendEmptyMessage(HIDE_PROGRESS_DIALOG);

        cacheMessages();
    }

    /**
     * Caches comments list to device external storage.
     */
    private void cacheMessages() {
        File cacheFile = new File(cachePath);
        if (!cacheFile.exists()) {
            cacheFile.mkdirs();
        }

        File cache = new File(cachePath + "/" + "ca-" + item.getId() + "-0");
        if (cache.exists()) {
            cache.delete();
        }

        try {
            cache.createNewFile();
        } catch (IOException iOEx) {
        }

        ArrayList<CommentItem> cMessages = new ArrayList<CommentItem>();

        if ((comments.size()) <= 20 && (!comments.isEmpty())) {
            cMessages = comments;
        } else if (comments.size() > 20) {
            for (int i = 0; i < 20; i++) {
                cMessages.add(comments.get(i));
            }
        }

        try {
            FileOutputStream fos = new FileOutputStream(cache);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(cMessages);
            oos.close();
            fos.close();
        } catch (IOException iOEx) {
        }
    }

    /**
     * Starts sharing activity to share on Facebook.
     */
    private void shareFacebook() {
        Intent it = new Intent(this, SharingActivity.class);
        it.putExtra("type", "facebook");
        it.putExtra("link", item.getUrl());
        it.putExtra("item", item);
        startActivity(it);
    }

    /**
     * Starts sharing activity to share on Twitter.
     */
    private void shareTwitter() {
        Intent it = new Intent(this, SharingActivity.class);
        it.putExtra("type", "twitter");
        it.putExtra("link", item.getUrl());
        it.putExtra("item", item);
        startActivity(it);
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
        if (arg0 == videoPreview) {
            if (item.getUrl().contains("youtube.com")) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://www.youtube.com")).setData(Uri.parse(item.getUrl())));
                return;
            }
            if (item.getUrl().contains("vimeo.com")) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(item.getUrl())));
                return;
            }

            if (item.getUrl().contains("m3u8")) {
                Intent it = new Intent(this, VideoBuffer.class);
                it.putExtra("position", position);
                it.putExtra("items", items);
                it.putExtra("Widget", widget);
                startActivity(it);
                return;
            }
            Intent it = new Intent(this, PlayerWebActivity.class);
            it.putExtra("position", position);
            it.putExtra("items", items);
            it.putExtra("Widget", widget);
            startActivity(it);
        } else if (arg0 == likeButton) {
            if ( Utils.networkAvailable( VideoPlayer.this ) ) {
                if (Authorization.getAuthorizedUser(Authorization.AUTHORIZATION_TYPE_FACEBOOK) != null) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                boolean res = FacebookAuthorizationActivity.like(items.get(position).getUrl());
                                if ( res )
                                {
                                    handler.sendEmptyMessage(UPDATE_LIKE_COUNTER);
                                    handler.sendEmptyMessage(HIDE_LIKE_BUTTON);
                                }
                            } catch (FacebookAuthorizationActivity.FacebookNotAuthorizedException e) {
                            } catch (FacebookAuthorizationActivity.FacebookAlreadyLiked facebookAlreadyLiked) {
                                handler.sendEmptyMessage(HIDE_LIKE_BUTTON);
                            }
                        }
                    }).start();
                } else {
                    action = ACTIONS.FACEBOOK_LIKE;
                    Authorization.authorize(this, FACEBOOK_AUTH, Authorization.AUTHORIZATION_TYPE_FACEBOOK);
                }
            } else {
                Toast.makeText(this, this.getResources().getString(R.string.romanblack_video_alert_like_need_internet),
                        Toast.LENGTH_LONG).show();
            }
        } else if (arg0 == shareButton) {
            needMenu = true;

            showDialogSharing(new DialogSharing.Configuration.Builder()
                            .setFacebookSharingClickListener(new DialogSharing.Item.OnClickListener() {
                                @Override
                                public void onClick() {
                                    if (Authorization.getAuthorizedUser(Authorization.AUTHORIZATION_TYPE_FACEBOOK) != null) {
                                        shareFacebook();
                                    } else {
                                        action = ACTIONS.FACEBOOK_SHARE;
                                        Authorization.authorize(VideoPlayer.this, FACEBOOK_AUTH, Authorization.AUTHORIZATION_TYPE_FACEBOOK);
                                    }

                                    needMenu = false;
                                }
                            })
                            .setTwitterSharingClickListener(new DialogSharing.Item.OnClickListener() {
                                @Override
                                public void onClick() {
                                    if (Authorization.getAuthorizedUser(Authorization.AUTHORIZATION_TYPE_TWITTER) != null) {
                                        shareTwitter();
                                    } else {
                                        Authorization.authorize(VideoPlayer.this, TWITTER_AUTH, Authorization.AUTHORIZATION_TYPE_TWITTER);
                                    }

                                    needMenu = false;
                                }
                            })
                            .setEmailSharingClickListener(new DialogSharing.Item.OnClickListener() {
                                @Override
                                public void onClick() {
                                    String text = getResources().getString(R.string.romanblack_video_sharingsms_first_part) + " " + item.getUrl()
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
                                    String text = getResources().getString(R.string.romanblack_video_sharingsms_first_part) + " " + item.getUrl()
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
//            openOptionsMenu();
        } else if (arg0 == postCommentButton) {

            if (Utils.networkAvailable( VideoPlayer.this)) {
                if (!Authorization.isAuthorized()) {
                    actionIntent = new Intent(this, SendMessageActivity.class);
                    actionIntent.putExtra("user", Authorization.getAuthorizedUser());
                    actionIntent.putExtra("Widget", widget);
                    actionIntent.putExtra("video", item);

                    action = ACTIONS.SEND_MESSAGE;

                    Intent it = new Intent(this, AuthorizationActivity.class);
                    it.putExtra("Widget", widget);
                    startActivityForResult(it, AUTHORIZATION_ACTIVITY);
                } else {
                    Intent it = new Intent(this, SendMessageActivity.class);
                    it.putExtra("user", Authorization.getAuthorizedUser());
                    it.putExtra("Widget", widget);
                    it.putExtra("video", item);
                    startActivityForResult(it, SEND_COMMENT_ACTIVITY);
                }
            } else
                handler.sendEmptyMessage(NEED_INTERNET_CONNECTION);
        }
    }

    /**
     * Implements OnCommentPushedListener callback.
     * @see OnCommentPushedListener
     */
    public void onCommentPushed(CommentItem comment) {
        if (comment != null) {
            if ((comment.getReplyId() == 0)
                    && (comment.getTrackId() == item.getId())) {
                if (comments == null) {
                    comments = new ArrayList<CommentItem>();
                }

                comments.add(comment);

                if (comments.size() == 1) {
                } else {
                    handler.sendEmptyMessage(REFRESH_LIST);
                }
            } else if (comment.getTrackId() == item.getId()) {
                if (comments != null) {
                    if (!comments.isEmpty()) {
                        for (int i = 0; i < comments.size(); i++) {
                            if (comments.get(i).getId() == comment.getId()) {
                                comments.get(i).setCommentsCount(comments.get(i).getCommentsCount() + 1);

                                break;
                            }
                        }
                        handler.sendEmptyMessage(REFRESH_LIST);
                    }
                }
            }
        }
    }

    /**
     * Implements OnCommentPushedListener callback.
     * @see OnCommentPushedListener
     */
    public void onCommentsUpdate(VideoItem item, CommentItem commentItem, int count, int newCommentsCount, ArrayList<CommentItem> comments) {
        if (item == null) {
            return;
        }

        if (commentItem == null) {
            if (item.getId() == this.item.getId()) {
                this.comments = comments;
                Collections.reverse(this.comments);

                adapter.setItems(this.comments);

                handler.sendEmptyMessageDelayed(REFRESH_LIST, 100);
            }
        } else {
            if (item.getId() == this.item.getId()) {
                ArrayList<CommentItem> tmpComments = this.comments;

                for (int i = 0; i < tmpComments.size(); i++) {
                    if (tmpComments.get(i).getId() == commentItem.getId()) {
                        tmpComments.get(i).setCommentsCount(count);
                    }
                }

                handler.sendEmptyMessageDelayed(REFRESH_LIST, 100);
            }
        }
    }

    /**
     * Sets up video thumbnail after it was loaded.
     */
    private void downloadComplete() {
        setThumb();
    }

    private void downloadRegistration(String value) {
        item.setCoverPath(value);
    }

    private enum ACTIONS {

        FACEBOOK_LIKE, FACEBOOK_SHARE, ACTION_NONE,
        SEND_MESSAGE
    };

    /**
     * This class creates a background thread to download video thumbnail.
     */
    private class ImageDownloadTask extends AsyncTask<VideoItem, String, Void> {

        @Override
        protected Void doInBackground(VideoItem... items) {
            try {//ErrorLogging

                if (isCancelled()) {
                    downloadComplete();
                    return null;
                }

                items[0].setCoverPath(cachePath + "/images/" + Utils.md5(items[0].getCoverUrl()));

                if (items[0].getCoverPath().length() > 0) {
                    File file = new File(items[0].getCoverPath());
                    if (file.exists()) {
                        downloadComplete();
                        return null;
                    }
                }

                if (items[0].getCoverUrl().length() == 0) {
                    downloadComplete();
                    return null;
                }

                SystemClock.sleep(10);
                try {
                    URL imageUrl = new URL(URLDecoder.decode(items[0].getCoverUrl()));
                    BufferedInputStream bis = new BufferedInputStream(imageUrl.openConnection().getInputStream());
                    ByteArrayBuffer baf = new ByteArrayBuffer(32);
                    int current = 0;
                    while ((current = bis.read()) != -1) {
                        baf.append((byte) current);
                    }
                    String fileImagesDir = cachePath + "/images/";
                    File fileImagesDirect = new File(fileImagesDir);
                    if (!fileImagesDirect.exists()) {
                        fileImagesDirect.mkdirs();
                    }
                    String filename = cachePath + "/images/" + Utils.md5(items[0].getCoverUrl());
                    FileOutputStream fos = new FileOutputStream(new File(filename));
                    fos.write(baf.toByteArray());
                    fos.close();

                    downloadRegistration(filename);
                } catch (Exception e) {
                    Log.e("", "");
                }
                publishProgress();

                return null;

            } catch (Exception e) {//ErrorLogging
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(String... param) {
        }

        @Override
        protected void onPostExecute(Void unused) {
            downloadComplete();
        }
    }
}
