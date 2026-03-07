package com.mycoffeestore.entity.table;

import com.mycoffeestore.entity.LlmModelConfig;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

/**
 * LLM 模型配置表定义
 *
 * @author Backend Developer
 * @since 2026-03-07
 */
public class LlmModelConfigTableDef extends TableDef {

    public static final LlmModelConfigTableDef LLM_MODEL_CONFIG = new LlmModelConfigTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");
    public final QueryColumn PROVIDER_ID = new QueryColumn(this, "provider_id");
    public final QueryColumn MODEL_CODE = new QueryColumn(this, "model_code");
    public final QueryColumn MODEL_NAME = new QueryColumn(this, "model_name");
    public final QueryColumn MAX_TOKENS = new QueryColumn(this, "max_tokens");
    public final QueryColumn TEMPERATURE = new QueryColumn(this, "temperature");
    public final QueryColumn ENABLED = new QueryColumn(this, "enabled");
    public final QueryColumn STATUS = new QueryColumn(this, "status");
    public final QueryColumn CREATED_AT = new QueryColumn(this, "created_at");
    public final QueryColumn UPDATED_AT = new QueryColumn(this, "updated_at");

    public LlmModelConfigTableDef() {
        super("", "mcs_llm_model_config");
    }
}
