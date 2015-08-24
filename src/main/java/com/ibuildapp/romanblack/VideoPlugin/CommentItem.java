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
package com.ibuildapp.romanblack.VideoPlugin;

import com.appbuilder.sdk.android.authorization.entities.User;
import java.io.Serializable;
import java.util.Date;

/**
 * Entity class that represents comment to video.
 */
public class CommentItem implements Serializable {

    private int commentsCount = 0;
    private long id = 0;
    private long trackId = 0;
    private long replyId = 0;
    private String author = "";
    private String text = "";
    private String avatarUrl = "";
    private String avatarPath = "";
    private User.ACCOUNT_TYPES accountType = User.ACCOUNT_TYPES.IBUILDAPP;
    private String accountId = "";
    private Date date = null;

    /**
     * Returns the count of comments that was made on this comment.
     * @return the count of comments
     */
    public int getCommentsCount() {
        return commentsCount;
    }

    /**
     * Sets the count of comments that was made on this comment.
     * @param commentsCount the count of comments to set
     */
    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
    }

    /**
     * Increases count of comments that was made on this comment.
     */
    public void increaseComments() {
        this.commentsCount++;
    }

    /**
     * Returns the ID of this comment.
     * @return the comment id
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the comment ID.
     * @param id comment ID to set
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Returns the author name of this comment.
     * @return the author name
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Sets the author name of this comment.
     * @param author the author name to set
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * Returns the comment text.
     * @return the comment text
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the comment text.
     * @param text the comment text to set
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Returns the author's avatar URL.
     * @return the author's avatar URL
     */
    public String getAvatarUrl() {
        return avatarUrl;
    }

    /**
     * Sets the author's avatar URL.
     * @param avatarUrl the author's avatar URL to set
     */
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    /**
     * Returns the author's avatar cache path.
     * @return the author's avatar cache path
     */
    public String getAvatarPath() {
        return avatarPath;
    }

    /**
     * Sets the author's avatar cache path.
     * @param avatarUrl the author's avatar cache path to set
     */
    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }

    /**
     * Returns the comment date.
     * @return the comment date
     */
    public Date getDate() {
        return date;
    }

    /**
     * Sets the comment date.
     * @param date the comment date to set
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * Sets the comment date.
     * @param millis the comment date timestamp
     */
    public void setDate(long millis) {
        this.date = new Date(millis);
    }

    /**
     * Sets the comment date.
     * @param millis the comment date timestamp string
     */
    public void setDate(String millis) {
        this.date = new Date(Long.parseLong(millis));
    }

    /**
     * Returns the video ID.
     * @return the video ID
     */
    public long getTrackId() {
        return trackId;
    }

    /**
     * Sets the video ID.
     * @param trackId the video ID to set
     */
    public void setTrackId(long trackId) {
        this.trackId = trackId;
    }
    
    /**
     * Returns the comment reply id if this comment was made to comment.
     * @return the comment reply id
     */
    public long getReplyId() {
        return replyId;
    }

    /**
     * Sets the comment reply id.
     * @param replyId the reply id to set
     */
    public void setReplyId(long replyId) {
        this.replyId = replyId;
    }

    /**
     * Returns the author's account type.
     * @return the author's account type
     */
    public User.ACCOUNT_TYPES getAccountType() {
        return accountType;
    }

    /**
     * Sets the author's account type.
     * Type of account can be one of the "facebook", "twitter", "ibuildapp", "vkontakte" or "linkedin"
     * @param accountType the account type to set
     */
    public void setAccountType(String accountType) {
        if (accountType.equalsIgnoreCase("facebook")) {
            this.accountType = User.ACCOUNT_TYPES.FACEBOOK;
        } else if (accountType.equalsIgnoreCase("twitter")) {
            this.accountType = User.ACCOUNT_TYPES.TWITTER;
        } else if (accountType.equalsIgnoreCase("ibuildapp")) {
            this.accountType = User.ACCOUNT_TYPES.IBUILDAPP;
        } else if (accountType.equalsIgnoreCase("vkontakte")) {
            this.accountType = User.ACCOUNT_TYPES.VKONTAKTE;
        } else if (accountType.equalsIgnoreCase("linkedin")) {
            this.accountType = User.ACCOUNT_TYPES.LINKEDIN;
        } else {
            this.accountType = User.ACCOUNT_TYPES.GUEST;
        }
    }

    /**
     * Returns the ID of the author's account.
     * @return the ID of the author's account
     */
    public String getAccountId() {
        return accountId;
    }

    /**
     * Sets the ID of the author's account.
     * @param accountId the account id to set
     */
    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }
}
