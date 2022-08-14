package ai.ecma.order.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttachCourierDto {

    @NotNull
    private UUID courierId;

    @NotNull
    private UUID orderId;
}
