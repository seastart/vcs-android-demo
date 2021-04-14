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
import com.freewind.meetingdemo.util.EncryptUtil;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class HttpHelper {
    private static final String TAG_RESPONSE = "HttpResponse";
    //"0a16828823ce41c5ad040be3ed384c14";

    /**
     * APPID:    8847d560e2764adbb479c83dcc5135c9
     * APPKEY:   4f7e524b73b84aa48f8ab27f0255df63
     */

    public static final String APP_ID = "8847d560e2764adbb479c83dcc5135c9";
    public static final String APP_KEY = "4f7e524b73b84aa48f8ab27f0255df63";


    private static AsyncHttpClient getAsyncInstance(){
        AsyncHttpClient httpClient = new AsyncHttpClient();

        httpClient.addHeader("nvc-appid",APP_ID);
        httpClient.addHeader("nvc-token", UserConfig.getRequestToken());
//        httpClient.addHeader("nvc-signature","");
        httpClient.addHeader("Accept", "application/json");
        httpClient.addHeader("Content-Type", "application/x-www-form-urlencoded");

        return httpClient;
    }

//    public static <T extends BaseBean> AsyncHttpClient executePost(final Class<T> cls, String url, final HttpCallBack<T> callBack) {
//        return executePost(cls, url, null, callBack);
//    }

    public static <T extends BaseBean> AsyncHttpClient executePost(final Class<T> cls, final String url, final RequestParams params, List<String> paramsList, final HttpCallBack<T> callBack) {
        AsyncHttpClient httpClient = getAsyncInstance();

        //将整个参数键值对集合按照参数名字典序排序
        //将排序好的参数集合输出成FormUrlEcnode的格式
        //将FormUrlEcnode字符串使用AppKey进行HMacSha1进行签名，所得结果转换为小写16进制字符串
        paramsList.add("appid=" + APP_ID);
        Collections.sort(paramsList, String::compareTo);
        StringBuilder stringBuilder = new StringBuilder();
        for (String name : paramsList) {
            stringBuilder.append(name).append("&");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        httpClient.addHeader("vcs-signature", EncryptUtil.HmacSHA1Encrypt(stringBuilder.toString(), APP_KEY));//签名

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

//    public static <T extends BaseBean> AsyncHttpClient executeGet(final Class<T> cls, String url, final HttpCallBack<T> callBack) {
//        return executeGet(cls, url, null, callBack);
//    }

    public static <T extends BaseBean> AsyncHttpClient executeGet(final Class<T> cls, final String url, final RequestParams params, List<String> paramsList, final HttpCallBack<T> callBack) {
        AsyncHttpClient httpClientient = getAsyncInstance();

        //将整个参数键值对集合按照参数名字典序排序
        //将排序好的参数集合输出成FormUrlEcnode的格式
        //将FormUrlEcnode字符串使用AppKey进行HMacSha1进行签名，所得结果转换为小写16进制字符串
        paramsList.add("appid=" + APP_ID);
        Collections.sort(paramsList, String::compareTo);
        StringBuilder stringBuilder = new StringBuilder();
        for (String name : paramsList) {
            stringBuilder.append(name).append("&");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        httpClientient.addHeader("vcs-signature", EncryptUtil.HmacSHA1Encrypt(stringBuilder.toString(), APP_KEY));//签名


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

