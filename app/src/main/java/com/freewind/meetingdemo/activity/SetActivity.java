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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SetActivity extends AppCompatActivity {

    @BindView(R.id.tv180)
    TextView tv180;
    @BindView(R.id.tvezm)
    TextView tvezm;
    @BindView(R.id.back_tv)
    TextView backTv;
    @BindView(R.id.addr_et)
    EditText addrEt;
    @BindView(R.id.save_btn)
    Button saveBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);
        ButterKnife.bind(this);
        addrEt.setText(UserConfig.getSpAddr());
    }

    @OnClick({R.id.back_tv, R.id.save_btn, R.id.tv180, R.id.tvezm})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back_tv:
                finish();
                break;
            case R.id.save_btn:
                if (addrEt.getText().toString().isEmpty()) {
                    ToastUtil.getInstance().showLongToast("请输入地址");
                    return;
                }
                UserConfig.setSpAddr(addrEt.getText().toString().trim());
                Constants.API_HOST = UserConfig.getSpAddr() + Constants.API_VERSION;
                finish();
                break;
            case R.id.tv180:
                addrEt.setText(tv180.getText());
                break;
            case R.id.tvezm:
                addrEt.setText(tvezm.getText());
                break;
        }
    }
}
