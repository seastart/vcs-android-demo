package com.freewind.meetingdemo.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.freewind.meetingdemo.R;
import com.freewind.meetingdemo.base.BaseBean;
import com.freewind.meetingdemo.bean.UserInfoBean;
import com.freewind.meetingdemo.common.UserConfig;
import com.freewind.meetingdemo.http.HttpCallBack;
import com.freewind.meetingdemo.util.MD5Util;
import com.freewind.meetingdemo.util.Requester;
import com.freewind.meetingdemo.util.ToastUtil;

public class RegisterActivity extends AppCompatActivity {

    Button getCodeBtn;
    EditText mobileEt;
    EditText codeEt;
    EditText pwdEt;
    TextView backTv;
    Button registerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        getCodeBtn = findViewById(R.id.register_get_code_btn);
        mobileEt = findViewById(R.id.register_account_et);
        codeEt = findViewById(R.id.register_code_et);
        pwdEt = findViewById(R.id.register_pwd_et);
        backTv = findViewById(R.id.back_tv);
        registerBtn = findViewById(R.id.register_confirm_btn);

        backTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        getCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getCode();
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register();
            }
        });
    }

    //获取验证码
    private void getCode() {
        if (mobileEt.getText().toString().isEmpty()) {
            ToastUtil.getInstance().showShortToast("请输入手机号码");
            return;
        }

        Requester.getCode(1,mobileEt.getText().toString(), new HttpCallBack<BaseBean>() {
            @Override
            public void onSucceed(BaseBean data) {
                ToastUtil.getInstance().showShortToast("验证码发送成功，请注意查收");
            }

            @Override
            protected void onComplete(boolean success) {

            }
        });
    }

    private void register() {
        String pwd = MD5Util.MD5Encode(pwdEt.getText().toString(), "utf8");
        Requester.register(mobileEt.getText().toString(), pwd, mobileEt.getText().toString(), mobileEt.getText().toString(), codeEt.getText().toString(), new HttpCallBack<UserInfoBean>() {
            @Override
            public void onSucceed(UserInfoBean data) {
                super.onSucceed(data);
                UserConfig.setSpAdmin(mobileEt.getText().toString());
                UserConfig.setSpPassword(pwdEt.getText().toString());
                UserConfig.setRequestToken(data.getData().getToken());
                UserConfig.updateUserInfo(data);
                startActivity(new Intent(RegisterActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
            }

            @Override
            protected void onNetError() {
                super.onNetError();
            }

            @Override
            protected void onComplete(boolean success) {

            }
        });
    }
}
