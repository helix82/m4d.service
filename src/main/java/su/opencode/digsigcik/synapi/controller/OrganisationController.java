package su.opencode.digsigcik.synapi.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import su.opencode.digsigcik.synapi.model.Organisation;
import su.opencode.digsigcik.synapi.service.OrganisationService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/organisation")
@Tag(name = "Организации", description = "API управления организациями")
@AllArgsConstructor
public class OrganisationController {
    private final OrganisationService service;

    @Operation(summary = "Получить список организаций", description = "Возвращает список организаций")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно получено"),
            @ApiResponse(code = 404, message = "Not found - Ни одна запись не найдена")
    })
    @GetMapping()
    public List<Organisation> findAllCredential() {
        return service.findAllOrganisation();
    }

    @Operation(summary = "Сохранить организацию", description = "Возвращает сохраненную организацию")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно сохранено"),
            @ApiResponse(code = 404, message = "Not found")
    })
    @PostMapping("save")
    public Organisation saveCredential(@RequestBody Organisation organisation) {
        return service.saveOrganisation(organisation);
    }

    @Operation(summary = "Получить организацию по ИД", description = "Возвращает запись организации")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно получено"),
            @ApiResponse(code = 404, message = "Not found - Ни одна запись не найдена")
    })
    @GetMapping("/{id}")
    public Organisation findByCredentialId(@PathVariable Long id) {
        return service.findByOrganisationId(id);
    }

    @Operation(summary = "Изменить запись организации", description = "Возвращает измененную организацию")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно изменено"),
            @ApiResponse(code = 404, message = "Not found")
    })
    @PutMapping("update")
    public Organisation updateCredential(@RequestBody Organisation organisation) {
        return service.updateOrganisation(organisation);
    }

    @Operation(summary = "Удалить организацию", description = "Ничего не возвращает")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно удалено"),
            @ApiResponse(code = 404, message = "Not found")
    })
    @DeleteMapping("delete/{id}")
    public void deleteCredential(@PathVariable Long id) {
        service.deleteOrganisation(id);
    }
}
