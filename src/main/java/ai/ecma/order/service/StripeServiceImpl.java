package ai.ecma.order.service;

import ai.ecma.lib.payload.ApiResult;
import ai.ecma.lib.payload.req.CardDto;
import com.stripe.Stripe;
import com.stripe.model.Token;
import com.stripe.param.TokenCreateParams;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StripeServiceImpl implements StripeService {
    @Value(value = "${stripe.secret-key}")
    private String secretKey;

    @Override
    @SneakyThrows
    public ApiResult<?> createToken(CardDto cardDto) {
        Stripe.apiKey = secretKey;

        TokenCreateParams.Card card = TokenCreateParams.Card.builder()
                .setNumber(cardDto.getCardNumber())
                .setExpMonth(cardDto.getExpireMonth())
                .setExpYear("20" + cardDto.getExpireYear())
                .setCvc(cardDto.getCvc())
                .build();

        TokenCreateParams tokenCreateParams = TokenCreateParams.builder()
                .setCard(card)
                .build();

        Token token = Token.create(tokenCreateParams);
        return ApiResult.successResponse((Object) token.getId());
    }
}
