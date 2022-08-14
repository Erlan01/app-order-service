package ai.ecma.order.service;

import ai.ecma.lib.entity.Attachment;
import ai.ecma.lib.entity.Order;
import ai.ecma.lib.entity.OrderRate;
import ai.ecma.lib.payload.ApiResult;
import ai.ecma.lib.payload.req.OrderRateReqDto;
import ai.ecma.lib.payload.resp.OrderRateRespDto;
import ai.ecma.lib.repository.AttachmentRepository;
import ai.ecma.lib.repository.OrderRateRepository;
import ai.ecma.lib.repository.OrderRepository;
import ai.ecma.order.exception.RestException;
import ai.ecma.order.mapper.OrderRateMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @AUTHOR: userdev
 * @DONE_ON: 2022/02/16
 */

@Service
@RequiredArgsConstructor
public class OrderRateServiceImpl implements OrderRateService {

    private final OrderRateRepository orderRateRepository;
    private final OrderRepository orderRepository;
    private final AttachmentRepository attachmentRepository;
    private final OrderRateMapper orderRateMapper;

    @Override
    public ApiResult<OrderRateRespDto> create(OrderRateReqDto rateReqDto) {
        Order order = orderRepository.findById(rateReqDto.getOrderId()).orElseThrow(() -> RestException.notFound("ORDER"));

        Attachment attachment = null;
        if (rateReqDto.getAttachmentId() != null) {
            attachment = attachmentRepository.findById(rateReqDto.getAttachmentId()).orElseThrow(() -> RestException.notFound("ATTACHMENT"));
        }

        OrderRate orderRate = new OrderRate(rateReqDto.getTitle(), rateReqDto.getRate(), order, rateReqDto.getComment(), attachment,
                rateReqDto.isDefect(), rateReqDto.getOrderRate());
        orderRate = orderRateRepository.save(orderRate);
        OrderRateRespDto orderRateRespDto = orderRateMapper.toOrderRateRespDto(orderRate);
        return ApiResult.successResponse(orderRateRespDto);
    }
}
