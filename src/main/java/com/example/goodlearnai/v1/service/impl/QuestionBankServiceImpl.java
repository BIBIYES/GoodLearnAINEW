package com.example.goodlearnai.v1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.entity.QuestionBank;
import com.example.goodlearnai.v1.exception.CustomException;
import com.example.goodlearnai.v1.mapper.QuestionBankMapper;
import com.example.goodlearnai.v1.service.IQuestionBankService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.goodlearnai.v1.utils.AuthUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Slf4j
@Service
public class QuestionBankServiceImpl extends ServiceImpl<QuestionBankMapper, QuestionBank> implements IQuestionBankService {

    @Override
    public Result<String> createQuestionBank(QuestionBank questionBank) {
        Long userId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();

        // 判断是否为老师角色
        if (!"teacher".equals(role)) {
            log.warn("用户暂无权限创建题库: userId={}", userId);
            return Result.error("暂无权限创建题库");
        }

        // 验证必要字段
        if (!StringUtils.hasText(questionBank.getBankName())) {
            return Result.error("题库名称不能为空");
        }

        // 检查题库名称是否重复
        LambdaQueryWrapper<QuestionBank> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionBank::getBankName, questionBank.getBankName())
                .eq(QuestionBank::getTeacherId, userId)
                .eq(QuestionBank::getStatus, true);
        if (count(wrapper) > 0) {
            return Result.error("题库名称已存在");
        }

        // 设置创建信息
        questionBank.setTeacherId(userId);
        questionBank.setCreatedAt(LocalDateTime.now());
        questionBank.setUpdatedAt(LocalDateTime.now());
        questionBank.setStatus(true);

        try {
            if (save(questionBank)) {
                log.info("题库创建成功: bankId={}, bankName={}", questionBank.getBankId(), questionBank.getBankName());
                return Result.success("创建题库成功");
            }
            return Result.error("创建题库失败");
        } catch (Exception e) {
            log.error("创建题库时发生异常", e);
            throw new CustomException("创建题库时发生未知异常");
        }
    }

    @Override
    public Result<String> deleteQuestionBank(QuestionBank questionBank) {
        Long userId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();

        if (!"teacher".equals(role)) {
            log.warn("用户暂无权限删除题库: userId={}", userId);
            return Result.error("暂无权限删除题库");
        }

        // 验证题库是否存在且属于当前教师
        QuestionBank existingBank = getById(questionBank.getBankId());
        if (existingBank == null) {
            return Result.error("题库不存在");
        }
        if (!existingBank.getTeacherId().equals(userId)) {
            log.warn("用户试图删除非本人创建的题库: userId={}, bankId={}", userId, questionBank.getBankId());
            return Result.error("只能删除自己创建的题库");
        }

        try {
            // 设置题库状态为已删除
            existingBank.setStatus(false);
            existingBank.setUpdatedAt(LocalDateTime.now());
            boolean updated = updateById(existingBank);
            if (updated) {
                log.info("题库删除成功: bankId={}", questionBank.getBankId());
                return Result.success("题库已删除");
            } else {
                return Result.error("题库删除失败");
            }
        } catch (Exception e) {
            log.error("删除题库时发生异常: bankId={}", questionBank.getBankId(), e);
            throw new CustomException("删除题库时发生未知异常");
        }
    }

    @Override
    public Result<String> updateQuestionBank(QuestionBank questionBank) {
        Long userId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();

        if (!"teacher".equals(role)) {
            log.warn("用户暂无权限更新题库: userId={}", userId);
            return Result.error("暂无权限更新题库");
        }

        // 验证题库是否存在且属于当前教师
        QuestionBank existingBank = getById(questionBank.getBankId());
        if (existingBank == null) {
            return Result.error("题库不存在");
        }
        if (!existingBank.getTeacherId().equals(userId)) {
            log.warn("用户试图更新非本人创建的题库: userId={}, bankId={}", userId, questionBank.getBankId());
            return Result.error("只能更新自己创建的题库");
        }

        // 验证必要字段
        if (!StringUtils.hasText(questionBank.getBankName())) {
            return Result.error("题库名称不能为空");
        }

        // 检查更新的题库名称是否与其他题库重复
        LambdaQueryWrapper<QuestionBank> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionBank::getBankName, questionBank.getBankName())
                .eq(QuestionBank::getTeacherId, userId)
                .eq(QuestionBank::getStatus, true)
                .ne(QuestionBank::getBankId, questionBank.getBankId());
        if (count(wrapper) > 0) {
            return Result.error("题库名称已存在");
        }

        try {
            // 更新题库信息
            questionBank.setTeacherId(existingBank.getTeacherId());
            questionBank.setUpdatedAt(LocalDateTime.now());
            questionBank.setStatus(true);
            boolean updated = updateById(questionBank);
            if (updated) {
                log.info("题库更新成功: bankId={}", questionBank.getBankId());
                return Result.success("题库更新成功");
            } else {
                return Result.error("题库更新失败");
            }
        } catch (Exception e) {
            log.error("更新题库时发生异常: bankId={}", questionBank.getBankId(), e);
            throw new CustomException("更新题库时发生未知异常");
        }
    }



    @Override
    public Result<IPage<QuestionBank>> pageQuestionBanks(long current, long size, String bankName) {
        Long userId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();

        if (!"teacher".equals(role)){
            log.warn("用户暂无权限查询题库: userId={}", userId);
            return Result.error("暂无权限查询题库");
        }

        try {
            // 构建查询条件
            Page<QuestionBank> page = new Page<>(current, size);
            LambdaQueryWrapper<QuestionBank> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(QuestionBank::getStatus, true);


            // 如果指定了题库名称关键词，则进行模糊查询
            if (StringUtils.hasText(bankName)) {
                queryWrapper.like(QuestionBank::getBankName, bankName);
            }

            // 按更新时间降序排序
            queryWrapper.orderByDesc(QuestionBank::getUpdatedAt);

            try {
                // 执行分页查询
                IPage<QuestionBank> questionBankPage = page(page, queryWrapper);
                log.info("分页查询题库成功: 当前页={}, 每页大小={}, 总记录数={}, 总页数={}",
                        current, size, questionBankPage.getTotal(), questionBankPage.getPages());
                return Result.success("查询成功", questionBankPage);
            } catch (Exception e) {
                log.error("分页查询题库失败", e);
                throw new CustomException("分页查询题库时发生未知异常");
            }
        } catch (CustomException e) {
            throw new RuntimeException(e);
        }
    }

}
