package ai.ecma.order.controller;

import ai.ecma.lib.payload.ApiResult;
import ai.ecma.lib.payload.req.OrderRateReqDto;
import ai.ecma.lib.payload.resp.OrderRateRespDto;
import ai.ecma.order.aop.CheckAuth;
import ai.ecma.order.service.OrderRateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

/**
 * @AUTHOR: userdev
 * @DONE_ON: 2022/02/16
 */

@RestController
@RequiredArgsConstructor
public class OrderRateControllerImpl implements OrderRateController {

    private final OrderRateService orderRateService;

    @CheckAuth
    @Override
    public ApiResult<OrderRateRespDto> create(OrderRateReqDto rateReqDto) {
        return orderRateService.create(rateReqDto);
    }


}
