package cn.iocoder.yudao.module.delta.service.worker;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.delta.controller.admin.worker.vo.DeltaWorkerPageReqVO;
import cn.iocoder.yudao.module.delta.controller.admin.worker.vo.DeltaWorkerUpdateReqVO;
import cn.iocoder.yudao.module.delta.controller.app.worker.vo.AppDeltaWorkerProfileUpdateReqVO;
import cn.iocoder.yudao.module.delta.dal.dataobject.worker.DeltaWorkerDO;

/**
 * 打手资料 Service 接口
 *
 * @author Delta-Vanguard
 */
public interface DeltaWorkerService {

    // ====== 查询 ======

    /**
     * 根据会员用户ID查询打手资料
     */
    DeltaWorkerDO getWorkerByUserId(Long userId);

    /**
     * 根据打手ID查询打手资料
     */
    DeltaWorkerDO getWorker(Long id);

    /**
     * 校验打手是否存在且有效（审核通过且启用）
     */
    DeltaWorkerDO validateWorkerAvailable(Long id);

    /**
     * 管理后台分页查询打手列表
     */
    PageResult<DeltaWorkerDO> getWorkerPage(DeltaWorkerPageReqVO pageReqVO);

    // ====== 打手身份 ======

    /**
     * 获取打手身份信息（整合 worker + application 状态）
     *
     * @param userId 会员用户ID
     * @return 打手资料（不存在返回 null）
     */
    DeltaWorkerDO getWorkerIdentity(Long userId);

    // ====== App 修改资料 ======

    /**
     * 打手修改自己的资料（白名单字段）
     */
    void updateMyProfile(Long userId, AppDeltaWorkerProfileUpdateReqVO reqVO);

    /**
     * 打手切换工作状态
     */
    void updateMyWorkStatus(Long userId, Integer workStatus);

    // ====== Admin 管理 ======

    /**
     * 管理员修改打手资料（白名单字段）
     */
    void updateWorker(DeltaWorkerUpdateReqVO reqVO);

    /**
     * 管理员修改打手状态（启用/停用）
     */
    void updateWorkerStatus(Long id, Integer status, String reason);

    /**
     * 创建打手资料（审核通过时调用）
     */
    Long createWorker(DeltaWorkerDO worker);

    /**
     * 获取 Mapper（供 Service 层 CAS 操作使用）
     */
    cn.iocoder.yudao.module.delta.dal.mysql.worker.DeltaWorkerMapper getWorkerMapper();

}
