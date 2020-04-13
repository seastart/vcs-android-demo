package com.freewind.meetingdemo.activity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;

import com.freewind.meetingdemo.MyApplication;
import com.freewind.meetingdemo.R;
import com.freewind.meetingdemo.bean.RoomInfoBean;
import com.freewind.meetingdemo.common.Constants;
import com.freewind.meetingdemo.http.HttpCallBack;
import com.freewind.meetingdemo.util.Requester;
import com.freewind.meetingdemo.util.ToastUtil;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Objects;

/**
 * author superK
 * update_at 2019/8/1
 * description
 */
public class MainActivity extends AppCompatActivity {

    AppCompatEditText roomNumberEt;
    AppCompatEditText debugAddrEt;
    AppCompatEditText agcEt;
    AppCompatEditText aecEt;
    AppCompatEditText sampleRateEt;
    AppCompatButton startBtn;
    CheckBox debugCheckBox;
    CheckBox closeSelfVideoBox;
    CheckBox closeSelfAudioBox;
    CheckBox closeOtherVideoBox;
    CheckBox closeOtherAudioBox;
    CheckBox hardDecoderBox;
    TextView ipTv;
    RadioButton video720Box;
    RadioButton video1080Box;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        roomNumberEt = findViewById(R.id.room_no_et);
        debugAddrEt = findViewById(R.id.debug_addr_et);
        agcEt = findViewById(R.id.agc_et);
        aecEt = findViewById(R.id.aec_et);
        startBtn = findViewById(R.id.start_btn);
        debugCheckBox = findViewById(R.id.debug_check_box);
        closeSelfVideoBox = findViewById(R.id.video_check_box);
        closeSelfAudioBox = findViewById(R.id.audio_check_box);
        closeOtherVideoBox = findViewById(R.id.video_other_box);
        closeOtherAudioBox = findViewById(R.id.audio_other_box);
        sampleRateEt = findViewById(R.id.sample_rate_et);
        hardDecoderBox = findViewById(R.id.hard_decoder_box);
        video720Box = findViewById(R.id.video_480_box);
        video1080Box = findViewById(R.id.video_720_box);
        ipTv = findViewById(R.id.ip_addr_tv);

        ipTv.setText("ip地址：" + getIpAddress(MyApplication.getContext()));

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Requester.enterMeeting(roomNumberEt.getText().toString(), "", new HttpCallBack<RoomInfoBean>() {
                    @Override
                    public void onSucceed(RoomInfoBean data) {
                        if (data.getCode() == Constants.NEED_PWD){
                            ToastUtil.getInstance().showLongToast("该会议室需要密码");
                            return;
                        }

                        int level = 0;
                        if (video1080Box.isChecked()){
                            level = 1;
                        }else {
                            level = 0;
                        }

                        startActivity(new Intent(MainActivity.this, MeetingActivity.class)
                                .putExtra(MeetingActivity.DEBUG_ADDR, Objects.requireNonNull(debugAddrEt.getText()).toString())
                                .putExtra(MeetingActivity.DEBUG_SWITCH, debugCheckBox.isChecked())
                                .putExtra(MeetingActivity.AGC, Objects.requireNonNull(agcEt.getText()).toString())
                                .putExtra(MeetingActivity.AEC, Objects.requireNonNull(aecEt.getText()).toString())
                                .putExtra(MeetingActivity.SAMPLE_RATE, Integer.valueOf(Objects.requireNonNull(sampleRateEt.getText()).toString()))
                                .putExtra(MeetingActivity.CLOSE_OTHER_VIDEO, closeOtherVideoBox.isChecked())
                                .putExtra(MeetingActivity.CLOSE_OTHER_AUDIO, closeOtherAudioBox.isChecked())
                                .putExtra(MeetingActivity.CLOSE_SELF_VIDEO, closeSelfVideoBox.isChecked())
                                .putExtra(MeetingActivity.CLOSE_SELF_AUDIO, closeSelfAudioBox.isChecked())
                                .putExtra(MeetingActivity.HARD_DECODER, hardDecoderBox.isChecked())
                                .putExtra(MeetingActivity.VIDEO_LEVEL, level)

                                .putExtra(MeetingActivity.ROOM_INFO, data.getData())
                        );
                    }

                    @Override
                    protected void onComplete(boolean success) {

                    }
                });
            }
        });
    }

    public String getIpAddress(Context context){
        NetworkInfo info = ((ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            // 3/4g网络
            if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                try {
                    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                        NetworkInterface intf = en.nextElement();
                        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                                return inetAddress.getHostAddress();
                            }
                        }
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                }

            } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                //  wifi网络
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ipAddress = intIP2StringIP(wifiInfo.getIpAddress());
                return ipAddress;
            }  else if (info.getType() == ConnectivityManager.TYPE_ETHERNET){
                // 有限网络
                return getLocalIp();
            }
        }
        return null;
    }

    private String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                (ip >> 24 & 0xFF);
    }

    // 获取有限网IP
    private String getLocalIp() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()
                            && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {

        }
        return "0.0.0.0";

    }
}
