package cn.iocoder.yudao.module.delta.service.worker;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.delta.controller.admin.workerapplication.vo.DeltaWorkerApplicationApproveReqVO;
import cn.iocoder.yudao.module.delta.controller.admin.workerapplication.vo.DeltaWorkerApplicationPageReqVO;
import cn.iocoder.yudao.module.delta.controller.app.worker.vo.AppDeltaWorkerApplyReqVO;
import cn.iocoder.yudao.module.delta.dal.dataobject.worker.DeltaWorkerApplicationDO;

import java.util.List;

/**
 * 打手申请 Service 接口
 *
 * @author Delta-Vanguard
 */
public interface DeltaWorkerApplicationService {

    // ====== App 端 ======

    /**
     * 会员提交打手申请
     *
     * @param userId 会员用户ID
     * @param reqVO  申请信息
     * @return 申请ID
     */
    Long applyWorker(Long userId, AppDeltaWorkerApplyReqVO reqVO);

    /**
     * 查询会员最近一次申请
     */
    DeltaWorkerApplicationDO getLatestApplicationByUserId(Long userId);

    /**
     * 查询会员申请历史列表
     */
    List<DeltaWorkerApplicationDO> getApplicationListByUserId(Long userId);

    // ====== Admin 端 ======

    /**
     * 根据ID查询申请记录
     */
    DeltaWorkerApplicationDO getApplication(Long id);

    /**
     * 管理后台分页查询申请列表
     */
    PageResult<DeltaWorkerApplicationDO> getApplicationPage(DeltaWorkerApplicationPageReqVO pageReqVO);

    /**
     * 审核通过
     *
     * @param reqVO      审核信息（含技能配置）
     * @param reviewerId 审核人ID
     */
    void approveApplication(DeltaWorkerApplicationApproveReqVO reqVO, Long reviewerId);

    /**
     * 审核驳回
     *
     * @param applicationId 申请ID
     * @param rejectReason  驳回原因
     * @param reviewerId    审核人ID
     */
    void rejectApplication(Long applicationId, String rejectReason, Long reviewerId);

}
