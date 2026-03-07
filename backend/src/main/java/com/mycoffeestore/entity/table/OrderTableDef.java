package com.mycoffeestore.entity.table;

import com.mycoffeestore.entity.Order;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

/**
 * 订单表定义
 *
 * @author Backend Developer
 * @since 2026-03-07
 */
public class OrderTableDef extends TableDef {

    public static final OrderTableDef ORDER = new OrderTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");
    public final QueryColumn USER_ID = new QueryColumn(this, "user_id");
    public final QueryColumn ORDER_TYPE = new QueryColumn(this, "order_type");
    public final QueryColumn STATUS = new QueryColumn(this, "status");
    public final QueryColumn TOTAL_AMOUNT = new QueryColumn(this, "total_amount");
    public final QueryColumn CREATED_AT = new QueryColumn(this, "created_at");
    public final QueryColumn UPDATED_AT = new QueryColumn(this, "updated_at");

    public OrderTableDef() {
        super("", "mcs_order");
    }
}
