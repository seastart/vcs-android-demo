/*
 * Copyright (c) 2017 Hangzhou Freewind Technology Co., Ltd.
 * All rights reserved.
 * http://company.zaoing.com
 */

package com.freewind.meetingdemo.http;

import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.freewind.meetingdemo.base.BaseBean;
import com.freewind.meetingdemo.common.Constants;
import com.freewind.meetingdemo.common.UserConfig;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import cz.msebera.android.httpclient.Header;

public class HttpHelper {
    private static final String TAG_RESPONSE = "HttpResponse";

    /**
     * APPID:     e518522054cd45028545888cb4c80c6d
     * APPKEY:   a67c660b29234e2891cc6627fc6401ce
     */

    private static AsyncHttpClient getAsyncInstance(){
        AsyncHttpClient httpClient = new AsyncHttpClient();

        httpClient.addHeader("vcs-appid","0a16828823ce41c5ad040be3ed384c14");
        httpClient.addHeader("vcs-token", UserConfig.getRequestToken());
        httpClient.addHeader("vcs-signature","");
        httpClient.addHeader("Accept", "application/json");
        httpClient.addHeader("Content-Type", "application/x-www-form-urlencoded");

        return httpClient;
    }

    public static <T extends BaseBean> AsyncHttpClient executePost(final Class<T> cls, String url, final HttpCallBack<T> callBack) {
        return executePost(cls, url, null, callBack);
    }

    public static <T extends BaseBean> AsyncHttpClient executePost(final Class<T> cls, final String url, final RequestParams params, final HttpCallBack<T> callBack) {
        AsyncHttpClient httpClient = getAsyncInstance();

        httpClient.post(url, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.e(TAG_RESPONSE, "FAIL:" + url + "\n参数：" + params );
                callBack.onNetError();
                callBack.onComplete(false);
            }
            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Log.i(TAG_RESPONSE, url + "\n" + "\n参数：" + params + "\n" + responseString );
                T t = null;
                try {
                    t = JSONObject.parseObject(responseString, cls);
                } catch (Exception e) {
                    Log.e("HttpHelper", "URL:" + url + "\nErrorMsg:" + e.getMessage());
                    callBack.onServerError(t);
                    callBack.onComplete(false);
                }
                if (null == t) {
                    Log.e(TAG_RESPONSE, responseString);
                    return;
                }
                if (t.getCode()==Constants.REQUESTER_SUCCESS) {
                    callBack.onSucceed(t);
                    callBack.onComplete(true);
                }else{
                    //解决老是显示暂无数据的问题
                    callBack.onServerError(t);
                    callBack.onComplete(false);
                }
            }
        });
        return httpClient;
    }

    public static <T extends BaseBean> AsyncHttpClient executeGet(final Class<T> cls, String url, final HttpCallBack<T> callBack) {
        return executeGet(cls, url, null, callBack);
    }

    public static <T extends BaseBean> AsyncHttpClient executeGet(final Class<T> cls, final String url, final RequestParams params, final HttpCallBack<T> callBack) {
        AsyncHttpClient httpClientient = getAsyncInstance();

        httpClientient.get(url, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.e(TAG_RESPONSE, "FAIL:" + url + "\n参数：" + params );

                callBack.onNetError();
                callBack.onComplete(false);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Log.i(TAG_RESPONSE, url + "\n" + "\n参数：" + params + responseString );
                T t = null;
                try {
                    t = JSONObject.parseObject(responseString, cls);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (null == t) {
                    Log.e(TAG_RESPONSE, responseString);
                    return;
                }
                if (t.getCode()==Constants.REQUESTER_SUCCESS) {
                    callBack.onSucceed(t);
                    callBack.onComplete(true);
                }else{
                    //解决老是显示暂无数据的问题
                    callBack.onServerError(t);
                    callBack.onComplete(false);
                }
            }
        });
        return httpClientient;
    }

}

