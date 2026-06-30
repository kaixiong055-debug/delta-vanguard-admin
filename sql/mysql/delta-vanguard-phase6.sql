-- Delta Vanguard Phase 6: 验收与返工闭环
-- 新增表：验收记录、返工记录

-- 验收记录表
CREATE TABLE IF NOT EXISTS `delta_order_acceptance` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `service_order_id` bigint NOT NULL COMMENT '服务订单ID',
    `worker_id` bigint DEFAULT NULL COMMENT '打手ID',
    `acceptance_result` tinyint NOT NULL DEFAULT '1' COMMENT '验收结果：1-验收通过 2-要求返工',
    `operator_type` tinyint NOT NULL COMMENT '操作人类型：1-客户 2-打手 3-客服 4-系统',
    `operator_id` bigint DEFAULT NULL COMMENT '操作人ID',
    `remark` varchar(512) DEFAULT NULL COMMENT '备注',
    `before_status` int DEFAULT NULL COMMENT '操作前状态',
    `after_status` int DEFAULT NULL COMMENT '操作后状态',
    `acceptance_time` datetime DEFAULT NULL COMMENT '验收时间',
    `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户ID',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    KEY `idx_service_order_id` (`service_order_id`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='验收记录表';

-- 返工记录表
CREATE TABLE IF NOT EXISTS `delta_order_rework` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `service_order_id` bigint NOT NULL COMMENT '服务订单ID',
    `worker_id` bigint DEFAULT NULL COMMENT '打手ID',
    `rework_no` int NOT NULL DEFAULT '1' COMMENT '返工序号（第几次返工）',
    `reason` varchar(512) NOT NULL COMMENT '返工原因',
    `operator_type` tinyint NOT NULL COMMENT '操作人类型：1-客户 2-打手 3-客服 4-系统',
    `operator_id` bigint DEFAULT NULL COMMENT '操作人ID',
    `before_status` int DEFAULT NULL COMMENT '操作前状态',
    `after_status` int DEFAULT NULL COMMENT '操作后状态',
    `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户ID',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    KEY `idx_service_order_id` (`service_order_id`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='返工记录表';
