package com.freewind.meetingdemo.activity;

import android.annotation.SuppressLint;
import android.app.PictureInPictureParams;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.freewind.meetingdemo.MyApplication;
import com.freewind.meetingdemo.R;
import com.freewind.meetingdemo.adapter.WindowAdapter;
import com.freewind.meetingdemo.bean.MeetingBean;
import com.freewind.meetingdemo.bean.MemberBean;
import com.freewind.meetingdemo.bean.RoomInfoBean;
import com.freewind.meetingdemo.common.Constants;
import com.freewind.meetingdemo.common.UserConfig;
import com.freewind.meetingdemo.http.HttpCallBack;
import com.freewind.meetingdemo.util.DialogUtils;
import com.freewind.meetingdemo.util.DisplayUtil;
import com.freewind.meetingdemo.util.FloatingButtonService;
import com.freewind.meetingdemo.util.ForeService;
import com.freewind.meetingdemo.util.GlideUtil;
import com.freewind.meetingdemo.util.PermissionUtil;
import com.freewind.meetingdemo.util.Requester;
import com.freewind.meetingdemo.util.ToastUtil;
import com.freewind.vcs.AudioStatusCallback;
import com.freewind.vcs.CameraPreview;
import com.freewind.vcs.MessageManager;
import com.freewind.vcs.Models;
import com.freewind.vcs.RoomClient;
import com.freewind.vcs.RoomEvent;
import com.freewind.vcs.RoomServer;
import com.freewind.vcs.bean.AudioStatusBean;
import com.freewind.vcs.board.DrawFrameLayout;
import com.freewind.vcs.impl.MqttConnectStatusImpl;
import com.freewind.vcs.impl.StreamStatusEvent;
import com.freewind.vcs.menu.AudioRouterMenu;
import com.ook.android.VCS_EVENT_TYPE;
import com.ook.android.ikPlayer.VcsPlayerGlSurfaceView;
import com.ook.android.ikPlayer.VcsPlayerGlTextureView;

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
    @BindView(R.id.rec_btn)
    Button recordingBtn;

    private int videoH = 480;
    private int videoW = 848;
    private int fps = 15;//如果视频源来自摄像头，24FPS已经是肉眼极限，所以一般20帧的FPS就已经可以达到很好的用户体验了
    private int bitRate = 1024;

    private boolean isLight = false;//是否打开闪光灯
    private boolean isFront = true;//是否前置
    private boolean isSendSelfVideo = true;//是否发送自己的视频
    private boolean isSendSelfAudio = true;//是否发送自己的音频

    private boolean isMulti = false;
    private int roomSdkNo;
    private static String TAG = "4444444444";

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

    int level = 0;//0:720P  1:1080P

    private boolean isRecvAudio = true, isRecvVideo = true;//默认接收

    private WindowAdapter windowAdapter;

    public int spanCount = 2;

    public MemberBean mainWindowMember;//保存在主窗口的成员的信息
    public MemberBean selfMember;

    Intent mForegroundService;

    @Override
    public void onEnter(int result) {
        //如果result != 0 则表示服务器上的token已经失效，需要进行重新进入房间的逻辑
        Log.e("2222222", result + "");
        if (result != 0) {
            DialogUtils.getInstance().getConfirmDialog(this, "提示", "是否进行重连？", "取消", "确定", new DialogUtils.CallBack() {
                @Override
                public void onConfirm() {
                    ToastUtil.getInstance().showLongToast("正在进行重连");
                    if (progressBar.getVisibility() == View.VISIBLE) {
                        return;
                    }
                    progressBar.setVisibility(View.VISIBLE);
                    Requester.enterMeeting(MeetingActivity.this, meetingBean.getRoom().getNo(), "", meetingBean.getUpload_id(),new HttpCallBack<RoomInfoBean>() {
                        @Override
                        public void onSucceed(RoomInfoBean data) {
//                    if (data.getCode() == Constants.NEED_PWD) {
//                        ToastUtil.getInstance().showLongToast("该会议室需要密码");
//                        onBackPressed();
//                    }
                            roomClient.setSession(data.getData().getSession());
                            MessageManager.getInstance().reSubscribeTopics();
                            roomClient.reconnect();
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

                @Override
                public void onCancel() {
                    onBackPressed();
                }
            }).show();
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

        switch (room.getSharingType().getNumber()) {
            case Models.SharingType.ST_None_VALUE:
                if (!shareAccId.isEmpty()) {//录屏恢复
                    if (mainWindowMember.getAccountId().equals(shareAccId)){
                        roomClient.pickStreamMain(shareAccId);
                    }else{
                        roomClient.pickStreamSub(shareAccId);
                    }
                }

                shareAccId = "";
                break;
            case Models.SharingType.ST_WhiteBoard_VALUE:
            case Models.SharingType.ST_Picture_VALUE:
                break;
//                GlideUtil.showImage(this, room.getSharingPicUrl(), drawFrameLayout.getBackView());
            case Models.SharingType.ST_Desktop_VALUE://开启
                shareAccId = room.getSharingAccId();
                int mask = 0;
                int sdkNo = 0;
                try {
                    sdkNo = Integer.parseInt(room.getSharingSdkno());
                    mask = Integer.parseInt(room.getSharingStreamId());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                roomClient.pickMember(sdkNo, true);
                roomClient.setStreamTrack(sdkNo, mask);
                break;
        }
    }

    @Override
    public void onNotifyKickOut(RoomServer.KickoutNotify kickoutNotify) {
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
    public void onNotifyEnter(Models.Account account, String msgId) {
        final int sdkNo = account.getStreamId();
        Log.e("5555555", "onNotifyEnter: 有人进入房间" + "  sdkno: " + sdkNo);

        if (shareAccId.isEmpty()){
            roomClient.pickMember(account.getStreamId(), true);
            roomClient.pickStreamSub(account.getStreamId());
        }

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

        windowAdapter.removeItem(sdkNo);
        if (mainWindowMember.getSdkNo() == sdkNo) {
            mainWindowMember = selfMember;
            cameraTextureView.setCameraId(isFront ? 1 : 0);
            updateViewInfo();
        }
    }

    @Override
    public void onNotifyBegin(String roomId) {
        Log.e(TAG, "onNotifyBegin");
    }

    @Override
    public void onNotifyEnd(String roomId) {
        Log.e(TAG, "onNotifyEnd");
        onBackPressed();
    }

    byte[] data;
    int width, height;

    @Override
    public void onFrame(byte[] ost, byte[] tnd, byte[] trd, int width, int height, int format, int streamId, int mask, int label, int stamp) {
//        Log.e("3333333333", "onFrame  " + "  clientId: " + streamId + "   " + width + " " + height + "   mask:" + mask + "   label:" + label
//                + "   ost:" + ost.length + "   tnd:" + tnd + "   trd:" + trd);
        if (windowAdapter != null) {
            VcsPlayerGlTextureView vcsPlayerGlTextureView = getTargetSurfaceView(streamId);
            if (vcsPlayerGlTextureView != null) {
                vcsPlayerGlTextureView.update(width, height, format);
                vcsPlayerGlTextureView.update(ost, tnd, trd, format, label);
            }
        }

    }

    private VcsPlayerGlTextureView getTargetSurfaceView(int sdkNo) {
        VcsPlayerGlTextureView vcsPlayerGlTextureView = null;
        if (mainWindowMember != null && mainWindowMember.getSdkNo() == sdkNo) {
            vcsPlayerGlTextureView = cameraTextureView;
        } else {
            WindowAdapter.MyViewHolder holder;
            if (sdkNo == selfMember.getSdkNo()) {
                holder = windowAdapter.getHolders().get(mainWindowMember.getSdkNo());
            } else {
                holder = windowAdapter.getHolders().get(sdkNo);
            }
            if (holder != null && holder.textureView != null) {
                vcsPlayerGlTextureView = holder.textureView;
            }
        }
        return vcsPlayerGlTextureView;
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
            streamLog.append("\n  id：").append(stream.getId()).append("   angle:").append(stream.getAngle()).append("    channel:").append(stream.getChannel()).append("    name:").append(stream.getName()).append("   type:").append(stream.getType()).append("   channelType:").append(stream.getChannelType());
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
    public void onNotifyMyAccount(RoomServer.MyAccountNotify myAccountNotify, boolean b) {
        //sdk已经把数据同步到RoomClient的account里面了，
        Models.Account account = myAccountNotify.getAccount();
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
    public void onRecvStatus(int i, int streamId) {

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
        ToastUtil.getInstance().showLongToast(chatNotify.getMessage());
    }

    @Override
    public void onNotifyHandUp(RoomServer.HandUpNotify handUpNotify) {

    }

    @Override
    public void onNotifyAudienceNum(RoomServer.WebinarAudienceNumNotify roomCountInfo) {
        Log.e("22222222", "audienceNum:"+roomCountInfo.getAudienceNum() + "   accountListSize:" + roomClient.getAccountList().size());
    }

    @Override
    public void onNotifyWebinarRole(RoomServer.WebinarRoleNotify roleNotify) {

    }

    @Override
    public void onMcuRunStateNotify(RoomServer.McuRunStateNotify mcuRunStateNotify) {
        mcuRunStateNotify.getMcuStatus();//0 正常录制 1 录制异常
        mcuRunStateNotify.getMcuMsg();//相关信息
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

//        Utils.sendNotification(this);

//        cameraTextureView.customDisplayCtrl(true);
//        cameraTextureView.setViewScaleType(VCS_EVENT_TYPE.CENTERCROP);

        mForegroundService = new Intent(this, ForeService.class);

        // Android 8.0使用startForegroundService在前台启动新服务
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            startForegroundService(mForegroundService);
        } else {
            startService(mForegroundService);
        }

    }

    boolean openDebug;
    private String trackServer = "";

    private void initView() {
        setContentView(R.layout.activity_meeting);
        ButterKnife.bind(this);

        Intent intent = getIntent();

        meetingBean = (MeetingBean) intent.getSerializableExtra(ROOM_INFO);
        roomSdkNo = Integer.parseInt(meetingBean.getSdk_no());
        videoH = 720;
        videoW = 1280;
        bitRate = 900;
        trackServer = intent.getStringExtra(Constants.DEBUG_ADDR);
        openDebug = intent.getBooleanExtra(Constants.DEBUG_SWITCH, false);

        closeOtherAudio = intent.getBooleanExtra(Constants.CLOSE_OTHER_AUDIO, false);
        closeOtherVideo = intent.getBooleanExtra(Constants.CLOSE_OTHER_VIDEO, false);
        isSendSelfVideo = !intent.getBooleanExtra(Constants.CLOSE_SELF_VIDEO, true);
        isSendSelfAudio = !intent.getBooleanExtra(Constants.CLOSE_SELF_AUDIO, true);
        isMulti = intent.getBooleanExtra(Constants.MULTI, true);


        windowAdapter = new WindowAdapter(MeetingActivity.this);
        windowRcView.setAdapter(windowAdapter);
        windowRcView.getRecycledViewPool().setMaxRecycledViews(0, 0);
        windowRcView.setLayoutManager(new GridLayoutManager(this, spanCount));
        nameTv.setText(meetingBean.getSdk_no());//UserConfig.getUserInfo().getData().getAccount().getRoom().getSdk_no()修改过
        windowAdapter.setOnItemClickListener((view, memberBean) -> {
            clickWindow(memberBean);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (roomClient != null) {
//            roomClient.onResumeCamera();
//        }
    }

    /**
     * 设置MCU录制轨道
     * @param isMainStream  是否录制大码流 true:大码流    false:小码流
     */
    private void updateMCUTrack(boolean isMainStream){
        int streamSize = roomClient.getAccount().getStreamsList().size();
        switch (streamSize){
            case 0://没有配置过发送流，返回不设置
                return;
            case 1://只配置了一路流
                roomClient.setMCUTrack(1);//默认去取第一个轨道
                break;
            default://配置了多路流
                boolean isSet = false;
                for (Models.Stream stream : roomClient.getAccount().getStreamsList()){
                    if (stream.getChannelType() == Models.ChannelType.CT_Default){//默认视频轨道类型
                        if (isMainStream){
                            if (stream.getType() == Models.StreamType.Stream_Main){//大码流
                                roomClient.setMCUTrack(stream.getId());
                                isSet = true;
                                break;
                            }
                        }else {
                            if (stream.getType() == Models.StreamType.Stream_Sub){//大码流
                                roomClient.setMCUTrack(stream.getId());
                                isSet = true;
                                break;
                            }
                        }
                    }
                }
                if (!isSet){//如果没找到设置，默认取第一轨道
                    roomClient.setMCUTrack(1);
                }
                break;
        }
    }

    //初始化API
    private void initVcsApi() {
        Log.e("seminarMode:",meetingBean.isSeminarMode() + "");
        //开启track 调试
        if (openDebug) {
            roomClient = new RoomClient(this, roomSdkNo, trackServer);
        } else {
            roomClient = new RoomClient(this, roomSdkNo, "", true, meetingBean.isSeminarMode());
        }
        roomClient.setRoomEvent(this);//设置会议回调
//        roomClient.setAudioCurrentModeCallback(new IAudioModeListener() {
//            @Override
//            public void OnAudioModeInfo(int i) {
//                Log.e("3333333333", "setAudioCurrentModeCallback:  " + i);
//            }
//        });

        /**
         * 手机版本：vcs：1.2.5-stream-status-1
         * 盒子版本：vcs-tv-fh:1.0.6-86-stream-status-1
         * RoomClient新增流媒体状态回调设置方法
         */
        roomClient.setStreamStatusEvent(new StreamStatusEvent() {
            @Override
            public void onStreamConnected(boolean connect) {//入会后第一次连接状态回调,true:成功 false:失败

            }

            @Override
            public void onStreamDisconnect() {//中途断开

            }

            @Override
            public void onStreamReconnected() {//发生了重连

            }
        });

        roomClient.setAudioStatusCallbackGap(1000);
        roomClient.setAudioStatusCallback(new AudioStatusCallback() {
            @Override
            public void onAudioStatus(List<AudioStatusBean> data) {
                for (AudioStatusBean statusBean : data){
//                    Log.e("xxxxxxx", statusBean.toString());
                }
            }

            @Override
            public void onSpeechStatus(boolean speech) {

            }
        });

        shareAccId = UserConfig.getUserInfo().getData().getAccount().getId();

        roomClient.setAccount(
                UserConfig.getUserInfo().getData().getAccount().getId(),
                UserConfig.getUserInfo().getData().getAccount().getRoom().getSdk_no(),
                UserConfig.getUserInfo().getData().getAccount().getName(),
                UserConfig.getUserInfo().getData().getAccount().getNickname(),
                UserConfig.getUserInfo().getData().getAccount().getPortrait());
        roomClient.setRoom(meetingBean.getRoom().getId(), roomSdkNo, meetingBean.getSession());
        roomClient.setStreamAddr(meetingBean.getStream_host(), meetingBean.getStream_port());

        roomClient.setMqttServerId(meetingBean.getMeeting_server_id(), meetingBean.getRoom().getId(), new MqttConnectStatusImpl() {
            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

            }

            @Override
            public void subscribeCtrlTopicFailure(String topic, Throwable throwable) {
            }

            @Override
            public void subscribeCtrlTopicSuccess(String topic) {
            }
        });

        roomClient.useMultiStream(isMulti);//设置开启多流
        roomClient.setMinEncoderSoft(false);//在setVideoOutput前设置
//        roomClient.setResolutionSize(videoW, videoH);
        //输出分辨率宽必须是16的倍数,高必须是2的倍数,否则容易出现绿边等问题
        //小码流高（会根据setVideoOutput设置的宽高自动计算宽，一定要放在setVideoOutput方法之前设置），码流，帧率
//        roomClient.setMinVideoOutput(360, 500, 15);
        roomClient.setVideoOutput(videoW, videoH, fps, bitRate);//设置视频分辨率宽高，帧率，码率Kb
        int sampleRate = 48000;
        roomClient.setAudioSampleRate(sampleRate);//设置采样率
        //自动增益
        int agc = 10000;
        //回音消除
        int aec = 12;
        roomClient.setAgcAec(agc, aec);//设置AGC,AEC
        roomClient.setFps(fps);//设置帧率
        roomClient.openCamera(null, isSendSelfVideo, this);//设置预览view
        roomClient.enableXDelay(true);//自适应延迟
        boolean hardDecoder = true;
        roomClient.useHwDecoder(hardDecoder);//是否硬解码

        selfMember = new MemberBean();
        selfMember.setAccountId(UserConfig.getUserInfo().getData().getAccount().getId());
        selfMember.setSdkNo(Integer.parseInt(UserConfig.getUserInfo().getData().getAccount().getRoom().getSdk_no()));
        selfMember.setMute(isSendSelfAudio ? Models.DeviceState.DS_Active : Models.DeviceState.DS_Closed);
        selfMember.setCloseVideo(isSendSelfVideo ? Models.DeviceState.DS_Active : Models.DeviceState.DS_Closed);

        mainWindowMember = selfMember;
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

        roomClient.open();
    }

    public void streamKickOut(String accId) {
        roomClient.streamKickOut(accId);
    }

    /**
     * 小窗口点击事件
     */
    private void clickWindow(MemberBean memberBean) {
        if (windowAdapter != null) {
            WindowAdapter.MyViewHolder holder = windowAdapter.getHolders().get(memberBean.getSdkNo());
            if (mainWindowMember == memberBean) {//点击的小窗口正显示在大窗口
                mainWindowMember = selfMember;
            } else {
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
    private void updateViewInfo() {
        if (mainWindowMember == null) {
            return;
        }

        nameTv.setText(mainWindowMember.getSdkNo() + "");
        closePreviewTv.setVisibility(mainWindowMember.isCloseVideo() ? View.VISIBLE : View.GONE);
        closePreviewTv.setText(mainWindowMember.getSdkNo() + "\n" + "视频已关闭");
    }

    /**
     * 更新小窗口的状态信息
     */
    private void updateViewInfoSmall(MemberBean memberBean, WindowAdapter.MyViewHolder holder) {
        if (holder == null) {
            return;
        }
        if (mainWindowMember != selfMember) {//大窗口不是预览，小窗口显示自己的
            holder.nameTv.setText(roomClient.getAccount().getStreamId() + "");
            holder.selfCloseTv.setText(roomClient.getAccount().getStreamId() + "\n" + "视频已关闭");
            holder.selfCloseTv.setVisibility(selfMember.isCloseVideo() ? View.VISIBLE : View.GONE);

            //大窗口设置大流
            Models.Account account = roomClient.getAccountList().get(mainWindowMember.getSdkNo());
            if (account != null) {
                if (account.getTerminalType() == Models.TerminalType.Terminal_Embedded) {
                    for (Models.Stream stream : account.getStreamsList()) {
                        if (stream.getId() == 1) {//取id=1的流为默认流
                            roomClient.pickStream(account.getId(), stream.getChannel(), stream.getChannelType(), Models.StreamType.Stream_Main);
                            break;
                        }
                    }
                } else {
                    roomClient.pickStreamMain(account.getId());
                }
            }
        } else {
            if (memberBean == null) {
                return;
            }
            holder.nameTv.setText(memberBean.getSdkNo() + "");
            holder.selfCloseTv.setText(memberBean.getSdkNo() + "\n" + "视频已关闭");
            holder.selfCloseTv.setVisibility(memberBean.isCloseVideo() ? View.VISIBLE : View.GONE);

            //恢复为小码流
            Models.Account account = roomClient.getAccountList().get(memberBean.getSdkNo());
            if (account != null) {
                if (account.getTerminalType() == Models.TerminalType.Terminal_Embedded) {//录播主机
                    for (Models.Stream stream : account.getStreamsList()) {
                        if (stream.getId() == 1) {
                            roomClient.pickStream(account.getId(), stream.getChannel(), stream.getChannelType(), Models.StreamType.Stream_Sub);
                            break;
                        }
                    }
                } else {
                    roomClient.pickStreamSub(account.getId());
                }
            }
        }
    }

    /**
     * 恢复原先小窗口的信息
     */
    private void updateViewInfoSmallOld(MemberBean memberBean) {
        if (memberBean == null) {
            return;
        }
        WindowAdapter.MyViewHolder holder = windowAdapter.getHolders().get(memberBean.getSdkNo());
        if (holder != null) {
            holder.nameTv.setText(memberBean.getSdkNo() + "");
            holder.selfCloseTv.setText(memberBean.getSdkNo() + "\n" + "视频已关闭");
            holder.selfCloseTv.setVisibility(memberBean.isCloseVideo() ? View.VISIBLE : View.GONE);
//            holder.textureView.setMirror(0);
        }
        //恢复小流
        Models.Account account = roomClient.getAccountList().get(memberBean.getSdkNo());
        if (account != null) {
            if (account.getTerminalType() == Models.TerminalType.Terminal_Embedded) {
                for (Models.Stream stream : account.getStreamsList()) {
                    if (stream.getId() == 1) {
                        roomClient.pickStream(account.getId(), stream.getChannel(), stream.getChannelType(), Models.StreamType.Stream_Sub);
                        break;
                    }
                }
            } else {
                roomClient.pickStreamSub(account.getId());
            }
        }
    }

    @Override
    protected void onDestroy() {
//        Utils.cancelNotification();

        if (roomClient != null) {
            roomClient.close();//退出释放
        }
        if (mForegroundService != null){
            stopService(mForegroundService);
        }
        super.onDestroy();
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

    //掩码方式
    public void useChannel(int sdkNo, int mask) {
        roomClient.setStreamTrack(sdkNo, mask);
//        roomClient.pickStreamMain(sdkNo);
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

    public void changeSize() {
        int width, height;

        if (isLand) {
            height = (DisplayUtil.getInstance().getMobileWidth(this) / spanCount) * 9 / 16;
        } else {
            height = (DisplayUtil.getInstance().getMobileWidth(this) / spanCount) * 16 / 9;
        }
        width = DisplayUtil.getInstance().getMobileWidth(this) / spanCount;

        for (WindowAdapter.MyViewHolder holder : windowAdapter.getHolders().values()) {
            holder.frameLayout.setLayoutParams(new ConstraintLayout.LayoutParams(width, height));
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        isLand = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE;

        changeSize();
    }

    Intent floatIntent;
    public boolean isLand = true;

    /**
     * 跳转画中画权限界面
     */
    public static void startPiPSettingsActivity(Context context) {
        try {
            context.startActivity(new Intent("android.settings.PICTURE_IN_PICTURE_SETTINGS", Uri.parse("package:" + context.getPackageName())));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 进入画中画模式
     */
    private PictureInPictureParams.Builder mPictureInPictureParamsBuilder;
    private void enterPiPMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (mPictureInPictureParamsBuilder == null) {
                mPictureInPictureParamsBuilder = new PictureInPictureParams.Builder();
            }
            // Calculate the aspect ratio of the PiP screen. 计算video的纵横比
//            mVideoWith = videoPlayer.getCurrentVideoWidth();
//            mVideoHeight = videoPlayer.getCurrentVideoHeight();
//            if (mVideoWith != 0 && mVideoHeight != 0) {
//                //设置param宽高比,根据宽高比例调整初始参数
//                Rational aspectRatio = new Rational(mVideoWith, mVideoHeight);
//                mPictureInPictureParamsBuilder.setAspectRatio(aspectRatio);
//            }
            //进入pip模式
            enterPictureInPictureMode(mPictureInPictureParamsBuilder.build());
        }
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
    @SuppressLint("SourceLockedOrientationActivity")
    @OnClick({R.id.send_msg_btn, R.id.camera_switch_btn,R.id.rec_btn,R.id.change_orientation_btn,
            R.id.camera_light_btn, R.id.close_self_video_btn, R.id.close_self_audio_btn,
            R.id.not_recv_audio_btn, R.id.not_recv_video_btn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.change_orientation_btn:
                if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {//当前是横屏
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//竖屏
                } else {//当前是竖屏
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//横屏
                }
                break;
            case R.id.rec_btn:
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    if (PermissionUtil.getInstance().isGrandFloating(this)) {
//                        startRecord();
//                    } else {
//                        PermissionUtil.getInstance().setPermissionFloat(this, success -> {
//                            //成功授权
//                            if (success) {
//                                startRecord();
//                            }
//                        });
//                    }
//                } else {
//                    //没有悬浮按钮
//                    startRecord();
//                }
                startRecord();
                break;
            case R.id.camera_light_btn://闪光灯开关
//                boolean isSupportPipMode = getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
//                if (isSupportPipMode){
//                    enterPiPMode();
//                }else {
//                    startPiPSettingsActivity(this);
//                    ToastUtil.getInstance().showShortToast("不支持画中画");
//                }
//                switchLight();
                roomClient.switchRouter(AudioRouterMenu.BLUETOOTH);
                break;
            case R.id.camera_switch_btn://切换摄像头
//                switchCamera();
                roomClient.switchRouter(AudioRouterMenu.SPEAKER);
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
//                roomClient.switchRouter(VCS_EVENT_TYPE.SPEAKER_ROUTER);
//                roomClient.sendChatMsg(null, "你好");
                roomClient.setSpeakerOn(true);
//                Log.e("ssssssssss", "startFocus:"+AudioFocusHelper.getInstance(this).startFocus());
                break;
        }
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode);
        if (isInPictureInPictureMode) {
            Log.e("aaaaaaa", "Hide the full-screen UI");

            // Hide the full-screen UI (controls, etc.) while in picture-in-picture mode.
        } else {
            // Restore the full-screen UI.
            Log.e("aaaaaaa", "Restore the full-screen UI");
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    //录屏
    /////////////////////////////////////////////////////////////////////////////////////////////////
    private void initRecording() {
        //这里采用编码方式 默认也采用编码方式，这时候流对应的track=2,camera 流没有停止，如果采用NoEncoderMode 模式 那么 录屏启动后 camera
        // 流将会被停止发送 这时候原来的track保持不变
        roomClient.initScreenRecorder(true,1200, 10, 1280, 720);
        //设置录屏时通知栏样式,shouldNotification = true时有效
        roomClient.setRecordNotification(R.mipmap.ic_launcher, "正在共享屏幕", "点击按钮结束录制", "停止录制");
        //设置录屏大小如果不采用编码方式那么这个大小应该和VCS_CreateVideoOutput 设置大小保持一致，编码方式那么就是自己設置【最大值1920x1080】

        roomClient.setScreenRecordListener((event, info) -> {
            Log.e("recording", "event:" + event + "   info:" + info + "   treahd:" + Thread.currentThread().getName());
            switch (event) {
                case VCS_EVENT_TYPE.ScreenRecordError:
//                    ToastUtil.getInstance().showLongToast(info);
                    break;
                case VCS_EVENT_TYPE.ScreenRecordStart:
                    if (recordingBtn.getText().equals("启动录屏")) {
                        startService(floatIntent);

                        ToastUtil.getInstance().showLongToast("开始录屏");
                        recordingBtn.setText("关闭录屏");
//                        moveTaskToBack(true);
                    }
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

    public void startRecord() {
        if (roomClient.isBusyRecording()) {
            roomClient.stopScreenRecording();//停止录屏
        } else {
            roomClient.startScreenRecording();//开始录屏
        }
    }

    boolean shot = false;

    @Override
    public void onPreviewFrame(byte[] yuv, int width, int height, long stamp, int format, int angle) {
        VcsPlayerGlTextureView vcsPlayerGlSurfaceView = getTargetSurfaceView(selfMember.getSdkNo());
        if (vcsPlayerGlSurfaceView != null) {
            vcsPlayerGlSurfaceView.update(width, height, format);
            vcsPlayerGlSurfaceView.update(yuv, format);
            vcsPlayerGlSurfaceView.setLANDSCAPE(angle);
            vcsPlayerGlSurfaceView.setCameraId(isFront ? 1 : 0);
        }
    }
}