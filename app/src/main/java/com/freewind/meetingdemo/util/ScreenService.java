package com.freewind.meetingdemo.util;

import static android.app.Notification.VISIBILITY_SECRET;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;

import com.freewind.meetingdemo.R;
import com.freewind.vcs.screen.ScreenHelper;

public class ScreenService extends Service {
   private NotificationManager mManager;
   private static final String VCS_CHANNEL_ID = "vcs_screen_channel";
   private static final int VCS_FOREGROUND_ID = 11;
   private static final String VCS_CHANNEL_NAME = "vcs_screen_channel_name";

   @Override
   public IBinder onBind(Intent intent) {
      return null;
   }

   @Override
   public void onCreate() {
      super.onCreate();
      startForeground(VCS_FOREGROUND_ID, getNotification("VCS Demo", "屏幕采集"));
   }

   @Override
   public int onStartCommand(Intent intent, int flags, int startId) {
      return START_STICKY;
   }

   @Override
   public void onDestroy() {
      super.onDestroy();
      //停止的时候销毁前台服务
      stopForeground(true);
   }

   private Notification getNotification(String title, String message) {

      //创建一个跳转到活动页面的意图
//      Intent clickIntent = new Intent(this, ScreenActivity.class);
      //clickIntent.putExtra("flag", count);//这里可以传值
      //创建一个用于页面跳转的延迟意图
//      PendingIntent contentIntent = PendingIntent.getActivity(this, 1012, clickIntent
//              , PendingIntent.FLAG_UPDATE_CURRENT);
      //创建一个通知消息的构造器
      Notification.Builder builder = new Notification.Builder(this);
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
         createNotificationChannel();

         //Android8.0开始必须给每个通知分配对应的渠道
         builder = new Notification.Builder(this, VCS_CHANNEL_ID);
      }else {
         return new Notification();
      }
      builder
              .setAutoCancel(true)//设置是否允许自动清除
              .setSmallIcon(R.mipmap.ic_launcher)//设置状态栏里的小图标
              .setTicker("提示消息来啦")//设置状态栏里面的提示文本
              .setWhen(System.currentTimeMillis())//设置推送时间，格式为"小时：分钟"
              .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))//设置通知栏里面的大图标
              .setContentTitle(title)//设置通知栏里面的标题文本
              .setContentText(message);//设置通知栏里面的内容文本
      //根据消息构造器创建一个通知对象
      return builder.build();
   }

   @TargetApi(Build.VERSION_CODES.O)
   private void createNotificationChannel() {
      NotificationChannel channel = new NotificationChannel(VCS_CHANNEL_ID, VCS_CHANNEL_NAME, NotificationManager.IMPORTANCE_MIN);
      //是否绕过请勿打扰模式
      channel.canBypassDnd();
      //闪光灯
      channel.enableLights(true);
      //锁屏显示通知
      channel.setLockscreenVisibility(VISIBILITY_SECRET);
      //闪关灯的灯光颜色
      channel.setLightColor(Color.RED);
      //桌面launcher的消息角标
      channel.canShowBadge();
      //是否允许震动
      channel.enableVibration(true);
      //获取系统通知响铃声音的配置
      channel.getAudioAttributes();
      //获取通知取到组
      channel.getGroup();
      //设置可绕过  请勿打扰模式
      channel.setBypassDnd(true);
      //设置震动模式
      channel.setVibrationPattern(new long[]{100, 100, 200});
      //是否会有灯光
      channel.shouldShowLights();
      getManager().createNotificationChannel(channel);
   }

   private NotificationManager getManager() {
      if (mManager == null) {
         mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
      }
      return mManager;
   }
}