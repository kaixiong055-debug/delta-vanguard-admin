package cn.iocoder.yudao.module.delta.convert.order;

import cn.iocoder.yudao.module.delta.controller.app.serviceorder.vo.AppDeltaServiceOrderEvidenceRespVO;
import cn.iocoder.yudao.module.delta.controller.app.workerorder.vo.AppDeltaWorkerOrderEvidenceRespVO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaOrderEvidenceDO;
import cn.iocoder.yudao.module.delta.enums.order.EvidenceTypeEnum;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 服务凭证 Convert
 */
@Mapper
public interface DeltaOrderEvidenceConvert {

    DeltaOrderEvidenceConvert INSTANCE = Mappers.getMapper(DeltaOrderEvidenceConvert.class);

    AppDeltaWorkerOrderEvidenceRespVO convert(DeltaOrderEvidenceDO bean);
    List<AppDeltaWorkerOrderEvidenceRespVO> convertList(List<DeltaOrderEvidenceDO> list);

    AppDeltaServiceOrderEvidenceRespVO convertForBuyer(DeltaOrderEvidenceDO bean);
    List<AppDeltaServiceOrderEvidenceRespVO> convertListForBuyer(List<DeltaOrderEvidenceDO> list);

    default void fillEvidenceTypeName(AppDeltaWorkerOrderEvidenceRespVO vo) {
        if (vo.getEvidenceType() != null) {
            for (EvidenceTypeEnum e : EvidenceTypeEnum.values()) {
                if (e.getType().equals(vo.getEvidenceType())) {
                    vo.setEvidenceTypeName(e.getName());
                    break;
                }
            }
        }
    }

    default void fillEvidenceTypeNameForBuyer(AppDeltaServiceOrderEvidenceRespVO vo) {
        if (vo.getEvidenceType() != null) {
            for (EvidenceTypeEnum e : EvidenceTypeEnum.values()) {
                if (e.getType().equals(vo.getEvidenceType())) {
                    vo.setEvidenceTypeName(e.getName());
                    break;
                }
            }
        }
    }

}
