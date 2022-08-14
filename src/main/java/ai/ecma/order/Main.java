package ai.ecma.order;

import ai.ecma.lib.enums.WeekdaysNameEnum;
import ai.ecma.lib.payload.req.CardDto;
import com.stripe.Stripe;
import com.stripe.model.Charge;
import com.stripe.model.Token;
import com.stripe.param.ChargeCreateParams;
import com.stripe.param.TokenCreateParams;
import lombok.SneakyThrows;

import java.util.Arrays;
import java.util.Calendar;

/**
 * @author Murtazayev Muhammad
 * @since 28.01.2022
 */
public class Main {
    public static void main(String[] args) {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);

        WeekdaysNameEnum[] values = WeekdaysNameEnum.values();
        System.out.println(day);
        System.out.println(Arrays.toString(values));
        System.out.println(values[--day]);


//        String token = createToken(new CardDto("4242424242424242", "12", "22", "123"));
//        payWithStripe(14, token);
//        System.out.println(new Time(System.currentTimeMillis()));
    }

    @SneakyThrows
    public static Charge payWithStripe(double amount, String token) {
        Stripe.apiKey = "sk_test_51JkAobLKO4a53BDZfp9GpkCGpBqM2an5RW9zDpbJcCbCpZCIcBf5wdD44Cnkq8KiBMkbqG7gy7DAw1DY07rS7tVb00g7927C5M";

        ChargeCreateParams createParams = ChargeCreateParams.builder()
                .setAmount((long) (amount * 100))
                .setCurrency("usd")
                .setSource(token)
                .setCapture(false)
                .build();

        Charge charge = Charge.create(createParams);
        return charge;
    }

    @SneakyThrows
    public static String createToken(CardDto cardDto) {
        Stripe.apiKey = "sk_test_51JkAobLKO4a53BDZfp9GpkCGpBqM2an5RW9zDpbJcCbCpZCIcBf5wdD44Cnkq8KiBMkbqG7gy7DAw1DY07rS7tVb00g7927C5M";

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
        return token.getId();
    }

}
