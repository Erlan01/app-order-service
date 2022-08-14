package ai.ecma.order.service;

import ai.ecma.lib.payload.ApiResult;
import ai.ecma.lib.payload.req.OrderRateReqDto;
import ai.ecma.lib.payload.resp.OrderRateRespDto;

/**
 * @AUTHOR: userdev
 * @DONE_ON: 2022/02/16
 */
public interface OrderRateService {


    ApiResult<OrderRateRespDto> create(OrderRateReqDto rateReqDto);
}
