package ai.ecma.order.controller;

import ai.ecma.order.feign.ProductFeign;
import ai.ecma.order.utils.AppConstant;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * This class not documented :(
 *
 * @author Muhammad Mo'minov
 * @since 20.01.2022
 */
@RestController
@RequestMapping(AppConstant.BASE_PATH + "/test")
@RequiredArgsConstructor
public class TestController {
    private final ProductFeign productFeign;

    @GetMapping
    public String test() {
//        return "Welcome to order service!";
        return productFeign.test();
    }


}
