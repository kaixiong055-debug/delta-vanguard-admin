package cn.iocoder.yudao.module.delta.service.club;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.delta.controller.admin.club.vo.*;
import cn.iocoder.yudao.module.delta.dal.dataobject.club.DeltaClubProfileDO;

import java.util.List;

/**
 * 俱乐部 Service 接口
 *
 * @author Delta-Vanguard
 */
public interface DeltaClubService {

    /**
     * 分页查询俱乐部列表
     */
    PageResult<DeltaClubProfileDO> getClubPage(DeltaClubPageReqVO pageReqVO);

    /**
     * 根据 ID 查询俱乐部详情（含服务范围）
     */
    DeltaClubRespVO getClub(Long id);

    /**
     * 查询当前租户俱乐部
     */
    DeltaClubRespVO getCurrentClub();

    /**
     * 更新俱乐部档案（不允许修改 tenantId、ownerMemberId、applicationId、clubCode）
     */
    void updateClub(DeltaClubUpdateReqVO reqVO);

    /**
     * 更新俱乐部经营状态
     */
    void updateClubStatus(DeltaClubUpdateStatusReqVO reqVO);

    /**
     * 更新俱乐部服务范围（事务全量替换）
     */
    void updateClubServiceScope(DeltaClubUpdateServiceScopeReqVO reqVO);

}
