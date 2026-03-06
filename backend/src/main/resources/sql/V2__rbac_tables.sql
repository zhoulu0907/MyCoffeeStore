-- RBAC 权限管理表结构
-- 创建时间：2026-03-06

-- ========================================
-- 1. 角色表
-- ========================================
CREATE TABLE IF NOT EXISTS mcs_role (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(32) NOT NULL,
    name VARCHAR(64) NOT NULL,
    description VARCHAR(255) DEFAULT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_role_code UNIQUE (code)
);

-- ========================================
-- 2. 权限表
-- ========================================
CREATE TABLE IF NOT EXISTS mcs_permission (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(64) NOT NULL,
    name VARCHAR(64) NOT NULL,
    description VARCHAR(255) DEFAULT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_permission_code UNIQUE (code)
);

-- ========================================
-- 3. 角色-权限关联表
-- ========================================
CREATE TABLE IF NOT EXISTS mcs_role_permission (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id)
);

-- ========================================
-- 4. 用户-角色关联表
-- ========================================
CREATE TABLE IF NOT EXISTS mcs_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id)
);

-- ========================================
-- 初始化角色数据
-- ========================================
INSERT INTO mcs_role (code, name, description) VALUES
    ('admin', '管理员', '系统管理员，拥有所有权限'),
    ('staff', '店员', '咖啡店员工，可管理订单'),
    ('user', '普通用户', '普通注册用户')
ON CONFLICT (code) DO NOTHING;

-- ========================================
-- 初始化权限数据
-- ========================================
INSERT INTO mcs_permission (code, name, description) VALUES
    ('order:create', '创建订单', '创建新订单'),
    ('order:view', '查看订单', '查看自己的订单'),
    ('order:cancel', '取消订单', '取消自己的订单'),
    ('order:update_status', '更新订单状态', '更新任意订单的状态'),
    ('order:view_all', '查看所有订单', '查看所有用户的订单'),
    ('llm:config', 'LLM配置管理', '管理LLM提供商和模型配置'),
    ('system:data_gen', '数据生成', '生成测试数据'),
    ('role:manage', '角色管理', '管理用户角色和权限')
ON CONFLICT (code) DO NOTHING;

-- ========================================
-- 初始化角色-权限关联
-- ========================================
-- admin 拥有所有权限
INSERT INTO mcs_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM mcs_role r, mcs_permission p
WHERE r.code = 'admin'
ON CONFLICT DO NOTHING;

-- staff 拥有订单管理相关权限
INSERT INTO mcs_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM mcs_role r, mcs_permission p
WHERE r.code = 'staff' AND p.code IN ('order:create', 'order:view', 'order:cancel', 'order:update_status', 'order:view_all')
ON CONFLICT DO NOTHING;

-- user 拥有基本权限
INSERT INTO mcs_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM mcs_role r, mcs_permission p
WHERE r.code = 'user' AND p.code IN ('order:create', 'order:view', 'order:cancel')
ON CONFLICT DO NOTHING;

-- ========================================
-- 初始化用户角色（给已有用户分配角色）
-- ========================================
-- admin 用户分配 admin 角色
INSERT INTO mcs_user_role (user_id, role_id)
SELECT u.id, r.id FROM mcs_user u, mcs_role r
WHERE u.username = 'admin' AND r.code = 'admin'
ON CONFLICT DO NOTHING;

-- test_user 分配 user 角色
INSERT INTO mcs_user_role (user_id, role_id)
SELECT u.id, r.id FROM mcs_user u, mcs_role r
WHERE u.username = 'test_user' AND r.code = 'user'
ON CONFLICT DO NOTHING;
