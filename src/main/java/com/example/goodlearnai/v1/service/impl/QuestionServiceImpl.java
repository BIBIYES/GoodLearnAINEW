package com.example.goodlearnai.v1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.Question;
import com.example.goodlearnai.v1.exception.CustomException;
import com.example.goodlearnai.v1.mapper.QuestionMapper;
import com.example.goodlearnai.v1.service.IQuestionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.goodlearnai.v1.utils.AuthUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * <p>
 * 题库中的题目表（全为简答题） 服务实现类
 * </p>
 *
 * @author DSfeiji
 * @since 2025-04-14
 */
@Service
@Slf4j
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements IQuestionService {

    @Override
    public Result<String> createQuestion(Question question) {
        Long userId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();

        if (!"teacher".equals(role)) {
            log.warn("用户暂无权限创建题目: userId = {}", userId);
            return Result.error("暂无权限创建题目");
        }
        if (question.getBankId() == null) {
            log.warn("题库ID为空: userId = {}" , userId);
            return Result.error("题库ID为空");
        }
        if (question.getContent() == null) {
            log.warn("题目内容为空: userId = {}", userId);
            return Result.error("题目内容为空");
        }
        question.setCreatedAt(LocalDateTime.now());
        question.setUpdatedAt(LocalDateTime.now());
        question.setStatus(true);
        try {
            if (save(question)){
                return Result.success("创建成功");
            }
            return Result.error("创建失败");
        }catch (Exception e){
            log.error("创建失败", e);
            throw new CustomException("创建题目时发生未知异常");
        }
    }

    @Override
    public Result<String> deleteQuestion(Long questionId) {
        Long userId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();

        if (!"teacher".equals(role)) {
            log.warn("用户暂无权限删除题目: userId = {}", userId);
            return Result.error("暂无权限删除题目");
        }

        Question question = getById(questionId);
        if (question == null) {
            log.warn("题目不存在: questionId = {}", questionId);
            return Result.error("题目不存在");
        }

        try {
            question.setStatus(false);
            question.setUpdatedAt(LocalDateTime.now());
            boolean updated = updateById(question);
            if (updated) {
                log.info("题库删除成功: bankId={}", question.getBankId());
                return Result.success("题库已删除");
            } else {
                return Result.error("题库删除失败");
            }
        } catch (Exception e) {
            log.error("删除题目失败", e);
            throw new CustomException("删除题目时发生未知异常");
        }
    }

    @Override
    public Result<String> updateQuestion(Question question) {
        Long userId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();

        if (!"teacher".equals(role)) {
            log.warn("用户暂无权限更新题目: userId = {}", userId);
            return Result.error("暂无权限更新题目");
        }

        if (question.getQuestionId() == null) {
            log.warn("题目ID为空: userId = {}", userId);
            return Result.error("题目ID为空");
        }

        Question existingQuestion = getById(question.getQuestionId());
        if (existingQuestion == null) {
            log.warn("题目不存在: questionId = {}", question.getQuestionId());
            return Result.error("题目不存在");
        }

        if (question.getContent() == null) {
            log.warn("题目内容为空: userId = {}", userId);
            return Result.error("题目内容为空");
        }

        question.setUpdatedAt(LocalDateTime.now());
        try {
            if (updateById(question)) {
                return Result.success("更新成功");
            }
            return Result.error("更新失败");
        } catch (Exception e) {
            log.error("更新题目失败", e);
            throw new CustomException("更新题目时发生未知异常");
        }
    }

    @Override
    public Result<IPage<Question>> pageQuestions(long current, long size, Long bankId, String content) {
        Long userId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();

        if (!"teacher".equals(role)){
            log.warn("用户暂无权限查询题库: userId={}", userId);
            return Result.error("暂无权限查询题库");
        }

        // 创建分页对象
        Page<Question> page = new Page<>(current, size);
        
        // 构建查询条件
        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Question::getStatus, true)
                .inSql(Question::getBankId, 
                    "SELECT bank_id FROM question_bank WHERE teacher_id = " + userId + " AND status = 1");
        
        // 如果指定了题库ID，则按题库ID查询
        if (bankId != null) {
            queryWrapper.eq(Question::getBankId, bankId);
        }
        
        // 如果指定了内容关键词，则进行模糊查询
        if (StringUtils.hasText(content)) {
            queryWrapper.like(Question::getContent, content);
        }
        
        // 按更新时间降序排序
        queryWrapper.orderByDesc(Question::getUpdatedAt);
        
        try {
            // 执行分页查询
            IPage<Question> questionPage = page(page, queryWrapper);
            log.info("分页查询题目成功: 当前页={}, 每页大小={}, 总记录数={}, 总页数={}", 
                    current, size, questionPage.getTotal(), questionPage.getPages());
            return Result.success("查询成功", questionPage);
        } catch (Exception e) {
            log.error("分页查询题目失败", e);
            throw new CustomException("分页查询题目时发生未知异常");
        }
    }
}
