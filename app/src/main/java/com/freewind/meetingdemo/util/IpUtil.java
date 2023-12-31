package com.freewind.meetingdemo.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class IpUtil {
   private static IpUtil instance;

   private IpUtil() {
   }

   public static IpUtil getInstance() {
      if (instance == null) {
         synchronized (IpUtil.class) {
            if (instance == null) {
               instance = new IpUtil();
            }
         }
      }
      return instance;
   }
   
   public String getIpAddress(Context context) {
      NetworkInfo info = ((ConnectivityManager) context
              .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
      if (info != null && info.isConnected()) {
         // 3/4g网络
         if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
            try {
               for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                  NetworkInterface intf = en.nextElement();
                  for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                     InetAddress inetAddress = enumIpAddr.nextElement();
                     if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                     }
                  }
               }
            } catch (SocketException e) {
               e.printStackTrace();
            }

         } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {
            //  wifi网络
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            return intIP2StringIP(wifiInfo.getIpAddress());
         } else if (info.getType() == ConnectivityManager.TYPE_ETHERNET) {
            // 有限网络
            return getLocalIp();
         }
      }
      return null;
   }

   private String intIP2StringIP(int ip) {
      return (ip & 0xFF) + "." +
              ((ip >> 8) & 0xFF) + "." +
              ((ip >> 16) & 0xFF) + "." +
              (ip >> 24 & 0xFF);
   }

   // 获取有限网IP
   private String getLocalIp() {
      try {
         for (Enumeration<NetworkInterface> en = NetworkInterface
                 .getNetworkInterfaces(); en.hasMoreElements(); ) {
            NetworkInterface intf = en.nextElement();
            for (Enumeration<InetAddress> enumIpAddr = intf
                    .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
               InetAddress inetAddress = enumIpAddr.nextElement();
               if (!inetAddress.isLoopbackAddress()
                       && inetAddress instanceof Inet4Address) {
                  return inetAddress.getHostAddress();
               }
            }
         }
      } catch (SocketException ignored) {

      }
      return "0.0.0.0";
   }
}
