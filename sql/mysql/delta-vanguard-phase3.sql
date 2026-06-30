-- =====================================================
-- Delta 先锋俱乐部 - 第三阶段增量 SQL
-- 为 delta_service_order 表新增字段
-- =====================================================

-- 新增商品图片快照字段
ALTER TABLE delta_service_order
    ADD COLUMN product_pic_url varchar(500) NULL DEFAULT '' COMMENT '商品图片（快照）'
    AFTER sku_name;

-- 新增购买数量快照字段
ALTER TABLE delta_service_order
    ADD COLUMN count int NOT NULL DEFAULT 1 COMMENT '购买数量（快照）'
    AFTER product_pic_url;

-- =====================================================
-- 第三阶段权限菜单 SQL（如果需要在数据库添加权限）
-- =====================================================

-- 商品服务配置权限
-- delta:product-service-config:query
-- delta:product-service-config:create
-- delta:product-service-config:update
-- delta:product-service-config:delete

-- 服务履约订单权限
-- delta:service-order:query
