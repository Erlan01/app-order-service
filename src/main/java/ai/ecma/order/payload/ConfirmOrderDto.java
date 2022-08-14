package ai.ecma.order.payload;

import ai.ecma.lib.payload.resp.GroupPromotionDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * This class not documented :(
 *
 * @author Muhammad Mo'minov
 * @since 31.01.2022
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ConfirmOrderDto {
//    @NotNull
//    private UUID orderId;

    @NotNull
    private Long payTypeId;

    private String stripeToken;

    @NotNull
    private Boolean delivered;

    private Long branchId;

    @NotNull
    private Double lat;

    @NotNull
    private Double lon;

    private Long receiveTime;

    private String description;

    private GroupPromotionDto groupPromotion;


}
