package com.example.goodlearnai.v1.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.dto.CreateQuestionWithOptionsDto;
import com.example.goodlearnai.v1.dto.QuestionWithOptionsDto;
import com.example.goodlearnai.v1.entity.Question;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * <p>
 * 题库中的题目表（全为简答题） 服务类
 * </p>
 *
 * @author DSfeiji
 * @since 2025-04-14
 */
public interface IQuestionService extends IService<Question> {
    
    /**
     * 创建单个题目
     * @param question 题目信息
     * @return 创建结果
     */
    Result<String> createQuestion(Question question);
    
    /**
     * 批量创建题目
     * @param questions 题目列表
     * @return 创建结果
     */
    Result<String> batchCreateQuestions(List<Question> questions);

    /**
     * 删除题目（软删除）
     * @param questionId 题目ID
     * @return 删除结果
     */
    Result<String> deleteQuestion(Long questionId);

    /**
     * 更新题目
     * @param question 题目信息
     * @return 更新结果
     */
    Result<String> updateQuestion(Question question);
    
    /**
     * 分页查询题目
     * @param current 当前页码
     * @param size 每页大小
     * @param bankId 题库ID（可选）
     * @return 分页结果
     */
    Result<IPage<Question>> pageQuestions(long current, long size, Long bankId ,String difficulty, String title);

    /**
     * 通过AI创建题目（流式响应）- 旧版本，仅支持简答题
     * @param question 题目要求描述
     * @return AI生成的题目列表（流式响应）
     * @deprecated 建议使用 createQuestionByAiStreamWithType
     */
    @Deprecated
    Flux<ChatResponse> createQuestionByAiStream(String question);

    /**
     * 根据教案生成题目（流式响应）
     * @param requestData 教案内容
     * @param constraints 限制条件
     * @param questionType 题目类型
     * @return AI生成的题目列表（流式响应）
     */
    Flux<ChatResponse> createQuestionByPlan(String requestData, String constraints, String questionType);

    /**
     * 上传Word教案文档并AI生成题目（流式响应）
     * @param file Word文档文件
     * @param constraints 题目生成的限制条件（例如：题目数量、难度、重点内容等）
     * @param questionType 题目类型：essay-简答题，single_choice-单选题，multiple_choice-多选题，true_false-判断题
     * @return AI生成的题目列表（流式响应）
     */
    Flux<ChatResponse> createQuestionByWordPlan(org.springframework.web.multipart.MultipartFile file, String constraints, String questionType);

    /**
     * 上传Word教案文档并AI生成题目（非流式响应）
     * @param file Word文档文件
     * @param constraints 题目生成的限制条件（例如：题目数量、难度、重点内容等）
     * @param questionType 题目类型：essay-简答题，single_choice-单选题，multiple_choice-多选题，true_false-判断题
     * @return AI生成的题目列表（JSON格式）
     */
    Result<String> createQuestionByWordPlanSync(org.springframework.web.multipart.MultipartFile file, String constraints, String questionType);

    /**
     * 通过AI创建题目（非流式响应）- 旧版本，仅支持简答题
     * @param question 题目要求描述
     * @return AI生成的题目列表（JSON格式）
     * @deprecated 建议使用 createQuestionByAiStreamWithType
     */
    @Deprecated
    Result<String> createQuestionByAi(String question);
    
    /**
     * 获取题库下所有题目不分页（支持标题、题干搜索）
     * @param bankId 题库ID
     * @param keyword 搜索关键词（可选，用于搜索标题和题干）
     * @return 题目列表
     */
    Result<List<Question>> getAllQuestionsByBankId(Long bankId, String keyword);
    
    /**
     * 创建带选项的题目（选择题、判断题等）
     * @param dto 题目和选项信息
     * @return 创建结果
     */
    Result<String> createQuestionWithOptions(CreateQuestionWithOptionsDto dto);
    
    /**
     * 获取带选项的题目详情
     * @param questionId 题目ID
     * @return 题目和选项信息
     */
    Result<QuestionWithOptionsDto> getQuestionWithOptions(Long questionId);
    
    /**
     * 通过AI创建题目 - 支持指定题目类型（简答题或选择题）（流式响应）
     * @param requestData 题目要求描述（包含题目类型、数量等参数）
     * @param questionType 题目类型：essay-简答题，single_choice-单选题，multiple_choice-多选题
     * @return AI生成的题目列表（流式响应）
     */
    Flux<ChatResponse> createQuestionByAiStreamWithType(String requestData, String questionType);
}
