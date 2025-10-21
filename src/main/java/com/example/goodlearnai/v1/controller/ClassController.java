package com.example.goodlearnai.v1.controller;

import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.Class;
import com.example.goodlearnai.v1.entity.ClassMembers;
import com.example.goodlearnai.v1.service.IClassService;
import com.example.goodlearnai.v1.vo.ClassVO;
import com.example.goodlearnai.v1.vo.ClassDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 课程下的班级表 前端控制器
 * </p>
 *
 * @author author
 * @since 2025-09-17
 */
@RestController
@RequestMapping("/v1/class")
public class ClassController {
    @Autowired
    private IClassService classService;
    /**
     * 创建班级
     * @param classEntity 班级实体
     * @return 创建结果
     */
    @PostMapping("/create")
    public Result<String> createClass(@RequestBody Class classEntity) {
        return classService.createClass(classEntity);
    }
    /**
     * 获取所有班级信息
     * @return 所有班级信息列表（包含教师姓名）
     */
    @GetMapping("/get-all-classes")
    public Result<java.util.List<ClassVO>> getAllClasses() {
        return classService.getAllClasses();
    }

    /**
     * 修改班级信息（班级名称和描述）
     * @param classEntity 班级实体
     * @return 修改结果
     */
    @PutMapping("/update-class-member")
    public Result<String> updateClassMember(@RequestBody Class classEntity){
        return classService.updateClassMember(classEntity);
    }

    /**
     * 删除班级
     * @param classId 班级ID
     * @return 删除结果
     */
    @PutMapping("/delete/{classId}")
    public Result<String> deleteClass(@PathVariable Long classId){
        return classService.deleteClass(classId);
    }

    /**
     * 获取班级详细信息（包含教师姓名、课程名、课程描述、课程创建时间）
     * @param classId 班级ID
     * @return 班级详细信息
     */
    @GetMapping("/detail/{classId}")
    public Result<ClassDetailVO> getClassDetail(@PathVariable Long classId){
        return classService.getClassDetail(classId);
    }

    /**
     * 切换班级学生加入权限（仅限班级负责教师）
     * @param classId 班级ID
     * @param allowJoin 是否允许学生加入（true-允许，false-不允许）
     * @return 修改结果
     */
    @PutMapping("/toggle-allow-join/{classId}")
    public Result<String> toggleAllowJoin(@PathVariable Long classId, @RequestParam Boolean allowJoin) {
        return classService.toggleAllowJoin(classId, allowJoin);
    }

}
