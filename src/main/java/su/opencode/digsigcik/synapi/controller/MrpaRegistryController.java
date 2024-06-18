package su.opencode.digsigcik.synapi.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import su.opencode.digsigcik.synapi.annotation.MchdExceptionHandler;
import su.opencode.digsigcik.synapi.dto.*;
import su.opencode.digsigcik.synapi.enums.StatusTypeEnum;
import su.opencode.digsigcik.synapi.exception.BadRequestException;
import su.opencode.digsigcik.synapi.model.MrpaRegistry;
import su.opencode.digsigcik.synapi.model.MrpaRegistryCredential;
import su.opencode.digsigcik.synapi.service.MrpaRegistryCredentialService;
import su.opencode.digsigcik.synapi.service.MrpaRegistryService;
import su.opencode.digsigcik.synapi.util.XMLUtil;

import javax.validation.ConstraintViolationException;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static su.opencode.digsigcik.synapi.util.Constant.ID_IS_NULL;
import static su.opencode.digsigcik.synapi.util.Constant.NULL;
import static su.opencode.digsigcik.synapi.util.XMLUtil.convertStringToDocument;

@RestController
@MchdExceptionHandler
@RequestMapping("/api/v1/mrpa_registry" )
@AllArgsConstructor
public class MrpaRegistryController {
    private final String MRPA_REGISTRY_IS_NULL = String.format("Mrpa registry is %s!", NULL);
    private final String MRPA_REGISTRY_CREDENTIAL_IS_NULL = String.format("Assign Credential to Mrpa registry is %s!", NULL);
    private final MrpaRegistryService mrpaRegistryService;
    private final MrpaRegistryCredentialService mrpaRegistryCredentialService;

    @Operation(summary = "Получить список МЧД с ФИО представителя и ФИО доверителя",
            description = "Возвращает список МЧД с ФИО представителя и ФИО доверителя" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно получено" ),
            @ApiResponse(code = 404, message = "Not found - Ни одна МЧД не найдена" )
    })
    @GetMapping(produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> findAllMrpaRegistry() {
        List<MrpaRegistryWithFioDTO> mrpaRegistryFindAll;
        try {
            mrpaRegistryFindAll = mrpaRegistryService.findAllMrpaRegistry();
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(MRPA_REGISTRY_IS_NULL);
        }
        for(MrpaRegistryWithFioDTO m: mrpaRegistryFindAll){
            m.credential = mrpaRegistryService.getCredentialByMrpaRegistryId(m.getId());
        }
        return new ResponseEntity<>(mrpaRegistryFindAll, HttpStatus.OK);
    }

    @Operation(summary = "Сохранить данные по МЧД в БД", description = "Возвращает созданную МЧД с ФИО представителя и ФИО доверителя" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно сохранено" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @PostMapping(value = "save",produces = "application/json; charset=UTF-8" )
    public ResponseEntity<?> saveMrpaRegistry(@RequestBody MrpaRegistrySaveWithCredentialDTO mrpaRegistrySaveWithCredentialDTO) {
        MrpaRegistry mrpaRegistry = new MrpaRegistry();
        mrpaRegistry.setStatus(StatusTypeEnum.NOT_SIGNED);
        mrpaRegistry.setOrganisationId(mrpaRegistrySaveWithCredentialDTO.getOrganisationId());
        mrpaRegistry.setPrincipalId(mrpaRegistrySaveWithCredentialDTO.getPrincipalId());
        mrpaRegistry.setRepresentativeId(mrpaRegistrySaveWithCredentialDTO.getRepresentativeId());
        mrpaRegistry.setStartDate(mrpaRegistrySaveWithCredentialDTO.getStartDate());
        mrpaRegistry.setEndDate(mrpaRegistrySaveWithCredentialDTO.getEndDate());
        mrpaRegistry.setGuid();
        MrpaRegistry mrpaRegistrySaved;
        try {
            mrpaRegistrySaved = mrpaRegistryService.saveMrpaRegistry(mrpaRegistry);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(MRPA_REGISTRY_IS_NULL);
        }

        Integer[] credentials = mrpaRegistrySaveWithCredentialDTO.getCredentials();
        for(Integer s : credentials) {
            MrpaRegistryCredential mrpaRegistryCredential = new MrpaRegistryCredential();
            mrpaRegistryCredential.setMrpaRegistryId(mrpaRegistrySaved.getId());
            mrpaRegistryCredential.setCredentialId(Long.valueOf(s));
            MrpaRegistryCredential mrpaRegistryCredentialSaved;
            try {
                mrpaRegistryCredentialSaved = mrpaRegistryCredentialService.saveMrpaRegistryCredential(mrpaRegistryCredential);
            } catch (ConstraintViolationException e) {
                throw new BadRequestException(MRPA_REGISTRY_CREDENTIAL_IS_NULL);
            }
            System.out.println(mrpaRegistryCredentialSaved);
        }

        MrpaRegistryWithFioDTO mrpaRegistryWithFioDTOS = mrpaRegistryService.findByMrpaRegistryId(mrpaRegistrySaved.getId());
        mrpaRegistryWithFioDTOS.credential = mrpaRegistryService.getCredentialByMrpaRegistryId(mrpaRegistryWithFioDTOS.getId());
        return new ResponseEntity<>(mrpaRegistryWithFioDTOS, HttpStatus.OK);
    }

    @Operation(summary = "Получить МЧД с ФИО представителя и ФИО доверителя по ИД МЧД",
            description = "Возвращает МЧД с ФИО представителя и ФИО доверителя" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно получено" ),
            @ApiResponse(code = 404, message = "Not found - Ни одна МЧД не найдена" )
    })
    @GetMapping(value = "/{id}", produces = "application/json; charset=UTF-8" )
    public ResponseEntity<?> findMrpaRegistryById(@PathVariable @Parameter(name = "id",
            description = "ИД МЧД", example = "1" ) Long id) {
        MrpaRegistryWithFioDTO mrpaRegistryFind;
        try {
            mrpaRegistryFind = mrpaRegistryService.findByMrpaRegistryId(id);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(MRPA_REGISTRY_IS_NULL);
        }
        mrpaRegistryFind.credential = mrpaRegistryService.getCredentialByMrpaRegistryId(mrpaRegistryFind.getId());
        return new ResponseEntity<>(mrpaRegistryFind, HttpStatus.OK);
    }

    @Operation(summary = "Изменить данные по МЧД", description = "Возвращает измененную МЧД с ФИО представителя и ФИО доверителя" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Изменения успешно сохранены" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @PutMapping(value = "update", produces = "application/json; charset=UTF-8" )
    public ResponseEntity<?> updateMrpaRegistry(@RequestBody MrpaRegistry mrpaRegistry) {
        MrpaRegistry mrpaRegistryUpdated;
        try {
            mrpaRegistryUpdated = mrpaRegistryService.updateMrpaRegistry(mrpaRegistry);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(MRPA_REGISTRY_IS_NULL);
        }
        MrpaRegistryWithFioDTO mrpaRegistryWithFioDTOS = mrpaRegistryService.findByMrpaRegistryId(mrpaRegistryUpdated.getId());
        mrpaRegistryWithFioDTOS.credential = mrpaRegistryService.getCredentialByMrpaRegistryId(mrpaRegistryUpdated.getId());
        return new ResponseEntity<>(mrpaRegistryWithFioDTOS, HttpStatus.OK);
    }

    @Operation(summary = "Удалить данные по МЧД", description = "Возвращает true при удачном удалении" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Запись успешно удалена" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @DeleteMapping(value = "delete/{id}", produces = "application/json; charset=UTF-8" )
    public ResponseEntity<?> deleteMrpaRegistry(@PathVariable @Parameter(name = "id",
            description = "ИД МЧД", example = "1" ) Long id) {
        try {
            mrpaRegistryService.deleteMrpaRegistry(id);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(ID_IS_NULL);
        }
        Map<String, String> body = new HashMap<>();
        body.put("result", "true");

        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    @Operation(summary = "Получить МЧД с ФИО представителя и ФИО доверителя по ИНН доверителя",
            description = "Возвращает МЧД с ФИО представителя и ФИО доверителя" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно получено" ),
            @ApiResponse(code = 404, message = "Not found - Ни одна МЧД не найдена" )
    })
    @GetMapping(value = "principal_inn/{inn}", produces = "application/json; charset=UTF-8" )
    public ResponseEntity<?> findMrpaRegistryByPrincipalInn(@PathVariable @Parameter(name = "inn",
            description = "ИНН доверителя", example = "1" ) String inn) {
        MrpaRegistryWithFioDTO mrpaRegistryFindByInn;
        try {
            mrpaRegistryFindByInn = mrpaRegistryService.findByMrpaRegistryPrincipalInn(inn);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(MRPA_REGISTRY_IS_NULL);
        }
        mrpaRegistryFindByInn.credential = mrpaRegistryService.getCredentialByMrpaRegistryId(mrpaRegistryFindByInn.getId());
        return new ResponseEntity<>(mrpaRegistryFindByInn, HttpStatus.OK);
    }

    @Operation(summary = "Загрузить ЭП", description = "Возвращает МЧД с ФИО представителя и ФИО доверителя")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно загружено"),
            @ApiResponse(code = 404, message = "Not found")
    })
    @PostMapping(value = "/{id}/upload_sig", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> upload(@PathVariable @Parameter(name = "id",
            description = "ИД МЧД", example = "1" ) Long id, @RequestPart("sigFile") MultipartFile sourceSig) throws IOException {
        String content = new String(sourceSig.getBytes());
        Document document = convertStringToDocument(content);
        assert document != null;
        //TODO: @helix82 Проверка валидности сигнатуры и загрузка на сервер в базу
        MrpaRegistry mrpaRegistry = mrpaRegistryService.findById(id);
        if (mrpaRegistry == null) {
            throw new BadRequestException(MRPA_REGISTRY_IS_NULL);
        }
        mrpaRegistry.setStatus(StatusTypeEnum.VALID);
        MrpaRegistry mrpaRegistrySaved;
        try {
            mrpaRegistrySaved = mrpaRegistryService.saveMrpaRegistry(mrpaRegistry);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(MRPA_REGISTRY_IS_NULL);
        }
        MrpaRegistryWithFioDTO mrpaRegistryWithFioDTOS = mrpaRegistryService.findByMrpaRegistryId(mrpaRegistrySaved.getId());
        mrpaRegistryWithFioDTOS.credential = mrpaRegistryService.getCredentialByMrpaRegistryId(mrpaRegistrySaved.getId());
        return new ResponseEntity<>(mrpaRegistryWithFioDTOS, HttpStatus.OK);
    }

    @Operation(summary = "Отозвать МЧД",
            description = "Возвращает МЧД с ФИО представителя и ФИО доверителя" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно отозвано" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @PutMapping(value = "recall/{id}", produces = "application/json; charset=UTF-8" )
    public ResponseEntity<?> recallMrpaRegistryById(@PathVariable @Parameter(name = "id",
            description = "ИД МЧД", example = "1" ) Long id) {
        try {
            mrpaRegistryService.recallMrpaRegistryById(id);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(ID_IS_NULL);
        }
        MrpaRegistryWithFioDTO mrpaRegistryWithFioDTOS = mrpaRegistryService.findByMrpaRegistryId(id);
        mrpaRegistryWithFioDTOS.credential = mrpaRegistryService.getCredentialByMrpaRegistryId(id);
        return new ResponseEntity<>(mrpaRegistryWithFioDTOS, HttpStatus.OK);
    }

    @Operation(summary = "Выгрузить МЧД в XML-файл", description = "Возвращает статус выполнения загрузки")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно загружено"),
            @ApiResponse(code = 404, message = "Not found")
    })
    @GetMapping(value = "/download/{id}", produces = MediaType.APPLICATION_XML_VALUE) //"application/json; charset=UTF-8"
    public ResponseEntity<?> download(@PathVariable @Parameter(name = "id",
            description = "ИД МЧД", example = "1" ) Long id) throws IOException, JAXBException {
        MrpaRegistryFullDTO mrpaRegistryFullDTO = mrpaRegistryService.findMrpaRegistryFullById(id);
        mrpaRegistryFullDTO.credential = mrpaRegistryService.getCredentialByMrpaRegistryFullId(id);
        MrpaRegistryFullXML mrpaRegistryFullXML = mrpaRegistryService.ConvertMrpaRegistryFullDtoToFullXml(mrpaRegistryFullDTO);
        String xmlString;
        xmlString = XMLUtil.marshall(mrpaRegistryFullXML, MrpaRegistryFullXML.class);

        assertNotNull(xmlString);
        return new ResponseEntity<>(xmlString, HttpStatus.OK);
    }
}
