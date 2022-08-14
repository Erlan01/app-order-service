package ai.ecma.order.service;

import ai.ecma.lib.payload.ApiResult;
import ai.ecma.lib.payload.req.CardDto;

public interface StripeService {
    ApiResult<?> createToken(CardDto cardDto);

}
