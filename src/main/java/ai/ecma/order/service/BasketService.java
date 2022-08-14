package ai.ecma.order.service;

import ai.ecma.lib.entity.Order;
import ai.ecma.lib.payload.ApiResult;
import ai.ecma.lib.payload.CustomPage;
import ai.ecma.lib.payload.resp.BasketRespDto;
import ai.ecma.lib.payload.resp.OrderRespDto;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

/**
 * @author Murtazayev Muhammad
 * @since 26.01.2022
 */
public interface BasketService {

    ApiResult<BasketRespDto> incrementProduct(UUID productId);

    ApiResult<BasketRespDto> decrementProduct(UUID productId);

    BasketRespDto toBasketDto(Order basket);

    ApiResult<List<BasketRespDto>> getAll();
}
