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

import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

import com.ibuildapp.romanblack.VideoPlugin.model.CommentItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class provides static methods for JSON parsing.
 */
public class JSONParser {

    /**
     * Parses JSON comments data.
     * @param data JSON data to parse.
     * @return comments array
     */
    public static ArrayList<CommentItem> parseCommentsString(String data) {
        try {
            String resp = data;

            if (resp == null) {
                return null;
            }

            if (resp.length() == 0) {
                return null;
            }

            JSONObject mainObject = new JSONObject(resp);

            JSONArray messagesJSON = mainObject.getJSONArray("data");

            ArrayList<CommentItem> parsedMessages = new ArrayList<CommentItem>();

            for (int i = 0; i < messagesJSON.length(); i++) {
                JSONObject messageJSON = messagesJSON.getJSONObject(i);

                CommentItem tmpMessage = new CommentItem();
                tmpMessage.setId(Long.valueOf(messageJSON.getString("id")));
                tmpMessage.setAuthor(messageJSON.getString("username"));
                tmpMessage.setDate(new Date(Long.valueOf(messageJSON.getString("create"))));
                tmpMessage.setAvatarUrl(messageJSON.getString("avatar"));
                tmpMessage.setText(messageJSON.getString("text"));

                try {
                    tmpMessage.setTrackId(Long.valueOf(messageJSON.getString("parent_id")));
                } catch (NumberFormatException nFEx) {
                    Log.e("", "");
                }

                try {
                    tmpMessage.setReplyId(Integer.valueOf(messageJSON.getString("reply_id")));
                } catch (NumberFormatException nFEx) {
                    Log.e("", "");
                }

                try {
                    tmpMessage.setCommentsCount(Integer.valueOf(messageJSON.getString("total_comments")));
                } catch (Exception ex) {
                    Log.d("", "");
                }

                parsedMessages.add(tmpMessage);
            }

            return parsedMessages;
        } catch (JSONException jSSONEx) {
            return null;
        }
    }

    /**
     * Downloads and parses JSON comments data.
     * @param url URL resource that contains JSON data
     * @return comments array
     */
    public static ArrayList<CommentItem> parseCommentsUrl(String url) {
        try {
            Log.e("URLTAG", url);
            
            String resp = loadURLData(url);

            if (resp == null) {
                return null;
            }

            if (resp.length() == 0) {
                return null;
            }

            JSONObject mainObject = new JSONObject(resp);

            JSONArray messagesJSON = mainObject.getJSONArray("data");

            ArrayList<CommentItem> parsedMessages = new ArrayList<CommentItem>();

            for (int i = 0; i < messagesJSON.length(); i++) {
                JSONObject messageJSON = messagesJSON.getJSONObject(i);

                CommentItem tmpMessage = new CommentItem();
                tmpMessage.setId(Long.valueOf(messageJSON.getString("id")));
                tmpMessage.setAuthor(messageJSON.getString("username"));
                tmpMessage.setDate(new Date(Long.valueOf(messageJSON.getString("create"))));
                tmpMessage.setAvatarUrl(messageJSON.getString("avatar"));
                tmpMessage.setText(messageJSON.getString("text"));

                try {
                    tmpMessage.setTrackId(Long.valueOf(messageJSON.getString("parent_id")));
                } catch (NumberFormatException nFEx) {
                    Log.e("", "");
                }

                try {
                    tmpMessage.setReplyId(Integer.valueOf(messageJSON.getString("reply_id")));
                } catch (NumberFormatException nFEx) {
                    Log.e("", "");
                }

                tmpMessage.setCommentsCount(Integer.valueOf(messageJSON.getString("total_comments")));

                parsedMessages.add(tmpMessage);
            }

            return parsedMessages;
        } catch (JSONException jSSONEx) {
            return null;
        }
    }


    /**
     * Downloads and parses single comment from given URL.
     * @param url comment URL
     * @return comment
     */
    public static CommentItem parseSingleCommentUrl(String url) {
        try {
            Log.e("URLTAG", url);
            
            String resp = loadURLData(url);

            if (resp == null) {
                return null;
            }

            if (resp.length() == 0) {
                return null;
            }

            JSONObject mainObject = new JSONObject(resp);

            JSONArray array = mainObject.getJSONArray("data");

            JSONObject commentObject = array.getJSONObject(0);

            CommentItem tmpMessage = new CommentItem();
            tmpMessage.setId(Long.valueOf(commentObject.getString("id")));
            tmpMessage.setAuthor(commentObject.getString("username"));
            tmpMessage.setDate(new Date(Long.valueOf(commentObject.getString("create"))));
            tmpMessage.setAvatarUrl(commentObject.getString("avatar"));
            tmpMessage.setText(commentObject.getString("text"));

            try {
                tmpMessage.setTrackId(Long.valueOf(commentObject.getString("parent_id")));
            } catch (NumberFormatException nFEx) {
                Log.e("", "");
            }

            try {
                tmpMessage.setReplyId(Integer.valueOf(commentObject.getString("reply_id")));
            } catch (NumberFormatException nFEx) {
                Log.e("", "");
            }

            try {
                tmpMessage.setCommentsCount(Integer.valueOf(commentObject.getString("total_comments")));
            } catch (Exception ex) {
                Log.d("", "");
            }

            tmpMessage.setAccountId(commentObject.getString("account_id"));
            tmpMessage.setAccountType(commentObject.getString("account_type"));

            return tmpMessage;
        } catch (JSONException jSSONEx) {
            return null;
        }
    }

    /**
     * Download URL data to String.
     * @param msgsUrl URL to download
     * @return data string
     */
    private static String loadURLData(String msgsUrl) {
        try {
            URL url = new URL(msgsUrl);
            URLConnection conn = url.openConnection();
            InputStreamReader streamReader = new InputStreamReader(conn.getInputStream());

            BufferedReader br = new BufferedReader(streamReader);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            br.close();
            String resp = sb.toString();

            return resp;
        } catch (IOException iOEx) {
            return "";
        }
    }
}
