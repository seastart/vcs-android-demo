package com.freewind.meetingdemo.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class PwdUtil {

    private static final String hexDigIts[] = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};

    /**
     * MD5加密
     *
     * @param origin      字符
     * @param charsetname 编码
     * @return
     */
    public static String MD5Encode(String origin, String charsetname) {
        String resultString = null;
        try {
            resultString = new String(origin);
            MessageDigest md = MessageDigest.getInstance("MD5");
            if (null == charsetname || "".equals(charsetname)) {
                resultString = byteArrayToHexString(md.digest(resultString.getBytes()));
            } else {
                resultString = byteArrayToHexString(md.digest(resultString.getBytes(charsetname)));
            }
        } catch (Exception e) {
        }
        return resultString;
    }

    /**
     * MD5加密
     *
     * @param origin 字符
     */
    public static String MD5Encode(String origin) {
        String resultString = null;
        try {
            resultString = new String(origin);
            MessageDigest md = MessageDigest.getInstance("MD5");
            resultString = byteArrayToHexString(md.digest(resultString.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
        }
        return resultString;
    }


    public static String byteArrayToHexString(byte[] b) {
        StringBuilder resultSb = new StringBuilder();
        for (byte value : b) {
            resultSb.append(byteToHexString(value));
        }
        return resultSb.toString();
    }

    public static String byteToHexString(byte b) {
        int n = b;
        if (n < 0) {
            n += 256;
        }
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigIts[d1] + hexDigIts[d2];
    }

    private static final String ALGORITHM = "HmacSHA1";
    private static final String SK = "0a6430bcb7084269817813a06e905979";

    public static String hmacSha1(String data) {
        String reString = "";
        try {
            //生成一个指定 Mac 算法 的 Mac 对象
            Mac mac = Mac.getInstance(ALGORITHM);
            //根据给定的字节数组构造一个密钥,第二参数指定一个密钥算法的名称
            SecretKey secretKey = new SecretKeySpec(SK.getBytes(StandardCharsets.UTF_8), mac.getAlgorithm());
            //用给定密钥初始化 Mac 对象
            mac.init(secretKey);
            //完成 Mac 操作
            byte[] text1 = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            reString = byteArrayToHexString(text1);
        } catch (Exception ignored) {

        }
        return reString;
    }

}
