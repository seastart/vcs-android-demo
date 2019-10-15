package com.freewind.meetingdemo.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.freewind.meetingdemo.R;
import com.freewind.meetingdemo.bean.UserInfoBean;
import com.freewind.meetingdemo.common.UserConfig;
import com.freewind.meetingdemo.http.HttpCallBack;
import com.freewind.meetingdemo.util.Requester;

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
