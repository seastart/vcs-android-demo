package com.freewind.meetingdemo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.freewind.meetingdemo.MyApplication;
import com.freewind.meetingdemo.R;
import com.freewind.meetingdemo.bean.RoomInfoBean;
import com.freewind.meetingdemo.bean.UserInfoBean;
import com.freewind.meetingdemo.common.UserConfig;
import com.freewind.meetingdemo.http.HttpCallBack;
import com.freewind.meetingdemo.util.Requester;
import com.freewind.meetingdemo.util.ToastUtil;
import com.freewind.vcs.Models;
import com.freewind.vcs.Register;
import com.freewind.vcs.VcsServer;

public class LoginActivity extends AppCompatActivity {

    Button loginBtn, registerBtn, setBtn, forgetBtn;
    EditText accountEt;
    EditText pwdEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginBtn = findViewById(R.id.login_btn);
        registerBtn = findViewById(R.id.register_btn);
        accountEt = findViewById(R.id.account_et);
        pwdEt = findViewById(R.id.pwd_et);
        setBtn = findViewById(R.id.set_btn);
        forgetBtn = findViewById(R.id.forget_btn);

        accountEt.setText(UserConfig.getSpAdmin());
        pwdEt.setText(UserConfig.getSpPassword());


        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Requester.login(accountEt.getText().toString(), pwdEt.getText().toString(),  new HttpCallBack<UserInfoBean>() {
                    @Override
                    public void onSucceed(UserInfoBean data) {
                        UserConfig.setSpAdmin(accountEt.getText().toString());
                        UserConfig.setSpPassword(pwdEt.getText().toString());

                        UserConfig.updateUserInfo(data);
                        UserConfig.setRequestToken(data.getData().getToken());

                        VcsServer.getInstance().start(data.getData().getAccount().getId(),
                                data.getData().getToken(),
                                data.getData().getReg().getPort(),
                                data.getData().getReg().getAddr());

                        //设置呼叫相关回调
                        VcsServer.getInstance().setVcsMsgListener(new VcsServer.VcsMsgListener() {
                            @Override
                            public void inviteNotification(String accountId, String accountName, String targetId, String roomNo, String roomName, String roomPwd) {
                                Log.e("222222222", "inviteConfirmNotification===roomNo:" + roomNo + "   accId:" + accountId + "   targetId:" + targetId);

                                //接收到邀请通知
                                if (MyApplication.isMeeting){
                                    //如果正在会议中，则回复拒绝
                                    VcsServer.getInstance().inviteConfirm(accountId, roomNo, Models.InviteResponse.IR_Rejected);
                                }else {
                                    startActivity(new Intent(getApplicationContext(), DialogActivity.class)
                                            .putExtra("accountName", accountName)
                                            .putExtra("accountId", accountId)
                                            .putExtra("roomPwd", roomPwd)
                                            .putExtra("roomNo", roomNo)
                                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                                }
                            }

                            @Override
                            public void inviteConfirmNotification(String roomNo, String accId, Models.InviteResponse response) {
                                //邀请确认通知
                                Log.e("222222222", "inviteConfirmNotification===roomNo:" + roomNo + "   accId:" + accId + "   response:" + response);
                                if (response == Models.InviteResponse.IR_Accepted){//对方接受了邀请
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Requester.enterMeeting(getApplicationContext(), roomNo, "", new HttpCallBack<RoomInfoBean>() {
                                                @Override
                                                public void onSucceed(RoomInfoBean data) {
                                                    startActivity(new Intent(getApplicationContext(), MeetingActivity.class)
                                                            .putExtra(MeetingActivity.DEBUG_ADDR, "")
                                                            .putExtra(MeetingActivity.DEBUG_SWITCH, false)
                                                            .putExtra(MeetingActivity.MULTI, true)
                                                            .putExtra(MeetingActivity.AGC, "10000")
                                                            .putExtra(MeetingActivity.AEC, "12")
                                                            .putExtra(MeetingActivity.FPS, "20")
                                                            .putExtra(MeetingActivity.SAMPLE_RATE, 48000)
                                                            .putExtra(MeetingActivity.CLOSE_OTHER_VIDEO, false)
                                                            .putExtra(MeetingActivity.CLOSE_OTHER_AUDIO, false)
                                                            .putExtra(MeetingActivity.CLOSE_SELF_VIDEO, false)
                                                            .putExtra(MeetingActivity.CLOSE_SELF_AUDIO, false)
                                                            .putExtra(MeetingActivity.HARD_DECODER, true)
                                                            .putExtra(MeetingActivity.VIDEO_LEVEL, 0)
                                                            .putExtra(MeetingActivity.ROOM_INFO, data.getData())
                                                    );
                                                }

                                                @Override
                                                protected void onComplete(boolean success) {

                                                }
                                            });
                                        }
                                    });
                                }else {//对方拒绝了邀请
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ToastUtil.getInstance().showLongToast("对方拒绝了您的邀请");
                                        }
                                    });
                                }
                            }

                            @Override
                            public void waitingBroadcast(Register.WaitingAccount waitingAccount) {

                            }

                            @Override
                            public void waitingUpdate(Register.WaitingAccount waitingAccount) {

                            }
                        });

                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    }

                    @Override
                    protected void onComplete(boolean success) {
                    }
                });
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        setBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SetActivity.class));
            }
        });

        forgetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class).putExtra(RegisterActivity.TYPE, 1));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VcsServer.getInstance().stop();
    }
}
