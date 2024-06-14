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
import su.opencode.digsigcik.synapi.model.RepresentativePassport;
import su.opencode.digsigcik.synapi.service.RepresentativePassportService;

import javax.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static su.opencode.digsigcik.synapi.util.Constant.ID_IS_NULL;
import static su.opencode.digsigcik.synapi.util.Constant.NULL;

@RestController
@MchdExceptionHandler
@RequestMapping("/api/v1/representative_passport")
@AllArgsConstructor
public class RepresentativePassportController {
    private final String REPRESENTATIVE_PASSPORT_IS_NULL = String.format("Representative passport is %s!", NULL);
    private final RepresentativePassportService service;

    @Operation(summary = "Получить список паспортов представителя", description = "Возвращает список паспортов представителя")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно получено"),
            @ApiResponse(code = 404, message = "Not found - Ни одна запись не найдена")
    })
    @GetMapping()
    public ResponseEntity<?> findAllRepresentativePassport() {
        List<RepresentativePassport> response;
        try {
            response = service.findAllRepresentativePassport();
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(REPRESENTATIVE_PASSPORT_IS_NULL);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "Сохранить паспорт представителя", description = "Возвращает сохраненный паспорт представителя")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно сохранено"),
            @ApiResponse(code = 404, message = "Not found")
    })
    @PostMapping("save")
    public ResponseEntity<?> saveRepresentativePassport(@RequestBody RepresentativePassport representativePassport) {
        RepresentativePassport response;
        try {
            response = service.saveRepresentativePassport(representativePassport);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(REPRESENTATIVE_PASSPORT_IS_NULL);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "Получить паспорт представителя по ИД", description = "Возвращает паспорт представителя по ИД")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно получено"),
            @ApiResponse(code = 404, message = "Not found - Ни одна запись не найдена")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> findByRepresentativePassportId(@PathVariable Long id) {
        RepresentativePassport response;
        try {
            response = service.findByRepresentativePassportId(id);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(REPRESENTATIVE_PASSPORT_IS_NULL);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "Изменить паспорт представителя", description = "Возвращает измененный паспорт представителя")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно изменено"),
            @ApiResponse(code = 404, message = "Not found")
    })
    @PutMapping("update")
    public ResponseEntity<?> updateRepresentativePassport(@RequestBody RepresentativePassport representativePassport) {
        RepresentativePassport response;
        try {
            response = service.updateRepresentativePassport(representativePassport);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(REPRESENTATIVE_PASSPORT_IS_NULL);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "Удалить паспорт представителя", description = "Ничего не возвращает")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно удалено"),
            @ApiResponse(code = 404, message = "Not found")
    })
    @DeleteMapping("delete/{id}")
    public ResponseEntity<?> deleteRepresentativePassport(@PathVariable Long id) {
        try {
            service.deleteRepresentativePassport(id);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(ID_IS_NULL);
        }
        Map<String, String> body = new HashMap<>();
        body.put("result", "true");

        return new ResponseEntity<>(body, HttpStatus.OK);
    }
}
