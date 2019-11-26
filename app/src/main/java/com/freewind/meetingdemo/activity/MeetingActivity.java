package com.freewind.meetingdemo.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.freewind.meetingdemo.MyApplication;
import com.freewind.meetingdemo.R;
import com.freewind.meetingdemo.adapter.WindowAdapter;
import com.freewind.meetingdemo.bean.MeetingBean;
import com.freewind.meetingdemo.bean.MemberBean;
import com.freewind.meetingdemo.common.UserConfig;
import com.freewind.meetingdemo.util.DisplayUtil;
import com.freewind.meetingdemo.util.TextureVideoViewOutlineProvider;
import com.freewind.vcs.Models;
import com.freewind.vcs.RoomClient;
import com.freewind.vcs.RoomEvent;
import com.freewind.vcs.RoomServer;
import com.freewind.vcs.StreamTrack;
import com.ook.android.CheckPermissionsUtil;
import com.ook.android.GLCameraView;
import com.ook.android.VCS_EVENT_TYPE;

import java.util.List;

/**
 * author superK
 * update_at 2019/7/30
 * description
 */
public class MeetingActivity extends AppCompatActivity implements View.OnClickListener, RoomEvent {

    RoomClient roomClient;
    private GLCameraView cameraSurfaceView;

    Button lightBtn;
    Button switchBtn;
    Button closeSelfVideoBtn;
    Button closeSelfAudioBtn;
    TextView customMsgTv;
    RecyclerView windowRcView;
    TextView clientTv, losTv, uploadTv;
    Button notRecvAudio, notRecvVideo, changeOrientationBtn;

    private int videoW = 1920;
    private int videoH = 1080;
    private final int fps = 25;
    private final int bitRate = 1024;

    private boolean isLight = false;//是否打开闪光灯
    private boolean isFront = true;//是否前置
    private boolean isSendSelfVideo = true;//是否发送自己的视频
    private boolean isSendSelfAudio = true;//是否发送自己的音频

    private String trackServer = "";
    private boolean openDebug = false;
    private int roomSdkNo;
    private int mySdkNo;
    private static String TAG = "4444444444";

    public static final String DEBUG_ADDR = "debug_addr";
    public static final String DEBUG_SWITCH = "debug_switch";
    public static final String AGC = "agc";
    public static final String AEC = "aec";
    public static final String CLOSE_SELF_VIDEO = "close_self_video";
    public static final String CLOSE_SELF_AUDIO = "close_self_audio";
    public static final String CLOSE_OTHER_AUDIO = "close_other_audio";
    public static final String CLOSE_OTHER_VIDEO = "close_other_video";
    public static final String SAMPLE_RATE = "sample_rate";
    public static final String HARD_DECODER = "hard_decoder";
    public static final String AUTO_BITRATE = "auto_bitrate";
    public static final String ROOM_INFO = "room_info";

    private MeetingBean meetingBean;

    private boolean closeOtherAudio;//关闭他人音频模式
    private boolean closeOtherVideo;//关闭他人视频模式

    private int agc = 10000;
    private int aec = 12;
    private int sampleRate = 48000;

    private boolean hardDecoder = false;
    private boolean isRecvAudio = true, isRecvVideo = true;//默认接收

    private WindowAdapter windowAdapter;

    @Override
    public void onEnter(int result) {
        Log.e(TAG, "你进入了会议室 result:" + result);
    }

    @Override
    public void onExit(int result) {
        Log.e(TAG, "你离开了会议室 result:" + result);
    }

    @Override
    public void onNotifyRoom(Models.Room room) {
        Log.e(TAG, "onNotifyRoom"
                + "  id:" + room.getId() + "  sdkNo:" + room.getSdkNo()
                + "  whiteBoard:" + room.getWhiteBoard() + "  state:" + room.getState()
                + "  type:" +room.getType());
    }

    @Override
    public void onNotifyKickout() {
        Log.e(TAG, "onNotifyKickout   你被踢出了会议室");
        finish();
    }

    @Override
    public void onNotifyEnter(Models.Account account) {

        final int sdkNo = account.getStreamId();
        Log.e(TAG, "onNotifyEnter: 有人进入房间" + "  sdkno: " + sdkNo);

//        roomClient.getApi().VCS_set_track(1, sdkNo);

        final MemberBean memberBean = new MemberBean();
        memberBean.setClientId(sdkNo + "");
        memberBean.setAccountId(account.getId());
        memberBean.setCloseVideo(account.getVideoState());
        memberBean.setMute(account.getAudioState());

        if (!isRecvAudio){
            memberBean.setCloseOtherAudio(true);
        }else {
            memberBean.setCloseOtherAudio(closeOtherAudio);
        }
        if (!isRecvVideo){
            memberBean.setCloseOtherVideo(true);
        }else {
            memberBean.setCloseOtherVideo(closeOtherVideo);
        }

        if (closeOtherVideo){
            roomClient.enableRecvVideo(sdkNo, false);
        }
        if (closeOtherAudio){
            roomClient.enableRecvAudio(sdkNo, false);
        }

        if (windowAdapter.getMemberList().isEmpty()) {
            windowAdapter.addItem(memberBean);
        } else {
            int count = 0;
            for (MemberBean s : windowAdapter.getMemberList()) {
                if (s.getClientId().equals("" + sdkNo)) {
                    count++;
                    break;
                }
            }
            if (count == 0) {
                windowAdapter.addItem(memberBean);
            }
        }
    }

    @Override
    public void onNotifyExit(Models.Account account) {
        final int sdkNo = account.getStreamId();
        Log.e(TAG, "onNotifyExit: 有人离开房间" + "  roomSdkNo: " + sdkNo);

        if (!windowAdapter.getMemberList().isEmpty()) {
            for (MemberBean s : windowAdapter.getMemberList()) {
                if (s.getClientId().equals(sdkNo + "")) {
                    windowAdapter.removeItem(sdkNo + "");
                    break;
                }
            }
        }
    }

    @Override
    public void onNotifyBegin(String roomId) {
        Log.e(TAG, "onNotifyBegin");
    }

    @Override
    public void onNotifyEnd(String roomId) {
        Log.e(TAG, "onNotifyEnd");
        showToast("主持人结束会议");
        finish();
    }

    @Override
    public void onFrame(byte[] ost, byte[] tnd, byte[] trd, int width, int height, int fourcc, int clientId) {
        //建议使用缓冲
        Log.e(TAG, "onFrame  " + "  clientId: " + clientId + "   " + width + " " + height);

        final WindowAdapter.MyViewHolder holder = windowAdapter.getHolders().get(clientId + "");
        if (holder != null && holder.meetingGLSurfaceView != null) {
            holder.meetingGLSurfaceView.update(width, height, fourcc);
            holder.meetingGLSurfaceView.update(ost, tnd, trd, fourcc);
        }
    }

    @Override
    public void onSendInfo(final String info) {
        //delay: 移动40-45， wifi 30  有线 20

        // 60000270::delay=17 status=1 speed=687 buffer=0 overflow=0 */
        // 60000270=id, delay=上传到服务器之间的延迟时间,越大越不好, status=-1上传出错 >=0正常, speed=发送速度 buffer=缓冲包0-4正常 */
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                uploadTv.setText(info);
            }
        });
    }

    @Override
    public void onRecvInfo(final String info) {
        // 60000002::recv=10144 comp=505 losf=91 */
        // 60000002=对方id, recv=接收包信息, comp=补偿 正常0, losf=丢失包信息 */
        //comp 高 网络不稳定
        //losf 高 就是网络差
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                losTv.setText(info);
            }
        });
    }

    @Override
    public void onXBitrate(int level) {
        switch (level) {
            case VCS_EVENT_TYPE.VCS_START_XBITRATE://自适应模式启动
                break;
            case VCS_EVENT_TYPE.VCS_BITRATE_RECOVERED://恢复码率
                break;
            case VCS_EVENT_TYPE.VCS_BITRATE_HALF_BITRATE://降为1/2
                break;
            case VCS_EVENT_TYPE.VCS_BITRATE_QUARTER_BITRATE://降为1/4
                break;
        }
    }

    @Override
    public void onNotifyAccount(Models.Account account) {

        Log.e(TAG, "onNotifyAccount"
                + "  id:" + account.getId() + "  streamId:" + account.getStreamId()
                + "  name:" + account.getName() + "  nickname:" + account.getNickname()
                + "  videoState:" + account.getVideoState() + "  audioState:" + account.getAudioState() + "  role:" + account.getRole());

        for (Models.Stream stream : account.getStreamsList()){
            Log.e("666666", "id："+stream.getId() + "    name:" + stream.getName() + "   type:" + stream.getType());
        }
        List<MemberBean> memberBeans = windowAdapter.getMemberList();
        int size = memberBeans.size();

        boolean isChange = false;

        for (int i =0; i< size; i++){
            MemberBean memberBean = memberBeans.get(i);
            if (memberBean.getAccountId().equals(account.getId())){
                if (memberBean.getCloseVideo() != account.getVideoState()){//状态发送改变
                    isChange = true;
                    memberBean.setCloseVideo(account.getVideoState());
                }
                if (memberBean.getMute() != account.getAudioState()){
                    isChange = true;
                    memberBean.setMute(account.getAudioState());
                }
                break;
            }
        }
        if (isChange){
            // TODO: 2019/10/29 用这个会黑屏一下，可以直接拿到控件去控制
            windowAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onNotifyMyAccount(RoomServer.MyAccountNotify notify) {
        //sdk已经把数据同步到RoomClient的account里面了，
        Models.Account account = notify.getAccount();
        Log.e(TAG, "onNotifyMyAccount"
                + "  id:" + account.getId() + "  streamId:" + account.getStreamId()
                + "  name:" + account.getName() + "  nickname:" + account.getNickname()
                + "  videoState:" + account.getVideoState() + "  audioState:" + account.getAudioState() + "  role:" + account.getRole());
        if (isSendSelfVideo != (account.getVideoState() == Models.DeviceState.DS_Active)){//主持人会控操作后，状态发生改变
            closeSelfVideo(true);
        }
        if (isSendSelfAudio != (account.getAudioState() == Models.DeviceState.DS_Active)){
            closeSelfAudio(true);
        }
    }

    //流变化通知，所有人收得到，演示流可以在这里做
    @Override
    public void onNotifyStreamChanged(RoomServer.StreamNotify streamNotify) {
        Log.e(TAG, "onNotifyStreamChanged" + " " + streamNotify.getOperation());
        streamNotify.getAccountId(); //判断哪个用户

        Models.Operation operation = streamNotify.getOperation();
        if (operation == Models.Operation.Operation_Add){//新增

        }else if (operation == Models.Operation.Operation_Remove){//删除

        }else if (operation == Models.Operation.Operation_Update){//修改

        }

        switch (streamNotify.getOperation().getNumber()){
            case Models.Operation.Operation_Remove_VALUE://流关闭
                break;
            case Models.Operation.Operation_Add_VALUE://流新增
                break;
            case Models.Operation.Operation_Update_VALUE://改变
                break;
        }
    }

    //透传消息
    @Override
    public void onNotifyPassThough(RoomServer.PassthroughNotify passthroughNotify) {

    }

    //主持人会控,只有被控制的人会收到回调
    @Override
    public void onNotifyHostCtrlStream(RoomServer.HostCtrlStreamNotify hostCtrlStreamNotify) {
        Models.Operation operation = hostCtrlStreamNotify.getOperation();
        if (operation == Models.Operation.Operation_Add){

        }else if (operation == Models.Operation.Operation_Remove){

        }else if (operation == Models.Operation.Operation_Update){

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        setContentView(R.layout.activity_meeting);
        CheckPermissionsUtil checkPermissionsUtil = new CheckPermissionsUtil(this);
        checkPermissionsUtil.requestAllPermission(this);

        initView();
        initVcsApi();
        Log.e("11111111111", roomClient.getApi().vcs_getVersion());
    }

    private void initView() {
        lightBtn = findViewById(R.id.camera_light_btn);
        switchBtn = findViewById(R.id.camera_switch_btn);
        closeSelfVideoBtn = findViewById(R.id.close_self_video_btn);
        closeSelfAudioBtn = findViewById(R.id.close_self_audio_btn);
        cameraSurfaceView = findViewById(R.id.cameraSurfaceView);
        customMsgTv = findViewById(R.id.custom_msg_tv);
        windowRcView = findViewById(R.id.window_rcview);
        clientTv = findViewById(R.id.client_tv);
        uploadTv = findViewById(R.id.upload_tv);
        losTv = findViewById(R.id.los_tv);
        notRecvAudio = findViewById(R.id.not_recv_audio_btn);
        notRecvVideo = findViewById(R.id.not_recv_video_btn);
        changeOrientationBtn = findViewById(R.id.change_orientation_btn);

        customMsgTv.setMovementMethod(ScrollingMovementMethod.getInstance());

        lightBtn.setOnClickListener(this);
        switchBtn.setOnClickListener(this);
        closeSelfVideoBtn.setOnClickListener(this);
        closeSelfAudioBtn.setOnClickListener(this);
        notRecvAudio.setOnClickListener(this);
        notRecvVideo.setOnClickListener(this);
        changeOrientationBtn.setOnClickListener(this);

        Intent intent = getIntent();
        meetingBean = (MeetingBean) intent.getSerializableExtra(ROOM_INFO);
//        meetingBean.setStream_host("103.219.32.162");
//        meetingBean.setStream_port(8006);
        roomSdkNo = Integer.parseInt(meetingBean.getSdk_no());
        trackServer = intent.getStringExtra(DEBUG_ADDR);
        openDebug = intent.getBooleanExtra(DEBUG_SWITCH, false);
        agc = Integer.valueOf(intent.getStringExtra(AGC));
        aec = Integer.valueOf(intent.getStringExtra(AEC));
        closeOtherAudio = intent.getBooleanExtra(CLOSE_OTHER_AUDIO, false);
        closeOtherVideo = intent.getBooleanExtra(CLOSE_OTHER_VIDEO, false);
        isSendSelfVideo = !intent.getBooleanExtra(CLOSE_SELF_VIDEO, true);
        isSendSelfAudio = !intent.getBooleanExtra(CLOSE_SELF_AUDIO, true);
        
        sampleRate = intent.getIntExtra(SAMPLE_RATE, 22050);

        hardDecoder = intent.getBooleanExtra(HARD_DECODER, false);

        windowAdapter = new WindowAdapter(MeetingActivity.this);
        windowRcView.setAdapter(windowAdapter);
        windowRcView.getRecycledViewPool().setMaxRecycledViews(0, 0);

        cameraSurfaceView.setOutlineProvider(new TextureVideoViewOutlineProvider(DisplayUtil.getInstance().dip2px(4)));
        cameraSurfaceView.setClipToOutline(true);

        mySdkNo = Integer.valueOf(UserConfig.getUserInfo().getData().getAccount().getRoom().getSdk_no());
        clientTv.setText(mySdkNo + "");
    }

    //初始化API
    private void initVcsApi() {
        //开启track 调试
        if (openDebug) {
            roomClient = new RoomClient(this, roomSdkNo, trackServer);
        }else {
            roomClient = new RoomClient(this, roomSdkNo);
        }

        roomClient.setRoomEvent(this);//设置会议回调

        roomClient.setAccount(
                UserConfig.getUserInfo().getData().getAccount().getId(),
                UserConfig.getUserInfo().getData().getAccount().getRoom().getSdk_no(),
                UserConfig.getUserInfo().getData().getAccount().getName(),
                UserConfig.getUserInfo().getData().getAccount().getNickname());
        roomClient.setRoom(meetingBean.getRoom().getId(), roomSdkNo + "");

        roomClient.setSessionId(meetingBean.getSession());
        roomClient.setStreamAddr(meetingBean.getStream_host());
        roomClient.setStreamPort(meetingBean.getStream_port());
        roomClient.setMeetingAddr(meetingBean.getMeeting_host());
        roomClient.setMeetingPort(meetingBean.getMeeting_port());

        roomClient.setVideoOutput(videoW, videoH, fps, bitRate);//设置视频分辨率宽高，帧率，码率
        roomClient.setAgcAec(agc, aec);//设置AGC,AEC
        roomClient.setFps(fps);//设置帧率
        roomClient.openCamera(cameraSurfaceView);//设置预览view
        roomClient.enableXDelay(true);//自适应延迟
        roomClient.useHwDecoder(hardDecoder);//是否硬解码
        roomClient.setAudioSampleRate(sampleRate);//设置采样率

        if (isSendSelfAudio) {
            closeSelfAudioBtn.setText("关闭自己的音频");
        } else {
            roomClient.setDefaultSendSelfAudio(false);
//            roomClient.enableSendAudio(false);
            closeSelfAudioBtn.setText("打开自己的音频");
        }

        if (isSendSelfVideo) {
            closeSelfVideoBtn.setText("关闭自己的视频");
        } else {
            roomClient.setDefaultSendSelfVideo(false);
//            roomClient.enableSendVideo(false);
            closeSelfVideoBtn.setText("打开自己的视频");
        }
        roomClient.setCenterInside(true);
        roomClient.open();
    }

    @Override
    protected void onDestroy() {
        roomClient.close();//退出释放
        super.onDestroy();
    }

    //闪光灯开关
    private void switchLight() {
        if (isFront) {
            showToast("后置摄像头才能开启闪光灯");
            return;
        }
        isLight = !isLight;
        roomClient.camera_Light(isLight);
        if (isLight) {
            lightBtn.setText("关闪光灯");
        } else {
            lightBtn.setText("开闪光灯");
        }
    }

    //切换摄像头
    private void switchCamera() {
        roomClient.switchCamera(isFront);
        isFront = !isFront;
        if (isFront) {
            switchBtn.setText("后置");
            lightBtn.setText("开闪光灯");
        } else {
            switchBtn.setText("前置");
        }
    }

    //设置是否接收某人的视频
    public void closeOtherVideo(String clientId, boolean isClose){
        roomClient.enableRecvVideo(Integer.valueOf(clientId), !isClose);
    }

    //设置是否接收某人音频
    public void muteOtherAudio(String clientId, boolean isMute){
        roomClient.enableRecvAudio(Integer.valueOf(clientId), !isMute);
    }

    //主持人踢人
    public void kickOut(String id){
        roomClient.hostKickout(id);
    }

    //主持人禁言
    public void hostMute(String acc, boolean mute){
        roomClient.hostCtrl(acc, null, mute ? Models.DeviceState.DS_Disabled: Models.DeviceState.DS_Active);
    }

    public void useChannel(int sdkNo, StreamTrack streamTrack){
        roomClient.setStreamTrack(sdkNo, streamTrack);
    }

    //主持人关闭视频
    public void hostCloseVideo(String acc, boolean isClose){
        roomClient.hostCtrl(acc, isClose ? Models.DeviceState.DS_Disabled: Models.DeviceState.DS_Active, null);
    }

    //设置自己的视频，是否发送
    private void closeSelfVideo(boolean isFromNotify) {
        Models.Account account = roomClient.getAccount();
        if (!isFromNotify && account.getVideoState() == Models.DeviceState.DS_Disabled){
            showToast("当前被主持人关闭，不可操作");
            return;
        }
        if (isFromNotify){
            roomClient.enableSendVideo(account.getVideoState());
            isSendSelfVideo = account.getVideoState() != Models.DeviceState.DS_Disabled;
        }else {
            isSendSelfVideo = account.getVideoState() != Models.DeviceState.DS_Active;
            if (isSendSelfVideo){
                roomClient.enableSendVideo(Models.DeviceState.DS_Active);
            }else {
                roomClient.enableSendVideo(Models.DeviceState.DS_Closed);
            }
        }
        if (isSendSelfVideo) {
            closeSelfVideoBtn.setText("关闭自己的视频");
        } else {
            closeSelfVideoBtn.setText("打开自己的视频");
        }
    }

    //设置自己的音频，是否发送
    private void closeSelfAudio(boolean isFromNotify) {
        Models.Account account = roomClient.getAccount();
        if (!isFromNotify && account.getAudioState() == Models.DeviceState.DS_Disabled){
            showToast("当前被主持人禁言，不可操作");
            return;
        }
        if (isFromNotify){
            roomClient.enableSendAudio(account.getAudioState());
            isSendSelfAudio = account.getAudioState() != Models.DeviceState.DS_Disabled;
        }else {
            isSendSelfAudio = account.getAudioState() != Models.DeviceState.DS_Active;
            if (isSendSelfAudio){
                roomClient.enableSendAudio(Models.DeviceState.DS_Active);
            }else {
                roomClient.enableSendAudio(Models.DeviceState.DS_Closed);
            }
        }
        if (isSendSelfAudio) {
            closeSelfAudioBtn.setText("关闭自己的音频");
        } else {
            closeSelfAudioBtn.setText("打开自己的音频");
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.camera_light_btn://闪光灯开关
                switchLight();
//                flag = !flag;
//                roomClient.hostSetWhiteboard(flag);
                break;
            case R.id.camera_switch_btn://切换摄像头
                switchCamera();
                break;
            case R.id.change_orientation_btn:
                if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {//当前是横屏
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//竖屏
                    roomClient.setCenterInside(true);
                }else {//当前是竖屏
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//横屏
                    roomClient.setCenterInside(false);
                }
                break;
            case R.id.close_self_video_btn://设置自己的视频，是否发送
                closeSelfVideo(false);
                break;
            case R.id.close_self_audio_btn://设置自己的音频，是否发送
                closeSelfAudio(false);
                break;
            case R.id.not_recv_audio_btn://不接收所有人音频
                isRecvAudio = !isRecvAudio;
                roomClient.enableRecvAudio(0, isRecvAudio);
                if (isRecvAudio){
                    notRecvAudio.setText("不接收所有人音频");
                }else {
                    notRecvAudio.setText("接收所有人音频");
                }
                for (MemberBean memberBean : windowAdapter.getMemberList()){
                    memberBean.setCloseOtherAudio(!isRecvAudio);
                }
                windowAdapter.notifyDataSetChanged();
                break;
            case R.id.not_recv_video_btn://不接收所有人视频
                isRecvVideo = !isRecvVideo;
                roomClient.enableRecvVideo(0, isRecvVideo);
                if (isRecvVideo){
                    notRecvVideo.setText("不接收所有人视频");
                }else {
                    notRecvVideo.setText("接收所有人视频");
                }
                for (MemberBean memberBean : windowAdapter.getMemberList()){
                    memberBean.setCloseOtherVideo(!isRecvVideo);
                }
                windowAdapter.notifyDataSetChanged();
                break;
        }
    }

    private void showToast(Object obj) {
        Toast.makeText(MyApplication.getContext(), null == obj ? "Unknow Error" : obj.toString(), Toast.LENGTH_LONG).show();
    }
}