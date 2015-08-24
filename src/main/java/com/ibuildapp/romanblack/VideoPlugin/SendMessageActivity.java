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
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.widget.TextView.OnEditorActionListener;
import com.appbuilder.sdk.android.AppBuilderModuleMain;
import com.appbuilder.sdk.android.authorization.Authorization;
import com.appbuilder.sdk.android.authorization.entities.User;
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

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;

/**
 * This activity provides send comment functionality.
 */
public class SendMessageActivity extends AppBuilderModuleMain implements
        OnClickListener, OnEditorActionListener, TextWatcher {

    private final int TAKE_A_PICTURE_ACTIVITY = 10000;
    private final int PICK_IMAGE_ACTIVITY = 10001;
    private final int CLOSE_ACTIVITY_OK = 0;
    private final int CLOSE_ACTIVITY_BAD = 1;
    private ProgressDialog progressDialog;
    private boolean uploading = false;
    private String imagePath = "";
    private CommentItem message = null;
    private CommentItem recievedMessage = null;
    private VideoItem video = null;
    private LinearLayout mainLayout = null;
    private LinearLayout imageLayout = null;
    private EditText messageEditText = null;
    private TextView cancelButton = null;
    private TextView clearButton = null;
    private TextView postButton = null;
//    private TextView symbolCounter = null;
    private static final String EN_LENGTH = "/150";
    private static final String NOT_EN_LENGTH = "/75";
    private CharsetEncoder charsetEncoder = Charset.forName("US-ASCII").newEncoder();
    private String length = EN_LENGTH;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case CLOSE_ACTIVITY_OK: {
                    closeActivityOK();
                }
                break;
                case CLOSE_ACTIVITY_BAD: {
                    closeActivityBad();
                }
                break;
            }
        }
    };

    private ProgressBar symbol_bytes_progress;
    private String messageBuffer;
    private static final int MESSAGE_MAX_BYTES = 150;

    @Override
    public void create() {
        setContentView(R.layout.romanblack_video_send_message);

        Intent currentIntent = getIntent();

        setTopBarTitle(" ");
        swipeBlock();
        setTopBarLeftButtonText(getResources().getString(R.string.common_back_upper), true, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        message = (CommentItem) currentIntent.getSerializableExtra("message");
        video = (VideoItem) currentIntent.getSerializableExtra("video");
        mainLayout = (LinearLayout) findViewById(R.id.romanblack_video_send_message_main);
        mainLayout.setBackgroundColor(Statics.color1);

        messageEditText = (EditText) findViewById(R.id.romanblack_video_sendmessage_edittext);
        //messageEditText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        messageEditText.addTextChangedListener(this);

        cancelButton = (TextView) findViewById(R.id.romanblack_fanwall_sendmessage_cancelbtn);

        symbol_bytes_progress = (ProgressBar)findViewById(R.id.symbol_bytes_progress);

        try {
            cancelButton.setTextColor(bottomBarDesign.leftButtonDesign.textColor);
            cancelButton.setTextSize(bottomBarDesign.leftButtonDesign.fontSize);
        } catch (NullPointerException nPEx) {
        }
        cancelButton.setOnClickListener(this);

        clearButton = (TextView) findViewById(R.id.romanblack_fanwall_sendmessage_clear_btn);
        try {
            clearButton.setTextColor(bottomBarDesign.leftButtonDesign.textColor);
            clearButton.setTextSize(bottomBarDesign.leftButtonDesign.fontSize);
        } catch (NullPointerException nPEx) {
        }
        clearButton.setOnClickListener(this);

        postButton = (TextView) findViewById(R.id.romanblack_fanwall_sendmessage_post_btn);
        try {
            postButton.setTextColor(bottomBarDesign.leftButtonDesign.textColor);
            postButton.setTextSize(bottomBarDesign.leftButtonDesign.fontSize);
        } catch (NullPointerException nPEx) {
        }
        postButton.setOnClickListener(this);

//        symbolCounter = (TextView) findViewById(R.id.romanblack_fanwall_sendmessage_symbols_counter);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d("", "");
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == TAKE_A_PICTURE_ACTIVITY) {
            if (resultCode == RESULT_OK) {
                imagePath = data.getStringExtra("imagePath");

                if (imagePath == null) {
                    return;
                }

                if (imagePath.length() == 0) {
                    return;
                }
            }
        } else if (requestCode == PICK_IMAGE_ACTIVITY) {
            if (resultCode == RESULT_OK) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = getContentResolver().query(
                        selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String filePath = cursor.getString(columnIndex);
                cursor.close();

                imagePath = filePath;

                if (imagePath == null) {
                    return;
                }

                if (imagePath.length() == 0) {
                    return;
                }

                if (imagePath.startsWith("http")) {
                    Toast.makeText(this, R.string.romanblack_video_alert_image_cannot_be_selected, Toast.LENGTH_LONG).show();
                    return;
                }

            }
        }
    }

    /**
     * Closes this activity with "OK" result.
     */
    private void closeActivityOK() {
        Intent it = new Intent();
        it.putExtra("message", recievedMessage);
        setResult(RESULT_OK, it);

        finish();
    }

    /**
     * Closes this activity with "Canceled" result.
     */
    private void closeActivityBad() {
        setResult(RESULT_CANCELED);

        finish();
    }

    public void onClick(View arg0) {
        if (!uploading) {
            if (arg0.getId() == R.id.romanblack_fanwall_sendmessage_cancelbtn) {
                finish();
            } else if (arg0.getId() == R.id.romanblack_fanwall_sendmessage_clear_btn) {
                messageEditText.setText("");
            } else if (arg0.getId() == R.id.romanblack_fanwall_sendmessage_post_btn) {
                if (messageEditText.getText().length() < 1) {
                    Toast.makeText(this, R.string.romanblack_video_alert_empty_message,
                            Toast.LENGTH_LONG).show();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showProgressDialog();
                    }
                });

                uploading = true;
                if (messageEditText.getText().length() > 150) {
                    Toast.makeText(this, R.string.romanblack_video_alert_big_text,
                            Toast.LENGTH_LONG).show();
                    uploading = false;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideProgressDialog();
                        }
                    });

                    return;
                }

                try {
                    if ((messageEditText.getText().length() == 0)
                            && (imageLayout.getVisibility() == View.GONE)) {
                        Toast.makeText(this, R.string.romanblack_video_alert_empty_message,
                                Toast.LENGTH_LONG).show();
                        uploading = false;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                hideProgressDialog();
                            }
                        });
                        return;
                    }
                } catch (NullPointerException nPEx) {
                    Toast.makeText(this, R.string.romanblack_video_alert_empty_message,
                            Toast.LENGTH_LONG).show();
                    uploading = false;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideProgressDialog();
                        }
                    });
                    return;
                }

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

                            MultipartEntity multipartEntity = new MultipartEntity();
                            multipartEntity.addPart("action", new StringBody("postcomment", Charset.forName("UTF-8")));
                            multipartEntity.addPart("app_id", new StringBody(com.appbuilder.sdk.android.Statics.appId, Charset.forName("UTF-8")));
                            multipartEntity.addPart("token", new StringBody(com.appbuilder.sdk.android.Statics.appToken, Charset.forName("UTF-8")));
                            multipartEntity.addPart("module_id", new StringBody(Statics.MODULE_ID, Charset.forName("UTF-8")));
                            multipartEntity.addPart("parent_id", new StringBody(video.getId() + "", Charset.forName("UTF-8")));
                            if (message != null) {
                                multipartEntity.addPart("reply_id", new StringBody(message.getId() + "", Charset.forName("UTF-8")));
                            }

                            if (Authorization.getAuthorizedUser().getAccountType() == User.ACCOUNT_TYPES.FACEBOOK) {
                                multipartEntity.addPart("account_type", new StringBody("facebook", Charset.forName("UTF-8")));
                            } else if (Authorization.getAuthorizedUser().getAccountType() == User.ACCOUNT_TYPES.TWITTER) {
                                multipartEntity.addPart("account_type", new StringBody("twitter", Charset.forName("UTF-8")));
                            } else {
                                multipartEntity.addPart("account_type", new StringBody("ibuildapp", Charset.forName("UTF-8")));
                            }
                            multipartEntity.addPart("account_id", new StringBody(Authorization.getAuthorizedUser().getAccountId(), Charset.forName("UTF-8")));
                            multipartEntity.addPart("username", new StringBody(Authorization.getAuthorizedUser().getUserName(), Charset.forName("UTF-8")));
                            multipartEntity.addPart("avatar", new StringBody(Authorization.getAuthorizedUser().getAvatarUrl(), Charset.forName("UTF-8")));

                            multipartEntity.addPart("text", new StringBody(messageEditText.getText().toString(), Charset.forName("UTF-8")));


                            httpPost.setEntity(multipartEntity);

                            Statics.onPost();

                            String resp = httpClient.execute(httpPost, new BasicResponseHandler());

                            recievedMessage = JSONParser.parseCommentsString(resp).get(0);

                            String commentsUrl = null;

                            if (message == null) {
                                commentsUrl = Statics.BASE_URL + "/getcomments/"
                                        + com.appbuilder.sdk.android.Statics.appId + "/" + Statics.MODULE_ID + "/"
                                        + video.getId() + "/0/"
                                        + com.appbuilder.sdk.android.Statics.appId + "/"
                                        + com.appbuilder.sdk.android.Statics.appToken;
                            } else {
                                commentsUrl = Statics.BASE_URL + "/getcomments/"
                                        + com.appbuilder.sdk.android.Statics.appId + "/" + Statics.MODULE_ID + "/"
                                        + video.getId() + "/" + message.getId() + "/"
                                        + com.appbuilder.sdk.android.Statics.appId + "/"
                                        + com.appbuilder.sdk.android.Statics.appToken;
                            }

                            ArrayList<CommentItem> comments = JSONParser.parseCommentsUrl(commentsUrl);

                            if (comments != null && !comments.isEmpty()) {
                                Statics.onCommentsUpdate(video, message, comments.size(), 0, comments);
                            }

                            Log.d("", "");

                            handler.sendEmptyMessage(CLOSE_ACTIVITY_OK);

                        } catch (Exception e) {
                            Log.d("", "");

                            handler.sendEmptyMessage(CLOSE_ACTIVITY_BAD);
                        }

                    }
                }).start();
            }
        }
    }

    public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
        return false;
    }

    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}

    public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

        int messageBytes = arg0.toString().getBytes().length;

        if(messageBytes > MESSAGE_MAX_BYTES) {
            messageEditText.removeTextChangedListener(this);
            messageEditText.setText(messageBuffer);
            messageEditText.setSelection(messageBuffer.length());
            symbol_bytes_progress.setProgress(messageBuffer.getBytes().length);
            messageEditText.addTextChangedListener(this);
            Toast.makeText(this, R.string.romanblack_video_alert_message_is_too_long, Toast.LENGTH_SHORT).show();
        } else {
            symbol_bytes_progress.setProgress(messageBytes);
            messageBuffer = arg0.toString();
        }

//        if(charsetEncoder.canEncode(arg0)) {
//            messageEditText.setFilters(new InputFilter[]{
//                    new InputFilter.LengthFilter(150)
//            });
//            length = EN_LENGTH;
//        } else {
//            messageEditText.setFilters(new InputFilter[]{
//                    new InputFilter.LengthFilter(75)
//            });
//            length = NOT_EN_LENGTH;
//        }

    }

    /**
     * Updates symbol counter when message text was changed.
     */
    public void afterTextChanged(Editable arg0) {
//        if (symbolCounter == null) {
//            symbolCounter = (TextView) findViewById(R.id.romanblack_fanwall_sendmessage_symbols_counter);
//        }

//        symbolCounter.setText(arg0.length() + length);
    }

    private void showProgressDialog() {
        try {
            if (progressDialog.isShowing()) {
                return;
            }
        } catch (NullPointerException nPEx) {
        }

        progressDialog = ProgressDialog.show(this, null, getString(R.string.loading));
        progressDialog.setCancelable(true);
    }

    private void hideProgressDialog() {
        try {
            progressDialog.dismiss();
        } catch (NullPointerException nPEx) {
        }
    }
}
