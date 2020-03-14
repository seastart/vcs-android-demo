package com.freewind.meetingdemo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.freewind.meetingdemo.R;
import com.freewind.meetingdemo.bean.UserInfoBean;
import com.freewind.meetingdemo.common.UserConfig;
import com.freewind.meetingdemo.http.HttpCallBack;
import com.freewind.meetingdemo.util.Requester;
import com.freewind.vcs.Models;
import com.freewind.vcs.VcsServer;

public class LoginActivity extends AppCompatActivity {

    Button loginBtn, registerBtn;
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

        accountEt.setText(UserConfig.getSpAdmin());
        pwdEt.setText(UserConfig.getSpPassword());

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Requester.login(accountEt.getText().toString(), pwdEt.getText().toString(), "", new HttpCallBack<UserInfoBean>() {
                    @Override
                    public void onSucceed(UserInfoBean data) {
                        UserConfig.setSpAdmin(accountEt.getText().toString());
                        UserConfig.setSpPassword(pwdEt.getText().toString());

                        UserConfig.updateUserInfo(data);
                        UserConfig.setRequestToken(data.getData().getToken());

                        VcsServer.getInstance().start(data.getData().getAccount().getId(),
                                data.getData().getToken(),
                                data.getData().getReg().getPort(),
                                data.getData().getReg().getAddr(), new VcsServer.VcsMsgListener() {
                                    @Override
                                    public void InviteNotification(String accountId, String accountName, String targetId, String roomNo, String roomName, String roomPwd) {
                                        Log.e("222222222", accountName);
                                    }

                                    @Override
                                    public void InviteConfirmNotification(String roomNo, String accId, Models.InviteResponse response) {
                                        Log.e("222222222", response.name());
                                    }
                                });

                        startActivity(new Intent(LoginActivity.this, MainActivity.class)
                                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
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

    }
}
