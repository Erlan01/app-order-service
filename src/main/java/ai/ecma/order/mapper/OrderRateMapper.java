package ai.ecma.order.mapper;

import ai.ecma.lib.entity.OrderRate;
import ai.ecma.lib.payload.resp.OrderRateRespDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

/**
 * @AUTHOR: userdev
 * @DONE_ON: 2022/02/16
 */
@Mapper(componentModel = "spring")
public interface OrderRateMapper {

    @Mappings({
            @Mapping(target = "orderId", source = "order.id"),
            @Mapping(target = "attachmentId", source = "photo.id")
    })
    OrderRateRespDto toOrderRateRespDto(OrderRate orderRate);
}
