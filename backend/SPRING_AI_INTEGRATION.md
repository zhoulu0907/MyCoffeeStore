# Spring AI Alibaba 集成文档

## 概述

本项目已完成 Spring AI Alibaba 集成基础架构搭建，支持以下 LLM 提供商：

- **ModelScope** - 阿里云 ModelScope 服务（已实现）
- **DashScope** - 阿里云通义千问服务（待实现）
- **OpenAI** - OpenAI 服务（待实现）

## 架构设计

### 包结构

```
com.mycoffeestore.ai
├── config/           # 配置类
│   ├── LlmProperties.java
│   ├── LlmAutoConfiguration.java
│   └── ModelScopeChatOptions.java
├── core/             # 核心抽象
│   └── LlmProvider.java
├── modelscope/       # ModelScope 适配器
│   ├── ModelScopeChatModel.java
│   └── ModelScopeStreamingChatModel.java
└── factory/          # 工厂类
    └── ChatModelFactory.java
```

### 核心类说明

#### 1. LlmProvider（枚举）
定义支持的 LLM 服务提供商类型
- `MODELSCOPE` - ModelScope 提供商
- `DASHSCOPE` - DashScope 提供商
- `OPENAI` - OpenAI 提供商

#### 2. LlmProperties（配置属性）
统一管理所有 LLM 提供商的配置，支持：
- ModelScope 配置（API密钥、模型名称、超时时间等）
- DashScope 配置
- OpenAI 配置
- 通用配置（最大Token数、流式输出等）

#### 3. ModelScopeChatOptions（聊天选项）
定义 ModelScope API 调用的可配置参数：
- model - 模型名称
- temperature - 温度参数（0-1）
- maxTokens - 最大Token数
- topP - Top-P采样参数
- topK - Top-K采样参数
- stop - 停止序列
- stream - 是否启用流式输出

#### 4. ModelScopeChatModel（同步模型）
实现同步调用模式：
- `call()` - 阻塞式调用
- `stream()` - 流式调用

#### 5. ModelScopeStreamingChatModel（流式模型）
专注于流式调用场景，提供更好的流式处理性能

#### 6. ChatModelFactory（工厂类）
根据 LLM 提供商创建对应的 ChatModel 实例：
- 支持动态切换提供商
- 内置缓存机制提升性能
- 自动配置 WebClient

## 配置说明

### 基本配置

在 `application.yml` 中添加以下配置：

```yaml
llm:
  provider: modelscope
  modelscope:
    api-key: your-api-key
    model: kimih/K2.5-Instruct
    timeout: 60000
```

### 环境变量配置

推荐使用环境变量：

```bash
export MODELSCOPE_API_KEY=your-api-key
```

### 完整配置示例

参见 `application-llm.yml` 文件。

## 使用指南

### 1. 注入 ChatModelFactory

```java
@Autowired
private ChatModelFactory chatModelFactory;
```

### 2. 创建 ChatModel

```java
// 使用默认提供商
ModelScopeChatModel chatModel = chatModelFactory.createChatModel();

// 指定提供商
ModelScopeChatModel chatModel = chatModelFactory.createChatModel(LlmProvider.MODELSCOPE);

// 指定提供商和模型
ModelScopeChatModel chatModel = chatModelFactory.createChatModel(
    LlmProvider.MODELSCOPE, 
    "kimih/K2.5-Instruct"
);
```

### 3. 同步调用

```java
List<Message> messages = List.of(
    new Message("user", "你好")
);

String response = chatModel.call(messages);
System.out.println(response);
```

### 4. 流式调用

```java
List<Message> messages = List.of(
    new Message("user", "介绍一下咖啡")
);

Flux<String> responseStream = chatModel.stream(messages);
responseStream.subscribe(
    content -> System.out.print(content),
    error -> System.err.println("Error: " + error),
    () -> System.out.println("\nDone")
);
```

### 5. 使用自定义选项

```java
ModelScopeChatOptions options = ModelScopeChatOptions.builder()
    .model("kimih/K2.5-Instruct")
    .temperature(0.8)
    .maxTokens(2048)
    .stream(true)
    .build();

String response = chatModel.call(messages, options);
```

## 编码规范

- 所有类使用 `@Author` 标签
- 使用 `@Schema` 注解描述类用途
- 遵循 Java 17+ 语法
- 使用 Lombok 简化代码

## 后续计划

### 阶段二：高级功能实现（Week 3-4）

1. **多模态支持**
   - [ ] 图片理解
   - [ ] 文档解析
   - [ ] 多模态对话

2. **RAG 增强**
   - [ ] 向量数据库集成
   - [ ] 文档索引
   - [ ] 语义搜索

3. **Agent 能力**
   - [ ] 工具调用
   - [ ] 函数编排
   - [ ] 多轮对话

4. **可观测性**
   - [ ] 调用链追踪
   - [ ] 性能监控
   - [ ] 错误日志

## 技术栈

- Spring Boot 3.1.5
- Spring AI Alibaba 1.0.0-M2
- WebFlux（响应式 HTTP 客户端）
- Jackson（JSON 处理）
- Lombok（代码简化）

## 参考资料

- [Spring AI 官方文档](https://docs.spring.io/spring-ai/reference/)
- [Spring AI Alibaba 文档](https://github.com/alibaba/spring-ai-alibaba)
- [ModelScope API 文档](https://api-inference.modelscope.cn/doc)

## 更新日志

### 2026-03-05
- ✅ 添加 Spring AI Alibaba 依赖
- ✅ 创建核心包结构
- ✅ 实现核心类（LlmProvider、LlmProperties 等）
- ✅ 实现 ModelScope ChatModel
- ✅ 实现 ChatModelFactory
- ✅ 创建配置文件和文档
