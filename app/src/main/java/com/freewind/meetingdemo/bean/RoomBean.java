// ////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2015-2017 Hangzhou Freewind Technology Co., Ltd.
// All rights reserved.
// http://www.seastart.cn
//
// ///////////////////////////////////////////////////////////////////////////
package com.freewind.meetingdemo.bean;


import com.freewind.meetingdemo.base.BaseBean;

import java.io.Serializable;

public class RoomBean extends BaseBean implements Serializable {
    private String id;//会议室编号
    private String account_id;//会议室归属的账号id
    private String no;//会议室号码
    private String sdk_no;//底层会议室编号
    private String name;
    private String room_name;//会议室名称
    private int access_type;//会议室的权限    0表示开放（没有设置权限） 1表示密码   2表示白名单
    private String access_pwd;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAccount_id() {
        return account_id;
    }

    public void setAccount_id(String account_id) {
        this.account_id = account_id;
    }

    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    public String getSdk_no() {
        return sdk_no;
    }

    public void setSdk_no(String sdk_no) {
        this.sdk_no = sdk_no;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRoom_name() {
        return room_name;
    }

    public void setRoom_name(String room_name) {
        this.room_name = room_name;
    }

    public int getAccess_type() {
        return access_type;
    }

    public void setAccess_type(int access_type) {
        this.access_type = access_type;
    }

    public String getAccess_pwd() {
        return access_pwd;
    }

    public void setAccess_pwd(String access_pwd) {
        this.access_pwd = access_pwd;
    }
}
