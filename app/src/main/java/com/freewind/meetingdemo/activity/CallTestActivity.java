package com.freewind.meetingdemo.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.freewind.meetingdemo.R;
import com.freewind.meetingdemo.bean.RoomInfoBean;
import com.freewind.meetingdemo.common.Constants;
import com.freewind.meetingdemo.common.UserConfig;
import com.freewind.meetingdemo.http.HttpCallBack;
import com.freewind.meetingdemo.util.Requester;
import com.freewind.meetingdemo.util.ToastUtil;
import com.freewind.vcs.VcsServer;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * author superK
 * update_at 2021/2/19
 * description 呼叫测试界面
 */
public class CallTestActivity extends AppCompatActivity {

    @BindView(R.id.accountMobileTv)
    TextView accountMobileTv;
    @BindView(R.id.accountIdTv)
    TextView accountIdTv;
    @BindView(R.id.accIdEt)
    EditText accIdEt;
    @BindView(R.id.roomEt)
    EditText roomEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_test);
        ButterKnife.bind(this);

        accountMobileTv.setText(UserConfig.getUserInfo().getData().getAccount().getMobile());
        accountIdTv.setText(UserConfig.getUserInfo().getData().getAccount().getId());

        accIdEt.setText(accountIdTv.getText().toString());
    }

    public void copyUrl(String str) {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboardManager.setPrimaryClip(ClipData.newPlainText(null, str));
        ToastUtil.getInstance().showShortToast("已复制到系统剪切板");
    }

    @OnClick({R.id.accountIdTv, R.id.inviteBtn})
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.accountIdTv:
                copyUrl(accountIdTv.getText().toString());
                break;
            case R.id.inviteBtn:
                VcsServer.getInstance().inviteAcc(roomEt.getText().toString(), accIdEt.getText().toString());
                break;
        }

        //回复邀请确认通知-接收
//        VcsServer.getInstance().inviteConfirm("邀请人accountId", "邀请的房间号", Models.InviteResponse.IR_Accepted);
        //回复邀请确认通知-拒绝
//        VcsServer.getInstance().inviteConfirm("邀请人accountId", "邀请的房间号", Models.InviteResponse.IR_Rejected);
    }
}