package com.mycoffeestore.entity.table;

import com.mycoffeestore.entity.LlmUserConfig;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

/**
 * LLM 用户配置表定义
 *
 * @author Backend Developer
 * @since 2026-03-07
 */
public class LlmUserConfigTableDef extends TableDef {

    public static final LlmUserConfigTableDef LLM_USER_CONFIG = new LlmUserConfigTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");
    public final QueryColumn USER_ID = new QueryColumn(this, "user_id");
    public final QueryColumn PROVIDER_ID = new QueryColumn(this, "provider_id");
    public final QueryColumn MODEL_CONFIG_ID = new QueryColumn(this, "model_config_id");
    public final QueryColumn ENABLED = new QueryColumn(this, "enabled");
    public final QueryColumn STATUS = new QueryColumn(this, "status");
    public final QueryColumn CREATED_AT = new QueryColumn(this, "created_at");
    public final QueryColumn UPDATED_AT = new QueryColumn(this, "updated_at");

    public LlmUserConfigTableDef() {
        super("", "mcs_llm_user_config");
    }
}
