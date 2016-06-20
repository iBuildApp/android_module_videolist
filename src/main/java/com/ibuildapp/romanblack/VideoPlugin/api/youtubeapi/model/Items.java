package com.ibuildapp.romanblack.VideoPlugin.api.youtubeapi.model;


import java.io.Serializable;

public class Items implements Serializable {
    private ContentDetails contentDetails;
    private Snippet snippet;

    public Snippet getSnippet() {
        return snippet;
    }

    public void setSnippet(Snippet snippet) {
        this.snippet = snippet;
    }

    public ContentDetails getContentDetails() {
        return contentDetails;
    }

    public void setContentDetails(ContentDetails contentDetails) {
        this.contentDetails = contentDetails;
    }
}
