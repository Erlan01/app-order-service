package ai.ecma.order.filter;

import ai.ecma.lib.payload.ApiResult;
import ai.ecma.lib.payload.ErrorData;
import ai.ecma.order.common.MessageService;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class Filter extends OncePerRequestFilter {
    private final Gson gson;

    private final Map<String, String> clients = Map.of(
            "gatewayServiceUsername", "gatewayServicePassword",
            "authServiceUsername", "authServicePassword",
            "productServiceUsername", "productServicePassword",
            "branchServiceUsername", "branchServicePassword"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String requestUsername = request.getHeader("serviceUsername");
        String requestPassword = request.getHeader("servicePassword");

        if (!checkUsernameAndPassword(requestUsername, requestPassword)) {
            ApiResult<Object> apiResult = new ApiResult<>(false, List.of(new ErrorData(MessageService.getMessage("FORBIDDEN"), 403)));

            response.getWriter().write(gson.toJson(apiResult));
            response.setStatus(403);
            response.setContentType("application/json");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean checkUsernameAndPassword(String requestUsername, String requestPassword) {
        try {
            String password = clients.get(requestUsername);
            return Objects.equals(requestPassword, password);
        } catch (Exception e) {
            return false;
        }
    }

}
