package com.example.goodlearnai.v1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.ClassExamQuestion;
import com.example.goodlearnai.v1.mapper.ClassExamQuestionMapper;
import com.example.goodlearnai.v1.service.IClassExamQuestionService;
import com.example.goodlearnai.v1.vo.ClassExamQuestionVO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 班级试卷副本题目表 服务实现类
 * </p>
 *
 * @author DSfeiji
 * @since 2025-10-18
 */
@Service
@Slf4j
public class ClassExamQuestionServiceImpl extends ServiceImpl<ClassExamQuestionMapper, ClassExamQuestion> implements IClassExamQuestionService {

    @Override
    public Result<IPage<ClassExamQuestionVO>> pageClassExamQuestions(Long classExamId, long current, long size) {
        try {
            // 创建分页对象
            Page<ClassExamQuestion> page = new Page<>(current, size);
            
            // 构建查询条件
            LambdaQueryWrapper<ClassExamQuestion> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ClassExamQuestion::getClassExamId, classExamId);
            queryWrapper.eq(ClassExamQuestion::getStatus, 1);
            queryWrapper.orderByAsc(ClassExamQuestion::getCeqId);
            
            // 执行分页查询
            IPage<ClassExamQuestion> questionPage = page(page, queryWrapper);
            
            // 将Entity转换为VO（只返回需要的字段）
            IPage<ClassExamQuestionVO> voPage = questionPage.convert(entity -> {
                ClassExamQuestionVO vo = new ClassExamQuestionVO();
                vo.setCeqId(entity.getCeqId());
                vo.setClassExamId(entity.getClassExamId());
                vo.setQuestionTitle(entity.getQuestionTitle());
                vo.setQuestionContent(entity.getQuestionContent());
                return vo;
            });
            
            log.info("分页查询班级试卷副本题目成功: classExamId={}, 当前页={}, 每页大小={}, 总记录数={}", 
                    classExamId, current, size, voPage.getTotal());
            return Result.success("查询成功", voPage);
        } catch (Exception e) {
            log.error("分页查询班级试卷副本题目失败: classExamId={}, error={}", classExamId, e.getMessage(), e);
            return Result.error("查询题目时发生未知异常");
        }
    }
}

