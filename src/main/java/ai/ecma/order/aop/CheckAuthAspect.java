package ai.ecma.order.aop;

import ai.ecma.lib.enums.PermissionEnum;
import ai.ecma.lib.enums.RoleTypeEnum;
import ai.ecma.lib.payload.ApiResult;
import ai.ecma.lib.payload.CheckAuthDto;
import ai.ecma.lib.payload.UserDto;
import ai.ecma.order.exception.RestException;
import ai.ecma.order.feign.AuthFeign;
import ai.ecma.order.service.CacheService;
import ai.ecma.order.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class not documented :(
 *
 * @author Muhammad Mo'minov
 * @since 21.01.2022
 */
@Aspect
@Order(1)
@Component
@RequiredArgsConstructor
public class CheckAuthAspect {
    private final CacheService cacheService;

    @Before(value = "@annotation(checkAuth)")
    public void checkAuthExecutor(CheckAuth checkAuth) {
        check(checkAuth);
    }

    public void check(CheckAuth checkAuth) {
        PermissionEnum[] permissions = checkAuth.permissions();
        RoleTypeEnum[] roleTypeEnums = checkAuth.roles();
        Set<String> authoritiesForCheck = Arrays.stream(permissions).map(PermissionEnum::name).collect(Collectors.toSet());
        authoritiesForCheck.addAll(Arrays.stream(roleTypeEnums).map(RoleTypeEnum::name).collect(Collectors.toSet()));

        HttpServletRequest httpServletRequest = CommonUtils.getCurrentRequest();

        String token = httpServletRequest.getHeader("Authorization");
        if (token == null) throw RestException.forbidden();

        UserDto userDto = cacheService.getUserByToken(token);

        if (!authoritiesForCheck.isEmpty()) {
            Set<String> userAuthorities = userDto.getAuthorities();

            boolean exists = false;
            for (String authority : authoritiesForCheck) {
                exists = userAuthorities.contains(authority);
            }

            if (!exists) throw RestException.forbidden();
        }

        httpServletRequest.setAttribute("user", userDto);
    }
}
