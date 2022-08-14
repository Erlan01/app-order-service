package ai.ecma.order.controller;

import ai.ecma.lib.payload.ApiResult;
import ai.ecma.lib.payload.req.OrderRateReqDto;
import ai.ecma.lib.payload.resp.OrderRateRespDto;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

import static ai.ecma.order.utils.AppConstant.BASE_PATH;

/**
 * @AUTHOR: userdev
 * @DONE_ON: 2022/02/16
 */
@RequestMapping(OrderRateController.ORDER_RATE_CONTROLLER)
public interface OrderRateController {

    String ORDER_RATE_CONTROLLER = BASE_PATH + "/order-rate";

    @PostMapping("/create")
    ApiResult<OrderRateRespDto> create(@RequestBody @Valid OrderRateReqDto rateReqDto);

}
