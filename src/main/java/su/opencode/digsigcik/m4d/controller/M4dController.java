package su.opencode.digsigcik.m4d.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;
import su.opencode.digsigcik.m4d.annotation.M4dExceptionHandler;
import su.opencode.digsigcik.m4d.dto.M4dFullXML;
import su.opencode.digsigcik.m4d.dto.M4dRequestDto;
import su.opencode.digsigcik.m4d.dto.M4dResponseDto;
import su.opencode.digsigcik.m4d.enums.FileMimeTypeEnum;
import su.opencode.digsigcik.m4d.enums.HashAlgorithm;
import su.opencode.digsigcik.m4d.exception.BadRequestException;
import su.opencode.digsigcik.m4d.model.*;
import su.opencode.digsigcik.m4d.service.M4dFileService;
import su.opencode.digsigcik.m4d.service.M4dService;
import su.opencode.digsigcik.m4d.util.CustomMultipartFile;
import su.opencode.digsigcik.m4d.util.XMLUtil;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.util.Objects.isNull;
import static su.opencode.digsigcik.m4d.util.Constants.*;

@Slf4j
@RestController
@M4dExceptionHandler
@RequestMapping("/api/v1/m4d")
@Tag(name = "M4d Controller")
public class M4dController {
    private final ModelMapper modelMapper;
    private final M4dService m4dService;
    private final M4dFileService m4dFileService;

    public M4dController(ModelMapper modelMapper, M4dService m4dService, M4dFileService m4dFileService) {
        this.modelMapper = modelMapper;
        this.m4dService = m4dService;
        this.m4dFileService = m4dFileService;
    }

    @Operation(summary = CONTROLLER_MRPA_FIND_ALL_SUMMARY,
            description = CONTROLLER_MRPA_FIND_ALL_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @GetMapping(produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> findAllMrpaRegistry() {
        List<M4dResponseDto> mrpaRegistryFindAll;
        try {
            mrpaRegistryFindAll = this.m4dService.findAllMrpaRegistry();
        } catch (ConstraintViolationException | ParseException e) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(mrpaRegistryFindAll, HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_MRPA_SAVE_SUMMARY, description = CONTROLLER_MRPA_SAVE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @PostMapping(value = "save", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> saveAllDataMrpaRegistry(@RequestBody M4dRequestDto m4dSaveFullDto) throws JAXBException, ParseException {
        m4dSaveFullDto.setGuid();
        m4dSaveFullDto.setCreateDate(LocalDateTime.now().toString());
        M4d m4d = convertMrpaRegistryToEntity(m4dSaveFullDto.mrpaRegistry);
        m4d.setCreateDate(LocalDateTime.now());
        m4d.setStatus("CREATED");
        Organisation organisation = convertOrganisationToEntity(m4dSaveFullDto.organisation);
        Principal principal = convertPrincipalToEntity(m4dSaveFullDto.principal);
        PrincipalDetail principalDetail = convertPrincipalDetailToEntity(m4dSaveFullDto.principalDetail);
        Representative representative = convertRepresentativeToEntity(m4dSaveFullDto.representative);
        RepresentativeDetail representativeDetail = convertRepresentativeDetailToEntity(m4dSaveFullDto.representativeDetail);
        M4dFullXML m4dFullXML = this.m4dService.convertM4dRequestDtoToFullXml(m4dSaveFullDto);
        String xmlString = XMLUtil.marshall(m4dFullXML, M4dFullXML.class);
        Long m4dSaved;
        try {
            m4dSaved = this.m4dService.saveAllDataMrpaRegistry(m4d, organisation, principal, principalDetail, representative, representativeDetail, null, List.of(m4dSaveFullDto.getCredentials()), xmlString);
        } catch (ConstraintViolationException e) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
        return findMrpaRegistryById(m4dSaved);
    }

    @Operation(summary = CONTROLLER_MRPA_FIND_BY_ID_SUMMARY,
            description = CONTROLLER_MRPA_FIND_BY_ID_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> findMrpaRegistryById(@PathVariable @Parameter(name = "id",
            description = PARAMETER_MRPA_ID_DESCRIPTION, example = "1") Long id) throws ParseException {
        if (id == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(ID_IS_NULL), HttpStatus.BAD_REQUEST);
        }
        M4d m4d;
        try {
            m4d = this.m4dService.findById(id);
        } catch (ConstraintViolationException e) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
        if (m4d == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_REGISTRY_IS_NULL), HttpStatus.NOT_FOUND);
        }
        M4dResponseDto m4dResponseDto = this.m4dService.convertM4dToM4dResponseDto(m4d, true, false, true, false);
        return new ResponseEntity<>(m4dResponseDto, HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_MRPA_FIND_BY_GUID_SUMMARY,
            description = CONTROLLER_MRPA_FIND_BY_GUID_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @GetMapping(value = "/guid/{guid}", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> findMrpaRegistryByGuid(@PathVariable @Parameter(name = "guid",
            description = PARAMETER_MRPA_GUID_DESCRIPTION, example = "1") String guid) throws ParseException, JAXBException {
        if (guid == null || guid.isEmpty()) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(GUID_IS_NULL), HttpStatus.BAD_REQUEST);
        }
        M4d m4d;
        try {
            m4d = this.m4dService.findMrpaRegistryByGuid(guid);
        } catch (ConstraintViolationException e) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
        if (m4d == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_REGISTRY_IS_NULL), HttpStatus.NOT_FOUND);
        }
        M4dResponseDto m4dResponseDto = this.m4dService.convertM4dToM4dResponseDto(m4d, true, false, true, false);
        return new ResponseEntity<>(m4dResponseDto, HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_MRPA_FIND_BY_REPRESENTATIVE_ID_SUMMARY,
            description = CONTROLLER_MRPA_FIND_BY_REPRESENTATIVE_ID_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @GetMapping(value = "/representative/{inn}", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> findMrpaRegistryByRepresentativeInn(@PathVariable @Parameter(name = "inn",
            description = PARAMETER_REPRESENTATIVE_INN_DESCRIPTION, example = "1") String inn) throws ParseException {
        if (inn == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(INN_IS_NULL), HttpStatus.BAD_REQUEST);
        }
        List<M4d> m4d;
        try {
            m4d = this.m4dService.findM4dByRepresentativeInn(inn);
        } catch (ConstraintViolationException e) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
        if (m4d == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_REGISTRY_IS_NULL), HttpStatus.NOT_FOUND);
        }
        List<M4dResponseDto> m4dResponseDtoList = new ArrayList<>();
        for (M4d m : m4d) {
            M4dResponseDto m4dResponseDto = this.m4dService.convertM4dToM4dResponseDto(m, false, false, false, false);
            m4dResponseDto.credentials = m4dService.getCredentialByMrpaRegistryId(m.getId());
            m4dResponseDto.setStatus(this.m4dService.getStatusM4d(m.getId()));
            m4dResponseDtoList.add(m4dResponseDto);
        }
        return new ResponseEntity<>(m4dResponseDtoList, HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_MRPA_FIND_BY_ID_AND_REPRESENTATIVE_ID_SUMMARY,
            description = CONTROLLER_MRPA_FIND_BY_ID_AND_REPRESENTATIVE_ID_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @GetMapping(value = "/{id}/representative/{inn}", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> findMrpaRegistryByIdAndRepresentativeInn(@PathVariable @Parameter(name = "id",
            description = PARAMETER_MRPA_ID_DESCRIPTION, example = "1") Long id, @PathVariable @Parameter(name = "inn",
            description = PARAMETER_REPRESENTATIVE_INN_DESCRIPTION, example = "1") String inn) {
        if (isNull(id) || isNull(inn)) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(ID_IS_NULL + " or " + INN_IS_NULL), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(m4dService.findM4dByIdAndRepresentativeInn(inn, id), HttpStatus.OK);
    }


    @Operation(summary = CONTROLLER_MRPA_UPDATE_SUMMARY, description = CONTROLLER_MRPA_UPDATE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @PutMapping(value = "update", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> updateAllDataMrpaRegistry(@RequestBody M4dRequestDto m4dSaveFullDto) throws JAXBException, ParseException {
        M4d m4d = this.m4dService.findById(m4dSaveFullDto.mrpaRegistry.getId());
        m4d.setStartDate(this.convertStringToLocalDateTime(m4dSaveFullDto.mrpaRegistry.getStartDate()));
        m4d.setEndDate(this.convertStringToLocalDateTime(m4dSaveFullDto.mrpaRegistry.getEndDate()));
        Organisation organisation = convertOrganisationToEntity(m4dSaveFullDto.organisation);
        Principal principal = convertPrincipalToEntity(m4dSaveFullDto.principal);
        PrincipalDetail principalDetail = convertPrincipalDetailToEntity(m4dSaveFullDto.principalDetail);
        Representative representative = convertRepresentativeToEntity(m4dSaveFullDto.representative);
        RepresentativeDetail representativeDetail = convertRepresentativeDetailToEntity(m4dSaveFullDto.representativeDetail);
        Long m4dUpdated;
        try {
            m4dUpdated = m4dService.updateAllDataMrpaRegistry(m4d, organisation, principal, principalDetail, representative, representativeDetail, null, List.of(m4dSaveFullDto.getCredentials()));
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(MRPA_REGISTRY_IS_NULL);
        }
        return findMrpaRegistryById(m4dUpdated);
    }

    //TODO: @helix82 При удалении физически не удаляем а ставим пометку об удалении
    @Operation(summary = CONTROLLER_MRPA_DELETE_SUMMARY, description = CONTROLLER_MRPA_DELETE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @DeleteMapping(value = "delete/{id}", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> deleteMrpaRegistry(@PathVariable @Parameter(name = "id",
            description = PARAMETER_MRPA_ID_DESCRIPTION, example = "1") Long id) {
        if (id == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(ID_IS_NULL), HttpStatus.BAD_REQUEST);
        }
        M4d m4d = this.m4dService.findById(id);
        if (m4d == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_IS_NULL), HttpStatus.NOT_FOUND);
        }
        try {
            this.m4dService.deleteMrpaRegistry(id);
        } catch (ConstraintViolationException e) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
        Map<String, String> body = new HashMap<>();
        body.put("result", "true");

        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_MRPA_UPLOAD_SUMMARY, description = CONTROLLER_MRPA_UPLOAD_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @PutMapping(value = "/{id}/upload-dscp", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> uploadSig(Authentication authentication,
                                       @PathVariable @Parameter(name = "id", description = PARAMETER_MRPA_ID_DESCRIPTION, example = "1") Long id,
                                       @RequestPart("container") MultipartFile container,
                                       @RequestParam(value = "verify", required = false, defaultValue = "true") boolean verify) throws IOException, ParseException, JAXBException {
        if (id == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(ID_IS_NULL), HttpStatus.BAD_REQUEST);
        }
        if (container == null || container.isEmpty()) {
            return new ResponseEntity<>(getCustomErrorJsonFromString("Container is null"), HttpStatus.BAD_REQUEST);
        }
        if (!Objects.equals(container.getContentType(), FileMimeTypeEnum.ZIP.toString()) && !Objects.equals(container.getContentType(), FileMimeTypeEnum.DSCP.toString())) {
            System.out.println(container.getContentType());
            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_REGISTRY_WRONG_FORMAT), HttpStatus.BAD_REQUEST);
        }
        Long mrpaId = this.m4dService.uploadSig(authentication, id, container, verify);
        return findMrpaRegistryById(mrpaId);
    }

    //TODO: @helix82 Временный эндпоинт для Дениса
    @Operation(summary = CONTROLLER_MRPA_CLEAR_SIG_SUMMARY, description = CONTROLLER_MRPA_CLEAR_SIG_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @PutMapping(value = "/{id}/clear-sig", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> clearSig(@PathVariable @Parameter(name = "id",
            description = PARAMETER_MRPA_ID_DESCRIPTION, example = "1") Long id) throws JAXBException, ParseException {
        if (id == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(ID_IS_NULL), HttpStatus.BAD_REQUEST);
        }
        M4d mrpaRegistry = this.m4dService.findById(id);
        if (mrpaRegistry == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_REGISTRY_IS_NULL), HttpStatus.BAD_REQUEST);
        }
        if (mrpaRegistry.getSigFile() != null) {
            Long fileId = mrpaRegistry.getSigFile().getId();
            mrpaRegistry.setSigFile(null);
            M4d mrpaRegistrySaved;
            try {
                mrpaRegistrySaved = this.m4dService.saveMrpaRegistry(mrpaRegistry);
                this.m4dFileService.deleteM4dFile(fileId);
            } catch (ConstraintViolationException e) {
                return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
            }
            return findMrpaRegistryById(mrpaRegistrySaved.getId());
        }
        return new ResponseEntity<>(getCustomErrorJsonFromString("Signature not found"), HttpStatus.BAD_REQUEST);
    }

    @Deprecated()
    @Operation(summary = CONTROLLER_MRPA_UPLOAD_XML_SUMMARY, description = CONTROLLER_MRPA_UPLOAD_XML_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> upload(@RequestPart("srcFile") MultipartFile sourceFile) throws IOException, ParseException, SAXException, JAXBException {
        String extension = FilenameUtils.getExtension(sourceFile.getOriginalFilename());
        if (!Objects.equals(extension, "xml")) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_REGISTRY_WRONG_FORMAT_XML), HttpStatus.BAD_REQUEST);
        }
        Long m4dId = this.m4dService.uploadMrpaRegistry(sourceFile);
        return new ResponseEntity<>(findMrpaRegistryById(m4dId), HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_MRPA_VERIFY_XML_SUMMARY, description = CONTROLLER_MRPA_VERIFY_XML_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @PostMapping(value = "/verify", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> verify(@RequestPart("srcFile") MultipartFile sourceFile) throws Exception {
        return new ResponseEntity<>(m4dService.verify(sourceFile), HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_MRPA_STATUS_VERIFY_XML_SUMMARY, description = CONTROLLER_MRPA_STATUS_VERIFY_XML_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @PostMapping(value = "/verify/{guid}", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> getStatusVerifyInFns(@PathVariable @Parameter(name = "guid",
            description = PARAMETER_MRPA_GUID_DESCRIPTION, example = "1") String guid) {
        if (guid == null || guid.isEmpty()) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(GUID_IS_NULL), HttpStatus.BAD_REQUEST);
        }
        M4dVerifyStatus m4dVerifyStatus;
        try {
            m4dVerifyStatus = this.m4dService.getStatusVerifyInFns(guid);
        } catch (ConstraintViolationException e) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
        if (m4dVerifyStatus == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_VERIFY_STATUS_IS_NULL), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(m4dVerifyStatus, HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_MRPA_FIND_ALL_FOR_USER_SUMMARY,
            description = CONTROLLER_MRPA_FIND_ALL_FOR_USER_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @GetMapping(value = "/me", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> findAllMrpaRegistryForUser(Authentication authentication) throws ParseException {
        return new ResponseEntity<>(m4dService.findAllM4dUsers(authentication), HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_MRPA_IMPORT_SUMMARY, description = CONTROLLER_MRPA_IMPORT_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE),
    })
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> importMrpa(
            Authentication authentication,
            @RequestPart("srcFile") MultipartFile sourceFile,
            @RequestPart(value="sigFile", required = false) MultipartFile sourceSig,
            @RequestParam(value="hashAlgorithm", required = false, defaultValue = "GOST3411_2012_256") HashAlgorithm hashAlgorithm) throws IOException, JAXBException, ParseException {
        if (sourceFile.isEmpty()){
            return new ResponseEntity<>(getCustomErrorJsonFromString("Обязательный xml-файл МЧД не указан"), HttpStatus.BAD_REQUEST);
        }
        Long m4dId;
        //upload m4d !!!!!!!!!!!!!!!!!!!!!!!!
        try {
            m4dId = this.m4dService.uploadMrpaRegistry(sourceFile);
        } catch (Exception e) {
            log.error("Ошибка импорта МЧД: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(getCustomErrorJsonFromString("Ошибка импорта МЧД: " + e.getMessage()), HttpStatus.BAD_REQUEST);
        }
        if (m4dId == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString("Ошибка импорта МЧД"), HttpStatus.BAD_REQUEST);
        }
        if (sourceSig != null){
            //create and save container !!!!!!!!!!!!!!!!!!!!!!!!!!!!
            String tempFileName;
            MultipartFile container = null;

            String originalFilename = sourceFile.getOriginalFilename();
            if (originalFilename != null) {
                String filenameWithoutExtension = originalFilename.substring(0, originalFilename.lastIndexOf('.'));
                tempFileName = "/tmp/" + filenameWithoutExtension + ".dscp";
            } else {
                String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                tempFileName = "/tmp/container_" + timestamp + ".dscp";
            }
            ByteArrayResource response = this.m4dService.createContainer(authentication, sourceFile, sourceSig, hashAlgorithm);
            if(response != null) {
                try (OutputStream outputStream = new FileOutputStream(tempFileName)) {
                    outputStream.write(response.getByteArray());
                    log.error("Контейнер успешно сохранен: " + tempFileName);
                    container = new CustomMultipartFile(new File(tempFileName));
                } catch (IOException e) {
                    log.error("Ошибка при сохранении контейнера: " + e.getMessage());
                    return new ResponseEntity<>(getCustomErrorJsonFromString("Ошибка при сохранении контейнера: " + e.getMessage()), HttpStatus.BAD_REQUEST);
                }
            }
            //upload sig for m4d !!!!!!!!!!!!!!!!!!!!!!
            try {
                var result = this.m4dService.uploadSig(authentication, m4dId, container, false);

                if (result == null) {
                    log.error("Загрузка подписи МЧД не вернула результат");
                    return new ResponseEntity<>(getCustomErrorJsonFromString("Загрузка подписи МЧД не вернула результат"), HttpStatus.BAD_REQUEST);
                }
            } catch (Exception e) {
                log.error("Ошибка при загрузке подписи МЧД: " + e.getMessage());
                e.printStackTrace();
                return new ResponseEntity<>(getCustomErrorJsonFromString("Ошибка при загрузке подписи МЧД: " + e.getMessage()), HttpStatus.BAD_REQUEST);
            }
        }
        return findMrpaRegistryById(m4dId);
    }

    @Operation(summary = CONTROLLER_MRPA_AS_ARCHIVE_SUMMARY, description = CONTROLLER_MRPA_AS_ARCHIVE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @GetMapping(value = "/{id}/archive", produces = "application/zip")
    public ResponseEntity<?> archive(Authentication authentication, HttpServletResponse response,
                                     @PathVariable @Parameter(name = "id", description = PARAMETER_MRPA_ID_DESCRIPTION, example = "1") Long id) throws IOException, ParseException, JAXBException {
        if (id == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(ID_IS_NULL), HttpStatus.BAD_REQUEST);
        }
        M4d m4d;
        try {
            m4d = this.m4dService.findById(id);
        } catch (ConstraintViolationException e) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
        if (m4d == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_REGISTRY_IS_NULL), HttpStatus.NOT_FOUND);
        }

        String filename = m4d.getXmlFile().getFilename();
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            filename = filename.substring(0, lastDotIndex);
        }
        if (m4d.getSigFile()==null){
            M4dResponseDto m4dResponseDto = this.m4dService.convertM4dToM4dResponseDto(m4d, true, false, true, false);
            String xmlString = m4dResponseDto.getXmlFile().getContent();
            try {
                response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + ".xml\"");

                return new ResponseEntity<>(
                        xmlString,
                        HttpStatus.OK
                );
            } catch (EntityNotFoundException e) {
                return ResponseEntity.noContent().build();
            }
        } else {
            ByteArrayResource resp = this.m4dService.createArchiveM4dSig(authentication, m4d.getSigFile().getId(), filename);
            if (resp == null) {
                return new ResponseEntity<>(getCustomErrorJsonFromString("Ошибка создания архива"), HttpStatus.NOT_FOUND);
            }
            try {
                response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + ".zip\"");

                return new ResponseEntity<>(
                        resp,
                        HttpStatus.OK
                );
            } catch (EntityNotFoundException e) {
                return ResponseEntity.noContent().build();
            }
        }

    }

    private M4d convertMrpaRegistryToEntity(M4dRequestDto.MrpaRegistry mrpaRegistryDto) {
        M4d m4d = new M4d();
        m4d.setStartDate(this.convertStringToLocalDateTime(mrpaRegistryDto.getStartDate()));
        m4d.setEndDate(this.convertStringToLocalDateTime(mrpaRegistryDto.getEndDate()));
        m4d.setGuid(mrpaRegistryDto.getGuid());
        return m4d;
    }

    private Organisation convertOrganisationToEntity(M4dRequestDto.Organisation organisationDto) {
        return modelMapper.map(organisationDto, Organisation.class);
    }

    private Principal convertPrincipalToEntity(M4dRequestDto.Principal principalDto) {
        return modelMapper.map(principalDto, Principal.class);
    }

    private PrincipalDetail convertPrincipalDetailToEntity(M4dRequestDto.PrincipalDetail principalDetailDto) {
        return modelMapper.map(principalDetailDto, PrincipalDetail.class);
    }

    private Representative convertRepresentativeToEntity(M4dRequestDto.Representative representativeDto) {
        Representative representative = new Representative();
        representative.setId(representativeDto.getId());
        representative.setInn(representativeDto.getInn());
        representative.setSnils(representativeDto.getSnils());
        representative.setBirthDate(this.convertStringToLocalDateTime(representativeDto.getBirthDate()));
        return representative;
    }

    private RepresentativeDetail convertRepresentativeDetailToEntity(M4dRequestDto.RepresentativeDetail representativeDetailDto) {
        RepresentativeDetail representativeDetail = new RepresentativeDetail();
        representativeDetail.setId(representativeDetailDto.getId());
        representativeDetail.setSurname(representativeDetailDto.getSurname());
        representativeDetail.setName(representativeDetailDto.getName());
        representativeDetail.setPatronymic(representativeDetailDto.getPatronymic());
        representativeDetail.setSeriesNumber(representativeDetailDto.getSeriesNumber());
        representativeDetail.setDateIssue(this.convertStringToLocalDateTime(representativeDetailDto.getDateIssue()));
        representativeDetail.setWhoIssue(representativeDetailDto.getWhoIssue());
        representativeDetail.setDepartmentCode(representativeDetailDto.getDepartmentCode());
        representativeDetail.setRepresentativeId(representativeDetailDto.getRepresentativeId());
        return representativeDetail;
    }

    public Map<String, String> getCustomErrorJsonFromString(String text) {
        Map<String, String> body = new HashMap<>();
        body.put("error", text);
        return body;
    }

    private LocalDateTime convertStringToLocalDateTime(String date) {
        DateTimeFormatter formatter;
        LocalDateTime localDateTime;
        if (date.length() == 8) {
            formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            LocalDate localDate = LocalDate.parse(date, formatter);
            localDateTime = localDate.atStartOfDay().atZone(ZoneOffset.UTC).toLocalDateTime();
        } else if (date.length() == 10) {
            formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate localDate = LocalDate.parse(date, formatter);
            localDateTime = localDate.atStartOfDay().atZone(ZoneOffset.UTC).toLocalDateTime();
        } else if (date.length() > 20) {
            formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
            localDateTime = LocalDateTime.from(formatter.parse(date));
        } else if (date.length() == 16) {
            formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
            localDateTime = LocalDateTime.from(formatter.parse(date));
        } else {
            formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX");
            localDateTime = LocalDateTime.from(formatter.parse(date));
        }
        return localDateTime;
    }
}
