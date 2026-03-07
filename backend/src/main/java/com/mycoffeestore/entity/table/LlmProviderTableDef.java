package com.mycoffeestore.entity.table;

import com.mycoffeestore.entity.LlmProvider;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

/**
 * LLM 提供商表定义
 *
 * @author Backend Developer
 * @since 2026-03-07
 */
public class LlmProviderTableDef extends TableDef {

    public static final LlmProviderTableDef LLM_PROVIDER = new LlmProviderTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");
    public final QueryColumn PROVIDER_CODE = new QueryColumn(this, "provider_code");
    public final QueryColumn PROVIDER_NAME = new QueryColumn(this, "provider_name");
    public final QueryColumn API_KEY = new QueryColumn(this, "api_key");
    public final QueryColumn BASE_URL = new QueryColumn(this, "base_url");
    public final QueryColumn ENABLED = new QueryColumn(this, "enabled");
    public final QueryColumn STATUS = new QueryColumn(this, "status");
    public final QueryColumn CREATED_AT = new QueryColumn(this, "created_at");
    public final QueryColumn UPDATED_AT = new QueryColumn(this, "updated_at");

    public LlmProviderTableDef() {
        super("", "mcs_llm_provider");
    }
}
