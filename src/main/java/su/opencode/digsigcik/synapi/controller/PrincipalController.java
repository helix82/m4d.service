package su.opencode.digsigcik.synapi.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import su.opencode.digsigcik.synapi.annotation.MchdExceptionHandler;
import su.opencode.digsigcik.synapi.exception.BadRequestException;
import su.opencode.digsigcik.synapi.model.Principal;
import su.opencode.digsigcik.synapi.service.PrincipalService;

import javax.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static su.opencode.digsigcik.synapi.util.Constant.ID_IS_NULL;
import static su.opencode.digsigcik.synapi.util.Constant.NULL;

@RestController
@MchdExceptionHandler
@RequestMapping("/api/v1/principal")
@AllArgsConstructor
public class PrincipalController {
    private final String PRINCIPAL_IS_NULL = String.format("Principal is %s!", NULL);

    private final PrincipalService service;

    @Operation(summary = "Получить список доверителей", description = "Возвращает список доверителей")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно получено"),
            @ApiResponse(code = 404, message = "Not found - Ни одна запись не найдена")
    })
    @GetMapping()
    public ResponseEntity<?> findAllPrincipal() {
        List<Principal> response;
        try {
            response = service.findAllPrincipal();
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(PRINCIPAL_IS_NULL);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "Сохранить доверителя", description = "Возвращает сохраненную запись доверителя")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно сохранено"),
            @ApiResponse(code = 404, message = "Not found")
    })
    @PostMapping("save")
    public ResponseEntity<?> savePrincipal(@RequestBody Principal principal) {
        Principal response;
        try {
            response = service.savePrincipal(principal);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(PRINCIPAL_IS_NULL);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "Получить доверителя по ИД", description = "Возвращает доверителя")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно получено"),
            @ApiResponse(code = 404, message = "Not found - Ни одна запись не найдена")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> findByPrincipalId(@PathVariable Long id) {
        Principal response;
        try {
            response = service.findByPrincipalId(id);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(PRINCIPAL_IS_NULL);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "Изменить доверителя", description = "Возвращает измененного доверителя")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно изменено"),
            @ApiResponse(code = 404, message = "Not found")
    })
    @PutMapping("update")
    public ResponseEntity<?> updatePrincipal(@RequestBody Principal principal) {
        Principal response;
        try {
            response = service.updatePrincipal(principal);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(PRINCIPAL_IS_NULL);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @Operation(summary = "Удалить доверителя", description = "Ничего не возвращает")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно удалено"),
            @ApiResponse(code = 404, message = "Not found")
    })
    @DeleteMapping("delete/{id}")
    public ResponseEntity<?> deletePrincipal(@PathVariable Long id) {
        try {
            service.deletePrincipal(id);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(ID_IS_NULL);
        }
        Map<String, String> body = new HashMap<>();
        body.put("result", "true");

        return new ResponseEntity<>(body, HttpStatus.OK);
    }
}
