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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.appbuilder.sdk.android.authorization.Authorization;
import com.appbuilder.sdk.android.authorization.entities.User;

/**
 * This class recieves information about comment updates.
 */
public class PushBroadcast extends BroadcastReceiver {

    @Override
    public void onReceive(Context arg0, Intent arg1) {
        Log.d("", "");

        String appId = arg1.getStringExtra("app_id");
        String moduleId = arg1.getStringExtra("module_id");
        String commentId = arg1.getStringExtra("comment_id");

        CommentItem tmpItem = null;

        try {
            tmpItem = JSONParser.parseSingleCommentUrl(Statics.BASE_URL + "/getcommentbyid/" + commentId
                    + "/" + com.appbuilder.sdk.android.Statics.appId + "/"
                    + com.appbuilder.sdk.android.Statics.appToken);
        } catch (Exception ex) {
            Log.d("", "");
        }

        User tmpUser = Authorization.getAuthorizedUser();

        try {
            if (tmpItem != null && tmpUser != null) {
                if (tmpItem.getAccountType() == tmpUser.getAccountType()) {
                    if (tmpItem.getAccountId() == tmpUser.getAccountId()) {
                        return;
                    }
                }
            }
        } catch (Exception ex) {
        }

        Statics.onCommentPushed(appId, moduleId, tmpItem);
    }
}
