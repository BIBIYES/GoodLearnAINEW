package com.example.goodlearnai.v1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.dto.UserChat;
import com.example.goodlearnai.v1.entity.Chat;
import com.example.goodlearnai.v1.entity.ChatHistory;
import com.example.goodlearnai.v1.mapper.ChatHistoryMapper;
import com.example.goodlearnai.v1.mapper.ChatMapper;
import com.example.goodlearnai.v1.service.IChatHistoryService;
import com.example.goodlearnai.v1.service.IChatService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;


import com.example.goodlearnai.v1.utils.AuthUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author mouse
 * @since 2025-04-05
 */
@Service
@Slf4j
public class ChatServiceImpl extends ServiceImpl<ChatMapper, Chat> implements IChatService {
    @Resource
    private IChatHistoryService iChatHistoryService;

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private ChatModel chatModel;

    @Autowired
    private List<McpSyncClient> mcpSyncClients;

    @Autowired
    private ChatHistoryMapper chatHistoryMapper;

    @Autowired
    private ChatMapper chatMapper;

    @Override
    public boolean chat(UserChat userChat) {
        if (isNullOrEmpty(userChat.getContent())) {
            log.warn("空消息");
            return false;
        }
        
        String sessionId = userChat.getSessionId();
        // 获取会话
        Chat chat = getById(sessionId);
        // 如果会话不存在责创建会话
        if (chat == null) {
            chat = new Chat();
            chat.setSessionId(sessionId);
            chat.setSessionName(userChat.getSessionName());
            chat.setUserId(AuthUtil.getCurrentUserId());
            boolean flag = save(chat);
            if (flag) {
                log.info("会话不存在，创建会话");
            } else {
                log.warn("会话创建失败");
                return false;
            }
        }
        log.info("添加到会话历史纪录{}", userChat.getContent());
        ChatHistory chatHistory = new ChatHistory();
        chatHistory.setContent(userChat.getContent());
        chatHistory.setSessionId(userChat.getSessionId());
        chatHistory.setRole(userChat.getRole());
        chatHistory.setUserId(AuthUtil.getCurrentUserId());
        return iChatHistoryService.addChatHistory(chatHistory);
    }

    @Override
    public Result<List<Chat>> getChatHistory() {

        LambdaQueryWrapper<Chat> chatLambdaQueryWrapper = new LambdaQueryWrapper<>();
        chatLambdaQueryWrapper.eq(Chat::getUserId, AuthUtil.getCurrentUserId());
        List<Chat> chatList = list(chatLambdaQueryWrapper);
        if (chatList.isEmpty()) {
            return Result.success("暂无会话");
        }
        return Result.success("获取历史会话成功", chatList);
    }

    @Override
    public Result<String> updateSessionName(Chat chat) {
        log.debug("修改会话名称{}",chat);
        boolean flag = updateById(chat);
        if(flag){
            return Result.success("修改成功");
        }
        return Result.error("修改失败");
    }
    
    /**
     * 保存AI回答到数据库
     * @param sessionId 会话ID
     * @param content AI回答内容
     * @return 保存结果
     */
    private boolean saveAiResponse(String sessionId, String content) {
        if (isNullOrEmpty(sessionId) || isNullOrEmpty(content)) {
            log.warn("会话ID或内容为空，无法保存AI回答");
            return false;
        }
        
        ChatHistory chatHistory = new ChatHistory();
        chatHistory.setContent(content);
        chatHistory.setSessionId(sessionId);
        chatHistory.setRole("assistant"); // AI的角色设为assistant
        chatHistory.setUserId(AuthUtil.getCurrentUserId());
        return iChatHistoryService.addChatHistory(chatHistory);
    }
    
    /**
     * 获取指定会话的聊天历史记录
     * @param sessionId 会话ID
     * @return 聊天历史记录
     */
    private String getChatHistoryBySessionId(String sessionId) {
        if (isNullOrEmpty(sessionId)) {
            return "";
        }
        
        LambdaQueryWrapper<ChatHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatHistory::getSessionId, sessionId)
                .orderByAsc(ChatHistory::getCreateTime);
        
        List<ChatHistory> historyList = chatHistoryMapper.selectList(queryWrapper);
        
        StringBuilder history = new StringBuilder();
        for (ChatHistory record : historyList) {
            history.append(record.getRole()).append(": ").append(record.getContent()).append("\n");
        }
        
        return history.toString();
    }
    
    @Override
    public Map<String, Object> McpChat(String message) throws JsonProcessingException {
        McpSyncClient fs = mcpSyncClients.get(0);
        
        // 查找会话ID - 修改为使用selectList
        LambdaQueryWrapper<Chat> chatLambdaQueryWrapper = new LambdaQueryWrapper<>();
        chatLambdaQueryWrapper.eq(Chat::getSessionName, message);
        List<Chat> chatList = chatMapper.selectList(chatLambdaQueryWrapper);
        
        if (chatList == null || chatList.isEmpty()) {
            // 统一返回Map
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("code", 500);
            errorMap.put("message", "会话不存在");
            errorMap.put("data", null);
            return errorMap;
        }
        
        // 如果有多条记录，取第一条
        Chat chat = chatList.get(0);
        if (chatList.size() > 1) {
            log.warn("查询到多条相同sessionName的记录，使用第一条记录。sessionName={}, 记录数={}", message, chatList.size());
        }
        
        String sessionId = chat.getSessionId();

        // —— 1. 初始化数据库连接 & 列表表格 ——
        fs.callTool(new McpSchema.CallToolRequest("connect_db", Map.of(
                "host", "198.23.201.122",
                "user", "root",
                "password", "zzx",
                "database", "good_learn_ai"
        )));
        McpSchema.CallToolResult list = fs.callTool(new McpSchema.CallToolRequest("list_tables", Collections.emptyMap()));

        // —— 2. 列出可用工具 & 构造初始 Prompt ——
        McpSchema.ListToolsResult tools = fs.listTools();
        String initialPrompt = buildToolSelectionPrompt(tools, message , list);

        // —— 3. 调用 AI 获得第一次 "工具调用指令" ——
        String aiRaw = chatModel.call(initialPrompt);
        System.out.println(aiRaw);

        // —— 4. 解析-执行-验证 循环 ——
        String finalAnswer = processAiLoop(fs, aiRaw, sessionId, message);

        // —— 5. 保存AI回答到数据库 ——
        boolean saved = saveAiResponse(sessionId, finalAnswer);
        if (!saved) {
            log.warn("AI回答保存失败: sessionId={}", sessionId);
        }

        // 统一返回Map
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("code", 200);
        resultMap.put("message", "成功");
        resultMap.put("data", finalAnswer);
        return resultMap;
    }

    /**
     * 构造工具选择的 Prompt
     */
    private String buildToolSelectionPrompt(McpSchema.ListToolsResult tools, String question ,McpSchema.CallToolResult list) {
        return tools
                + "\n这是我现在已有的工具，"+list+"这是我已有的数据库，." +
                "请你直接输出 JSON 文本（不要包含 ```json，不要mackdown形式，求求你了，如有sql语句，只能使用select）。\n"
                + "以下是用户的问题：\n" + question + "\n"
                + "请选择最合适的工具，并给出参数，格式如下：\n"
                + "{\n"
                + "  \"Tool_name\": \"\",\n"
                + "  \"parameter\": { \"key\": \"value\" }\n"
                + "}";
    }

    /**
     * 解析 AI 返回的 "工具调用 JSON"，执行并验证，若 #invalid# 则循环重试
     */
    private String processAiLoop(McpSyncClient fs, String aiRaw, String sessionId, String userQuestion) throws JsonProcessingException {
        String aiResp = aiRaw;
        List<String> aiHistory = new ArrayList<>();
        List<String> toolHistory = new ArrayList<>();

        while (true) {
            String jsonOnly = cleanJson(aiResp);
            System.out.println(jsonOnly);

            JsonNode root = mapper.readTree(jsonOnly);

            String toolName = root.get("Tool_name").asText();
            ObjectNode paramObj = (ObjectNode) root.get("parameter");

            Map<String, Object> params = new HashMap<>();
            paramObj.fields().forEachRemaining(e -> params.put(e.getKey(), e.getValue().asText()));
            McpSchema.CallToolResult toolResult;
            try {
                toolResult = fs.callTool(new McpSchema.CallToolRequest(toolName, params));
                toolHistory.add("工具调用：" + toolName + " 参数：" + params + "\n反馈：" + toolResult.toString());
            } catch (io.modelcontextprotocol.spec.McpError ex) {
                String errorPrompt = buildErrorPrompt(toolName, params, ex.getMessage(), sessionId, aiHistory, toolHistory, userQuestion);
                aiResp = chatModel.call(errorPrompt);
                aiHistory.add("AI回答：" + aiResp);
                continue;
            }

            String validationPrompt = buildValidationPrompt(toolResult, sessionId, aiHistory, toolHistory, userQuestion);
            aiResp = chatModel.call(validationPrompt);
            aiHistory.add("AI回答：" + aiResp);

            if (aiResp.startsWith("#valid#")) {
                return aiResp.substring("#valid#".length()).trim();
            }
            if (aiResp.startsWith("#invalid#")) {
                aiResp = aiResp.substring("#invalid#".length()).trim();
                System.out.println(aiResp);
                continue;
            }
            throw new IllegalStateException("AI 验证返回未包含 #valid# 或 #invalid# 前缀");
        }
    }

    /**
     * 构造给 AI 的验证 Prompt，包含用户问题、历史AI回答和工具反馈
     */
    private String buildValidationPrompt(McpSchema.CallToolResult result, String sessionId, List<String> aiHistory, List<String> toolHistory, String userQuestion) {
        McpSyncClient fs = mcpSyncClients.get(0);
        McpSchema.ListToolsResult tools = fs.listTools();
        StringBuilder historyBuilder = new StringBuilder();
        historyBuilder.append("用户问题：").append(userQuestion).append("\n");
        historyBuilder.append(getChatHistoryBySessionId(sessionId));
        for (String ai : aiHistory) {
            historyBuilder.append(ai).append("\n");
        }
        for (String tool : toolHistory) {
            historyBuilder.append(tool).append("\n");
        }
        return historyBuilder
                .append("以下是工具执行结果，请判断是否解决了用户的问题：\n")
                .append(result.toString()).append("\n\n")
                .append("1. 若已解决，请在开头添加 #valid#，并优化回答后输出答案；\n")
                .append("2. 若未解决，请在开头添加 #invalid#，并重新给出调用工具，根据之前的信息，直接生成正确的sql语句，让mcp执行")
                .append(tools)
                .append("  并输出JSON，不要包含 ```json，不要mackdown形式，求求你了（修改时还是要将完整的json输出出来，如有sql语句，只能使用select）：\n")
                .append("{\n")
                .append("  \"Tool_name\": \"\",\n")
                .append("  \"parameter\": { \"sql\": \"value\" }\n")
                .append("}")
                .toString();
    }

    /**
     * 构造错误处理 Prompt，包含用户问题、历史AI回答和工具反馈
     */
    private String buildErrorPrompt(String toolName, Map<String, Object> params, String errorMsg, String sessionId, List<String> aiHistory, List<String> toolHistory, String userQuestion) {
        McpSyncClient fs = mcpSyncClients.get(0);
        McpSchema.ListToolsResult tools = fs.listTools();
        StringBuilder historyBuilder = new StringBuilder();
        historyBuilder.append("用户问题：").append(userQuestion).append("\n");
        historyBuilder.append(getChatHistoryBySessionId(sessionId));
        for (String ai : aiHistory) {
            historyBuilder.append(ai).append("\n");
        }
        for (String tool : toolHistory) {
            historyBuilder.append(tool).append("\n");
        }
        return historyBuilder
                .append("在尝试调用工具 `").append(toolName).append("` 时，MCP 返回了如下错误：\n")
                .append(errorMsg).append("\n\n")
                .append("请根据这个错误信息，修正或替换工具").append(tools)
                .append("并直接输出新的 JSON，不要包含 ```json，不要mackdown形式，求求你了（不要包含任何多余文字或代码块标记，修改时还是要将完整的json输出出来，如有sql语句，只能使用select），格式：\n")
                .append("{\n")
                .append("  \"Tool_name\": \"\",\n")
                .append("  \"parameter\": { \"sql\": \"value\" }\n")
                .append("}")
                .toString();
    }
    
    /**
     * 去掉 ```json ``` 包裹、#valid#/invalid# 前缀，及多余文字，只保留最外层 {…}
     */
    private String cleanJson(String raw) {
        String noMd = raw.replaceAll("(?s)```json.*?```", "")
                .replaceAll("(?m)^#valid#|^#invalid#", "")
                .trim();
        Matcher m = Pattern.compile("\\{[\\s\\S]*}\\}").matcher(noMd);
        return m.find() ? m.group() : noMd;
    }

    /**
     * 检查字符串是否为空或null
     * @param s 待检查的字符串
     * @return 如果字符串为null或空，返回true；否则返回false
     */
    private static boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }
}