package com.mycoffeestore.entity.table;

import com.mycoffeestore.entity.ConversationEntity;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

/**
 * 对话记忆表定义
 * <p>
 * 提供 MyBatis-Flex 查询构建器的表字段定义
 *
 * @author Backend Developer
 * @since 2026-03-07
 */
public class ConversationEntityTableDef extends TableDef {

    /**
     * 对话记忆表实例
     */
    public static final ConversationEntityTableDef CONVERSATION_ENTITY = new ConversationEntityTableDef();

    /**
     * 主键ID
     */
    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * 用户ID
     */
    public final QueryColumn USER_ID = new QueryColumn(this, "user_id");

    /**
     * 会话ID
     */
    public final QueryColumn SESSION_ID = new QueryColumn(this, "session_id");

    /**
     * Agent类型
     */
    public final QueryColumn AGENT_ID = new QueryColumn(this, "agent_id");

    /**
     * 消息ID
     */
    public final QueryColumn MESSAGE_ID = new QueryColumn(this, "message_id");

    /**
     * 消息角色
     */
    public final QueryColumn ROLE = new QueryColumn(this, "role");

    /**
     * 消息内容
     */
    public final QueryColumn CONTENT = new QueryColumn(this, "content");

    /**
     * 元数据
     */
    public final QueryColumn METADATA = new QueryColumn(this, "metadata");

    /**
     * 消息创建时间
     */
    public final QueryColumn CREATED_AT = new QueryColumn(this, "created_at");

    /**
     * 更新时间
     */
    public final QueryColumn UPDATED_AT = new QueryColumn(this, "updated_at");

    /**
     * 构造函数
     */
    public ConversationEntityTableDef() {
        super("", "mcs_agent_conversation");
    }
}
