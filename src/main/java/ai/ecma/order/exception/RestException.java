package ai.ecma.order.exception;

import ai.ecma.lib.payload.ErrorData;
import ai.ecma.order.common.MessageService;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * This class not documented :(
 *
 * @author Muhammad Mo'minov
 * @since 17.12.2021
 */

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RestException extends RuntimeException {

    private String message;

    private HttpStatus status;

    private List<ErrorData> errorData;


    public RestException(String message, HttpStatus status) {
        this.message = message;
        this.status = status;
    }

    public RestException(List<ErrorData> errorData, HttpStatus status) {
        this.errorData = errorData;
        this.status = status;
    }

    public static RestException restThrow(String message, HttpStatus status) {
        return new RestException(message, status);
    }

    public static RestException restThrow(List<ErrorData> errorData, HttpStatus status) {
        return new RestException(errorData, status);
    }

    public static RestException notFound(String termin) {
        return new RestException(MessageService.notFound(termin), HttpStatus.NOT_FOUND);
    }


    public static RestException forbidden() {
        return new RestException(MessageService.getMessage("FORBIDDEN"), HttpStatus.FORBIDDEN);
    }

    public static RestException attackResponse() {
        return new RestException(MessageService.getMessage("ATTACK_RESPONSE"), HttpStatus.BAD_REQUEST);
    }

    public static RestException badRequest() {
        return new RestException(MessageService.getMessage("BAD_REQUEST"), HttpStatus.BAD_REQUEST);
    }

    public static RestException serverError() {
        return new RestException(MessageService.getMessage("INTERNAL_SERVER_ERROR"),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
