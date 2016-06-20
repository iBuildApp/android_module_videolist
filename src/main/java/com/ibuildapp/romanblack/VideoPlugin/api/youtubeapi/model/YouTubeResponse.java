package com.ibuildapp.romanblack.VideoPlugin.api.youtubeapi.model;


import java.io.Serializable;

public class YouTubeResponse implements Serializable {
    private Items[] items;

    public Items[] getItems() {
        return items;
    }

    public void setItems(Items[] items) {
        this.items = items;
    }
}
