package ai.ecma.order.service;

import com.stripe.model.Charge;
import com.stripe.model.Refund;

public interface PaymentService {
    Charge payWithStripe(double amount, String token);

    Refund cancelPaymentWithStripe(String chargeId);
}
