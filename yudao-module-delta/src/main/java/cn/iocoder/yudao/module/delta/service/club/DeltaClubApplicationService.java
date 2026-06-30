package cn.iocoder.yudao.module.delta.service.club;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.delta.controller.admin.clubapplication.vo.DeltaClubApplicationApproveReqVO;
import cn.iocoder.yudao.module.delta.controller.admin.clubapplication.vo.DeltaClubApplicationPageReqVO;
import cn.iocoder.yudao.module.delta.controller.app.clubapplication.vo.AppDeltaClubApplicationSubmitReqVO;
import cn.iocoder.yudao.module.delta.dal.dataobject.club.DeltaClubApplicationDO;

/**
 * 俱乐部入驻申请 Service 接口
 *
 * @author Delta-Vanguard
 */
public interface DeltaClubApplicationService {

    // ===== App 端 =====

    /**
     * 会员提交入驻申请
     *
     * @param memberId 会员用户 ID
     * @param reqVO    申请信息
     * @return 申请 ID
     */
    Long submitApplication(Long memberId, AppDeltaClubApplicationSubmitReqVO reqVO);

    /**
     * 查询当前会员最新申请
     */
    DeltaClubApplicationDO getMyLatestApplication(Long memberId);

    /**
     * 撤销申请
     *
     * @param memberId 会员用户 ID
     */
    void cancelApplication(Long memberId);

    // ===== Admin 端 =====

    /**
     * 根据 ID 查询申请
     */
    DeltaClubApplicationDO getApplication(Long id);

    /**
     * 分页查询申请列表
     */
    PageResult<DeltaClubApplicationDO> getApplicationPage(DeltaClubApplicationPageReqVO pageReqVO);

    /**
     * 审核通过
     *
     * @param reqVO     审核信息
     * @param auditorId 审核人 ID
     */
    void approveApplication(DeltaClubApplicationApproveReqVO reqVO, Long auditorId);

    /**
     * 审核拒绝
     *
     * @param applicationId 申请 ID
     * @param reason        拒绝原因
     * @param auditorId     审核人 ID
     */
    void rejectApplication(Long applicationId, String reason, Long auditorId);

}
