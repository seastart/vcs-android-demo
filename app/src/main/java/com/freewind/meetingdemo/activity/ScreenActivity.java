package com.freewind.meetingdemo.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.ContextCompat;

import com.freewind.meetingdemo.R;
import com.freewind.meetingdemo.util.DialogUtils;
import com.freewind.meetingdemo.util.FloatingButtonService;
import com.freewind.meetingdemo.util.ScreenService;
import com.freewind.meetingdemo.util.ScreenUtil;
import com.freewind.meetingdemo.util.ToastUtil;
import com.freewind.vcs.devices.utils.GeneralUtils;
import com.freewind.vcs.interfaces.ScreenEvent;
import com.ook.android.SreenRecorder.MediaCaptureService;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * author superK
 * update_at 2023/6/17
 * description 投屏功能界面
 */
public class ScreenActivity extends AppCompatActivity implements ScreenEvent{
    CheckBox audioCb;
    AppCompatEditText ipAddressEt, nameEt;
    Button startBtn, sendAudioBtn, sendVideoBtn;
    TextView logTv;
    FrameLayout progressFl;

    Dialog quitDialog;
    Intent floatIntent;
    Intent mForegroundService;

    boolean sendAudio = true;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startRecord();
                } else {
                    ToastUtil.getInstance().showShortToast("请打开麦克风权限");
                }
            });

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_screen);
        audioCb = findViewById(R.id.audio_check_box);
        ipAddressEt = findViewById(R.id.address_et);
        nameEt = findViewById(R.id.name_et);
        startBtn = findViewById(R.id.start_btn);
        findViewById(R.id.address_ll).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra(MediaCaptureService.EXTRA_ACTION_NAME,MediaCaptureService.ACTION_STOP);
                intent.setAction(MediaCaptureService.ACTION_ALL);
                sendBroadcast(intent);
            }
        });
        logTv = findViewById(R.id.logTv);
        logTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DisplayManager disp = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
                Display[] allDisplays = disp.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION);
                Log.e("ccccccccccc" , "Display Count  " + allDisplays.length);
                for (Display dl : allDisplays) {
                    Log.e("ccccccccccc" , dl.toString());
                }
            }
        });
        sendAudioBtn = findViewById(R.id.send_audio_btn);
        sendVideoBtn = findViewById(R.id.send_video_btn);
        progressFl = findViewById(R.id.progressFl);
        progressFl.setOnClickListener(view -> {});
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {//可采集音频
            audioCb.setVisibility(View.VISIBLE);
        }else{
            audioCb.setVisibility(View.INVISIBLE);
        }
        quitDialog = DialogUtils.getInstance().getConfirmDialog(this, "温馨提示", "确定要停止投屏吗？", "取消", "确定", new DialogUtils.CallBack() {
            @Override
            public void onConfirm() {
                stopRecoding();
            }

            @Override
            public void onCancel() {

            }
        });
        sendAudioBtn.setOnClickListener(view -> enableSendAudio());
        sendVideoBtn.setOnClickListener(view->enableSendVideo());
        startBtn.setOnClickListener(view -> {
            if (ScreenUtil.getInstance().isRecording()){//正在录
                if (quitDialog != null && !quitDialog.isShowing()){
                    quitDialog.show();
                }
            }else{
                String ipAddress =  ipAddressEt.getText().toString();
                if (isCorrectIp(ipAddress)){//检测ip是否正确
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                        startRecord();
                    } else if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
                        ToastUtil.getInstance().showShortToast("请打开录音权限");
                    } else {
                        requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
                    }
                }else{
                    ToastUtil.getInstance().showLongToast("请输入正确的ip");
                }
            }
        });
        ScreenUtil.getInstance().setScreenEvent(this);

        //可能存在activity被回收的情况，重新进入后恢复状态
//        audioCb.setChecked(ScreenUtil.getInstance().isEnableSendAudio());
        updateStatus();
    }

    private void updateStatus(){
        if (ScreenUtil.getInstance().isEnableSendAudio()){
            sendAudioBtn.setText("关闭音频");
        }else{
            sendAudioBtn.setText("打开音频");
        }
        if (ScreenUtil.getInstance().isEnableSendVideo()){
            sendVideoBtn.setText("关闭视频");
        }else{
            sendVideoBtn.setText("打开视频");
        }
        if (ScreenUtil.getInstance().isRecording()){
            startBtn.setText("结束投屏");
            progressFl.setVisibility(View.GONE);
            startForeService();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {//可采集音频
                sendAudioBtn.setVisibility(View.VISIBLE);
                if (sendAudio){
                    sendAudioBtn.setText("关闭音频");
                }else{
                    sendAudioBtn.setText("打开音频");
                }
            }
        }else{
            startBtn.setText("开始投屏");
            logTv.setText("");
            progressFl.setVisibility(View.GONE);
            sendAudioBtn.setVisibility(View.INVISIBLE);
            stopForeService();
        }
    }

    /**
     * 设置音频发送状态
     */
    public void enableSendAudio(){
        ScreenUtil.getInstance().enableSendAudio(!ScreenUtil.getInstance().isEnableSendAudio());

        if (ScreenUtil.getInstance().isEnableSendAudio()){
            sendAudioBtn.setText("关闭音频");
        }else{
            sendAudioBtn.setText("打开音频");
        }
    }

    /**
     * 设置音频发送状态
     */
    public void enableSendVideo(){
        ScreenUtil.getInstance().enableSendVideo(!ScreenUtil.getInstance().isEnableSendVideo());

        if (ScreenUtil.getInstance().isEnableSendVideo()){
            sendVideoBtn.setText("关闭视频");
        }else{
            sendVideoBtn.setText("打开视频");
        }
    }

    public void startRecord(){
        if (progressFl.getVisibility() == View.VISIBLE){
            return;
        }
        progressFl.setVisibility(View.VISIBLE);
        sendAudio = audioCb.isChecked();
        String ipAddress =  ipAddressEt.getText().toString();
        String name = GeneralUtils.getAndroidID();
        ScreenUtil.getInstance().init(this, ipAddress, name);
        ScreenUtil.getInstance().startRecording(sendAudio);
    }

    public void startFloatWindowService(){
        if (floatIntent == null){
            floatIntent = new Intent(this, FloatingButtonService.class);
            startService(floatIntent);
        }
    }

    public void stopFloatWindowService(){
        if (floatIntent != null){
            stopService(floatIntent);
            floatIntent = null;
        }
    }

    /**
     * 开启前台服务
     */
    public void startForeService(){
        if (mForegroundService == null){
            mForegroundService = new Intent(ScreenActivity.this, ScreenService.class);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                startForegroundService(mForegroundService);
            } else {
                startService(mForegroundService);
            }
        }
        startFloatWindowService();
    }

    /**
     * 结束前台服务
     */
    public void stopForeService(){
        if (mForegroundService != null){
            stopService(mForegroundService);
            mForegroundService = null;
        }
        stopFloatWindowService();
    }

    public void stopRecoding(){
        ScreenUtil.getInstance().stopRecording();
        ScreenUtil.getInstance().release();
    }

    /** * 判断是否为合法IP **/
    public static boolean isCorrectIp(String ipAddress) {
        if (ipAddress == null || ipAddress.isEmpty()){
            return false;
        }
        String ip = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";
        Pattern pattern = Pattern.compile(ip);
        Matcher matcher = pattern.matcher(ipAddress);
        return matcher.matches();
    }

    @Override
    public void onBackPressed() {
        if (startBtn.isSelected()){
            if (quitDialog != null && !quitDialog.isShowing()){
                quitDialog.show();
            }
        }else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ScreenUtil.getInstance().setScreenEvent(null);
        Log.e("xxxxxxxxxx", "onDestroy");
    }

    @Override
    public void onScreenConfirm(boolean enable, String extra) {
        if (!enable){//盒子拒绝投屏
            stopRecoding();
            ToastUtil.getInstance().showShortToast(extra);
        }
    }

    @Override
    public void onRecordingStart() {
        updateStatus();
    }

    @Override
    public void onRecordingStop() {
        updateStatus();
    }

    @Override
    public void onRecordingError(String msg) {
        ToastUtil.getInstance().showLongToast(msg);
        stopRecoding();//收到指令后结束本地
    }

    @Override
    public void onClose(String reason) {
        ToastUtil.getInstance().showLongToast("收到强制结束指令：" + reason);
        stopRecoding();//收到指令后结束本地
    }

    @Override
    public void onTimeOut() {
        ToastUtil.getInstance().showLongToast("盒子超时了，结束投屏");
        stopRecoding();//收到回调后结束本地
    }

    @Override
    public void onError(int code, String msg) {
        ToastUtil.getInstance().showShortToast(msg);
        progressFl.setVisibility(View.GONE);
    }

    @Override
    public void onUploadStatus(String info) {
        if (progressFl.getVisibility() == View.GONE){
            logTv.setText(info);
        }
    }
}
