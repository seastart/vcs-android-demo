// ////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2015-2017 Hangzhou Freewind Technology Co., Ltd.
// All rights reserved.
// http://www.seastart.cn
//
// ///////////////////////////////////////////////////////////////////////////
package com.freewind.meetingdemo.bean;

import java.io.Serializable;

/**
 * 会议成员
 */
public class MemberBean implements Serializable {
    private boolean mute = false;
    private boolean closeVideo = false;
    private String clientId;
    private boolean isMuteMode = false;
    private int width;
    private int height;

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setMuteMode(boolean muteMode) {
        isMuteMode = muteMode;
    }

    public boolean isMuteMode() {
        return isMuteMode;
    }

    public boolean isMute() {
        return mute;
    }

    public void setMute(boolean mute) {
        this.mute = mute;
    }

    public boolean isCloseVideo() {
        return isMuteMode || closeVideo;
    }

    public void setCloseVideo(boolean closeVideo) {
        this.closeVideo = closeVideo;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
