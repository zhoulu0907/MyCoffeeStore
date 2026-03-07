-- 对话记忆表
-- 用于持久化存储用户与 Agent 的对话历史

CREATE TABLE IF NOT EXISTS mcs_conversation_memory (
    id BIGSERIAL PRIMARY KEY,
    session_id VARCHAR(128) NOT NULL COMMENT '会话 ID',
    user_id BIGINT COMMENT '用户 ID（未登录用户为 NULL）',
    agent_type VARCHAR(50) NOT NULL COMMENT 'Agent 类型（coffee_advisor / customer_service / order_assistant）',
    messages TEXT COMMENT '对话消息列表（JSON 格式）',
    title VARCHAR(255) COMMENT '会话标题',
    last_active_at TIMESTAMP COMMENT '最后活跃时间',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted INTEGER NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除'
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_conversation_memory_session_id ON mcs_conversation_memory(session_id);
CREATE INDEX IF NOT EXISTS idx_conversation_memory_user_id ON mcs_conversation_memory(user_id);
CREATE INDEX IF NOT EXISTS idx_conversation_memory_agent_type ON mcs_conversation_memory(agent_type);
CREATE INDEX IF NOT EXISTS idx_conversation_memory_last_active ON mcs_conversation_memory(last_active_at);

-- 添加表注释
COMMENT ON TABLE mcs_conversation_memory IS '对话记忆表';
