package ai.ecma.order.service;

import ai.ecma.lib.entity.*;
import ai.ecma.lib.enums.OrderStatusEnum;
import ai.ecma.lib.enums.PaymentStatusEnum;
import ai.ecma.lib.enums.RoleTypeEnum;
import ai.ecma.lib.payload.ApiResult;
import ai.ecma.lib.payload.CustomPage;
import ai.ecma.lib.payload.UserDto;
import ai.ecma.lib.payload.resp.*;
import ai.ecma.lib.repository.*;
import ai.ecma.lib.service.CommonService;
import ai.ecma.order.common.MessageService;
import ai.ecma.order.exception.RestException;
import ai.ecma.order.feign.BranchFeign;
import ai.ecma.order.mapper.ProductMapper;
import ai.ecma.order.payload.AttachCourierDto;
import ai.ecma.order.payload.ConfirmOrderDto;
import ai.ecma.order.utils.CommonUtils;
import com.stripe.Stripe;
import com.stripe.model.Charge;
import com.stripe.model.Refund;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Murtazayev Muhammad
 * @since 27.01.2022
 */
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderRateRepository orderRateRepository;
    private final OrderProductRepository orderProductRepository;
    private final OrderPaymentRepository orderPaymentRepository;
    private final OrderHistoryRepository orderHistoryRepository;
    private final UserRepository userRepository;
    private final BranchFeign branchFeign;
    private final SimplePromotionRepository simplePromotionRepository;
    private final DeliveryPromotionRepository deliveryPromotionRepository;
    private final QuantityPromotionRepository quantityPromotionRepository;
    private final GroupPromotionRepository groupPromotionRepository;
    private final GroupPromotionProductRepository groupPromotionProductRepository;
    private final PayTypeRepository payTypeRepository;
    private final PaymentService paymentService;
    private final BranchRepository branchRepository;
    private final DeliveryTariffRepository deliveryTariffRepository;
    private final PromotionRepository promotionRepository;
    private final OrderPromotionRepository orderPromotionRepository;
    private final PromotionProductRepository promotionProductRepository;
    private final BranchScheduleRepository branchScheduleRepository;
    private final ProductMapper productMapper;

    @Value("${stripe.secret-key}")
    private String secretKey;

    @Override
    public ApiResult<CustomPage<OrderRespDto>> getNewOrders(Integer page, Integer size) {
        UserDto userDto = CommonUtils.getCurrentUser();

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt")));
        Page<Order> newOrders = orderRepository.findAllByClientIdAndStatus(userDto.getId(), OrderStatusEnum.NEW, pageRequest);
        return ApiResult.successResponse(makeCustomPage(newOrders));
    }

    @Override
    public ApiResult<CustomPage<OrderRespDto>> getSuccessOrders(Integer page, Integer size) {
        UserDto userDto = CommonUtils.getCurrentUser();

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt")));
        Page<Order> successOrders = orderRepository.findAllByClientIdAndStatus(userDto.getId(), OrderStatusEnum.SUCCESS, pageRequest);
        return ApiResult.successResponse(makeCustomPage(successOrders));
    }

    @Override
    public ApiResult<?> getReadyOrders(Integer page, Integer size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt")));

        Page<Order> orderPage = orderRepository.findAllByStatusAndCourierIsNull(OrderStatusEnum.READY, pageRequest);
        return ApiResult.successResponse(makeCustomPage(orderPage));
    }

    @Override
    public ApiResult<CustomPage<OrderRespDto>> getOperatorOrders(Integer page, Integer size) {
        UserDto userDto = CommonUtils.getCurrentUser();
        User operator = userRepository.findById(userDto.getId()).orElseThrow(() -> RestException.notFound("USER"));

        if (operator.getBranches().isEmpty())
            throw RestException.restThrow(MessageService.getMessage("NO_WORK_BRANCH"), HttpStatus.BAD_REQUEST);

        Branch branch = operator.getBranches().stream().findFirst().get();

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt")));
        Page<Order> orderPage = orderRepository.findAllByBranchIdAndStatus(branch.getId(), OrderStatusEnum.NEW, pageRequest);
        return ApiResult.successResponse(makeCustomPage(orderPage));
    }


    @Override
    public ApiResult<ToOrderRespDto> toOrder() {
        UserDto userDto = CommonUtils.getCurrentUser();

        Order order = orderRepository.findByClientBasket(userDto.getId(), OrderStatusEnum.BASKET).orElseThrow(() -> RestException.notFound("BASKET"));
        Double totalSum = order.getTotalSum();
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        List<OrderProduct> orderProducts = orderProductRepository.findByOrderId(order.getId());
        Set<UUID> productsId = orderProducts.stream().map(orderProduct -> orderProduct.getProduct().getId()).collect(Collectors.toSet());

        SimplePromotion simplePromotion =
                simplePromotionRepository.findActiveSimplePromotion(currentTime, totalSum).orElse(null);
        SimplePromotionDto simplePromotionDto = null;
        if (Objects.nonNull(simplePromotion)) {
            simplePromotionDto = new SimplePromotionDto(
                    simplePromotion.getId(),
                    simplePromotion.getPromotion().getName(),
                    simplePromotion.getPercent());
        }

        DeliveryPromotion deliveryPromotion = deliveryPromotionRepository.findActiveDeliveryPromotion(currentTime, totalSum).orElse(null);
        DeliveryPromotionDto deliveryPromotionDto = null;
        if (Objects.nonNull(deliveryPromotion)) {
            deliveryPromotionDto = new DeliveryPromotionDto(
                    deliveryPromotion.getPromotion().getName(),
                    deliveryPromotion.getStartTime(),
                    deliveryPromotion.getEndTime()
            );
        }

        QuantityPromotion quantityPromotion = quantityPromotionRepository.findActiveQuantityPromotions(currentTime, productsId).orElse(null);
        QuantityPromotionDto quantityPromotionDto = null;
        if (Objects.nonNull(quantityPromotion)) {
            for (OrderProduct orderProduct : orderProducts) {
                if (orderProduct.getProduct().getId().equals(quantityPromotion.getPurchasedProduct().getId())
                        && orderProduct.getQuantity() >= quantityPromotion.getPurchasedCount()) {
                    Product product = quantityPromotion.getBonusProduct();
                    quantityPromotionDto = new QuantityPromotionDto(
                            quantityPromotion.getId(),
                            productMapper.toProductRespDto(quantityPromotion.getBonusProduct()),
                            quantityPromotion.getBonusCount()
                    );
                }
            }
        }

        List<GroupPromotionProduct> promotionProducts = groupPromotionProductRepository.findPromotionProducts(currentTime, totalSum);
        List<GroupPromotionDto> groupPromotionsDto = promotionProducts.stream().map(groupPromotionProduct -> new GroupPromotionDto(
                groupPromotionProduct.getGroupPromotion().getId(),
                groupPromotionProduct.getName(),
                productMapper.toProductRespDto(groupPromotionProduct.getProduct()),
                groupPromotionProduct.getQuantity()
        )).collect(Collectors.toList());

        PromotionDto promotionDto = new PromotionDto(
                simplePromotionDto,
                deliveryPromotionDto,
                quantityPromotionDto,
                groupPromotionsDto
        );

        OrderRespDto orderRespDto = toOrderRespDto(order);

        double discountPrice = calculateDiscount(orderProducts);
        return ApiResult.successResponse(new ToOrderRespDto(promotionDto, orderRespDto, discountPrice));
    }

    @Override
    public CustomPage<OrderRespDto> makeCustomPage(Page<Order> orders) {
        List<OrderRespDto> orderRespDto = orders.stream().map(this::toOrderRespDto).collect(Collectors.toList());

        return new CustomPage<>(
                orderRespDto,
                orders.getNumberOfElements(),
                orders.getNumber(),
                orders.getTotalElements(),
                orders.getTotalPages(),
                orders.getSize()
        );
    }

    @Override
    @Transactional
    public ApiResult<?> confirm(ConfirmOrderDto orderDto) {
        UserDto currentUserDto = CommonUtils.getCurrentUser();
        User user = userRepository.findById(currentUserDto.getId()).orElseThrow(() -> RestException.notFound("USER"));

        Order basket = orderRepository.findByClientBasket(currentUserDto.getId(), OrderStatusEnum.BASKET).orElseThrow(() -> RestException.notFound("BASKET"));
        List<OrderProduct> orderProductList = orderProductRepository.findByOrderId(basket.getId());

        if (orderProductList.isEmpty())
            throw RestException.restThrow(MessageService.getMessage("BASKET_IS_EMPTY"), HttpStatus.BAD_REQUEST);

        Double totalSum = basket.getTotalSum();
        Double totalDiscountPrice = calculateDiscount(orderProductList);

        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        Time currentHour = new Time(Objects.isNull(orderDto.getReceiveTime()) ? System.currentTimeMillis() : orderDto.getReceiveTime());

        List<OrderPromotion> orderPromotions = new ArrayList<>();

        // Simple promotion
        double totalSimplePromotionPrice = 0d;
        Optional<SimplePromotion> optionalSimplePromotion = simplePromotionRepository.findActiveSimplePromotion(currentTime, totalSum);
        if (optionalSimplePromotion.isPresent()) {
            SimplePromotion simplePromotion = optionalSimplePromotion.get();
            totalSimplePromotionPrice = totalSum / 100d * simplePromotion.getPercent();
            orderPromotions.add(
                    new OrderPromotion(
                            simplePromotion.getPromotion(),
                            basket,
                            totalSimplePromotionPrice,
                            simplePromotion.getPercent()
                    )
            );
        }

        // Delivery promotion
        Double deliveryPrice = 0d;
        if (orderDto.getDelivered()) {
            basket.setDelivery(orderDto.getDelivered());
            if (Objects.isNull(orderDto.getLat()) || Objects.isNull(orderDto.getLon()))
                throw RestException.restThrow(MessageService.getMessage("ADDRESS_REQUIRED"), HttpStatus.BAD_REQUEST);
            BranchRespDto branchDto = branchFeign.getNearly(orderDto.getLat(), orderDto.getLon()).orElseThrow(() -> RestException.restThrow(MessageService.getMessage("NEAR_BRANCH_NOT_FOUND"), HttpStatus.NOT_FOUND));
            Branch branch = branchRepository.findById(branchDto.getId()).orElseThrow(() -> RestException.notFound("BRANCH"));

            basket.setBranch(branch);

            Address branchAddress = branch.getAddress();
            double distance = CommonService.distance(branchAddress.getLat(), branchAddress.getLon(), orderDto.getLat(), orderDto.getLon());
            deliveryPrice = calculateDeliveryPrice(branch, distance);

            Optional<DeliveryPromotion> optionalDeliveryPromotion = deliveryPromotionRepository.findActiveDeliveryPromotionWithDeliveryTime(currentTime, totalSum, currentHour);
            if (optionalDeliveryPromotion.isPresent()) {
                DeliveryPromotion deliveryPromotion = optionalDeliveryPromotion.get();
                deliveryPrice = 0d;
                orderPromotions.add(
                        new OrderPromotion(
                                deliveryPromotion.getPromotion(),
                                basket,
                                deliveryPrice,
                                null
                        )
                );
            }
            basket.setDeliveryPrice(deliveryPrice);
        } else {
            Branch branch = branchRepository.findByIdAndActiveIsTrue(orderDto.getBranchId()).orElseThrow(() -> RestException.notFound("BRANCH"));
            boolean freeTime = branchScheduleRepository.existsAllByBranchIdAndWeekdaysNameEnumAndStar(branch.getId(), CommonService.getCurrentWeekdayName().name(), new Time(orderDto.getReceiveTime()));
            if (!freeTime)
                throw RestException.restThrow(MessageService.getMessage("NO_BRANCH_WORK_TIME"), HttpStatus.CONFLICT);

            basket.setBranch(branch);
        }


        List<PromotionProduct> promotionProducts = new ArrayList<>();

        // Quantity Promotion
        Set<UUID> orderProductIdList = orderProductList.stream().map(o -> o.getProduct().getId()).collect(Collectors.toSet());
        Optional<QuantityPromotion> optionalQuantityPromotion = quantityPromotionRepository.findActiveQuantityPromotions(currentTime, orderProductIdList);
        if (optionalQuantityPromotion.isPresent()) {
            QuantityPromotion quantityPromotion = optionalQuantityPromotion.get();
            for (OrderProduct orderProduct : orderProductList) {
                if (orderProduct.getProduct().equals(quantityPromotion.getPurchasedProduct()) && orderProduct.getQuantity().equals(quantityPromotion.getPurchasedCount())) {
                    OrderPromotion orderPromotion = new OrderPromotion(
                            quantityPromotion.getPromotion(),
                            basket,
                            null,
                            null
                    );
                    orderPromotions.add(orderPromotion);

                    promotionProducts.add(new PromotionProduct(orderPromotion, quantityPromotion.getBonusProduct(), quantityPromotion.getBonusCount(), null));
                    break;
                }
            }
        }


        // Group promotion
        List<Product> bonusGroupProducts = new ArrayList<>();
        if (orderDto.getGroupPromotion() != null) {
            GroupPromotionDto groupPromotionDto = orderDto.getGroupPromotion();
            Promotion promotion = promotionRepository.findById(groupPromotionDto.getId()).orElseThrow(() -> RestException.notFound("PROMOTION"));

            List<GroupPromotionProduct> groupPromotionProducts = groupPromotionProductRepository.findPromotionProducts(currentTime, totalSum);

            if (!promotion.getId().equals(groupPromotionProducts.get(1).getGroupPromotion().getPromotion().getId()))
                throw RestException.attackResponse();

            Double groupPromotionPrice = 0d;
            Integer groupPromotionQuantity = 0;
            for (GroupPromotionProduct groupPromotionProduct : groupPromotionProducts) {
                if (groupPromotionProduct.getName() != null && groupPromotionDto.getGroupNumber().equals(groupPromotionProduct.getName())) {
                    bonusGroupProducts.add(groupPromotionProduct.getProduct());
                    groupPromotionPrice = groupPromotionProduct.getGroupPromotion().getPrice();
                    groupPromotionQuantity = groupPromotionProduct.getQuantity();
                } else if (groupPromotionDto.getProductRespDto().getId().equals(groupPromotionProduct.getProduct().getId())) {
                    bonusGroupProducts.add(groupPromotionProduct.getProduct());
                    groupPromotionPrice = groupPromotionProduct.getGroupPromotion().getPrice();
                    groupPromotionQuantity = groupPromotionProduct.getQuantity();
                    break;
                }
            }
            OrderPromotion orderPromotion = new OrderPromotion(
                    promotion,
                    basket,
                    groupPromotionPrice,
                    null
            );
            orderPromotions.add(orderPromotion);

            for (Product bonusGroupProduct : bonusGroupProducts) {
                promotionProducts.add(new PromotionProduct(orderPromotion, bonusGroupProduct, groupPromotionQuantity, groupPromotionDto.getGroupNumber()));

            }
        }

        orderPromotionRepository.saveAll(orderPromotions);
        promotionProductRepository.saveAll(promotionProducts);

        double paymentSumma = (totalSum + deliveryPrice) - (totalDiscountPrice + totalSimplePromotionPrice);
        PayType payType = payTypeRepository.findById(orderDto.getPayTypeId()).orElseThrow(() -> RestException.notFound("PAY_TYPE"));

        switch (payType.getName()) {
            case STRIPE: {
                String stripeToken = orderDto.getStripeToken();
                if (Objects.isNull(stripeToken)) throw RestException.restThrow("PAY_FIRST", HttpStatus.BAD_REQUEST);
                Charge charge = paymentService.payWithStripe(paymentSumma, stripeToken);
                OrderPayment orderPayment = new OrderPayment(basket, payType, paymentSumma, PaymentStatusEnum.PENDING, charge.getId());
                orderPaymentRepository.save(orderPayment);
                break;
            }
            case CASH: {
                System.out.println();
            }
        }

        basket.setPayType(payType);
        double confidenceRate = calculateConfidenceRate(currentUserDto.getId());

        if (confidenceRate >= 60d) {
            basket.setStatus(OrderStatusEnum.PENDING);
        } else {
            basket.setStatus(OrderStatusEnum.NEW);
        }
        basket.setDescription(orderDto.getDescription());
        orderRepository.save(basket);
        orderHistoryRepository.save(new OrderHistory(basket, basket.getStatus(), currentTime, user));

        return ApiResult.successResponse("ORDER_SUCCESSFULLY_CONFIRMED");
    }

    @Override
    @Transactional
    public ApiResult<?> cancelOrder(UUID orderId) {
        UserDto currentUser = CommonUtils.getCurrentUser();
        User user = userRepository.findById(currentUser.getId()).orElseThrow(() -> RestException.notFound("USER"));

        Order order = orderRepository.findByClientIdAndId(currentUser.getId(), orderId).orElseThrow(() -> RestException.notFound("ORDER"));

        if (!order.getStatus().equals(OrderStatusEnum.NEW))
            throw RestException.restThrow(MessageService.getMessage("CANCELLING_ORDER_IMPOSSIBLE"), HttpStatus.BAD_REQUEST);

        Optional<OrderPayment> optionalPayment = orderPaymentRepository.findByOrderId(orderId);
        if (optionalPayment.isPresent()) {
            OrderPayment payment = optionalPayment.get();
            PayType payType = payment.getPayType();
            switch (payType.getName()) {
                case STRIPE: {
                    Refund refund = paymentService.cancelPaymentWithStripe(payment.getChargeId());
                }
                case PAYME: {
                }
            }
            payment.setPaymentStatus(PaymentStatusEnum.REFUND);
            orderPaymentRepository.save(payment);
        }

        order.setStatus(OrderStatusEnum.CANCELED);
        orderRepository.save(order);
        orderHistoryRepository.save(new OrderHistory(order, OrderStatusEnum.CANCELED, new Timestamp(System.currentTimeMillis()), user));


        return new ApiResult<>(MessageService.getMessage("ORDER_SUCCESSFULLY_CANCELLED"));
    }

    @Override
    @Transactional
    public ApiResult<?> toPending(UUID id) {
        UserDto currentUser = CommonUtils.getCurrentUser();
        User user = userRepository.findById(currentUser.getId()).orElseThrow(() -> RestException.notFound("USER"));

        Order order = orderRepository.findById(id).orElseThrow(() -> RestException.notFound("ORDER"));
        if (!order.getStatus().equals(OrderStatusEnum.NEW))
            throw RestException.restThrow(MessageService.getMessage("ORDER_STATUS_MUST_BE_NEW"), HttpStatus.BAD_REQUEST);

        order.setOperator(user);
        order.setStatus(OrderStatusEnum.PENDING);
        orderRepository.save(order);

        orderHistoryRepository.save(new OrderHistory(order, OrderStatusEnum.PENDING, new Timestamp(System.currentTimeMillis()), user));

        return ApiResult.successResponse(MessageService.successEdit("ORDER"));
    }

    @Override
    public ApiResult<CustomPage<OrderRespDto>> getPending(Integer page, Integer size) {
        UserDto userDto = CommonUtils.getCurrentUser();

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt")));
        Page<Order> newOrders = orderRepository.findAllByClientIdAndStatus(userDto.getId(), OrderStatusEnum.PENDING, pageRequest);
        return ApiResult.successResponse(makeCustomPage(newOrders));
    }

    @Override
    public ApiResult<?> toProcess(UUID id) {
        UserDto currentUser = CommonUtils.getCurrentUser();
        User user = userRepository.findById(currentUser.getId()).orElseThrow(() -> RestException.notFound("USER"));

        Order order = orderRepository.findById(id).orElseThrow(() -> RestException.notFound("ORDER"));
        if (!order.getStatus().equals(OrderStatusEnum.PENDING))
            throw RestException.restThrow(MessageService.getMessage("ORDER_STATUS_MUST_BE_PENDING"), HttpStatus.BAD_REQUEST);

        order.setStatus(OrderStatusEnum.IN_PROCESS);
        orderRepository.save(order);

        orderHistoryRepository.save(new OrderHistory(order, OrderStatusEnum.IN_PROCESS, new Timestamp(System.currentTimeMillis()), user));

        return ApiResult.successResponse(MessageService.successEdit("ORDER"));
    }

    @Override
    @Transactional
    public ApiResult<?> toReady(UUID id) {
        UserDto currentUser = CommonUtils.getCurrentUser();
        User user = userRepository.findById(currentUser.getId()).orElseThrow(() -> RestException.notFound("USER"));

        Order order = orderRepository.findById(id).orElseThrow(() -> RestException.notFound("ORDER"));
        if (!order.getStatus().equals(OrderStatusEnum.IN_PROCESS))
            throw RestException.restThrow(MessageService.getMessage("ORDER_STATUS_MUST_BE_PROCESS"), HttpStatus.BAD_REQUEST);

        order.setStatus(OrderStatusEnum.READY);

        // todo courier topishning eng optimal yo'lini topish kk
        findAndAttachCourier(order);

        orderRepository.save(order);

        orderHistoryRepository.save(new OrderHistory(order, OrderStatusEnum.READY, new Timestamp(System.currentTimeMillis()), user));

        return ApiResult.successResponse(MessageService.successEdit("ORDER"));
    }


    @Override
    public ApiResult<?> attachCourier(AttachCourierDto attachCourierDto) {
        Order order = orderRepository.findByIdAndStatusAndCourierIsNull(attachCourierDto.getOrderId(), OrderStatusEnum.READY)
                .orElseThrow(() -> RestException.notFound("ORDER"));

        User courier = userRepository.findByIdAndRole_RoleTypeAndOnlineIsTrue(attachCourierDto.getCourierId(), RoleTypeEnum.COURIER)
                .orElseThrow(() -> RestException.notFound("COURIER"));

        order.setCourier(courier);

        orderRepository.save(order);
        return ApiResult.successResponse(MessageService.successEdit("ORDER"));

    }

    @Override
    public ApiResult<?> toWay() {
        UserDto currentUser = CommonUtils.getCurrentUser();
        User user = userRepository.findById(currentUser.getId()).orElseThrow(() -> RestException.notFound("USER"));

        List<Order> orderList = orderRepository.findAllByCourierIdAndStatus(user.getId(), OrderStatusEnum.READY);

        if (orderList.isEmpty())
            throw RestException.restThrow(MessageService.getMessage("YOU_HAVE_NOT_ORDERS"), HttpStatus.BAD_REQUEST);

        List<OrderHistory> orderHistoryList = new ArrayList<>();
        for (Order order : orderList) {
            order.setStatus(OrderStatusEnum.ON_THE_WAY);
            orderHistoryList.add(new OrderHistory(order, OrderStatusEnum.ON_THE_WAY, new Timestamp(System.currentTimeMillis()), user));
        }
        orderRepository.saveAll(orderList);
        orderHistoryRepository.saveAll(orderHistoryList);

        return ApiResult.successResponse(MessageService.successEdit("ORDER"));
    }

    @Override
    @SneakyThrows
    @Transactional
    public ApiResult<?> toSuccess(UUID id) {
        UserDto currentUser = CommonUtils.getCurrentUser();
        User user = userRepository.findById(currentUser.getId()).orElseThrow(() -> RestException.notFound("USER"));

        Order order = orderRepository.findById(id).orElseThrow(() -> RestException.notFound("ORDER"));

        if (!(Objects.equals(order.getClient().getId(), user.getId())
                || Objects.equals(order.getOperator().getId(), user.getId())
                || Objects.equals(order.getCourier().getId(), user.getId()))) {
            throw RestException.attackResponse();
        }

        if (Objects.nonNull(order.getCourier())){
            order.getCourier().setOnline(true);
            userRepository.save(order.getCourier());
        }

        switch (order.getPayType().getName()) {
            case STRIPE: {
                Stripe.apiKey = secretKey;
                OrderPayment orderPayment = orderPaymentRepository.findByOrderId(order.getId()).orElseThrow(() -> RestException.notFound("PAYMENT"));
                Charge charge = Charge.retrieve(orderPayment.getChargeId());
                charge.capture();
                orderPayment.setPaymentStatus(PaymentStatusEnum.SUCCESS);
                orderPaymentRepository.save(orderPayment);
                break;
            }
            case CASH: {
                orderPaymentRepository.save(new OrderPayment(order, order.getPayType(), order.getTotalSum(), PaymentStatusEnum.SUCCESS));
                break;
            }
        }

        order.setStatus(OrderStatusEnum.SUCCESS);
        orderRepository.save(order);
        orderHistoryRepository.save(new OrderHistory(order, OrderStatusEnum.SUCCESS, new Timestamp(System.currentTimeMillis()), user));

        return ApiResult.successResponse(MessageService.getMessage("ORDER_SUCCESSFUL"));
    }

    private void findAndAttachCourier(Order order) {
        Branch branch = order.getBranch();
        if (branch.isAutoDistribution() && order.isDelivery()) {
            Optional<User> optionalCourier = userRepository.getNearlyCourier(branch.getAddress().getLat(), branch.getAddress().getLon());
            optionalCourier.ifPresent(order::setCourier);
        }
    }

    private Double calculateDiscount(List<OrderProduct> orderProducts) {
        double discountPrice = 0D;
        for (OrderProduct orderProduct : orderProducts) {
            Discount discount = orderProduct.getProduct().getDiscount();
            if (Objects.nonNull(discount) && CommonService.validateDiscount(discount)) {
                if (Objects.nonNull(discount.getPrice())) {
                    discountPrice += discount.getPrice() * orderProduct.getQuantity();
                } else {
                    discountPrice += (orderProduct.getPrice() * discount.getPercent() / 100) * orderProduct.getQuantity();
                }
            }
        }
        return discountPrice;
    }

    private Double calculateSimplePromotion(double totalPrice) {
        Optional<SimplePromotion> optionalSimplePromotion = simplePromotionRepository.findActiveSimplePromotion(new Timestamp(System.currentTimeMillis()), totalPrice);
        if (optionalSimplePromotion.isPresent()) {
            SimplePromotion simplePromotion = optionalSimplePromotion.get();
            return totalPrice / 100d * simplePromotion.getPercent();
        }
        return 0D;
    }

    private OrderRespDto toOrderRespDto(Order order) {
        Branch branch = order.getBranch();

        Address address = order.getAddress();
        OrderAddressDto addressDto = null;
        if (Objects.nonNull(address)) {
            addressDto = new OrderAddressDto(address.getId(), address.getName(), address.getLat(), address.getLon());
        }

        User courier = order.getCourier();
        OrderCourierDto orderCourierDto = null;
        if (Objects.nonNull(courier)) {
            orderCourierDto = new OrderCourierDto(courier.getId(), courier.getFullName(), courier.getPhoneNumber());
        }

        List<OrderProduct> orderProducts = orderProductRepository.findByOrderId(order.getId());
        List<OrderProductDto> orderProductsDto = orderProducts.stream().map(orderProduct ->
                        new OrderProductDto(
                                orderProduct.getProduct().getId(),
                                orderProduct.getProduct().getPhoto().getId(),
                                orderProduct.getProduct().getName(),
                                orderProduct.getPrice(),
                                orderProduct.getQuantity()
                        ))
                .collect(Collectors.toList());

        StringBuilder comment = new StringBuilder();
        for (OrderRate orderRate : orderRateRepository.findAllByOrderId(order.getId())) {
            comment.append(orderRate.getOrderRate()).append(": ").append(orderRate.getComment()).append("\n");
        }

        OrderPayment orderPayment = orderPaymentRepository.findByOrderId(order.getId()).orElse(null);

        return new OrderRespDto(
                order.getId(),
                order.getReceiveTime(),
                Objects.isNull(branch) ? null : order.getBranch().getId(),
                Objects.isNull(branch) ? null : order.getBranch().getName(),
                addressDto,
                orderCourierDto,
                orderProductsDto,
                comment.toString(),
                order.getDeliveryPrice(),
                order.getTotalSum(),
                Objects.nonNull(orderPayment) ? orderPayment.getPayType().getName() : null
        );
    }

    private double calculateDeliveryPrice(Branch branch, double distance) {
        List<DeliveryTariff> deliveryTariffList = deliveryTariffRepository.findAllByBranchIdOrderByDistance(branch.getId());
        if (deliveryTariffList.isEmpty())
            return 0d;

        double deliveryPrice = 0d;
        DeliveryTariff minimumTariff = deliveryTariffList.stream().filter(DeliveryTariff::isMinimum).findAny().get();
        deliveryPrice += minimumTariff.getPrice();

        DeliveryTariff otherTariff = deliveryTariffList.stream().filter(o -> !o.isMinimum()).findAny().get();

        if (distance > minimumTariff.getDistance()) {
            deliveryPrice += (distance - minimumTariff.getDistance()) / otherTariff.getDistance() * otherTariff.getPrice();
        }
        return deliveryPrice;
    }

    private double calculateConfidenceRate(UUID userId) {
        try {
            long successOrderCount = orderRepository.countByClientIdAndStatus(userId, OrderStatusEnum.SUCCESS);
            long cancelledOrderCount = orderRepository.countByClientIdAndStatus(userId, OrderStatusEnum.CANCELED);
            long confidenceRate = (successOrderCount / (successOrderCount + cancelledOrderCount)) * 100;
            return (double) confidenceRate;
        } catch (Exception e) {
            return 0;
        }
    }
}
