// ////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2015-2017 Hangzhou Freewind Technology Co., Ltd.
// All rights reserved.
// http://www.seastart.cn
//
// ///////////////////////////////////////////////////////////////////////////
package com.freewind.meetingdemo.util;


import com.freewind.meetingdemo.base.BaseBean;
import com.freewind.meetingdemo.bean.RoomInfoBean;
import com.freewind.meetingdemo.bean.UserInfoBean;
import com.freewind.meetingdemo.common.Constants;
import com.freewind.meetingdemo.http.HttpCallBack;
import com.freewind.meetingdemo.http.HttpHelper;
import com.loopj.android.http.RequestParams;

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
    public static void enterMeeting(String room_no, String password, final HttpCallBack<RoomInfoBean> callBack) {
        String url = Constants.API_HOST + "room/enter-room";
        RequestParams params = new RequestParams();
        params.put("room_no", room_no);
        if (!password.isEmpty()) {
            params.put("password", password);
        }
        HttpHelper.executePost(RoomInfoBean.class, url, params, callBack);
    }

    /**
     * 登陆
     *
     * @param mobile   手机
     * @param code     验证码（与密码二选一）
     * @param password 密码（与验证码二选一）
     */
    public static void login(String mobile, String password, String code, final HttpCallBack<UserInfoBean> callBack) {
        String url = Constants.API_HOST + "account/login";
        RequestParams params = new RequestParams();
        params.put("loginname", mobile);
        params.put("password", MD5Util.MD5Encode(password));
        params.put("dev_type", 2);
        HttpHelper.executePost(UserInfoBean.class, url, params, callBack);
    }

    /**
     * 获取手机验证码
     *
     * @param mobile 手机号
     */
    public static void getCode(int user_for,String mobile, final HttpCallBack<BaseBean> callBack) {
        String url = Constants.API_HOST + "account/vcode";
        RequestParams params = new RequestParams();
        params.put("used_for", user_for);
        params.put("mobile", mobile);
        if (user_for == 2){  // 重置密码
            params.put("account_name", mobile);
        }
        HttpHelper.executePost(BaseBean.class, url, params, callBack);
    }

    /**
     * 注册
     */
    public static void register(String name, String password, String nickname, String mobile, String code, HttpCallBack<UserInfoBean> callBack) {
        String url = Constants.API_HOST + "account/register";
        RequestParams params = new RequestParams();
        params.put("name", name);
        params.put("password", password);
        params.put("nickname", nickname);
        params.put("mobile", mobile);
        params.put("type", 1);
        params.put("vcode", code);
        HttpHelper.executePost(UserInfoBean.class, url, params, callBack);
    }
}

