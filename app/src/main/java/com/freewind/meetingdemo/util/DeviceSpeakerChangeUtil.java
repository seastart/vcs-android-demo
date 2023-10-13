package com.freewind.meetingdemo.util;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

public class DeviceSpeakerChangeUtil {
    /**
     * 简介：设备语音切换-蓝牙-外放-听筒
     * 主要功能: 设备语音切换-蓝牙-外放-听筒
     */
    private static AudioManager mAudioManager;

    /**
     * 切换到外放
     */
    public static void changeToSpeaker(Context context){
        deviceChangeToHeadPhone(context);
        mAudioManager.setSpeakerphoneOn(true);  //打开外放
    }

    /**
     * 切换到蓝牙耳机
     */
    public static void changeToBlueTooth(Context context){
        deviceChangeToHeadPhone(context);
        mAudioManager.startBluetoothSco(); //启动蓝牙耳机
        mAudioManager.setBluetoothScoOn(true); //设置socOn开启，蓝牙耳机开启
        Log.e("111111111111", "切到蓝牙");
    }

    /**
     * 切换到耳机模式
     */
    public static void  deviceChangeToHeadPhone(Context context){
        if(mAudioManager==null) {
            mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        }
        mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        mAudioManager.stopBluetoothSco();        //停止蓝牙耳机
        mAudioManager.setBluetoothScoOn(false);  //设蓝牙ScoOn关闭，蓝牙耳机则会停止
        mAudioManager.setSpeakerphoneOn(false);  //设置外放话筒关闭
    }

}