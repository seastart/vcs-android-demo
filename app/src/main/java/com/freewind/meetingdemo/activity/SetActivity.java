package com.freewind.meetingdemo.activity;

import android.app.UiModeManager;
import android.content.res.Configuration;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
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
    @BindView(R.id.encoder_tv)
    TextView encoderTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);
        ButterKnife.bind(this);
        addrEt.setText(UserConfig.getSpAddr());
        encoderTv.setText(selectCodec());
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

    private String selectCodec() {
        UiModeManager uiModeManager = (UiModeManager)getSystemService(UI_MODE_SERVICE);
        String mimeType="video/avc";
        StringBuilder stringBuilder = new StringBuilder();
        // 获取所有支持编解码器数量
        int numCodecs = MediaCodecList.getCodecCount();

        if (uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION) {
            // 获取所有支持编解码器数量
            for (int i = 0; i < numCodecs; i++) {
                //trackPrintf("Running on a TV Device");
                stringBuilder.append("TV Device support encoder::[ ");

//                for (int j = 0; j < types.length; j++) {
//                    if (types[j].equalsIgnoreCase(mimeType)) {
//                        trackPrintf("TV support encoder::"+codecInfo.getName());
//                        if(!codecInfo.getName().contains("OMX.google.h264"))
//                            ik++;
//                    }
//                    trackPrintf(" ]");
//                }
            }
        } else {
            //trackPrintf("Running on a non-TV Device");
            stringBuilder.append("Device support ").append(mimeType).append(" encoder:");
        }

        int k =0;
        for (int i = 0; i < numCodecs; i++) {
            // 编解码器相关性信息存储在MediaCodecInfo中
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            // 判断是否为编码器
            if (!codecInfo.isEncoder()) {
                continue;
            }
            String[] types = codecInfo.getSupportedTypes();
            for (String type : types) {
                if (type.equalsIgnoreCase(mimeType)) {
                    k++;
                    stringBuilder.append("\n").append(k).append(". ").append(codecInfo.getName());
                }
            }

        }

        return stringBuilder.toString();
    }
}
