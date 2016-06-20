package com.ibuildapp.romanblack.VideoPlugin.api.youtubeapi.model;


import java.io.Serializable;
import java.util.Date;

public class Snippet implements Serializable {
    private Thumbnails thumbnails;
    private String publishedAt;
    private Date publishedDate;

    public Thumbnails getThumbnails() {
        return thumbnails;
    }

    public void setThumbnails(Thumbnails thumbnails) {
        this.thumbnails = thumbnails;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Date getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(Date publishedDate) {
        this.publishedDate = publishedDate;
    }
}
