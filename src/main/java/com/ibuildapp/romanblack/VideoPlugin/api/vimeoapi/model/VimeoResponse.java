package com.ibuildapp.romanblack.VideoPlugin.api.vimeoapi.model;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;

public class VimeoResponse implements Serializable{
    private String duration;

    @SerializedName("upload_date")
    @Expose
    private String uploadDate;

    private String parsedDuration;
    private Date postDate;

    @SerializedName("video_id")
    @Expose
    private String videoId;

    @SerializedName("thumbnail_url_with_play_button")
    @Expose
    private String thumbnailUrlWithPlayButton;

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(String uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getParsedDuration() {
        return parsedDuration;
    }

    public void setParsedDuration(String parsedDuration) {
        this.parsedDuration = parsedDuration;
    }

    public Date getPostDate() {
        return postDate;
    }

    public void setPostDate(Date postDate) {
        this.postDate = postDate;
    }

    public String getVideoId() {
        return videoId;
    }

    public String getThumbnailUrlWithPlayButton() {
        return thumbnailUrlWithPlayButton;
    }
}
