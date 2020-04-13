package com.freewind.meetingdemo.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.freewind.meetingdemo.R;
import com.freewind.meetingdemo.common.Constants;
import com.freewind.meetingdemo.common.UserConfig;
import com.freewind.meetingdemo.util.ToastUtil;

public class SetActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);

        final EditText addrEt = findViewById(R.id.addr_et);

        Button saveBtn = findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (addrEt.getText().toString().isEmpty()){
                    ToastUtil.getInstance().showLongToast("请输入地址");
                    return;
                }
                UserConfig.setSpAddr(addrEt.getText().toString().trim());
                Constants.API_HOST = UserConfig.getSpAddr() + Constants.API_VERSION;
                finish();
            }
        });

        TextView backTv = findViewById(R.id.back_tv);
        backTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        addrEt.setText(UserConfig.getSpAddr());
    }
}
