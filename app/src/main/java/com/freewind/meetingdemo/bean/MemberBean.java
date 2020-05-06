// ////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2015-2017 Hangzhou Freewind Technology Co., Ltd.
// All rights reserved.
// http://www.seastart.cn
//
// ///////////////////////////////////////////////////////////////////////////
package com.freewind.meetingdemo.bean;

import com.freewind.vcs.Models;

import java.io.Serializable;

/**
 * 会议成员
 */
public class MemberBean implements Serializable {
    private Models.DeviceState mute;
    private Models.DeviceState closeVideo;
    private boolean closeOtherVideo;//本地是否关闭视频
    private boolean closeOtherAudio;//本地是否关闭音频
    private String sdkNo;
    private String accountId;

    public boolean isCloseOtherAudio() {
        return closeOtherAudio;
    }

    public void setCloseOtherAudio(boolean closeOtherAudio) {
        this.closeOtherAudio = closeOtherAudio;
    }

    public boolean isCloseOtherVideo() {
        return closeOtherVideo;
    }

    public void setCloseOtherVideo(boolean closeOtherVideo) {
        this.closeOtherVideo = closeOtherVideo;
    }

    public boolean isMute() {
        return mute == Models.DeviceState.DS_Closed || mute == Models.DeviceState.DS_Disabled;
    }

    public void setMute(Models.DeviceState deviceState) {
        this.mute = deviceState;
    }

    //是否关闭视频 true 关闭
    public boolean isCloseVideo() {
        return closeVideo == Models.DeviceState.DS_Closed || closeVideo == Models.DeviceState.DS_Disabled;
    }

    public Models.DeviceState getMute() {
        return mute;
    }

    public Models.DeviceState getCloseVideo() {
        return closeVideo;
    }

    public void setCloseVideo(Models.DeviceState deviceState) {
        this.closeVideo = deviceState;
    }

    public String getSdkNo() {
        return sdkNo;
    }

    public void setSdkNo(String sdkNo) {
        this.sdkNo = sdkNo;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }
}
