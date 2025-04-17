package com.example.goodlearnai.v1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.CourseHomework;
import com.example.goodlearnai.v1.exception.CustomException;
import com.example.goodlearnai.v1.mapper.CourseHomeworkMapper;
import com.example.goodlearnai.v1.service.ICourseHomeworkService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.goodlearnai.v1.utils.AuthUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author DSfeiji
 * @since 2025-04-17
 */
@Service
@Slf4j
public class CourseHomeworkServiceImpl extends ServiceImpl<CourseHomeworkMapper, CourseHomework> implements ICourseHomeworkService {

    @Override
    public Result<String> createCourseHomework(CourseHomework courseHomework) {
        Long userId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();

        // 判断是否为老师角色
        if (!"teacher".equals(role)) {
            log.warn("用户暂无权限创建试卷: userId={}", userId);
            return Result.error("暂无权限创建试卷");
        }
        try {
            courseHomework.setAssignTime(LocalDateTime.now());
            if (courseHomework.getDeadline() == null){
                courseHomework.setDeadline(LocalDateTime.now().plusDays(7));
            }
            courseHomework.setStatus(1);
            if (save(courseHomework)){
                return Result.success("作业创建成功");
            }else {
                return Result.error("作业创建失败");
            }
        }catch (Exception e){
            log.error("作业创建失败: userId={}, courseHomework={}, error={}", userId, courseHomework, e.getMessage());
            return Result.error("作业创建失败");
        }

    }

    @Override
    public Result<String> deleteCourseHomework(Long homeworkId) {
        Long userId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();

        // 判断是否为老师角色
        if (!"teacher".equals(role)) {
            log.warn("用户暂无权限创建试卷: userId={}", userId);
            return Result.error("暂无权限创建试卷");
        }

        try {
            CourseHomework courseHomework = getById(homeworkId);
            courseHomework.setStatus(0);
            if (updateById(courseHomework)){
                return Result.success("作业删除成功");
            }else {
                return Result.error("作业删除失败");
            }
        } catch (Exception e) {
            log.error("作业删除失败: userId={}, homeworkId={}, error={}", userId, homeworkId, e.getMessage());
            return Result.error("作业删除失败");
        }
    }

    @Override
    public Result<IPage<CourseHomework>> pageCourseHomework(long current, long size) {
        try {
            // 创建分页对象
            Page<CourseHomework> page = new Page<>(current, size);

            // 构建查询条件
            LambdaQueryWrapper<CourseHomework> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(CourseHomework::getStatus, 1);


            // 按更新时间降序排序
            queryWrapper.orderByDesc(CourseHomework::getAssignTime);

            try {
                // 执行分页查询
                IPage<CourseHomework> courseHomeworkPage =  page(page, queryWrapper);

                // 如果没有查询到数据，返回空的分页对象
                if (courseHomeworkPage == null || courseHomeworkPage.getRecords().isEmpty()) {
                    log.info("未查询到相关试卷数据: 当前页={}, 每页大小={}", current, size);
                    return Result.success("未查询到相关数据", new Page<>());
                }

                log.info("分页查询试卷成功: 当前页={}, 每页大小={}, 总记录数={}, 总页数={}",
                        current, size, courseHomeworkPage.getTotal(), courseHomeworkPage.getPages());
                return Result.success("查询成功", courseHomeworkPage);
            } catch (Exception e) {
                log.error("分页查询试卷失败", e);
                throw new CustomException("分页查询试卷时发生未知异常");
            }
        } catch (Exception e) {
            log.error("分页查询试卷失败", e);
            throw new CustomException("分页查询试卷时发生未知异常");
        }
    }
}
