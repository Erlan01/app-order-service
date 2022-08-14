package ai.ecma.order.service;

import ai.ecma.lib.entity.Order;
import ai.ecma.lib.payload.ApiResult;
import ai.ecma.lib.payload.CustomPage;
import ai.ecma.lib.payload.resp.OrderRespDto;
import ai.ecma.lib.payload.resp.ToOrderRespDto;
import ai.ecma.order.payload.AttachCourierDto;
import ai.ecma.order.payload.ConfirmOrderDto;
import org.springframework.data.domain.Page;

import java.util.UUID;

/**
 * @author Murtazayev Muhammad
 * @since 27.01.2022
 */
public interface OrderService {
    ApiResult<CustomPage<OrderRespDto>> getNewOrders(Integer page, Integer size);

    ApiResult<CustomPage<OrderRespDto>> getSuccessOrders(Integer page, Integer size);

    ApiResult<CustomPage<OrderRespDto>> getOperatorOrders(Integer page, Integer size);

    ApiResult<?> getReadyOrders(Integer page, Integer size);

    ApiResult<ToOrderRespDto> toOrder();

    CustomPage<OrderRespDto> makeCustomPage(Page<Order> orders);

    ApiResult<?> confirm(ConfirmOrderDto orderDto);

    ApiResult<?> cancelOrder(UUID orderId);

    ApiResult<?> toPending(UUID id);

    ApiResult<CustomPage<OrderRespDto>> getPending(Integer page, Integer size);

    ApiResult<?> toProcess(UUID id);

    ApiResult<?> toReady(UUID id);

    ApiResult<?> attachCourier(AttachCourierDto attachCourierDto);

    ApiResult<?> toWay();

    ApiResult<?> toSuccess(UUID id);
}
