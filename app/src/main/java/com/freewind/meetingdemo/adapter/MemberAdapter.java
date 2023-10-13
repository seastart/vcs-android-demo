package com.freewind.meetingdemo.adapter;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.freewind.meetingdemo.R;
import com.freewind.vcs.Models;
import com.ook.android.ikPlayer.VcsPlayerGlTextureView;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemberAdapter extends BaseQuickAdapter<Models.Account, BaseViewHolder> {
    private final Map<Integer, VcsPlayerGlTextureView> playerList = Collections.synchronizedMap(new HashMap<>());

    public Map<Integer, VcsPlayerGlTextureView> getPlayerList() {
        return playerList;
    }

    public VcsPlayerGlTextureView getPlayerByStreamId(int streamId){
        return playerList.get(streamId);
    }

    /**
     * 添加用户信息
     * @param account 用户信息
     */
    public void addAccount(Models.Account account){
        int index = -1;
        int size = getData().size();
        for (int i =0; i < size; i++){
            if (getData().get(i).getId().equals(account.getId())){
                index = i;
                break;
            }
        }
        if (index==-1){
            addData(account);
        }
    }

    /**
     * 从列表中移除对应用户信息
     * @param account 用户信息
     */
    public void removeAccount(Models.Account account){
        int index = -1;
        int size = getData().size();
        for (int i =0; i < size; i++){
            if (getData().get(i).getId().equals(account.getId())){
                index = i;
                break;
            }
        }
        if (index>-1){
            removeAt(index);
            playerList.remove(account.getStreamId());
        }
    }

    public MemberAdapter(@Nullable List data) {
        super(R.layout.layout_member, data);
    }

    @Override
    protected void convert(BaseViewHolder holder, Models.Account item) {
        holder.setText(R.id.member_nick_tv, item.getNickname());
        holder.getView(R.id.member_video_iv).setSelected(item.getVideoState() == Models.DeviceState.DS_Active);
        holder.getView(R.id.member_mic_iv).setSelected(item.getAudioState() == Models.DeviceState.DS_Active);
        playerList.put(item.getStreamId(), holder.getView(R.id.member_player));
    }

}
