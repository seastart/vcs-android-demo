// ////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2015-2017 Hangzhou Freewind Technology Co., Ltd.
// All rights reserved.
// http://www.seastart.cn
//
// ///////////////////////////////////////////////////////////////////////////
package com.freewind.meetingdemo.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.freewind.meetingdemo.base.BaseBean;
import com.freewind.meetingdemo.bean.RoomInfoBean;
import com.freewind.meetingdemo.bean.UserInfoBean;
import com.freewind.meetingdemo.common.Constants;
import com.freewind.meetingdemo.http.HttpCallBack;
import com.freewind.meetingdemo.http.HttpHelper;
import com.loopj.android.http.RequestParams;

import java.util.ArrayList;
import java.util.List;

/**
 * api接口定义
 */
public class Requester {

    /**
     * 视频会议进入
     *
     * @param room_no  会议室创建后获取的id
     * @param password 视频会议密码(如果会议启用密码)
     */
    public static void enterMeeting(Context context, String room_no, String password, final HttpCallBack<RoomInfoBean> callBack) {
        String url = Constants.API_HOST + "room/enter";
        RequestParams params = new RequestParams();
        List<String> paramsList = new ArrayList<>();

        params.put("room_no", room_no);
        paramsList.add("room_no="+room_no);

        if (!password.isEmpty()) {
            params.put("password", password);
            paramsList.add("password="+password);
        }
        params.put("device_id", getDeviceID(context));

        paramsList.add("device_id="+getDeviceID(context));


        HttpHelper.executePost(RoomInfoBean.class, url, params, paramsList, callBack);
    }

    @SuppressLint({"NewApi", "MissingPermission"})
    private static String getDeviceID(Context context){
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            return tm.getDeviceId();
        } catch (Exception e){
            return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        }
    }

    /**
     * 登陆
     *
     * @param mobile   手机
//     * @param code     验证码（与密码二选一）
     * @param password 密码（与验证码二选一）
     */
    public static void login(String mobile, String password, final HttpCallBack<UserInfoBean> callBack) {
        String url = Constants.API_HOST + "account/login";
        RequestParams params = new RequestParams();

        params.put("loginname", mobile);
        params.put("password", PwdUtil.hmacSha1(mobile + password));
        params.put("dev_type", 2);


        List<String> paramsList = new ArrayList<>();
        paramsList.add("loginname="+mobile);
        paramsList.add("password="+PwdUtil.hmacSha1(mobile + password));
        paramsList.add("dev_type="+2);

        HttpHelper.executePost(UserInfoBean.class, url, params, paramsList, callBack);
    }

    /**
     * 获取手机验证码
     * @param mobile 手机号：1-注册；2-重置密码
     */
    public static void getCode(int user_for,String mobile, final HttpCallBack<BaseBean> callBack) {
        String url = Constants.API_HOST + "account/vcode";
        RequestParams params = new RequestParams();
        params.put("used_for", user_for);
        params.put("mobile", mobile);

        List<String> paramsList = new ArrayList<>();
        paramsList.add("used_for="+user_for);
        paramsList.add("mobile="+mobile);

        if (user_for == 2){  // 重置密码
            params.put("account_name", mobile);
            paramsList.add("account_name="+mobile);
        }

        HttpHelper.executePost(BaseBean.class, url, params, paramsList, callBack);
    }

    /**
     * 注册
     */
    public static void register(String mobile, String password, String code, HttpCallBack<UserInfoBean> callBack) {
        String url = Constants.API_HOST + "account/register";
        RequestParams params = new RequestParams();
        params.put("name", mobile);
        params.put("password", PwdUtil.hmacSha1(mobile + password));
        params.put("nickname", mobile);
        params.put("mobile", mobile);
        params.put("type", 1);
        params.put("vcode", code);

        List<String> paramsList = new ArrayList<>();
        paramsList.add("name="+mobile);
        paramsList.add("nickname="+mobile);
        paramsList.add("password="+PwdUtil.hmacSha1(mobile + password));
        paramsList.add("mobile="+mobile);
        paramsList.add("type="+1);
        paramsList.add("vcode="+code);

        HttpHelper.executePost(UserInfoBean.class, url, params, paramsList, callBack);
    }

    /**
     * 修改密码/忘记密码
     */
    public static void rePassWord(String mobile, String code, String password, final HttpCallBack<BaseBean> callBack) {
        String url = Constants.API_HOST + "account/reset-password";
        RequestParams params = new RequestParams();
        params.put("name", mobile);
        params.put("vcode", code);
        params.put("new_password", PwdUtil.hmacSha1(mobile + password));

        List<String> paramsList = new ArrayList<>();
        paramsList.add("name="+mobile);
        paramsList.add("vcode="+code);
        paramsList.add("new_password="+PwdUtil.hmacSha1(mobile + password));

        HttpHelper.executePost(BaseBean.class, url, params, paramsList, callBack);
    }
}

