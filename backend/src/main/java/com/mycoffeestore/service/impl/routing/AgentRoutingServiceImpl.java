package com.mycoffeestore.service.impl.routing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycoffeestore.agent.AgentRegistry;
import com.mycoffeestore.agent.routing.AgentRoutingConfig;
import com.mycoffeestore.config.AgentConfig;
import com.mycoffeestore.dto.agent.AgentChatRequestDTO;
import com.mycoffeestore.service.agent.AgentService;
import com.mycoffeestore.service.memory.ConversationMemoryService;
import com.mycoffeestore.service.routing.IntentRecognitionService;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 智能路由服务实现
 * 实现智能路由逻辑，支持单 Agent、顺序执行、并行执行
 * 集成对话记忆功能
 *
 * 功能：
 * 1. 智能意图识别和路由
 * 2. 单 Agent 执行
 * 3. 顺序执行多个 Agent（传递上下文）
 * 4. 并行执行多个 Agent（聚合结果）
 * 5. 对话记忆集成
 * 6. 路由失败处理
 *
 * @author Backend Developer
 * @since 2026-03-07
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Schema(description = "智能路由服务实现")
public class AgentRoutingServiceImpl {

    private final IntentRecognitionService intentRecognitionService;
    private final AgentRegistry agentRegistry;
    private final ConversationMemoryService memoryService;
    private final AgentService agentService;
    private final ObjectMapper objectMapper;

    @Qualifier("defaultRouteRules")
    private final Map<AgentRoutingConfig.IntentType, AgentRoutingConfig.RouteRule> routeRules;

    /**
     * 执行上下文
     * 用于在 Agent 之间传递上下文信息
     */
    @lombok.Builder
    @lombok.Data
    @Schema(description = "执行上下文")
    public static class ExecutionContext {
        /**
         * 用户 ID
         */
        @Schema(description = "用户 ID")
        private Long userId;

        /**
         * 会话 ID
         */
        @Schema(description = "会话 ID")
        private String sessionId;

        /**
         * 原始用户输入
         */
        @Schema(description = "原始用户输入")
        private String userInput;

        /**
         * 意图识别结果
         */
        @Schema(description = "意图识别结果")
        private IntentRecognitionService.IntentRecognitionResult intentResult;

        /**
         * 路由规则
         */
        @Schema(description = "路由规则")
        private AgentRoutingConfig.RouteRule routeRule;

        /**
         * Agent 执行历史
         */
        @Schema(description = "Agent 执行历史")
        private List<AgentExecutionRecord> executionHistory;

        /**
         * 上下文数据（用于在 Agent 之间传递）
         */
        @Schema(description = "上下文数据")
        private Map<String, Object> contextData;

        /**
         * SSE 发射器
         */
        @Schema(description = "SSE 发射器")
        private SseEmitter emitter;
    }

    /**
     * Agent 执行记录
     */
    @lombok.Builder
    @lombok.Data
    @Schema(description = "Agent 执行记录")
    public static class AgentExecutionRecord {
        /**
         * Agent 名称
         */
        @Schema(description = "Agent 名称")
        private String agentName;

        /**
         * 执行顺序
         */
        @Schema(description = "执行顺序")
        private Integer sequence;

        /**
         * 执行开始时间
         */
        @Schema(description = "执行开始时间")
        private Long startTime;

        /**
         * 执行结束时间
         */
        @Schema(description = "执行结束时间")
        private Long endTime;

        /**
         * 执行耗时（毫秒）
         */
        @Schema(description = "执行耗时")
        private Long duration;

        /**
         * 执行状态
         */
        @Schema(description = "执行状态")
        private ExecutionStatus status;

        /**
         * Agent 输出
         */
        @Schema(description = "Agent 输出")
        private String output;

        /**
         * 错误信息（如果执行失败）
         */
        @Schema(description = "错误信息")
        private String errorMessage;

        /**
         * 工具调用记录
         */
        @Schema(description = "工具调用记录")
        private List<ToolCallRecord> toolCalls;
    }

    /**
     * 工具调用记录
     */
    @lombok.Builder
    @lombok.Data
    @Schema(description = "工具调用记录")
    public static class ToolCallRecord {
        /**
         * 工具名称
         */
        @Schema(description = "工具名称")
        private String toolName;

        /**
         * 工具参数
         */
        @Schema(description = "工具参数")
        private String arguments;

        /**
         * 工具执行结果
         */
        @Schema(description = "工具执行结果")
        private String result;

        /**
         * 执行耗时（毫秒）
         */
        @Schema(description = "执行耗时")
        private Long duration;
    }

    /**
     * 执行状态枚举
     */
    @Schema(description = "执行状态枚举")
    public enum ExecutionStatus {
        /**
         * 待执行
         */
        PENDING,

        /**
         * 执行中
         */
        RUNNING,

        /**
         * 执行成功
         */
        SUCCESS,

        /**
         * 执行失败
         */
        FAILED,

        /**
         * 跳过
         */
        SKIPPED
    }

    /**
     * 路由执行结果
     */
    @lombok.Builder
    @lombok.Data
    @Schema(description = "路由执行结果")
    public static class RoutingResult {
        /**
         * 是否成功
         */
        @Schema(description = "是否成功")
        private Boolean success;

        /**
         * 识别的意图
         */
        @Schema(description = "识别的意图")
        private AgentRoutingConfig.IntentType intentType;

        /**
         * 执行策略
         */
        @Schema(description = "执行策略")
        private AgentRoutingConfig.ExecutionStrategy strategy;

        /**
         * 执行记录列表
         */
        @Schema(description = "执行记录列表")
        private List<AgentExecutionRecord> executionRecords;

        /**
         * 最终输出
         */
        @Schema(description = "最终输出")
        private String finalOutput;

        /**
         * 错误信息（如果失败）
         */
        @Schema(description = "错误信息")
        private String errorMessage;

        /**
         * 总执行耗时（毫秒）
         */
        @Schema(description = "总执行耗时")
        private Long totalDuration;
    }

    /**
     * 处理路由聊天请求
     * 这是主要的入口方法，协调整个路由流程
     *
     * @param request 聊天请求
     * @param userId  用户 ID
     * @param emitter SSE 发射器
     */
    @Schema(description = "处理路由聊天请求")
    public void processRoutingChat(AgentChatRequestDTO request, Long userId, SseEmitter emitter) {
        CompletableFuture.runAsync(() -> {
            try {
                long startTime = System.currentTimeMillis();

                // 获取用户消息
                String userInput = getLastUserMessage(request);
                log.info("开始处理路由请求: userId={}, userInput={}", userId, userInput);

                // 步骤 1: 意图识别
                IntentRecognitionService.IntentRecognitionResult intentResult =
                        intentRecognitionService.recognizeIntent(userInput);

                sendSseEvent(emitter, "intent_recognized", Map.of(
                        "intent", intentResult.getIntentType().getCode(),
                        "confidence", intentResult.getConfidence(),
                        "method", intentResult.getRecognitionMethod()
                ));

                // 步骤 2: 获取路由规则
                AgentRoutingConfig.RouteRule routeRule = routeRules.get(intentResult.getIntentType());
                if (routeRule == null) {
                    log.warn("未找到路由规则: intent={}", intentResult.getIntentType());
                    routeRule = createFallbackRule();
                }

                // 步骤 3: 构建执行上下文
                ExecutionContext context = ExecutionContext.builder()
                        .userId(userId)
                        .sessionId(memoryService.generateSessionId())
                        .userInput(userInput)
                        .intentResult(intentResult)
                        .routeRule(routeRule)
                        .executionHistory(new ArrayList<>())
                        .contextData(new ConcurrentHashMap<>())
                        .emitter(emitter)
                        .build();

                // 步骤 4: 根据执行策略执行
                RoutingResult result;
                switch (routeRule.getStrategy()) {
                    case SEQUENTIAL:
                        result = executeSequential(context);
                        break;
                    case PARALLEL:
                        result = executeParallel(context);
                        break;
                    case SINGLE:
                    default:
                        result = executeSingle(context);
                        break;
                }

                result.setTotalDuration(System.currentTimeMillis() - startTime);

                // 步骤 5: 发送最终结果
                handleRoutingResult(result, context);

            } catch (Exception e) {
                log.error("路由处理失败: {}", e.getMessage(), e);
                sendSseEvent(emitter, "error", Map.of(
                        "type", "error",
                        "message", "路由处理失败: " + e.getMessage()
                ));
                completeSse(emitter);
            }
        });
    }

    /**
     * 单 Agent 执行
     *
     * @param context 执行上下文
     * @return 执行结果
     */
    @Schema(description = "单 Agent 执行")
    private RoutingResult executeSingle(ExecutionContext context) {
        log.info("执行单 Agent 策略: intent={}",
                context.getIntentResult().getIntentType());

        List<String> agentChain = context.getRouteRule().getAgentChain();
        if (agentChain.isEmpty()) {
            return RoutingResult.builder()
                    .success(false)
                    .errorMessage("Agent 执行链为空")
                    .build();
        }

        String agentName = agentChain.get(0);
        return executeAgent(agentName, context, 0);
    }

    /**
     * 顺序执行多个 Agent
     * 每个 Agent 的输出会传递给下一个 Agent
     *
     * @param context 执行上下文
     * @return 执行结果
     */
    @Schema(description = "顺序执行多个 Agent")
    private RoutingResult executeSequential(ExecutionContext context) {
        log.info("执行顺序策略: intent={}, agentChain={}",
                context.getIntentResult().getIntentType(),
                context.getRouteRule().getAgentChain());

        List<String> agentChain = context.getRouteRule().getAgentChain();
        List<AgentExecutionRecord> records = new ArrayList<>();
        StringBuilder aggregatedOutput = new StringBuilder();

        for (int i = 0; i < agentChain.size(); i++) {
            String agentName = agentChain.get(i);

            // 通知前端正在执行的 Agent
            sendSseEvent(context.getEmitter(), "agent_start", Map.of(
                    "agentName", agentName,
                    "sequence", i + 1,
                    "total", agentChain.size()
            ));

            // 执行 Agent
            RoutingResult result = executeAgent(agentName, context, i);
            records.addAll(result.getExecutionRecords());

            if (!result.getSuccess()) {
                // 执行失败，停止后续执行
                log.warn("Agent 执行失败，停止顺序执行: agent={}", agentName);
                return RoutingResult.builder()
                        .success(false)
                        .intentType(context.getIntentResult().getIntentType())
                        .strategy(AgentRoutingConfig.ExecutionStrategy.SEQUENTIAL)
                        .executionRecords(records)
                        .errorMessage(result.getErrorMessage())
                        .build();
            }

            // 将输出添加到上下文（传递给下一个 Agent）
            if (result.getFinalOutput() != null && !result.getFinalOutput().isEmpty()) {
                aggregatedOutput.append(result.getFinalOutput()).append("\n\n");

                // 根据配置传递上下文
                if (shouldPassContext(context, i)) {
                    context.getContextData().put("previousAgentOutput", result.getFinalOutput());
                    context.getContextData().put("previousAgentName", agentName);
                }
            }

            // 通知前端 Agent 执行完成
            sendSseEvent(context.getEmitter(), "agent_complete", Map.of(
                    "agentName", agentName,
                    "sequence", i + 1,
                    "total", agentChain.size()
            ));
        }

        return RoutingResult.builder()
                .success(true)
                .intentType(context.getIntentResult().getIntentType())
                .strategy(AgentRoutingConfig.ExecutionStrategy.SEQUENTIAL)
                .executionRecords(records)
                .finalOutput(aggregatedOutput.toString().trim())
                .build();
    }

    /**
     * 并行执行多个 Agent
     * 所有 Agent 同时执行，结果聚合
     *
     * @param context 执行上下文
     * @return 执行结果
     */
    @Schema(description = "并行执行多个 Agent")
    private RoutingResult executeParallel(ExecutionContext context) {
        log.info("执行并行策略: intent={}, agentChain={}",
                context.getIntentResult().getIntentType(),
                context.getRouteRule().getAgentChain());

        List<String> agentChain = context.getRouteRule().getAgentChain();

        // 创建并行执行任务
        List<CompletableFuture<RoutingResult>> futures = agentChain.stream()
                .map(agentName -> CompletableFuture.supplyAsync(() ->
                        executeAgent(agentName, context, agentChain.indexOf(agentName))))
                .toList();

        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // 收集结果
        List<AgentExecutionRecord> allRecords = new ArrayList<>();
        Map<String, String> outputs = new LinkedHashMap<>();

        for (CompletableFuture<RoutingResult> future : futures) {
            try {
                RoutingResult result = future.get();
                allRecords.addAll(result.getExecutionRecords());

                String agentName = result.getExecutionRecords().isEmpty() ?
                        "unknown" : result.getExecutionRecords().get(0).getAgentName();
                outputs.put(agentName, result.getFinalOutput());

            } catch (Exception e) {
                log.error("并行执行任务失败: {}", e.getMessage(), e);
            }
        }

        // 聚合输出
        String finalOutput;
        if (context.getRouteRule().getAggregateResults()) {
            finalOutput = aggregateOutputs(outputs);
        } else {
            // 只返回第一个成功的结果
            finalOutput = outputs.values().stream()
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse("");
        }

        return RoutingResult.builder()
                .success(true)
                .intentType(context.getIntentResult().getIntentType())
                .strategy(AgentRoutingConfig.ExecutionStrategy.PARALLEL)
                .executionRecords(allRecords)
                .finalOutput(finalOutput)
                .build();
    }

    /**
     * 执行单个 Agent
     *
     * @param agentName Agent 名称
     * @param context   执行上下文
     * @param sequence  执行顺序
     * @return 执行结果
     */
    @Schema(description = "执行单个 Agent")
    private RoutingResult executeAgent(String agentName, ExecutionContext context, int sequence) {
        long startTime = System.currentTimeMillis();

        AgentExecutionRecord record = AgentExecutionRecord.builder()
                .agentName(agentName)
                .sequence(sequence)
                .startTime(startTime)
                .status(ExecutionStatus.RUNNING)
                .build();

        try {
            // 获取 Agent 配置
            AgentConfig.AgentConfigInfo agentConfig = agentRegistry.getAgentConfig(agentName);
            if (agentConfig == null) {
                throw new IllegalArgumentException("Agent 不存在: " + agentName);
            }

            // 构建聊天请求（包含上下文）
            AgentChatRequestDTO agentRequest = buildAgentRequest(agentName, context, agentConfig);

            // 创建自定义 SSE 发射器来捕获输出
            SseEmitterCapture capture = new SseEmitterCapture();

            // 执行 Agent
            agentService.chatStream(agentRequest, context.getUserId(), capture.getSseEmitter());

            // 等待执行完成
            capture.awaitCompletion();

            // 构建执行记录
            record.setOutput(capture.getOutput());
            record.setEndTime(System.currentTimeMillis());
            record.setDuration(record.getEndTime() - record.getStartTime());
            record.setStatus(ExecutionStatus.SUCCESS);

            return RoutingResult.builder()
                    .success(true)
                    .executionRecords(Collections.singletonList(record))
                    .finalOutput(capture.getOutput())
                    .build();

        } catch (Exception e) {
            log.error("Agent 执行失败: agent={}, error={}", agentName, e.getMessage(), e);
            record.setEndTime(System.currentTimeMillis());
            record.setDuration(record.getEndTime() - record.getStartTime());
            record.setStatus(ExecutionStatus.FAILED);
            record.setErrorMessage(e.getMessage());

            return RoutingResult.builder()
                    .success(false)
                    .executionRecords(Collections.singletonList(record))
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    /**
     * 构建子 Agent 的聊天请求
     *
     * @param agentName   Agent 名称
     * @param context     执行上下文
     * @param agentConfig Agent 配置
     * @return 聊天请求
     */
    @Schema(description = "构建子 Agent 聊天请求")
    private AgentChatRequestDTO buildAgentRequest(String agentName,
                                                   ExecutionContext context,
                                                   AgentConfig.AgentConfigInfo agentConfig) {
        List<AgentChatRequestDTO.Message> messages = new ArrayList<>();

        // 添加系统提示词
        if (agentConfig.getSystemPrompt() != null) {
            messages.add(AgentChatRequestDTO.Message.builder()
                    .role("system")
                    .content(agentConfig.getSystemPrompt())
                    .build());
        }

        // 如果有前置 Agent 的输出，添加到上下文
        if (context.getContextData().containsKey("previousAgentOutput")) {
            String previousOutput = (String) context.getContextData().get("previousAgentOutput");
            String previousAgent = (String) context.getContextData().get("previousAgentName");

            messages.add(AgentChatRequestDTO.Message.builder()
                    .role("system")
                    .content(String.format(
                            "[上下文信息] 前一个 Agent (%s) 的处理结果：\n%s\n\n请基于以上信息继续处理。",
                            previousAgent, previousOutput
                    ))
                    .build());
        }

        // 添加用户消息
        messages.add(AgentChatRequestDTO.Message.builder()
                .role("user")
                .content(context.getUserInput())
                .build());

        return AgentChatRequestDTO.builder()
                .agentType(agentName)
                .messages(messages)
                .build();
    }

    /**
     * 判断是否应该传递上下文到下一个 Agent
     *
     * @param context  执行上下文
     * @param sequence 当前执行顺序
     * @return 是否传递上下文
     */
    @Schema(description = "判断是否传递上下文")
    private boolean shouldPassContext(ExecutionContext context, int sequence) {
        AgentRoutingConfig.ContextPassingConfig config = context.getRouteRule().getContextPassing();
        return config != null &&
                (config.getPassMessageHistory() || config.getPassToolResults());
    }

    /**
     * 聚合多个 Agent 的输出
     *
     * @param outputs Agent 输出映射
     * @return 聚合后的输出
     */
    @Schema(description = "聚合多个 Agent 的输出")
    private String aggregateOutputs(Map<String, String> outputs) {
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, String> entry : outputs.entrySet()) {
            sb.append("## ").append(entry.getKey()).append(" 的处理结果\n\n");
            sb.append(entry.getValue()).append("\n\n");
        }

        return sb.toString().trim();
    }

    /**
     * 处理路由执行结果
     *
     * @param result  执行结果
     * @param context 执行上下文
     */
    @Schema(description = "处理路由执行结果")
    private void handleRoutingResult(RoutingResult result, ExecutionContext context) {
        if (result.getSuccess()) {
            // 保存到对话记忆
            saveToMemory(context, result);

            // 发送完成事件
            sendSseEvent(context.getEmitter(), "routing_complete", Map.of(
                    "intent", result.getIntentType().getCode(),
                    "strategy", result.getStrategy().name(),
                    "duration", result.getTotalDuration(),
                    "agentCount", result.getExecutionRecords().size()
            ));

            completeSse(context.getEmitter());
        } else {
            // 发送错误事件
            sendSseEvent(context.getEmitter(), "routing_error", Map.of(
                    "message", result.getErrorMessage() != null ?
                            result.getErrorMessage() : "路由执行失败"
            ));
            completeSse(context.getEmitter());
        }
    }

    /**
     * 保存到对话记忆
     *
     * @param context 执行上下文
     * @param result  执行结果
     */
    @Schema(description = "保存到对话记忆")
    private void saveToMemory(ExecutionContext context, RoutingResult result) {
        if (!memoryService.isRedisAvailable()) {
            log.debug("Redis 不可用，跳过保存对话记忆");
            return;
        }

        try {
            // TODO: 实现对话记忆保存逻辑
            log.debug("保存对话记忆: sessionId={}", context.getSessionId());
        } catch (Exception e) {
            log.warn("保存对话记忆失败: {}", e.getMessage());
        }
    }

    /**
     * 创建回退路由规则
     *
     * @return 回退规则
     */
    @Schema(description = "创建回退路由规则")
    private AgentRoutingConfig.RouteRule createFallbackRule() {
        return AgentRoutingConfig.RouteRule.builder()
                .intentType(AgentRoutingConfig.IntentType.GENERAL)
                .strategy(AgentRoutingConfig.ExecutionStrategy.SINGLE)
                .agentChain(Collections.singletonList("general_chat"))
                .priority(999)
                .aggregateResults(false)
                .contextPassing(AgentRoutingConfig.ContextPassingConfig.builder()
                        .passMessageHistory(true)
                        .passToolResults(false)
                        .passReasoning(false)
                        .maxHistoryRounds(5)
                        .build())
                .build();
    }

    /**
     * 获取最后一条用户消息
     *
     * @param request 聊天请求
     * @return 用户消息内容
     */
    @Schema(description = "获取最后一条用户消息")
    private String getLastUserMessage(AgentChatRequestDTO request) {
        if (request.getMessages() == null || request.getMessages().isEmpty()) {
            return "";
        }

        return request.getMessages().get(request.getMessages().size() - 1).getContent();
    }

    /**
     * 发送 SSE 事件
     *
     * @param emitter SSE 发射器
     * @param eventName 事件名称
     * @param data    数据
     */
    @Schema(description = "发送 SSE 事件")
    private void sendSseEvent(SseEmitter emitter, String eventName, Map<String, Object> data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            emitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(json));
        } catch (Exception e) {
            log.warn("SSE 发送失败: {}", e.getMessage());
        }
    }

    /**
     * 完成 SSE 连接
     *
     * @param emitter SSE 发射器
     */
    @Schema(description = "完成 SSE 连接")
    private void completeSse(SseEmitter emitter) {
        try {
            emitter.complete();
        } catch (Exception e) {
            log.debug("SSE 完成异常: {}", e.getMessage());
        }
    }

    /**
     * SSE 发射器捕获
     * 用于捕获 Agent 执行的输出
     */
    @Schema(description = "SSE 发射器捕获")
    private static class SseEmitterCapture {
        private final SseEmitter sseEmitter = new SseEmitter(60000L);
        private final StringBuilder output = new StringBuilder();
        private final CompletableFuture<Void> completionFuture = new CompletableFuture<>();
        private boolean completed = false;

        public SseEmitter getSseEmitter() {
            // 设置完成回调
            sseEmitter.onCompletion(() -> {
                completed = true;
                completionFuture.complete(null);
            });

            sseEmitter.onTimeout(() -> {
                completed = true;
                completionFuture.complete(null);
            });

            return sseEmitter;
        }

        public String getOutput() {
            return output.toString();
        }

        public void awaitCompletion() {
            try {
                completionFuture.get();
            } catch (Exception e) {
                log.warn("等待完成失败: {}", e.getMessage());
            }
        }

        public boolean isCompleted() {
            return completed;
        }
    }
}
