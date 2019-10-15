package com.freewind.meetingdemo.activity;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.freewind.meetingdemo.R;
import com.freewind.meetingdemo.common.UserConfig;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent it;
//                if (UserConfig.isLogined()){
//                    it = new Intent(WelcomeActivity.this, MainActivity.class);
//                }else {
                    it = new Intent(WelcomeActivity.this, LoginActivity.class);
//                }
                startActivity(it);
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        }, 1000);
    }
}
