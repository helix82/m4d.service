package su.opencode.digsigcik.synapi.util.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import su.opencode.digsigcik.synapi.annotation.SesExceptionHandler;
import su.opencode.digsigcik.synapi.dto.ResponseErrorDto;
import su.opencode.digsigcik.synapi.dto.SesVerifyJsonDto;
import su.opencode.digsigcik.synapi.exception.BadRequestException;
import su.opencode.digsigcik.synapi.exception.SesException;
import su.opencode.digsigcik.synapi.exception.SesVerifyException;

@ControllerAdvice(annotations = SesExceptionHandler.class)
public class CustomAdvice {

    @ExceptionHandler(SesException.class)
    public ResponseEntity<?> handleSesException(SesException error) {
        ResponseErrorDto response = createResponseErrorDto(error.getMessage(), HttpStatus.OK);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?> handleSesBadRequestException(BadRequestException error) {
        ResponseErrorDto response = createResponseErrorDto(error.getMessage(), HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(SesVerifyException.class)
    public ResponseEntity<?> handleSesVerifyException(SesVerifyException error) {
        SesVerifyJsonDto response = SesVerifyJsonDto.builder()
                .verifyResultMessage(error.getMessage())
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private ResponseErrorDto createResponseErrorDto(String message, HttpStatus status) {
        return ResponseErrorDto.builder()
                .code(status.value())
                .message(message)
                .build();
    }
}
