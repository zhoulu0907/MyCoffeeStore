# Spring AI Alibaba 集成 - 项目结构

## 包结构
```
com.mycoffeestore.ai
├── config/                              # 配置包
│   ├── LlmProperties.java              # LLM 通用配置属性
│   ├── LlmAutoConfiguration.java       # 自动配置类
│   └── ModelScopeChatOptions.java      # ModelScope 聊天选项
├── core/                                # 核心抽象包
│   └── LlmProvider.java                # LLM 提供商枚举
├── modelscope/                          # ModelScope 适配器包
│   ├── ModelScopeChatModel.java        # 同步 ChatModel 实现
│   └── ModelScopeStreamingChatModel.java # 流式 ChatModel 实现
└── factory/                             # 工厂包
    └── ChatModelFactory.java           # ChatModel 工厂类
```

## 类关系图
```
┌─────────────────────┐
│   LlmProvider       │
│   (Enum)            │
├─────────────────────┤
│ + MODELSCOPE        │
│ + DASHSCOPE         │
│ + OPENAI            │
│ + fromCode()        │
└─────────────────────┘
         △
         │
         │ uses
         │
┌─────────────────────┐       ┌─────────────────────┐
│  LlmProperties      │       │ModelScopeChatOptions│
├─────────────────────┤       ├─────────────────────┤
│ + provider          │──────▶│ + model             │
│ + modelscope        │       │ + temperature       │
│ + dashscope         │       │ + maxTokens         │
│ + openai            │       │ + stream            │
│ + common            │       │ + toMap()           │
└─────────────────────┘       └─────────────────────┘
         △                                    △
         │                                    │
         │ configures                        │ configures
         │                                    │
┌─────────────────────┐       ┌─────────────────────┐
│ LlmAutoConfiguration│       │ModelScopeChatModel  │
├─────────────────────┤       ├─────────────────────┤
│ + chatModelFactory()│       │ + call()            │
└─────────────────────┘       │ + stream()          │
         │                    └─────────────────────┘
         │                             △
         │ creates                    │
         │                            │
┌─────────────────────┐               │
│ ChatModelFactory    ├───────────────┘
├─────────────────────┤               │
│ + createChatModel() │               │ implements
│ + createStreaming() │               │
│ + switchProvider()  │       ┌─────────────────────┐
│ + clearCache()      │       │ModelScopeStreaming  │
│ + getCacheStats()   │       │ChatModel            │
└─────────────────────┘       ├─────────────────────┤
                               │ + stream()          │
                               │ + parseStreamChunk()│
                               └─────────────────────┘
```

## 配置层次结构
```
application.yml
       │
       ▼
┌─────────────────────┐
│ llm                 │
├─────────────────────┤
│ provider: MODELSCOPE│
│                     │
│ modelscope:         │───▶ Base URL
│   - baseUrl         │───▶ API Key
│   - apiKey          │───▶ Model Name
│   - model           │───▶ Timeout
│   - timeout         │
│                     │
│ common:             │───▶ Max Tokens
│   - maxTokens       │───▶ Stream
│   - stream          │
└─────────────────────┘
       │
       ▼
┌─────────────────────┐
│ LlmProperties       │
├─────────────────────┤
│ (Spring Bean)       │
│ @Configuration      │
│   Properties        │
└─────────────────────┘
       │
       ▼
┌─────────────────────┐
│ ChatModelFactory    │
├─────────────────────┤
│ (Spring Bean)       │
│ Creates instances   │
│ based on config     │
└─────────────────────┘
```

## 使用流程
```
1. 配置 (application.yml)
   │
   ▼
2. 自动配置 (LlmAutoConfiguration)
   │
   ▼
3. 创建工厂 (ChatModelFactory)
   │
   ▼
4. 创建模型 (ModelScopeChatModel)
   │
   ▼
5. 调用 API (call/stream)
   │
   ▼
6. 获取响应
```

## 关键设计模式

### 1. 工厂模式
- `ChatModelFactory` 负责创建不同提供商的 ChatModel 实例
- 支持动态切换提供商
- 缓存已创建的实例

### 2. 策略模式
- `LlmProvider` 枚举定义不同的提供商策略
- 每个提供商可以有独立的配置和实现

### 3. 建造者模式
- `ModelScopeChatOptions` 使用 Lombok @Builder
- 提供流畅的 API 来构建复杂配置

### 4. 配置模式
- Spring Boot `@ConfigurationProperties`
- 类型安全的配置绑定
- 支持默认值和验证

## 扩展点

### 添加新的提供商
1. 在 `LlmProvider` 枚举中添加新值
2. 在 `LlmProperties` 中添加对应的配置类
3. 在 `ChatModelFactory` 中实现创建逻辑
4. 创建对应的 ChatModel 实现

### 添加新的配置选项
1. 在 `ModelScopeChatOptions` 中添加字段
2. 更新 `toMap()` 方法以包含新选项
3. 在配置文件中添加对应配置

## 技术栈
- **Spring Boot** 3.1.5 - 应用框架
- **Spring AI Alibaba** 1.0.0-M2 - AI 集成
- **WebFlux** - 响应式 HTTP 客户端
- **Jackson** - JSON 处理
- **Lombok** - 代码简化
- **Swagger** - API 文档

## 编译产物
```
target/classes/com/mycoffeestore/ai/
├── core/
│   └── LlmProvider.class
├── config/
│   ├── LlmAutoConfiguration.class
│   ├── LlmProperties.class
│   ├── LlmProperties$ModelScopeConfig.class
│   ├── LlmProperties$DashScopeConfig.class
│   ├── LlmProperties$OpenAIConfig.class
│   ├── LlmProperties$CommonConfig.class
│   └── ModelScopeChatOptions.class
├── modelscope/
│   ├── ModelScopeChatModel.class
│   ├── ModelScopeChatModel$Message.class
│   ├── ModelScopeStreamingChatModel.class
│   └── ModelScopeStreamingChatModel$ChatResponse.class
└── factory/
    └── ChatModelFactory.class
```

## 下一步
- [ ] 实现单元测试
- [ ] 实现集成测试
- [ ] 添加日志记录
- [ ] 实现错误处理
- [ ] 添加性能监控
- [ ] 实现缓存策略
