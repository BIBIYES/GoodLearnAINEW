package com.example.goodlearnai.v1.controller;


import com.example.goodlearnai.v1.entity.Classes;
import com.example.goodlearnai.v1.service.IClassesService;
import org.springframework.beans.factory.annotation.Autowired;
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
public class ClassesController {
@Autowired
private IClassesService iclassesService;
    // 老师添加班级
    @PostMapping("/add")
    public String addClass(@RequestBody Classes classes) {
        System.out.println(classes);

        return iclassesService.addClass(classes);
    }

}
