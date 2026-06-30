-- =====================================================
-- Delta 先锋俱乐部 第四阶段增量SQL
-- 索引优化 + 分配状态枚举扩展
-- =====================================================

-- 1. delta_service_order 优化索引（支持订单池、打手订单查询）
CREATE INDEX IF NOT EXISTS idx_tenant_status_worker
    ON delta_service_order (tenant_id, status, assigned_worker_id);

CREATE INDEX IF NOT EXISTS idx_status_worker_id
    ON delta_service_order (status, assigned_worker_id);

-- 2. delta_order_assignment 索引（支持有效分配查询）
CREATE INDEX IF NOT EXISTS idx_tenant_service_order_status
    ON delta_order_assignment (tenant_id, service_order_id, assignment_status);

CREATE INDEX IF NOT EXISTS idx_tenant_worker_status
    ON delta_order_assignment (tenant_id, worker_id, assignment_status);

-- 3. delta_worker 索引（支持高效校验）
CREATE INDEX IF NOT EXISTS idx_tenant_user_id
    ON delta_worker (tenant_id, user_id);

CREATE INDEX IF NOT EXISTS idx_status_work_status
    ON delta_worker (status, work_status);

-- 4. delta_worker_skill 索引（支持技能匹配）
CREATE INDEX IF NOT EXISTS idx_worker_device_service_status
    ON delta_worker_skill (worker_id, device_type, service_type, status);

-- 5. delta_order_log 索引（支持日志查询）
CREATE INDEX IF NOT EXISTS idx_service_order_id
    ON delta_order_log (service_order_id);
