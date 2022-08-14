package ai.ecma.order.common;

import ai.ecma.lib.entity.PayType;
import ai.ecma.lib.enums.PayTypeEnum;
import ai.ecma.lib.repository.AttachmentRepository;
import ai.ecma.lib.repository.OrderRepository;
import ai.ecma.lib.repository.PayTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * @author Osiyo Adilova
 * @project app-eticket-server
 * @since 12/16/2021
 */

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final OrderRepository orderRepository;
    private final PayTypeRepository payTypeRepository;
    private final AttachmentRepository attachmentRepository;

    @Value("${spring.sql.init.mode}")
    private String initialMode;

    @Override
    public void run(String... args) {
        if (initialMode.equals("always")) {
            createPayTypes();
            orderRepository.createDistanceCalculatorFunc();
        } else {

        }

        System.err.printf("Sql init mode is %s \n", initialMode);
    }

    public void createPayTypes() {
        PayTypeEnum[] payTypeEnums = PayTypeEnum.values();

        List<PayType> payTypeList =
                LongStream.range(0, payTypeEnums.length)
                        .mapToObj(i -> new PayType(payTypeEnums[(int) i], true, attachmentRepository.getById(i + 1)))
                        .collect(Collectors.toList());

        payTypeRepository.saveAll(payTypeList);
    }
}
