package com.freewind.meetingdemo;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.util.Log;

import com.freewind.meetingdemo.activity.DialogActivity;
import com.freewind.meetingdemo.activity.MeetingActivity;
import com.freewind.meetingdemo.bean.RoomInfoBean;
import com.freewind.meetingdemo.common.Constants;
import com.freewind.meetingdemo.common.UserConfig;
import com.freewind.meetingdemo.http.HttpCallBack;
import com.freewind.meetingdemo.util.Requester;
import com.freewind.meetingdemo.util.ToastUtil;
import com.freewind.vcs.Models;
import com.freewind.vcs.Register;
import com.freewind.vcs.VcsServer;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


public class MyApplication extends Application {
    private static MyApplication app;

    public static Context getContext() {
        return app.getApplicationContext();
    }
    public static boolean isMeeting = false;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        UserConfig.init(this);
        Constants.API_HOST = UserConfig.getSpAddr() + Constants.API_VERSION;
        trustAllHosts();
        VcsServer.getInstance().init(this);
        VcsServer.getInstance().enableMqttLog(true);
    }

    public static void trustAllHosts() {
        // Create a trust manager that does not validate certificate chains
        // Android use X509 cert
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[] {};
            }

            public void checkClientTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }
        }};

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
