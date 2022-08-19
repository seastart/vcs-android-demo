package com.freewind.meetingdemo.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;

import com.freewind.meetingdemo.R;
import com.freewind.meetingdemo.bean.RoomInfoBean;
import com.freewind.meetingdemo.common.Constants;
import com.freewind.meetingdemo.http.HttpCallBack;
import com.freewind.meetingdemo.util.IpUtil;
import com.freewind.meetingdemo.util.Requester;
import com.freewind.meetingdemo.util.ToastUtil;
import com.freewind.vcs.Register;
import com.freewind.vcs.VcsServer;

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
        ipTv.setText("ip地址：" + IpUtil.getInstance().getIpAddress(this));
    }

    private void enter(Class<?> cls){
        Requester.enterMeeting(this, Objects.requireNonNull(roomNumberEt.getText()).toString(), "", "", new HttpCallBack<RoomInfoBean>() {
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

                startActivity(new Intent(MainActivity.this, cls)
                        .putExtra(Constants.DEBUG_ADDR, Objects.requireNonNull(debugAddrEt.getText()).toString())
                        .putExtra(Constants.DEBUG_SWITCH, debugCheckBox.isChecked())
                        .putExtra(Constants.MULTI, multiBox.isChecked())
                        .putExtra(Constants.AGC, Objects.requireNonNull(agcEt.getText()).toString())
                        .putExtra(Constants.AEC, Objects.requireNonNull(aecEt.getText()).toString())
                        .putExtra(Constants.FPS, Objects.requireNonNull(fpsEt.getText()).toString())
                        .putExtra(Constants.SAMPLE_RATE, Integer.valueOf(Objects.requireNonNull(sampleRateEt.getText()).toString()))
                        .putExtra(Constants.CLOSE_OTHER_VIDEO, closeOtherVideoBox.isChecked())
                        .putExtra(Constants.CLOSE_OTHER_AUDIO, closeOtherAudioBox.isChecked())
                        .putExtra(Constants.CLOSE_SELF_VIDEO, closeSelfVideoBox.isChecked())
                        .putExtra(Constants.CLOSE_SELF_AUDIO, closeSelfAudioBox.isChecked())
                        .putExtra(Constants.HARD_DECODER, hardDecoderBox.isChecked())
                        .putExtra(Constants.VIDEO_LEVEL, level)

                        .putExtra(Constants.ROOM_INFO, data.getData())
                );
            }

            @Override
            protected void onComplete(boolean success) {

            }
        });
    }

    @OnClick({R.id.start_btn, R.id.callBtn, R.id.ip_addr_tv})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start_btn:
                enter(MeetingActivity.class);
                break;
            case R.id.callBtn:
                startActivity(new Intent(this, CallTestActivity.class));
                break;
            case R.id.ip_addr_tv:
                Register.WaitingAccount.Builder builder = Register.WaitingAccount.newBuilder();
                builder.setId("5c3500d3826946a38bc9804daa03bbbb");
                builder.setRoomNo("915105013005");
                builder.setName("22222");
                builder.setNickname("222223333");
                builder.setStatus(Register.InviteStatus.Waiting);

                VcsServer.getInstance().call("915105013005", false, builder.build());
//                Uri uri = Uri.parse("ezmconf://info?mobile=15105013001&pwd=123456&no=41849892&video=true&audio=true");
//                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//                startActivity(intent);
//                VcsServer.getInstance().accountStatusUpdate(Register.WaitingAccount.newBuilder()
//                        .setId(UserConfig.getUserInfo().getData().getAccount().getId())
//                        .setName(UserConfig.getUserInfo().getData().getAccount().getName())
//                        .setNickname(UserConfig.getUserInfo().getData().getAccount().getNickname())
//                        .setRoomNo("915105013005")
//                        .setStatus(Register.InviteStatus.Waiting)
//                        .build());
                break;
        }

        //初始化
        //发送邀请通知
//        VcsServer.getInstance().inviteAcc("915105013005", "9bb38794308e4949b6f8a40cd0de80b2");
        //回复邀请确认通知-接收
//        VcsServer.getInstance().inviteConfirm("邀请人accountId", "邀请的房间号", Models.InviteResponse.IR_Accepted);
        //回复邀请确认通知-拒绝
//        VcsServer.getInstance().inviteConfirm("邀请人accountId", "邀请的房间号", Models.InviteResponse.IR_Rejected);
    }

    @Override
    protected void onStop() {
        super.onStop();
        setResult(RESULT_OK);
    }

}
