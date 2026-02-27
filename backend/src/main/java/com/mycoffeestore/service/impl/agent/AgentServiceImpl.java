package com.mycoffeestore.service.impl.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mycoffeestore.config.ModelScopeProperties;
import com.mycoffeestore.dto.agent.AgentChatRequestDTO;
import com.mycoffeestore.service.agent.AgentService;
import com.mycoffeestore.util.AgentToolExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.*;

/**
 * AI Agent 服务实现
 * 对接 Modelscope Kimi-K2.5 模型，支持 SSE 流式响应和工具调用
 *
 * @author zhoulu
 * @since 2026-02-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentServiceImpl implements AgentService {

    private final WebClient modelScopeWebClient;
    private final ModelScopeProperties modelScopeProperties;
    private final AgentToolExecutor toolExecutor;
    private final ObjectMapper objectMapper;

    /**
     * 最大工具调用轮次（防止无限循环）
     */
    private static final int MAX_TOOL_ROUNDS = 5;

    /**
     * System Prompt 模板
     */
    private static final Map<String, String> SYSTEM_PROMPTS = Map.of(
        "coffee_advisor",
        """
        你是「咖咖」，Haight Ashbury Coffee 的专业咖啡顾问。你了解店内所有咖啡产品，擅长根据顾客的口味偏好、饮用场景推荐咖啡。
        规则：
        1. 用友善亲切的中文回答
        2. 推荐时先了解用户偏好，然后使用工具查询咖啡
        3. 推荐时说明推荐理由，包括风味特点
        4. 每次最多推荐3款
        5. 回答简洁，不要过于冗长
        """,
        "customer_service",
        """
        你是 Haight Ashbury Coffee 的客服助手。帮助顾客解答门店信息、营业时间、配送政策、退款等常见问题。
        门店信息：
        - 地址：旧金山 Haight Ashbury 区
        - 营业时间：周一到周日 7:00-21:00
        - 配送范围：旧金山市区，30分钟内送达
        - 退款政策：下单后15分钟内可取消，已制作不可退
        规则：
        1. 用友善亲切的中文回答
        2. 如果用户询问订单问题，可以使用工具查询
        3. 回答简洁准确
        """,
        "order_assistant",
        """
        你是 Haight Ashbury Coffee 的订单助手，帮助顾客完成点单全流程。
        能力：查询咖啡菜单、加入购物车、创建订单、查看订单状态。
        规则：
        1. 用友善亲切的中文回答
        2. 操作前确认用户意图
        3. 涉及购物车和订单操作需要用户登录
        4. 下单时确认订单类型（dine_in 堂食 / takeaway 外带 / delivery 外卖）
        5. 回答简洁，引导用户完成操作
        """
    );

    @Override
    public void chatStream(AgentChatRequestDTO request, Long userId, SseEmitter emitter) {
        try {
            // 构建消息列表
            List<Map<String, Object>> messages = buildMessages(request);

            // 获取工具定义
            List<Map<String, Object>> tools = toolExecutor.getToolDefinitions(request.getAgentType());

            // 执行流式对话（支持工具调用循环）
            executeStreamChat(messages, tools, userId, emitter, 0);
        } catch (Exception e) {
            log.error("Agent 聊天异常: {}", e.getMessage(), e);
            sendSseEvent(emitter, "error", Map.of("type", "error", "message", "AI 服务暂时不可用，请稍后重试"));
            completeSse(emitter);
        }
    }

    /**
     * 构建消息列表（system prompt + 用户对话历史）
     */
    private List<Map<String, Object>> buildMessages(AgentChatRequestDTO request) {
        List<Map<String, Object>> messages = new ArrayList<>();

        // 添加 system prompt
        String systemPrompt = SYSTEM_PROMPTS.getOrDefault(request.getAgentType(), SYSTEM_PROMPTS.get("coffee_advisor"));
        messages.add(Map.of("role", "system", "content", systemPrompt));

        // 添加用户对话历史
        for (AgentChatRequestDTO.Message msg : request.getMessages()) {
            messages.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
        }

        return messages;
    }

    /**
     * 执行流式对话，支持工具调用循环
     */
    private void executeStreamChat(List<Map<String, Object>> messages,
                                    List<Map<String, Object>> tools,
                                    Long userId,
                                    SseEmitter emitter,
                                    int round) {
        if (round >= MAX_TOOL_ROUNDS) {
            log.warn("工具调用轮次超过上限: {}", MAX_TOOL_ROUNDS);
            sendSseEvent(emitter, "message", Map.of("type", "text", "content", "抱歉，处理过程过于复杂，请简化你的请求。"));
            sendSseEvent(emitter, "done", Map.of("type", "done"));
            completeSse(emitter);
            return;
        }

        try {
            // 构建请求体
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", modelScopeProperties.getModel());
            requestBody.set("messages", objectMapper.valueToTree(messages));
            requestBody.put("stream", true);
            requestBody.put("temperature", 0.6);
            requestBody.put("max_tokens", 4096);

            // 添加工具定义
            if (!tools.isEmpty()) {
                requestBody.set("tools", objectMapper.valueToTree(tools));
            }

            // 关闭思考模式（Kimi-K2.5 Instant Mode）
            ObjectNode extraBody = objectMapper.createObjectNode();
            ObjectNode chatTemplateKwargs = objectMapper.createObjectNode();
            chatTemplateKwargs.put("thinking", false);
            extraBody.set("chat_template_kwargs", chatTemplateKwargs);
            requestBody.set("extra_body", extraBody);

            log.info("调用 Modelscope API，轮次: {}，消息数: {}", round, messages.size());

            // 流式请求
            Flux<String> responseFlux = modelScopeWebClient.post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody.toString())
                    .retrieve()
                    .bodyToFlux(String.class);

            // 收集流式响应
            StringBuilder contentBuilder = new StringBuilder();
            List<Map<String, Object>> toolCalls = new ArrayList<>();
            // 用于累积 tool_call 的 arguments
            Map<Integer, StringBuilder> toolCallArgsBuilders = new HashMap<>();
            Map<Integer, String> toolCallIds = new HashMap<>();
            Map<Integer, String> toolCallNames = new HashMap<>();

            responseFlux
                .doOnNext(line -> {
                    // 处理 SSE 数据行
                    processStreamLine(line, contentBuilder, toolCalls,
                            toolCallArgsBuilders, toolCallIds, toolCallNames,
                            emitter);
                })
                .doOnComplete(() -> {
                    // 组装完整的 tool_calls
                    finalizeToolCalls(toolCalls, toolCallArgsBuilders, toolCallIds, toolCallNames);

                    if (!toolCalls.isEmpty()) {
                        // 有工具调用，执行工具并递归
                        handleToolCalls(messages, tools, toolCalls, contentBuilder.toString(), userId, emitter, round);
                    } else {
                        // 没有工具调用，完成
                        sendSseEvent(emitter, "done", Map.of("type", "done"));
                        completeSse(emitter);
                    }
                })
                .doOnError(error -> {
                    log.error("Modelscope API 流式调用失败: {}", error.getMessage(), error);
                    sendSseEvent(emitter, "message", Map.of("type", "error", "message", "AI 服务暂时不可用"));
                    completeSse(emitter);
                })
                .subscribe();

        } catch (Exception e) {
            log.error("构建 Modelscope 请求失败: {}", e.getMessage(), e);
            sendSseEvent(emitter, "message", Map.of("type", "error", "message", "请求构建失败"));
            completeSse(emitter);
        }
    }

    /**
     * 处理流式响应的每一行数据
     */
    private void processStreamLine(String line,
                                    StringBuilder contentBuilder,
                                    List<Map<String, Object>> toolCalls,
                                    Map<Integer, StringBuilder> toolCallArgsBuilders,
                                    Map<Integer, String> toolCallIds,
                                    Map<Integer, String> toolCallNames,
                                    SseEmitter emitter) {
        // WebClient bodyToFlux(String.class) 会自动处理 SSE data: 前缀
        String data = line.trim();
        if (data.isEmpty() || data.equals("[DONE]")) {
            return;
        }

        // 去掉可能的 "data: " 前缀
        if (data.startsWith("data:")) {
            data = data.substring(5).trim();
        }
        if (data.isEmpty() || data.equals("[DONE]")) {
            return;
        }

        try {
            JsonNode json = objectMapper.readTree(data);
            JsonNode choices = json.get("choices");
            if (choices == null || !choices.isArray() || choices.isEmpty()) {
                return;
            }

            JsonNode choice = choices.get(0);
            JsonNode delta = choice.get("delta");
            if (delta == null) {
                return;
            }

            // 处理文本内容
            if (delta.has("content") && !delta.get("content").isNull()) {
                String content = delta.get("content").asText();
                if (!content.isEmpty()) {
                    contentBuilder.append(content);
                    sendSseEvent(emitter, "message", Map.of("type", "text", "content", content));
                }
            }

            // 处理工具调用
            if (delta.has("tool_calls") && delta.get("tool_calls").isArray()) {
                for (JsonNode tc : delta.get("tool_calls")) {
                    int index = tc.has("index") ? tc.get("index").asInt() : 0;

                    // 收集 tool call id
                    if (tc.has("id") && !tc.get("id").isNull()) {
                        toolCallIds.put(index, tc.get("id").asText());
                    }

                    // 收集 function name
                    if (tc.has("function")) {
                        JsonNode func = tc.get("function");
                        if (func.has("name") && !func.get("name").isNull()) {
                            toolCallNames.put(index, func.get("name").asText());
                        }
                        // 累积 arguments（可能分多个 chunk 传输）
                        if (func.has("arguments") && !func.get("arguments").isNull()) {
                            toolCallArgsBuilders
                                    .computeIfAbsent(index, k -> new StringBuilder())
                                    .append(func.get("arguments").asText());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("解析流式响应行失败: {} - {}", data, e.getMessage());
        }
    }

    /**
     * 组装完整的 tool_calls 列表
     */
    private void finalizeToolCalls(List<Map<String, Object>> toolCalls,
                                    Map<Integer, StringBuilder> argsBuilders,
                                    Map<Integer, String> ids,
                                    Map<Integer, String> names) {
        for (Integer index : names.keySet()) {
            String id = ids.getOrDefault(index, "call_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8));
            String name = names.get(index);
            String args = argsBuilders.containsKey(index) ? argsBuilders.get(index).toString() : "{}";

            Map<String, Object> toolCall = new HashMap<>();
            toolCall.put("id", id);
            toolCall.put("type", "function");
            toolCall.put("function", Map.of("name", name, "arguments", args));
            toolCalls.add(toolCall);
        }
    }

    /**
     * 处理工具调用：执行工具 → 拼入消息 → 递归调用模型
     */
    private void handleToolCalls(List<Map<String, Object>> messages,
                                  List<Map<String, Object>> tools,
                                  List<Map<String, Object>> toolCalls,
                                  String assistantContent,
                                  Long userId,
                                  SseEmitter emitter,
                                  int round) {
        // 添加 assistant 消息（包含 tool_calls）
        Map<String, Object> assistantMsg = new HashMap<>();
        assistantMsg.put("role", "assistant");
        if (assistantContent != null && !assistantContent.isEmpty()) {
            assistantMsg.put("content", assistantContent);
        } else {
            assistantMsg.put("content", null);
        }
        assistantMsg.put("tool_calls", toolCalls);
        messages.add(assistantMsg);

        // 逐个执行工具
        for (Map<String, Object> toolCall : toolCalls) {
            @SuppressWarnings("unchecked")
            Map<String, String> function = (Map<String, String>) toolCall.get("function");
            String toolName = function.get("name");
            String toolArgs = function.get("arguments");
            String toolCallId = (String) toolCall.get("id");

            // 通知前端正在执行工具
            sendSseEvent(emitter, "message", Map.of(
                    "type", "tool_call",
                    "toolName", toolName,
                    "toolArgs", toolArgs
            ));

            // 执行工具
            log.info("执行工具: {} 参数: {}", toolName, toolArgs);
            String result = toolExecutor.executeTool(toolName, toolArgs, userId);

            // 通知前端工具执行结果
            sendSseEvent(emitter, "message", Map.of(
                    "type", "tool_result",
                    "toolName", toolName,
                    "result", result
            ));

            // 添加 tool 结果消息
            Map<String, Object> toolResultMsg = new HashMap<>();
            toolResultMsg.put("role", "tool");
            toolResultMsg.put("tool_call_id", toolCallId);
            toolResultMsg.put("content", result);
            messages.add(toolResultMsg);
        }

        // 递归调用模型，获取基于工具结果的回复
        executeStreamChat(messages, tools, userId, emitter, round + 1);
    }

    /**
     * 发送 SSE 事件
     */
    private void sendSseEvent(SseEmitter emitter, String eventName, Map<String, Object> data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            emitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(json));
        } catch (IOException e) {
            log.warn("SSE 发送失败: {}", e.getMessage());
        }
    }

    /**
     * 完成 SSE 连接
     */
    private void completeSse(SseEmitter emitter) {
        try {
            emitter.complete();
        } catch (Exception e) {
            log.debug("SSE 完成异常: {}", e.getMessage());
        }
    }
}
