package com.mycoffeestore.service.routing;

import com.mycoffeestore.agent.routing.AgentRoutingConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 意图识别服务
 * 负责识别用户输入的意图类型，并返回推荐的 Agent
 *
 * 功能：
 * 1. 基于 LLM 的智能意图识别
 * 2. 关键词匹配快速识别
 * 3. 意图置信度评估
 * 4. 支持多意图识别
 *
 * @author Backend Developer
 * @since 2026-03-07
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Schema(description = "意图识别服务")
public class IntentRecognitionService {

    @Qualifier("intentRecognitionChatModel")
    private final org.springframework.ai.chat.model.ChatModel intentRecognitionChatModel;

    /**
     * 意图关键词映射
     * 用于快速识别常见意图
     */
    @Schema(description = "意图关键词映射")
    private static final List<IntentKeyword> INTENT_KEYWORDS = Arrays.asList(
            // 购买订单意图关键词
            new IntentKeyword(
                    AgentRoutingConfig.IntentType.ORDER_PURCHASE,
                    Arrays.asList("买", "下单", "订购", "支付", "结账", "购买", "要一杯", "来一杯", "我想买", "我要买")
            ),
            // 投诉意图关键词
            new IntentKeyword(
                    AgentRoutingConfig.IntentType.COMPLAINT,
                    Arrays.asList("投诉", "问题", "不满", "质量差", "服务差", "难喝", "慢", "态度差", "不新鲜", "投诉")
            ),
            // 咨询意图关键词
            new IntentKeyword(
                    AgentRoutingConfig.IntentType.CONSULT,
                    Arrays.asList("推荐", "建议", "哪种", "什么好", "口味", "风味", "酸", "苦", "浓", "淡", "咨询", "推荐")
            ),
            // 订单查询意图关键词
            new IntentKeyword(
                    AgentRoutingConfig.IntentType.ORDER_QUERY,
                    Arrays.asList("订单", "我的订单", "订单状态", "查订单", "订单到哪", "配送", "进度")
            )
    );

    /**
     * 意图识别结果
     */
    @lombok.Builder
    @lombok.Data
    @Schema(description = "意图识别结果")
    public static class IntentRecognitionResult {
        /**
         * 识别的意图类型
         */
        @Schema(description = "识别的意图类型")
        private AgentRoutingConfig.IntentType intentType;

        /**
         * 置信度（0.0 - 1.0）
         */
        @Schema(description = "置信度")
        private Double confidence;

        /**
         * 推荐的 Agent 名称列表
         */
        @Schema(description = "推荐的 Agent 名称列表")
        private List<String> recommendedAgents;

        /**
         * 识别方法（keyword / llm / fallback）
         */
        @Schema(description = "识别方法")
        private String recognitionMethod;

        /**
         * 原始用户输入
         */
        @Schema(description = "原始用户输入")
        private String userInput;

        /**
         * 识别耗时（毫秒）
         */
        @Schema(description = "识别耗时（毫秒）")
        private Long durationMs;
    }

    /**
     * 意图关键词映射
     */
    @lombok.Builder
    @lombok.Data
    @Schema(description = "意图关键词映射")
    private static class IntentKeyword {
        /**
         * 意图类型
         */
        private AgentRoutingConfig.IntentType intentType;

        /**
         * 关键词列表
         */
        private List<String> keywords;

        /**
         * 检查输入是否包含任何关键词
         */
        public boolean matches(String input) {
            return keywords.stream()
                    .anyMatch(keyword -> input.contains(keyword));
        }

        /**
         * 计算匹配分数（匹配的关键词数量）
         */
        public int matchScore(String input) {
            return (int) keywords.stream()
                    .filter(input::contains)
                    .count();
        }
    }

    /**
     * 识别用户意图
     * 首先尝试关键词快速匹配，如果失败则使用 LLM 进行智能识别
     *
     * @param userInput 用户输入
     * @return 意图识别结果
     */
    @Schema(description = "识别用户意图")
    public IntentRecognitionResult recognizeIntent(String userInput) {
        long startTime = System.currentTimeMillis();

        log.debug("开始识别用户意图: {}", userInput);

        try {
            // 步骤 1: 尝试关键词快速匹配
            IntentRecognitionResult keywordResult = recognizeByKeywords(userInput);
            if (keywordResult != null && keywordResult.getConfidence() > 0.7) {
                keywordResult.setDurationMs(System.currentTimeMillis() - startTime);
                log.info("关键词识别成功: intent={}, confidence={}",
                        keywordResult.getIntentType(), keywordResult.getConfidence());
                return keywordResult;
            }

            // 步骤 2: 使用 LLM 进行智能识别
            IntentRecognitionResult llmResult = recognizeByLLM(userInput);
            llmResult.setDurationMs(System.currentTimeMillis() - startTime);
            log.info("LLM 识别完成: intent={}, confidence={}",
                    llmResult.getIntentType(), llmResult.getConfidence());
            return llmResult;

        } catch (Exception e) {
            log.error("意图识别失败: {}", e.getMessage(), e);
            // 返回默认的一般对话意图
            return IntentRecognitionResult.builder()
                    .intentType(AgentRoutingConfig.IntentType.GENERAL)
                    .confidence(0.5)
                    .recommendedAgents(Arrays.asList("general_chat"))
                    .recognitionMethod("fallback")
                    .userInput(userInput)
                    .durationMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    /**
     * 基于关键词的快速意图识别
     *
     * @param userInput 用户输入
     * @return 意图识别结果，如果无法识别返回 null
     */
    @Schema(description = "基于关键词识别意图")
    private IntentRecognitionResult recognizeByKeywords(String userInput) {
        String normalizedInput = userInput.toLowerCase().trim();

        // 找出匹配分数最高的意图
        IntentKeyword bestMatch = null;
        int bestScore = 0;

        for (IntentKeyword intentKeyword : INTENT_KEYWORDS) {
            int score = intentKeyword.matchScore(normalizedInput);
            if (score > bestScore) {
                bestScore = score;
                bestMatch = intentKeyword;
            }
        }

        // 如果没有匹配或置信度太低，返回 null
        if (bestMatch == null || bestScore == 0) {
            return null;
        }

        // 计算置信度（基于匹配的关键词数量）
        double confidence = Math.min(0.9, 0.5 + (bestScore * 0.1));

        return IntentRecognitionResult.builder()
                .intentType(bestMatch.getIntentType())
                .confidence(confidence)
                .recommendedAgents(getRecommendedAgents(bestMatch.getIntentType()))
                .recognitionMethod("keyword")
                .userInput(userInput)
                .build();
    }

    /**
     * 基于 LLM 的智能意图识别
     *
     * @param userInput 用户输入
     * @return 意图识别结果
     */
    @Schema(description = "基于 LLM 识别意图")
    private IntentRecognitionResult recognizeByLLM(String userInput) {
        // 构建意图类型描述
        String intentDescriptions = String.join("\n",
                Arrays.stream(AgentRoutingConfig.IntentType.values())
                        .map(intent -> String.format("- %s: %s", intent.getCode(), intent.getDescription()))
                        .toArray(String[]::new)
        );

        // 构建系统消息
        Message systemMessage = new SystemMessage(String.format("""
                你是意图识别专家。分析用户输入，识别其意图类型。

                ## 支持的意图类型
                %s

                ## 识别规则
                1. 用户明确提到购买相关词汇 → order_purchase
                2. 用户提到投诉或问题 → complaint
                3. 用户询问咖啡相关问题 → consult
                4. 用户询问订单相关信息 → order_query
                5. 其他一般性对话 → general

                ## 输出格式（JSON）
                {
                  "intent": "意图类型代码",
                  "confidence": 置信度(0.0-1.0),
                  "reasoning": "识别理由"
                }

                仅返回 JSON，不要包含其他内容。
                """, intentDescriptions));

        // 构建用户消息
        Message userMessage = new UserMessage(userInput);

        // 创建 ChatClient
        ChatClient chatClient = ChatClient.builder((org.springframework.ai.chat.model.ChatModel) intentRecognitionChatModel)
                .defaultOptions(com.mycoffeestore.ai.config.ModelScopeChatOptions.builder()
                        .temperature(0.3)
                        .maxTokens(200)
                        .build())
                .build();

        try {
            // 调用 LLM
            ChatResponse response = chatClient.prompt()
                    .messages(systemMessage, userMessage)
                    .call()
                    .chatResponse();

            String responseText = response.getResult().getOutput().getContent();
            log.debug("LLM 意图识别原始响应: {}", responseText);

            // 解析响应
            return parseLLMResponse(responseText, userInput);

        } catch (Exception e) {
            log.warn("LLM 意图识别失败，使用默认值: {}", e.getMessage());
            // 返回默认意图
            return IntentRecognitionResult.builder()
                    .intentType(AgentRoutingConfig.IntentType.GENERAL)
                    .confidence(0.5)
                    .recommendedAgents(Arrays.asList("general_chat"))
                    .recognitionMethod("llm_fallback")
                    .userInput(userInput)
                    .build();
        }
    }

    /**
     * 解析 LLM 响应
     *
     * @param responseText LLM 响应文本
     * @param userInput    原始用户输入
     * @return 意图识别结果
     */
    @Schema(description = "解析 LLM 响应")
    private IntentRecognitionResult parseLLMResponse(String responseText, String userInput) {
        try {
            // 尝试提取 JSON
            String jsonText = extractJson(responseText);

            // 简单解析（实际项目中应使用 Jackson 或 Gson）
            String intentCode = extractJsonValue(jsonText, "intent");
            String confidenceStr = extractJsonValue(jsonText, "confidence");

            // 查找对应的意图类型
            AgentRoutingConfig.IntentType intentType = Arrays.stream(AgentRoutingConfig.IntentType.values())
                    .filter(intent -> intent.getCode().equals(intentCode))
                    .findFirst()
                    .orElse(AgentRoutingConfig.IntentType.GENERAL);

            // 解析置信度
            double confidence = 0.5;
            try {
                confidence = Double.parseDouble(confidenceStr);
                confidence = Math.max(0.0, Math.min(1.0, confidence)); // 限制在 0-1 之间
            } catch (NumberFormatException e) {
                log.debug("无法解析置信度: {}", confidenceStr);
            }

            return IntentRecognitionResult.builder()
                    .intentType(intentType)
                    .confidence(confidence)
                    .recommendedAgents(getRecommendedAgents(intentType))
                    .recognitionMethod("llm")
                    .userInput(userInput)
                    .build();

        } catch (Exception e) {
            log.warn("解析 LLM 响应失败: {}", e.getMessage());
            // 返回默认意图
            return IntentRecognitionResult.builder()
                    .intentType(AgentRoutingConfig.IntentType.GENERAL)
                    .confidence(0.5)
                    .recommendedAgents(Arrays.asList("general_chat"))
                    .recognitionMethod("llm_fallback")
                    .userInput(userInput)
                    .build();
        }
    }

    /**
     * 从文本中提取 JSON
     *
     * @param text 文本
     * @return JSON 字符串
     */
    @Schema(description = "从文本中提取 JSON")
    private String extractJson(String text) {
        // 查找第一个 { 和最后一个 }
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');

        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }

        return text;
    }

    /**
     * 从 JSON 中提取值
     *
     * @param json      JSON 字符串
     * @param key       键名
     * @return 值
     */
    @Schema(description = "从 JSON 中提取值")
    private String extractJsonValue(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*\"?([^,}\\\"]+)\"?";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);

        if (m.find()) {
            return m.group(1).trim();
        }

        return "";
    }

    /**
     * 根据意图类型获取推荐的 Agent
     *
     * @param intentType 意图类型
     * @return 推荐的 Agent 名称列表
     */
    @Schema(description = "根据意图类型获取推荐的 Agent")
    private List<String> getRecommendedAgents(AgentRoutingConfig.IntentType intentType) {
        switch (intentType) {
            case ORDER_PURCHASE:
                return Arrays.asList("coffee_advisor", "order_assistant", "customer_service");
            case COMPLAINT:
                return Arrays.asList("customer_service");
            case CONSULT:
                return Arrays.asList("coffee_advisor");
            case ORDER_QUERY:
                return Arrays.asList("order_assistant");
            case GENERAL:
            default:
                return Arrays.asList("general_chat");
        }
    }

    /**
     * 批量识别意图
     *
     * @param userInputs 用户输入列表
     * @return 意图识别结果列表
     */
    @Schema(description = "批量识别意图")
    public List<IntentRecognitionResult> recognizeIntents(List<String> userInputs) {
        return userInputs.stream()
                .map(this::recognizeIntent)
                .toList();
    }

    /**
     * 获取所有支持的意图类型
     *
     * @return 意图类型列表
     */
    @Schema(description = "获取所有支持的意图类型")
    public List<AgentRoutingConfig.IntentType> getSupportedIntents() {
        return Arrays.asList(AgentRoutingConfig.IntentType.values());
    }

    /**
     * 验证意图识别结果
     * 检查置信度是否足够高
     *
     * @param result           意图识别结果
     * @param minConfidence 最小置信度阈值
     * @return 是否有效
     */
    @Schema(description = "验证意图识别结果")
    public boolean isConfidenceValid(IntentRecognitionResult result, double minConfidence) {
        return result != null && result.getConfidence() >= minConfidence;
    }
}
