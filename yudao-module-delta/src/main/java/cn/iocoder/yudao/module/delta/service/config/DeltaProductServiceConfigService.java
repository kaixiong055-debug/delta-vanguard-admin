package cn.iocoder.yudao.module.delta.service.config;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.delta.controller.admin.config.vo.DeltaProductServiceConfigPageReqVO;
import cn.iocoder.yudao.module.delta.controller.admin.config.vo.DeltaProductServiceConfigSaveReqVO;
import cn.iocoder.yudao.module.delta.dal.dataobject.config.DeltaProductServiceConfigDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 商品服务配置 Service 接口
 *
 * @author Delta-Vanguard
 */
public interface DeltaProductServiceConfigService {

    /**
     * 创建商品服务配置
     */
    Long createConfig(@Valid DeltaProductServiceConfigSaveReqVO createReqVO);

    /**
     * 更新商品服务配置
     */
    void updateConfig(@Valid DeltaProductServiceConfigSaveReqVO updateReqVO);

    /**
     * 删除商品服务配置（逻辑删除）
     */
    void deleteConfig(Long id);

    /**
     * 更新配置启用/停用状态
     */
    void updateConfigStatus(Long id, Boolean enabled);

    /**
     * 查询单个配置
     */
    DeltaProductServiceConfigDO getConfig(Long id);

    /**
     * 分页查询配置
     */
    PageResult<DeltaProductServiceConfigDO> getConfigPage(DeltaProductServiceConfigPageReqVO pageReqVO);

    /**
     * 根据SKU ID查询服务配置
     */
    DeltaProductServiceConfigDO getConfigBySkuId(Long skuId);

    /**
     * 校验并获取有效的服务配置
     */
    DeltaProductServiceConfigDO validateConfigEnabled(Long skuId);

    /**
     * 批量根据SKU ID查询有效配置 (enabled=true)
     */
    List<DeltaProductServiceConfigDO> getEnabledConfigsBySkuIds(Collection<Long> skuIds);

    /**
     * 批量根据SPU ID查询有效配置 (enabled=true)
     */
    List<DeltaProductServiceConfigDO> getEnabledConfigsBySpuIds(Collection<Long> spuIds);

}
