-- MyCoffeeStore 数据库初始化脚本 (PostgreSQL)
-- 创建时间：2026-02-26

-- 用户表
CREATE TABLE IF NOT EXISTS mcs_user (
  id BIGSERIAL PRIMARY KEY,
  username VARCHAR(32) NOT NULL,
  password VARCHAR(128) NOT NULL,
  email VARCHAR(64) NOT NULL,
  phone VARCHAR(20) DEFAULT NULL,
  avatar VARCHAR(255) DEFAULT NULL,
  status SMALLINT NOT NULL DEFAULT 1,
  balance DECIMAL(10,2) NOT NULL DEFAULT 500.00,
  last_login_at TIMESTAMP DEFAULT NULL,
  last_login_ip VARCHAR(64) DEFAULT NULL,
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted SMALLINT NOT NULL DEFAULT 0,
  CONSTRAINT uk_username UNIQUE (username),
  CONSTRAINT uk_email UNIQUE (email)
);

CREATE INDEX IF NOT EXISTS idx_user_phone ON mcs_user(phone);
CREATE INDEX IF NOT EXISTS idx_user_create_time ON mcs_user(create_time);

-- 咖啡产品表
CREATE TABLE IF NOT EXISTS mcs_coffee (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(64) NOT NULL,
  description TEXT,
  price DECIMAL(10,2) NOT NULL,
  original_price DECIMAL(10,2) DEFAULT NULL,
  category VARCHAR(32) NOT NULL,
  category_name VARCHAR(32) NOT NULL,
  image_url VARCHAR(255) NOT NULL,
  images JSONB DEFAULT NULL,
  stock INTEGER NOT NULL DEFAULT 0,
  sales INTEGER NOT NULL DEFAULT 0,
  status SMALLINT NOT NULL DEFAULT 1,
  sort_order INTEGER NOT NULL DEFAULT 0,
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted SMALLINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_coffee_category ON mcs_coffee(category);
CREATE INDEX IF NOT EXISTS idx_coffee_status ON mcs_coffee(status);
CREATE INDEX IF NOT EXISTS idx_coffee_sort_order ON mcs_coffee(sort_order DESC);
CREATE INDEX IF NOT EXISTS idx_coffee_create_time ON mcs_coffee(create_time);

-- 购物车表
CREATE TABLE IF NOT EXISTS mcs_cart (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  coffee_id BIGINT NOT NULL,
  quantity INTEGER NOT NULL DEFAULT 1,
  price DECIMAL(10,2) NOT NULL,
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted SMALLINT NOT NULL DEFAULT 0,
  CONSTRAINT uk_user_coffee UNIQUE (user_id, coffee_id)
);

CREATE INDEX IF NOT EXISTS idx_cart_user_id ON mcs_cart(user_id);

-- 订单表
CREATE TABLE IF NOT EXISTS mcs_order (
  id BIGSERIAL PRIMARY KEY,
  order_no VARCHAR(32) NOT NULL,
  user_id BIGINT NOT NULL,
  total_amount DECIMAL(10,2) NOT NULL,
  order_type VARCHAR(20) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'pending',
  remark VARCHAR(500) DEFAULT NULL,
  delivery_address JSONB DEFAULT NULL,
  cancel_reason VARCHAR(255) DEFAULT NULL,
  paid_at TIMESTAMP DEFAULT NULL,
  completed_at TIMESTAMP DEFAULT NULL,
  cancelled_at TIMESTAMP DEFAULT NULL,
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted SMALLINT NOT NULL DEFAULT 0,
  CONSTRAINT uk_order_no UNIQUE (order_no)
);

CREATE INDEX IF NOT EXISTS idx_order_user_id ON mcs_order(user_id);
CREATE INDEX IF NOT EXISTS idx_order_status ON mcs_order(status);
CREATE INDEX IF NOT EXISTS idx_order_create_time ON mcs_order(create_time);

-- 订单详情表
CREATE TABLE IF NOT EXISTS mcs_order_item (
  id BIGSERIAL PRIMARY KEY,
  order_id BIGINT NOT NULL,
  coffee_id BIGINT NOT NULL,
  coffee_name VARCHAR(64) NOT NULL,
  image_url VARCHAR(255) NOT NULL,
  quantity INTEGER NOT NULL,
  price DECIMAL(10,2) NOT NULL,
  subtotal DECIMAL(10,2) NOT NULL,
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted SMALLINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_order_item_order_id ON mcs_order_item(order_id);
CREATE INDEX IF NOT EXISTS idx_order_item_coffee_id ON mcs_order_item(coffee_id);

-- ========================================
-- 插入测试数据
-- ========================================

-- 测试用户（密码均为: Coffee123!，BCrypt加密后）
INSERT INTO mcs_user (username, password, email, phone) VALUES
('admin', '$2b$10$.t7uDrC1gkBp9an6b5qqK.yi2LFRSRKkoXT4EJghCo8xdLvfSOAXS', 'admin@mycoffeestore.com', '14155550001'),
('test_user', '$2b$10$.t7uDrC1gkBp9an6b5qqK.yi2LFRSRKkoXT4EJghCo8xdLvfSOAXS', 'test@example.com', '14155550002')
ON CONFLICT (username) DO NOTHING;

-- 咖啡产品数据
INSERT INTO mcs_coffee (name, description, price, original_price, category, category_name, image_url, images, stock, sales, status, sort_order) VALUES
-- 意式浓缩系列 (8款)
('经典美式', '精选阿拉比卡豆，深度烘焙，口感醇厚', 4.50, 5.50, 'espresso', '意式浓缩系列', 'https://images.unsplash.com/photo-1521302080334-4bebac2763a6?w=400', '["https://images.unsplash.com/photo-1521302080334-4bebac2763a6?w=800", "https://images.unsplash.com/photo-1551030173-122aabc4489c?w=800"]', 100, 1250, 1, 100),
('卡布奇诺', '浓缩咖啡与丝滑奶泡的完美融合', 5.50, 6.50, 'espresso', '意式浓缩系列', 'https://images.unsplash.com/photo-1572442388796-11668a67e53d?w=400', NULL, 80, 980, 1, 99),
('拿铁', '香浓浓缩咖啡配顺滑牛奶', 5.00, 6.00, 'espresso', '意式浓缩系列', 'https://images.unsplash.com/photo-1561882468-9110e03e0f78?w=400', NULL, 90, 1100, 1, 98),
('焦糖玛奇朵', '香草糖浆与焦糖酱的甜蜜邂逅', 6.00, 7.00, 'espresso', '意式浓缩系列', 'https://images.unsplash.com/photo-1485808191679-5f86510681a2?w=400', NULL, 70, 750, 1, 97),
('摩卡', '浓缩咖啡与巧克力酱的浓郁组合', 6.50, 7.50, 'espresso', '意式浓缩系列', 'https://images.unsplash.com/photo-1578314675249-a6910f80cc4e?w=400', NULL, 60, 620, 1, 96),
('浓缩意式', '双份浓缩，浓烈醇厚，真正咖啡爱好者的选择', 4.00, 5.00, 'espresso', '意式浓缩系列', 'https://images.unsplash.com/photo-1510707577719-ae7c14805e3a?w=400', '["https://images.unsplash.com/photo-1510707577719-ae7c14805e3a?w=800", "https://images.unsplash.com/photo-1514432324607-a09d9b4aefda?w=800"]', 85, 430, 1, 95),
('香草拿铁', '马达加斯加香草糖浆与丝滑牛奶的温柔邂逅', 6.00, 7.00, 'espresso', '意式浓缩系列', 'https://images.unsplash.com/photo-1572490122747-3968b75cc699?w=400', '["https://images.unsplash.com/photo-1572490122747-3968b75cc699?w=800"]', 65, 560, 1, 94),
('榛果拿铁', '意大利榛果风味，甜香与咖啡的完美平衡', 6.00, 7.00, 'espresso', '意式浓缩系列', 'https://images.unsplash.com/photo-1534687941688-651ccaafbff8?w=400', '["https://images.unsplash.com/photo-1534687941688-651ccaafbff8?w=800"]', 70, 480, 1, 93),
-- 手冲系列 (7款)
('耶加雪菲', '埃塞俄比亚产区，花果香气突出', 6.00, NULL, 'brew', '手冲系列', 'https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=400', NULL, 50, 320, 1, 92),
('哥斯达黎加', '平衡酸甜，巧克力尾韵', 5.50, NULL, 'brew', '手冲系列', 'https://images.unsplash.com/photo-1511920170033-f8396924c348?w=400', NULL, 45, 280, 1, 91),
('肯尼亚AA', '明亮果酸，莓果风味', 6.50, NULL, 'brew', '手冲系列', 'https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=400', NULL, 40, 250, 1, 90),
('巴拿马瑰夏', '翡翠庄园传奇品种，茉莉花香与热带水果风味', 12.00, NULL, 'brew', '手冲系列', 'https://images.unsplash.com/photo-1442512595331-e89e73853f31?w=400', '["https://images.unsplash.com/photo-1442512595331-e89e73853f31?w=800", "https://images.unsplash.com/photo-1507133750040-4a8f57021571?w=800"]', 20, 150, 1, 89),
('曼特宁', '苏门答腊湿刨法处理，低酸醇厚，草本与焦糖余韵', 5.50, NULL, 'brew', '手冲系列', 'https://images.unsplash.com/photo-1507133750040-4a8f57021571?w=400', '["https://images.unsplash.com/photo-1507133750040-4a8f57021571?w=800"]', 55, 310, 1, 88),
('哥伦比亚', '惠兰产区水洗处理，坚果与焦糖的经典风味', 5.00, NULL, 'brew', '手冲系列', 'https://images.unsplash.com/photo-1501747315-124a0eaca060?w=400', '["https://images.unsplash.com/photo-1501747315-124a0eaca060?w=800"]', 50, 270, 1, 87),
('危地马拉安提瓜', '火山灰土壤培育，烟熏巧克力与香料气息', 6.50, NULL, 'brew', '手冲系列', 'https://images.unsplash.com/photo-1498804103079-a6351b050096?w=400', '["https://images.unsplash.com/photo-1498804103079-a6351b050096?w=800"]', 35, 190, 1, 86),
-- 冷萃/冰咖啡 (7款)
('冷萃咖啡', '12小时低温萃取，口感顺滑', 5.50, 6.50, 'cold', '冷萃/冰咖啡', 'https://images.unsplash.com/photo-1517701550927-30cf4ba1dba5?w=400', NULL, 30, 450, 1, 85),
('冰美式', '经典美式加冰，清爽提神', 4.50, 5.50, 'cold', '冷萃/冰咖啡', 'https://images.unsplash.com/photo-1461023058943-07fcbe16d735?w=400', NULL, 80, 890, 1, 84),
('氮气冷萃', '注入氮气，如丝般顺滑口感', 7.00, 8.00, 'cold', '冷萃/冰咖啡', 'https://images.unsplash.com/photo-1592663527359-cf6642f54cff?w=400', NULL, 25, 180, 1, 83),
('冰拿铁', '冰镇牛奶与浓缩咖啡的清凉碰撞', 5.50, 6.50, 'cold', '冷萃/冰咖啡', 'https://images.unsplash.com/photo-1517701550927-30cf4ba1dba5?w=400', '["https://images.unsplash.com/photo-1517701550927-30cf4ba1dba5?w=800"]', 75, 780, 1, 82),
('冷萃燕麦拿铁', '12小时冷萃搭配瑞典燕麦奶，植物基健康之选', 7.00, 8.00, 'cold', '冷萃/冰咖啡', 'https://images.unsplash.com/photo-1553909489-cd47e0907980?w=400', '["https://images.unsplash.com/photo-1553909489-cd47e0907980?w=800"]', 40, 350, 1, 81),
('冰摩卡', '巧克力酱与冰咖啡的夏日甜蜜', 6.50, 7.50, 'cold', '冷萃/冰咖啡', 'https://images.unsplash.com/photo-1462917882517-e150b5a7413d?w=400', '["https://images.unsplash.com/photo-1462917882517-e150b5a7413d?w=800"]', 55, 420, 1, 80),
('冰焦糖玛奇朵', '焦糖淋面冰拿铁，层次分明的视觉与味觉享受', 6.50, 7.50, 'cold', '冷萃/冰咖啡', 'https://images.unsplash.com/photo-1547721064-da6cfb341d50?w=400', '["https://images.unsplash.com/photo-1547721064-da6cfb341d50?w=800"]', 50, 380, 1, 79),
-- 拼配系列 (7款)
('经典拼配', '巴西与哥伦比亚豆的经典组合', 5.00, NULL, 'blend', '拼配系列', 'https://images.unsplash.com/photo-1497935586351-b67a49e012bf?w=400', NULL, 60, 520, 1, 78),
('季节限定', '当季精选产区拼配', 7.50, 8.50, 'blend', '拼配系列', 'https://images.unsplash.com/photo-1504630083234-14187a9df0f5?w=400', NULL, 35, 150, 1, 77),
('深度烘焙拼配', '印尼与巴西深烘豆，浓郁焦糖与可可风味', 5.50, NULL, 'blend', '拼配系列', 'https://images.unsplash.com/photo-1559056199-641a0ac8b55e?w=400', '["https://images.unsplash.com/photo-1559056199-641a0ac8b55e?w=800"]', 55, 410, 1, 76),
('早安拼配', '中浅烘焙，柑橘与蜂蜜的清新晨间风味', 5.00, NULL, 'blend', '拼配系列', 'https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=400', '["https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=800"]', 65, 380, 1, 75),
('意式拼配', '专为意式萃取设计，油脂丰富，坚果巧克力基调', 5.50, 6.50, 'blend', '拼配系列', 'https://images.unsplash.com/photo-1514432324607-a09d9b4aefda?w=400', '["https://images.unsplash.com/photo-1514432324607-a09d9b4aefda?w=800"]', 50, 340, 1, 74),
('单一产地拼配', '同一产区不同批次精选拼配，突出产地特色', 6.00, NULL, 'blend', '拼配系列', 'https://images.unsplash.com/photo-1587734195503-904fca47e0e9?w=400', '["https://images.unsplash.com/photo-1587734195503-904fca47e0e9?w=800"]', 40, 220, 1, 73),
('日落拼配', '中度烘焙，蓝莓与黑巧克力的层次感', 6.50, 7.50, 'blend', '拼配系列', 'https://images.unsplash.com/photo-1442512595331-e89e73853f31?w=400', '["https://images.unsplash.com/photo-1442512595331-e89e73853f31?w=800"]', 30, 180, 1, 72)
ON CONFLICT DO NOTHING;
