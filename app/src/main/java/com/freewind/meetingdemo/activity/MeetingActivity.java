package com.freewind.meetingdemo.activity;

import android.content.Intent;
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
import com.ook.android.CheckPermissionsUtil;
import com.ook.android.GLCameraView;
import com.ook.android.VCS_EVENT_TYPE;

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

    private int videoW = 1280;
    private int videoH = 720;
    private final int fps = 25;
    private final int bitRate = 800;

    private boolean isLight = false;//是否打开闪光灯
    private boolean isFront = true;//是否前置
    private boolean isSendSelfVideo = true;//是否发送自己的视频
    private boolean isCloseSelfAudio = false;//是否关闭发送自己的音频

    private String trackServer = "";
    private boolean openDebug = false;
    private int roomSdkNo;
    private int mySdkNo;
    private static String TAG = "kkp";

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

    private WindowAdapter windowAdapter;

    @Override
    public void onEnter(int result) {
        Log.e("444444444444", result + "  result");

    }

    @Override
    public void onExit(int result) {
        Log.e("444444444444", "result   " + result);

    }

    @Override
    public void onNotifyRoom(Models.Room room) {
        Log.e("444444444444", "onNotifyRoom"
                + "  id:" + room.getId() + "  sdkNo:" + room.getSdkNo()
                + "  whiteBoard:" + room.getWhiteBoard() + "  state:" + room.getState()
                + "  type:" +room.getType());

    }

    @Override
    public void onNotifyAccount(Models.Account account) {
        Log.e("444444444444", "onNotifyAccount"
                + "  id:" + account.getId() + "  streamId:" + account.getStreamId()
                + "  name:" + account.getName() + "  nickname:" + account.getNickname()
                + "  videoState:" + account.getVideoState() + "  audioState:" + account.getAudioState());

    }

    @Override
    public void onNotifyKickout() {
        Log.e("444444444444", "44");

    }

    @Override
    public void onNotifyEnter(Models.Account account) {
        final int sdkNo = account.getStreamId();
        Log.e(TAG, "event: 有人进入房间" + "  sdkno: " + sdkNo);
        final MemberBean memberBean = new MemberBean();
        memberBean.setClientId(sdkNo + "");

        if (closeOtherVideo){
            memberBean.setCloseVideo(true);
            roomClient.enableRecvVideo(sdkNo, false);
        }
        if (closeOtherAudio){
            memberBean.setMute(true);
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
        Log.e(TAG, "event: 有人离开房间" + "  roomSdkNo: " + sdkNo);

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
        Log.e("444444444444", "777");
    }

    @Override
    public void onNotifyEnd(String roomId) {
        Log.e("444444444444", "888");
    }

    @Override
    public void onFrame(byte[] ost, byte[] tnd, byte[] trd, int width, int height, int fourcc, int clientId) {
        //建议使用缓冲
        Log.e(TAG, "vcs_Pic_CallbackFrame " + "  clientId: " + clientId + "   " + width + " " + height);
        final WindowAdapter.MyViewHolder holder = windowAdapter.getHolders().get(clientId + "");
        if (holder != null && holder.meetingGLSurfaceView != null) {
            holder.meetingGLSurfaceView.update(width, height, fourcc);
            holder.meetingGLSurfaceView.update(ost, tnd, trd, fourcc);
        }
    }

    @Override
    public void onSendInfo(int speed, int delay) {
        // 60000270::delay=17 status=1 speed=687 buffer=0 overflow=0 */
        // 60000270=id, delay=上传到服务器之间的延迟时间,越大越不好, status=-1上传出错 >=0正常, speed=发送速度 buffer=缓冲包0-4正常 */
    }

    @Override
    public void onRecvInfo(String s) {
        // 60000002::recv=10144 comp=505 losf=91 */
        // 60000002=对方id, recv=接收包信息, comp=补偿 正常0, losf=丢失包信息 */
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
    public void onNotifyMyAccount(RoomServer.MyAccountNotify notify) {
        Log.e("444444444444", "1515");
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

        customMsgTv.setMovementMethod(ScrollingMovementMethod.getInstance());

        lightBtn.setOnClickListener(this);
        switchBtn.setOnClickListener(this);
        closeSelfVideoBtn.setOnClickListener(this);
        closeSelfAudioBtn.setOnClickListener(this);

        Intent intent = getIntent();
        meetingBean = (MeetingBean) intent.getSerializableExtra(ROOM_INFO);
        roomSdkNo = Integer.parseInt(meetingBean.getSdk_no());
        trackServer = intent.getStringExtra(DEBUG_ADDR);
        openDebug = intent.getBooleanExtra(DEBUG_SWITCH, false);
        agc = Integer.valueOf(intent.getStringExtra(AGC));
        aec = Integer.valueOf(intent.getStringExtra(AEC));
        closeOtherAudio = intent.getBooleanExtra(CLOSE_OTHER_AUDIO, false);
        closeOtherVideo = intent.getBooleanExtra(CLOSE_OTHER_VIDEO, false);
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

        roomClient.setRoomEvent(this);

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

        roomClient.setVideoOutput(videoW, videoH, fps, bitRate);
        roomClient.setAgcAec(agc, aec);
        roomClient.setFps(fps);
        roomClient.openCamera(cameraSurfaceView);
        roomClient.enableXDelay(true);//自适应延迟
        roomClient.useHwDecoder(hardDecoder);//是否硬解码
        roomClient.setAudioSampleRate(sampleRate);//设置采样率

        if (!isSendSelfVideo && isCloseSelfAudio){
            roomClient.sendPause(true);
        }
        roomClient.open();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //退出释放
        roomClient.close();
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

    public void muteOtherAudio(String clientId, boolean isMute){
        roomClient.enableRecvAudio(Integer.valueOf(clientId), !isMute);
    }

    //设置自己的视频，是否发送
    private void closeSelfVideo() {
        roomClient.enableSendVideo(!isSendSelfVideo);
        isSendSelfVideo = !isSendSelfVideo;
        if (isSendSelfVideo) {
            roomClient.sendPause(false);
            closeSelfVideoBtn.setText("关闭自己的视频");
        } else {
            closeSelfVideoBtn.setText("打开自己的视频");
        }
    }

    //设置自己的音频，是否发送
    private void closeSelfAudio() {
        roomClient.enableSendAudio(isCloseSelfAudio);
        isCloseSelfAudio = !isCloseSelfAudio;
        if (isCloseSelfAudio) {
            closeSelfAudioBtn.setText("打开自己的音频");
        } else {
            roomClient.sendPause(false);
            closeSelfAudioBtn.setText("关闭自己的音频");
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.camera_light_btn://闪光灯开关
                switchLight();
                break;
            case R.id.camera_switch_btn://切换摄像头
                switchCamera();
                break;
            case R.id.close_self_video_btn://设置自己的视频，是否发送
                closeSelfVideo();
                break;
            case R.id.close_self_audio_btn://设置自己的音频，是否发送
                closeSelfAudio();
                break;
        }
    }

    private void showToast(Object obj) {
        Toast.makeText(MyApplication.getContext(), null == obj ? "Unknow Error" : obj.toString(), Toast.LENGTH_LONG).show();
    }
}