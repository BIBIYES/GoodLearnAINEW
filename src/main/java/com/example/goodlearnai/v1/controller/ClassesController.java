package com.example.goodlearnai.v1.controller;


import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.Classes;
import com.example.goodlearnai.v1.service.IClassesService;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 前端控制器
 * </p>
 *
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
     * Add class result.
     *
     * @param classes the classes
     * @return the result
     */
// 老师添加班级
    /**
     * 添加班级接口
     *
     * @param classes 班级对象
     * @return 返回添加结果
     */
    @PostMapping("/add")
    public Result<String> addClass(@RequestBody Classes classes) {

        return iclassesService.createClass(classes);

    }

}
