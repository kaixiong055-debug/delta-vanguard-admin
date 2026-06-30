package cn.iocoder.yudao.module.delta.convert.worker;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.delta.controller.admin.workerapplication.vo.DeltaWorkerApplicationRespVO;
import cn.iocoder.yudao.module.delta.controller.app.worker.vo.AppDeltaWorkerApplicationRespVO;
import cn.iocoder.yudao.module.delta.dal.dataobject.worker.DeltaWorkerApplicationDO;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 打手申请 Convert
 *
 * @author Delta-Vanguard
 */
@Mapper
public interface DeltaWorkerApplicationConvert {

    DeltaWorkerApplicationConvert INSTANCE = Mappers.getMapper(DeltaWorkerApplicationConvert.class);

    // ========== DO -> App RespVO ==========
    AppDeltaWorkerApplicationRespVO convert(DeltaWorkerApplicationDO bean);

    // ========== DO -> Admin RespVO ==========
    DeltaWorkerApplicationRespVO convert2(DeltaWorkerApplicationDO bean);
    List<DeltaWorkerApplicationRespVO> convertList2(List<DeltaWorkerApplicationDO> list);
    PageResult<DeltaWorkerApplicationRespVO> convertPage2(PageResult<DeltaWorkerApplicationDO> page);

    // ========== 带会员信息的 Admin RespVO ==========

    default DeltaWorkerApplicationRespVO convert2(DeltaWorkerApplicationDO bean,
                                                   MemberUserRespDTO memberUser) {
        DeltaWorkerApplicationRespVO vo = convert2(bean);
        if (memberUser != null) {
            vo.setMemberNickname(memberUser.getNickname());
            vo.setMemberAvatar(memberUser.getAvatar());
        }
        return vo;
    }

    default PageResult<DeltaWorkerApplicationRespVO> convertPage2(
            PageResult<DeltaWorkerApplicationDO> pageResult,
            java.util.Map<Long, MemberUserRespDTO> memberUserMap) {
        PageResult<DeltaWorkerApplicationRespVO> result = convertPage2(pageResult);
        result.getList().forEach(vo -> {
            MemberUserRespDTO memberUser = memberUserMap.get(vo.getUserId());
            if (memberUser != null) {
                vo.setMemberNickname(memberUser.getNickname());
                vo.setMemberAvatar(memberUser.getAvatar());
            }
        });
        return result;
    }

}
