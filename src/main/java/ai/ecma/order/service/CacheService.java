package ai.ecma.order.service;

import ai.ecma.lib.payload.UserDto;
import ai.ecma.order.exception.RestException;
import ai.ecma.order.feign.AuthFeign;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * This class not documented :(
 *
 * @author Muhammad Mo'minov
 * @since 17.02.2022
 */
@Service
@RequiredArgsConstructor
public class CacheService {
    private final AuthFeign authFeign;

    @Cacheable(value = "users", key = "#token")
    public UserDto getUserByToken(String token) {
        return authFeign.checkAuth(token).orElseThrow(RestException::forbidden);
    }

}
