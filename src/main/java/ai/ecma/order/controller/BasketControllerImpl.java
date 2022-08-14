package ai.ecma.order.controller;

import ai.ecma.lib.enums.PermissionEnum;
import ai.ecma.lib.payload.ApiResult;
import ai.ecma.lib.payload.CustomPage;
import ai.ecma.lib.payload.resp.BasketRespDto;
import ai.ecma.order.aop.CheckAuth;
import ai.ecma.order.service.BasketService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * @author Murtazayev Muhammad
 * @since 26.01.2022
 */
@RestController
@RequiredArgsConstructor
public class BasketControllerImpl implements BasketController {
    private final BasketService basketService;

    @CheckAuth(permissions = PermissionEnum.ADD_PRODUCT)
    @Override
    public ApiResult<BasketRespDto> incrementProduct(UUID productId) {
        return basketService.incrementProduct(productId);
    }

    @CheckAuth
    @Override
    public ApiResult<BasketRespDto> decrementProduct(UUID productId) {
        return basketService.decrementProduct(productId);
    }

    @CheckAuth
    @Override
    public ApiResult<List<BasketRespDto>> getAll() {
        return basketService.getAll();
    }
}
