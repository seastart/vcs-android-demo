/*
 * Copyright (c) 2017 Hangzhou Freewind Technology Co., Ltd.
 * All rights reserved.
 * http://company.zaoing.com
 */

package com.freewind.meetingdemo.bean;

import com.freewind.meetingdemo.base.BaseBean;

public class RegBean extends BaseBean {
    private String addr;
    private int port;
    private int mqtt_port;
    private String mqtt_address;
    private String server_id;

    public String getServer_id() {
        return server_id;
    }

    public void setServer_id(String server_id) {
        this.server_id = server_id;
    }

    public String getMqtt_address() {
        return mqtt_address;
    }

    public void setMqtt_address(String mqtt_address) {
        this.mqtt_address = mqtt_address;
    }

    public int getMqtt_port() {
        return mqtt_port;
    }

    public void setMqtt_port(int mqtt_port) {
        this.mqtt_port = mqtt_port;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
