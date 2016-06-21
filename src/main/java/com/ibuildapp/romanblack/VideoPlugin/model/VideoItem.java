/****************************************************************************
*                                                                           *
*  Copyright (C) 2014-2015 iBuildApp, Inc. ( http://ibuildapp.com )         *
*                                                                           *
*  This file is part of iBuildApp.                                          *
*                                                                           *
*  This Source Code Form is subject to the terms of the iBuildApp License.  *
*  You can obtain one at http://ibuildapp.com/license/                      *
*                                                                           *
****************************************************************************/
package com.ibuildapp.romanblack.VideoPlugin.model;

import java.io.Serializable;
import java.util.Date;

import android.graphics.Color;
import android.speech.tts.UtteranceProgressListener;

import com.ibuildapp.romanblack.VideoPlugin.api.vimeoapi.model.VimeoResponse;
import com.ibuildapp.romanblack.VideoPlugin.api.youtubeapi.model.YouTubeResponse;

/**
 * Entity class that represents parsed video item.
 */
public class VideoItem implements Serializable {
    private long id = 0;
    private String title = "";
    private String url = "";
    private String description = "";
    private String coverPath = "";
    private String coverUrl = "";
    private int color = Color.WHITE;
    private int totalComments = 0;
    private int likesCount = 0;
    private boolean liked = false;
    private VideoType videoType;
    private YouTubeResponse response;
    private VimeoResponse vimeoResponse;
    private String uploadDuration;
    public volatile boolean isLoading = false;
    private String xmlDuration = "";
    private Long creationLong = new Date().getTime();

    /**
     * Constructs new video item instance.
     */
    public VideoItem() {
    }

    /**
     * Sets the video title.
     * @param value the video title
     */
    public void setTitle(String value) {
        title = value;
    }

    /**
     * Returns the video title.
     * @return the video title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the video description.
     * @param value the video description to set
     */
    public void setDescription(String value) {
        description = value;
    }

    /**
     * Returns the video description.
     * @return the video description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the video URL.
     * @param value the video URL to set
     */
    public void setUrl(String value) {
        url = value;

        if (url!= null && url.contains("youtube"))
            videoType = VideoType.YOUTUBE;
        else if (url!= null && url.contains("vimeo"))
            videoType = VideoType.VIMEO;
        else if (url!= null && url.contains("http://ibuildapp.s3-website-us-east-1.amazonaws.com"))
            videoType = VideoType.UPLOAD;
            else videoType = VideoType.NOT_SPECIFIED;
    }

    /**
     * Returns the video URL.
     * @return the video URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the URL that contains video cover image.
     * @param value the video cover image URL
     */
    public void setCoverUrl(String value) {
        coverUrl = value;
    }

    /**
     * Returns the video cover image URL.
     * @return the video cover image URL
     */
    public String getCoverUrl() {
        return coverUrl;
    }

    /**
     * Sets the video cover image cache path.
     * @param value the cache path to set
     */
    public void setCoverPath(String value) {
        coverPath = value;
    }

    /**
     * Returns the video cover image cache path.
     * @return the video cover image cache path
     */
    public String getCoverPath() {
        return coverPath;
    }

    /**
     * Sets the video item text color.
     * @param color the color to set
     */
    public void setTextColor(int color) {
        this.color = color;
    }

    /**
     * Returns the video item text color.
     * @return the video item text color
     */
    public int getTextColor() {
        return color;
    }

    /**
     * Returns the video ID.
     * @return the video ID
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the video ID.
     * @param id ID to set
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Returns the comments count that was made on this video.
     * @return the comments count
     */
    public int getTotalComments() {
        return totalComments;
    }

    /**
     * Sets the comments count that was made on this video.
     * @param totalComments the comments count to set
     */
    public void setTotalComments(int totalComments) {
        this.totalComments = totalComments;
    }

    /**
     * Returns the Facebook likes count that was made on this video.
     * @return the likes count
     */
    public int getLikesCount() {
        return likesCount;
    }

    /**
     * Sets the comments count that was made on this video.
     * @param likesCount the likes count to set
     */
    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    /**
     * Returns true if this video was liked by this user.
     * @return true if this video was liked by this user, false otherwise
     */
    public boolean isLiked() {
        return liked;
    }

    /**
     * Sets flag that this video was liked by this user. 
     * @param liked flag to set
     */
    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public VideoType getVideoType() {
        return videoType;
    }

    public YouTubeResponse getResponse() {
        return response;
    }

    public void setResponse(YouTubeResponse response) {
        this.response = response;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void setLoading(boolean loading) {
        isLoading = loading;
    }

    public VimeoResponse getVimeoResponse() {
        return vimeoResponse;
    }

    public void setVimeoResponse(VimeoResponse vimeoResponse) {
        this.vimeoResponse = vimeoResponse;
    }

    public String getUploadDuration() {
        return uploadDuration;
    }

    public void setUploadDuration(String uploadDuration) {
        this.uploadDuration = uploadDuration;
    }

    public void setXmlDuration(String trim) {
        this.xmlDuration = trim;
    }

    public void setCreationLong(Long creationLong) {
        this.creationLong = creationLong;
    }

    public Long getCreationLong() {
        return creationLong * 1000;
    }

    public String getXmlDuration() {
        return xmlDuration;
    }
}
