package com.example.goodlearnai.v1.utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5加密工具类
 * @author Mouse
 */
public class MD5Util {

    /**
     * 对输入的字符串进行MD5加密
     *
     * @param input 需要加密的字符串
     * @return 加密后的32位小写MD5字符串
     */
    public static String encrypt(String input) {
        try {
            // 获取MD5算法实例
            MessageDigest md = MessageDigest.getInstance("MD5");

            // 计算MD5散列值
            byte[] messageDigest = md.digest(input.getBytes());

            // 将byte数组转换为16进制字符串
            BigInteger number = new BigInteger(1, messageDigest);
            StringBuilder hashtext = new StringBuilder(number.toString(16));

            // 补全前导零，确保是32位
            while (hashtext.length() < 32) {
                hashtext.insert(0, "0");
            }

            return hashtext.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5加密出错", e);
        }
    }

    /**
     * 对输入的字符串进行MD5加密，并返回大写形式
     *
     * @param input 需要加密的字符串
     * @return 加密后的32位大写MD5字符串
     */
    public static String encryptToUpperCase(String input) {
        return encrypt(input).toUpperCase();
    }

    /**
     * 校验输入的字符串与MD5是否匹配
     *
     * @param input 原文字符串
     * @param md5 MD5加密字符串
     * @return 是否匹配
     */
    public static boolean verify(String input, String md5) {
        String calculatedMD5 = encrypt(input);
        return calculatedMD5.equalsIgnoreCase(md5);
    }
}