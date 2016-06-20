package com.ibuildapp.romanblack.VideoPlugin.api.ibaapi.model;


import java.util.ArrayList;
import java.util.List;

public class CommentsData {
    private String error;

    private List<CommentData> data;

    public CommentsData(){
        error = "";
        data = new ArrayList<>();
    }
    public String getError() {
        return error;
    }

    public List<CommentData> getData() {
        return data;
    }

    public void setData(List<CommentData> data) {
        this.data = data;
    }
}
