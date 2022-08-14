package ai.ecma.order.controller;

import ai.ecma.lib.payload.ApiResult;
import ai.ecma.lib.payload.req.CardDto;
import ai.ecma.order.service.StripeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Murtazayev Muhammad
 * @since 28.01.2022
 */
@RestController
@RequiredArgsConstructor
public class StripeControllerImpl implements StripeController {
    private final StripeService stripeService;

    @Override
    public ApiResult<?> createToken(CardDto cardDto) {
        return stripeService.createToken(cardDto);
    }
}
