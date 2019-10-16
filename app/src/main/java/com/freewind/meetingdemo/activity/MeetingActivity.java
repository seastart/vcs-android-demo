package com.freewind.meetingdemo.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.freewind.meetingdemo.MyApplication;
import com.freewind.meetingdemo.PB.Models;
import com.freewind.meetingdemo.PB.RoomClient;
import com.freewind.meetingdemo.PB.RoomEvent;
import com.freewind.meetingdemo.PB.RoomServer;
import com.freewind.meetingdemo.PB.VcsException;
import com.freewind.meetingdemo.R;
import com.freewind.meetingdemo.adapter.WindowAdapter;
import com.freewind.meetingdemo.bean.MeetingBean;
import com.freewind.meetingdemo.bean.MemberBean;
import com.freewind.meetingdemo.common.Constants;
import com.freewind.meetingdemo.common.UserConfig;
import com.freewind.meetingdemo.util.DisplayUtil;
import com.freewind.meetingdemo.util.TextureVideoViewOutlineProvider;
import com.ook.android.CheckPermissionsUtil;
import com.ook.android.ErrListener;
import com.ook.android.GLCameraView;
import com.ook.android.IVCSCB;
import com.ook.android.VCS_EVENT_TYPE;
import com.ook.android.VcsApi;
import com.ook.android.realPreviewSize;

import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * author superK
 * update_at 2019/7/30
 * description
 */
public class MeetingActivity extends AppCompatActivity implements View.OnClickListener, IVCSCB, ErrListener, realPreviewSize, RoomEvent {
    static final int GET_FRAME = 1;

    RoomClient roomClient;
    VcsApi apiCtrl;
    private GLCameraView cameraSurfaceView;

    Button lightBtn;
    Button switchBtn;
    Button closeSelfVideoBtn;
    Button closeSelfAudioBtn;
    Button sendMsgBtn;
    Button clearMsgBtn;
    TextView customMsgTv;
    RecyclerView windowRcView;
    TextView clientTv, losTv, uploadTv;

    private int videoW = 1280;
    private int videoH = 720;

    private boolean isLight = false;//是否打开闪光灯
    private boolean isFront = true;//是否前置
    private boolean isSendSelfVideo = true;//是否发送自己的视频
    private boolean isCloseSelfAudio = false;//是否关闭发送自己的音频

    String szSessionId = "";//进入的授权码，通过平台API接口获取

    private String roomName = "";
    private String trackServer = "";
    private boolean openDebug = false;
    private int room;
    private int myClientId;
    private static String TAG = "kkp";
    private boolean mWorking = false;
    private Thread mThread;

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

    private String addr = "";
    private int port = 8006;
    private int agc = 10000;
    private int aec = 10;
    private int sampleRate = 22050;

    private final int fps = 25;
    private final int bitRate = 800;

    private boolean hardDecoder = false;
    private boolean autoBitrate = false;

    private WindowAdapter windowAdapter;

    protected static final int TIME_COUNT = 12;

    @Override
    public void onEnter(int result) {
        Log.e("444444444444", "111");

    }

    @Override
    public void onExit(int result) {
        Log.e("444444444444", "222");

    }

    @Override
    public void onNotifyRoom(Models.Room room) {
        Log.e("444444444444", apiCtrl.VCS_getVersion());
    }

    @Override
    public void onNotifyAccount(Models.Account account) {
        Log.e("444444444444", "33");

    }

    @Override
    public void onNotifyKickout() {
        Log.e("444444444444", "44");

    }

    @Override
    public void onNotifyEnter(Models.Account account) {
        Log.e("444444444444", "555");

    }

    @Override
    public void onNotifyExit(Models.Account account) {
        Log.e("444444444444", "666");

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
        Log.e("444444444444", "client " + clientId);

    }

    @Override
    public void onSendInfo(int speed, int delay) {
        Log.e("444444444444", speed + "");

    }

    @Override
    public void onRecvInfo(String s) {
        Log.e("444444444444", s + "   sss");

    }

    @Override
    public void onXBitrate(int level) {
        Log.e("444444444444", "1414");

    }

    @Override
    public void onNotifyMyAccount(RoomServer.MyAccountNotify notify) {
        Log.e("444444444444", "1515");

    }

    //内部静态handler，避免内存泄漏，建议写法
    private static class MyHandler extends Handler {
        private final WeakReference<MeetingActivity> mActivity;

        MyHandler(MeetingActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final MeetingActivity activity = mActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case TIME_COUNT:
                        if (activity.apiCtrl != null){
                            activity.apiCtrl.vcs_GetUploadStatus();
                            activity.apiCtrl.vcs_GetRecvStatus();
                        }
                        sendEmptyMessageDelayed(TIME_COUNT, 1000);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private MyHandler mHandler = new MyHandler(this);

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

        mHandler.sendEmptyMessageDelayed(TIME_COUNT, 1000);
    }

    private void initView() {
        lightBtn = findViewById(R.id.camera_light_btn);
        switchBtn = findViewById(R.id.camera_switch_btn);
        closeSelfVideoBtn = findViewById(R.id.close_self_video_btn);
        closeSelfAudioBtn = findViewById(R.id.close_self_audio_btn);
        sendMsgBtn = findViewById(R.id.send_msg_btn);
        cameraSurfaceView = findViewById(R.id.cameraSurfaceView);
        customMsgTv = findViewById(R.id.custom_msg_tv);
        windowRcView = findViewById(R.id.window_rcview);
        clientTv = findViewById(R.id.client_tv);
        clearMsgBtn = findViewById(R.id.clear_msg_btn);
        uploadTv = findViewById(R.id.upload_tv);
        losTv = findViewById(R.id.los_tv);

        customMsgTv.setMovementMethod(ScrollingMovementMethod.getInstance());

        lightBtn.setOnClickListener(this);
        switchBtn.setOnClickListener(this);
        closeSelfVideoBtn.setOnClickListener(this);
        closeSelfAudioBtn.setOnClickListener(this);
        sendMsgBtn.setOnClickListener(this);
        clearMsgBtn.setOnClickListener(this);

        Intent intent = getIntent();
        meetingBean = (MeetingBean) intent.getSerializableExtra(ROOM_INFO);
        addr = meetingBean.getStream_host();
        port = meetingBean.getStream_port();
        roomName = meetingBean.getSdk_no();
        trackServer = intent.getStringExtra(DEBUG_ADDR);
        szSessionId = meetingBean.getSession();
        openDebug = intent.getBooleanExtra(DEBUG_SWITCH, false);
        agc = Integer.valueOf(intent.getStringExtra(AGC));
        aec = Integer.valueOf(intent.getStringExtra(AEC));
        closeOtherAudio = intent.getBooleanExtra(CLOSE_OTHER_AUDIO, false);
        closeOtherVideo = intent.getBooleanExtra(CLOSE_OTHER_VIDEO, false);
        sampleRate = intent.getIntExtra(SAMPLE_RATE, 22050);

        hardDecoder = intent.getBooleanExtra(HARD_DECODER, false);

        room = Integer.parseInt(roomName);

        windowAdapter = new WindowAdapter(MeetingActivity.this);
        windowRcView.setAdapter(windowAdapter);
        windowRcView.getRecycledViewPool().setMaxRecycledViews(0, 0);

        cameraSurfaceView.setOutlineProvider(new TextureVideoViewOutlineProvider(DisplayUtil.getInstance().dip2px(4)));
        cameraSurfaceView.setClipToOutline(true);

    }

    //初始化API
    private void initVcsApi() {

        apiCtrl = new VcsApi(this);
        apiCtrl.VCS_RegisterSDK();//必须进行Register 再初始化SDK


        //开启track 调试
        if (openDebug && trackServer.length() > 4) {
            Log.e(TAG, "open track " + trackServer);
            apiCtrl.VCS_openTrace(trackServer, 3);
        }

        //初始化SDK
        if (apiCtrl.VCS_Init() < VCS_EVENT_TYPE.VCS_R_OK) {
            showToast("初始化失败");
            return;
        }
        Log.e("kkp", apiCtrl.VCS_getVersion());

        myClientId = Integer.valueOf(meetingBean.getSdk_no());

        clientTv.setText(myClientId + "");


        roomClient = new RoomClient(apiCtrl);
        roomClient.setRoomEvent(this);


        Models.Account.Builder accountBuilder = Models.Account.newBuilder();
        accountBuilder.setId(UserConfig.getUserInfo().getData().getAccount().getId());
        accountBuilder.setStreamId(myClientId);
        roomClient.setAccount(accountBuilder.build());

        Models.Room.Builder roomBuilder = Models.Room.newBuilder();
        roomBuilder.setId(meetingBean.getRoom().getId());
        roomBuilder.setSdkNo(myClientId + "");
        roomClient.setRoom(roomBuilder.build());

        roomClient.setSessionId(meetingBean.getSession());
        roomClient.setStreamAddr(meetingBean.getStream_host());
        roomClient.setStreamPort(meetingBean.getStream_port());
        roomClient.setMeetingPort(meetingBean.getMeeting_port());
        roomClient.setMeetingAddr(meetingBean.getMeeting_host());

        //设置事件回调
//        apiCtrl.VCS_SetRoomEvent(room, this, this);
        //设置错误监听事件
//        apiCtrl.VCS_SetEncodeEvent(this);
        //设置数据回调
//        apiCtrl.VCS_SetPicDataEvent(room, this, this);
        //设置系统AGC
        apiCtrl.VCS_SetOutputAgc(room, agc);
        //设置系统AEC
        apiCtrl.VCS_SetOutputAec(room, aec > 32000 ? 12 : 10);

        //创建房间
        apiCtrl.VCS_CreateRoom(addr, port, room + "", szSessionId);
        //设置延时网络抖动,级别效果参考文档说明
        apiCtrl.VCS_SetRoomPlc(room, 1);
        // apiCtrl.setPreViewSize(1280,720);//设置预期预览  如果设置 将找到最接近的分辨率 不设置 将采用全屏最合适分辨率
        apiCtrl.setDefaultBackCamera(false);//是否默认后置相机
        apiCtrl.setCameraFPS(fps);
        apiCtrl.VCS_OpenCamera(cameraSurfaceView);//开启后置相机
        apiCtrl.VCS_SetRealSizeListen(this);

        apiCtrl.VCS_SetRoomXdelay(true);
        if (hardDecoder){
            if (apiCtrl.getSupportHw() == 1){//是否支持，1支持
                apiCtrl.useHwDecoder(true);//使用硬解码
                showToast("当前使用硬解码");
            }
        }else {
            apiCtrl.useHwDecoder(false);//不使用硬解码

        }
        apiCtrl.VCS_setAudioSamplerate(sampleRate);//设置采样率
        apiCtrl.VCS_setAudioChannels(1);

        apiCtrl.VCS_CreateAudioOutput(22000);
        apiCtrl.VCS_FrequencyShifter(1);
        apiCtrl.VCS_SetOutPutVad(room, 1);//VAD
        apiCtrl.VCS_SteOutputDen(room,1);//DEN

        if (!isSendSelfVideo && isCloseSelfAudio){
            apiCtrl.VCS_SendPause(1);
        }

        try {
            roomClient.open();
        } catch (VcsException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


//        //加入房间
//        if (apiCtrl.VCS_JoniRoom(room, myClientId) < VCS_EVENT_TYPE.VCS_R_OK) {
//            showToast("进入房间失败");
//            return;
//        }

        if (autoBitrate){
            apiCtrl.VCS_SetRoomXBitrate(room, 5);//0-关闭自适应；开启[>=3] 建议5秒 在移动网络情况下 建议>=5
        }else {
            apiCtrl.VCS_SetRoomXBitrate(room, 0);//0-关闭自适应；开启[>=3] 建议5秒 在移动网络情况下 建议>=5
        }
        apiCtrl.VCS_setVolumePlayer(getVolue(100));
        GLstart();
    }

    //0-100
    private int getVolue(int percent) {
        int vol;
        if (percent > 30) {
            vol = (100 - percent) * -20;
        } else if (percent > 25) {
            vol = (100 - percent) * -22;
        } else if (percent > 20) {
            vol = (100 - percent) * -25;
        } else if (percent > 15) {
            vol = (100 - percent) * -28;
        } else if (percent > 10) {
            vol = (100 - percent) * -30;
        } else if (percent > 5) {
            vol = (100 - percent) * -34;
        } else if (percent > 3) {
            vol = (100 - percent) * -37;
        } else if (percent > 0) {
            vol = (100 - percent) * -40;
        } else {
            vol = (100 - percent) * -100;
        }
        return vol;
    }

    public void GLstart() {
        mWorking = true;
        if (mThread != null && mThread.isAlive()) {
            Log.e("kkp", "start: thread is alive");
        } else {
            mThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (mWorking) {
                        SystemClock.sleep(1000 / fps);
                        if (apiCtrl != null) {
                            apiCtrl.VCS_getClientFrame();
                        }
                    }
                }
            });
            mThread.start();
        }
    }

    public void GLstop() {
        if (mWorking) {
            mWorking = false;
        }
    }

    private void releaseSDK() {
        roomClient.close();
        GLstop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //退出释放
        releaseSDK();
    }

    //回调
    @Override
    public void vcs_Cbf_RoomeVent(int iEvent, int lparam, int wparam, String ptr) {
        Log.e(TAG, "vcs_Cbf_RoomeVent iEvent: " + iEvent + "  lparam: " + lparam + "  wparam: " + wparam + "  ptr：" + ptr);
        switch (iEvent) {
            case VCS_EVENT_TYPE.VIDEO_PUSH_INIT:
                if (lparam > 0 && wparam > 0) {
                    windowAdapter.setWidthAndHeight(lparam, wparam);
                }
                break;
            case VCS_EVENT_TYPE.VCS_USER_JOIN_IN://有人进入房间
                Log.e(TAG, "event: 有人进入房间" + "  lparam: " + lparam + "  wparam: " + wparam + "  ptr：" + ptr);
                MemberBean memberBean = new MemberBean();
                memberBean.setClientId(wparam + "");
                if (closeOtherVideo){
                    memberBean.setCloseVideo(true);
                    apiCtrl.vcs_EnableRecvVideo(room, wparam, 1);
                }
                if (closeOtherAudio){
                    memberBean.setMute(true);
                    apiCtrl.vcs_EnableRecvAudio(room, wparam, 1);
                }
                if (windowAdapter.getMemberList().isEmpty()) {
                    windowAdapter.addItem(memberBean);
                } else {
                    int count = 0;
                    for (MemberBean s : windowAdapter.getMemberList()) {
                        if (s.getClientId().equals("" + wparam)) {
                            count++;
                            break;
                        }
                    }
                    if (count == 0) {
                        windowAdapter.addItem(memberBean);
                    }
                }

                break;
            case VCS_EVENT_TYPE.VCS_USER_EVENT_OUT://有人离开房间
                Log.e(TAG, "event: 有人离开房间" + "  lparam: " + lparam + "  wparam: " + wparam + "  ptr：" + ptr);

                if (!windowAdapter.getMemberList().isEmpty()) {
                    for (MemberBean s : windowAdapter.getMemberList()) {
                        if (s.getClientId().equals(wparam + "")) {
                            windowAdapter.removeItem(wparam + "");
                            break;
                        }
                    }
                }
                break;
            case VCS_EVENT_TYPE.VCS_XBITRATE://码率自适应状态提示
                Log.e(TAG, "event: 码率自适应状态提示" + "  lparam: " + lparam + "  wparam: " + wparam + "  ptr：" + ptr);
                switch (lparam) {
                    case VCS_EVENT_TYPE.VCS_START_XBITRATE://自适应模式启动
                        break;
                    case VCS_EVENT_TYPE.VCS_BITRATE_RECOVERED://恢复码率
                        break;
                    case VCS_EVENT_TYPE.VCS_BITRATE_HALF_BITRATE://降为1/2
                        break;
                    case VCS_EVENT_TYPE.VCS_BITRATE_QUARTER_BITRATE://降为1/4
                        break;
                }
                break;
            case VCS_EVENT_TYPE.VCS_CONNECT_EVENT://连接事件
                Log.e(TAG, "event: 连接事件" + "  lparam: " + lparam + "  wparam: " + wparam + "  ptr：" + ptr);
                switch (lparam) {
                    case VCS_EVENT_TYPE.VCS_UPLOAD_STATUS_NOMACFOUND:
                        break;
                    case VCS_EVENT_TYPE.VCS_UPLOAD_STATUS_PACKERFAIL:
                        break;
                    case VCS_EVENT_TYPE.VCS_UPLOAD_STATUS_DNSERROR:
                        break;
                    case VCS_EVENT_TYPE.VCS_UPLOAD_STATUS_INITING:
                        break;
                    case VCS_EVENT_TYPE.VCS_DISCONNECT://断开连接
                        break;
                    case VCS_EVENT_TYPE.VCS_CONNECTING://正在连接...
                        break;
                    case VCS_EVENT_TYPE.VCS_CONNECTED://已连接
                        if (getIntent().getBooleanExtra(CLOSE_SELF_VIDEO, false)){
                            closeSelfVideo();
                        }else{
                            apiCtrl.VCS_EnableSendVideo(room, myClientId, Constants.SEND_VIDEO_OR_AUDIO);
                        }
                        if (getIntent().getBooleanExtra(CLOSE_SELF_AUDIO, false)){
                            closeSelfAudio();
                        }else{
                            apiCtrl.VCS_EnableSendAudio(room, myClientId, Constants.SEND_VIDEO_OR_AUDIO);
                        }
                        break;
                    case VCS_EVENT_TYPE.VCS_CONNECT_FAIL://连接失败
                        break;
                }
                break;
            case VCS_EVENT_TYPE.VCS_MESSAGE://消息事件
                Log.e(TAG, "event: 消息事件" + "  lparam: " + lparam + "  wparam: " + wparam + "  ptr：" + ptr);
                customMsgTv.append(ptr + "\n");
                break;
            case VCS_EVENT_TYPE.UPLOAD_REPT_STATIST://上传流统计
                Log.e(TAG, "event: 上传流统计" + "  lparam: " + lparam + "  wparam: " + wparam + "  ptr：" + ptr);
                // 60000270::delay=17 status=1 speed=687 buffer=0 overflow=0 */
                // 60000270=id, delay=上传到服务器之间的延迟时间,越大越不好, status=-1上传出错 >=0正常, speed=发送速度 buffer=缓冲包0-4正常 */
                uploadTv.setText("上传：" + ptr);
                break;
            case VCS_EVENT_TYPE.UPLOAD_REPT_RECVINF://接收统计
                Log.e(TAG, "event: 接收统计" + "  lparam: " + lparam + "  wparam: " + wparam + "  ptr：" + ptr);
                // 60000002::recv=10144 comp=505 losf=91 */
                // 60000002=对方id, recv=接收包信息, comp=补偿 正常0, losf=丢失包信息 */
                losTv.setText("丢包：" + ptr);
                break;
        }
    }

    @Override
    public void vcs_Pic_CallbackFrame(final byte[] OstData, final byte[] TndData, final byte[] TrdData, int width, int height, int fourcc, int clientId) {
        //建议使用缓冲
        Log.e(TAG, "====vcs_Pic_CallbackFrame " + "  clientId: " + clientId + "   " + width + " " + height);
        final WindowAdapter.MyViewHolder holder = windowAdapter.getHolders().get(clientId + "");
        if (holder != null && holder.meetingGLSurfaceView != null) {
            holder.meetingGLSurfaceView.update(width, height);
            holder.meetingGLSurfaceView.update(OstData, TndData, TrdData);
        }
    }

    @Override
    public void OnErrCb(int errCode, String msg) {
        Log.e(TAG, "OnErrCb: " + "  ErrCode: " + errCode + "  msg: " + msg);
    }

    @Override
    public void OnrealSize(int width, int height, boolean isFont) {
        if (apiCtrl != null) {
            //根据实际大小设置输出
            Log.e("main", "===========real size::====" + width + " x " + height);
            if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                apiCtrl.setRotation(0);//相机角度影响拍照
                apiCtrl.setDegree(0);//视频输出角度 //前置相机270
                apiCtrl.setMirror(false);//90° 270°
                //视频码率设置 width*height*pts pts:[3,5]
                //可以根据getRealWidth  getRealHeight进行等比率缩放 不等比缩放 那么 自动会添加黑边
                apiCtrl.VCS_CreateVideoOutput(videoW, videoH, fps, bitRate * 1024, 1);
            } else {
                apiCtrl.setRotation(0);//相机角度影响拍照
                if (isFont) {
                    Log.e("main", "前置");
                    apiCtrl.setDegree(270);//视频输出角度 //前置相机270
                    apiCtrl.setMirror(true);//90° 270°
                } else {

                    Log.e("main", "后置");
                    apiCtrl.setDegree(90);//视频输出角度 //前置相机270
                    apiCtrl.setMirror(false);//90° 270°
                }
                //可以根据getRealWidth  getRealHeight进行等比率缩放 不等比缩放 那么 自动会添加黑边
                apiCtrl.VCS_CreateVideoOutput(videoH, videoW, fps, bitRate * 1024, 1);
            }
        }
    }

    @Override
    public void capPicture(String s) {
        //截屏
    }

    //闪光灯开关
    private void switchLight() {
        if (isFront) {
            showToast("后置摄像头才能开启闪光灯");
            return;
        }
        isLight = !isLight;
        apiCtrl.VCS_Camera_Light(isLight);//闪光灯
        if (isLight) {
            lightBtn.setText("关闪光灯");
        } else {
            lightBtn.setText("开闪光灯");
        }
    }

    //切换摄像头
    private void switchCamera() {
        apiCtrl.VCS_switchCamera(isFront);
        isFront = !isFront;
        if (isFront) {
            switchBtn.setText("后置");
            lightBtn.setText("开闪光灯");
        } else {
            switchBtn.setText("前置");
        }
//        if (apiCtrl.isDefaultCak()) {
//            apiCtrl.setDegree(90);//视频输出角度 //前置相机270
//            apiCtrl.setMirror(false);
//        } else {
//            apiCtrl.setDegree(270);//视频输出角度 //前置相机270
//            apiCtrl.setMirror(true);
//        }
    }

    //设置是否接收某人的视频
    public void closeOtherVideo(String clientId, boolean isClose){
        apiCtrl.vcs_EnableRecvVideo(room, Integer.valueOf(clientId), isClose ? 1 : 0);
    }

    public void muteOtherAudio(String clientId, boolean isMute){
        apiCtrl.vcs_EnableRecvAudio(room, Integer.valueOf(clientId), isMute ? 1 : 0);
    }

    //设置自己的视频，是否发送
    private void closeSelfVideo() {
        if (apiCtrl.VCS_EnableSendVideo(room, myClientId, isSendSelfVideo ? Constants.CLOSE_VIDEO_OR_AUDIO : Constants.SEND_VIDEO_OR_AUDIO) >= VCS_EVENT_TYPE.VCS_R_OK) {
            isSendSelfVideo = !isSendSelfVideo;
            if (isSendSelfVideo) {
                apiCtrl.VCS_SendPause(0);
                closeSelfVideoBtn.setText("关闭自己的视频");
                apiCtrl.vcs_SendMessage(room, myClientId, myClientId + "打开了自己的视频");
            } else {
                closeSelfVideoBtn.setText("打开自己的视频");
                apiCtrl.vcs_SendMessage(room, myClientId, myClientId + "关闭了自己的视频");
            }
        } else {
            showToast("设置失败了");
        }
    }

    //设置自己的音频，是否发送
    private void closeSelfAudio() {
        if (apiCtrl.VCS_EnableSendAudio(room, myClientId, !isCloseSelfAudio ? Constants.CLOSE_VIDEO_OR_AUDIO : Constants.SEND_VIDEO_OR_AUDIO) >= VCS_EVENT_TYPE.VCS_R_OK) {
            isCloseSelfAudio = !isCloseSelfAudio;
            if (isCloseSelfAudio) {
                closeSelfAudioBtn.setText("打开自己的音频");
                apiCtrl.vcs_SendMessage(room, myClientId, myClientId + "关闭了自己的音频");
            } else {
                apiCtrl.VCS_SendPause(0);
                closeSelfAudioBtn.setText("关闭自己的音频");
                apiCtrl.vcs_SendMessage(room, myClientId, myClientId + "打开了自己的音频");
            }
        } else {
            showToast("设置失败了");
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
            case R.id.send_msg_btn:
                apiCtrl.vcs_SendMessage(room, myClientId, "一条广播消息");
                break;
            case R.id.clear_msg_btn:
                customMsgTv.setText("");
//                changeView();
                break;
        }
    }

    private boolean isFull = true;

    private void changeView(){
        isFull = !isFull;
        if (isFull){
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(DisplayUtil.getInstance().getMobileWidth(this), DisplayUtil.getInstance().getMobileHeight(this));
            cameraSurfaceView.setLayoutParams(params);
        }else {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(256, 144);
            params.gravity = Gravity.BOTTOM;
            cameraSurfaceView.setLayoutParams(params);
        }
    }

    private boolean isTop = false;

    private void setTop(){
        isTop = !isTop;
        cameraSurfaceView.setZOrderOnTop(isTop);
    }

    private void showToast(Object obj) {
        Toast.makeText(MyApplication.getContext(), null == obj ? "Unknow Error" : obj.toString(), Toast.LENGTH_LONG).show();
    }
}