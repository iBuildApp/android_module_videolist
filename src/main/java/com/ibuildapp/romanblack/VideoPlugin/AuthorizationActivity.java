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
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.appbuilder.sdk.android.AppBuilderModuleMain;
import com.appbuilder.sdk.android.authorization.Authorization;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This activity provides a selection of ways to authorize using facebook, twitter or email.
 * Also user can register using his own email.
 */
public class AuthorizationActivity extends AppBuilderModuleMain implements
        OnClickListener, View.OnFocusChangeListener, TextWatcher {

    private final int FACEBOOK_AUTHORIZATION_REQUEST_CODE = 10000;
    private final int TWITTER_AUTHORIZATION_REQUEST_CODE = 10001;
    private final int EMAIL_AUTHORIZATION_REQUEST_CODE = 10002;
    private final int EMAIL_SIGNUP_REQUEST_CODE = 10003;
    private final int CLOSE_ACTIVITY_OK = 0;
    private final int CLOSE_ACTIVITY_BAD = 1;
    private final int SHOW_PROGRESS_DIALOG = 2;
    private final int HIDE_PROGRESS_DIALOG = 3;
    private final int SIGNUP_FAILED_MESSAGE = 4;
    private boolean signUpActive = false;
    private boolean needCheckFields = false;
    private String DEFAULT_EMAIL_TEXT = "";
    private String DEFAULT_PASSWORD_TEXT = "";
    private View btnFacebookAuth = null;
    private View btnEmailAuth = null;
    private View btnTwitterAuth = null;
    private TextView btnSignUp = null;
    private TextView btnEmailAuthTextView = null;
    private EditText emailEditText = null;
    private EditText passwordEditText = null;
    private ProgressDialog progressDialog = null;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case CLOSE_ACTIVITY_OK: {
                    closeActivityOk();
                }
                break;
                case CLOSE_ACTIVITY_BAD: {
                    closeActivityBad();
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
                case SIGNUP_FAILED_MESSAGE: {
                    Toast.makeText(AuthorizationActivity.this, getResources().getString(R.string.romanblack_video_signup_failed), Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    };

    @Override
    public void create() {
        setContentView(R.layout.romanblack_video_authorization_main);

        DEFAULT_EMAIL_TEXT = getString(R.string.romanblack_video_email);
        DEFAULT_PASSWORD_TEXT = getString(R.string.romanblack_video_password);

        btnFacebookAuth = findViewById(R.id.romanblack_video_login_facebookbtn);
        btnFacebookAuth.setOnClickListener(this);

        btnTwitterAuth = findViewById(R.id.romanblack_video_login_twitterbtn);
        btnTwitterAuth.setOnClickListener(this);

        btnEmailAuth = findViewById(R.id.romanblack_video_login_emailbtn);
        btnEmailAuth.setOnClickListener(this);

        btnEmailAuthTextView = (TextView) findViewById(R.id.romanblack_video_login_emailbtn_text);
        btnEmailAuthTextView.setText(Html.fromHtml("<u>" + getString(R.string.romanblack_video_have_an_account) + "</u>"));

        btnSignUp = (TextView) findViewById(R.id.romanblack_video_login_signuplabel);
        btnSignUp.setOnClickListener(this);

        emailEditText = (EditText) findViewById(R.id.romanblack_video_login_email);
        emailEditText.addTextChangedListener(this);

        passwordEditText = (EditText) findViewById(R.id.romanblack_video_login_password);
        passwordEditText.addTextChangedListener(this);

        // set topbar title
        setTopBarTitle(getString(R.string.romanblack_video_login));
        swipeBlock();
        setTopBarLeftButtonText(getResources().getString(R.string.common_back_upper), true, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        if (!com.appbuilder.sdk.android.Statics.BASE_DOMEN.contains("ibuildapp.com")) {
        }

        needCheckFields = true;
    }

    @Override
    public void resume() {
        super.resume();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Statics.onAuth();
        }

        if (requestCode == FACEBOOK_AUTHORIZATION_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                setResult(RESULT_OK, data);
                finish();

            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, getString(R.string.romanblack_video_alert_facebook_authorization_failed),
                        Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == TWITTER_AUTHORIZATION_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                setResult(RESULT_OK, data);
                finish();

            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, getString(R.string.romanblack_video_alert_twitter_authorization_failed),
                        Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == EMAIL_AUTHORIZATION_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                setResult(RESULT_OK, data);
                finish();
            }
        } else if (requestCode == EMAIL_SIGNUP_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                setResult(RESULT_OK, data);
                finish();

            } else if (resultCode == RESULT_CANCELED) {
            }
        }
    }

    /**
     * Validates email and password fields.
     */
    private void checkFields() {
        if (!needCheckFields) {
            return;
        }

        if ((emailEditText.getText().toString().length() > 0)
                && (passwordEditText.getText().toString().length() > 0)) {
            String regExpn =
                    "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                    + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                    + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                    + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                    + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                    + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$";

            Pattern pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(emailEditText.getText().toString());

            if (matcher.matches()) {
            } else {
                signUpActive = false;
                return;
            }

            if (passwordEditText.getText().toString().length() >= 4) {
            } else {
                signUpActive = false;
                return;
            }

            signUpActive = true;

        } else {
            signUpActive = false;
        }
    }

    /**
     * Colses this activity with "OK" result.
     */
    private void closeActivityOk() {
        Intent resIntent = new Intent();
        setResult(RESULT_OK, resIntent);
        finish();
    }

    /**
     * Colses this activity with "Cancel" result.
     */
    private void closeActivityBad() {
        finish();
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = ProgressDialog.show(this, null, getString(R.string.romanblack_video_loading));
        } else {
            if (!progressDialog.isShowing()) {
                progressDialog = ProgressDialog.show(this, null, getString(R.string.romanblack_video_loading));
            }
        }
    }

    private void hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    /*Listeners interface methods*/
    public void onClick(View arg0) {
        if (arg0.getId() == R.id.romanblack_video_login_facebookbtn) {
            Authorization.authorize(this, FACEBOOK_AUTHORIZATION_REQUEST_CODE, Authorization.AUTHORIZATION_TYPE_FACEBOOK);
        } else if (arg0.getId() == R.id.romanblack_video_login_twitterbtn) {
            Authorization.authorize(this, TWITTER_AUTHORIZATION_REQUEST_CODE, Authorization.AUTHORIZATION_TYPE_TWITTER);
        } else if (arg0.getId() == R.id.romanblack_video_login_emailbtn) {
            Intent emailIntent = new Intent(this, EMailAuthorizationActivity.class);
            startActivityForResult(emailIntent, EMAIL_AUTHORIZATION_REQUEST_CODE);
        } else if (arg0.getId() == R.id.romanblack_video_login_signuplabel) {
            if (signUpActive) {
                handler.sendEmptyMessage(SHOW_PROGRESS_DIALOG);

                new Thread(new Runnable() {
                    public void run() {
                        String firstNameString = "";
                        String lastNameString = "";
                        String emailString = emailEditText.getText().toString();
                        String passwordString = passwordEditText.getText().toString();
                        String rePasswordString = passwordEditText.getText().toString();

                        if (Authorization.registerEmail(firstNameString, lastNameString,
                                emailString, passwordString, rePasswordString)) {
                            handler.sendEmptyMessage(HIDE_PROGRESS_DIALOG);
                            handler.sendEmptyMessage(CLOSE_ACTIVITY_OK);
                        } else {
                            handler.sendEmptyMessage(HIDE_PROGRESS_DIALOG);
                            handler.sendEmptyMessage(SIGNUP_FAILED_MESSAGE);
                        }
                    }
                }).start();
            }
        } else if (arg0.getId() == R.id.romanblack_fanwall_main_home) {// Home image
            finish();
        }
    }

    public void onFocusChange(View arg0, boolean arg1) {
        if (arg0.getId() == R.id.romanblack_video_emailsignup_email) {
            if (arg1) {
                if (((TextView) arg0).getText().toString().equals(DEFAULT_EMAIL_TEXT)) {
                    ((TextView) arg0).setText("");
                }
            } else {
                if (((TextView) arg0).getText().toString().equals("")) {
                    ((TextView) arg0).setText(DEFAULT_EMAIL_TEXT);
                }
            }
        } else if (arg0.getId() == R.id.romanblack_video_emailsignup_pwd) {
            if (arg1) {
                if (((TextView) arg0).getText().toString().equals(DEFAULT_PASSWORD_TEXT)) {
                    ((TextView) arg0).setText("");
                }
            } else {
                if (((TextView) arg0).getText().toString().equals("")) {
                    ((TextView) arg0).setText(DEFAULT_PASSWORD_TEXT);
                }
            }
        }
    }

    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
    }

    public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
    }

    public void afterTextChanged(Editable arg0) {
        checkFields();
    }
}
