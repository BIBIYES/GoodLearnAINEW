package com.example.goodlearnai.v1.controller;


import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.Schools;
import com.example.goodlearnai.v1.service.ISchoolsService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author mouse
 * @since 2025-04-01
 */
@RestController
@RequestMapping("/v1/schools")
public class SchoolsController {
    @Resource
    private ISchoolsService iSchoolsService;
    // 获取所有学校
    @GetMapping("/get-schools")
    public Result<List<Schools>> getSchools() {
        return iSchoolsService.getSchools();
    }
}
