package com.freewind.meetingdemo.util;

import android.app.Activity;

import com.freewind.vcs.interfaces.ScreenEvent;
import com.freewind.vcs.screen.ScreenHelper;

/**
 * 投屏工具类
 * Created by SuperK on 2023/6/21.
 */
public class ScreenUtil {

    private ScreenUtil(){}

    private static final class InstanceHolder {
        static final ScreenUtil instance = new ScreenUtil();
    }

    public static ScreenUtil getInstance(){
        return InstanceHolder.instance;
    }

    ScreenHelper screenHelper;
    ScreenEvent screenEvent;
    boolean enableSendAudio;
    boolean enableSendVideo;

    /**
     * 设置录屏事件回调
     * @param screenEvent 回调
     */
    public void setScreenEvent(ScreenEvent screenEvent){
        this.screenEvent = screenEvent;
    }

    /**
     * 初始化投屏组件
     * @param activity activity
     * @param ipAddress 地址
     * @param name 昵称
     */
    public void init(Activity activity, String ipAddress, String name){
        screenHelper = new ScreenHelper(activity, ipAddress, name);
        screenHelper.setScreenEvent(new ScreenEvent() {
            @Override
            public void onScreenConfirm(boolean enable, String extra) {
                if (screenEvent != null){
                    screenEvent.onScreenConfirm(enable, extra);
                }
            }

            @Override
            public void onRecordingStart(){
                if (screenEvent != null){
                    screenEvent.onRecordingStart();
                }
            }

            @Override
            public void onRecordingStop(){
                if (screenEvent != null){
                    screenEvent.onRecordingStop();
                }
            }

            @Override
            public void onRecordingError(String msg) {
                if (screenEvent != null){
                    screenEvent.onRecordingError(msg);
                }
            }

            @Override
            public void onClose(String reason) {
                if (screenEvent != null){
                    screenEvent.onClose(reason);
                }
            }

            @Override
            public void onTimeOut() {
                if (screenEvent != null){
                    screenEvent.onTimeOut();
                }
            }

            @Override
            public void onError(int code, String msg) {
                if (screenEvent != null){
                    screenEvent.onError(code, msg);
                }
            }

            @Override
            public void onUploadStatus(String info) {
                if (screenEvent != null){
                    screenEvent.onUploadStatus(info);
                }
            }

            @Override
            public void onJamLevelChanged(int i) {

            }
        });
    }

    /**
     * 开始录屏
     */
    public void startRecording(boolean sendAudio, String debugAddress){
        if (screenHelper == null){
            return;
        }
        screenHelper.startScreenRecording(sendAudio, true, debugAddress);
    }

    /**
     * 开始录屏
     */
    public void startRecording(boolean sendAudio){
        startRecording(sendAudio, null);
        enableSendAudio = sendAudio;
        enableSendVideo = true;
    }

    /**
     * 是否发送音频
     * @param enable Boolean
     * @return 设置后当前音频的状态
     */
    public boolean enableSendAudio(boolean enable){
        if (screenHelper == null){
            enableSendAudio = false;
        }else {
            if (screenHelper.enableSendAudio(enable)){
                enableSendAudio = enable;
            }else {//设置失败
                enableSendAudio = false;
            }
        }
        return enableSendAudio;
    }

    /**
     * 是否发送音频
     * @param enable Boolean
     * @return 设置后当前音频的状态
     */
    public boolean enableSendVideo(boolean enable){
        if (screenHelper == null){
            enableSendVideo = false;
        }else {
            if (screenHelper.enableSendVideo(enable)){
                enableSendVideo = enable;
            }else {//设置失败
                enableSendVideo = false;
            }
        }
        return enableSendVideo;
    }

    /**
     * 当前是否录音
     * @return Boolean
     */
    public boolean isEnableSendAudio(){
        return enableSendAudio;
    }

    /**
     * 当前是否发送视频
     * @return Boolean
     */
    public boolean isEnableSendVideo(){
        return enableSendVideo;
    }

    /**
     * 结束录屏
     */
    public void stopRecording(){
        enableSendAudio = false;
        enableSendVideo = false;
        if (screenHelper == null){
            return;
        }
        screenHelper.stopScreenRecording();
    }

    /**
     * 是否在投屏
     * @return boolean
     */
    public boolean isRecording(){
        if (screenHelper != null){
            return screenHelper.isBusyRecording();
        }
        return false;
    }

    /**
     * 释放
     */
    public void release(){
        if (screenHelper != null){
            screenHelper.release();
        }
    }
}
