/*
 * Copyright (c) 2017 Hangzhou Freewind Technology Co., Ltd.
 * All rights reserved.
 * http://company.zaoing.com
 */

package com.freewind.meetingdemo.util;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.freewind.meetingdemo.R;

/**
 * Glide工具类
 * Created by superK on 2017/9/25.
 */

public class GlideUtil {

    private static RequestOptions myOptions = new RequestOptions();

    /**
     * 判断url是否要加前缀,并显示图片
     * @param context   上下文
     * @param url   地址
     * @param imageView    控件
     */
    public static void showImage(Context context, String url, ImageView imageView){
        if (url != null){
            if (url.toLowerCase().startsWith("/storage")){
                Glide.with(context).asBitmap().load(url).apply(myOptions).into(imageView);
                return;
            }
            if (url.toLowerCase().contains("http:")){
                Glide.with(context).asBitmap().load(url).apply(myOptions).into(imageView);
            }else {
                if (url.isEmpty()){
//                    Glide.with(context).asBitmap().load(BaseApp.appType != 0 ? R.drawable.default_avatar : R.drawable.icon_logo).apply(myOptions).into(imageView);
                }else {
                    if (url.toLowerCase().contains("https:")){
                        Glide.with(context).asBitmap().load(url).apply(myOptions).into(imageView);
                    }else {
                        Glide.with(context).asBitmap().load(url).apply(myOptions).into(imageView);
                    }
                }
            }
        }else {
            Glide.with(context).load(R.drawable.ic_launcher_background).apply(myOptions).into(imageView);
        }
    }

}
