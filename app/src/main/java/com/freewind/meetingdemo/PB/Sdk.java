package com.freewind.meetingdemo.PB;

import android.content.Context;

import com.ook.android.VcsApi;

/**
 * SDK 全局实例
 */
public class Sdk {
    private static Sdk instance;

    private Sdk() {
    }

    public static Sdk getInstance() {
        if (instance == null) {
            synchronized (Sdk.class) {
                if (instance == null) {
                    instance = new Sdk();
                }
            }
        }
        return instance;
    }

    private VcsApi vcsApi;

    /**
     * 全局初始化
     * @return 0表示成功，其他表示失败
     */
    public int init(Context context){
        vcsApi = new VcsApi(context);
        return vcsApi.VCS_Init();
    }

    /**
     * 全局注销
     */
    public void Cleanup(){
        if(vcsApi!=null){
            vcsApi.VCS_Cleanup();
            vcsApi=null;
        }
    }

    /**
     * 创建房间对象
     * @return
     */
    public RoomClient createRoomClient(){
        return new RoomClient(vcsApi);
    }

    /**
     * 是否支持硬件解码
     * @return
     */
    public boolean isHWSupport(){
        return vcsApi.VCS_getHWSupport()==0?false:true;
    }

    /**
     * 启用硬件解码,默认启用
     * @param enabled
     */
    public void setHwDecoder(boolean enabled){
        vcsApi.VCS_setUseHwDecoder(enabled);
    }

    /**
     * 获取当前解码模式
     * @return 0-软件；1-硬解；-1-未就绪
     */
    public int getHwDecoder(){
        return vcsApi.VCS_getCurrentDecoder();
    }
}
