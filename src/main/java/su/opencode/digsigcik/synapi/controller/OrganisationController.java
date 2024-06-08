package su.opencode.digsigcik.synapi.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import su.opencode.digsigcik.synapi.model.Organisation;
import su.opencode.digsigcik.synapi.service.OrganisationService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/organisation")
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

    @Operation(summary = "Сохранить организацию", description = "Если указанной организации нет в базе, создаем её и возвращаем, иначе возвращаем уже существующую организацию")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно сохранено"),
            @ApiResponse(code = 404, message = "Not found")
    })
    @PostMapping("save")
    public Organisation saveCredential(@RequestBody Organisation organisation) {
        Organisation existsOrganisation = service.findByOrganisationInn(organisation.getInn());
        if(existsOrganisation != null) {
            return existsOrganisation;
        }
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
