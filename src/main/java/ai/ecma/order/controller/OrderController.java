package ai.ecma.order.controller;

import ai.ecma.lib.payload.ApiResult;
import ai.ecma.lib.payload.CustomPage;
import ai.ecma.lib.payload.resp.OrderRespDto;
import ai.ecma.lib.payload.resp.ToOrderRespDto;
import ai.ecma.order.payload.AttachCourierDto;
import ai.ecma.order.payload.ConfirmOrderDto;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

import static ai.ecma.order.utils.AppConstant.*;

/**
 * @author Murtazayev Muhammad
 * @since 26.01.2022
 */
@RequestMapping(OrderController.ORDER_CONTROLLER)
public interface OrderController {
    String ORDER_CONTROLLER = BASE_PATH + "/order";

    @GetMapping("/new")
    ApiResult<CustomPage<OrderRespDto>> getNewOrders(@RequestParam(name = "page", defaultValue = DEFAULT_PAGE_NUMBER, required = false) Integer page,
                                                     @RequestParam(name = "size", defaultValue = DEFAULT_PAGE_SIZE, required = false) Integer size);

    @GetMapping("/success")
    ApiResult<CustomPage<OrderRespDto>> getSuccessOrders(@RequestParam(name = "page", defaultValue = DEFAULT_PAGE_NUMBER, required = false) Integer page,
                                                         @RequestParam(name = "size", defaultValue = DEFAULT_PAGE_SIZE, required = false) Integer size);

    @GetMapping("/operator/new")
    ApiResult<CustomPage<OrderRespDto>> getOperatorOrders(@RequestParam(name = "page", defaultValue = DEFAULT_PAGE_NUMBER, required = false) Integer page,
                                                          @RequestParam(name = "size", defaultValue = DEFAULT_PAGE_SIZE, required = false) Integer size);

    @GetMapping("/dispatcher/ready")
    ApiResult<?> getReadyOrders(@RequestParam(name = "page", defaultValue = DEFAULT_PAGE_NUMBER, required = false) Integer page,
                                @RequestParam(name = "size", defaultValue = DEFAULT_PAGE_SIZE, required = false) Integer size);

    @PostMapping("/create")
    ApiResult<ToOrderRespDto> toOrder();

    @PostMapping("/confirm")
    ApiResult<?> confirm(@RequestBody @Valid ConfirmOrderDto orderDto);

    @PutMapping("/cancel/{orderId}")
    ApiResult<?> cancelOrder(@PathVariable UUID orderId);

    @GetMapping("/to-pending/{id}")
    ApiResult<?> toPending(@PathVariable UUID id);

    @GetMapping("/pending")
    ApiResult<CustomPage<OrderRespDto>> getPending(@RequestParam(name = "page", defaultValue = DEFAULT_PAGE_NUMBER, required = false) Integer page,
                                                   @RequestParam(name = "size", defaultValue = DEFAULT_PAGE_SIZE, required = false) Integer size);

    @GetMapping("/to-process/{id}")
    ApiResult<?> toProcess(@PathVariable UUID id);

    @GetMapping("/to-ready/{id}")
    ApiResult<?> toReady(@PathVariable UUID id);

    @PostMapping("/attach-courier")
    ApiResult<?> attachCourier(@RequestBody @Valid AttachCourierDto attachCourierDto);

    @GetMapping("/to-way")
    ApiResult<?> toWay();

    @GetMapping("/to-success/{id}")
    ApiResult<?> toSuccess(@PathVariable UUID id);
}
