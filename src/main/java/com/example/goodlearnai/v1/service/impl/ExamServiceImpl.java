package com.example.goodlearnai.v1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.Exam;
import com.example.goodlearnai.v1.exception.CustomException;
import com.example.goodlearnai.v1.mapper.ExamMapper;
import com.example.goodlearnai.v1.service.IExamService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.goodlearnai.v1.utils.AuthUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * <p>
 * 试卷表 服务实现类
 * </p>
 *
 * @author DSfeiji
 * @since 2025-04-16
 */
@Service
@Slf4j
public class ExamServiceImpl extends ServiceImpl<ExamMapper, Exam> implements IExamService {

    @Override
    public Result<String> addExam(Exam exam) {
        Long userId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();

        // 判断是否为老师角色
        if (!"teacher".equals(role)) {
            log.warn("用户暂无权限创建试卷: userId={}", userId);
            return Result.error("暂无权限创建试卷");
        }
        try {
            exam.setCreatedAt(LocalDateTime.now());
            exam.setStatus(1);
            exam.setTeacherId(userId);
            if (save(exam)){
                return Result.success("试卷创建成功");
            } else {
                return Result.error("试卷创建失败");

            }
        } catch (Exception e) {
            log.error("试卷创建失败: userId={}, exam={}, error={}", userId, exam, e.getMessage());
            return Result.error("试卷创建失败");
        }
    }

    @Override
    public Result<String> deleteExam(Long examId) {
        Long userId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();

        if (!"teacher".equals(role)){
            log.warn("用户暂无权限删除试卷: userId={}", userId);
            return Result.error("暂无权限删除试卷");
        }
        try {
            Exam exam = getById(examId);
            if (exam == null) {
                log.warn("试卷不存在: examId={}", examId);
                return Result.error("试卷不存在");
            }
            exam.setStatus(0);
            exam.setUpdatedAt(LocalDateTime.now());
            boolean updated = updateById(exam);
            if (updated){
                return Result.success("试卷已删除");
            }else {
                return Result.error("试卷删除失败");
            }
        }catch (Exception e){
            log.error("删除试卷时发生异常: examId={}, error={}", examId, e.getMessage());
            return Result.error("删除试卷时发生未知异常");
        }
    }

    @Override
    public Result<String> updateExam(Exam exam) {
        Long userId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();

        if (!"teacher".equals(role)){
            log.warn("用户暂无权限更新试卷: userId={}", userId);
            return Result.error("暂无权限更新试卷");
        }

        Exam exam1 = getById(exam.getExamId());
        if (exam1 == null){
            log.warn("试卷不存在: examId={}", exam.getExamId());
            return Result.error("试卷不存在");
        }

        if (!exam1.getTeacherId().equals(userId)){
            log.warn("用户无权限更新试卷: userId={}", userId);
            return Result.error("用户无权限更新试卷");
        }

        try {
            exam.setUpdatedAt(LocalDateTime.now());
            if (updateById(exam)){
                return Result.success("试卷更新成功");
            }else {
                return Result.error("试卷更新失败");
            }
        }catch (Exception e){
            log.error("更新试卷时发生异常: exam={}, error={}", exam, e.getMessage());
            return Result.error("更新试卷时发生未知异常");
        }
    }



    @Override
    public Result<IPage<Exam>> pageExams(long current, long size, String examName) {
        Long userId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();

        if (!"teacher".equals(role)) {
            log.warn("用户暂无权限查询试卷: userId={}", userId);
            return Result.error("暂无权限查询试卷");
        }

        try {
            // 创建分页对象
            Page<Exam> page = new Page<>(current, size);
            
            // 构建查询条件
            LambdaQueryWrapper<Exam> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Exam::getStatus, 1);
            queryWrapper.eq(Exam::getTeacherId, userId);
            
            // 如果指定了试卷名称关键词，则进行模糊查询
            if (StringUtils.hasText(examName)) {
                queryWrapper.like(Exam::getExamName, examName);
            }
            
            // 按更新时间降序排序
            queryWrapper.orderByDesc(Exam::getUpdatedAt);
            
            try {
                // 执行分页查询
                IPage<Exam> examPage = page(page, queryWrapper);
                
                // 如果没有查询到数据，返回空的分页对象
                if (examPage == null || examPage.getRecords().isEmpty()) {
                    log.info("未查询到相关试卷数据: 当前页={}, 每页大小={}", current, size);
                    return Result.success("未查询到相关数据", new Page<>());
                }
                
                log.info("分页查询试卷成功: 当前页={}, 每页大小={}, 总记录数={}, 总页数={}", 
                        current, size, examPage.getTotal(), examPage.getPages());
                return Result.success("查询成功", examPage);
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
