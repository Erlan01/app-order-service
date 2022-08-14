package ai.ecma.order.common;

import ai.ecma.lib.entity.Order;
import ai.ecma.lib.entity.OrderHistory;
import ai.ecma.lib.entity.User;
import ai.ecma.lib.enums.OrderStatusEnum;
import ai.ecma.lib.repository.OrderHistoryRepository;
import ai.ecma.lib.repository.OrderRepository;
import ai.ecma.lib.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * This class not documented :(
 *
 * @author Muhammad Mo'minov
 * @since 04.01.2022
 */
@EnableScheduling
@Component
@RequiredArgsConstructor
public class ScheduledComponent {
    private final OrderRepository orderRepository;
    private final OrderHistoryRepository orderHistoryRepository;
    private final UserRepository userRepository;

    @Value("${app.order.ready-wait-time}")
    private Long readyWaitTime;

    @Scheduled(fixedRate = 1000 * 60)
    private void orderToWay(){
        Timestamp pastTime = new Timestamp(System.currentTimeMillis() - readyWaitTime);
        List<User> kuniBitganCouriers = userRepository.getKuniBitganCouriers(pastTime);

        Set<UUID> uuidSet = kuniBitganCouriers.stream().map(User::getId).collect(Collectors.toSet());

        List<Order> orderList = orderRepository.findAllByCourierIdInAndStatus(uuidSet, OrderStatusEnum.READY);

        List<OrderHistory> orderHistoryList = new ArrayList<>();
        for (Order order : orderList) {
            order.setStatus(OrderStatusEnum.ON_THE_WAY);
            orderHistoryList.add(new OrderHistory(order, OrderStatusEnum.ON_THE_WAY, new Timestamp(System.currentTimeMillis()), null));
        }
        orderRepository.saveAll(orderList);
        orderHistoryRepository.saveAll(orderHistoryList);
    }

}
