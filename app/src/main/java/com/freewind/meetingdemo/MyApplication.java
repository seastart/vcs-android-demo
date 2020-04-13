package com.freewind.meetingdemo;

import android.app.Application;
import android.content.Context;

import com.freewind.meetingdemo.common.Constants;
import com.freewind.meetingdemo.common.UserConfig;


public class MyApplication extends Application {

    private static MyApplication app;

    public static Context getContext() {
        return app.getApplicationContext();
    }


    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        UserConfig.init(this);
        Constants.API_HOST = UserConfig.getSpAddr() + Constants.API_VERSION;
    }


}
