package cn.iocoder.yudao.module.delta.convert.order;

import cn.iocoder.yudao.module.delta.controller.app.serviceorder.vo.DeltaOrderReworkRespVO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaOrderReworkDO;
import cn.iocoder.yudao.module.delta.enums.order.OperatorTypeEnum;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 返工记录 Convert
 *
 * @author Delta-Vanguard
 */
@Mapper
public interface DeltaOrderReworkConvert {

    DeltaOrderReworkConvert INSTANCE = Mappers.getMapper(DeltaOrderReworkConvert.class);

    DeltaOrderReworkRespVO convert(DeltaOrderReworkDO bean);

    List<DeltaOrderReworkRespVO> convertList(List<DeltaOrderReworkDO> list);

    /**
     * 填充操作人类型名称
     */
    default void fillNames(DeltaOrderReworkRespVO vo) {
        if (vo == null) return;
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
