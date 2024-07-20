package su.opencode.digsigcik.synapi.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import su.opencode.digsigcik.synapi.annotation.MchdExceptionHandler;
import su.opencode.digsigcik.synapi.dto.MrpaRegistryFullDTO;
import su.opencode.digsigcik.synapi.dto.m4d.MrpaRegistryFullXML;
import su.opencode.digsigcik.synapi.dto.MrpaRegistrySaveWithCredentialDTO;
import su.opencode.digsigcik.synapi.dto.MrpaRegistryWithFioDTO;
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
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static su.opencode.digsigcik.synapi.util.Constants.*;
import static su.opencode.digsigcik.synapi.util.XMLUtil.convertStringToDocument;

@RestController
@MchdExceptionHandler
@RequestMapping("/api/v1/mrpa_registry" )
@AllArgsConstructor
public class MrpaRegistryController {
    private final MrpaRegistryService mrpaRegistryService;
    private final MrpaRegistryCredentialService mrpaRegistryCredentialService;

    @Operation(summary = CONTROLLER_MRPA_FIND_ALL_SUMMARY,
            description = CONTROLLER_MRPA_FIND_ALL_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
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

    @Operation(summary = CONTROLLER_MRPA_SAVE_SUMMARY, description = CONTROLLER_MRPA_SAVE_DESCRIPTION )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @PostMapping(value = "save",produces = "application/json; charset=UTF-8" )
    public ResponseEntity<?> saveMrpaRegistry(@RequestBody MrpaRegistrySaveWithCredentialDTO mrpaRegistrySaveWithCredentialDTO) throws ParseException, JAXBException {
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

        Long[] credentials = mrpaRegistrySaveWithCredentialDTO.getCredentials();
        for(Long s : credentials) {
            MrpaRegistryCredential mrpaRegistryCredential = new MrpaRegistryCredential();
            mrpaRegistryCredential.setMrpaRegistryId(mrpaRegistrySaved.getId());
            mrpaRegistryCredential.setCredentialId(s);
            MrpaRegistryCredential mrpaRegistryCredentialSaved;
            try {
                mrpaRegistryCredentialSaved = mrpaRegistryCredentialService.saveMrpaRegistryCredential(mrpaRegistryCredential);
            } catch (ConstraintViolationException e) {
                throw new BadRequestException(MRPA_REGISTRY_CREDENTIAL_IS_NULL);
            }
            System.out.println(mrpaRegistryCredentialSaved);
        }
//--- Generate and save XML M4D ---
        MrpaRegistryFullDTO mrpaRegistryFullDTO = mrpaRegistryService.findMrpaRegistryFullById(mrpaRegistrySaved.getId());
        mrpaRegistryFullDTO.credential = mrpaRegistryService.getCredentialByMrpaRegistryFullId(mrpaRegistrySaved.getId());
        MrpaRegistryFullXML mrpaRegistryFullXML = mrpaRegistryService.ConvertMrpaRegistryFullDtoToFullXml(mrpaRegistryFullDTO);
        String xmlString = XMLUtil.marshall(mrpaRegistryFullXML, MrpaRegistryFullXML.class);
        try {
            mrpaRegistryService.saveMrpaXml(mrpaRegistrySaved.getId(), xmlString, mrpaRegistryFullXML.getIdFile());
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(MRPA_REGISTRY_XML_IS_NULL);
        }
//--------------------


        MrpaRegistryWithFioDTO mrpaRegistryWithFioDTOS = mrpaRegistryService.findByMrpaRegistryId(mrpaRegistrySaved.getId());
        mrpaRegistryWithFioDTOS.credential = mrpaRegistryService.getCredentialByMrpaRegistryId(mrpaRegistryWithFioDTOS.getId());
        return new ResponseEntity<>(mrpaRegistryWithFioDTOS, HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_MRPA_FIND_BY_ID_SUMMARY,
            description = CONTROLLER_MRPA_FIND_BY_ID_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @GetMapping(value = "/{id}", produces = "application/json; charset=UTF-8" )
    public ResponseEntity<?> findMrpaRegistryById(@PathVariable @Parameter(name = "id",
            description = PARAMETER_MRPA_ID_DESCRIPTION, example = "1" ) Long id) {
        MrpaRegistryFullDTO mrpaRegistryFind;
        try {
            mrpaRegistryFind = mrpaRegistryService.findMrpaRegistryFullById(id);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(MRPA_REGISTRY_IS_NULL);
        }
        mrpaRegistryFind.credential = mrpaRegistryService.getCredentialByMrpaRegistryFullId(mrpaRegistryFind.getId());
        return new ResponseEntity<>(mrpaRegistryFind, HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_MRPA_UPDATE_SUMMARY, description = CONTROLLER_MRPA_UPDATE_DESCRIPTION )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
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

    //TODO: @helix82 При удалении физически не удаляем а ставим пометку об удалении
    @Operation(summary = CONTROLLER_MRPA_DELETE_SUMMARY, description = CONTROLLER_MRPA_DELETE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @DeleteMapping(value = "delete/{id}", produces = "application/json; charset=UTF-8" )
    public ResponseEntity<?> deleteMrpaRegistry(@PathVariable @Parameter(name = "id",
            description = PARAMETER_MRPA_ID_DESCRIPTION, example = "1" ) Long id) {
        try {
            mrpaRegistryService.deleteMrpaRegistry(id);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(ID_IS_NULL);
        }
        Map<String, String> body = new HashMap<>();
        body.put("result", "true");

        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_MRPA_FIND_BY_PRINCIPAL_INN_SUMMARY,
            description = CONTROLLER_MRPA_FIND_BY_PRINCIPAL_INN_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @GetMapping(value = "principal_inn/{inn}", produces = "application/json; charset=UTF-8" )
    public ResponseEntity<?> findMrpaRegistryByPrincipalInn(@PathVariable @Parameter(name = "inn",
            description = PARAMETER_PRINCIPAL_INN_DESCRIPTION, example = "1" ) String inn) {
        MrpaRegistryWithFioDTO mrpaRegistryFindByInn;
        try {
            mrpaRegistryFindByInn = mrpaRegistryService.findByMrpaRegistryPrincipalInn(inn);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(MRPA_REGISTRY_IS_NULL);
        }
        mrpaRegistryFindByInn.credential = mrpaRegistryService.getCredentialByMrpaRegistryId(mrpaRegistryFindByInn.getId());
        return new ResponseEntity<>(mrpaRegistryFindByInn, HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_MRPA_UPLOAD_SUMMARY, description = CONTROLLER_MRPA_UPLOAD_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @PostMapping(value = "/{id}/upload_sig", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> upload(@PathVariable @Parameter(name = "id",
            description = PARAMETER_MRPA_ID_DESCRIPTION, example = "1" ) Long id, @RequestPart("sigFile") MultipartFile sourceSig) throws IOException {
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

    @Operation(summary = CONTROLLER_MRPA_RECALL_SUMMARY,
            description = CONTROLLER_MRPA_RECALL_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @PutMapping(value = "recall/{id}", produces = "application/json; charset=UTF-8" )
    public ResponseEntity<?> recallMrpaRegistryById(@PathVariable @Parameter(name = "id",
            description = PARAMETER_MRPA_ID_DESCRIPTION, example = "1" ) Long id) {
        try {
            mrpaRegistryService.recallMrpaRegistryById(id);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(ID_IS_NULL);
        }
        MrpaRegistryWithFioDTO mrpaRegistryWithFioDTOS = mrpaRegistryService.findByMrpaRegistryId(id);
        mrpaRegistryWithFioDTOS.credential = mrpaRegistryService.getCredentialByMrpaRegistryId(id);
        return new ResponseEntity<>(mrpaRegistryWithFioDTOS, HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_MRPA_DOWNLOAD_SUMMARY, description = CONTROLLER_MRPA_DOWNLOAD_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @GetMapping(value = "/download/{id}", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> download(@PathVariable @Parameter(name = "id",
            description = PARAMETER_MRPA_ID_DESCRIPTION, example = "1" ) Long id) throws IOException, JAXBException, ParseException {
        MrpaRegistryWithFioDTO mrpaRegistryWithFioDTO = mrpaRegistryService.findByMrpaRegistryId(id);
        Map<String, String> body = new HashMap<>();
        body.put("xml_data", mrpaRegistryWithFioDTO.getMrpaXml());
        body.put("xml_filename", mrpaRegistryWithFioDTO.getMrpaXmlFilename());

        return new ResponseEntity<>(body, HttpStatus.OK);
    }
}
