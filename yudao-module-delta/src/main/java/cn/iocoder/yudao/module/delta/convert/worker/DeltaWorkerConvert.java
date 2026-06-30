package cn.iocoder.yudao.module.delta.convert.worker;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.delta.controller.admin.worker.vo.DeltaWorkerRespVO;
import cn.iocoder.yudao.module.delta.controller.app.worker.vo.AppDeltaWorkerProfileRespVO;
import cn.iocoder.yudao.module.delta.dal.dataobject.worker.DeltaWorkerDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.worker.DeltaWorkerSkillDO;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 打手 Convert
 *
 * @author Delta-Vanguard
 */
@Mapper
public interface DeltaWorkerConvert {

    DeltaWorkerConvert INSTANCE = Mappers.getMapper(DeltaWorkerConvert.class);

    // ========== DO -> App RespVO ==========

    @Mappings({
            @Mapping(source = "id", target = "workerId"),
            @Mapping(source = "status", target = "enabledStatus")
    })
    AppDeltaWorkerProfileRespVO convert(DeltaWorkerDO bean);

    // ========== DO -> Admin RespVO ==========
    DeltaWorkerRespVO convert2(DeltaWorkerDO bean);
    List<DeltaWorkerRespVO> convertList2(List<DeltaWorkerDO> list);
    PageResult<DeltaWorkerRespVO> convertPage2(PageResult<DeltaWorkerDO> page);

    // ========== SkillDO -> SkillInfo ==========
    AppDeltaWorkerProfileRespVO.SkillInfo convertSkill(DeltaWorkerSkillDO bean);
    List<AppDeltaWorkerProfileRespVO.SkillInfo> convertSkillList(List<DeltaWorkerSkillDO> list);

    DeltaWorkerRespVO.SkillInfo convertSkill2(DeltaWorkerSkillDO bean);
    List<DeltaWorkerRespVO.SkillInfo> convertSkillList2(List<DeltaWorkerSkillDO> list);

    // ========== 带关联字段填充的 Admin RespVO ==========

    /**
     * Admin RespVO 填充会员昵称和头像
     */
    default DeltaWorkerRespVO convert2(DeltaWorkerDO bean, MemberUserRespDTO memberUser) {
        DeltaWorkerRespVO vo = convert2(bean);
        if (memberUser != null) {
            vo.setMemberNickname(memberUser.getNickname());
            vo.setMemberAvatar(memberUser.getAvatar());
        }
        return vo;
    }

    /**
     * Admin 分页 RespVO 批量填充会员信息
     */
    default PageResult<DeltaWorkerRespVO> convertPage2(PageResult<DeltaWorkerDO> pageResult,
                                                        java.util.Map<Long, MemberUserRespDTO> memberUserMap) {
        PageResult<DeltaWorkerRespVO> result = convertPage2(pageResult);
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
