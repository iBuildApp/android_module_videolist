package com.ibuildapp.romanblack.VideoPlugin.api.ibaapi;


import android.text.TextUtils;

import com.appbuilder.sdk.android.authorization.Authorization;
import com.appbuilder.sdk.android.authorization.FacebookAuthorizationActivity;
import com.appbuilder.sdk.android.authorization.entities.User;
import com.ibuildapp.romanblack.VideoPlugin.api.ibaapi.model.CommentsData;
import com.ibuildapp.romanblack.VideoPlugin.model.VideoItem;
import com.ibuildapp.romanblack.VideoPlugin.utils.Statics;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.functions.Func0;

public class IbaApi {
        private IbaApiRetrofit ibaApi;

    public IbaApi(){
        ibaApi = new IbaApiService(com.appbuilder.sdk.android.Statics.BASE_DOMEN).getIbaApiRetrofit();
    }

    public Observable<CommentsData> getComments(String itemId){
        return ibaApi.getComments(
                com.appbuilder.sdk.android.Statics.appId,
                Statics.MODULE_ID,
                itemId,
                com.appbuilder.sdk.android.Statics.appToken);
    }

    public Observable<CommentsData> getReplies(String itemId, String commentId){
        return ibaApi.getReplies(
                com.appbuilder.sdk.android.Statics.appId,
                Statics.MODULE_ID,
                itemId,
                commentId,
                com.appbuilder.sdk.android.Statics.appToken);
    }

    private Integer getVideoLikesCount(VideoItem item) {

        String token = FacebookAuthorizationActivity.getFbToken(com.appbuilder.sdk.android.Statics.FACEBOOK_APP_ID, com.appbuilder.sdk.android.Statics.FACEBOOK_APP_SECRET);
        if ( TextUtils.isEmpty(token) )
            return null;

        List<String> urlList = new ArrayList<>();
        urlList.add(item.getUrl());
        Map<String, String> resultMap = FacebookAuthorizationActivity.getLikesForUrls(urlList, token);
        if (resultMap != null && resultMap.containsKey(item.getUrl()))
            return Integer.valueOf(resultMap.get(item.getUrl()));
        return 0;
    }

    public Observable<Integer> getLikeCount(final VideoItem item){
        return Observable.defer(new Func0<Observable<Integer>>() {
            @Override
            public Observable<Integer> call() {
                return Observable.just(getVideoLikesCount(item));
            }
        });
    }

    public Observable<CommentsData> postComment(String targetId, String message){
        String appId = com.appbuilder.sdk.android.Statics.appId;
        String token = com.appbuilder.sdk.android.Statics.appToken;
        String moduleId = Statics.MODULE_ID;
        String accountType;
        if (Authorization.getAuthorizedUser().getAccountType() == User.ACCOUNT_TYPES.FACEBOOK)
            accountType = "facebook";
         else if (Authorization.getAuthorizedUser().getAccountType() == User.ACCOUNT_TYPES.TWITTER)
            accountType = "twitter";
         else
            accountType = "ibuildapp";

        String accountId = Authorization.getAuthorizedUser().getAccountId();
        String username = Authorization.getAuthorizedUser().getFullName();
        String url = Authorization.getAuthorizedUser().getAvatarUrl();

        return ibaApi.postComment(appId,
                token,
                moduleId,
                "postcomment",
                targetId,
                accountType,
                accountId,
                username,
                url,
                message);
    }

    public Observable<CommentsData> postComment(String targetId, String parentId, String message){

        String accountType;
        if (Authorization.getAuthorizedUser().getAccountType() == User.ACCOUNT_TYPES.FACEBOOK)
            accountType = "facebook";
        else if (Authorization.getAuthorizedUser().getAccountType() == User.ACCOUNT_TYPES.TWITTER)
            accountType = "twitter";
        else
            accountType = "ibuildapp";

        return ibaApi.postComment(com.appbuilder.sdk.android.Statics.appId,
                com.appbuilder.sdk.android.Statics.appToken,
                Statics.MODULE_ID,
                "postcomment",
                targetId,
                parentId,
                accountType,
                Authorization.getAuthorizedUser().getAccountId(),
                Authorization.getAuthorizedUser().getFullName(),
                Authorization.getAuthorizedUser().getAvatarUrl(),
                message);
    }
}
