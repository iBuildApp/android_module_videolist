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

import android.graphics.Color;
import android.location.Location;
import com.ibuildapp.romanblack.VideoPlugin.callbacks.OnAuthListener;
import com.ibuildapp.romanblack.VideoPlugin.callbacks.OnCommentPushedListener;
import com.ibuildapp.romanblack.VideoPlugin.callbacks.OnPostListener;

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
    static final String BASE_URL = "http://" + com.appbuilder.sdk.android.Statics.BASE_DOMEN + "/mdscr/video";
    public static int PLAY_NEXT_VIDEO = 1002;
    static float near = 180;
    static int moduleId = 0;
    static String canEdit = "all";
    static String sharingOn = "off";
    static String commentsOn = "off";
    /*Color Scheme*/
    static int color1 = Color.parseColor("#4d4948");// background
    static int color2 = Color.parseColor("#fff58d");// category header
    static int color3 = Color.parseColor("#fff7a2");// text header 
    static int color4 = Color.parseColor("#ffffff");// text
    static int color5 = Color.parseColor("#bbbbbb");// date
    /*Color Scheme ends*/
    static String APP_ID = "0";
    static String MODULE_ID = "0";
    static String APP_NAME = "";
    static String FACEBOOK_APP_TOKEN = "";
    static Location currentLocation = null;
    static boolean isOnline = false;
    
    /* Presed callbacks */
    static ArrayList<OnPostListener> onPostListeners =
            new ArrayList<OnPostListener>();
    static ArrayList<OnAuthListener> onAuthListeners =
            new ArrayList<OnAuthListener>();
    static ArrayList<OnCommentPushedListener> onCommentPushedListeners =
            new ArrayList<OnCommentPushedListener>();
    /* Presed callbacks ends */

    /**
     * This happen when user posted new comment.
     * This method call all preset callbacks.
     */
    static void onPost() {
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
    static void onAuth() {
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
    static void onCommentPushed(String appId, String moduleId, CommentItem comment) {
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
    static void onCommentsUpdate(VideoItem item, CommentItem commentItem, int count, int newCommentsCount, ArrayList<CommentItem> comments) {
        if (onCommentPushedListeners != null) {
            if (!onCommentPushedListeners.isEmpty()) {
                for (int i = 0; i < onCommentPushedListeners.size(); i++) {
                    onCommentPushedListeners.get(i).
                            onCommentsUpdate(item, commentItem, count, newCommentsCount, comments);
                }
            }
        }
    }

    enum STATES {

        NO_MESSAGES, HAS_MESSAGES, AUTHORIZATION_NO,
        AUTHORIZATION_YES,
        AUTHORIZATION_FACEBOOK, AUTHORIZATION_TWITTER, AUTHORIZATION_EMAIL
    };
}
