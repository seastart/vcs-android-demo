/*
 * Copyright (c) 2017 Hangzhou Freewind Technology Co., Ltd.
 * All rights reserved.
 * http://company.zaoing.com
 */

package com.freewind.meetingdemo.common;

public class Constants {
    public static final String DEBUG_ADDR = "debug_addr";
    public static final String DEBUG_SWITCH = "debug_switch";
    public static final String MULTI = "multi";
    public static final String AGC = "agc";
    public static final String AEC = "aec";
    public static final String FPS = "fps";
    public static final String CLOSE_SELF_VIDEO = "close_self_video";
    public static final String CLOSE_SELF_AUDIO = "close_self_audio";
    public static final String CLOSE_OTHER_AUDIO = "close_other_audio";
    public static final String CLOSE_OTHER_VIDEO = "close_other_video";
    public static final String SAMPLE_RATE = "sample_rate";
    public static final String HARD_DECODER = "hard_decoder";
    public static final String ROOM_INFO = "room_info";
    public static final String VIDEO_LEVEL = "video_level";


    public static final int CLOSE_VIDEO_OR_AUDIO = 1;//关闭视频/音频，不发送频/音频
    public static final int SEND_VIDEO_OR_AUDIO = 0;//打开视频/音频，发送视频/音频

    //服务器相关配置
//    public static String SERVER_HOST = "http://101.37.67.56:5000/";
    public static String SERVER_HOST = "http://vcs.anyconf.cn:5000";
//    public static String SERVER_HOST = "http://192.168.43.113:5000/";

//    public static String SERVER_HOST = "http://103.219.32.162:5100/";

    public static String API_VERSION = "/vcs/";
    public static String API_HOST = SERVER_HOST + API_VERSION;

    public static final String MSG_NET_ERROR = "服务器开小差啦，请重试";

    public static final int NEED_PWD = 490;
    public static final int REQUESTER_SUCCESS = 200;
}
