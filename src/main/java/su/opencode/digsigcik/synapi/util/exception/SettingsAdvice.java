package su.opencode.digsigcik.synapi.util.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import su.opencode.digsigcik.synapi.annotation.SettingsExceptionHandler;
import su.opencode.digsigcik.synapi.dto.ResponseErrorDto;
import su.opencode.digsigcik.synapi.exception.BadRequestException;

import javax.persistence.EntityNotFoundException;

@ControllerAdvice(annotations = SettingsExceptionHandler.class)
public class SettingsAdvice {

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
