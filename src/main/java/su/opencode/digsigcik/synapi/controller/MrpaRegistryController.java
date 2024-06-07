package su.opencode.digsigcik.synapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import su.opencode.digsigcik.synapi.dto.MrpaRegistryFullDTO;
import su.opencode.digsigcik.synapi.dto.MrpaRegistryWithFioDTO;
import su.opencode.digsigcik.synapi.model.MrpaRegistry;
import su.opencode.digsigcik.synapi.model.MrpaRegistryCredential;
import su.opencode.digsigcik.synapi.service.MrpaRegistryCredentialService;
import su.opencode.digsigcik.synapi.service.MrpaRegistryService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/mrpa_registry")
@AllArgsConstructor
public class MrpaRegistryController {
    private final MrpaRegistryService service;
    private final MrpaRegistryCredentialService mrpaRegistryCredentialService;

    @Operation(summary = "Получить список МЧД с ФИО представителя и ФИО доверителя", description = "Возвращает список МЧД с ФИО представителя и ФИО доверителя")
    @GetMapping()
    public List<MrpaRegistryWithFioDTO>  findAllMrpaRegistry() {
        return service.findAllMrpaRegistry();
    }

    @Operation(summary = "Получить список МЧД с ФИО представителя и ФИО доверителя с фильтром по пользователю", description = "Возвращает список МЧД с ФИО представителя и ФИО доверителя")
    @GetMapping("list/{userId}")
    public List<MrpaRegistryWithFioDTO> findListMrpaRegistryByUserId(@PathVariable Long userId) {
        return service.findAllByMrpaRegistryUserId(userId);
    }

    @Operation(summary = "Сохранить данные по МЧД в БД", description = "Возвращает данные по созданному МЧД")
    @PostMapping("save")
    public List<MrpaRegistryWithFioDTO> saveMrpaRegistry(@RequestBody MrpaRegistry mrpaRegistry) {
        MrpaRegistry mrpaRegistrySaved = service.saveMrpaRegistry(mrpaRegistry);
        return service.findByMrpaRegistryId(mrpaRegistrySaved.getId());
    }

    @Operation(summary = "Получить МЧД с ФИО представителя и ФИО доверителя по ИД МЧД", description = "Возвращает МЧД с ФИО представителя и ФИО доверителя")
    @GetMapping("/{id}")
    public List<MrpaRegistryWithFioDTO> findMrpaRegistryById(@PathVariable Long id) {
        return service.findByMrpaRegistryId(id);
    }

    @Operation(summary = "Получить МЧД по ИД пользователя, вся информация", description = "Возвращает всю информацию по МЧД")
    @GetMapping("user_id/{id}")
    public List<MrpaRegistryFullDTO> findMrpaRegistryByUserId(@PathVariable Long id) {
        return service.findByMrpaRegistryUserId(id);
    }

    @Operation(summary = "Изменить данные по МЧД", description = "Возвращает основные данные по МЧД")
    @PutMapping("update")
    public List<MrpaRegistryWithFioDTO> updateMrpaRegistry(@RequestBody MrpaRegistry mrpaRegistry) {
        MrpaRegistry mrpaRegistryUpdated = service.updateMrpaRegistry(mrpaRegistry);
        return service.findByMrpaRegistryId(mrpaRegistryUpdated.getId());
    }

    @Operation(summary = "Удалить данные по МЧД", description = "Возвращает список МЧД с ФИО представителя и ФИО доверителя")
    @DeleteMapping("delete/{id}")
    public List<MrpaRegistryWithFioDTO> deleteMrpaRegistry(@PathVariable Long id) {
        service.deleteMrpaRegistry(id);
        return service.findAllMrpaRegistry();
    }

    @Operation(summary = "Привязать полномочия к МЧД", description = "Возвращает данные о привязке полномочий к МЧД")
    @PutMapping("/{mrpaId}/credential/{credentialId}")
    public MrpaRegistryCredential assignCredentialToMrpaRegistry(@PathVariable Long mrpaId, @PathVariable Long credentialId) {
        MrpaRegistryCredential mrpaRegistryCredential = new MrpaRegistryCredential();
        mrpaRegistryCredential.setMrpaRegistryId(mrpaId);
        mrpaRegistryCredential.setCredentialId(credentialId);
        return mrpaRegistryCredentialService.saveMrpaRegistryCredential(mrpaRegistryCredential);
    }

    @Operation(summary = "Получить МЧД с ФИО представителя и ФИО доверителя по ИНН доверителя", description = "Возвращает МЧД с ФИО представителя и ФИО доверителя")
    @GetMapping("principal_inn/{inn}")
    public List<MrpaRegistryWithFioDTO> findMrpaRegistryByPrincipalInn(@PathVariable String inn) {
        return service.findByMrpaRegistryPrincipalInn(inn);
    }

}
