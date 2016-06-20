package com.ibuildapp.romanblack.VideoPlugin.api.youtubeapi.model;


import java.io.Serializable;

public class ContentDetails implements Serializable {
    private String duration;
    private String parsedDuration;

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getParsedDuration() {
        return parsedDuration;
    }

    public void setParsedDuration(String parsedDuration) {
        this.parsedDuration = parsedDuration;
    }
}
