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
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;
import com.appbuilder.sdk.android.AppBuilderModuleMain;
import com.appbuilder.sdk.android.Utils;
import com.appbuilder.sdk.android.authorization.Authorization;
import com.appbuilder.sdk.android.authorization.FacebookAuthorizationActivity;
import com.ibuildapp.romanblack.VideoPlugin.model.VideoItem;
import com.ibuildapp.romanblack.VideoPlugin.utils.Statics;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

/**
 * This activity provides share on Facebook or Twitter functionality.
 */
public class SharingActivity extends AppBuilderModuleMain implements
        OnClickListener {

    private final int NEED_INTERNET_CONNECTION = 0;
    private final int INITIALIZATION_FAILED = 1;
    private final int SHOW_PROGRESS_DIALOG = 2;
    private final int HIDE_PROGRESS_DIALOG = 3;
    private final int SHARED_ON_FACEBOOK = 4;
    private final int SHARED_ON_TWITTER = 5;
    private String text = "";
    private String sharingType = "";
    private String link = "";
    private VideoItem item = null;
    private Twitter twitter = null;
    private EditText mainEditText = null;
    private ProgressDialog progressDialog = null;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case INITIALIZATION_FAILED: {
                    finish();
                }
                break;
                case NEED_INTERNET_CONNECTION: {
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
                case SHARED_ON_FACEBOOK: {
                    Toast.makeText(SharingActivity.this, R.string.romanblack_video_shared_on_facebook, Toast.LENGTH_LONG).show();
                }
                break;
                case SHARED_ON_TWITTER: {
                    Toast.makeText(SharingActivity.this, R.string.romanblack_video_shared_on_twitter, Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    };
    private View sharingLayout;

    @Override
    public void create() {
        setContentView(R.layout.video_plugin_sharing);

        Intent currentIntent = getIntent();

        link = currentIntent.getStringExtra("link");
        if (TextUtils.isEmpty(link)) {
            handler.sendEmptyMessage(INITIALIZATION_FAILED);
            return;
        }

        sharingType = currentIntent.getStringExtra("type");

        item = (VideoItem) currentIntent.getSerializableExtra("item");
        if (item == null) {
            handler.sendEmptyMessage(INITIALIZATION_FAILED);
            return;
        }

        swipeBlock();
        setTopBarTitleColor(Color.parseColor("#000000"));

        setTopBarLeftButtonTextAndColor(getResources().getString(R.string.common_back_upper), Color.parseColor("#000000"), true, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        setTopBarBackgroundColor(Statics.color1);
        setTopBarRightButtonText(getString(R.string.post), false, new OnClickListener() {
            public void onClick(View arg0) {
                if ( !Utils.networkAvailable( SharingActivity.this ) )
                {
                    handler.sendEmptyMessage(NEED_INTERNET_CONNECTION);
                    return;
                }

                post();
            }
        });

        sharingLayout = findViewById(R.id.video_plugin_sharing_main_layout);
        mainEditText = (EditText) findViewById(R.id.video_plugin_sharing_edit_text);

        sharingLayout.setBackgroundColor(Statics.color1);
        mainEditText.setTextColor(Statics.color3);

        if (sharingType.equalsIgnoreCase("facebook")) {
            setTopBarTitle("Facebook");
        } else if (sharingType.equalsIgnoreCase("twitter")) {
            setTopBarTitle("Twitter");
        } else
            setTopBarTitle(" ");
    }

    private void showProgressDialog() {
        try {
            if (progressDialog.isShowing()) {
                return;
            }
        } catch (NullPointerException nPEx) {
        }

        progressDialog = ProgressDialog.show(this, null, getString(R.string.romanblack_video_loading));
        progressDialog.setCancelable(true);
    }

    private void hideProgressDialog() {
        try {
            progressDialog.dismiss();
        } catch (NullPointerException nPEx) {
        }

        finish();
    }

    public void onClick(View arg0) {
    }

    /**
     * Posts message on Facebook or Twitter depending on sharing type.
     */
    private void post() {
        text = mainEditText.getText().toString();

        if (sharingType.equalsIgnoreCase("facebook")) {
            final FacebookClient fbClient = new DefaultFacebookClient(Authorization.getAuthorizedUser(Authorization.AUTHORIZATION_TYPE_FACEBOOK).getAccessToken());

            handler.sendEmptyMessage(SHOW_PROGRESS_DIALOG);

            text = Authorization.getAuthorizedUser(Authorization.AUTHORIZATION_TYPE_FACEBOOK).getFullName() + " "
                    + getString(R.string.romanblack_video_sharing_first_part) + " "
                    + link + " " + getString(R.string.romanblack_video_sharing_second_part) + " "
                    + Statics.APP_NAME
                    + " app:\n\"" + mainEditText.getText() + "\"\n";

            if(com.appbuilder.sdk.android.Statics.showLink){
                text = text + getString(R.string.romanblack_video_sharing_third_part)
                        + " app: http://" + com.appbuilder.sdk.android.Statics.BASE_DOMEN
                        + "/projects.php?action=info&projectid="
                        + Statics.APP_ID;
            }

            new Thread(new Runnable() {
                public void run() {
                    try {
                        boolean res = FacebookAuthorizationActivity.sharing(Authorization.getAuthorizedUser(Authorization.AUTHORIZATION_TYPE_FACEBOOK).getAccessToken(), text, null);
                        if ( res )
                        {
                            setResult(RESULT_OK);
                            handler.sendEmptyMessage(SHARED_ON_FACEBOOK);
                        }
                    } catch (FacebookAuthorizationActivity.FacebookNotAuthorizedException e) {
                    }
                    handler.sendEmptyMessage(HIDE_PROGRESS_DIALOG);
                }
            }).start();
        } else if (sharingType.equalsIgnoreCase("twitter")) {
            handler.sendEmptyMessage(SHOW_PROGRESS_DIALOG);

            new Thread(new Runnable() {
                public void run() {
                    try {
                        twitter = new TwitterFactory().getInstance();
                        twitter.setOAuthConsumer(Authorization.getAuthorizedUser(Authorization.AUTHORIZATION_TYPE_TWITTER).getConsumerKey(),
                                Authorization.getAuthorizedUser(Authorization.AUTHORIZATION_TYPE_TWITTER).getConsumerSecret());
                        twitter.setOAuthAccessToken(new AccessToken(Authorization.getAuthorizedUser(Authorization.AUTHORIZATION_TYPE_TWITTER).getAccessToken(),
                                Authorization.getAuthorizedUser(Authorization.AUTHORIZATION_TYPE_TWITTER).getAccessTokenSecret()));

                        text = Authorization.getAuthorizedUser(Authorization.AUTHORIZATION_TYPE_TWITTER).getUserName() + " "
                                + getString(R.string.romanblack_video_sharing_first_part) + " "
                                + link + " " + getString(R.string.romanblack_video_sharing_second_part) + " "
                                + Statics.APP_NAME
                                + " app:\n\"" + mainEditText.getText() + "\"\n";

                        if(com.appbuilder.sdk.android.Statics.showLink){
                            text = text + getString(R.string.romanblack_video_sharing_third_part)
                                    + " app: http://" + com.appbuilder.sdk.android.Statics.BASE_DOMEN
                                    + "/projects.php?action=info&projectid="
                                    + Statics.APP_ID;
                        }

                        if (text.length() > 140) {
                            text = text.substring(0, 139);
                        }

                        twitter.updateStatus(text);

                        setResult(RESULT_OK);

                        handler.sendEmptyMessage(SHARED_ON_TWITTER);
                    } catch (TwitterException tEx) {
                        Log.d("", "");
                    }

                    handler.sendEmptyMessage(HIDE_PROGRESS_DIALOG);
                }
            }).start();
        }
    }
}
