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
package com.ibuildapp.romanblack.VideoPlugin.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.location.Location;

import com.ibuildapp.romanblack.VideoPlugin.model.VideoItem;
import com.ibuildapp.romanblack.VideoPlugin.callbacks.OnAuthListener;
import com.ibuildapp.romanblack.VideoPlugin.callbacks.OnCommentPushedListener;
import com.ibuildapp.romanblack.VideoPlugin.callbacks.OnPostListener;
import com.ibuildapp.romanblack.VideoPlugin.model.CommentItem;

import java.util.ArrayList;

/**
 * This class contains global module variables.
 */
public class Statics {

    public static final int PLAY_PREV_VIDEO = 1001;
    /**
     * Base module URL.
     * This URL depending on service domen.
     */
    public static final String BASE_URL = "http://" + com.appbuilder.sdk.android.Statics.BASE_DOMEN + "/mdscr/video";
    public static int PLAY_NEXT_VIDEO = 1002;
    public static float near = 180;
    public static int moduleId = 0;
    public static String canEdit = "all";
    public static String sharingOn = "off";
    public static String commentsOn = "off";
    public static String likesOn = "on";
    /*Color Scheme*/
    public static int color1 = Color.parseColor("#4d4948");// background
    public static int color2 = Color.parseColor("#fff58d");// category header
    public static int color3 = Color.parseColor("#fff7a2");// text header
    public static int color4 = Color.parseColor("#ffffff");// text
    public static int color5 = Color.parseColor("#bbbbbb");// date
    /*Color Scheme ends*/
    public static String APP_ID = "0";
    public static String MODULE_ID = "0";
    public static String APP_NAME = "";
    public static String FACEBOOK_APP_TOKEN = "";
    public static Location currentLocation = null;
    public static boolean isOnline = false;
    
    /* Presed callbacks */
    public static ArrayList<OnPostListener> onPostListeners =
            new ArrayList<>();
    public static ArrayList<OnAuthListener> onAuthListeners =
            new ArrayList<>();
    public static ArrayList<OnCommentPushedListener> onCommentPushedListeners =
            new ArrayList<>();
    public static boolean isLight;
    /* Presed callbacks ends */

    public static Bitmap appyColorFilterForResource(Context context, int resourceId, int color, PorterDuff.Mode mode ){
        Bitmap immutable = BitmapFactory.decodeResource(context.getResources(), resourceId);
        final Bitmap mutable = immutable.copy(Bitmap.Config.ARGB_8888, true);
        Canvas c = new Canvas(mutable);
        Paint p = new Paint();
        p.setColorFilter(new PorterDuffColorFilter(color, mode));
        c.drawBitmap(mutable, 0.f, 0.f, p);
        return mutable;
    }
    /**
     * This happen when user posted new comment.
     * This method call all preset callbacks.
     */
    public static void onPost() {
        if (onPostListeners != null) {
            if (!onPostListeners.isEmpty()) {
                for (int i = 0; i < onPostListeners.size(); i++) {
                    onPostListeners.get(i).onPost();
                }
            }
        }
    }

    /**
     * This happen when user authorized.
     * This method call all preset callbacks.
     */
    public static void onAuth() {
        if (onAuthListeners != null) {
            if (!onAuthListeners.isEmpty()) {
                for (int i = 0; i < onAuthListeners.size(); i++) {
                    onAuthListeners.get(i).onAuth();
                }
            }
        }
    }

    /**
     * This new comment information was recieved.
     * This method call all preset callbacks.
     */
    public static void onCommentPushed(String appId, String moduleId, CommentItem comment) {
        if (onCommentPushedListeners != null) {
            if (!onCommentPushedListeners.isEmpty()) {
                for (int i = 0; i < onCommentPushedListeners.size(); i++) {
                    if (APP_ID.equals(appId) && MODULE_ID.equals(moduleId)) {
                        onCommentPushedListeners.get(i).
                                onCommentPushed(comment);
                    }
                }
            }
        }
    }

    /**
     * This happen when comments list was updates.
     * This method call all preset callbacks.
     */
    public static void onCommentsUpdate(VideoItem item, CommentItem commentItem, int count, int newCommentsCount, ArrayList<CommentItem> comments) {
        if (onCommentPushedListeners != null) {
            if (!onCommentPushedListeners.isEmpty()) {
                for (int i = 0; i < onCommentPushedListeners.size(); i++) {
                    onCommentPushedListeners.get(i).
                            onCommentsUpdate(item, commentItem, count, newCommentsCount, comments);
                }
            }
        }
    }

    public enum STATES {

        NO_MESSAGES, HAS_MESSAGES, AUTHORIZATION_NO,
        AUTHORIZATION_YES,
        AUTHORIZATION_FACEBOOK, AUTHORIZATION_TWITTER, AUTHORIZATION_EMAIL
    }
}
