package ai.ecma.order.service;

import ai.ecma.order.exception.RestException;
import com.stripe.Stripe;
import com.stripe.model.Charge;
import com.stripe.model.Refund;
import com.stripe.param.ChargeCreateParams;
import com.stripe.param.RefundCreateParams;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PaymentServiceImpl implements PaymentService {
    @Value(value = "${stripe.secret-key}")
    private String secretKey;

    @Override
    @SneakyThrows
    public Charge payWithStripe(double amount, String token) {
        Stripe.apiKey = secretKey;

        ChargeCreateParams createParams = ChargeCreateParams.builder()
                .setAmount((long) (amount * 100))
                .setCurrency("uzs")
                .setSource(token)
                .setCapture(false)
                .build();

        return Charge.create(createParams);
    }

    @Override
    public Refund cancelPaymentWithStripe(String chargeId) {
        Stripe.apiKey = secretKey;
        try {
            Charge charge = Charge.retrieve(chargeId);
            RefundCreateParams refundCreateParams = RefundCreateParams.builder()
                    .setCharge(chargeId)
                    .build();

            Refund refund = Refund.create(refundCreateParams);
            return refund;
        } catch (Exception e) {
            e.printStackTrace();
            throw RestException.notFound("PAYMENT");
        }
    }

}
