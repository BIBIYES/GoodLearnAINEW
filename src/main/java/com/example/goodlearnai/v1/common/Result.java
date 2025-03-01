package com.example.goodlearnai.v1.common;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 统一响应结果类
 *
 * @param <T> 数据类型泛型
 */
@Data
@Accessors(chain = true)
public class Result<T> {

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 消息
     */
    private String message;

    /**
     * 数据
     */
    private T data;

    /**
     * 成功响应
     */
    public static <T> Result<T> success(String message) {
        return new Result<T>().setCode(200).setMessage( message);
    }

    /**
     * 成功响应 带消息 和 带数据
     */
    public static <T> Result<T> success(String message,T data) {
        return new Result<T>().setCode(200).setMessage(message).setData(data);
    }



    /**
     * 失败响应
     */
    public static <T> Result<T> error() {
        return new Result<T>().setCode(500).setMessage("操作失败");
    }

    /**
     * 失败响应（带消息）
     */
    public static <T> Result<T> error(String message) {
        return new Result<T>().setCode(500).setMessage(message);
    }

    /**
     * 失败响应（带状态码和消息）
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<T>().setCode(code).setMessage(message);
    }

    /**
     * 自定义响应
     */
    public static <T> Result<T> build(Integer code, String message, T data) {
        return new Result<T>().setCode(code).setMessage(message).setData(data);
    }
}