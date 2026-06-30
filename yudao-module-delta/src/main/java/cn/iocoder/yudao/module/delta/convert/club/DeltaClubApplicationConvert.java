package cn.iocoder.yudao.module.delta.convert.club;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.delta.controller.admin.clubapplication.vo.DeltaClubApplicationRespVO;
import cn.iocoder.yudao.module.delta.controller.app.clubapplication.vo.AppDeltaClubApplicationRespVO;
import cn.iocoder.yudao.module.delta.dal.dataobject.club.DeltaClubApplicationDO;
import cn.iocoder.yudao.module.delta.enums.club.DeltaClubApplicationStatusEnum;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

/**
 * 俱乐部入驻申请 Convert
 *
 * @author Delta-Vanguard
 */
@Mapper
public interface DeltaClubApplicationConvert {

    DeltaClubApplicationConvert INSTANCE = Mappers.getMapper(DeltaClubApplicationConvert.class);

    // ========== DO -> App RespVO ==========
    AppDeltaClubApplicationRespVO convertApp(DeltaClubApplicationDO bean);

    // ========== DO -> Admin RespVO ==========
    DeltaClubApplicationRespVO convert(DeltaClubApplicationDO bean);
    List<DeltaClubApplicationRespVO> convertList(List<DeltaClubApplicationDO> list);
    PageResult<DeltaClubApplicationRespVO> convertPage(PageResult<DeltaClubApplicationDO> page);

    // ========== 带会员信息的 Admin RespVO ==========
    default DeltaClubApplicationRespVO convert(DeltaClubApplicationDO bean, MemberUserRespDTO memberUser) {
        DeltaClubApplicationRespVO vo = convert(bean);
        if (memberUser != null) {
            vo.setMemberNickname(memberUser.getNickname());
            vo.setMemberAvatar(memberUser.getAvatar());
        }
        // 状态名称
        vo.setApplicationStatusName(getStatusName(bean.getApplicationStatus()));
        return vo;
    }

    default PageResult<DeltaClubApplicationRespVO> convertPage(
            PageResult<DeltaClubApplicationDO> pageResult,
            Map<Long, MemberUserRespDTO> memberUserMap) {
        PageResult<DeltaClubApplicationRespVO> result = convertPage(pageResult);
        result.getList().forEach(vo -> {
            MemberUserRespDTO memberUser = memberUserMap.get(vo.getApplicantMemberId());
            if (memberUser != null) {
                vo.setMemberNickname(memberUser.getNickname());
                vo.setMemberAvatar(memberUser.getAvatar());
            }
            vo.setApplicationStatusName(getStatusName(vo.getApplicationStatus()));
        });
        return result;
    }

    default String getStatusName(Integer status) {
        if (status == null) return "-";
        for (DeltaClubApplicationStatusEnum e : DeltaClubApplicationStatusEnum.values()) {
            if (e.getStatus().equals(status)) return e.getName();
        }
        return "-";
    }

}
