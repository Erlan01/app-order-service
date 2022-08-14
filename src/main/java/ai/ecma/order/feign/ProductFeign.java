package ai.ecma.order.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * This class not documented :(
 *
 * @author Muhammad Mo'minov
 * @since 20.01.2022
 */
@FeignClient(name = "PRODUCT-SERVICE/api/product/v1")
public interface ProductFeign {
    @GetMapping("/test")
    String test();


}
