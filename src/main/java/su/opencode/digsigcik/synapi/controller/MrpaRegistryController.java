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
import su.opencode.digsigcik.synapi.dto.MrpaRegistrySaveWithCredentialDTO;
import su.opencode.digsigcik.synapi.dto.MrpaRegistryWithFioDTO;
import su.opencode.digsigcik.synapi.enums.StatusTypeEnum;
import su.opencode.digsigcik.synapi.exception.BadRequestException;
import su.opencode.digsigcik.synapi.model.MrpaRegistry;
import su.opencode.digsigcik.synapi.model.MrpaRegistryCredential;
import su.opencode.digsigcik.synapi.model.ca.CAXml;
import su.opencode.digsigcik.synapi.service.MrpaRegistryCredentialService;
import su.opencode.digsigcik.synapi.service.MrpaRegistryService;
import su.opencode.digsigcik.synapi.util.XMLUtil;

import javax.validation.ConstraintViolationException;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.List;

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
    @GetMapping()
    public List<MrpaRegistryWithFioDTO> findAllMrpaRegistry() {
        return mrpaRegistryService.findAllMrpaRegistry();
    }

    @Operation(summary = "Сохранить данные по МЧД в БД", description = "Возвращает данные по созданному МЧД" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно сохранено" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @PostMapping("save" )
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

    @Operation(summary = "Загрузить ЭП", description = "Возвращает МЧД")
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
        List<MrpaRegistryWithFioDTO> mrpaRegistryWithFioDTOS = mrpaRegistryService.findByMrpaRegistryId(mrpaRegistrySaved.getId());
        return new ResponseEntity<>(mrpaRegistryWithFioDTOS, HttpStatus.OK);
    }

    @Operation(summary = "Отозвать МЧД",
            description = "Возвращает МЧД с ФИО представителя и ФИО доверителя" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно отозвано" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @GetMapping("recall/{id}" )
    public ResponseEntity<?> recallMrpaRegistryById(@PathVariable @Parameter(name = "id",
            description = "ИД МЧД", example = "1" ) Long id) {
        try {
            mrpaRegistryService.recallMrpaRegistryById(id);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(ID_IS_NULL);
        }
        List<MrpaRegistryWithFioDTO> mrpaRegistryWithFioDTOS = mrpaRegistryService.findAllMrpaRegistry();
        return new ResponseEntity<>(mrpaRegistryWithFioDTOS, HttpStatus.OK);
    }

    @Operation(summary = "Выгрузить МЧД в XML-файл", description = "Возвращает статус выполнения загрузки")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно загружено"),
            @ApiResponse(code = 404, message = "Not found")
    })
    @PostMapping(value = "/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public String upload(@RequestPart("srcFile") MultipartFile sourceFile) throws IOException, JAXBException {
        String content = new String(sourceFile.getBytes());
        Document document = convertStringToDocument(content);
        assert document != null;
        CAXml caXml = XMLUtil.unmarshal(document, CAXml.class);

//        for(CAXml.CA ca : caXml.getCa()){
//            var stat = ca.getAccreditationStatus().getStatus();
//            List<CertificateData> certData = ca.getShSystems().getShSystem().stream().map(y->y.getKeysAuthPersons().getKeyAuthPerson()
//                    .stream().map(z->z.getCertificates().getCertificateData()).collect(Collectors.toList())).collect(Collectors.toList()).stream().flatMap(Collection::stream).flatMap(Collection::stream).collect(Collectors.toList());
//            for(CertificateData cert : certData){
//
//                var substr = cert.getIssuedTo().split(", ");
//                for(String s : substr) {
//                    var st = s.substring(0,3);
//                    if (st.equals("CN="))
//                        cert.setIssuedTo(s.substring(3));
//                }
//                cert.setTypeInput("Автоматически");
//                cert.setAccreditationStatus(stat);
//                certificateDataService.saveCertificateData(cert);
//            }
//        }
        return "OK";
    }
}
