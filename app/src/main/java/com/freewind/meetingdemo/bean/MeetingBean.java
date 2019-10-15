package com.freewind.meetingdemo.bean;

import com.freewind.meetingdemo.base.BaseBean;

import java.io.Serializable;

/**
 * @author superK
 * created at 2018/5/15 10:37
 */
public class MeetingBean extends BaseBean {
    private String id;
    private String room_id;
    private long created_at;
    private UserBean account;
    private RoomBean room;
    private String host;
    private String port;
    private String session;
    private String wb_host;
    private boolean isInvite = false;

    public boolean isInvite() {
        return isInvite;
    }

    public void setInvite(boolean invite) {
        isInvite = invite;
    }

    public String getWb_host() {
        return wb_host;
    }

    public void setWb_host(String wb_host) {
        this.wb_host = wb_host;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRoom_id() {
        return room_id;
    }

    public void setRoom_id(String room_id) {
        this.room_id = room_id;
    }

    public UserBean getAccount() {
        return account;
    }

    public void setAccount(UserBean account) {
        this.account = account;
    }

    public long getCreated_at() {
        return created_at;
    }

    public void setCreated_at(long created_at) {
        this.created_at = created_at;
    }

    public RoomBean getRoom() {
        return room;
    }

    public void setRoom(RoomBean room) {
        this.room = room;
    }

    /**
     * code : 200
     * data : {"id":"2e724df523134bcc9cbfd1dfc171e122","account_id":"a7667ee1c25f43358ce334971610d25a","no":"915067778691","sdk_no":"60000425","name":"笔记","access_type":0}
     */

    private DataBean data;

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean implements Serializable {
        /**
         * id : 2e724df523134bcc9cbfd1dfc171e122
         * account_id : a7667ee1c25f43358ce334971610d25a
         * no : 915067778691
         * sdk_no : 60000425
         * name : 笔记
         * access_type : 0
         */

        private String id;
        private String account_id;
        private String no;
        private String sdk_no;
        private String name;
        private int access_type;
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

}
