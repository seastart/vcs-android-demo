package com.freewind.meetingdemo.PB;

public interface RoomEvent {

    /**
     * 我进入房间事件
     * @param result  0-成功，其他表示错误代码
     */
    void onEnter(int result);

    /**
     * 我退出房间事件
     * @param result  0-成功，其他表示错误代码
     */
    void onExit(int result);

    /**
     * 房间信息同步事件
     * @param room
     */
    void onNotifyRoom(Models.Room room);

    /**
     * 成员信息同步事件
     * @param account
     */
    void onNotifyAccount(Models.Account account);

    /**
     * 你被踢出了房间
     */
    void onNotifyKickout();

    /**
     * 有人进入了房间
     * @param account
     */
    void onNotifyEnter(Models.Account account);

    /**
     * 有人离开了房间
     * @param account
     */
    void onNotifyExit(Models.Account account);

    /**
     * 会议开始
     * @param roomId
     */
    void onNotifyBegin(String roomId);

    /**
     * 会议结束
     * @param roomId
     */
    void onNotifyEnd(String roomId);

    /**
     * 视频数据回调
     * @param ost
     * @param tnd
     * @param trd
     * @param width
     * @param height
     * @param fourcc
     * @param clientId
     */
    void onFrame(byte[] ost, byte[] tnd, byte[] trd, int width, int height, int fourcc, int clientId);

    /**
     * 上行网络
     * @param speed  上行带宽 bps
     * @param delay  延迟 ms
     */
    void onSendInfo(int speed, int delay);

    /**
     * 下线网络信息
     * @param s
     */
    void onRecvInfo(String s);

    /**
     * 网络自适应
     * @param level 0：正常码率；-1：1/2码率；-2：1/4码率；1000：码率自适应开始工作
     */
    void onXBitrate(int level);
}
