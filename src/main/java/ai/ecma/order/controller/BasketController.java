package ai.ecma.order.controller;

import ai.ecma.lib.payload.ApiResult;
import ai.ecma.lib.payload.CustomPage;
import ai.ecma.lib.payload.resp.BasketRespDto;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static ai.ecma.order.utils.AppConstant.*;

/**
 * @author Murtazayev Muhammad
 * @since 26.01.2022
 */
@RequestMapping(BasketController.BASKET_CONTROLLER)
public interface BasketController {
    String BASKET_CONTROLLER = BASE_PATH + "/basket";

    @PostMapping("/increment-product/{productId}")
    ApiResult<BasketRespDto> incrementProduct(@PathVariable UUID productId);

    @PostMapping("/decrement-product/{productId}")
    ApiResult<BasketRespDto> decrementProduct(@PathVariable UUID productId);

    @GetMapping()
    ApiResult<List<BasketRespDto>> getAll();
}
