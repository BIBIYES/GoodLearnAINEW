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
import com.example.goodlearnai.v1.utils.WordDocumentParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Autowired
    private OpenAiChatModel openAiChatModel;
    
    @Autowired
    private ObjectMapper objectMapper;

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
    @Transactional
    public Result<String> batchCreateQuestions(List<Question> questions) {
        Long userId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();

        if (!"teacher".equals(role)) {
            log.warn("用户暂无权限批量创建题目: userId = {}", userId);
            return Result.error("暂无权限创建题目");
        }
        
        if (CollectionUtils.isEmpty(questions)) {
            log.warn("题目列表为空: userId = {}", userId);
            return Result.error("题目列表不能为空");
        }
        
        // 校验题目信息
        List<Question> validQuestions = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        for (int i = 0; i < questions.size(); i++) {
            Question question = questions.get(i);
            if (question.getBankId() == null) {
                log.warn("第{}个题目的题库ID为空: userId = {}", i+1, userId);
                return Result.error("第" + (i+1) + "个题目的题库ID为空");
            }
            if (question.getContent() == null || question.getContent().trim().isEmpty()) {
                log.warn("第{}个题目的内容为空: userId = {}", i+1, userId);
                return Result.error("第" + (i+1) + "个题目的内容为空");
            }
            
            // 设置创建信息
            question.setCreatedAt(now);
            question.setUpdatedAt(now);
            question.setStatus(true);
            validQuestions.add(question);
        }
        
        try {
            // 批量保存题目
            boolean success = saveBatch(validQuestions);
            if (success) {
                log.info("批量创建题目成功: userId={}, 数量={}", userId, validQuestions.size());
                return Result.success("批量创建成功，共创建" + validQuestions.size() + "道题目");
            } else {
                return Result.error("批量创建失败");
            }
        } catch (Exception e) {
            log.error("批量创建题目异常: userId={}, 数量={}, error={}", userId, validQuestions.size(), e.getMessage(), e);
            throw new CustomException("批量创建题目时发生未知异常");
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
    public Result<IPage<Question>> pageQuestions(long current, long size, Long bankId, String difficulty, String title) {
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
        queryWrapper.eq(Question::getStatus, true);
        
        // 如果指定了题库ID，则按题库ID查询
        if (bankId != null) {
            queryWrapper.eq(Question::getBankId, bankId);
        }
        if(difficulty != null && !difficulty.trim().isEmpty()){
            queryWrapper.eq(Question::getDifficulty, difficulty);
        }
        if(title != null){
            queryWrapper.like(Question::getTitle, title);
        }

        // 按更新时间降序排序
        queryWrapper.orderByDesc(Question::getUpdatedAt);
        
        try {
            // 执行分页查询
            IPage<Question> questionPage = page(page, queryWrapper);
            
            // 如果没有查询到数据，返回空的分页对象
            if (questionPage == null || questionPage.getRecords().isEmpty()) {
                log.info("未查询到相关题目数据: 当前页={}, 每页大小={}", current, size);
                return Result.success("未查询到相关数据", new Page<>());
            }
            
            log.info("分页查询题目成功: 当前页={}, 每页大小={}, 总记录数={}, 总页数={}", 
                    current, size, questionPage.getTotal(), questionPage.getPages());
            return Result.success("查询成功", questionPage);
        } catch (Exception e) {
            log.error("分页查询题目失败", e);
            throw new CustomException("分页查询题目时发生未知异常");
        }
    }

    @Override
    public Flux<ChatResponse> createQuestionByAiStream(String requestData) {
        Long userId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();
        
        if (!"teacher".equals(role)) {
            log.warn("用户暂无权限使用AI创建题目: userId = {}", userId);
            return Flux.error(new RuntimeException("暂无权限使用AI创建题目"));
        }
        
        try {
            // 构建提示词
            String prompt = "你是一个教育问答AI，能够根据用户提供的题目要求，生成多个类似的问题。\n" +
                    "请根据以下用户的需求，生成 **符合要求的相关问题**，并以 **JSON 数组格式** 返回，每道题包含如下字段：\n" +
                    "- `title`：题目标题\n" +
                    "- `content`：题目详情，使用 Markdown 格式书写（例如粗体、列表、表格等）\n" +
                    "- `difficulty`：题目难度，使用整数 1（简单）、2（中等）、3（困难） 表示\n\n" +
                    "请注意：\n" +
                    "1. 保证输出格式是合法的 JSON 数组；\n" +
                    "2. `content` 字段中合理使用 Markdown 语法增强可读性。\n\n" +
                    "用户提供的需求如下：\n" +
                    requestData;


            log.info("AI流式创建题目开始: userId={}", userId);
            
            // 使用Spring AI的流式调用
            return openAiChatModel.stream(new Prompt(prompt))
                    .doOnNext(response -> log.debug("AI流式响应: {}", response))
                    .doOnComplete(() -> log.info("AI流式创建题目完成: userId={}", userId))
                    .doOnError(error -> log.error("AI流式创建题目失败: userId={}", userId, error));
                    
        } catch (Exception e) {
            log.error("AI流式创建题目异常: userId={}", userId, e);
            return Flux.error(new RuntimeException("AI创建题目失败: " + e.getMessage(), e));
        }
    }
    
    @Override
    @Transactional
    public Result<String> createQuestionByAi(String requestData) {
        Long userId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();

        if (!"teacher".equals(role)) {
            log.warn("用户暂无权限使用AI创建题目: userId = {}", userId);
            return Result.error("暂无权限使用AI创建题目");
        }
        try {
            // 构建提示词
            String prompt = "你是一个教育问答AI，能够根据用户提供的题目要求生成多个问题。请根据要求生成符合要求的题目，并以 JSON 格式返回，每道问题包含以下字段：title（题目标题）、content（题目内容）、difficulty（题目难度），题目难度用1、2、3来表示。\n" +
                    "\n" +
                    "用户提供的格式要求如下：\n" +
                    "[\n" +
                    "{\n" +
                    "  \"title\": \"题目标题\",\n" +
                    "  \"content\": \"题目详情\",\n" +
                    "  \"difficulty\": \"题目难度\"\n" +
                    "}\n" +
                    "]\n"+
                    "\n" +
                    "需求：" + requestData;

            // 调用AI服务
            Object aiResponseObj = openAiChatModel.call(prompt);
            String aiResponse = aiResponseObj.toString();
            log.debug("AI响应结果: {}", aiResponse);
            
            // 提取JSON部分，避免AI可能在JSON前后添加的说明文字
            String jsonResponse = extractJsonFromResponse(aiResponse);
            
            log.info("AI创建题目成功: userId={}", userId);
            return Result.success("AI创建题目成功", jsonResponse);
        }catch (Exception e) {
            log.error("AI创建题目失败: userId={}", userId, e);
            return Result.error("AI创建题目失败");
        }
    }
    
    /**
     * 从AI响应中提取JSON部分
     * AI有时会在JSON前后添加说明文字，需要提取出纯JSON
     * @param response AI的原始响应
     * @return 提取出的JSON字符串
     */
    private String extractJsonFromResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            return "[]";
        }
        
        // 尝试查找JSON数组开始和结束位置
        int startIndex = response.indexOf('[');
        int endIndex = response.lastIndexOf(']');
        
        if (startIndex >= 0 && endIndex > startIndex) {
            return response.substring(startIndex, endIndex + 1);
        }
        
        // 如果没有找到数组格式，尝试查找对象格式
        startIndex = response.indexOf('{');
        endIndex = response.lastIndexOf('}');
        
        if (startIndex >= 0 && endIndex > startIndex) {
            return response.substring(startIndex, endIndex + 1);
        }
        
        // 如果都没找到，返回原始响应
        return response;
    }
    
    @Override
    public Flux<ChatResponse> createQuestionByPlan(String requestData, String constraints) {
        Long userId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();
        
        if (!"teacher".equals(role)) {
            log.warn("用户暂无权限使用AI根据教案创建题目: userId = {}", userId);
            return Flux.error(new RuntimeException("暂无权限使用AI创建题目"));
        }
        
        try {
            // 构建针对教案的专用提示词
            String prompt = "你是一个专业的教育AI助手，擅长分析教案内容并生成高质量的教学题目。\n" +
                    "请仔细分析以下教案内容，特别注意：\n" +
                    "- 教案中的表格内容包含了重要的教学信息\n" +
                    "- 【】标记的内容通常是教学要素的标题或分类\n" +
                    "- | 分隔的内容是表格中的不同列信息\n" +
                    "- 重点关注教学目标、教学重点、教学难点、教学方法、教学内容等关键信息\n\n" +
                    "**生成步骤（重要）：**\n" +
                    "第一步：先解析教案，提取出所有的知识点，列出知识点清单\n" +
                    "第二步：根据这些知识点生成对应的练习题目\n\n" +
                    "根据教案的具体内容，生成紧密相关的练习题目：\n" +
                    "1. **深度分析教案内容**：仔细理解教学目标、知识点、技能要求\n" +
                    "2. **精准对应知识点**：确保每道题目都直接对应教案中的具体知识点\n" +
                    "3. **体现教学层次**：根据教案的难度设计不同层次的题目\n" +
                    "4. **结合教学方法**：参考教案中的教学方法设计题目形式\n" +
                    "5. **突出重点难点**：重点针对教案标明的教学重点和难点出题\n\n" +
                    "请以 **JSON 数组格式** 返回生成的题目，每道题包含如下字段：\n" +
                    "- `title`：题目标题（要体现具体的知识点）\n" +
                    "- `content`：题目详情，使用 Markdown 格式，包含具体的题目内容和要求，**不需要显示题目的分数**\n" +
                    "- `difficulty`：题目难度，使用整数 1（基础理解）、2（应用分析）、3（综合运用） 表示\n\n" +
                    "输出要求：\n" +
                    "1. 确保输出格式是合法的 JSON 数组\n" +
                    "2. 题目内容要具体、明确，避免泛泛而谈\n" +
                    "3. 题目要有实际教学和考核价值\n" +
                    "4. 充分利用教案中的具体信息，如课程名称、章节内容、实验步骤等\n" +
                    "5. **题目内容中不要包含分数、评分标准等信息**\n\n" +
                    "6." + constraints + "\n" +
                    "教案内容如下：\n" +
                    requestData;

            log.info("AI流式根据教案创建题目开始: userId={}", userId);
            
            // 使用Spring AI的流式调用
            return openAiChatModel.stream(new Prompt(prompt))
                    .doOnNext(response -> log.debug("AI流式响应: {}", response))
                    .doOnComplete(() -> log.info("AI流式根据教案创建题目完成: userId={}", userId))
                    .doOnError(error -> log.error("AI流式根据教案创建题目失败: userId={}", userId, error));
                    
        } catch (Exception e) {
            log.error("AI流式根据教案创建题目异常: userId={}", userId, e);
            return Flux.error(new RuntimeException("AI根据教案创建题目失败: " + e.getMessage(), e));
        }
    }

    @Override
    public Flux<ChatResponse> createQuestionByWordPlan(MultipartFile file, String constraints) {
        Long userId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();
        
        if (!"teacher".equals(role)) {
            log.warn("用户暂无权限使用AI根据Word教案创建题目: userId = {}", userId);
            return Flux.error(new RuntimeException("暂无权限使用AI创建题目"));
        }
        
        try {
            // 验证文件格式
            if (!WordDocumentParser.isWordDocument(file)) {
                log.warn("上传的文件不是Word文档: fileName = {}", file.getOriginalFilename());
                return Flux.error(new IllegalArgumentException("请上传Word文档(.doc或.docx格式)"));
            }
            
            // 解析Word文档内容
            String documentContent = WordDocumentParser.parseWordDocument(file);
            String cleanedContent = WordDocumentParser.cleanContent(documentContent);
            
            log.info("成功解析Word教案文档: fileName = {}, contentLength = {}, constraints = {}", 
                    file.getOriginalFilename(), cleanedContent.length(), constraints);
            
            // 验证文档内容不为空
            if (cleanedContent.trim().isEmpty()) {
                log.warn("Word文档内容为空: fileName = {}", file.getOriginalFilename());
                return Flux.error(new IllegalArgumentException("Word文档内容为空，请检查文档内容"));
            }
            

            
            // 调用已有的根据教案生成题目的方法
            return createQuestionByPlan(cleanedContent, constraints);
            
        } catch (Exception e) {
            log.error("解析Word教案文档失败: fileName = {}, userId = {}", 
                    file.getOriginalFilename(), userId, e);
            return Flux.error(new RuntimeException("解析Word文档失败: " + e.getMessage(), e));
        }
    }

    @Override
    public Result<String> createQuestionByWordPlanSync(MultipartFile file, String constraints) {
        Long userId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();
        
        if (!"teacher".equals(role)) {
            log.warn("用户暂无权限使用AI根据Word教案创建题目: userId = {}", userId);
            return Result.error("暂无权限使用AI创建题目");
        }
        
        try {
            // 验证文件格式
            if (!WordDocumentParser.isWordDocument(file)) {
                log.warn("上传的文件不是Word文档: fileName = {}", file.getOriginalFilename());
                return Result.error("请上传Word文档(.doc或.docx格式)");
            }
            
            // 解析Word文档内容
            String documentContent = WordDocumentParser.parseWordDocument(file);
            String cleanedContent = WordDocumentParser.cleanContent(documentContent);
            
            log.info("成功解析Word教案文档: fileName = {}, contentLength = {}, constraints = {}", 
                    file.getOriginalFilename(), cleanedContent.length(), constraints);
            
            // 验证文档内容不为空
            if (cleanedContent.trim().isEmpty()) {
                log.warn("Word文档内容为空: fileName = {}", file.getOriginalFilename());
                return Result.error("Word文档内容为空，请检查文档内容");
            }
            
            // 构建针对教案的专用提示词
            StringBuilder promptBuilder = new StringBuilder();
            promptBuilder.append("你是一个专业的教育AI助手，擅长分析教案内容并生成高质量的教学题目。\n")
                    .append("请仔细分析以下教案内容，特别注意：\n")
                    .append("- 教案中的表格内容包含了重要的教学信息\n")
                    .append("- 【】标记的内容通常是教学要素的标题或分类\n")
                    .append("- | 分隔的内容是表格中的不同列信息\n")
                    .append("- 重点关注教学目标、教学重点、教学难点、教学方法、教学内容等关键信息\n\n")
                    .append("**生成步骤（重要）：**\n")
                    .append("第一步：先解析教案，提取出所有的知识点，列出知识点清单\n")
                    .append("第二步：根据这些知识点生成对应的练习题目\n\n");
            
            // 如果有用户限制条件，添加到提示词中
            if (constraints != null && !constraints.trim().isEmpty()) {
                promptBuilder.append("**用户的特殊要求和限制条件：**\n")
                        .append(constraints)
                        .append("\n\n请严格按照以上用户要求生成题目。\n\n");
            }
            
            promptBuilder.append("根据教案的具体内容，生成紧密相关的练习题目：\n")
                    .append("1. **深度分析教案内容**：仔细理解教学目标、知识点、技能要求\n")
                    .append("2. **精准对应知识点**：确保每道题目都直接对应教案中的具体知识点\n")
                    .append("3. **体现教学层次**：根据教案的难度设计不同层次的题目\n")
                    .append("4. **结合教学方法**：参考教案中的教学方法设计题目形式\n")
                    .append("5. **突出重点难点**：重点针对教案标明的教学重点和难点出题\n\n")
                    .append("请以 **JSON 数组格式** 返回生成的题目，每道题包含如下字段：\n")
                    .append("- `title`：题目标题（要体现具体的知识点）\n")
                    .append("- `content`：题目详情，使用 Markdown 格式，包含具体的题目内容和要求，**不需要显示题目的分数**\n")
                    .append("- `difficulty`：题目难度，使用整数 1（基础理解）、2（应用分析）、3（综合运用） 表示\n\n")
                    .append("输出要求：\n")
                    .append("1. 确保输出格式是合法的 JSON 数组\n")
                    .append("2. 题目内容要具体、明确，避免泛泛而谈\n")
                    .append("3. 生成5-8道题目，覆盖教案的主要知识点\n")
                    .append("4. 题目要有实际教学和考核价值\n")
                    .append("5. 充分利用教案中的具体信息，如课程名称、章节内容、实验步骤等\n")
                    .append("6. **题目内容中不要包含分数、评分标准等信息**\n\n")
                    .append("教案内容如下：\n")
                    .append(cleanedContent);
            
            String prompt = promptBuilder.toString();

            log.info("AI根据Word教案创建题目开始: userId={}, fileName={}", userId, file.getOriginalFilename());
            
            // 调用AI服务
            Object aiResponseObj = openAiChatModel.call(prompt);
            String aiResponse = aiResponseObj.toString();
            log.debug("AI响应结果: {}", aiResponse);
            
            // 提取JSON部分，避免AI可能在JSON前后添加的说明文字
            String jsonResponse = extractJsonFromResponse(aiResponse);
            
            log.info("AI根据Word教案创建题目成功: userId={}, fileName={}", userId, file.getOriginalFilename());
            return Result.success("AI根据Word教案创建题目成功", jsonResponse);
            
        } catch (Exception e) {
            log.error("AI根据Word教案创建题目失败: fileName = {}, userId = {}", 
                    file.getOriginalFilename(), userId, e);
            return Result.error("AI根据Word教案创建题目失败: " + e.getMessage());
        }
    }

    @Override
    public Result<List<Question>> getAllQuestionsByBankId(Long bankId, String keyword) {
        Long userId = AuthUtil.getCurrentUserId();
        String role = AuthUtil.getCurrentRole();

        if (!"teacher".equals(role)) {
            log.warn("用户暂无权限查询题目: userId={}", userId);
            return Result.error("暂无权限查询题目");
        }

        if (bankId == null) {
            return Result.error("题库ID不能为空");
        }

        try {
            // 构建查询条件
            LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Question::getStatus, true)
                    .eq(Question::getBankId, bankId);

            // 如果提供了关键词，则在标题和题干中搜索
            if (keyword != null && !keyword.trim().isEmpty()) {
                queryWrapper.and(wrapper -> wrapper
                        .like(Question::getTitle, keyword)
                        .or()
                        .like(Question::getContent, keyword)
                );
            }

            // 按更新时间降序排序
            queryWrapper.orderByDesc(Question::getUpdatedAt);

            List<Question> questions = list(queryWrapper);
            
            log.info("查询题库下所有题目成功: 题库ID={}, 关键词={}, 题目数量={}", bankId, keyword, questions.size());
            return Result.success("查询成功", questions);
        } catch (Exception e) {
            log.error("查询题库下所有题目失败", e);
            throw new CustomException("查询题库下所有题目时发生未知异常");
        }
    }
}
