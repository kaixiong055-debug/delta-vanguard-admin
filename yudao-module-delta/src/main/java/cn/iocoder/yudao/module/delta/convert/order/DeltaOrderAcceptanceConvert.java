package cn.iocoder.yudao.module.delta.convert.order;

import cn.iocoder.yudao.module.delta.controller.app.serviceorder.vo.DeltaOrderAcceptanceRespVO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaOrderAcceptanceDO;
import cn.iocoder.yudao.module.delta.enums.order.AcceptanceResultEnum;
import cn.iocoder.yudao.module.delta.enums.order.OperatorTypeEnum;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 验收记录 Convert
 *
 * @author Delta-Vanguard
 */
@Mapper
public interface DeltaOrderAcceptanceConvert {

    DeltaOrderAcceptanceConvert INSTANCE = Mappers.getMapper(DeltaOrderAcceptanceConvert.class);

    DeltaOrderAcceptanceRespVO convert(DeltaOrderAcceptanceDO bean);

    List<DeltaOrderAcceptanceRespVO> convertList(List<DeltaOrderAcceptanceDO> list);

    /**
     * 填充验收结果名称和操作人类型名称
     */
    default void fillNames(DeltaOrderAcceptanceRespVO vo) {
        if (vo == null) return;
        if (vo.getAcceptanceResult() != null) {
            for (AcceptanceResultEnum e : AcceptanceResultEnum.values()) {
                if (e.getResult().equals(vo.getAcceptanceResult())) {
                    vo.setAcceptanceResultName(e.getName());
                    break;
                }
            }
        }
        if (vo.getOperatorType() != null) {
            for (OperatorTypeEnum e : OperatorTypeEnum.values()) {
                if (e.getType().equals(vo.getOperatorType())) {
                    vo.setOperatorTypeName(e.getName());
                    break;
                }
            }
        }
    }

}
