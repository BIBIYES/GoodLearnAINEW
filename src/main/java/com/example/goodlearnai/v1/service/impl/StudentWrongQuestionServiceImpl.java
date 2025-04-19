package com.example.goodlearnai.v1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.dto.WrongQuestionDetailDto;
import com.example.goodlearnai.v1.entity.ExamQuestion;
import com.example.goodlearnai.v1.entity.StudentAnswer;
import com.example.goodlearnai.v1.entity.StudentWrongQuestion;
import com.example.goodlearnai.v1.exception.CustomException;
import com.example.goodlearnai.v1.mapper.ExamQuestionMapper;
import com.example.goodlearnai.v1.mapper.StudentAnswerMapper;
import com.example.goodlearnai.v1.mapper.StudentWrongQuestionMapper;
import com.example.goodlearnai.v1.service.IStudentWrongQuestionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.goodlearnai.v1.utils.AuthUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 学生错题汇总表：记录错题信息 服务实现类
 * </p>
 *
 * @author DSfeiji
 * @since 2025-04-19
 */
@Service
@Slf4j
public class StudentWrongQuestionServiceImpl extends ServiceImpl<StudentWrongQuestionMapper, StudentWrongQuestion> implements IStudentWrongQuestionService {

    @Autowired
    private ExamQuestionMapper examQuestionMapper;
    
    @Autowired
    private StudentAnswerMapper studentAnswerMapper;

    @Override
    public Result<IPage<StudentWrongQuestion>> pageStudentWrongQuestions(Long userId, long current, long size) {
        try {
            // 创建分页对象
            Page<StudentWrongQuestion> page = new Page<>(current, size);
            
            // 构建查询条件
            LambdaQueryWrapper<StudentWrongQuestion> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(StudentWrongQuestion::getUserId, userId);
            
            // 执行分页查询
            IPage<StudentWrongQuestion> wrongQuestionPage = page(page, queryWrapper);
            
            // 如果没有查询到数据，返回空的分页对象
            if (wrongQuestionPage == null || wrongQuestionPage.getRecords().isEmpty()) {
                log.info("未查询到相关错题记录: userId={}, 当前页={}, 每页大小={}", 
                        userId, current, size);
                return Result.success("未查询到相关数据", new Page<>());
            }
            
            log.info("分页查询学生错题记录成功: userId={}, 当前页={}, 每页大小={}, 总记录数={}, 总页数={}", 
                    userId, current, size, wrongQuestionPage.getTotal(), wrongQuestionPage.getPages());
            return Result.success("查询成功", wrongQuestionPage);
        } catch (Exception e) {
            log.error("分页查询学生错题记录异常: userId={}, error={}", userId, e.getMessage(), e);
            throw new CustomException("分页查询学生错题记录时发生未知异常");
        }
    }
    
    @Override
    public Result<IPage<WrongQuestionDetailDto>> pageWrongQuestionDetails(Long userId, long current, long size) {
        try {
            // 创建分页对象
            Page<StudentWrongQuestion> page = new Page<>(current, size);
            
            // 构建查询条件
            LambdaQueryWrapper<StudentWrongQuestion> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(StudentWrongQuestion::getUserId, userId);
            
            // 执行分页查询
            IPage<StudentWrongQuestion> wrongQuestionPage = page(page, queryWrapper);
            
            // 如果没有查询到数据，返回空的分页对象
            if (wrongQuestionPage == null || wrongQuestionPage.getRecords().isEmpty()) {
                log.info("未查询到相关错题记录: userId={}, 当前页={}, 每页大小={}", 
                        userId, current, size);
                return Result.success("未查询到相关数据", new Page<WrongQuestionDetailDto>());
            }
            
            // 组装返回结果
            Page<WrongQuestionDetailDto> resultPage = new Page<>();
            BeanUtils.copyProperties(wrongQuestionPage, resultPage, "records");
            
            List<WrongQuestionDetailDto> detailList = new ArrayList<>();
            for (StudentWrongQuestion wrongQuestion : wrongQuestionPage.getRecords()) {
                WrongQuestionDetailDto detailDto = new WrongQuestionDetailDto();
                // 复制错题基本信息
                BeanUtils.copyProperties(wrongQuestion, detailDto);
                
                // 获取最近的作答时间信息
                LambdaQueryWrapper<StudentAnswer> answerWrapper = new LambdaQueryWrapper<>();
                answerWrapper.eq(StudentAnswer::getUserId, userId)
                        .eq(StudentAnswer::getEqId, wrongQuestion.getEqId())
                        .orderByDesc(StudentAnswer::getAnsweredAt)
                        .last("LIMIT 1");
                StudentAnswer recentAnswer = studentAnswerMapper.selectOne(answerWrapper);
                if (recentAnswer != null) {
                    detailDto.setAnsweredAt(recentAnswer.getAnsweredAt());
                }
                
                // 获取题目难度等额外信息
                ExamQuestion question = examQuestionMapper.selectById(wrongQuestion.getEqId());
                if (question != null) {
                    try {
                        Integer difficultyLevel = null;
                        String difficultyStr = question.getDifficulty();
                        // 尝试转换difficulty为Integer
                        if (difficultyStr != null) {
                            difficultyLevel = Integer.parseInt(difficultyStr);
                        }
                        detailDto.setDifficulty(difficultyLevel);
                    } catch (NumberFormatException e) {
                        // 如果转换失败，设置为null
                        detailDto.setDifficulty(null);
                        log.warn("题目难度解析失败: {}", question.getDifficulty());
                    }
                    detailDto.setOriginalQuestionId(question.getOriginalQuestionId());
                }
                
                detailList.add(detailDto);
            }
            
            resultPage.setRecords(detailList);
            
            log.info("分页查询学生错题详情成功: userId={}, 当前页={}, 每页大小={}, 总记录数={}, 总页数={}", 
                    userId, current, size, resultPage.getTotal(), resultPage.getPages());
            return Result.success("查询成功", resultPage);
        } catch (Exception e) {
            log.error("分页查询学生错题详情异常: userId={}, error={}", userId, e.getMessage(), e);
            throw new CustomException("分页查询学生错题详情时发生未知异常");
        }
    }
}
