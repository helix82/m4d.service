package su.opencode.digsigcik.m4d.util.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import su.opencode.digsigcik.m4d.annotation.M4dExceptionHandler;
import su.opencode.digsigcik.m4d.dto.ResponseErrorDto;
import su.opencode.digsigcik.m4d.exception.BadRequestException;
import su.opencode.digsigcik.m4d.exception.M4dException;

import javax.persistence.EntityNotFoundException;

@ControllerAdvice(annotations = M4dExceptionHandler.class)
public class MchdAdvice {

    @ExceptionHandler(M4dException.class)
    public ResponseEntity<?> handleMchdException(M4dException error) {
        ResponseErrorDto response = ResponseErrorDto.createResponseErrorDto(error.getMessage(), HttpStatus.OK);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?> handleMchdBadRequestException(BadRequestException error) {
        ResponseErrorDto response = ResponseErrorDto.createResponseErrorDto(error.getMessage(), HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleMchdDataIntegrityViolationException(DataIntegrityViolationException error) {
        ResponseErrorDto response = ResponseErrorDto.createResponseErrorDto(error.getMessage(), HttpStatus.CONFLICT);
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> handleMchdEntityNotFoundException(EntityNotFoundException error) {
        ResponseErrorDto response = ResponseErrorDto.createResponseErrorDto(error.getMessage(), HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
}
