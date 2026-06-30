package cn.iocoder.yudao.module.delta.convert.settlement;

import cn.iocoder.yudao.module.delta.controller.admin.settlement.vo.DeltaWorkerSettlementDetailRespVO;
import cn.iocoder.yudao.module.delta.controller.admin.settlement.vo.DeltaWorkerSettlementRespVO;
import cn.iocoder.yudao.module.delta.controller.app.settlement.vo.AppDeltaWorkerSettlementRespVO;
import cn.iocoder.yudao.module.delta.controller.app.settlement.vo.AppDeltaWorkerSettlementSummaryRespVO;
import cn.iocoder.yudao.module.delta.dal.dataobject.settlement.DeltaWorkerSettlementDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.settlement.DeltaWorkerSettlementLogDO;
import cn.iocoder.yudao.module.delta.enums.settlement.SettlementStatusEnum;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 打手结算 Convert
 */
@Mapper
public interface DeltaWorkerSettlementConvert {

    DeltaWorkerSettlementConvert INSTANCE = Mappers.getMapper(DeltaWorkerSettlementConvert.class);

    // ====== App ======

    AppDeltaWorkerSettlementRespVO convert(DeltaWorkerSettlementDO bean);

    List<AppDeltaWorkerSettlementRespVO> convertListApp(List<DeltaWorkerSettlementDO> list);


    // ====== Admin ======

    DeltaWorkerSettlementRespVO convertAdmin(DeltaWorkerSettlementDO bean);

    List<DeltaWorkerSettlementRespVO> convertListAdmin(List<DeltaWorkerSettlementDO> list);

    DeltaWorkerSettlementDetailRespVO convertDetail(DeltaWorkerSettlementDO bean);

    // ====== Log ======

    DeltaWorkerSettlementDetailRespVO.SettlementLogItem convertLogItem(DeltaWorkerSettlementLogDO bean);

    List<DeltaWorkerSettlementDetailRespVO.SettlementLogItem> convertLogItems(List<DeltaWorkerSettlementLogDO> list);

    // ====== 填充名称 ======

    default void fillStatusName(AppDeltaWorkerSettlementRespVO vo) {
        if (vo != null && vo.getSettlementStatus() != null) {
            for (SettlementStatusEnum e : SettlementStatusEnum.values()) {
                if (e.getStatus().equals(vo.getSettlementStatus())) {
                    vo.setSettlementStatusName(e.getName());
                    break;
                }
            }
        }
    }

    default void fillStatusNameAdmin(DeltaWorkerSettlementRespVO vo) {
        if (vo != null && vo.getSettlementStatus() != null) {
            for (SettlementStatusEnum e : SettlementStatusEnum.values()) {
                if (e.getStatus().equals(vo.getSettlementStatus())) {
                    vo.setSettlementStatusName(e.getName());
                    break;
                }
            }
        }
    }

    default void fillStatusNameDetail(DeltaWorkerSettlementDetailRespVO vo) {
        if (vo != null && vo.getSettlementStatus() != null) {
            for (SettlementStatusEnum e : SettlementStatusEnum.values()) {
                if (e.getStatus().equals(vo.getSettlementStatus())) {
                    vo.setSettlementStatusName(e.getName());
                    break;
                }
            }
        }
    }

}
