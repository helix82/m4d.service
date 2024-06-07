package su.opencode.digsigcik.synapi.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import su.opencode.digsigcik.synapi.model.Credential;
import su.opencode.digsigcik.synapi.service.CredentialService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/credential")
@AllArgsConstructor
public class CredentialController {
    private final CredentialService service;

    @Operation(summary = "Получить список полномочий", description = "Возвращает список полномочий")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно получено"),
            @ApiResponse(code = 404, message = "Not found - Ни одна запись не найдена")
    })
    @GetMapping()
    public List<Credential> findAllCredential() {
        return service.findAllCredential();
    }

    @Operation(summary = "Сохранить запись полномочия", description = "Возвращает созданную запись полномочия")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно сохранено"),
            @ApiResponse(code = 404, message = "Not found")
    })
    @PostMapping("save")
    public Credential saveCredential(@RequestBody Credential credential) {
        return service.saveCredential(credential);
    }

    @Operation(summary = "Получить запись полномочия по ИД", description = "Возвращает запись полномочия")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно получено"),
            @ApiResponse(code = 404, message = "Not found - Ни одна запись не найдена")
    })
    @GetMapping("/{id}")
    public Credential findByCredentialId(@PathVariable Long id) {
        return service.findByCredentialId(id);
    }

    @Operation(summary = "Изменить запись полномочия", description = "Возвращает измененную запись полномочия")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно получено"),
            @ApiResponse(code = 404, message = "Not found - Ни одна запись не найдена")
    })
    @PutMapping("update")
    public Credential updateCredential(@RequestBody Credential credential) {
        return service.updateCredential(credential);
    }

    @Operation(summary = "Удалить запись полномочия по ИД", description = "Ничего не возвращает")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно удалено"),
            @ApiResponse(code = 404, message = "Not found")
    })
    @DeleteMapping("delete/{id}")
    public void deleteCredential(@PathVariable Long id) {
        service.deleteCredential(id);
    }
}
