package com.freewind.meetingdemo.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.freewind.meetingdemo.R;
import com.freewind.meetingdemo.adapter.MemberAdapter;
import com.freewind.meetingdemo.bean.MeetingBean;
import com.freewind.meetingdemo.common.Constants;
import com.freewind.meetingdemo.common.UserConfig;
import com.freewind.meetingdemo.util.DisplayUtil;
import com.freewind.meetingdemo.util.FloatingButtonService;
import com.freewind.meetingdemo.util.ForeService;
import com.freewind.meetingdemo.util.ToastUtil;
import com.freewind.vcs.CameraPreview;
import com.freewind.vcs.Models;
import com.freewind.vcs.RoomClient;
import com.freewind.vcs.RoomEvent;
import com.freewind.vcs.RoomServer;
import com.freewind.vcs.impl.MqttConnectStatusImpl;
import com.ook.android.VCS_EVENT_TYPE;
import com.ook.android.ikPlayer.VcsPlayerGlTextureView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class Meeting2Activity extends PermissionActivity implements RoomEvent, CameraPreview {

    RoomClient roomClient;
    @BindView(R.id.close_self_video_btn)
    Button closeSelfVideoBtn;
    @BindView(R.id.close_self_audio_btn)
    Button closeSelfAudioBtn;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.layout_meeting_cl)
    LinearLayout meetingLayoutCl;
    @BindView(R.id.members_rv)
    RecyclerView memberRv;
    @BindView(R.id.contentFl)
    FrameLayout contentFl;
    @BindView(R.id.member_player)
    VcsPlayerGlTextureView playerBig;
    @BindView(R.id.member_nick_tv)
    TextView nickBig;
    @BindView(R.id.member_mic_iv)
    ImageView micBig;
    @BindView(R.id.member_video_iv)
    ImageView videoBig;

    private int videoH = 480;
    private int videoW = 640;
    private int fps = 15;//如果视频源来自摄像头，24FPS已经是肉眼极限，所以一般20帧的FPS就已经可以达到很好的用户体验了
    private int bitRate = 512;

    private boolean isFront = true;//是否前置
    private boolean isSendSelfVideo = true;//是否发送自己的视频
    private boolean isSendSelfAudio = true;//是否发送自己的音频

    private String trackServer = "";
    private boolean openDebug = false;
    private boolean isMulti = false;
    private int roomSdkNo;
    private static String TAG = "4444444444";

    private MeetingBean meetingBean;

    private int agc = 10000;//自动增益
    private int aec = 12;//回音消除
    private int sampleRate = 48000;

    public Models.Account mainWindowMember;//保存在主窗口的成员的信息
    Intent mForegroundService;

    MemberAdapter memberAdapter = new MemberAdapter(new ArrayList<>());

    @Override
    public void onEnter(int result) {
        //如果result != 0 则表示服务器上的token已经失效，需要进行重新进入房间的逻辑
        Log.e(TAG, "onEnter:" + result + "");
    }

    @Override
    public void onExit(int result) {
        Log.e(TAG, "你离开了会议室 result:" + result);
    }

    @Override
    public void onNotifyRoom(Models.Room room) {}

    @Override
    public void onNotifyKickOut(RoomServer.KickoutNotify kickoutNotify) {}

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
        Log.e(TAG, "onNotifyEnter: 有人进入房间" + "  streamId: " + account.getStreamId());
        roomClient.pickMember(account.getStreamId(), true);
        memberAdapter.addAccount(account);
    }

    @Override
    public void onNotifyExit(Models.Account account) {
        Log.e("5555555", "onNotifyExit: 有人离开房间" + "  streamId: " + account.getStreamId());
        roomClient.pickMember(account.getStreamId(), false);
        memberAdapter.removeAccount(account);
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
    public void onFrame(byte[] ost, byte[] tnd, byte[] trd, int width, int height, int format, int streamId, int mask, int label) {
        Log.e("3333333333", "onFrame  " + "  clientId: " + streamId + "   " + width + " " + height + "   mask:" + mask);
        VcsPlayerGlTextureView vcsPlayerGlTextureView = getTargetSurfaceView(streamId);
        if (vcsPlayerGlTextureView != null) {
            vcsPlayerGlTextureView.update(width, height, format);
            vcsPlayerGlTextureView.update(ost, tnd, trd, format, label);
        }
    }

    /**
     * 获取对应播放器
     * @param streamId streamId
     * @return  对应渲染控件
     */
    private VcsPlayerGlTextureView getTargetSurfaceView(int streamId){
        VcsPlayerGlTextureView vcsPlayerGlTextureView;
        if (mainWindowMember != null && mainWindowMember.getStreamId() == streamId) {
            vcsPlayerGlTextureView = playerBig;
        } else {
            if (streamId == roomClient.getAccount().getStreamId()){
                vcsPlayerGlTextureView = memberAdapter.getPlayerByStreamId(mainWindowMember.getStreamId());
            }else {
                vcsPlayerGlTextureView = memberAdapter.getPlayerByStreamId(streamId);
            }
        }
        return vcsPlayerGlTextureView;
    }

    @Override
    public void onSendInfo(String info) {
        //delay: 移动40-45， wifi 30  有线 20
        // 60000270::delay=17 status=1 speed=687 buffer=0 overflow=0 */
        // 60000270=id, delay=上传到服务器之间的延迟时间,越大越不好, status=-1上传出错 >=0正常, speed=发送速度 buffer=缓冲包0-4正常 */
    }

    /**
     * {
     * "recvinfo": [
     * {
     * "linkid": 12340001,  对方streamId
     * "recv": 4127 接收包信息
     * "comp": 13,  补偿 高 网络不稳定
     * "losf": 0,   丢失包信息  高 就是网络差
     * "lrl": 6.8, //短时端到端丢包率（对方手机到你手机）
     * "lrd": 8.9 //短时下行丢包率（服务器到你）
     * }]}
     */
    @Override
    public void onRecvInfo(String info) {
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

        memberAdapter.setData(findIndexInList(account.getId()), account);
    }
    @Override
    public void onNotifyMyAccount(RoomServer.MyAccountNotify notify, boolean fromRoomEnter) {
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
    public void onNotifyStreamChanged(RoomServer.StreamNotify streamNotify) {}

    //透传消息
    @Override
    public void onNotifyPassThough(RoomServer.PassthroughNotify passthroughNotify) {}

    @Override
    public void onNotifyHostCtrlStream(RoomServer.HostCtrlStreamNotify hostCtrlStreamNotify) {}

    /**
     * 网络丢包状态，网络差的时候才会回调
     * 0    0% - 8%
     * -1   8% - 15%
     * -2   15% - 30%
     * -3   >=30% 丢包
     */
    @Override
    public void onRecvStatus(int i, int streamId) {
        Log.e("onRecvStatus", streamId + "   " + i);
    }

    @Override
    public void onTestSpeed(String s) {}

    @Override
    public void onNotifyChat(RoomServer.ChatNotify chatNotify) {}

    @Override
    public void onNotifyHandUp(RoomServer.HandUpNotify handUpNotify) {}

    @Override
    public void onMcuRunStateNotify(RoomServer.McuRunStateNotify mcuRunStateNotify) {}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    LinearLayout.LayoutParams porParams;//竖屏参数
    LinearLayout.LayoutParams landParams;//横屏参数

    @Override
    public void onCreate() {
        super.onCreate();
        floatIntent = new Intent(Meeting2Activity.this, FloatingButtonService.class);

        initView();
        initVcsApi();

        mForegroundService = new Intent(this, ForeService.class);
        // Android 8.0使用startForegroundService在前台启动新服务
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            startForegroundService(mForegroundService);
        } else {
            startService(mForegroundService);
        }
    }

    /**
     * 计算横竖屏时的布局参数
     */
    private void calcParams(){
        int screenPhysicsWidth;//手机竖屏下宽度，短边
        int porHeight;//竖屏时视频区域的高度
        Configuration configuration = new Configuration();
        isLand = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        if (isLand){
            screenPhysicsWidth = DisplayUtil.getInstance().getMobileHeight(this);
            configuration.orientation = Configuration.ORIENTATION_LANDSCAPE;
        }else {
            screenPhysicsWidth = DisplayUtil.getInstance().getMobileWidth(this);
            configuration.orientation = Configuration.ORIENTATION_PORTRAIT;
        }
        porHeight = screenPhysicsWidth * 9 / 16;

        porParams  = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, porHeight);
        landParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        onConfigurationChanged(configuration);//触发布局设置
    }

    private int findIndexInList(String id){
        int index = -1;
        int size = memberAdapter.getData().size();
        List<Models.Account> list = memberAdapter.getData();
        for (int i = 0; i < size; i++){
            if (list.get(i).getId().equals(id)){
                index = i;
                break;
            }
        }
        return index;
    }

    private void initView() {
        setContentView(R.layout.activity_meeting2);
        ButterKnife.bind(this);

        calcParams();

        Intent intent = getIntent();
        meetingBean = (MeetingBean) intent.getSerializableExtra(Constants.ROOM_INFO);
        roomSdkNo = Integer.parseInt(meetingBean.getSdk_no());
        trackServer = intent.getStringExtra(Constants.DEBUG_ADDR);
        openDebug = intent.getBooleanExtra(Constants.DEBUG_SWITCH, false);
        isMulti = intent.getBooleanExtra(Constants.MULTI, false);
        agc = Integer.parseInt(intent.getStringExtra(Constants.AGC));
        aec = Integer.parseInt(intent.getStringExtra(Constants.AEC));
        fps = Integer.parseInt(intent.getStringExtra(Constants.FPS));
        isSendSelfVideo = !intent.getBooleanExtra(Constants.CLOSE_SELF_VIDEO, true);
        isSendSelfAudio = !intent.getBooleanExtra(Constants.CLOSE_SELF_AUDIO, true);

        sampleRate = intent.getIntExtra(Constants.SAMPLE_RATE, 48000);

        memberRv.setAdapter(memberAdapter);
        memberAdapter.setEmptyView(R.layout.layout_empty);
        memberAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                clickWindow((Models.Account) adapter.getData().get(position));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (roomClient != null){
            roomClient.onResumeCamera();
        }
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

        roomClient.setAccount(
                UserConfig.getUserInfo().getData().getAccount().getId(),
                UserConfig.getUserInfo().getData().getAccount().getRoom().getSdk_no(),
                UserConfig.getUserInfo().getData().getAccount().getName(),
                UserConfig.getUserInfo().getData().getAccount().getNickname(),
                UserConfig.getUserInfo().getData().getAccount().getPortrait());
        roomClient.setRoom(meetingBean.getRoom().getId(), roomSdkNo, meetingBean.getSession());
        roomClient.setStreamAddr(meetingBean.getStream_host(), meetingBean.getStream_port());
        roomClient.setMeetingAddr(meetingBean.getMeeting_host(), meetingBean.getMeeting_port());

        roomClient.setMqttServerId(meetingBean.getMeeting_server_id(), meetingBean.getRoom().getId(), new MqttConnectStatusImpl() {
            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void connectComplete(boolean reconnect, String ip) {
                if (reconnect){//发生了断线重连
                    // TODO: 2023/5/11  清空列表等等
                    memberAdapter.setList(new ArrayList<>());
                }
            }

            @Override
            public void subscribeCtrlTopicFailure() {

            }

            @Override
            public void subscribeCtrlTopicSuccess() {

            }
        });

        roomClient.setSoft3A(true);//音频优化

        roomClient.useMultiStream(isMulti);//设置开启多流
        roomClient.setMinEncoderSoft(false);//在setVideoOutput前设置

        roomClient.setResolutionSize(videoW, videoH);//预览分辨率
        //输出分辨率宽必须是16的倍数,高必须是2的倍数,否则容易出现绿边等问题
        roomClient.setVideoOutput(videoW, videoH, fps, bitRate);//设置视频分辨率宽高，帧率，码率
        //小码流高（会根据setVideoOutput设置的宽高自动计算宽，一定要放在setVideoOutput方法之前设置），码流，帧率
//        roomClient.setMinVideoOutput(360, 500, 15);
        roomClient.setAudioSampleRate(sampleRate);//设置采样率
        roomClient.setAgcAec(agc, aec);//设置AGC,AEC
        roomClient.setFps(fps);//设置帧率

        roomClient.openCamera(null, this);//设置预览view

        roomClient.enableXDelay(true);//自适应延迟
        roomClient.useHwDecoder(true);//是否硬解码

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
        } else {
            roomClient.setDefaultSendSelfVideo(false);
            closeSelfVideoBtn.setText("打开自己的视频");
        }
        micBig.setSelected(isSendSelfAudio);
        videoBig.setSelected(isSendSelfVideo);
        nickBig.setText(roomClient.getAccount().getNickname());

        mainWindowMember = roomClient.getAccount();

        roomClient.open();
    }

    /**
     * 小窗口点击事件
     */
    private void clickWindow(Models.Account account) {
        // TODO: 2023/10/12 窗口点击事件
    }


    @Override
    protected void onDestroy() {
        if (roomClient != null) {
            roomClient.close();//退出释放
        }
        if (mForegroundService != null){
            stopService(mForegroundService);
        }
        super.onDestroy();
    }

    //设置自己的视频，是否发送
    private void closeSelfVideo(boolean isFromNotify, Models.DeviceState fromNotifyState) {
        if (isFromNotify) {//来自会控操作
            roomClient.enableSendVideo(fromNotifyState);
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
            } else {
                roomClient.enableSendVideo(Models.DeviceState.DS_Closed);
            }
        }

        if (mainWindowMember.getStreamId() == roomClient.getAccount().getStreamId()) {
            videoBig.setSelected(isSendSelfVideo);
        } else {
            // TODO: 2023/10/12 自己不在主窗口时的显示
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
            } else {
                roomClient.enableSendAudio(Models.DeviceState.DS_Closed);
            }
        }

        if (mainWindowMember.getStreamId() == roomClient.getAccount().getStreamId()) {
            micBig.setSelected(isSendSelfAudio);
        } else {
            // TODO: 2023/10/12 自己不在主窗口时的显示
        }

        if (isSendSelfAudio) {
            closeSelfAudioBtn.setText("关闭自己的音频");
        } else {
            closeSelfAudioBtn.setText("打开自己的音频");
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        isLand = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE;

        if (isLand){
            meetingLayoutCl.setLayoutParams(landParams);
            contentFl.setVisibility(View.GONE);
        }else {
            meetingLayoutCl.setLayoutParams(porParams);
            contentFl.setVisibility(View.VISIBLE);
        }
    }

    Intent floatIntent;
    public boolean isLand = true;

    @OnClick({ R.id.close_self_video_btn, R.id.close_self_audio_btn, R.id.change_orientation_btn})
    public void onClick(View view) {
        switch (view.getId()) {
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
        }
    }

    @Override
    public void onPreviewFrame(byte[] yuv, int width, int height, long stamp, int format, int angle) {
        VcsPlayerGlTextureView vcsPlayerGlSurfaceView = getTargetSurfaceView(roomClient.getAccount().getStreamId());
        if (vcsPlayerGlSurfaceView != null) {
            vcsPlayerGlSurfaceView.update(width, height, format);
            vcsPlayerGlSurfaceView.update(yuv, format);
            vcsPlayerGlSurfaceView.setLANDSCAPE(angle);
            vcsPlayerGlSurfaceView.setCameraId(isFront ? 1 :0);
        }
    }
}