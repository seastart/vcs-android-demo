package com.freewind.meetingdemo.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.freewind.meetingdemo.R;
import com.freewind.meetingdemo.adapter.WindowAdapter;
import com.freewind.meetingdemo.bean.MeetingBean;
import com.freewind.meetingdemo.bean.MemberBean;
import com.freewind.meetingdemo.bean.RoomInfoBean;
import com.freewind.meetingdemo.common.Constants;
import com.freewind.meetingdemo.common.UserConfig;
import com.freewind.meetingdemo.http.HttpCallBack;
import com.freewind.meetingdemo.util.DisplayUtil;
import com.freewind.meetingdemo.util.FloatingButtonService;
import com.freewind.meetingdemo.util.PermissionUtil;
import com.freewind.meetingdemo.util.Requester;
import com.freewind.meetingdemo.util.ToastUtil;
import com.freewind.vcs.CameraPreview;
import com.freewind.vcs.Models;
import com.freewind.vcs.RoomClient;
import com.freewind.vcs.RoomEvent;
import com.freewind.vcs.RoomServer;
import com.ook.android.VCS_EVENT_TYPE;
import com.ook.android.ikPlayer.VcsPlayerGlTextureView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * author superK
 * update_at 2019/7/30
 * description
 */
public class MeetingActivity extends PermissionActivity implements RoomEvent, CameraPreview {

    RoomClient roomClient;
    @BindView(R.id.cameraTextureView)
    VcsPlayerGlTextureView cameraTextureView;
    @BindView(R.id.close_preview_tv)
    TextView closePreviewTv;
    @BindView(R.id.window_rcview)
    RecyclerView windowRcView;
    @BindView(R.id.rec_btn)
    Button recordingBtn;
    @BindView(R.id.camera_switch_btn)
    Button cameraSwitchBtn;
    @BindView(R.id.camera_light_btn)
    Button cameraLightBtn;
    @BindView(R.id.close_self_video_btn)
    Button closeSelfVideoBtn;
    @BindView(R.id.close_self_audio_btn)
    Button closeSelfAudioBtn;
    @BindView(R.id.not_recv_audio_btn)
    Button notRecvAudioBtn;
    @BindView(R.id.not_recv_video_btn)
    Button notRecvVideoBtn;
    @BindView(R.id.upload_tv)
    TextView uploadTv;
    @BindView(R.id.los_tv)
    TextView losTv;
    @BindView(R.id.client_tv)
    TextView nameTv;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.webView)
    WebView webView;
    @BindView(R.id.boardCl)
    ConstraintLayout boardCl;

    private int videoH = 480;
    private int videoW = 848;
    private int fps = 20;//如果视频源来自摄像头，24FPS已经是肉眼极限，所以一般20帧的FPS就已经可以达到很好的用户体验了
    private int bitRate = 1024;

    private boolean isLight = false;//是否打开闪光灯
    private boolean isFront = true;//是否前置
    private boolean isSendSelfVideo = true;//是否发送自己的视频
    private boolean isSendSelfAudio = true;//是否发送自己的音频

    private String trackServer = "";
    private boolean openDebug = false;
    private boolean isMulti = false;
    private int roomSdkNo;
    private static String TAG = "4444444444";

    public static final String DEBUG_ADDR = "debug_addr";
    public static final String DEBUG_SWITCH = "debug_switch";
    public static final String MULTI = "multi";
    public static final String AGC = "agc";
    public static final String AEC = "aec";
    public static final String FPS = "fps";
    public static final String CLOSE_SELF_VIDEO = "close_self_video";
    public static final String CLOSE_SELF_AUDIO = "close_self_audio";
    public static final String CLOSE_OTHER_AUDIO = "close_other_audio";
    public static final String CLOSE_OTHER_VIDEO = "close_other_video";
    public static final String SAMPLE_RATE = "sample_rate";
    public static final String HARD_DECODER = "hard_decoder";
    public static final String ROOM_INFO = "room_info";
    public static final String VIDEO_LEVEL = "video_level";

    private MeetingBean meetingBean;

    private boolean closeOtherAudio;//关闭他人音频模式
    private boolean closeOtherVideo;//关闭他人视频模式

    private int agc = 10000;//自动增益
    private int aec = 12;//回音消除
    private int sampleRate = 48000;

    int level = 0;//0:720P  1:1080P

    private boolean hardDecoder = false;
    private boolean isRecvAudio = true, isRecvVideo = true;//默认接收

    private WindowAdapter windowAdapter;

    public int spanCount = 2;

    public MemberBean mainWindowMember;//保存在主窗口的成员的信息
    public MemberBean selfMember;

    @Override
    public void onEnter(int result) {
        //如果result != 0 则表示服务器上的token已经失效，需要进行重新进入房间的逻辑
        Log.e("2222222", result + "");
        if (result != 0) {
            ToastUtil.getInstance().showLongToast("正在进行重连");
            if (progressBar.getVisibility() == View.VISIBLE) {
                return;
            }
            if (meetingBean.getAccount() == null || meetingBean.getAccount().getRoom() == null) {
                return;
            }
            progressBar.setVisibility(View.VISIBLE);
            Requester.enterMeeting(meetingBean.getAccount().getRoom().getNo(), "", new HttpCallBack<RoomInfoBean>() {
                @Override
                public void onSucceed(RoomInfoBean data) {
                    if (data.getCode() == Constants.NEED_PWD) {
                        ToastUtil.getInstance().showLongToast("该会议室需要密码");
                        onBackPressed();
                    }
                }

                @Override
                protected void onComplete(boolean success) {
                    progressBar.setVisibility(View.GONE);
                    if (!success){
                        finish();
                        ToastUtil.getInstance().showLongToast("连接失败，请重新进入房间");
                    }
                }
            });
        }
    }

    @Override
    public void onExit(int result) {
        Log.e(TAG, "你离开了会议室 result:" + result);
    }

    public String shareAccId = "";

    @Override
    public void onNotifyRoom(Models.Room room) {
        Log.e(TAG, "onNotifyRoom"
                + "  id:" + room.getId() + "  sdkNo:" + room.getSdkNo()
                + "  sharingAccId:" + room.getSharingAccId()
                + "  sharingType:" + room.getSharingType()
                + "  whiteBoard:" + room.getWhiteBoard() + "  state:" + room.getState()
                + "  type:" + room.getType());

        shareAccId = room.getSharingAccId();

        if (room.getSharingType() == Models.SharingType.ST_WhiteBoard) {
            if (boardCl.getVisibility() != View.VISIBLE) {
                loadWebView();
            }
        } else {
            if (boardCl.getVisibility() != View.GONE) {
                hideWebView();
            }
        }
    }

    @Override
    public void onNotifyKickOut(String accountId) {
        Log.e(TAG, "onNotifyKickout   你被踢出了会议室");
        onBackPressed();
    }

    /**
     * Account类部分字段
     * String id;                          //id
     * int StreamId;                       //流媒体连接标识
     * String name;                        //用户名
     * String nickname;                    //昵称
     * Models.ConferenceRole role;         //参会角色
     * Models.DeviceState videoState;      //视频状态
     * Models.DeviceState audioState;      //音频状态
     * int terminalType;                   //登录终端类型：1-PC,2-Android,3-IOS,4-安卓一体机,5-录播主机
     */
    @Override
    public void onNotifyEnter(Models.Account account) {
        final int sdkNo = account.getStreamId();
        Log.e("5555555", "onNotifyEnter: 有人进入房间" + "  sdkno: " + sdkNo);

        final MemberBean memberBean = new MemberBean();
        memberBean.setSdkNo(sdkNo);
        memberBean.setAccountId(account.getId());
        memberBean.setCloseVideo(account.getVideoState());
        memberBean.setMute(account.getAudioState());

        if (!isRecvAudio) {
            memberBean.setCloseOtherAudio(true);
        } else {
            memberBean.setCloseOtherAudio(closeOtherAudio);
        }
        if (!isRecvVideo) {
            memberBean.setCloseOtherVideo(true);
        } else {
            memberBean.setCloseOtherVideo(closeOtherVideo);
        }

        if (closeOtherVideo) {
            roomClient.enableRecvVideo(sdkNo, false);
        }
        if (closeOtherAudio) {
            roomClient.enableRecvAudio(sdkNo, false);
        }

        if (windowAdapter.getMemberList().isEmpty()) {
            windowAdapter.addItem(memberBean);
        } else {
            int count = 0;
            for (MemberBean s : windowAdapter.getMemberList()) {
                if (s.getSdkNo() == sdkNo) {
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
        Log.e("5555555", "onNotifyExit: 有人离开房间" + "  roomSdkNo: " + sdkNo);

        if (!windowAdapter.getMemberList().isEmpty()) {
            for (MemberBean s : windowAdapter.getMemberList()) {
                if (s.getSdkNo() == sdkNo) {
                    windowAdapter.removeItem(sdkNo);
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
        ToastUtil.getInstance().showLongToast("主持人结束会议");
        onBackPressed();
    }

    @Override
    public void onFrame(byte[] ost, byte[] tnd, byte[] trd, int width, int height, int format, int streamId, int mask) {
//        Log.e("3333333333", "onFrame  " + "  clientId: " + clientId + "   " + width + " " + height + "   mask:" + mask);
        if (windowAdapter != null) {
            if (mainWindowMember != null && mainWindowMember.getSdkNo() == streamId) {
                if (cameraTextureView != null) {
                    cameraTextureView.update(width, height, format);
                    cameraTextureView.update(ost, tnd, trd, format);
                }
            } else {
                WindowAdapter.MyViewHolder holder = windowAdapter.getHolders().get(streamId);
                if (holder != null && holder.textureView != null) {
                    holder.textureView.update(width, height, format);
                    holder.textureView.update(ost, tnd, trd, format);
                }
            }
        }
    }

    @Override
    public void onSendInfo(String info) {
        //delay: 移动40-45， wifi 30  有线 20
        // 60000270::delay=17 status=1 speed=687 buffer=0 overflow=0 */
        // 60000270=id, delay=上传到服务器之间的延迟时间,越大越不好, status=-1上传出错 >=0正常, speed=发送速度 buffer=缓冲包0-4正常 */
        uploadTv.setText(info);
    }

    /**
     * {
     * "recvinfo": [
     * {
     * "linkid": 12340001,  对方Sdkno
     * "recv": 4127 接收包信息
     * "comp": 13,  补偿 高 网络不稳定
     * "losf": 0,   丢失包信息  高 就是网络差
     * "lrl": 6.8, //短时端到端丢包率（对方手机到你手机）
     * "lrd": 8.9 //短时下行丢包率（服务器到你）
     * }
     * ]
     * }
     */
    @Override
    public void onRecvInfo(String info) {
        losTv.setText(info);
    }

    @Override
    public void onXBitrate(int level, int bitRate) {
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
        Log.e(TAG, "onNotifyAccount" + "   delay:" + account.getDelay()
                + "  id:" + account.getId() + "  streamId:" + account.getStreamId()
                + "  name:" + account.getName() + "  nickname:" + account.getNickname()
                + "  videoState:" + account.getVideoState() + "  audioState:" + account.getAudioState() + "  role:" + account.getRole());

        StringBuilder streamLog = new StringBuilder("StreamList:   streamId:" + account.getStreamId());
        for (Models.Stream stream : account.getStreamsList()) {
            streamLog.append("\n  id：").append(stream.getId()).append("    channel:").append(stream.getChannel()).append("    name:").append(stream.getName()).append("   type:").append(stream.getType()).append("   channelType:").append(stream.getChannelType());
        }
        Log.e(TAG, streamLog.toString());

        List<MemberBean> memberBeans = windowAdapter.getMemberList();
        int size = memberBeans.size();

        boolean isChange = false;

        for (int i = 0; i < size; i++) {
            MemberBean memberBean = memberBeans.get(i);
            if (memberBean.getAccountId().equals(account.getId())) {
                if (memberBean.getCloseVideo() != account.getVideoState()) {//状态发送改变
                    isChange = true;
                    memberBean.setCloseVideo(account.getVideoState());
                }
                if (memberBean.getMute() != account.getAudioState()) {
                    isChange = true;
                    memberBean.setMute(account.getAudioState());
                }
                break;
            }
        }
        if (isChange) {
            // TODO: 2019/10/29 用notifyDataSetChanged会黑屏一下，可以直接拿到控件去控制
            windowAdapter.notifyDataSetChanged();
        }
    }

    //取流示例
    public void pickStream(Models.Account account) {
        int coursewareMain = 0;//课件大流mask
        int coursewareSub = 0;//课件小流

        int defaultMain = 0;//默认显示通道的大流
        int defaultSub = 0;//默认显示通道的小流

        int defaultChannel = -1;//默认显示的通道号
        int defaultChannelType = -1;//默认通道类型

        List<Models.Stream> streams = account.getStreamsList();
        //录播主机和硬终端
        if (account.getTerminalType() == Models.TerminalType.Terminal_Embedded) {
            for (Models.Stream stream : streams) {
                if (stream.getChannelType() != Models.ChannelType.CT_Courseware) {
                    defaultChannel = stream.getChannel();//以不是课件流的第一个通道为默认通道，基本是id=1的通道
                    defaultChannelType = stream.getChannelType().getNumber();
                    break;
                }
            }

            //如果这里defaultChannel == 0，表示除了课件外没其他通道了

            for (Models.Stream stream : streams) {
                if (stream.getChannelType() == Models.ChannelType.CT_Courseware) {//如果是课件
                    if (stream.getType() == Models.StreamType.Stream_Main) {
                        coursewareMain = stream.getId();
                    }
                    if (stream.getType() == Models.StreamType.Stream_Sub) {
                        coursewareSub = stream.getId();
                    }
                }
                //默认通道的通道类型
                if (stream.getChannel() == defaultChannel && stream.getChannelType().getNumber() == defaultChannelType) {
                    if (stream.getType() == Models.StreamType.Stream_Main) {
                        defaultMain = stream.getId();
                    }
                    if (stream.getType() == Models.StreamType.Stream_Sub) {
                        defaultSub = stream.getId();
                    }
                }
            }
        }

        //显示课件和默认通道画面,且网络ok
        if (coursewareMain != 0 && defaultMain != 0) {
            roomClient.setStreamTrack(account.getStreamId(), coursewareMain | defaultMain);
        }
        // TODO: 2020/6/1 其他判断是否有大小流，网络等

    }

    @Override
    public void onNotifyMyAccount(RoomServer.MyAccountNotify notify) {
        //sdk已经把数据同步到RoomClient的account里面了，
        Models.Account account = notify.getAccount();
        Log.e(TAG, "onNotifyMyAccount"
                + "  id:" + account.getId() + "  streamId:" + account.getStreamId()
                + "  name:" + account.getName() + "  nickname:" + account.getNickname()
                + "  hasVideo:" + account.hasVideoState() + "   hasAudio:" + account.hasAudioState()
                + "  videoState:" + account.getVideoState() + "  audioState:" + account.getAudioState() + "  role:" + account.getRole());
        if (account.hasVideoState() && isSendSelfVideo != (account.getVideoState() == Models.DeviceState.DS_Active)) {//主持人会控操作后，状态发生改变
            closeSelfVideo(true, account.getVideoState());
        }
        if (account.hasAudioState() && isSendSelfAudio != (account.getAudioState() == Models.DeviceState.DS_Active)) {
            closeSelfAudio(true, account.getAudioState());
        }
    }

    @Override
    public void onNotifyStreamChanged(RoomServer.StreamNotify streamNotify) {
        Log.e(TAG, "onNotifyStreamChanged:  "
                + "  sdkNo:" + streamNotify.getSdkNo()
                + "  operation:" + streamNotify.getOperation()
                + "  streamName:" + streamNotify.getStream().getName()
                + "  channelType:" + streamNotify.getStream().getChannelType()
                + "  type:" + streamNotify.getStream().getType());

        streamNotify.getAccountId(); //判断哪个用户

        switch (streamNotify.getOperation().getNumber()) {
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
        Log.e(TAG, "onNotifyPassThough:  " + passthroughNotify.getMessage());
    }

    //主持人会控,只有被控制的人会收到回调
    @Override
    public void onNotifyHostCtrlStream(RoomServer.HostCtrlStreamNotify hostCtrlStreamNotify) {
//        Models.Operation operation = hostCtrlStreamNotify.getOperation();
//        if (operation == Models.Operation.Operation_Add){
//
//        }else if (operation == Models.Operation.Operation_Remove){
//
//        }else if (operation == Models.Operation.Operation_Update){
//
//        }
    }

    /**
     * 网络丢包状态，网络差的时候才会回调
     * 0    0% - 8%
     * -1   8% - 15%
     * -2   15% - 30%
     * -3   >=30% 丢包
     */
    @Override
    public void onRecvStatus(int i) {

    }

    /**
     * 测速结果事件
     *
     * @param s 返回格式
     *          upld::recv=191 miss=10 losf=18 speed=2029127 delay=23
     *          down::recv=1164 miss=41 losf=67 speed=2078873 delay=22
     */
    @Override
    public void onTestSpeed(String s) {

    }

    /**
     * 聊天消息事件
     */
    @Override
    public void onNotifyChat(RoomServer.ChatNotify chatNotify) {
        ToastUtil.getInstance().showLongToast(chatNotify.getAccountName() + ":" + chatNotify.getMessage());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        floatIntent = new Intent(MeetingActivity.this, FloatingButtonService.class);
        isLand = this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;

        initView();
        initVcsApi();
        initRecording();
        initWebView();

        cameraTextureView.setCameraId(isFront ? 1 : 0);//1：前置  0：后置
        cameraTextureView.setLANDSCAPE(getDeviceRotation());//true:横屏 false:竖屏

    }

    private void initView() {
        setContentView(R.layout.activity_meeting);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        meetingBean = (MeetingBean) intent.getSerializableExtra(ROOM_INFO);
        roomSdkNo = Integer.parseInt(meetingBean.getSdk_no());
        trackServer = intent.getStringExtra(DEBUG_ADDR);
        openDebug = intent.getBooleanExtra(DEBUG_SWITCH, false);
        isMulti = intent.getBooleanExtra(MULTI, false);
        agc = Integer.parseInt(intent.getStringExtra(AGC));
        aec = Integer.parseInt(intent.getStringExtra(AEC));
        fps = Integer.parseInt(intent.getStringExtra(FPS));
        closeOtherAudio = intent.getBooleanExtra(CLOSE_OTHER_AUDIO, false);
        closeOtherVideo = intent.getBooleanExtra(CLOSE_OTHER_VIDEO, false);
        isSendSelfVideo = !intent.getBooleanExtra(CLOSE_SELF_VIDEO, true);
        isSendSelfAudio = !intent.getBooleanExtra(CLOSE_SELF_AUDIO, true);

        level = intent.getIntExtra(VIDEO_LEVEL, 0);

        if (level == 0) {
            videoH = 480;
            videoW = 848;
            bitRate = 512;
        } else {
            videoH = 720;
            videoW = 1280;
            bitRate = 900;
        }

        sampleRate = intent.getIntExtra(SAMPLE_RATE, 48000);

        hardDecoder = intent.getBooleanExtra(HARD_DECODER, false);

        windowAdapter = new WindowAdapter(MeetingActivity.this);
        windowRcView.setAdapter(windowAdapter);
        windowRcView.getRecycledViewPool().setMaxRecycledViews(0, 0);
        windowRcView.setLayoutManager(new GridLayoutManager(this, spanCount));

        nameTv.setText(UserConfig.getUserInfo().getData().getAccount().getRoom().getSdk_no());

        windowAdapter.setOnItemClickListener((view, memberBean) -> {
            clickWindow(memberBean);
            setVcsPlayer();
        });
    }

    //初始化API
    private void initVcsApi() {
        //开启track 调试
        if (openDebug) {
            roomClient = new RoomClient(this, roomSdkNo, trackServer);
        } else {
            roomClient = new RoomClient(this, roomSdkNo);
        }

        roomClient.setRoomEvent(this);//设置会议回调

        shareAccId = UserConfig.getUserInfo().getData().getAccount().getId();

        roomClient.setAccount(
                UserConfig.getUserInfo().getData().getAccount().getId(),
                UserConfig.getUserInfo().getData().getAccount().getRoom().getSdk_no(),
                UserConfig.getUserInfo().getData().getAccount().getName(),
                UserConfig.getUserInfo().getData().getAccount().getNickname(),
                UserConfig.getUserInfo().getData().getAccount().getPortrait());
        roomClient.setRoom(meetingBean.getRoom().getId(), roomSdkNo, meetingBean.getSession());
        roomClient.setStreamAddr(meetingBean.getStream_host(), meetingBean.getStream_port());
        roomClient.setMeetingAddr(meetingBean.getMeeting_host(), meetingBean.getMeeting_port());

        //测试用，保存流媒体信息
//        File file = getExternalFilesDir(null);
//        String filePath = file.getAbsolutePath();
//        roomClient.setDebugLog(filePath);

        //测试用，保存流信息
//        File file1 = getExternalFilesDir(null);
//        String filePath1 = file1.getAbsolutePath();
//        roomClient.getApi().VCS_setSaveFrameForTest(1, filePath1);

        roomClient.useMultiStream(isMulti);//设置开启多流
        roomClient.setMinEncoderSoft(false);//在setVideoOutput前设置
        roomClient.setCenterInside(true);

//        roomClient.setResolutionSize(1280, 720);
        //输出分辨率宽必须是16的倍数,高必须是2的倍数,否则容易出现绿边等问题
        //小码流高（会根据setVideoOutput设置的宽高自动计算宽，一定要放在setVideoOutput方法之前设置），码流，帧率
        roomClient.setMinVideoOutput(360, 500, 15);
        roomClient.setVideoOutput(videoW, videoH, fps, bitRate);//设置视频分辨率宽高，帧率，码率
        roomClient.setAudioSampleRate(sampleRate);//设置采样率
        roomClient.setAgcAec(agc, aec);//设置AGC,AEC
        roomClient.setFps(fps);//设置帧率

        roomClient.openCamera(null, this);//设置预览view

        roomClient.enableXDelay(true);//自适应延迟
        roomClient.useHwDecoder(hardDecoder);//是否硬解码

        if (isSendSelfAudio) {
            roomClient.setDefaultSendSelfAudio(true);
            closeSelfAudioBtn.setText("关闭自己的音频");
        } else {
            roomClient.setDefaultSendSelfAudio(false);
            closeSelfAudioBtn.setText("打开自己的音频");
        }

        if (isSendSelfVideo) {
            roomClient.setDefaultSendSelfVideo(true);
            closeSelfVideoBtn.setText("关闭自己的视频");
            closePreviewTv.setVisibility(View.GONE);
        } else {
            roomClient.setDefaultSendSelfVideo(false);
            closeSelfVideoBtn.setText("打开自己的视频");
            closePreviewTv.setVisibility(View.VISIBLE);
        }

        selfMember = new MemberBean();
        selfMember.setAccountId(UserConfig.getUserInfo().getData().getAccount().getId());
        selfMember.setSdkNo(Integer.parseInt(UserConfig.getUserInfo().getData().getAccount().getRoom().getSdk_no()));
        selfMember.setMute(isSendSelfAudio ? Models.DeviceState.DS_Active : Models.DeviceState.DS_Closed);
        selfMember.setCloseVideo(isSendSelfVideo ? Models.DeviceState.DS_Active : Models.DeviceState.DS_Closed);

        mainWindowMember = selfMember;

        roomClient.setOrientationChange((streamId, x, y) -> {
            Log.e("5555555", "setOrientationChange  x:" + x + "  y:" + y);
            for (MemberBean memberBean : windowAdapter.getMemberList()){
                WindowAdapter.MyViewHolder holder = windowAdapter.getHolders().get(streamId);

                if (memberBean.getSdkNo()==streamId){
                    memberBean.setX(x);
                    memberBean.setY(y);

                    if (holder != null) {
                        setVcsPlayerScale(holder.textureView, memberBean);
                    }
                    break;
                }
            }
        });

        roomClient.open();
    }

    //设置显示控件的属性
    private void setVcsPlayer(){
        //设置大窗口属性，currentMember代表当前大窗口展示人的信息
        if (mainWindowMember.getAccountId().equals(roomClient.getAccount().getId())){
            //如果切换后的大窗口是自己预览画面
            cameraTextureView.setCameraId(isFront ? 1 : 0);//1：前置  0：后置
            cameraTextureView.setLANDSCAPE(getDeviceRotation());//true:横屏 false:竖屏
            cameraTextureView.setScaleType(VCS_EVENT_TYPE.CENTERINSIDE);//默认CENTERINSIDE
        }else {//大窗口不是自己的预览画面,则设置为默认状态
            cameraTextureView.setCameraId(0);//1：前置  0：后置
            cameraTextureView.setLANDSCAPE(90);
            setVcsPlayerScale(cameraTextureView, mainWindowMember);
        }

        //设置小窗口属性
        for (MemberBean member : windowAdapter.getMemberList()){
            WindowAdapter.MyViewHolder holder = windowAdapter.getHolders().get(member.getSdkNo());

            if (mainWindowMember.getAccountId().equals(member.getAccountId())){
                //当前成员在大窗口上，则该小窗口显示的是自己预览画面
                if (holder != null){
                    holder.textureView.setCameraId(isFront ? 1 : 0);//1：前置  0：后置
                    holder.textureView.setLANDSCAPE(getDeviceRotation());
                    holder.textureView.setScaleType(VCS_EVENT_TYPE.CENTERINSIDE);//默认CENTERINSIDE
                }
            }else {
                //小窗口显示原先成员信息
                if (holder != null){
                    holder.textureView.setCameraId(0);//恢复默认
                    holder.textureView.setLANDSCAPE(90);//恢复默认
                    setVcsPlayerScale(holder.textureView, member);
                }
            }
        }
    }

    public void setVcsPlayerScale(VcsPlayerGlTextureView textureView, MemberBean memberBean){
        if (memberBean.getX() == 1 && !isLand){
            textureView.setScaleType(VCS_EVENT_TYPE.CENTERCROP);
            Log.e("5555555", "setOrientationChange x crop");
        }else {
            if (memberBean.getY() == 1 && isLand){
                textureView.setScaleType(VCS_EVENT_TYPE.CENTERCROP);
                Log.e("5555555", "setOrientationChange y crop");
            }else {
                textureView.setScaleType(VCS_EVENT_TYPE.CENTERINSIDE);
                Log.e("5555555", "setOrientationChange inside");
            }
        }
    }

    /**
     * 小窗口点击事件
     */
    private void clickWindow(MemberBean memberBean) {
        if (windowAdapter != null) {
            WindowAdapter.MyViewHolder holder = windowAdapter.getHolders().get(memberBean.getSdkNo());
            if (mainWindowMember == memberBean){//点击的小窗口正显示在大窗口
                mainWindowMember = selfMember;
            }else {


                updateViewInfoSmallOld(mainWindowMember);//恢复原先小窗口的信息，用先前的memberBean
                mainWindowMember = memberBean;
            }
            updateViewInfo();
            updateViewInfoSmall(memberBean, holder);
        }
    }

    /**
     * 更新主画面的状态信息，昵称，音视频状态等
     */
    private void updateViewInfo(){
        if (mainWindowMember == null){
            return;
        }

        nameTv.setText(mainWindowMember.getSdkNo() + "");
        closePreviewTv.setVisibility(mainWindowMember.isCloseVideo() ? View.VISIBLE : View.GONE);
        closePreviewTv.setText(mainWindowMember.getSdkNo() + "\n" + "视频已关闭");
    }

    /**
     * 更新小窗口的状态信息
     */
    private void updateViewInfoSmall(MemberBean memberBean, WindowAdapter.MyViewHolder holder){
        if (holder == null){
            return;
        }
        if (mainWindowMember != selfMember){//大窗口不是预览，小窗口显示自己的
            holder.nameTv.setText(roomClient.getAccount().getStreamId() + "");
            holder.selfCloseTv.setText(roomClient.getAccount().getStreamId() + "\n" + "视频已关闭");
            holder.selfCloseTv.setVisibility(selfMember.isCloseVideo() ? View.VISIBLE : View.GONE);

            //大窗口设置大流
            Models.Account account = roomClient.getAccountList().get(mainWindowMember.getSdkNo());
            if (account != null){
                if (account.getTerminalType() == Models.TerminalType.Terminal_Embedded){
                    for (Models.Stream stream : account.getStreamsList()){
                        if (stream.getId() == 1){//取id=1的流为默认流
                            roomClient.pickStream(account.getId(), stream.getChannel(), stream.getChannelType(), Models.StreamType.Stream_Main);
                            break;
                        }
                    }
                }else {
                    roomClient.pickStreamMain(account.getId());
                }
            }
        } else {
            if (memberBean == null){
                return;
            }
            holder.nameTv.setText(memberBean.getSdkNo() + "");
            holder.selfCloseTv.setText(memberBean.getSdkNo() + "\n" + "视频已关闭");
            holder.selfCloseTv.setVisibility(memberBean.isCloseVideo() ? View.VISIBLE : View.GONE);

            //恢复为小码流
            Models.Account account = roomClient.getAccountList().get(memberBean.getSdkNo());
            if (account != null){
                if (account.getTerminalType() == Models.TerminalType.Terminal_Embedded){//录播主机
                    for (Models.Stream stream : account.getStreamsList()){
                        if (stream.getId() == 1){
                            roomClient.pickStream(account.getId(), stream.getChannel(), stream.getChannelType(), Models.StreamType.Stream_Sub);
                            break;
                        }
                    }
                }else {
                    roomClient.pickStreamSub(account.getId());
                }
            }
        }
    }

    /**
     * 恢复原先小窗口的信息
     */
    private void updateViewInfoSmallOld(MemberBean memberBean){
        if (memberBean == null){
            return;
        }
        WindowAdapter.MyViewHolder holder = windowAdapter.getHolders().get(memberBean.getSdkNo());
        if (holder != null){
            holder.nameTv.setText(memberBean.getSdkNo() + "");
            holder.selfCloseTv.setText(memberBean.getSdkNo() + "\n" + "视频已关闭");
            holder.selfCloseTv.setVisibility(memberBean.isCloseVideo() ? View.VISIBLE : View.GONE);
//            holder.textureView.setMirror(0);
        }
        //恢复小流
        Models.Account account = roomClient.getAccountList().get(memberBean.getSdkNo());
        if (account != null){
            if (account.getTerminalType() == Models.TerminalType.Terminal_Embedded){
                for (Models.Stream stream : account.getStreamsList()){
                    if (stream.getId() == 1){
                        roomClient.pickStream(account.getId(), stream.getChannel(), stream.getChannelType(), Models.StreamType.Stream_Sub);
                        break;
                    }
                }
            }else {
                roomClient.pickStreamSub(account.getId());
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (roomClient != null) {
            roomClient.close();//退出释放
        }
//        orientationEventListener.disable();
        super.onDestroy();
    }

    //闪光灯开关
    private void switchLight() {
        if (isFront) {
            ToastUtil.getInstance().showLongToast("后置摄像头才能开启闪光灯");
            return;
        }
        isLight = !isLight;
        roomClient.cameraLight(isLight);
        if (isLight) {
            cameraLightBtn.setText("关闪光灯");
        } else {
            cameraLightBtn.setText("开闪光灯");
        }
    }

    //切换摄像头
    private void switchCamera() {
        roomClient.switchCamera(isFront);
        isFront = !isFront;
        if (isFront) {
            cameraSwitchBtn.setText("后置");
        } else {
            cameraSwitchBtn.setText("前置");
        }

        cameraTextureView.setCameraId(isFront ? 1 : 0);//1：前置  0：后置
    }

    //设置是否接收某人的视频
    public void closeOtherVideo(int streamId, boolean isClose) {
        roomClient.enableRecvVideo(streamId, !isClose);
    }

    //设置是否接收某人音频
    public void muteOtherAudio(int streamId, boolean isMute) {
        roomClient.enableRecvAudio(streamId, !isMute);
    }

    //主持人踢人
    public void kickOut(String id) {
        roomClient.hostKickOut(id);
    }

    //主持人会控
    public void hostCtrlMember(String acc, Models.DeviceState video, Models.DeviceState audio) {
        roomClient.hostCtrl(acc, video, audio);
    }

    //掩码方式
    public void useChannel(int sdkNo, int mask) {
        roomClient.setStreamTrack(sdkNo, mask);
    }

    //设置自己的视频，是否发送
    private void closeSelfVideo(boolean isFromNotify, Models.DeviceState fromNotifyState) {
        if (isFromNotify) {//来自会控操作
            roomClient.enableSendVideo(fromNotifyState);
            selfMember.setCloseVideo(fromNotifyState);
            isSendSelfVideo = fromNotifyState != Models.DeviceState.DS_Disabled;
        } else {
            Models.Account account = roomClient.getAccount();
            if (account.getVideoState() == Models.DeviceState.DS_Disabled) {
                ToastUtil.getInstance().showLongToast("当前被主持人关闭，不可操作");
                return;
            }
            isSendSelfVideo = account.getVideoState() != Models.DeviceState.DS_Active;
            if (isSendSelfVideo) {
                roomClient.enableSendVideo(Models.DeviceState.DS_Active);
                selfMember.setCloseVideo(Models.DeviceState.DS_Active);
            } else {
                roomClient.enableSendVideo(Models.DeviceState.DS_Closed);
                selfMember.setCloseVideo(Models.DeviceState.DS_Closed);
            }
        }

        if (mainWindowMember == selfMember) {
            closePreviewTv.setVisibility(!isSendSelfVideo ? View.VISIBLE : View.GONE);
        } else {
            WindowAdapter.MyViewHolder holder = windowAdapter.getHolders().get(mainWindowMember.getSdkNo());
            holder.selfCloseTv.setVisibility(selfMember.isCloseVideo() ? View.VISIBLE : View.GONE);
        }

        if (isSendSelfVideo) {
            closeSelfVideoBtn.setText("关闭自己的视频");
        } else {
            closeSelfVideoBtn.setText("打开自己的视频");
        }
    }

    //设置自己的音频，是否发送
    private void closeSelfAudio(boolean isFromNotify, Models.DeviceState fromNotifyState) {
        if (isFromNotify) {
            roomClient.enableSendAudio(fromNotifyState);
            selfMember.setMute(fromNotifyState);
            isSendSelfAudio = fromNotifyState != Models.DeviceState.DS_Disabled;
        } else {
            Models.Account account = roomClient.getAccount();
            if (account.getAudioState() == Models.DeviceState.DS_Disabled) {
                ToastUtil.getInstance().showLongToast("当前被主持人禁言，不可操作");
                return;
            }
            isSendSelfAudio = account.getAudioState() != Models.DeviceState.DS_Active;
            if (isSendSelfAudio) {
                roomClient.enableSendAudio(Models.DeviceState.DS_Active);
                selfMember.setMute(Models.DeviceState.DS_Active);
            } else {
                roomClient.enableSendAudio(Models.DeviceState.DS_Closed);
                selfMember.setMute(Models.DeviceState.DS_Closed);
            }
        }

        if (isSendSelfAudio) {
            closeSelfAudioBtn.setText("关闭自己的音频");
        } else {
            closeSelfAudioBtn.setText("打开自己的音频");
        }
    }

    public void changeSize(){
        int width, height;

        if (isLand){
            height = (DisplayUtil.getInstance().getMobileWidth(this)/spanCount) * 9 / 16;
        }else {
            height = (DisplayUtil.getInstance().getMobileWidth(this)/spanCount) * 16 / 9;
        }
        width = DisplayUtil.getInstance().getMobileWidth(this)/spanCount;

        for (WindowAdapter.MyViewHolder holder : windowAdapter.getHolders().values()){
            holder.frameLayout.setLayoutParams(new ConstraintLayout.LayoutParams(width, height));
        }
    }

    @Override
    public void onConfigurationChanged(@NotNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        isLand = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE;

        for (MemberBean memberBean : windowAdapter.getMemberList()){
            WindowAdapter.MyViewHolder holder = windowAdapter.getHolders().get(memberBean.getSdkNo());
            if (holder != null){
                setVcsPlayerScale(holder.textureView, memberBean);
            }
        }
        changeSize();

        if (mainWindowMember.getSdkNo() == selfMember.getSdkNo()){
            cameraTextureView.setLANDSCAPE(getDeviceRotation());
        }else {
            WindowAdapter.MyViewHolder holder;
            for (MemberBean memberBean : windowAdapter.getMemberList()){
                if (memberBean.getSdkNo() == mainWindowMember.getSdkNo()){
                    holder = windowAdapter.getHolders().get(memberBean.getSdkNo());
                    if (holder != null){
                        holder.textureView.setLANDSCAPE(getDeviceRotation());
                    }
                    break;
                }
            }
        }
    }

    Intent floatIntent;
    public boolean isLand = true;

    @SuppressLint("SourceLockedOrientationActivity")
    @OnClick({R.id.rec_btn, R.id.send_msg_btn, R.id.camera_switch_btn,
            R.id.camera_light_btn, R.id.close_self_video_btn, R.id.close_self_audio_btn,
            R.id.not_recv_audio_btn, R.id.not_recv_video_btn, R.id.change_orientation_btn,
            R.id.closeBoardBtn, R.id.bgBtn, R.id.toggleToolBtn, R.id.openBoardBtn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.openBoardBtn:
                roomClient.startToShare(Models.SharingType.ST_WhiteBoard, "");
                break;
            case R.id.closeBoardBtn:
                if (!shareAccId.equals(roomClient.getAccount().getId())) {
                    //不是当前白板发起人，无法关闭
                    ToastUtil.getInstance().showLongToast("您没有权限完成此操作");
                    return;
                }
                roomClient.stopToShare();
                break;
            case R.id.bgBtn:
                webView.evaluateJavascript("javascript:setBgImage('http://crazy.image.alimmdn.com/iSaior/14878273006128.png')", value -> { });
                break;
            case R.id.toggleToolBtn:
                webView.evaluateJavascript("javascript:toggleBtns()", value -> { });
                break;
            case R.id.rec_btn:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (PermissionUtil.getInstance().isGrandFloating(this)) {
                        startRecord();
                    } else {
                        PermissionUtil.getInstance().setPermissionFloat(this, success -> {
                            //成功授权
                            if (success) {
                                startRecord();
                            }
                        });
                    }
                } else {
                    //没有悬浮按钮
                    startRecord();
                }
                break;
            case R.id.camera_light_btn://闪光灯开关
                switchLight();
                break;
            case R.id.camera_switch_btn://切换摄像头
                switchCamera();
                break;
            case R.id.change_orientation_btn:
                if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {//当前是横屏
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//竖屏
                } else {//当前是竖屏
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//横屏
                }
                break;
            case R.id.close_self_video_btn://设置自己的视频，是否发送
                closeSelfVideo(false, null);
                break;
            case R.id.close_self_audio_btn://设置自己的音频，是否发送
                closeSelfAudio(false, null);
                break;
            case R.id.not_recv_audio_btn://不接收所有人音频
                isRecvAudio = !isRecvAudio;
                roomClient.enableRecvAudio(0, isRecvAudio);
                if (isRecvAudio) {
                    notRecvAudioBtn.setText("不接收所有人音频");
                } else {
                    notRecvAudioBtn.setText("接收所有人音频");
                }
                for (MemberBean memberBean : windowAdapter.getMemberList()) {
                    memberBean.setCloseOtherAudio(!isRecvAudio);
                }
                windowAdapter.notifyDataSetChanged();
                break;
            case R.id.not_recv_video_btn://不接收所有人视频
                isRecvVideo = !isRecvVideo;
                roomClient.enableRecvVideo(0, isRecvVideo);
                if (isRecvVideo) {
                    notRecvVideoBtn.setText("不接收所有人视频");
                } else {
                    notRecvVideoBtn.setText("接收所有人视频");
                }
                for (MemberBean memberBean : windowAdapter.getMemberList()) {
                    memberBean.setCloseOtherVideo(!isRecvVideo);
                }
                windowAdapter.notifyDataSetChanged();
                break;
            case R.id.send_msg_btn:
                roomClient.sendChatMsg(null, "你好");
                break;
        }
    }

    public void startRecord() {
        if (roomClient.isBusyRecording()) {
            roomClient.stopScreenRecording();//停止录屏
        } else {
            roomClient.startScreenRecording();//开始录屏
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    //录屏
    /////////////////////////////////////////////////////////////////////////////////////////////////
    private void initRecording() {
        //这里采用编码方式 默认也采用编码方式，这时候流对应的track=2,camera 流没有停止，如果采用NoEncoderMode 模式 那么 录屏启动后 camera
        // 流将会被停止发送 这时候原来的track保持不变
        roomClient.initScreenRecorder(true, VCS_EVENT_TYPE.NoEncoderMode);
        //设置录屏时通知栏样式,shouldNotification = true时有效
        roomClient.setRecordNotification(R.mipmap.ic_launcher, "正在共享屏幕", "点击按钮结束录制", "停止录制");
        //设置录屏大小如果不采用编码方式那么这个大小应该和VCS_CreateVideoOutput 设置大小保持一致，编码方式那么就是自己設置【最大值1920x1080】
        //alterable 设置大小是否随横竖屏进行变换调整，当前版本设置无效·
        roomClient.setRecordingSize(960, 540, true);

        roomClient.setScreenRecordListener((event, info) -> {
            Log.e("22222222222", event + "");
            switch (event) {
                case VCS_EVENT_TYPE.ScreenRecordError:
//                    ToastUtil.getInstance().showLongToast(info);
                    break;
                case VCS_EVENT_TYPE.ScreenRecordStart:
                    startService(floatIntent);

                    ToastUtil.getInstance().showLongToast("开始录屏");
                    recordingBtn.setText("关闭录屏");
                    moveTaskToBack(true);
                    break;
                case VCS_EVENT_TYPE.ScreenRecordStop:
                    if (recordingBtn.getText().equals("关闭录屏")) {
                        stopService(floatIntent);

                        recordingBtn.setText("启动录屏");
                        ToastUtil.getInstance().showLongToast("停止录屏");
                    }
                    break;
            }
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);// 缩放至屏幕的大小
        webSettings.setUseWideViewPort(true);//将图片调整到适合webview的大小
        webSettings.setSupportZoom(false);//支持缩放，默认为true。
        webSettings.setBuiltInZoomControls(false);//设置内置的缩放控件。若为false，则该WebView不可缩放
    }

    private void loadWebView() {
        boolean isHost = roomClient.getAccount().getRole() != Models.ConferenceRole.CR_Member;
        webView.loadUrl(meetingBean.getWb_host() + "?meetingId=" + roomSdkNo + "&userId=" + roomClient.getAccount().getId() + "&isOwner=" + isHost);
//        webView.loadUrl("https://www.dev.swmeeting.cn:9001/test/");
        boardCl.setVisibility(View.VISIBLE);
    }

    private void hideWebView() {
        webView.loadUrl("about:blank");
        webView.clearCache(true);
        boardCl.setVisibility(View.GONE);
    }

    @Override
    public void onPreviewFrame(byte[] yuv, int width, int height, long stamp, int format) {
        if (mainWindowMember.getSdkNo() == selfMember.getSdkNo()){//主画面是自己的预览
            if (cameraTextureView != null){
                cameraTextureView.update(width, height, format);
                cameraTextureView.update(yuv, format);
            }
        }else {//切换了位置，预览在小窗口
            WindowAdapter.MyViewHolder holder = windowAdapter.getHolders().get(mainWindowMember.getSdkNo());
            if (holder != null) {
                holder.textureView.update(width, height, format);
                holder.textureView.update(yuv, format);
            }
        }
    }

    private int getDeviceRotation(){
        int rotation = 0;
        int angle = getWindowManager().getDefaultDisplay().getRotation();
        switch (angle) {
            case Surface.ROTATION_0:
                rotation = 0;
                break;
            case Surface.ROTATION_90:
                rotation = 90;
                break;
            case Surface.ROTATION_180:
                rotation = 180;
                break;
            case Surface.ROTATION_270:
                rotation = 270;
                break;
        }
        return rotation;
    }
}