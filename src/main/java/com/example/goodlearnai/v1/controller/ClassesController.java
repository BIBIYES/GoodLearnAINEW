package com.example.goodlearnai.v1.controller;


import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.Classes;
import com.example.goodlearnai.v1.entity.Users;
import com.example.goodlearnai.v1.service.IClassesService;

import com.example.goodlearnai.v1.vo.UserInfo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * 班级的控制
 * @author Mouse
 * @since 2025 -03-01
 */
@RestController
@RequestMapping("/v1/classes")
@Slf4j
public class ClassesController {
@Resource
private IClassesService iclassesService;


    /**
     * 添加班级接口
     *
     * @param classes 班级对象
     * @return 返回添加结果
     */
    @PostMapping("/createClass")
    public Result<String> createClass(@RequestBody Classes classes) {

        return iclassesService.createClass(classes);

    }

    @PostMapping("/set-monitor")
    public Result<String> setMonitor(@RequestParam Long monitor,@RequestBody Classes classes) {
        return iclassesService.setMonitor(classes,monitor);
    }

}
