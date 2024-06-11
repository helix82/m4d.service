package su.opencode.digsigcik.synapi.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import su.opencode.digsigcik.synapi.dto.MrpaRegistryFullDTO;
import su.opencode.digsigcik.synapi.dto.MrpaRegistryWithFioDTO;
import su.opencode.digsigcik.synapi.enums.StatusTypeEnum;
import su.opencode.digsigcik.synapi.model.MrpaRegistry;
import su.opencode.digsigcik.synapi.model.MrpaRegistryCredential;
import su.opencode.digsigcik.synapi.model.ca.CAXml;
import su.opencode.digsigcik.synapi.service.MrpaRegistryCredentialService;
import su.opencode.digsigcik.synapi.service.MrpaRegistryService;
import su.opencode.digsigcik.synapi.util.XMLUtil;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.List;

import static su.opencode.digsigcik.synapi.util.XMLUtil.convertStringToDocument;

@RestController
@RequestMapping("/api/v1/mrpa_registry")
@AllArgsConstructor
public class MrpaRegistryController {
    private final MrpaRegistryService service;
    private final MrpaRegistryCredentialService mrpaRegistryCredentialService;

    @Operation(summary = "Получить список МЧД с ФИО представителя и ФИО доверителя", description = "Возвращает список МЧД с ФИО представителя и ФИО доверителя")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно получено"),
            @ApiResponse(code = 404, message = "Not found - Ни одна МЧД не найдена")
    })
    @GetMapping()
    public List<MrpaRegistryWithFioDTO>  findAllMrpaRegistry() {
        return service.findAllMrpaRegistry();
    }

    @Operation(summary = "Получить список МЧД с ФИО представителя и ФИО доверителя с фильтром по пользователю", description = "Возвращает список МЧД с ФИО представителя и ФИО доверителя")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно получено"),
            @ApiResponse(code = 404, message = "Not found - Ни одна МЧД для пользователя не найдена")
    })
    @GetMapping("list/{userId}")
    public List<MrpaRegistryWithFioDTO> findListMrpaRegistryByUserId(@PathVariable @Parameter(name = "userId", description = "ИД пользователя", example = "1") Long userId) {
        return service.findAllByMrpaRegistryUserId(userId);
    }

    @Operation(summary = "Сохранить данные по МЧД в БД", description = "Возвращает данные по созданному МЧД")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно сохранено"),
            @ApiResponse(code = 404, message = "Not found")
    })
    @PostMapping("save")
    public List<MrpaRegistryWithFioDTO> saveMrpaRegistry(@RequestBody MrpaRegistry mrpaRegistry) {
        mrpaRegistry.setStatus(StatusTypeEnum.NOT_SIGNED);
        MrpaRegistry mrpaRegistrySaved = service.saveMrpaRegistry(mrpaRegistry);
        return service.findByMrpaRegistryId(mrpaRegistrySaved.getId());
    }

    @Operation(summary = "Получить МЧД с ФИО представителя и ФИО доверителя по ИД МЧД", description = "Возвращает МЧД с ФИО представителя и ФИО доверителя")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно получено"),
            @ApiResponse(code = 404, message = "Not found - Ни одна МЧД не найдена")
    })
    @GetMapping("/{id}")
    public List<MrpaRegistryWithFioDTO> findMrpaRegistryById(@PathVariable @Parameter(name = "id", description = "ИД МЧД", example = "1") Long id) {
        return service.findByMrpaRegistryId(id);
    }

    @Operation(summary = "Получить МЧД по ИД пользователя, вся информация", description = "Возвращает всю информацию по МЧД")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно получено"),
            @ApiResponse(code = 404, message = "Not found - Ни одна МЧД не найдена")
    })
    @GetMapping("user_id/{id}")
    public List<MrpaRegistryFullDTO> findMrpaRegistryByUserId(@PathVariable @Parameter(name = "id", description = "ИД пользователя", example = "1") Long id) {
        return service.findByMrpaRegistryUserId(id);
    }

    @Operation(summary = "Изменить данные по МЧД", description = "Возвращает основные данные по МЧД")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Изменения успешно сохранены"),
            @ApiResponse(code = 404, message = "Not found")
    })
    @PutMapping("update")
    public List<MrpaRegistryWithFioDTO> updateMrpaRegistry(@RequestBody MrpaRegistry mrpaRegistry) {
        MrpaRegistry mrpaRegistryUpdated = service.updateMrpaRegistry(mrpaRegistry);
        return service.findByMrpaRegistryId(mrpaRegistryUpdated.getId());
    }

    @Operation(summary = "Удалить данные по МЧД", description = "Возвращает список МЧД с ФИО представителя и ФИО доверителя")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Запись успешно удалена"),
            @ApiResponse(code = 404, message = "Not found")
    })
    @DeleteMapping("delete/{id}")
    public List<MrpaRegistryWithFioDTO> deleteMrpaRegistry(@PathVariable @Parameter(name = "id", description = "ИД МЧД", example = "1") Long id) {
        service.deleteMrpaRegistry(id);
        return service.findAllMrpaRegistry();
    }

    @Operation(summary = "Привязать полномочия к МЧД", description = "Возвращает данные о привязке полномочий к МЧД")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно привязано"),
            @ApiResponse(code = 404, message = "Not found")
    })
    @PutMapping("/{mrpaId}/credential/{credentialId}")
    public MrpaRegistryCredential assignCredentialToMrpaRegistry(@PathVariable @Parameter(name = "mrpaId", description = "ИД МЧД", example = "1") Long mrpaId, @PathVariable @Parameter(name = "credentialId", description = "ИД полномочия", example = "1") Long credentialId) {
        MrpaRegistryCredential mrpaRegistryCredential = new MrpaRegistryCredential();
        mrpaRegistryCredential.setMrpaRegistryId(mrpaId);
        mrpaRegistryCredential.setCredentialId(credentialId);
        return mrpaRegistryCredentialService.saveMrpaRegistryCredential(mrpaRegistryCredential);
    }

    @Operation(summary = "Получить МЧД с ФИО представителя и ФИО доверителя по ИНН доверителя", description = "Возвращает МЧД с ФИО представителя и ФИО доверителя")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно получено"),
            @ApiResponse(code = 404, message = "Not found - Ни одна МЧД не найдена")
    })
    @GetMapping("principal_inn/{inn}")
    public List<MrpaRegistryWithFioDTO> findMrpaRegistryByPrincipalInn(@PathVariable @Parameter(name = "inn", description = "ИНН доверителя", example = "1") String inn) {
        return service.findByMrpaRegistryPrincipalInn(inn);
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
