/*
 * Copyright (c) 2017 Hangzhou Freewind Technology Co., Ltd.
 * All rights reserved.
 * http://company.zaoing.com
 */

package com.freewind.meetingdemo.util;

import android.widget.Toast;

import com.freewind.meetingdemo.MyApplication;


public class ToastUtil {
    private static ToastUtil instance;

    private ToastUtil(){}

    public static ToastUtil getInstance(){
        if(instance == null){
            synchronized (ToastUtil.class) {
                if (instance == null) {
                    instance = new ToastUtil();
                }
            }
        }
        return instance;
    }

    public void showLongToast(Object obj) {
        showToast(obj, Toast.LENGTH_LONG);
    }

    private static void showToast(Object obj, int time) {
//        Toast toast = new Toast(context);
//        toast.setDuration(time);
//        toast.setGravity(Gravity.CENTER, 0, 0);
//        TextView tv = new TextView(context);
//        tv.setTextColor(Color.WHITE);
//        tv.setGravity(Gravity.CENTER);
//        tv.setBackgroundColor(Color.argb(200, 0, 0, 0));
//        tv.setPadding(20, 10, 20, 10);
//        tv.setText(null == obj ? "Unknow Error" : obj.toString());
//        toast.setView(tv);
//        toast.show();
        Toast.makeText(MyApplication.getContext(), null == obj ? "Unknow Error" : obj.toString(),time).show();
    }

    public void showShortToast(Object obj) {
        showToast(obj, Toast.LENGTH_SHORT);
    }
}
