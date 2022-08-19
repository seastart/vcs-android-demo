package com.freewind.meetingdemo.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.freewind.meetingdemo.R;
import com.freewind.meetingdemo.common.UserConfig;
import com.freewind.meetingdemo.util.ToastUtil;
import com.freewind.vcs.Models;
import com.freewind.vcs.Register;
import com.freewind.vcs.VcsServer;
import com.freewind.vcs.util.MessageManager;

import java.util.ArrayList;
import java.util.List;

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
    EditText targetAccIdEt;
    @BindView(R.id.roomEt)
    EditText roomEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_test);
        ButterKnife.bind(this);

        accountMobileTv.setText(UserConfig.getUserInfo().getData().getAccount().getMobile());
        accountIdTv.setText(UserConfig.getUserInfo().getData().getAccount().getId());

        targetAccIdEt.setText("beae84c1eb914be89eb16309f285fcd6");
        //004: 5c3500d3826946a38bc9804daa03bbbb
        //005: beae84c1eb914be89eb16309f285fcd6

        MessageManager.getInstance().addMessageCallback(new MessageManager.MessageArrivedListener() {
            @Override
            public void messageArrived(Models.ImBody imBody) {
                ToastUtil.getInstance().showShortToast(imBody.getMessage());
            }
        });
    }

    public void copyUrl(String str) {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboardManager.setPrimaryClip(ClipData.newPlainText(null, str));
        ToastUtil.getInstance().showShortToast("已复制到系统剪切板");
    }

    @OnClick({R.id.accountIdTv, R.id.inviteBtn, R.id.sendMsgBtn, R.id.callBtn, R.id.updateBtn})
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.accountIdTv:
                copyUrl(accountIdTv.getText().toString());
                break;
            case R.id.inviteBtn:
                VcsServer.getInstance().inviteAcc(roomEt.getText().toString(), targetAccIdEt.getText().toString());
                break;
            case R.id.sendMsgBtn:
                MessageManager.getInstance().sendMessage(targetAccIdEt.getText().toString(), "你好");
                break;
            case  R.id.callBtn:
                //自己的
                Register.WaitingAccount.Builder builder = Register.WaitingAccount.newBuilder();
                builder.setId(UserConfig.getUserInfo().getData().getAccount().getId());
                builder.setRoomNo("915105013005");
                builder.setName(UserConfig.getUserInfo().getData().getAccount().getName());
                builder.setNickname(UserConfig.getUserInfo().getData().getAccount().getNickname());
                builder.setStatus(Register.InviteStatus.Waiting);

                //要邀请的成员
                Register.WaitingAccount.Builder builder2 = Register.WaitingAccount.newBuilder();
                builder2.setId(targetAccIdEt.getText().toString());
                builder2.setRoomNo("915105013005");
                builder2.setName("1111");
                builder2.setNickname("1111111111111");
                builder2.setStatus(Register.InviteStatus.Waiting);

                List<Register.WaitingAccount> list = new ArrayList<>();
                list.add(builder.build());
                list.add(builder2.build());

                VcsServer.getInstance().call("915105013005", false, list);

//                VcsServer.getInstance().accountStatusUpdate(Register.WaitingAccount.newBuilder()
//                        .setId(UserConfig.getUserInfo().getData().getAccount().getId())
//                        .setName(UserConfig.getUserInfo().getData().getAccount().getName())
//                        .setNickname(UserConfig.getUserInfo().getData().getAccount().getNickname())
//                        .setRoomNo("915105013005")
//                        .setStatus(Register.InviteStatus.Waiting)
//                        .build());
                break;
            case R.id.updateBtn:
                //更新自己状态
                Register.WaitingAccount.Builder builder3 = Register.WaitingAccount.newBuilder();
                builder3.setId(UserConfig.getUserInfo().getData().getAccount().getId());
                builder3.setRoomNo("915105013005");
                builder3.setName(UserConfig.getUserInfo().getData().getAccount().getName());
                builder3.setNickname(UserConfig.getUserInfo().getData().getAccount().getNickname());
                builder3.setStatus(Register.InviteStatus.Accepted);

                VcsServer.getInstance().call("915105013005", false, builder3.build());
                break;
        }

        //回复邀请确认通知-接收
//        VcsServer.getInstance().inviteConfirm("邀请人accountId", "邀请的房间号", Models.InviteResponse.IR_Accepted);
        //回复邀请确认通知-拒绝
//        VcsServer.getInstance().inviteConfirm("邀请人accountId", "邀请的房间号", Models.InviteResponse.IR_Rejected);
    }
}