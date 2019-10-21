// ////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2015-2017 Hangzhou Freewind Technology Co., Ltd.
// All rights reserved.
// http://www.seastart.cn
//
// ///////////////////////////////////////////////////////////////////////////
package com.freewind.meetingdemo.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.freewind.meetingdemo.activity.MeetingActivity;
import com.freewind.meetingdemo.bean.MemberBean;
import com.freewind.meetingdemo.R;
import com.ook.android.showview.MeetingGLSurfaceView;

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
            if (clienId.equals(memberList.get(position).getClientId())){
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
    public void onBindViewHolder(@NonNull final WindowAdapter.MyViewHolder holder, int position) {
        final MemberBean memberBean = memberList.get(position);

//        holder.meetingGLSurfaceView.setScaleType(CENTERCROP);
        holder.nameTv.setText(memberBean.getClientId());

        if (memberBean.isCloseVideo()){
            holder.closeVideoBtn.setText("打开视频");
        }else {
            holder.closeVideoBtn.setText("关闭视频");
        }

        if (memberBean.isMute()){
            holder.muteBtn.setText("关闭静音");
        }else {
            holder.muteBtn.setText("打开静音");
        }

        holder.muteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                memberBean.setMute(!memberBean.isMute());
                if (memberBean.isMute()){
                    holder.muteBtn.setText("关闭静音");
                }else {
                    holder.muteBtn.setText("打开静音");
                }
                ((MeetingActivity)context).muteOtherAudio(memberBean.getClientId(), memberBean.isMute());
            }
        });

        holder.closeVideoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                memberBean.setCloseVideo(!memberBean.isCloseVideo());
                if (memberBean.isCloseVideo()){
                    holder.closeVideoBtn.setText("打开视频");
                }else {
                    holder.closeVideoBtn.setText("关闭视频");
                }
                ((MeetingActivity)context).closeOtherVideo(memberBean.getClientId(), memberBean.isCloseVideo());
            }
        });

        holders.put(memberBean.getClientId(), holder);
    }

    @Override
    public int getItemCount() {
        return memberList == null ? 0 : memberList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public MeetingGLSurfaceView meetingGLSurfaceView;
        FrameLayout itemFl;
        TextView nameTv;
        ImageView selfMuteIv, otherMuteIv;
        TextView selfCloseTv, otherCloseTv;
        Button closeVideoBtn, muteBtn;

        MyViewHolder(View convertView) {
            super(convertView);
            itemFl = convertView.findViewById(R.id.item_fl);
            meetingGLSurfaceView = convertView.findViewById(R.id.gl_view);
            nameTv = convertView.findViewById(R.id.id_tv);
            selfMuteIv = convertView.findViewById(R.id.self_mute_iv);
            otherMuteIv = convertView.findViewById(R.id.other_mute_iv);
            selfCloseTv = convertView.findViewById(R.id.self_close_tv);
            otherCloseTv = convertView.findViewById(R.id.other_close_tv);
            closeVideoBtn = convertView.findViewById(R.id.close_video_btn);
            muteBtn = convertView.findViewById(R.id.mute_btn);
        }
    }
}