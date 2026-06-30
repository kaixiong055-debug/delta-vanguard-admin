package cn.iocoder.yudao.module.delta.convert.order;

import cn.iocoder.yudao.module.delta.controller.app.serviceorder.vo.AppDeltaServiceOrderEvidenceRespVO;
import cn.iocoder.yudao.module.delta.controller.app.serviceorder.vo.AppDeltaServiceOrderProgressRespVO;
import cn.iocoder.yudao.module.delta.controller.app.workerorder.vo.AppDeltaWorkerOrderEvidenceRespVO;
import cn.iocoder.yudao.module.delta.controller.app.workerorder.vo.AppDeltaWorkerOrderProgressRespVO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaOrderEvidenceDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaOrderProgressDO;
import cn.iocoder.yudao.module.delta.enums.order.EvidenceTypeEnum;
import cn.iocoder.yudao.module.delta.enums.order.ProgressTypeEnum;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 服务进度 Convert
 */
@Mapper
public interface DeltaOrderProgressConvert {

    DeltaOrderProgressConvert INSTANCE = Mappers.getMapper(DeltaOrderProgressConvert.class);

    AppDeltaWorkerOrderProgressRespVO convert(DeltaOrderProgressDO bean);
    List<AppDeltaWorkerOrderProgressRespVO> convertList(List<DeltaOrderProgressDO> list);

    AppDeltaServiceOrderProgressRespVO convertForBuyer(DeltaOrderProgressDO bean);
    List<AppDeltaServiceOrderProgressRespVO> convertListForBuyer(List<DeltaOrderProgressDO> list);

    default void fillProgressTypeName(AppDeltaWorkerOrderProgressRespVO vo) {
        if (vo.getProgressType() != null) {
            for (ProgressTypeEnum e : ProgressTypeEnum.values()) {
                if (e.getType().equals(vo.getProgressType())) {
                    vo.setProgressTypeName(e.getName());
                    break;
                }
            }
        }
    }

    default void fillProgressTypeNameForBuyer(AppDeltaServiceOrderProgressRespVO vo) {
        if (vo.getProgressType() != null) {
            for (ProgressTypeEnum e : ProgressTypeEnum.values()) {
                if (e.getType().equals(vo.getProgressType())) {
                    vo.setProgressTypeName(e.getName());
                    break;
                }
            }
        }
    }

}
