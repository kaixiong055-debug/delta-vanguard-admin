package cn.iocoder.yudao.module.delta.dal.mysql.club;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.delta.controller.admin.clubapplication.vo.DeltaClubApplicationPageReqVO;
import cn.iocoder.yudao.module.delta.dal.dataobject.club.DeltaClubApplicationDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 俱乐部入驻申请 Mapper
 *
 * @author Delta-Vanguard
 */
@Mapper
public interface DeltaClubApplicationMapper extends BaseMapperX<DeltaClubApplicationDO> {

    /** 查询会员最新一条申请 */
    default DeltaClubApplicationDO selectLatestByMemberId(Long memberId) {
        return selectOne(new LambdaQueryWrapperX<DeltaClubApplicationDO>()
                .eq(DeltaClubApplicationDO::getApplicantMemberId, memberId)
                .orderByDesc(DeltaClubApplicationDO::getId)
                .last("LIMIT 1"));
    }

    /** 分页查询 */
    default PageResult<DeltaClubApplicationDO> selectPage(DeltaClubApplicationPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<DeltaClubApplicationDO>()
                .eqIfPresent(DeltaClubApplicationDO::getApplicationNo, reqVO.getApplicationNo())
                .likeIfPresent(DeltaClubApplicationDO::getClubName, reqVO.getClubName())
                .eqIfPresent(DeltaClubApplicationDO::getApplicantMemberId, reqVO.getApplicantMemberId())
                .eqIfPresent(DeltaClubApplicationDO::getContactMobile, reqVO.getContactMobile())
                .eqIfPresent(DeltaClubApplicationDO::getApplicationStatus, reqVO.getApplicationStatus())
                .betweenIfPresent(DeltaClubApplicationDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(DeltaClubApplicationDO::getId));
    }

}
