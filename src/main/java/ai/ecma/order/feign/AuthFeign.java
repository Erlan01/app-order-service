package ai.ecma.order.feign;

import ai.ecma.lib.payload.ApiResult;
import ai.ecma.lib.payload.CheckAuthDto;
import ai.ecma.lib.payload.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * This interface not documented :(
 *
 * @author Muhammad Mo'minov
 * @since 21.01.2022
 */
@FeignClient(name = "AUTH-SERVICE/api/auth")
public interface AuthFeign {
    @PostMapping("/v1/user/check-auth")
    ApiResult<UserDto> checkAuth(@RequestHeader String authorization);
}
