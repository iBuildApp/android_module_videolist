package com.ibuildapp.romanblack.VideoPlugin.api.ibaapi;


import com.ibuildapp.romanblack.VideoPlugin.api.ibaapi.model.CommentsData;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import rx.Observable;

public interface IbaApiRetrofit {
    @GET("/mdscr/video/getcomments/{app_id}/{module_id}/{item_id}/0/{app_id}/{token}")
    Observable<CommentsData> getComments(
            @Path("app_id") String appId,
            @Path("module_id") String moduleId,
            @Path("item_id") String itemId,
            @Path("token") String token);

    @FormUrlEncoded
    @POST("/mdscr/video/")
    Observable<CommentsData> postComment(@Field("app_id") String appId,
                                         @Field("token") String token,
                                         @Field("module_id") String moduleId,
                                         @Field("action") String action,
                                         @Field("parent_id") String parentId,
                                         @Field("reply_id") String replayId,
                                         @Field("account_type") String accountType,
                                         @Field("account_id") String accountId,
                                         @Field("username") String username,
                                         @Field("avatar") String avatar,
                                         @Field("text") String text);

    @FormUrlEncoded
    @POST("/mdscr/video/")
    Observable<CommentsData> postComment(@Field("app_id") String appId,
                                         @Field("token") String token,
                                         @Field("module_id") String moduleId,
                                         @Field("action") String action,
                                         @Field("parent_id") String parentId,
                                         @Field("account_type") String accountType,
                                         @Field("account_id") String accountId,
                                         @Field("username") String username,
                                         @Field("avatar") String avatar,
                                         @Field("text") String text);


    @GET("/mdscr/video/getcomments/{app_id}/{module_id}/{item_id}/{comment_id}/{app_id}/{token}")
    Observable<CommentsData> getReplies(
            @Path("app_id") String appId,
            @Path("module_id") String moduleId,
            @Path("item_id") String itemId,
            @Path("comment_id") String commentId,
            @Path("token") String token);
}
