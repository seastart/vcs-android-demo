package com.freewind.meetingdemo.util;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class EncryptUtil {
    public static final String KEY_PASSWORD_ENCRYPT = "0a6430bcb7084269817813a06e905979";
    /**
     * 使用 HMAC-SHA1 签名方法对对encryptText进行签名
     *
     * @param encryptText 被签名的字符串
     * @param encryptKey  密钥
     * @return
     * @throws Exception
     */
    public static String HmacSHA1Encrypt(String encryptText, String encryptKey) {
        try {
            String encoding = "UTF-8";
            String algorithmName = "HmacSHA1";
            byte[] data = encryptKey.getBytes(encoding);
            //根据给定的字节数组构造一个密钥,第二参数指定一个密钥算法的名称
            SecretKey secretKey = new SecretKeySpec(data, algorithmName);
            //生成一个指定 Mac 算法 的 Mac 对象
            Mac mac = Mac.getInstance(algorithmName);
            //用给定密钥初始化 Mac 对象
            mac.init(secretKey);
            byte[] text = encryptText.getBytes(encoding);
            //完成 Mac 操作
            byte[] rawHmac = mac.doFinal(text);
            return byte2hex(rawHmac);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 字节数组转16进制
     *
     * @param b 字节数组
     * @return
     */
    private static String byte2hex(byte[] b) {
        StringBuilder hs = new StringBuilder();
        String stmp;
        for (int n = 0; b != null && n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0XFF);
            if (stmp.length() == 1) {
                hs.append('0');
            }
            hs.append(stmp);
        }
        return hs.toString().toLowerCase();
    }
}
