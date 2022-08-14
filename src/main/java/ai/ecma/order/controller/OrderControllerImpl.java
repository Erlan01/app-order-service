package ai.ecma.order.controller;

import ai.ecma.lib.enums.PermissionEnum;
import ai.ecma.lib.enums.RoleTypeEnum;
import ai.ecma.lib.payload.ApiResult;
import ai.ecma.lib.payload.CustomPage;
import ai.ecma.lib.payload.resp.OrderRespDto;
import ai.ecma.lib.payload.resp.ToOrderRespDto;
import ai.ecma.order.aop.CheckAuth;
import ai.ecma.order.payload.AttachCourierDto;
import ai.ecma.order.payload.ConfirmOrderDto;
import ai.ecma.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * @author Murtazayev Muhammad
 * @since 27.01.2022
 */
@RestController
@RequiredArgsConstructor
public class OrderControllerImpl implements OrderController {
    private final OrderService orderService;

    @CheckAuth
    @Override
    public ApiResult<CustomPage<OrderRespDto>> getNewOrders(Integer page, Integer size) {
        return orderService.getNewOrders(page, size);
    }

    @CheckAuth
    @Override
    public ApiResult<CustomPage<OrderRespDto>> getSuccessOrders(Integer page, Integer size) {
        return orderService.getSuccessOrders(page, size);
    }

    @Override
    @CheckAuth/*(roles = RoleTypeEnum.OPERATOR)*/
    public ApiResult<CustomPage<OrderRespDto>> getOperatorOrders(Integer page, Integer size) {
        return orderService.getOperatorOrders(page, size);
    }

    @Override
    public ApiResult<?> getReadyOrders(Integer page, Integer size) {
        return orderService.getReadyOrders(page, size);
    }

    @CheckAuth
    @Override
    public ApiResult<ToOrderRespDto> toOrder() {
        return orderService.toOrder();
    }

    @Override
    @CheckAuth
    public ApiResult<?> confirm(ConfirmOrderDto orderDto) {
        return orderService.confirm(orderDto);
    }

    @Override
    public ApiResult<?> cancelOrder(UUID orderId) {
        return orderService.cancelOrder(orderId);
    }

    @Override
    @CheckAuth/*(roles = RoleTypeEnum.OPERATOR)*/
    public ApiResult<?> toPending(UUID id) {
        return orderService.toPending(id);
    }

    @Override
    @CheckAuth/*(roles = RoleTypeEnum.OPERATOR)*/
    public ApiResult<CustomPage<OrderRespDto>> getPending(Integer page, Integer size) {
        return orderService.getPending(page, size);
    }

    @Override
    @CheckAuth/*(roles = RoleTypeEnum.COOK)*/
    public ApiResult<?> toProcess(UUID id) {
        return orderService.toProcess(id);
    }

    @Override
    @CheckAuth/*(roles = RoleTypeEnum.COOK)*/
    public ApiResult<?> toReady(UUID id) {
        return orderService.toReady(id);
    }

    //    @CheckAuth(roles = RoleTypeEnum.DISPATCHER)
    @Override
    public ApiResult<?> attachCourier(AttachCourierDto attachCourierDto) {
        return orderService.attachCourier(attachCourierDto);
    }

    @Override
    @CheckAuth/*(roles = RoleTypeEnum.COURIER)*/
    public ApiResult<?> toWay() {
        return orderService.toWay();
    }

    @Override
    @CheckAuth
    public ApiResult<?> toSuccess(UUID id) {
        return orderService.toSuccess(id);
    }
}
