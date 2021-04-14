package com.freewind.meetingdemo.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import com.freewind.meetingdemo.R;
import com.freewind.meetingdemo.bean.RoomInfoBean;
import com.freewind.meetingdemo.http.HttpCallBack;
import com.freewind.meetingdemo.util.DialogUtils;
import com.freewind.meetingdemo.util.Requester;
import com.freewind.vcs.Models;
import com.freewind.vcs.VcsServer;

public class DialogActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);

        String accountName = getIntent().getStringExtra("accountName");
        String accountId = getIntent().getStringExtra("accountId");
        String roomNo = getIntent().getStringExtra("roomNo");
        String roomPwd = getIntent().getStringExtra("roomPwd");

        Dialog dialog = DialogUtils.getInstance().getConfirmDialog(this, "邀请", "收到" + accountName + "的会议邀请，房间号:" + roomNo + " 是否接听？", "拒绝", "接听", new DialogUtils.CallBack() {
            @Override
            public void onConfirm() {
                VcsServer.getInstance().inviteConfirm(accountId, roomNo, Models.InviteResponse.IR_Accepted);
                Requester.enterMeeting(DialogActivity.this, roomNo, roomPwd, new HttpCallBack<RoomInfoBean>() {
                    @Override
                    public void onSucceed(RoomInfoBean data) {
                        startActivity(new Intent(DialogActivity.this, MeetingActivity.class)
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

            @Override
            public void onCancel() {
                VcsServer.getInstance().inviteConfirm(accountId, roomNo, Models.InviteResponse.IR_Rejected);
            }
        });
        dialog.setOnDismissListener(dialog1 -> finish());
        dialog.show();

    }
}