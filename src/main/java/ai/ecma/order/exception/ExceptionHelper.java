package ai.ecma.order.exception;

import ai.ecma.lib.payload.ApiResult;
import ai.ecma.lib.payload.ErrorData;
import ai.ecma.order.common.MessageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class not documented :(
 *
 * @author Muhammad Mo'minov
 * @since 17.12.2021
 */
@RestControllerAdvice
public class ExceptionHelper {

    private ObjectError error;

    @ExceptionHandler(value = {RestException.class})
    public ResponseEntity<?> exceptionHandler(RestException e) {
        List<ErrorData> errorDataList = new ArrayList<>();

        if (Objects.nonNull(e.getErrorData())){
            errorDataList.addAll(e.getErrorData());
        }else {
            errorDataList.add(new ErrorData(e.getMessage(), e.getStatus().value()));
        }
        return ResponseEntity
                .status(e.getStatus())
                .body(new ApiResult<>(false, errorDataList));
    }

    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    public ResponseEntity<?> exceptionHandler(MethodArgumentNotValidException e) {
        List<ErrorData> errorDataList = new ArrayList<>();

        List<ObjectError> allErrors = e.getAllErrors();
        for (ObjectError error : allErrors) {
            FieldError fieldError = (FieldError) error;
            ErrorData errorData = new ErrorData(error.getDefaultMessage(), HttpStatus.BAD_REQUEST.value(), fieldError.getField());
            errorDataList.add(errorData);
        }
        ApiResult<Object> apiResult = new ApiResult<>(false, errorDataList);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResult);
    }

    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<?> exceptionHandler(Exception e) {
        e.printStackTrace();
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResult<>(false, MessageService.getMessage("INTERNAL_SERVER_ERROR")));
    }

//    @ExceptionHandler(value = {EmptyResultDataAccessException.class})
//    public ResponseEntity<?> exceptionHandler(EmptyResultDataAccessException e){
//        e.printStackTrace();
//        System.out.println("HANDLER USHLADI");
//        return null;
//    }
}
