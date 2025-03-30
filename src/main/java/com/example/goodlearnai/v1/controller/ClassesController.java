package com.example.goodlearnai.v1.controller;


import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.Classes;
import com.example.goodlearnai.v1.service.IClassesService;
import com.example.goodlearnai.v1.utils.AuthUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author author
 * @since 2025-03-01
 */
@RestController
@RequestMapping("/v1/classes")
@Slf4j
public class ClassesController {
@Resource
private IClassesService iclassesService;
    // 老师添加班级
    @PostMapping("/add")
    public Result<String> addClass(@RequestBody Classes classes) {

        return iclassesService.addClass(classes);

    }

}
