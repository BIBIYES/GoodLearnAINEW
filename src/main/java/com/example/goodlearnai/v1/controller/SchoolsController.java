package com.example.goodlearnai.v1.controller;


import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.Schools;
import com.example.goodlearnai.v1.service.ISchoolsService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 管理员添加学校
     */
    @PostMapping("/add-schools")
    public Result<List<Schools>> addSchools(@RequestBody Schools schools) {
        return iSchoolsService.addSchools(schools);
    }
}
