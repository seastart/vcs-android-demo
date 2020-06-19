// ////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2015-2017 Hangzhou Freewind Technology Co., Ltd.
// All rights reserved.
// http://www.seastart.cn
//
// ///////////////////////////////////////////////////////////////////////////
package com.freewind.meetingdemo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.freewind.meetingdemo.R;
import com.freewind.meetingdemo.activity.MeetingActivity;
import com.freewind.meetingdemo.bean.MemberBean;
import com.freewind.meetingdemo.util.DisplayUtil;
import com.freewind.vcs.Models;
import com.ook.android.VCS_EVENT_TYPE;
import com.ook.android.ikPlayer.VcsPlayerGlTextureView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WindowAdapter extends  RecyclerView.Adapter<WindowAdapter.MyViewHolder> {
    private List<MemberBean> memberList;
    private HashMap<String, MyViewHolder> holders;
    private Context context;

    public HashMap<String, MyViewHolder> getHolders() {
        return holders == null ? new HashMap<String, MyViewHolder>() : holders;
    }

    public List<MemberBean> getMemberList() {
        return memberList;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    public WindowAdapter(Context context) {
        this.memberList = new ArrayList<>();
        this.holders = new HashMap<>();
        this.context = context;
    }

    public void addItem(MemberBean memberBean){
        memberList.add(memberBean);
        notifyItemInserted(memberList.size());
        notifyItemRangeChanged(memberList.size() - 1,1);//通知数据与界面重新绑定
    }

    public void removeItem(String clienId){
        if (memberList.size() < 1){
            return;
        }
        for (int position = 0; position < memberList.size(); position++){
            if (clienId.equals(memberList.get(position).getSdkNo())){
//                holders.get(clienId).itemFl.removeView(holders.get(clienId).meetingGLSurfaceView);
                holders.remove(clienId);
                notifyItemRemoved(position);
                memberList.remove(position);
                notifyItemRangeChanged(position, memberList.size() - position);//通知数据与界面重新绑定
                break;
            }
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_window, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final WindowAdapter.MyViewHolder holder, final int position) {
        final MemberBean memberBean = memberList.get(position);
        holder.nameTv.setText(memberBean.getSdkNo());

        ((MeetingActivity)context).setVcsPlayerScale(holder.meetingGLSurfaceView, memberBean);

        if (memberBean.isCloseOtherVideo()){
//            holder.otherCloseTv.setVisibility(View.VISIBLE);
            holder.closeVideoBtn.setText("打开视频");
        }else {
//            holder.otherCloseTv.setVisibility(View.GONE);
            holder.closeVideoBtn.setText("关闭视频");
        }

        if (memberBean.isCloseOtherAudio()){
            holder.muteBtn.setText("关静音");
        }else {
            holder.muteBtn.setText("开静音");
        }

        if (memberBean.isCloseVideo()){
            holder.selfCloseTv.setVisibility(View.VISIBLE);
            if (memberBean.getCloseVideo() == Models.DeviceState.DS_Active){
                //被主持人关闭了视频
            }else {
                //没被主持人关闭视频
            }
        }else {
            holder.selfCloseTv.setVisibility(View.GONE);
        }

        if (memberBean.getMute() == Models.DeviceState.DS_Disabled){
            //主持人禁言了
        }else {
            //主持人没禁言
        }

        if (memberBean.getMute() == Models.DeviceState.DS_Active){
            holder.hostCloseAudioBtn.setText("主持人关闭音频");
        }else {
            holder.hostCloseAudioBtn.setText("主持人开启音频");
        }

        if (memberBean.getCloseVideo() == Models.DeviceState.DS_Active){
            holder.hostCloseVideoBtn.setText("主持人关闭视频");
        }else {
            holder.hostCloseVideoBtn.setText("主持人开启视频");
        }

        holder.muteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                memberBean.setCloseOtherAudio(!memberBean.isCloseOtherAudio());
                notifyItemChanged(position);
                ((MeetingActivity)context).muteOtherAudio(memberBean.getSdkNo(), memberBean.isCloseOtherAudio());
            }
        });

        holder.closeVideoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                memberBean.setCloseOtherVideo(!memberBean.isCloseOtherVideo());
                notifyItemChanged(position);
                ((MeetingActivity)context).closeOtherVideo(memberBean.getSdkNo(), memberBean.isCloseOtherVideo());
            }
        });

        holder.kickBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MeetingActivity)context).kickOut(memberBean.getAccountId());
            }
        });

        holder.hostCloseVideoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MeetingActivity)context).hostCtrlMember(
                        memberBean.getAccountId(),
                        memberBean.getCloseVideo() == Models.DeviceState.DS_Active ? Models.DeviceState.DS_Disabled : Models.DeviceState.DS_Active,
                        null);
            }
        });

        holder.hostCloseAudioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MeetingActivity)context).hostCtrlMember(
                        memberBean.getAccountId(),
                        null,
                        memberBean.getMute() == Models.DeviceState.DS_Active ? Models.DeviceState.DS_Disabled : Models.DeviceState.DS_Active);
            }
        });

        holder.btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                holder.meetingGLSurfaceView.setScaleType(VCS_EVENT_TYPE.CENTERCROP);
                ((MeetingActivity)context).useChannel(Integer.parseInt(memberBean.getSdkNo()), 1);//track0
            }
        });

        holder.btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                holder.meetingGLSurfaceView.setScaleType(VCS_EVENT_TYPE.CENTERINSIDE);
                ((MeetingActivity)context).useChannel(Integer.parseInt(memberBean.getSdkNo()), 2);//track1
            }
        });

        holder.btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MeetingActivity)context).useChannel(Integer.parseInt(memberBean.getSdkNo()), 4);//track2
            }
        });

        holder.btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MeetingActivity)context).useChannel(Integer.parseInt(memberBean.getSdkNo()), 3);//track3
            }
        });

        holders.put(memberBean.getSdkNo(), holder);
    }

    @Override
    public int getItemCount() {
        return memberList == null ? 0 : memberList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public VcsPlayerGlTextureView meetingGLSurfaceView;
        TextView nameTv;
        ImageView selfMuteIv, otherMuteIv;
        TextView selfCloseTv, otherCloseTv;
        Button closeVideoBtn, muteBtn, kickBtn, hostCloseVideoBtn, hostCloseAudioBtn, btn1, btn2, btn3, btn4;
        public FrameLayout frameLayout;

        MyViewHolder(View convertView) {
            super(convertView);
//            setIsRecyclable(false);

            meetingGLSurfaceView = convertView.findViewById(R.id.gl_view);

            frameLayout = convertView.findViewById(R.id.fl_view);

            nameTv = convertView.findViewById(R.id.id_tv);
            selfMuteIv = convertView.findViewById(R.id.self_mute_iv);
            otherMuteIv = convertView.findViewById(R.id.other_mute_iv);
            selfCloseTv = convertView.findViewById(R.id.self_close_tv);
            otherCloseTv = convertView.findViewById(R.id.other_close_tv);
            closeVideoBtn = convertView.findViewById(R.id.close_video_btn);
            muteBtn = convertView.findViewById(R.id.mute_btn);
            kickBtn = convertView.findViewById(R.id.kick_out_btn);
            hostCloseVideoBtn = convertView.findViewById(R.id.host_close_video_btn);
            hostCloseAudioBtn = convertView.findViewById(R.id.host_close_audio_btn);
            btn1 = convertView.findViewById(R.id.btn1);
            btn2 = convertView.findViewById(R.id.btn2);
            btn3 = convertView.findViewById(R.id.btn3);
            btn4 = convertView.findViewById(R.id.btn4);

            int width=0, height=0;

            if (((MeetingActivity)context).isLand){
                height = (DisplayUtil.getInstance().getMobileWidth(context)/((MeetingActivity)context).spanCount) * 9 / 16;
            }else {
                height = (DisplayUtil.getInstance().getMobileWidth(context)/((MeetingActivity)context).spanCount) * 16 / 9;
            }
            width = DisplayUtil.getInstance().getMobileWidth(context)/((MeetingActivity)context).spanCount;

            frameLayout.setLayoutParams(new ConstraintLayout.LayoutParams(width, height));
        }
    }
}