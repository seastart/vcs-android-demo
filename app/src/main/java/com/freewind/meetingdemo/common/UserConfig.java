/*
 * Copyright (c) 2017 Hangzhou Freewind Technology Co., Ltd.
 * All rights reserved.
 * http://company.zaoing.com
 */

package com.freewind.meetingdemo.common;

import android.content.Context;
import android.content.SharedPreferences;

import com.alibaba.fastjson.JSONObject;
import com.freewind.meetingdemo.bean.UserInfoBean;

/**
 * 轻量级本地存储工具类
 *
 * @author superK
 * created at 2017/8/4 16:02
 */
public class UserConfig {
    private static final String SP_IS_FIRST_USE = "IsFirstUse";
    private static final String SP_REQUEST_TOKEN = "request_token";
    private static final String SP_PASSWORD = "password";
    private static final String SP_ADMIN = "admin";
    private static final String SP_USER = "user";
//    public static final String

    private static UserInfoBean userInfo;
    private static SharedPreferences sp;



    public static void init(Context context) {
        if (null == sp) {
            sp = context.getSharedPreferences("UserConfig", Context.MODE_PRIVATE);
        }
    }

    public static boolean isFirstUse() {
        return sp.getBoolean(SP_IS_FIRST_USE, true);
    }

    public static void setFirstUse(boolean b) {
        sp.edit().putBoolean(SP_IS_FIRST_USE, b).apply();
    }

    public static String getSpAdmin() {
        return sp.getString(SP_ADMIN, "");
    }

    public static void setSpAdmin(String spAdmin) {
        sp.edit().putString(SP_ADMIN, spAdmin).apply();
    }


    public static String getRequestToken() {
        return sp.getString(SP_REQUEST_TOKEN, "");
    }

    public static void setRequestToken(String token) {
        sp.edit().putString(SP_REQUEST_TOKEN, token).apply();
    }


    public static boolean isLogined() {
        return !sp.getString(SP_REQUEST_TOKEN, "").isEmpty();
    }


    public static String getSpPassword() {
        return sp.getString(SP_PASSWORD, "");
    }

    public static void setSpPassword(String token) {
        sp.edit().putString(SP_PASSWORD, token).apply();
    }

    public static UserInfoBean getUserInfo() {
        if (null == userInfo) {
            String userStr = sp.getString(SP_USER, "");
            if (userStr.isEmpty()) {
                userInfo = new UserInfoBean();
            } else {
                userInfo = JSONObject.parseObject(userStr, UserInfoBean.class);
            }
        }
        return userInfo;
    }

    public static void updateUserInfo(UserInfoBean u) {
        userInfo = u;
        sp.edit().putString(SP_USER, JSONObject.toJSONString(userInfo)).apply();
    }
}
