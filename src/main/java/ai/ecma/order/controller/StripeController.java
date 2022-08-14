package ai.ecma.order.controller;

import ai.ecma.lib.payload.ApiResult;
import ai.ecma.lib.payload.req.CardDto;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

import static ai.ecma.order.utils.AppConstant.BASE_PATH;

/**
 * @author Murtazayev Muhammad
 * @since 28.01.2022
 */
@RequestMapping(StripeController.STRIPE_CONTROLLER)
public interface StripeController {
    String STRIPE_CONTROLLER = BASE_PATH + "/stripe";

    @PostMapping
    ApiResult<?> createToken(@RequestBody @Valid CardDto cardDto);
}
