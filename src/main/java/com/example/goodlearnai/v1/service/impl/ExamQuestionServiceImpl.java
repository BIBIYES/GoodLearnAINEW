package com.example.goodlearnai.v1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.dto.ExamQuestionDto;
import com.example.goodlearnai.v1.entity.ExamQuestion;
import com.example.goodlearnai.v1.entity.Question;
import com.example.goodlearnai.v1.mapper.ExamQuestionMapper;
import com.example.goodlearnai.v1.mapper.QuestionMapper;
import com.example.goodlearnai.v1.service.IExamQuestionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.goodlearnai.v1.utils.AuthUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 试卷题目表（存储题目快照） 服务实现类
 * </p>
 *
 * @author DSfeiji
 * @since 2025-04-16
 */
@Service
@Slf4j
public class ExamQuestionServiceImpl extends ServiceImpl<ExamQuestionMapper, ExamQuestion> implements IExamQuestionService {

    @Autowired
    private QuestionMapper questionMapper;

    @Override
    public Result<String> createExamQuestion(ExamQuestionDto examQuestionDto) {
        Long userId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();
        List<Long> questionId = examQuestionDto.getQuestionId();
        Long examId = examQuestionDto.getExamId();

        // 判断是否为老师角色
        if (!"teacher".equals(role)) {
            log.warn("用户暂无权限创建试卷: userId={}", userId);
            return Result.error("暂无权限创建试卷");
        }

        List<ExamQuestion> examQuestionList = new java.util.ArrayList<>();
        try {
            for (Long qId : questionId) {
                LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(Question::getQuestionId, qId);
                Question question = questionMapper.selectOne(wrapper);
                if (question == null) {
                    log.warn("题目不存在: questionId={}", qId);
                    // 可以选择继续处理其他题目或直接返回错误
                    // return Result.error("题目不存在，无法创建试卷: " + qId);
                    continue; // 跳过不存在的题目
                }
                ExamQuestion examQuestionSnap = new ExamQuestion();
                examQuestionSnap.setQuestionContent(question.getContent());
                examQuestionSnap.setReferenceAnswer(question.getAnswer());
                examQuestionSnap.setDifficulty(question.getDifficulty());
                examQuestionSnap.setOriginalQuestionId(qId);
                examQuestionSnap.setExamId(examId);
                examQuestionSnap.setCreatedAt(LocalDateTime.now());
                examQuestionSnap.setStatus(1);
                examQuestionList.add(examQuestionSnap);
            }

            if (examQuestionList.isEmpty()) {
                log.warn("没有有效的题目可以添加到试卷: examId={}", examId);
                return Result.error("没有有效的题目可以添加到试卷");
            }

            boolean success = saveBatch(examQuestionList);
            if (success) {
                return Result.success("创建成功");
            } else {
                log.error("批量保存试卷题目失败: examId={}", examId);
                return Result.error("创建试卷题目失败");
            }
        } catch (Exception e) {
            log.error("创建试卷题目时发生异常: examId={}", examId, e);
            throw new RuntimeException("创建题目时发生未知异常");
        }
    }
}
