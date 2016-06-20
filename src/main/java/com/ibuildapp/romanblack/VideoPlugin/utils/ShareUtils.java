package com.ibuildapp.romanblack.VideoPlugin.utils;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;

import com.appbuilder.sdk.android.AppBuilderModuleMainAppCompat;
import com.appbuilder.sdk.android.DialogSharing;
import com.appbuilder.sdk.android.authorization.Authorization;
import com.ibuildapp.romanblack.VideoPlugin.R;
import com.ibuildapp.romanblack.VideoPlugin.SharingActivity;
import com.ibuildapp.romanblack.VideoPlugin.model.VideoItem;

public abstract class ShareUtils {

    public static void onSharePressed(final AppBuilderModuleMainAppCompat context, final VideoItem item) {

        context.showDialogSharing(new DialogSharing.Configuration.Builder()
                .setFacebookSharingClickListener(new DialogSharing.Item.OnClickListener() {
                    @Override
                    public void onClick() {
                        if (Authorization.isAuthorized(Authorization.AUTHORIZATION_TYPE_FACEBOOK)) {
                            shareFacebook(context, item);
                        } else {
                            Authorization.authorize(context, VideoPluginConstants.FACEBOOK_AUTH_SHARE, Authorization.AUTHORIZATION_TYPE_FACEBOOK);
                        }
                    }
                })
                .setTwitterSharingClickListener(new DialogSharing.Item.OnClickListener() {
                    @Override
                    public void onClick() {
                        if (Authorization.getAuthorizedUser(Authorization.AUTHORIZATION_TYPE_TWITTER) != null) {
                            shareTwitter(context, item);
                        } else {
                            Authorization.authorize(context, VideoPluginConstants.TWITTER_AUTH, Authorization.AUTHORIZATION_TYPE_TWITTER);
                        }
                    }
                })
                .setEmailSharingClickListener(new DialogSharing.Item.OnClickListener() {
                    @Override
                    public void onClick() {
                        String text = context.getResources().getString(R.string.romanblack_video_sharingsms_first_part) + " "
                                + item.getUrl()
                                + " " + context.getResources().getString(R.string.romanblack_video_sharingsms_second_part) + " "
                                + Statics.APP_NAME + " "
                                + context.getResources().getString(R.string.romanblack_video_sharingsms_third_part)
                                + Statics.APP_NAME + " "
                                + context.getResources().getString(R.string.romanblack_video_sharingsms_fourth_part)
                                + " "
                                + "http://ibuildapp.com/projects.php?action=info&projectid=" + Statics.APP_ID;

                        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                        emailIntent.setType("text/html");
                        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, Html.fromHtml(text));
                        context.startActivity(emailIntent);
                    }
                })
                .setSmsSharingClickListener(new DialogSharing.Item.OnClickListener() {
                    @Override
                    public void onClick() {
                        String text = context.getResources().getString(R.string.romanblack_video_sharingsms_first_part) + " "
                                + item.getUrl()
                                + " " + context.getResources().getString(R.string.romanblack_video_sharingsms_second_part) + " "
                                + Statics.APP_NAME + " "
                                + context.getResources().getString(R.string.romanblack_video_sharingsms_third_part)
                                + Statics.APP_NAME + " "
                                + context.getResources().getString(R.string.romanblack_video_sharingsms_fourth_part)
                                + " "
                                + "http://ibuildapp.com/projects.php?action=info&projectid=" + Statics.APP_ID;

                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:"));
                        intent.putExtra("sms_body", text);
                        context.startActivity(intent);
                    }
                })
                .build()
        );

    }

    public static void shareFacebook(Activity context, VideoItem item) {
        Intent it = new Intent(context, SharingActivity.class);
        it.putExtra("type", "facebook");
        it.putExtra("link", item.getUrl());
        it.putExtra("item", item);
        context.startActivityForResult(it, VideoPluginConstants.SHARING_FACEBOOK);
        context.overridePendingTransition(R.anim.activity_open_translate, R.anim.activity_close_scale);

    }

    public static void shareTwitter(Activity context, VideoItem item) {
        Intent it = new Intent(context, SharingActivity.class);
        it.putExtra("type", "twitter");
        it.putExtra("link", item.getUrl());
        it.putExtra("item", item);
        context.startActivityForResult(it, VideoPluginConstants.SHARING_TWITTER);
        context.overridePendingTransition(R.anim.activity_open_translate, R.anim.activity_close_scale);
    }

}
