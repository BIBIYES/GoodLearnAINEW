package com.example.goodlearnai.v1.utils;

/**
 * 认证工具类，用于线程本地存储用户信息
 */
public class AuthUtil {

    // 使用ThreadLocal存储当前用户ID
    private static final ThreadLocal<Long> currentUserId = new ThreadLocal<>();

    // 使用ThreadLocal存储当前用户角色
    private static final ThreadLocal<String> currentRole = new ThreadLocal<>();

    /**
     * 设置当前线程的用户ID
     */
    public static void setCurrentUserId(Long userId) {
        currentUserId.set(userId);
    }

    /**
     * 获取当前线程的用户ID
     */
    public static Long getCurrentUserId() {
        return currentUserId.get();
    }

    /**
     * 设置当前线程的用户角色
     */
    public static void setCurrentRole(String role) {
        currentRole.set(role);
    }

    /**
     * 获取当前线程的用户角色
     */
    public static String getCurrentRole() {
        return currentRole.get();
    }

    /**
     * 清除ThreadLocal中的数据，防止内存泄漏
     * 在请求结束时必须调用此方法
     */
    public static void clear() {
        currentUserId.remove();
        currentRole.remove();
    }
}