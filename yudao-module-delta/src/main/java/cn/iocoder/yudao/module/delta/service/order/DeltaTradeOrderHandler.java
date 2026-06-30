package cn.iocoder.yudao.module.delta.service.order;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.delta.dal.dataobject.config.DeltaProductServiceConfigDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaServiceOrderDO;
import cn.iocoder.yudao.module.delta.enums.order.ServiceOrderStatusEnum;
import cn.iocoder.yudao.module.delta.service.config.DeltaProductServiceConfigService;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import cn.iocoder.yudao.module.trade.service.order.handler.TradeOrderHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Delta 服务履约订单 - 支付后处理器
 * <p>
 * 商城订单支付成功后，识别属于 Delta 服务的订单项，按订单项创建服务履约订单。
 * 本 Handler 不侵入商城支付核心逻辑，通过 {@link TradeOrderHandler#afterPayOrder} 扩展点实现。
 * <p>
 * 匹配规则：SKU精确配置优先 → SPU通用配置 → 无配置则跳过
 * <p>
 * 幂等保证：数据库级 ({@code uk_tenant_trade_order_item}) + 业务级（先查已有再创建）
 *
 * @author Delta-Vanguard
 */
@Component
@Slf4j
public class DeltaTradeOrderHandler implements TradeOrderHandler {

    @Resource
    private DeltaProductServiceConfigService deltaProductServiceConfigService;

    @Resource
    private DeltaServiceOrderService deltaServiceOrderService;

    @Override
    public void afterPayOrder(TradeOrderDO order, List<TradeOrderItemDO> orderItems) {
        // 1. 获取当前租户
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            log.info("[afterPayOrder] 无租户上下文，跳过 Delta 履约单创建 (tradeOrderId={})", order.getId());
            return;
        }

        if (CollUtil.isEmpty(orderItems)) {
            return;
        }

        try {
            // 2. 收集所有 SKU ID 和 SPU ID（去重）
            Set<Long> skuIds = orderItems.stream()
                    .map(TradeOrderItemDO::getSkuId).collect(Collectors.toSet());
            Set<Long> spuIds = orderItems.stream()
                    .map(TradeOrderItemDO::getSpuId).collect(Collectors.toSet());

            // 3. 批量查询有效的 SKU 配置和 SPU 配置
            List<DeltaProductServiceConfigDO> skuConfigs =
                    deltaProductServiceConfigService.getEnabledConfigsBySkuIds(skuIds);
            List<DeltaProductServiceConfigDO> spuConfigs =
                    deltaProductServiceConfigService.getEnabledConfigsBySpuIds(spuIds);
            // SPU配置补充（只查那些没有SKU配置的SPU）
            Set<Long> spuAlreadyMatched = skuConfigs.stream()
                    .map(DeltaProductServiceConfigDO::getSpuId).collect(Collectors.toSet());
            Set<Long> unmatchedSpuIds = spuIds.stream()
                    .filter(spuId -> !spuAlreadyMatched.contains(spuId)).collect(Collectors.toSet());
            if (!unmatchedSpuIds.isEmpty()) {
                spuConfigs.addAll(deltaProductServiceConfigService.getEnabledConfigsBySpuIds(unmatchedSpuIds));
            }

            // 4. 构建匹配映射：orderItemId → config（SKU优先）
            Map<Long, DeltaProductServiceConfigDO> skuConfigMap = skuConfigs.stream()
                    .collect(Collectors.toMap(DeltaProductServiceConfigDO::getSkuId, c -> c, (a, b) -> a));
            Map<Long, DeltaProductServiceConfigDO> spuConfigMap = spuConfigs.stream()
                    .collect(Collectors.toMap(DeltaProductServiceConfigDO::getSpuId, c -> c, (a, b) -> a));

            Map<TradeOrderItemDO, DeltaProductServiceConfigDO> matchedItems = new LinkedHashMap<>();
            List<TradeOrderItemDO> unmatchedItems = new ArrayList<>();
            for (TradeOrderItemDO item : orderItems) {
                // SKU精确匹配优先
                DeltaProductServiceConfigDO config = skuConfigMap.get(item.getSkuId());
                if (config == null) {
                    // SPU通用匹配兜底
                    config = spuConfigMap.get(item.getSpuId());
                }
                if (config != null) {
                    matchedItems.put(item, config);
                } else {
                    unmatchedItems.add(item);
                }
            }

            if (unmatchedItems.size() > 0) {
                log.info("[afterPayOrder] {} 个订单项无 Delta 服务配置，跳过 (tradeOrderId={})",
                        unmatchedItems.size(), order.getId());
            }
            if (matchedItems.isEmpty()) {
                return;
            }

            // 5. 批量查询已有的履约订单（幂等防重）
            Set<Long> matchedItemIds = matchedItems.keySet().stream()
                    .map(TradeOrderItemDO::getId).collect(Collectors.toSet());
            List<DeltaServiceOrderDO> existingOrders =
                    deltaServiceOrderService.getServiceOrdersByTradeOrderItemIds(matchedItemIds);
            Set<Long> existingItemIds = existingOrders.stream()
                    .map(DeltaServiceOrderDO::getTradeOrderItemId).collect(Collectors.toSet());

            // 6. 构建需要创建的履约订单
            List<DeltaServiceOrderDO> toCreate = new ArrayList<>();
            for (Map.Entry<TradeOrderItemDO, DeltaProductServiceConfigDO> entry : matchedItems.entrySet()) {
                TradeOrderItemDO item = entry.getKey();
                DeltaProductServiceConfigDO config = entry.getValue();

                // 已存在则跳过
                if (existingItemIds.contains(item.getId())) {
                    log.debug("[afterPayOrder] 履约订单已存在 (tradeOrderItemId={})，幂等跳过", item.getId());
                    continue;
                }

                DeltaServiceOrderDO serviceOrder = new DeltaServiceOrderDO();
                // 商城订单关联
                serviceOrder.setTradeOrderId(order.getId());
                serviceOrder.setTradeOrderNo(order.getNo());
                serviceOrder.setTradeOrderItemId(item.getId());
                // 会员
                serviceOrder.setBuyerUserId(order.getUserId());
                // 商品快照
                serviceOrder.setSpuId(item.getSpuId());
                serviceOrder.setSkuId(item.getSkuId());
                serviceOrder.setProductName(item.getSpuName());
                serviceOrder.setSkuName(JSONUtil.toJsonStr(item.getProperties()));
                serviceOrder.setProductPicUrl(item.getPicUrl());
                serviceOrder.setCount(item.getCount());
                serviceOrder.setServiceAmount(item.getPayPrice());
                // 服务配置快照
                serviceOrder.setServiceType(config.getServiceType());
                serviceOrder.setDeviceType(config.getDeviceType());
                serviceOrder.setDispatchMode(config.getDefaultDispatchMode() != null
                        ? config.getDefaultDispatchMode() : 2);
                serviceOrder.setCommissionRate(config.getCommissionRate());
                // 初始状态
                serviceOrder.setStatus(ServiceOrderStatusEnum.PENDING_DISPATCH.getStatus());
                serviceOrder.setVersion(0);

                toCreate.add(serviceOrder);
            }

            if (toCreate.isEmpty()) {
                log.info("[afterPayOrder] 所有 Delta 订单项已有履约订单 (tradeOrderId={})，跳过", order.getId());
                return;
            }

            // 7. 批量创建
            List<DeltaServiceOrderDO> created = deltaServiceOrderService.batchCreateServiceOrders(toCreate);
            log.info("[afterPayOrder] Delta 履约订单创建完成 (tradeOrderId={}, created={}, skipped={})",
                    order.getId(), created.size(), existingItemIds.size());

        } catch (Exception e) {
            log.error("[afterPayOrder] 创建 Delta 履约订单失败 (tradeOrderId={})", order.getId(), e);
            throw e;
        }
    }
}
