package com.ibuildapp.romanblack.VideoPlugin.api.ibaapi.model;


import java.io.Serializable;

public class CommentData implements Serializable{
    private String id;
    private String username;
    private String text;
    private String account_id;
    private String module_id;
    private String reply_id;
    private int total_comments;
    private String app_id;
    private String avatar;
    private String create;
    private String account_type;
    private String parent_id;

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getText() {
        return text;
    }

    public String getAccount_id() {
        return account_id;
    }

    public String getModule_id() {
        return module_id;
    }

    public String getReply_id() {
        return reply_id;
    }


    public String getApp_id() {
        return app_id;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getCreate() {
        return create;
    }

    public String getAccount_type() {
        return account_type;
    }

    public String getParent_id() {
        return parent_id;
    }

    public int getTotal_comments() {
        return total_comments;
    }

    public void setTotal_comments(int total_comments) {
        this.total_comments = total_comments;
    }
}
