package com.example.goodlearnai.v1.controller;


import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.dto.ClassJoinRequest;
import com.example.goodlearnai.v1.service.impl.ClassMembersServiceImpl;
import com.example.goodlearnai.v1.vo.ClassMemberVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 班级成员表 前端控制器
 * </p>
 *
 * @author author
 * @since 2025-09-17
 */
@RestController
@RequestMapping("/v1/class-members")
public class ClassMembersController {
    @Autowired
    private ClassMembersServiceImpl classMembersService;
    /**
     * 学生加入班级（需要输入班级加入码）
     * @param classMembers 班级成员信息（包含classId和joinCode）
     * @return 加入结果
     */
    @PostMapping("/into-class")
    public Result<String> intoClass(@RequestBody ClassJoinRequest request){
        return classMembersService.intoClass(request);
    }

    /**
     * 获取班级成员列表
     * @param classId
     * @return 班级成员列表（包含学生基本信息，包含userId）
     */
    @GetMapping("/get-class-members/{classId}")
    public Result<List<ClassMemberVO>> getClassMembers(@PathVariable Long classId){
        return classMembersService.getClassMembers(classId);
    }

    /**
     * 班级成员退出班级（学生退出自己的班级，教师移除班级成员）
     * @param userId 用户ID（学生只能操作自己的ID，教师可以操作班级内任意学生ID）
     * @param classId 班级ID
     * @return 退出/移除结果
     */
    @PostMapping("/exit-class/{userId}/{classId}")
    public Result<String> exitClass(@PathVariable Long userId, @PathVariable Long classId){
        return classMembersService.exitClass(userId, classId);
    }

    /**
     * 查询当前用户加入的班级列表（仅限学生）
     * @return 当前用户加入的班级列表，包含班级基本信息和教师信息
     */
    @GetMapping("/get-my-classes")
    public Result<List<ClassMemberVO>> getMyClasses(){
        return classMembersService.getMyClasses();
    }



}
