package su.opencode.digsigcik.synapi.util.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import su.opencode.digsigcik.synapi.annotation.SesExceptionHandler;
import su.opencode.digsigcik.synapi.dto.ResponseErrorDto;
import su.opencode.digsigcik.synapi.exception.BadRequestException;

@ControllerAdvice(annotations = SesExceptionHandler.class)
public class SesAdvice {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?> handleSesBadRequestException(BadRequestException error) {
        ResponseErrorDto response = ResponseErrorDto.createResponseErrorDto(error.getMessage(), HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
