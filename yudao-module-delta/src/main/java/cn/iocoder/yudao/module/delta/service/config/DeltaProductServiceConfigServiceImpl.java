package cn.iocoder.yudao.module.delta.service.config;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.delta.controller.admin.config.vo.DeltaProductServiceConfigPageReqVO;
import cn.iocoder.yudao.module.delta.controller.admin.config.vo.DeltaProductServiceConfigSaveReqVO;
import cn.iocoder.yudao.module.delta.dal.dataobject.config.DeltaProductServiceConfigDO;
import cn.iocoder.yudao.module.delta.dal.mysql.config.DeltaProductServiceConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.delta.enums.ErrorCodeConstants.*;

/**
 * 商品服务配置 Service 实现
 *
 * @author Delta-Vanguard
 */
@Service
@Validated
@Slf4j
public class DeltaProductServiceConfigServiceImpl implements DeltaProductServiceConfigService {

    @Resource
    private DeltaProductServiceConfigMapper deltaProductServiceConfigMapper;

    @Override
    public Long createConfig(DeltaProductServiceConfigSaveReqVO createReqVO) {
        // 校验唯一性：同一租户下 (spuId, skuId) 组合不可重复
        validateUnique(createReqVO.getSpuId(), createReqVO.getSkuId(), null);
        DeltaProductServiceConfigDO config = new DeltaProductServiceConfigDO();
        config.setSpuId(createReqVO.getSpuId());
        config.setSkuId(createReqVO.getSkuId());
        config.setServiceType(createReqVO.getServiceType());
        config.setDeviceType(createReqVO.getDeviceType());
        config.setRequiredWorkerLevel(createReqVO.getRequiredWorkerLevel());
        config.setAllowDesignatedWorker(createReqVO.getAllowDesignatedWorker());
        config.setAllowPublicClaim(createReqVO.getAllowPublicClaim());
        config.setDefaultDispatchMode(createReqVO.getDefaultDispatchMode());
        config.setMaxServiceHours(createReqVO.getMaxServiceHours());
        config.setCommissionRate(createReqVO.getCommissionRate());
        config.setEnabled(createReqVO.getEnabled() != null ? createReqVO.getEnabled() : Boolean.TRUE);
        deltaProductServiceConfigMapper.insert(config);
        return config.getId();
    }

    @Override
    public void updateConfig(DeltaProductServiceConfigSaveReqVO updateReqVO) {
        DeltaProductServiceConfigDO existConfig = validateExists(updateReqVO.getId());
        // 校验唯一性（排除自身）
        validateUnique(updateReqVO.getSpuId(), updateReqVO.getSkuId(), updateReqVO.getId());
        existConfig.setSpuId(updateReqVO.getSpuId());
        existConfig.setSkuId(updateReqVO.getSkuId());
        existConfig.setServiceType(updateReqVO.getServiceType());
        existConfig.setDeviceType(updateReqVO.getDeviceType());
        existConfig.setRequiredWorkerLevel(updateReqVO.getRequiredWorkerLevel());
        existConfig.setAllowDesignatedWorker(updateReqVO.getAllowDesignatedWorker());
        existConfig.setAllowPublicClaim(updateReqVO.getAllowPublicClaim());
        existConfig.setDefaultDispatchMode(updateReqVO.getDefaultDispatchMode());
        existConfig.setMaxServiceHours(updateReqVO.getMaxServiceHours());
        existConfig.setCommissionRate(updateReqVO.getCommissionRate());
        if (updateReqVO.getEnabled() != null) {
            existConfig.setEnabled(updateReqVO.getEnabled());
        }
        deltaProductServiceConfigMapper.updateById(existConfig);
    }

    @Override
    public void deleteConfig(Long id) {
        validateExists(id);
        deltaProductServiceConfigMapper.deleteById(id);
    }

    @Override
    public void updateConfigStatus(Long id, Boolean enabled) {
        DeltaProductServiceConfigDO config = validateExists(id);
        config.setEnabled(enabled);
        deltaProductServiceConfigMapper.updateById(config);
    }

    @Override
    public DeltaProductServiceConfigDO getConfig(Long id) {
        return validateExists(id);
    }

    @Override
    public PageResult<DeltaProductServiceConfigDO> getConfigPage(DeltaProductServiceConfigPageReqVO pageReqVO) {
        return deltaProductServiceConfigMapper.selectPage(pageReqVO);
    }

    @Override
    public DeltaProductServiceConfigDO getConfigBySkuId(Long skuId) {
        return deltaProductServiceConfigMapper.selectBySkuId(skuId);
    }

    @Override
    public DeltaProductServiceConfigDO validateConfigEnabled(Long skuId) {
        DeltaProductServiceConfigDO config = deltaProductServiceConfigMapper.selectBySkuId(skuId);
        if (config == null) {
            throw exception(PRODUCT_SERVICE_CONFIG_NOT_EXISTS);
        }
        if (!Boolean.TRUE.equals(config.getEnabled())) {
            throw exception(PRODUCT_SERVICE_CONFIG_DISABLED);
        }
        return config;
    }

    @Override
    public List<DeltaProductServiceConfigDO> getEnabledConfigsBySkuIds(Collection<Long> skuIds) {
        if (skuIds == null || skuIds.isEmpty()) return Collections.emptyList();
        List<DeltaProductServiceConfigDO> all = deltaProductServiceConfigMapper.selectListBySkuIds(skuIds);
        return all.stream().filter(c -> Boolean.TRUE.equals(c.getEnabled())).collect(Collectors.toList());
    }

    @Override
    public List<DeltaProductServiceConfigDO> getEnabledConfigsBySpuIds(Collection<Long> spuIds) {
        if (spuIds == null || spuIds.isEmpty()) return Collections.emptyList();
        List<DeltaProductServiceConfigDO> all = deltaProductServiceConfigMapper.selectListBySpuIds(spuIds);
        return all.stream().filter(c -> Boolean.TRUE.equals(c.getEnabled())).collect(Collectors.toList());
    }

    private DeltaProductServiceConfigDO validateExists(Long id) {
        DeltaProductServiceConfigDO config = deltaProductServiceConfigMapper.selectById(id);
        if (config == null) {
            throw exception(PRODUCT_SERVICE_CONFIG_NOT_EXISTS);
        }
        return config;
    }

    private void validateUnique(Long spuId, Long skuId, Long excludeId) {
        if (skuId != null) {
            DeltaProductServiceConfigDO exist = deltaProductServiceConfigMapper.selectBySkuId(skuId);
            if (exist != null && !exist.getId().equals(excludeId)) {
                throw exception(PRODUCT_SERVICE_CONFIG_DUPLICATE);
            }
        }
        if (spuId != null && skuId == null) {
            DeltaProductServiceConfigDO exist = deltaProductServiceConfigMapper.selectBySpuId(spuId);
            if (exist != null && !exist.getId().equals(excludeId)) {
                throw exception(PRODUCT_SERVICE_CONFIG_DUPLICATE);
            }
        }
    }

}
