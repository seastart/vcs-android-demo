/*
 * Copyright (c) 2017 Hangzhou Freewind Technology Co., Ltd.
 * All rights reserved.
 * http://company.zaoing.com
 */

package com.freewind.meetingdemo.util;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;

import com.freewind.meetingdemo.MyApplication;

/**
 * 屏幕相关的工具类
 * Created by SuperK on 2016/6/17.
 */
public class DisplayUtil {
    private static DisplayUtil instance;

    private DisplayUtil(){}

    public static DisplayUtil getInstance(){
        if(instance == null){
            synchronized (DisplayUtil.class) {
                if (instance == null) {
                    instance = new DisplayUtil();
                }
            }
        }
        return instance;
    }

    /**
     * 得到屏幕的高度
     * @param context   上下文
     * @return int
     */
    public int getMobileHeight(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels;
    }

    /**
     * 得到屏幕的宽度
     * @param context   上下文
     * @return int
     */
    public int getMobileWidth(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    /**
     * 将px值转换为dip或dp值，保证尺寸大小不变
     * @param pxValue   px
     * @return int
     */
    public int px2dip(float pxValue) {
        final float scale = MyApplication.getContext().getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 将dip或dp值转换为px值，保证尺寸大小不变
     * @param dipValue   dip
     * @return int
     */
    public int dip2px(float dipValue) {
        final float scale = MyApplication.getContext().getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    /**
     * 将px值转换为sp值，保证文字大小不变
     * @param pxValue   px
     * @return int
     */
    public int px2sp(float pxValue) {
        final float fontScale = MyApplication.getContext().getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     * @param spValue   sp
     * @return int
     */
    public int sp2px(float spValue) {
        final float fontScale = MyApplication.getContext().getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * 获取状态栏高度
     * @return int
     */
    public int getStatusBarHeight() {
        int result = 0;
        try {
            int resourceId = MyApplication.getContext().getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                result = MyApplication.getContext().getResources().getDimensionPixelSize(resourceId);
            }
        } catch (Exception e){
            result = 84;
            e.printStackTrace();
        }
        return result;
    }
}
