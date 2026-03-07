-- MyCoffeeStore 对话记忆表创建脚本
-- 创建时间：2026-03-07
-- 说明：用于存储 AI Agent 与用户的对话历史记录，支持 Redis + PostgreSQL 分层存储

-- 对话记忆表
CREATE TABLE IF NOT EXISTS mcs_agent_conversation (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    session_id VARCHAR(64) NOT NULL,
    agent_id VARCHAR(32) NOT NULL,
    message_id VARCHAR(64) NOT NULL,
    role VARCHAR(16) NOT NULL,
    content TEXT NOT NULL,
    metadata JSONB DEFAULT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_message_id UNIQUE (message_id)
);

-- 表注释
COMMENT ON TABLE mcs_agent_conversation IS 'AI Agent 对话记忆表，存储与用户的对话历史记录';

-- 字段注释
COMMENT ON COLUMN mcs_agent_conversation.id IS '主键ID';
COMMENT ON COLUMN mcs_agent_conversation.user_id IS '用户ID，关联 mcs_user 表';
COMMENT ON COLUMN mcs_agent_conversation.session_id IS '会话ID，用于关联同一会话的所有消息';
COMMENT ON COLUMN mcs_agent_conversation.agent_id IS 'Agent类型（coffee_advisor/customer_service/order_assistant）';
COMMENT ON COLUMN mcs_agent_conversation.message_id IS '消息ID，唯一标识单条消息';
COMMENT ON COLUMN mcs_agent_conversation.role IS '消息角色（user/assistant/system）';
COMMENT ON COLUMN mcs_agent_conversation.content IS '消息内容';
COMMENT ON COLUMN mcs_agent_conversation.metadata IS '元数据（JSON格式，存储token数量、模型参数等）';
COMMENT ON COLUMN mcs_agent_conversation.created_at IS '消息创建时间，用于排序';
COMMENT ON COLUMN mcs_agent_conversation.create_time IS '记录创建时间';
COMMENT ON COLUMN mcs_agent_conversation.update_time IS '记录更新时间';
COMMENT ON COLUMN mcs_agent_conversation.is_deleted IS '删除标记：0-未删除，1-已删除';

-- 创建索引以提升查询性能
-- 会话ID索引（用于查询完整会话历史）
CREATE INDEX IF NOT EXISTS idx_conversation_session_id ON mcs_agent_conversation(session_id);

-- 用户ID索引（用于查询用户的所有对话）
CREATE INDEX IF NOT EXISTS idx_conversation_user_id ON mcs_agent_conversation(user_id);

-- Agent类型索引（用于按Agent类型过滤）
CREATE INDEX IF NOT EXISTS idx_conversation_agent_id ON mcs_agent_conversation(agent_id);

-- 用户ID + AgentID组合索引（用于查询用户与特定Agent的对话历史）
CREATE INDEX IF NOT EXISTS idx_conversation_user_agent ON mcs_agent_conversation(user_id, agent_id);

-- 创建时间索引（用于时间范围查询和数据清理）
CREATE INDEX IF NOT EXISTS idx_conversation_created_at ON mcs_agent_conversation(created_at);

-- 用户ID + 创建时间组合索引（用于查询用户最近的对话）
CREATE INDEX IF NOT EXISTS idx_conversation_user_created ON mcs_agent_conversation(user_id, created_at DESC);

-- 添加外键约束（可选，根据需要启用）
-- ALTER TABLE mcs_agent_conversation
--     ADD CONSTRAINT fk_conversation_user
--     FOREIGN KEY (user_id) REFERENCES mcs_user(id)
--     ON DELETE CASCADE;
