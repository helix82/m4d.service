package su.opencode.digsigcik.synapi.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import su.opencode.digsigcik.synapi.annotation.MchdExceptionHandler;
import su.opencode.digsigcik.synapi.dto.MrpaRegistryFullDTO;
import su.opencode.digsigcik.synapi.dto.MrpaRegistryWithFioDTO;
import su.opencode.digsigcik.synapi.exception.BadRequestException;
import su.opencode.digsigcik.synapi.model.MrpaRegistry;
import su.opencode.digsigcik.synapi.model.MrpaRegistryCredential;
import su.opencode.digsigcik.synapi.service.MrpaRegistryCredentialService;
import su.opencode.digsigcik.synapi.service.MrpaRegistryService;

import javax.validation.ConstraintViolationException;
import java.util.List;

import static su.opencode.digsigcik.synapi.util.Constant.ID_IS_NULL;
import static su.opencode.digsigcik.synapi.util.Constant.NULL;

@RestController
@MchdExceptionHandler
@RequestMapping("/api/v1/mrpa_registry" )
@AllArgsConstructor
public class MrpaRegistryController {
    private final String MRPA_REGISTRY_IS_NULL = String.format("Mrpa registry is %s!", NULL);
    private final MrpaRegistryService mrpaRegistryService;
    private final MrpaRegistryCredentialService mrpaRegistryCredentialService;

    @Operation(summary = "Получить список МЧД с ФИО представителя и ФИО доверителя",
            description = "Возвращает список МЧД с ФИО представителя и ФИО доверителя" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно получено" ),
            @ApiResponse(code = 404, message = "Not found - Ни одна МЧД не найдена" )
    })
    @GetMapping()
    public List<MrpaRegistryWithFioDTO> findAllMrpaRegistry() {
        return mrpaRegistryService.findAllMrpaRegistry();
    }

    @Operation(summary = "Получить список МЧД с ФИО представителя и ФИО доверителя с фильтром по пользователю",
            description = "Возвращает список МЧД с ФИО представителя и ФИО доверителя" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно получено" ),
            @ApiResponse(code = 404, message = "Not found - Ни одна МЧД для пользователя не найдена" )
    })
    @GetMapping("list/{userId}" )
    public List<MrpaRegistryWithFioDTO> findListMrpaRegistryByUserId(
            @PathVariable @Parameter(name = "userId", description = "ИД пользователя", example = "1" ) Long userId
    ) {
        return mrpaRegistryService.findAllByMrpaRegistryUserId(userId);
    }

    @Operation(summary = "Сохранить данные по МЧД в БД", description = "Возвращает данные по созданному МЧД" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно сохранено" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @PostMapping("save" )
    public ResponseEntity<?> saveMrpaRegistry(@RequestBody MrpaRegistry mrpaRegistry) {
        MrpaRegistry mrpaRegistrySaved;
        try {
            mrpaRegistrySaved = mrpaRegistryService.saveMrpaRegistry(mrpaRegistry);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(MRPA_REGISTRY_IS_NULL);
        }
        //todo добавить проверку для списка
        List<MrpaRegistryWithFioDTO> mrpaRegistryWithFioDTOS = mrpaRegistryService.findByMrpaRegistryId(mrpaRegistrySaved.getId());
        return new ResponseEntity<>(mrpaRegistryWithFioDTOS, HttpStatus.OK);
    }

    @Operation(summary = "Получить МЧД с ФИО представителя и ФИО доверителя по ИД МЧД",
            description = "Возвращает МЧД с ФИО представителя и ФИО доверителя" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно получено" ),
            @ApiResponse(code = 404, message = "Not found - Ни одна МЧД не найдена" )
    })
    @GetMapping("/{id}" )
    public List<MrpaRegistryWithFioDTO> findMrpaRegistryById(@PathVariable @Parameter(name = "id",
            description = "ИД МЧД", example = "1" ) Long id) {
        return mrpaRegistryService.findByMrpaRegistryId(id);
    }

    @Operation(summary = "Получить МЧД по ИД пользователя, вся информация",
            description = "Возвращает всю информацию по МЧД" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно получено" ),
            @ApiResponse(code = 404, message = "Not found - Ни одна МЧД не найдена" )
    })
    @GetMapping("user_id/{id}" )
    public List<MrpaRegistryFullDTO> findMrpaRegistryByUserId(@PathVariable @Parameter(name = "id",
            description = "ИД пользователя", example = "1" ) Long id) {
        return mrpaRegistryService.findByMrpaRegistryUserId(id);
    }

    @Operation(summary = "Изменить данные по МЧД", description = "Возвращает основные данные по МЧД" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Изменения успешно сохранены" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @PutMapping("update" )
    public ResponseEntity<?> updateMrpaRegistry(@RequestBody MrpaRegistry mrpaRegistry) {
        MrpaRegistry mrpaRegistryUpdated;
        try {
            mrpaRegistryUpdated = mrpaRegistryService.updateMrpaRegistry(mrpaRegistry);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(MRPA_REGISTRY_IS_NULL);
        }

        List<MrpaRegistryWithFioDTO> mrpaRegistryWithFioDTOS = mrpaRegistryService.findByMrpaRegistryId(mrpaRegistryUpdated.getId());
        return new ResponseEntity<>(mrpaRegistryWithFioDTOS, HttpStatus.OK);
    }

    @Operation(summary = "Удалить данные по МЧД", description = "Возвращает список МЧД с ФИО представителя и ФИО доверителя" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Запись успешно удалена" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @DeleteMapping("delete/{id}" )
    public ResponseEntity<?> deleteMrpaRegistry(@PathVariable @Parameter(name = "id",
            description = "ИД МЧД", example = "1" ) Long id) {
        try {
            mrpaRegistryService.deleteMrpaRegistry(id);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(ID_IS_NULL);
        }

        List<MrpaRegistryWithFioDTO> mrpaRegistryWithFioDTOS = mrpaRegistryService.findAllMrpaRegistry();
        return new ResponseEntity<>(mrpaRegistryWithFioDTOS, HttpStatus.OK);
    }

    @Operation(summary = "Привязать полномочия к МЧД", description = "Возвращает данные о привязке полномочий к МЧД" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно привязано" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @PutMapping("/{mrpaId}/credential/{credentialId}" )
    public MrpaRegistryCredential assignCredentialToMrpaRegistry(
            @PathVariable @Parameter(name = "mrpaId", description = "ИД МЧД", example = "1" ) Long mrpaId,
            @PathVariable @Parameter(name = "credentialId", description = "ИД полномочия", example = "1" ) Long credentialId) {
        MrpaRegistryCredential mrpaRegistryCredential = new MrpaRegistryCredential();
        mrpaRegistryCredential.setMrpaRegistryId(mrpaId);
        mrpaRegistryCredential.setCredentialId(credentialId);
        return mrpaRegistryCredentialService.saveMrpaRegistryCredential(mrpaRegistryCredential);
    }

    @Operation(summary = "Получить МЧД с ФИО представителя и ФИО доверителя по ИНН доверителя",
            description = "Возвращает МЧД с ФИО представителя и ФИО доверителя" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно получено" ),
            @ApiResponse(code = 404, message = "Not found - Ни одна МЧД не найдена" )
    })
    @GetMapping("principal_inn/{inn}" )
    public List<MrpaRegistryWithFioDTO> findMrpaRegistryByPrincipalInn(@PathVariable @Parameter(name = "inn",
            description = "ИНН доверителя", example = "1" ) String inn) {
        return mrpaRegistryService.findByMrpaRegistryPrincipalInn(inn);
    }
}
