package com.ibuildapp.romanblack.VideoPlugin.details.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.appbuilder.sdk.android.authorization.Authorization;
import com.appbuilder.sdk.android.authorization.FacebookAuthorizationActivity;
import com.ibuildapp.romanblack.VideoPlugin.R;
import com.ibuildapp.romanblack.VideoPlugin.adapters.DetailsInfoAdapter;
import com.ibuildapp.romanblack.VideoPlugin.api.ibaapi.IbaApi;
import com.ibuildapp.romanblack.VideoPlugin.api.ibaapi.model.CommentDataComparator;
import com.ibuildapp.romanblack.VideoPlugin.api.ibaapi.model.CommentsData;
import com.ibuildapp.romanblack.VideoPlugin.callbacks.FBLikePressedListener;
import com.ibuildapp.romanblack.VideoPlugin.details.VideoDetailsActivity;
import com.ibuildapp.romanblack.VideoPlugin.model.VideoItem;
import com.ibuildapp.romanblack.VideoPlugin.utils.Statics;
import com.ibuildapp.romanblack.VideoPlugin.utils.VideoPluginConstants;
import com.ibuildapp.romanblack.VideoPlugin.utils.rx.KeyboardUtils;
import com.ibuildapp.romanblack.VideoPlugin.utils.rx.RxUtils;
import com.ibuildapp.romanblack.VideoPlugin.utils.rx.SimpleSubscriber;
import com.restfb.util.StringUtils;

import java.util.Collections;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

public class DetailsInfoFragment extends Fragment {

    private RecyclerView list;
    private IbaApi api;
    private VideoItem currentItem;
    private DetailsInfoAdapter adapter;

    private View sendButton;
    private EditText messageText;
    private boolean isVimeo;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragment = inflater.inflate(R.layout.video_plugin_details_info, container, false);
        fragment.setBackgroundColor(Statics.color1);

        list = (RecyclerView) fragment.findViewById(R.id.video_plugin_details_info_list);
        sendButton = fragment.findViewById(R.id.video_plugin_details_info_send_button);
        messageText = (EditText) fragment.findViewById(R.id.video_plugin_details_info_edit_text);

        api = new IbaApi();

        Bundle args = getArguments();
        currentItem = (VideoItem) args.getSerializable(VideoPluginConstants.ITEM);
        isVimeo = args.getBoolean(VideoPluginConstants.VIMEO);
        initData();
        return fragment;
    }

    private void initData() {
        list.setLayoutManager(new LinearLayoutManager(getActivity()));

        Observable<CommentsData> commentsDataObservable = api.getComments(String.valueOf(currentItem.getId()))
                .compose(RxUtils.<CommentsData>applyCustomSchedulers(Schedulers.io(), AndroidSchedulers.mainThread()))
                .map(new Func1<CommentsData, CommentsData>() {
                    @Override
                    public CommentsData call(CommentsData commentsData) {
                        Collections.sort(commentsData.getData(), new CommentDataComparator());
                        return commentsData;
                    }
                }).onErrorResumeNext(new Func1<Throwable, Observable<? extends CommentsData>>() {
                    @Override
                    public Observable<? extends CommentsData> call(Throwable throwable) {
                        return Observable.create(new Observable.OnSubscribe<CommentsData>() {
                            @Override
                            public void call(Subscriber<? super CommentsData> subscriber) {
                                subscriber.onNext(new CommentsData());
                                subscriber.onCompleted();
                            }
                        });
                    }
                });
        commentsDataObservable.subscribe(new SimpleSubscriber<CommentsData>());

        Observable<Integer> likesObservable = api.getLikeCount(currentItem)
                .compose(RxUtils.<Integer>applyCustomSchedulers(Schedulers.io(), AndroidSchedulers.mainThread()));
        likesObservable.subscribe(new SimpleSubscriber<Integer>());

        Observable.combineLatest(commentsDataObservable, likesObservable
                , new Func2<CommentsData, Integer, Void>() {
                    @Override
                    public Void call(CommentsData commentsData, Integer likesCount) {
                        onOperationCompleted(commentsData, likesCount);
                        return null;
                    }
                }).subscribe(new SimpleSubscriber<Void>());
    }

    private void onOperationCompleted(CommentsData commentsData, Integer likesCount) {
        currentItem.setLikesCount(likesCount);
        adapter = new DetailsInfoAdapter((VideoDetailsActivity) getActivity(), commentsData, currentItem, isVimeo);
        adapter.setListener(new FBLikePressedListener() {
            @Override
            public void onLikePressed() {
                likePressed();
            }
        });
        list.setAdapter(adapter);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Authorization.isAuthorized())
                    ((VideoDetailsActivity) getActivity()).authorize();
                else postMessage();
            }
        });
    }

    public void likePressed(){
        if (Authorization.isAuthorized(Authorization.AUTHORIZATION_TYPE_FACEBOOK)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        boolean res = FacebookAuthorizationActivity.like(currentItem.getUrl());
                        if (res) {
                            currentItem.setLikesCount(currentItem.getLikesCount() + 1);
                            currentItem.setLiked(true);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.notifyItemChanged(0);
                                }
                            });
                        }
                    } catch (FacebookAuthorizationActivity.FacebookNotAuthorizedException e) {
                        Authorization.authorize(getActivity(), VideoPluginConstants.FACEBOOK_AUTH_LIKE, Authorization.AUTHORIZATION_TYPE_FACEBOOK);
                    } catch (FacebookAuthorizationActivity.FacebookAlreadyLiked facebookAlreadyLiked) {
                        currentItem.setLiked(true);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyItemChanged(0);
                            }
                        });
                    }
                }
            }).start();
        } else
            Authorization.authorize(getActivity(), VideoPluginConstants.FACEBOOK_AUTH_LIKE, Authorization.AUTHORIZATION_TYPE_FACEBOOK);
    }

    public void postMessage() {
        String textForPost = messageText.getText().toString();
        if (StringUtils.isBlank(textForPost))
            return;

        KeyboardUtils.hideKeyboard(getActivity());
        messageText.setText("");

        api.postComment(String.valueOf(currentItem.getId()), textForPost)
                .compose(RxUtils.<CommentsData>applyCustomSchedulers(Schedulers.io(), AndroidSchedulers.mainThread()))
                .subscribe(new SimpleSubscriber<CommentsData>(){
                    @Override
                    public void onNext(CommentsData commentsData) {
                        adapter.addNewItem(commentsData);
                    }
                });
    }

    public void onCommentCountChange(int total_comments, int position) {
        adapter.getItemByPosition(position).setTotal_comments(total_comments);
        adapter.notifyItemChanged(position);
    }
}
