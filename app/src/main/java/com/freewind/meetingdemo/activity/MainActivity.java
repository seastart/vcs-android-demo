package com.freewind.meetingdemo.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;

import com.freewind.meetingdemo.MyApplication;
import com.freewind.meetingdemo.R;
import com.freewind.meetingdemo.bean.RoomInfoBean;
import com.freewind.meetingdemo.common.Constants;
import com.freewind.meetingdemo.http.HttpCallBack;
import com.freewind.meetingdemo.util.Requester;
import com.freewind.meetingdemo.util.ToastUtil;
import com.freewind.vcs.VcsServer;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * author superK
 * update_at 2019/8/1
 * description
 */
public class MainActivity extends AppCompatActivity {
    @BindView(R.id.room_no_et)
    AppCompatEditText roomNumberEt;
    @BindView(R.id.agc_et)
    AppCompatEditText agcEt;
    @BindView(R.id.aec_et)
    AppCompatEditText aecEt;
    @BindView(R.id.fps_et)
    AppCompatEditText fpsEt;
    @BindView(R.id.sample_rate_et)
    AppCompatEditText sampleRateEt;
    @BindView(R.id.debug_addr_et)
    AppCompatEditText debugAddrEt;
    @BindView(R.id.multi_box)
    CheckBox multiBox;
    @BindView(R.id.debug_check_box)
    CheckBox debugCheckBox;
    @BindView(R.id.hard_decoder_box)
    CheckBox hardDecoderBox;
    @BindView(R.id.video_check_box)
    CheckBox closeSelfVideoBox;
    @BindView(R.id.audio_check_box)
    CheckBox closeSelfAudioBox;
    @BindView(R.id.video_other_box)
    CheckBox closeOtherVideoBox;
    @BindView(R.id.audio_other_box)
    CheckBox closeOtherAudioBox;
    @BindView(R.id.video_720_box)
    RadioButton video720Box;
    @BindView(R.id.ip_addr_tv)
    TextView ipTv;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        ipTv.setText("ip地址：" + getIpAddress(MyApplication.getContext()));
    }

    public String getIpAddress(Context context) {
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
                return intIP2StringIP(wifiInfo.getIpAddress());
            } else if (info.getType() == ConnectivityManager.TYPE_ETHERNET) {
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
        } catch (SocketException ignored) {

        }
        return "0.0.0.0";
    }

    @OnClick(R.id.start_btn)
    public void onClick() {
        Requester.enterMeeting(Objects.requireNonNull(roomNumberEt.getText()).toString(), "", new HttpCallBack<RoomInfoBean>() {
            @Override
            public void onSucceed(RoomInfoBean data) {
                if (data.getCode() == Constants.NEED_PWD) {
                    ToastUtil.getInstance().showLongToast("该会议室需要密码");
                    return;
                }

                int level;
                if (video720Box.isChecked()) {
                    level = 1;
                } else {
                    level = 0;
                }

                startActivity(new Intent(MainActivity.this, MeetingActivity.class)
                        .putExtra(MeetingActivity.DEBUG_ADDR, Objects.requireNonNull(debugAddrEt.getText()).toString())
                        .putExtra(MeetingActivity.DEBUG_SWITCH, debugCheckBox.isChecked())
                        .putExtra(MeetingActivity.MULTI, multiBox.isChecked())
                        .putExtra(MeetingActivity.AGC, Objects.requireNonNull(agcEt.getText()).toString())
                        .putExtra(MeetingActivity.AEC, Objects.requireNonNull(aecEt.getText()).toString())
                        .putExtra(MeetingActivity.FPS, Objects.requireNonNull(fpsEt.getText()).toString())
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

//        VcsServer.getInstance().inviteAcc("915105013005", "16158f0a1feb49edb7d9872a92f6546d");
    }
}
