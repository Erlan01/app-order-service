package ai.ecma.order.service;

import ai.ecma.lib.entity.Order;
import ai.ecma.lib.entity.OrderProduct;
import ai.ecma.lib.entity.Product;
import ai.ecma.lib.entity.User;
import ai.ecma.lib.enums.OrderStatusEnum;
import ai.ecma.lib.payload.ApiResult;
import ai.ecma.lib.payload.CustomPage;
import ai.ecma.lib.payload.UserDto;
import ai.ecma.lib.payload.resp.BasketRespDto;
import ai.ecma.lib.payload.resp.OrderProductDto;
import ai.ecma.lib.payload.resp.OrderRespDto;
import ai.ecma.lib.repository.OrderProductRepository;
import ai.ecma.lib.repository.OrderRepository;
import ai.ecma.lib.repository.ProductRepository;
import ai.ecma.lib.repository.UserRepository;
import ai.ecma.order.exception.RestException;
import ai.ecma.order.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Murtazayev Muhammad
 * @since 26.01.2022
 */
@Service
@RequiredArgsConstructor
public class BasketServiceImpl implements BasketService {
    private final ProductRepository productRepository;
    private final OrderProductRepository orderProductRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ApiResult<BasketRespDto> incrementProduct(UUID productId) {
        UserDto userDto = CommonUtils.getCurrentUser();

        User user = userRepository.findById(userDto.getId()).orElseThrow(() -> RestException.notFound("USER"));
        Product product = productRepository.findByIdAndActiveIsTrue(productId).orElseThrow(() -> RestException.notFound("PRODUCT"));

        Optional<Order> optionalBasket = orderRepository.findByClientBasket(user.getId(), OrderStatusEnum.BASKET);
        Order basket;
        if (optionalBasket.isEmpty()) {
            basket = Order.builder()
                    .setClient(user)
                    .setSerialNumber(generateSerialNumber())
                    .setStatus(OrderStatusEnum.BASKET)
                    .setTotalSum(product.getPrice())
                    .build();

        } else {
            basket = optionalBasket.get();
            basket.setTotalSum(basket.getTotalSum() + product.getPrice());
        }

        basket = orderRepository.save(basket);

        Optional<OrderProduct> optionalOrderProduct = orderProductRepository.findByOrderIdAndProductId(basket.getId(), productId);
        OrderProduct orderProduct;
        if (optionalOrderProduct.isEmpty()) {
            orderProduct = new OrderProduct(1, product.getPrice(), basket, product);
        } else {
            orderProduct = optionalOrderProduct.get();
            orderProduct.setQuantity(orderProduct.getQuantity() + 1);
        }

        orderProductRepository.save(orderProduct);

        return ApiResult.successResponse(toBasketDto(basket));
    }

    @Override
    @Transactional
    public ApiResult<BasketRespDto> decrementProduct(UUID productId) {
        UserDto userDto = CommonUtils.getCurrentUser();

        Product product = productRepository.findByIdAndActiveIsTrue(productId).orElseThrow(() -> RestException.notFound("PRODUCT"));
        Order basket = orderRepository.findByClientBasket(userDto.getId(), OrderStatusEnum.BASKET).orElseThrow(() -> RestException.notFound("BASKET"));
        OrderProduct orderProduct = orderProductRepository.findByOrderIdAndProductId(basket.getId(), productId).orElseThrow(() -> RestException.notFound("BASKET_PRODUCT"));

        orderProduct.setQuantity(orderProduct.getQuantity() - 1);
        if (orderProduct.getQuantity() == 0) {
            orderProductRepository.deleteById(orderProduct.getId());
        } else {
            orderProductRepository.save(orderProduct);
        }

        basket.setTotalSum(basket.getTotalSum() - product.getPrice());
        basket = orderRepository.save(basket);

        return ApiResult.successResponse(toBasketDto(basket));
    }

    private String generateSerialNumber() {
        String serialNumber = "";
        boolean unique;

        do {
            for (int i = 0; i < 8; i++) {
                serialNumber += (int) (Math.random() * 10);
            }

            unique = orderRepository.existsBySerialNumber(serialNumber);

        } while (unique);

        return serialNumber;
    }

    @Override
    public BasketRespDto toBasketDto(Order basket) {
        List<OrderProduct> orderProducts = orderProductRepository.findByOrderId(basket.getId());
        List<OrderProductDto> basketProducts = orderProducts.stream().map(orderProduct ->
                        new OrderProductDto(
                                orderProduct.getProduct().getId(),
                                orderProduct.getProduct().getPhoto().getId(),
                                orderProduct.getProduct().getName(),
                                orderProduct.getPrice(),
                                orderProduct.getQuantity()
                        ))
                .collect(Collectors.toList());

        //todo calculate promotion

        return new BasketRespDto(basketProducts, basket.getTotalSum());
    }

    @Override
    public ApiResult<List<BasketRespDto>> getAll() {
        UserDto userDto = CommonUtils.getCurrentUser();
        List<Order> basket = orderRepository.findAllByClientId(userDto.getId());
        List<BasketRespDto> basketRespDtoList = basket.stream().map(this::toBasketDto).collect(Collectors.toList());
        return ApiResult.successResponse(basketRespDtoList);
    }
}
