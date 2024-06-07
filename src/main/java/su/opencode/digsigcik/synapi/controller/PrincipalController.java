package su.opencode.digsigcik.synapi.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import su.opencode.digsigcik.synapi.model.Principal;
import su.opencode.digsigcik.synapi.service.PrincipalService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/principal")
@AllArgsConstructor
public class PrincipalController {
    private final PrincipalService service;

    @Operation(summary = "Получить список доверителей", description = "Возвращает список доверителей")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно получено"),
            @ApiResponse(code = 404, message = "Not found - Ни одна запись не найдена")
    })
    @GetMapping()
    public List<Principal> findAllPrincipal() {
        return service.findAllPrincipal();
    }

    @Operation(summary = "Сохранить доверителя", description = "Возвращает сохраненную запись доверителя")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно сохранено"),
            @ApiResponse(code = 404, message = "Not found")
    })
    @PostMapping("save")
    public Principal savePrincipal(@RequestBody Principal principal) {
        return service.savePrincipal(principal);
    }

    @Operation(summary = "Получить доверителя по ИД", description = "Возвращает доверителя")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно получено"),
            @ApiResponse(code = 404, message = "Not found - Ни одна запись не найдена")
    })
    @GetMapping("/{id}")
    public Principal findByPrincipalId(@PathVariable Long id) {
        return service.findByPrincipalId(id);
    }

    @Operation(summary = "Изменить доверителя", description = "Возвращает измененного доверителя")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно изменено"),
            @ApiResponse(code = 404, message = "Not found")
    })
    @PutMapping("update")
    public Principal updatePrincipal(@RequestBody Principal principal) {
        return service.updatePrincipal(principal);
    }


    @Operation(summary = "Удалить доверителя", description = "Ничего не возвращает")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно удалено"),
            @ApiResponse(code = 404, message = "Not found")
    })
    @DeleteMapping("delete/{id}")
    public void deletePrincipal(@PathVariable Long id) {
        service.deletePrincipal(id);
    }
}
