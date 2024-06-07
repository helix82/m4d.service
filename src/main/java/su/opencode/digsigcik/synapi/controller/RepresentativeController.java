package su.opencode.digsigcik.synapi.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import su.opencode.digsigcik.synapi.model.Representative;
import su.opencode.digsigcik.synapi.service.RepresentativeService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/representative")
@Tag(name = "Представители", description = "API управления представителями")
@AllArgsConstructor
public class RepresentativeController {
    private final RepresentativeService service;

    @Operation(summary = "Получить список представителей", description = "Возвращает список представителей")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно получено"),
            @ApiResponse(code = 404, message = "Not found - Ни одна запись не найдена")
    })
    @GetMapping()
    public List<Representative> findAllRepresentative() {
        return service.findAllRepresentative();
    }

    @Operation(summary = "Сохранить представителя", description = "Возвращает сохраненного представителя")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно сохранено"),
            @ApiResponse(code = 404, message = "Not found")
    })
    @PostMapping("save")
    public Representative saveRepresentative(@RequestBody Representative representative) {
        return service.saveRepresentative(representative);
    }

    @Operation(summary = "Получить представителя по ИД", description = "Возвращает представителя")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно получено"),
            @ApiResponse(code = 404, message = "Not found - Ни одна запись не найдена")
    })
    @GetMapping("/{id}")
    public Representative findByRepresentativeId(@PathVariable Long id) {
        return service.findByRepresentativeId(id);
    }

    @Operation(summary = "Изменить представителя", description = "Возвращает измененного представителя")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно изменено"),
            @ApiResponse(code = 404, message = "Not found")
    })
    @PutMapping("update")
    public Representative updateRepresentative(@RequestBody Representative representative) {
        return service.updateRepresentative(representative);
    }

    @Operation(summary = "Удалить представителя", description = "Ничего не возвращает")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно удалено"),
            @ApiResponse(code = 404, message = "Not found")
    })
    @DeleteMapping("delete/{id}")
    public void deleteRepresentative(@PathVariable Long id) {
        service.deleteRepresentative(id);
    }
}
