package ai.ecma.order.exception;

import ai.ecma.lib.payload.ApiResult;
import com.google.gson.Gson;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

/**
 * This class not documented :(
 *
 * @author Muhammad Mo'minov
 * @since 24.01.2022
 */
@Component
@RequiredArgsConstructor
public class FeignErrorDecoder implements ErrorDecoder {
    private final Gson gson;

    @Override
    public Exception decode(final String s, final Response response) {
        String body = getBody(response.body());
        ApiResult apiResult = gson.fromJson(body, ApiResult.class);
        throw RestException.restThrow(apiResult.getErrors(), HttpStatus.valueOf(response.status()));
    }

    @SneakyThrows
    private String getBody(Response.Body body) {
        byte[] bytes = StreamUtils.copyToByteArray(body.asInputStream());
        return new String(bytes);
    }
}
