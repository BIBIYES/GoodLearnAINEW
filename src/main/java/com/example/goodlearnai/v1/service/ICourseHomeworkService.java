package com.example.goodlearnai.v1.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.CourseHomework;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author DSfeiji
 * @since 2025-04-17
 */
public interface ICourseHomeworkService extends IService<CourseHomework> {
    Result<String> createCourseHomework(CourseHomework courseHomework);

    Result<String> deleteCourseHomework(Long homeworkId);

    Result<IPage<CourseHomework>> pageCourseHomework(@RequestParam(defaultValue = "1") long current, @RequestParam(defaultValue = "10") long size);
    
    /**
     * 根据班级ID查询该班级收到的试卷
     * @param classId 班级ID
     * @return 试卷列表
     */
    Result<List<CourseHomework>> getHomeworksByClassId(Long classId);
}