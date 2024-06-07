package su.opencode.digsigcik.synapi.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import su.opencode.digsigcik.synapi.model.RepresentativePassport;
import su.opencode.digsigcik.synapi.service.RepresentativePassportService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/representative_passport")
@AllArgsConstructor
public class RepresentativePassportController {
    private final RepresentativePassportService service;

    @Operation(summary = "Получить список паспортов представителя", description = "Возвращает список паспортов представителя")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно получено"),
            @ApiResponse(code = 404, message = "Not found - Ни одна запись не найдена")
    })
    @GetMapping()
    public List<RepresentativePassport> findAllRepresentativePassport() {
        return service.findAllRepresentativePassport();
    }

    @Operation(summary = "Сохранить паспорт представителя", description = "Возвращает сохраненный паспорт представителя")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно сохранено"),
            @ApiResponse(code = 404, message = "Not found")
    })
    @PostMapping("save")
    public RepresentativePassport saveRepresentativePassport(@RequestBody RepresentativePassport representativePassport) {
        return service.saveRepresentativePassport(representativePassport);
    }

    @Operation(summary = "Получить паспорт представителя по ИД", description = "Возвращает паспорт представителя по ИД")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно получено"),
            @ApiResponse(code = 404, message = "Not found - Ни одна запись не найдена")
    })
    @GetMapping("/{id}")
    public RepresentativePassport findByRepresentativePassportId(@PathVariable Long id) {
        return service.findByRepresentativePassportId(id);
    }

    @Operation(summary = "Изменить паспорт представителя", description = "Возвращает измененный паспорт представителя")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно изменено"),
            @ApiResponse(code = 404, message = "Not found")
    })
    @PutMapping("update")
    public RepresentativePassport updateRepresentativePassport(@RequestBody RepresentativePassport representativePassport) {
        return service.updateRepresentativePassport(representativePassport);
    }

    @Operation(summary = "Удалить паспорт представителя", description = "Ничего не возвращает")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно удалено"),
            @ApiResponse(code = 404, message = "Not found")
    })
    @DeleteMapping("delete/{id}")
    public void deleteRepresentativePassport(@PathVariable Long id) {
        service.deleteRepresentativePassport(id);
    }
}
