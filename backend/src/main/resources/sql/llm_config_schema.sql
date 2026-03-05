-- LLM 配置管理数据库初始化脚本 (PostgreSQL)
-- 创建时间：2026-03-05
-- 阶段二：Week 3-4 配置管理与安全

-- ========================================
-- LLM 提供商表
-- ========================================
CREATE TABLE IF NOT EXISTS mcs_llm_provider (
  id BIGSERIAL PRIMARY KEY,
  provider_code VARCHAR(32) NOT NULL,
  provider_name VARCHAR(64) NOT NULL,
  provider_type VARCHAR(32) NOT NULL,
  api_endpoint VARCHAR(255) NOT NULL,
  description TEXT,
  status SMALLINT NOT NULL DEFAULT 1,
  sort_order INTEGER NOT NULL DEFAULT 0,
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted SMALLINT NOT NULL DEFAULT 0,
  CONSTRAINT uk_llm_provider_code UNIQUE (provider_code)
);

CREATE INDEX IF NOT EXISTS idx_llm_provider_code ON mcs_llm_provider(provider_code);
CREATE INDEX IF NOT EXISTS idx_llm_provider_status ON mcs_llm_provider(status);

-- ========================================
-- LLM 模型配置表
-- ========================================
CREATE TABLE IF NOT EXISTS mcs_llm_model_config (
  id BIGSERIAL PRIMARY KEY,
  provider_id BIGINT NOT NULL,
  model_code VARCHAR(64) NOT NULL,
  model_name VARCHAR(128) NOT NULL,
  model_id VARCHAR(128) NOT NULL,
  model_type VARCHAR(32) NOT NULL,
  context_window INTEGER NOT NULL DEFAULT 4096,
  max_output_tokens INTEGER NOT NULL DEFAULT 2048,
  support_streaming SMALLINT NOT NULL DEFAULT 1,
  support_function_calling SMALLINT NOT NULL DEFAULT 0,
  pricing_input DECIMAL(10,6) DEFAULT NULL,
  pricing_output DECIMAL(10,6) DEFAULT NULL,
  status SMALLINT NOT NULL DEFAULT 1,
  sort_order INTEGER NOT NULL DEFAULT 0,
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted SMALLINT NOT NULL DEFAULT 0,
  CONSTRAINT fk_llm_model_provider FOREIGN KEY (provider_id) REFERENCES mcs_llm_provider(id) ON DELETE CASCADE,
  CONSTRAINT uk_llm_provider_model UNIQUE (provider_id, model_code)
);

CREATE INDEX IF NOT EXISTS idx_llm_model_provider ON mcs_llm_model_config(provider_id);
CREATE INDEX IF NOT EXISTS idx_llm_model_code ON mcs_llm_model_config(model_code);
CREATE INDEX IF NOT EXISTS idx_llm_model_status ON mcs_llm_model_config(status);

-- ========================================
-- LLM 用户配置表（加密存储 API Key）
-- ========================================
CREATE TABLE IF NOT EXISTS mcs_llm_user_config (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  provider_id BIGINT NOT NULL,
  api_key_encrypted TEXT NOT NULL,
  api_key_iv VARCHAR(64) NOT NULL,
  config_json JSONB DEFAULT NULL,
  status SMALLINT NOT NULL DEFAULT 1,
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted SMALLINT NOT NULL DEFAULT 0,
  CONSTRAINT fk_llm_config_provider FOREIGN KEY (provider_id) REFERENCES mcs_llm_provider(id) ON DELETE CASCADE,
  CONSTRAINT uk_llm_user_provider UNIQUE (user_id, provider_id)
);

CREATE INDEX IF NOT EXISTS idx_llm_user_config_user ON mcs_llm_user_config(user_id);
CREATE INDEX IF NOT EXISTS idx_llm_user_config_provider ON mcs_llm_user_config(provider_id);
CREATE INDEX IF NOT EXISTS idx_llm_user_config_status ON mcs_llm_user_config(status);

-- ========================================
-- 插入默认提供商数据
-- ========================================
INSERT INTO mcs_llm_provider (provider_code, provider_name, provider_type, api_endpoint, description, status, sort_order) VALUES
-- 国内服务商
('modelscope', 'ModelScope 灵积', 'modelscope', 'https://api-inference.modelscope.cn/v1', 'ModelScope 灵积 AI 模型服务', 1, 100),
('qwen_dashscope', '阿里云灵积', 'dashscope', 'https://dashscope.aliyuncs.com/compatible-mode/v1', '阿里云通义千问 API', 1, 90),
('hunyuan', '腾讯混元', 'hunyuan', 'https://hunyuan.tencentcloudapi.com', '腾讯混元 AI 大模型', 1, 80),
('zhipu', '智谱 AI', 'zhipu', 'https://open.bigmodel.cn/api/paas/v4', '智谱 AI GLM 系列模型', 1, 70),
('baichuan', '百川智能', 'baichuan', 'https://api.baichuan-ai.com/v1', '百川智能大模型服务', 1, 60),
-- 国际服务商
('openai', 'OpenAI', 'openai', 'https://api.openai.com/v1', 'OpenAI GPT 系列模型', 1, 50),
('anthropic', 'Anthropic', 'anthropic', 'https://api.anthropic.com/v1', 'Anthropic Claude 系列模型', 1, 40),
('openrouter', 'OpenRouter', 'openrouter', 'https://openrouter.ai/api/v1', 'OpenRouter 多模型聚合服务', 1, 30),
('deepseek', 'DeepSeek', 'deepseek', 'https://api.deepseek.com/v1', 'DeepSeek 深度求索模型', 1, 20)
ON CONFLICT (provider_code) DO NOTHING;

-- ========================================
-- 插入默认模型配置数据
-- ========================================

-- ModelScope 模型
INSERT INTO mcs_llm_model_config (provider_id, model_code, model_name, model_id, model_type, context_window, max_output_tokens, support_streaming, support_function_calling, pricing_input, pricing_output, status, sort_order) VALUES
((SELECT id FROM mcs_llm_provider WHERE provider_code = 'modelscope'), 'kimi-k2.5', 'Kimi K2.5', 'moonshotai/Kimi-K2.5', 'chat', 32768, 8192, 1, 1, 0.000012, 0.000012, 1, 100),
((SELECT id FROM mcs_llm_provider WHERE provider_code = 'modelscope'), 'qwen-max', 'Qwen Max', 'Qwen/Qwen2.5-72B-Instruct', 'chat', 32768, 8192, 1, 1, 0.000020, 0.000020, 1, 99),
((SELECT id FROM mcs_llm_provider WHERE provider_code = 'modelscope'), 'qwen-plus', 'Qwen Plus', 'Qwen/Qwen2.5-32B-Instruct', 'chat', 32768, 8192, 1, 1, 0.000008, 0.000008, 1, 98),
((SELECT id FROM mcs_llm_provider WHERE provider_code = 'modelscope'), 'qwen-turbo', 'Qwen Turbo', 'Qwen/Qwen2.5-14B-Instruct', 'chat', 8192, 4096, 1, 1, 0.000003, 0.000003, 1, 97)
ON CONFLICT (provider_id, model_code) DO NOTHING;

-- 阿里云灵积模型
INSERT INTO mcs_llm_model_config (provider_id, model_code, model_name, model_id, model_type, context_window, max_output_tokens, support_streaming, support_function_calling, pricing_input, pricing_output, status, sort_order) VALUES
((SELECT id FROM mcs_llm_provider WHERE provider_code = 'qwen_dashscope'), 'qwen-max', 'Qwen Max', 'qwen-max', 'chat', 32768, 8192, 1, 1, 0.000020, 0.000020, 1, 100),
((SELECT id FROM mcs_llm_provider WHERE provider_code = 'qwen_dashscope'), 'qwen-plus', 'Qwen Plus', 'qwen-plus', 'chat', 32768, 8192, 1, 1, 0.000008, 0.000008, 1, 99),
((SELECT id FROM mcs_llm_provider WHERE provider_code = 'qwen_dashscope'), 'qwen-turbo', 'Qwen Turbo', 'qwen-turbo', 'chat', 8192, 4096, 1, 1, 0.000003, 0.000003, 1, 98),
((SELECT id FROM mcs_llm_provider WHERE provider_code = 'qwen_dashscope'), 'qwen-long', 'Qwen Long', 'qwen-long', 'chat', 1048576, 4096, 1, 0, 0.000100, 0.000100, 1, 97)
ON CONFLICT (provider_id, model_code) DO NOTHING;

-- 腾讯混元模型
INSERT INTO mcs_llm_model_config (provider_id, model_code, model_name, model_id, model_type, context_window, max_output_tokens, support_streaming, support_function_calling, pricing_input, pricing_output, status, sort_order) VALUES
((SELECT id FROM mcs_llm_provider WHERE provider_code = 'hunyuan'), 'hunyuan-pro', '混元 Pro', 'hunyuan-pro', 'chat', 32768, 4096, 1, 1, 0.000015, 0.000015, 1, 100),
((SELECT id FROM mcs_llm_provider WHERE provider_code = 'hunyuan'), 'hunyuan-standard', '混元 Standard', 'hunyuan-standard', 'chat', 32768, 4096, 1, 1, 0.000006, 0.000006, 1, 99),
((SELECT id FROM mcs_llm_provider WHERE provider_code = 'hunyuan'), 'hunyuan-lite', '混元 Lite', 'hunyuan-lite', 'chat', 8192, 2048, 1, 1, 0.000003, 0.000003, 1, 98)
ON CONFLICT (provider_id, model_code) DO NOTHING;

-- 智谱 AI 模型
INSERT INTO mcs_llm_model_config (provider_id, model_code, model_name, model_id, model_type, context_window, max_output_tokens, support_streaming, support_function_calling, pricing_input, pricing_output, status, sort_order) VALUES
((SELECT id FROM mcs_llm_provider WHERE provider_code = 'zhipu'), 'glm-4-plus', 'GLM-4 Plus', 'glm-4-plus', 'chat', 128000, 4096, 1, 1, 0.000050, 0.000050, 1, 100),
((SELECT id FROM mcs_llm_provider WHERE provider_code = 'zhipu'), 'glm-4-air', 'GLM-4 Air', 'glm-4-air', 'chat', 128000, 4096, 1, 1, 0.000010, 0.000010, 1, 99),
((SELECT id FROM mcs_llm_provider WHERE provider_code = 'zhipu'), 'glm-4-flash', 'GLM-4 Flash', 'glm-4-flash', 'chat', 128000, 4096, 1, 1, 0.000001, 0.000001, 1, 98),
((SELECT id FROM mcs_llm_provider WHERE provider_code = 'zhipu'), 'glm-4v-plus', 'GLM-4V Plus', 'glm-4v-plus', 'vision', 8192, 1024, 1, 0, 0.000055, 0.000055, 1, 97)
ON CONFLICT (provider_id, model_code) DO NOTHING;

-- 百川智能模型
INSERT INTO mcs_llm_model_config (provider_id, model_code, model_name, model_id, model_type, context_window, max_output_tokens, support_streaming, support_function_calling, pricing_input, pricing_output, status, sort_order) VALUES
((SELECT id FROM mcs_llm_provider WHERE provider_code = 'baichuan'), 'baichuan4', 'Baichuan4', 'Baichuan4', 'chat', 128000, 4096, 1, 1, 0.000012, 0.000012, 1, 100),
((SELECT id FROM mcs_llm_provider WHERE provider_code = 'baichuan'), 'baichuan3-turbo', 'Baichuan3 Turbo', 'Baichuan3-Turbo', 'chat', 32000, 2048, 1, 1, 0.000005, 0.000005, 1, 99),
((SELECT id FROM mcs_llm_provider WHERE provider_code = 'baichuan'), 'baichuan2-turbo', 'Baichuan2 Turbo', 'Baichuan2-Turbo', 'chat', 32000, 2048, 1, 1, 0.000003, 0.000003, 1, 98)
ON CONFLICT (provider_id, model_code) DO NOTHING;

-- OpenAI 模型
INSERT INTO mcs_llm_model_config (provider_id, model_code, model_name, model_id, model_type, context_window, max_output_tokens, support_streaming, support_function_calling, pricing_input, pricing_output, status, sort_order) VALUES
((SELECT id FROM mcs_llm_provider WHERE provider_code = 'openai'), 'gpt-4o', 'GPT-4o', 'gpt-4o', 'chat', 128000, 4096, 1, 1, 0.000025, 0.000100, 1, 100),
((SELECT id FROM mcs_llm_provider WHERE provider_code = 'openai'), 'gpt-4o-mini', 'GPT-4o Mini', 'gpt-4o-mini', 'chat', 128000, 16384, 1, 1, 0.000001, 0.000002, 1, 99),
((SELECT id FROM mcs_llm_provider WHERE provider_code = 'openai'), 'gpt-4-turbo', 'GPT-4 Turbo', 'gpt-4-turbo', 'chat', 128000, 4096, 1, 1, 0.000010, 0.000030, 1, 98),
((SELECT id FROM mcs_llm_provider WHERE provider_code = 'openai'), 'o1-preview', 'o1-preview', 'o1-preview', 'chat', 128000, 32768, 1, 0, 0.000150, 0.000600, 1, 97)
ON CONFLICT (provider_id, model_code) DO NOTHING;

-- Anthropic 模型
INSERT INTO mcs_llm_model_config (provider_id, model_code, model_name, model_id, model_type, context_window, max_output_tokens, support_streaming, support_function_calling, pricing_input, pricing_output, status, sort_order) VALUES
((SELECT id FROM mcs_llm_provider WHERE provider_code = 'anthropic'), 'claude-sonnet-4', 'Claude Sonnet 4', 'claude-sonnet-4-20250514', 'chat', 200000, 8192, 1, 1, 0.000030, 0.000150, 1, 100),
((SELECT id FROM mcs_llm_provider WHERE provider_code = 'anthropic'), 'claude-haiku-4', 'Claude Haiku 4', 'claude-haiku-4-20250514', 'chat', 200000, 8192, 1, 1, 0.000001, 0.000005, 1, 99),
((SELECT id FROM mcs_llm_provider WHERE provider_code = 'anthropic'), 'claude-opus-4', 'Claude Opus 4', 'claude-opus-4-20250514', 'chat', 200000, 8192, 1, 1, 0.000150, 0.000750, 1, 98)
ON CONFLICT (provider_id, model_code) DO NOTHING;

-- OpenRouter 模型
INSERT INTO mcs_llm_model_config (provider_id, model_code, model_name, model_id, model_type, context_window, max_output_tokens, support_streaming, support_function_calling, pricing_input, pricing_output, status, sort_order) VALUES
((SELECT id FROM mcs_llm_provider WHERE provider_code = 'openrouter'), 'deepseek-r1', 'DeepSeek R1', 'deepseek/deepseek-r1', 'chat', 64000, 8192, 1, 1, 0.000014, 0.000028, 1, 100),
((SELECT id FROM mcs_llm_provider WHERE provider_code = 'openrouter'), 'qwen-qwq-32b', 'Qwen QwQ 32B', 'qwen/qwq-32b-preview', 'chat', 32768, 32768, 1, 1, 0.000001, 0.000001, 1, 99),
((SELECT id FROM mcs_llm_provider WHERE provider_code = 'openrouter'), 'google-gemini-2.5', 'Gemini 2.5 Pro', 'google/gemini-2.5-pro-exp-03-25', 'chat', 1000000, 8192, 1, 1, 0.000125, 0.000500, 1, 98)
ON CONFLICT (provider_id, model_code) DO NOTHING;

-- DeepSeek 模型
INSERT INTO mcs_llm_model_config (provider_id, model_code, model_name, model_id, model_type, context_window, max_output_tokens, support_streaming, support_function_calling, pricing_input, pricing_output, status, sort_order) VALUES
((SELECT id FROM mcs_llm_provider WHERE provider_code = 'deepseek'), 'deepseek-chat', 'DeepSeek Chat', 'deepseek-chat', 'chat', 64000, 8192, 1, 1, 0.000014, 0.000028, 1, 100),
((SELECT id FROM mcs_llm_provider WHERE provider_code = 'deepseek'), 'deepseek-reasoner', 'DeepSeek Reasoner', 'deepseek-reasoner', 'chat', 64000, 8192, 1, 1, 0.000055, 0.000219, 1, 99)
ON CONFLICT (provider_id, model_code) DO NOTHING;
