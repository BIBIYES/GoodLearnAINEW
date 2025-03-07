package com.example.goodlearnai.v1.exception;

import com.example.goodlearnai.v1.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;


// 定义一个管理的路径，
@ControllerAdvice(basePackages="com.example.goodlearnai.v1.controller")
public class GlobalExceptionHandler  {
    // 日志
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    //统一异常处理@ExceptionHandler,主要用于Exception  
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result<?> error(HttpServletRequest request, Exception e){
        log.error("异常信息：",e);
        return Result.error("系统出现异常");
    }
    // 自定义处理  
    @ExceptionHandler(CustomException.class)
    @ResponseBody
    public Result<?> customError(HttpServletRequest request, CustomException e){
        return Result.error(e.getMsg());
    }
}