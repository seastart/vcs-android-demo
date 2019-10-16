package com.freewind.meetingdemo.PB;

import android.os.SystemClock;

import com.google.protobuf.InvalidProtocolBufferException;
import com.ook.android.IVCSCB;
import com.ook.android.VCS_EVENT_TYPE;
import com.ook.android.VcsApi;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 会议房间对象
 */
public class RoomClient implements IVCSCB {

    private VcsApi api;

    private String streamAddr;      //流媒体服务地址
    private int streamPort;         //流媒体服务端口

    private String meetingAddr;     //会控服务地址
    private int meetingPort;        //会控服务端口
    private InetAddress inetMeetingAddress;

    private String sessionId;       //进入房间的令牌
    private Models.Room room;       //目标房间信息
    private Models.Account account; //我的帐号信息

    private int roomHandle;     //房间句柄
    private RoomEvent roomEvent;    //事件

    UdpSocket udpSocket;
    Thread receiveWorker;
    Thread heartbeatWorker;

    boolean active;
    boolean entered;

    public RoomClient(VcsApi api){
        this.api=api;
        roomHandle=-1;
        active=false;
    }

    public void setRoomEvent(RoomEvent roomEvent){
        this.roomEvent=roomEvent;
    }

    public String getStreamAddr() {
        return streamAddr;
    }

    public void setStreamAddr(String streamAddr) {
        this.streamAddr = streamAddr;
    }

    public int getStreamPort() {
        return streamPort;
    }

    public void setStreamPort(int streamPort) {
        this.streamPort = streamPort;
    }

    public String getMeetingAddr() {
        return meetingAddr;
    }

    public void setMeetingAddr(String meetingAddr)  {
        this.meetingAddr = meetingAddr;
        try {
            this.inetMeetingAddress=InetAddress.getByName(meetingAddr);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public int getMeetingPort() {
        return meetingPort;
    }

    public void setMeetingPort(int meetingPort) {
        this.meetingPort = meetingPort;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public synchronized Models.Room getRoom() {
        return room;
    }

    public synchronized void setRoom(Models.Room room) {
        this.room = room;
    }

    public synchronized Models.Account getAccount() {
        return account;
    }

    public synchronized void setAccount(Models.Account account) {
        this.account = account;
    }

    /**
     * 打开房间
     * @throws VcsException
     */
    public void open() throws VcsException, IOException {
        try {

            active=true;

            roomHandle = api.VCS_CreateRoom(streamAddr, streamPort, room.getSdkNo(), sessionId);
            if (roomHandle < 0)
                throw new VcsException("创建流媒体句柄失败");

            api.VCS_SetRoomEvent(roomHandle, this, null);
            api.VCS_SetPicDataEvent(roomHandle, this, null);

            if (api.VCS_JoniRoom(roomHandle, account.getStreamId()) < 0)
                throw new VcsException("连接流媒体失败");


            udpSocket=new UdpSocket();
            udpSocket.open();

            this.entryRoom();
            receiveWorker=new Thread(receiveRunner);
            receiveWorker.start();

            heartbeatWorker=new Thread(heartbeatRunner);
            heartbeatWorker.start();
        }
        catch (Exception ex){
            this.close();
            throw ex;
        }
    }

    /**
     * 关闭房间
     */
    public void close(){
        active=false;
        if(roomHandle>=0){
            if (api != null) {
                api.VCS_CloseCamera();
                api.VCS_closeTrace();
                api.VCS_ExitRoom(roomHandle);
                api.VCS_Cleanup();
                api = null;
            }
            roomHandle=-1;
        }
        if(udpSocket!=null){

            udpSocket.close();
            udpSocket=null;
        }

        if(heartbeatWorker!=null){
            try {
                heartbeatWorker.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            heartbeatWorker=null;
        }
        if(receiveWorker!=null){
            try {
                receiveWorker.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            receiveWorker=null;
        }
    }

    /**
     * 自己进入房间
     * @throws IOException
     */
    private void entryRoom() throws IOException {
        RoomServer.EnterRoomRequest.Builder b= RoomServer.EnterRoomRequest.newBuilder();
        b.setAccount(this.getAccount());
        b.setRoom(this.getRoom());
        b.setToken(this.getSessionId());
        Packet pkt=new Packet(Models.Command.CMD_Room_Enter,b.build().toByteArray());
        if (udpSocket != null)
            udpSocket.send(pkt,inetMeetingAddress,meetingPort);
    }

    /**
     * 自己退出房间
     * @throws IOException
     */
    private void exitRoom() throws IOException {
        Models.Account acc=this.getAccount();
        Models.Room room=this.getRoom();

        RoomServer.ExitRoomRequest.Builder b= RoomServer.ExitRoomRequest.newBuilder();
        b.setAccountId(acc.getId());
        b.setRoomId(room.getId());
        b.setToken(this.getSessionId());

        Packet pkt=new Packet(Models.Command.CMD_Room_Exit,b.build().toByteArray());
        udpSocket.send(pkt,inetMeetingAddress,meetingPort);
    }

    /**
     * 心跳
     * @throws IOException
     */
    private void heartbeat() throws IOException {
        Models.Account acc=this.getAccount();
        Models.Room room=this.getRoom();

        RoomServer.HeartbeatRequest.Builder b= RoomServer.HeartbeatRequest.newBuilder();
        b.setAccount(acc);
        b.setRoomId(room.getId());
        b.setToken(this.getSessionId());

        Packet pkt=new Packet(Models.Command.CMD_Room_Heartbeat,b.build().toByteArray());
        udpSocket.send(pkt,inetMeetingAddress,meetingPort);
    }

    /**
     * 主持人会控
     * @param accId  目标帐号ID
     * @param videoState 视频状态，null表示不操作
     * @param audioState 音频状态，null表示不操作
     * @throws IOException
     */
    private void hostCtrl(String accId, Models.DeviceState videoState, Models.DeviceState audioState) throws IOException {
        Models.Account acc=this.getAccount();
        Models.Room room=this.getRoom();

        RoomServer.HostCtrlRequest.Builder b= RoomServer.HostCtrlRequest.newBuilder();
        b.setAccountId(acc.getId());
        b.setToken(this.getSessionId());
        b.setRoomId(room.getId());
        b.setTargetId(accId);
        if(videoState!=null){
            b.setVideoState(videoState);
        }
        if(audioState!=null){
            b.setAudioState(audioState);
        }
        b.setToken(this.getSessionId());

        Packet pkt=new Packet(Models.Command.CMD_Room_HostCtrl,b.build().toByteArray());
        udpSocket.send(pkt,inetMeetingAddress,meetingPort);
    }

    /**
     * 主持人踢人
     * @param accId 目标帐号ID
     * @throws IOException
     */
    private void hostKickout(String accId) throws IOException {
        Models.Account acc=this.getAccount();
        Models.Room room=this.getRoom();

        RoomServer.HostKickoutRequest.Builder b= RoomServer.HostKickoutRequest.newBuilder();
        b.setAccountId(acc.getId());
        b.setToken(this.getSessionId());
        b.setRoomId(room.getId());
        b.setTargetId(accId);

        Packet pkt=new Packet(Models.Command.CMD_Room_HostKickout,b.build().toByteArray());
        udpSocket.send(pkt,inetMeetingAddress,meetingPort);
    }

    /**
     * 主持人操作电子白板
     * @param open 是否打开
     * @throws IOException
     */
    private void hostSetWhiteboard(boolean open) throws IOException {
        Models.Account acc=this.getAccount();
        Models.Room room=this.getRoom();

        RoomServer.HostWhiteBoardRequest.Builder b= RoomServer.HostWhiteBoardRequest.newBuilder();
        b.setAccountId(acc.getId());
        b.setToken(this.getSessionId());
        b.setRoomId(room.getId());
        b.setIsOpen(open);
        Packet pkt=new Packet(Models.Command.CMD_Room_HostWhiteBoard,b.build().toByteArray());
        udpSocket.send(pkt,inetMeetingAddress,meetingPort);
    }

    @Override
    public void vcs_Cbf_RoomeVent(int i, int i1, int i2, String s) {
        if(i== VCS_EVENT_TYPE.VCS_XBITRATE){
            roomEvent.onXBitrate(i2);
        }else if(i== VCS_EVENT_TYPE.VCS_STATIST_SEND){
            roomEvent.onSendInfo(0,0);
        }else if(i== VCS_EVENT_TYPE.VCS_STATIST_RECV){
            roomEvent.onRecvInfo(s);
        }
    }

    @Override
    public void vcs_Pic_CallbackFrame(byte[] bytes, byte[] bytes1, byte[] bytes2, int i, int i1, int i2, int i3) {
        roomEvent.onFrame(bytes,bytes1,bytes2,i,i1,i2,i3);
    }

    /**
     * 是否发送自己视频
     * @param enabled
     */
    public void enableSendVideo(boolean enabled){
        api.VCS_EnableSendVideo(roomHandle,account.getStreamId(),enabled?1:0);
    }

    /**
     * 是否发送自己音频
     * @param enabled
     */
    public void enableSendAudio(boolean enabled){
        api.VCS_EnableSendAudio(roomHandle,account.getStreamId(),enabled?1:0);
    }

    /**
     * 是否接收对方视频
     * @param otherId
     * @param enabled
     */
    public void enableRecvVideo(int otherId,boolean enabled){
        api.VCS_EnableRecvVideo(roomHandle,otherId,enabled?1:0);
    }

    /**
     * 是否接收对方音频
     * @param otherId
     * @param enabled
     */
    public void enableRecvAudio(int otherId,boolean enabled){
        api.VCS_EnableRecvAudio(roomHandle,otherId,enabled?1:0);
    }

    /**
     * 设置网络抗抖动等级
     * @param plc
     * 超短（0）单向延迟 120 ms 左右，这种模式下没有丢包补偿机制，并且编码关闭了 B 帧，一般不建议实际使用；
     * 短  （1）单向延迟 200 ms 左右，单次丢包补偿，B 帧为 1，双向对讲环境下可以使用；
     * 中  （2）单向延迟 350 ms 左右，两次丢包补偿，B 帧为 1，双向对讲环境下推荐使用；
     * 长  （3）单向延迟 600 ms 左右，三次丢包补偿，B 帧为 3，这种模式仅用于单向收看，双向对讲环境下不建议使用，该参数无法动态设置
     */
    public void setPlc(int plc){
        api.VCS_SetRoomPlc(roomHandle,plc);
    }

    /**
     * 设置码率自适应,网络发生波动后会回调vcs_Cbf_RoomeVent事件
     * @param second 0-关闭自适应；开启[>=3] 建议5秒 在移动网络情况下 建议>=5
     */
    public void setXBitrate(int second){
        api.vcs_SetRoomXBitrate(roomHandle,second);
    }

    /**
     * 设置摄像机帧率,25
     * @param fps
     */
    public void setFps(int fps){
        api.setCameraFPS(fps);
    }

    /**
     * 是否启用自适应延迟
     * @param enbaled
     */
    public void enableXDelay(boolean enbaled){
        api.VCS_SetRoomXdelay(enbaled);
    }

    /**
     * 设置音频采样率
     * @param sampleRate 22050,44100,48000
     */
    public void setAudioSampleRate(int sampleRate){
        api.VCS_setAudioSamplerate(sampleRate);
    }

    /**
     * 设置音频声道数
     * @param channels  1-单声道；2-双声道
     */
    public void setAudioChannels(int channels){
        api.VCS_setAudioChannels(channels);
    }

    /**
     * 设置输出视频参数
     * @param width
     * @param height
     * @param frameRate
     * @param bitRate
     * @param keyFrame
     */
    public void setVideoOutput(int width, int height, int frameRate, int bitRate, int keyFrame){
        api.VCS_CreateVideoOutput(width,height,frameRate,bitRate,keyFrame);
    }

    /**
     * 设置输出音频参数
     * @param i
     */
    public void setAudioOutput(int i){
        api.VCS_CreateAudioOutput(i);
    }

    /**
     * 打开摄像头
     * @param view 绘制窗口
     */
    public void openCamera(Object view){
        api.VCS_OpenCamera(view);
    }

    private Runnable heartbeatRunner=new Runnable() {
        @Override
        public void run() {
            int sec=0;
            while (active){
                if(sec>=10) {
                    try {
                        if (entered) {
                            heartbeat();
                        } else {
                            entryRoom();
                        }
                        api.VCS_GetUploadStatus();
                        api.VCS_GetRecvStatus();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    sec=0;
                }
                SystemClock.sleep(1000);
                sec++;
            }
        }
    };

    private Runnable receiveRunner=new Runnable() {
        @Override
        public void run() {
            while (active){
                try {
                    DatagramPacket dp=udpSocket.receive();
                    if(dp.getLength()<=0){
                        continue;
                    }

                    byte[] data=dp.getData();
                    Packet packet=Packet.parse(data,0,data.length);
                    if(packet==null){
                        continue;
                    }

                    onPacket(packet,dp.getAddress(),dp.getPort());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private void onPacket(Packet packet,InetAddress address,int port) throws InvalidProtocolBufferException {
        Models.Command cmd=packet.getCommand();
        if(cmd== Models.Command.CMD_Room_Enter){
            roomEvent.onEnter(packet.getResult().getNumber());
        }else if(cmd== Models.Command.CMD_Room_Exit){
            roomEvent.onExit(packet.getResult().getNumber());
        }else if(cmd== Models.Command.CMD_Room_Heartbeat){
            onHeartbeat(packet,address,port);
        }else if(cmd== Models.Command.CMD_Room_NotifyAccount){
            onNotifyAccount(packet,address,port);
        }else if(cmd== Models.Command.CMD_Room_NotifyRoom){
            onNotifyRoom(packet,address,port);
        }else if(cmd== Models.Command.CMD_Room_NotifyBegin){
            onNotifyBegin(packet,address,port);
        }else if(cmd== Models.Command.CMD_Room_NotifyEnded){
            onNotifyEnd(packet,address,port);
        }else if(cmd== Models.Command.CMD_Room_NotifyEnter){
            onNotifyEnter(packet,address,port);
        }else if(cmd== Models.Command.CMD_Room_NotifyExit){
            onNotifyExit(packet,address,port);
        }else if(cmd== Models.Command.CMD_Room_NotifyKickout){
            onNotifyKickout(packet,address,port);
        }else if(cmd== Models.Command.CMD_Room_NotifyMyAccount){
            onNotifyMyAccount(packet,address,port);
        }
    }

    private void onHeartbeat(Packet packet,InetAddress address,int port){

        if(packet.getResult()== Models.Result.RESULT_OK){
            entered=true;
        }else{
            entered=false;
        }
    }

    private void onNotifyAccount(Packet packet,InetAddress address,int port) throws InvalidProtocolBufferException {
        RoomServer.AccountNotify pb= RoomServer.AccountNotify.parseFrom(packet.getData());
//        if (pb.getAccount().getId().equals(this.account.getId())){
//            this.setAccount(pb.getAccount());//更新本地的信息
//        }
        roomEvent.onNotifyAccount(pb.getAccount());
    }
    private void onNotifyMyAccount(Packet packet,InetAddress address,int port) throws InvalidProtocolBufferException {
        RoomServer.MyAccountNotify pb= RoomServer.MyAccountNotify.parseFrom(packet.getData());
//        if (pb.getAccount().getId().equals(this.account.getId())){
        Models.Account.Builder account = this.getAccount().toBuilder();
        if (pb.hasVideoState()){
            account.setVideoState(pb.getVideoState());
        }
        if (pb.hasAudioState()){
            account.setAudioState(pb.getAudioState());
        }
            this.setAccount(account.build());//更新本地的信息
//        }
        roomEvent.onNotifyMyAccount(pb);
    }
    private void onNotifyRoom(Packet packet,InetAddress address,int port) throws InvalidProtocolBufferException {
        RoomServer.RoomNotify pb= RoomServer.RoomNotify.parseFrom(packet.getData());
        this.setRoom(pb.getRoom());//更新本地的信息
        roomEvent.onNotifyRoom(pb.getRoom());
    }
    private void onNotifyBegin(Packet packet,InetAddress address,int port) throws InvalidProtocolBufferException {
        RoomServer.RoomBeginNotify pb= RoomServer.RoomBeginNotify.parseFrom(packet.getData());
        roomEvent.onNotifyBegin(pb.getRoomId());
    }
    private void onNotifyEnd(Packet packet,InetAddress address,int port) throws InvalidProtocolBufferException {
        RoomServer.RoomEndedNotify pb= RoomServer.RoomEndedNotify.parseFrom(packet.getData());
        roomEvent.onNotifyEnd(pb.getRoomId());
    }
    private void onNotifyEnter(Packet packet,InetAddress address,int port) throws InvalidProtocolBufferException {
        RoomServer.EnterNotify pb= RoomServer.EnterNotify.parseFrom(packet.getData());
        roomEvent.onNotifyEnter(pb.getAccount());
    }
    private void onNotifyExit(Packet packet,InetAddress address,int port) throws InvalidProtocolBufferException {
        RoomServer.ExitNotify pb= RoomServer.ExitNotify.parseFrom(packet.getData());
        roomEvent.onNotifyExit(pb.getAccount());
    }
    private void onNotifyKickout(Packet packet,InetAddress address,int port) throws InvalidProtocolBufferException {
        RoomServer.KickoutNotify pb= RoomServer.KickoutNotify.parseFrom(packet.getData());
        roomEvent.onNotifyKickout();
    }
}
