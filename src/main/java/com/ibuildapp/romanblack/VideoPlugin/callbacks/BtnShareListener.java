package com.ibuildapp.romanblack.VideoPlugin.callbacks;

import android.view.View;
import android.widget.Toast;

import com.appbuilder.sdk.android.tools.NetworkUtils;
import com.ibuildapp.romanblack.VideoPlugin.R;

public class BtnShareListener implements View.OnClickListener {

    private int position = 0;
    private SharePressedListener listener;

    public BtnShareListener(int position, SharePressedListener listener) {
        this.position = position;
        this.listener = listener;
    }

    public void onClick(View arg0) {

        if (NetworkUtils.isOnline(arg0.getContext())) {
            if (listener != null) {
                listener.onSharePressed(position);
            }
        } else {
            Toast.makeText(arg0.getContext(), arg0.getContext().getResources().getString(R.string.romanblack_video_alert_share_need_internet), Toast.LENGTH_LONG).show();
        }
    }
}