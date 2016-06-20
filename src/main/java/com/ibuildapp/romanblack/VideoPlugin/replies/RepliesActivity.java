package com.ibuildapp.romanblack.VideoPlugin.replies;


import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;

import com.appbuilder.sdk.android.AppBuilderModuleMainAppCompat;
import com.appbuilder.sdk.android.Widget;
import com.appbuilder.sdk.android.authorization.Authorization;
import com.ibuildapp.romanblack.VideoPlugin.AuthorizationActivity;
import com.ibuildapp.romanblack.VideoPlugin.R;
import com.ibuildapp.romanblack.VideoPlugin.adapters.RepliesAdapter;
import com.ibuildapp.romanblack.VideoPlugin.api.ibaapi.IbaApi;
import com.ibuildapp.romanblack.VideoPlugin.api.ibaapi.model.CommentData;
import com.ibuildapp.romanblack.VideoPlugin.api.ibaapi.model.CommentDataComparator;
import com.ibuildapp.romanblack.VideoPlugin.api.ibaapi.model.CommentsData;
import com.ibuildapp.romanblack.VideoPlugin.model.VideoItem;
import com.ibuildapp.romanblack.VideoPlugin.utils.Statics;
import com.ibuildapp.romanblack.VideoPlugin.utils.VideoPluginConstants;
import com.ibuildapp.romanblack.VideoPlugin.utils.rx.KeyboardUtils;
import com.ibuildapp.romanblack.VideoPlugin.utils.rx.RxUtils;
import com.ibuildapp.romanblack.VideoPlugin.utils.rx.SimpleSubscriber;
import com.restfb.util.StringUtils;

import java.util.Collections;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;


public class RepliesActivity extends AppBuilderModuleMainAppCompat {

    private VideoItem videoItem;
    private CommentData commentItem;
    private Widget widget;

    private IbaApi api;

    private RecyclerView list;
    private RepliesAdapter adapter;

    private EditText editText;

    @Override
    public void create() {
        setContentView(R.layout.video_plugin_details_info);

        Intent currentIntent = getIntent();
        videoItem = (VideoItem) currentIntent.getSerializableExtra(VideoPluginConstants.ITEM);

        commentItem = (CommentData) currentIntent.getSerializableExtra(VideoPluginConstants.ITEM_2);

        api = new IbaApi();
        setTopBarTitle(getString(R.string.romanblack_video_main_capture));

        setTopBarTitleColor(Color.parseColor("#000000"));

        setTopBarLeftButtonTextAndColor(getResources().getString(R.string.common_back_upper), Color.parseColor("#000000"), true, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        widget = (Widget) currentIntent.getSerializableExtra(VideoPluginConstants.WIDGET);

        setTopBarTitle(getString(R.string.video_plugin_comments));
        setTopBarBackgroundColor(Statics.color1);

        list = (RecyclerView) findViewById(R.id.video_plugin_details_info_list);
        list.setBackgroundColor(Statics.color1);
        list.setLayoutManager(new LinearLayoutManager(this));

        editText = (EditText) findViewById(R.id.video_plugin_details_info_edit_text);
        View postButton = findViewById(R.id.video_plugin_details_info_send_button);
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postMessage();
            }
        });

        api.getReplies(String.valueOf(videoItem.getId()), commentItem.getId())
                .compose(RxUtils.<CommentsData>applyCustomSchedulers(Schedulers.io(), AndroidSchedulers.mainThread()))
                .map(new Func1<CommentsData, CommentsData>() {
                    @Override
                    public CommentsData call(CommentsData commentsData) {
                        Collections.sort(commentsData.getData(), new CommentDataComparator());
                        return commentsData;
                    }
                })
                .subscribe(new SimpleSubscriber<CommentsData>(){
                    @Override
                    public void onNext(CommentsData commentsData) {
                        onDataLoaded(commentsData);
                    }
                });

    }

    private void onDataLoaded(CommentsData commentsData){
        adapter = new RepliesAdapter(this, commentsData, commentItem);
        list.setAdapter(adapter);
    }

    public void postMessage() {
        if (!Authorization.isAuthorized()){
            authorize();
            return;
        }

        String textForPost = editText.getText().toString();
        if (StringUtils.isBlank(textForPost))
            return;

        editText.setText("");
        KeyboardUtils.hideKeyboard(RepliesActivity.this);

        api.postComment(String.valueOf(videoItem.getId()), commentItem.getId(), textForPost)
                .compose(RxUtils.<CommentsData>applyCustomSchedulers(Schedulers.io(), AndroidSchedulers.mainThread()))
                .subscribe(new SimpleSubscriber<CommentsData>(){
                    @Override
                    public void onNext(CommentsData commentsData) {
                        adapter.addNewItem(commentsData);
                    }
                });
    }
    @Override
    public void finish() {
        Intent intent = new Intent();
        intent.putExtra(VideoPluginConstants.ITEM, commentItem);
        setResult(RESULT_OK, intent);

        super.finish();
        overridePendingTransition(R.anim.activity_open_scale, R.anim.activity_close_translate);
    }

    public void authorize(){
        Intent it = new Intent(this, AuthorizationActivity.class);
        it.putExtra(VideoPluginConstants.WIDGET, widget);
        startActivityForResult(it, VideoPluginConstants.AUTHORIZATION_ACTIVITY);
        overridePendingTransition(R.anim.activity_open_translate, R.anim.activity_close_scale);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VideoPluginConstants.AUTHORIZATION_ACTIVITY && resultCode == RESULT_OK) {
            postMessage();
        }
    }
}
