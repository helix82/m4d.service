package su.opencode.digsigcik.m4d.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.Explode;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.enums.ParameterStyle;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.modelmapper.ModelMapper;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;
import su.opencode.digsigcik.common.exception.custom.BadRequestException;
import su.opencode.digsigcik.m4d.dto.*;
import su.opencode.digsigcik.m4d.dto.response.M4dCountAndRevokeCountResponseDto;
import su.opencode.digsigcik.m4d.dto.response.M4dResponseInfo;
import su.opencode.digsigcik.m4d.enums.StatusM4dInRrTypeEnum;
import su.opencode.digsigcik.m4d.enums.StatusM4dTypeEnum;
import su.opencode.digsigcik.m4d.model.*;
import su.opencode.digsigcik.m4d.service.*;
import su.opencode.digsigcik.m4d.util.CustomMultipartFile;
import su.opencode.digsigcik.m4d.util.DateUtils;
import su.opencode.digsigcik.m4d.util.FileUtils;
import su.opencode.digsigcik.m4d.util.XMLUtil;
import su.opencode.digsigcik.specification.SpecificationRequest;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.ParseException;
import java.util.*;

import static java.util.Objects.isNull;
import static su.opencode.digsigcik.m4d.util.Constants.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/m4d")
@Tag(name = "M4d Controller")
public class M4dController {

    private final ModelMapper modelMapper;
    private final M4dService m4dService;
    private final M4dFileService m4dFileService;
    private final M4dConfirmService m4dConfirmService;
    private final ContainerService containerService;
    private final PvvService pvvService;
    private final M4dRequestService m4dRequestService;

    @Operation(summary = CONTROLLER_MRPA_FIND_ALL_SUMMARY,
            description = CONTROLLER_MRPA_FIND_ALL_DESCRIPTION,
            deprecated = true)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @GetMapping(produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> findAllMrpaRegistry(Authentication authentication) {
        List<M4dRegistryResponseDto> mrpaRegistryFindAll;
        try {
            mrpaRegistryFindAll = this.m4dService.findAllMrpaRegistry(authentication);
        } catch (ConstraintViolationException | ParseException e) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(mrpaRegistryFindAll, HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_MRPA_FIND_ALL_PAGINATED_SUMMARY)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @GetMapping(value = "/paginated", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> findAllMrpaRegistryPaginated(
            @Parameter(in = ParameterIn.QUERY, style = ParameterStyle.DEEPOBJECT, explode = Explode.TRUE, required = true)
            @ParameterObject SpecificationRequest request,
            Authentication authentication
    ) {
        try {
            return ResponseEntity.ok(m4dService.findAllMrpaRegistry(authentication, request));
        } catch (ConstraintViolationException | ParseException e) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = CONTROLLER_MRPA_SAVE_SUMMARY, description = CONTROLLER_MRPA_SAVE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @PostMapping(produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> saveAllDataMrpaRegistry(Authentication authentication, @RequestBody M4dRegistryRequestDto m4dSaveFullDto) throws JAXBException, ParseException {
        m4dSaveFullDto.setGuid();
        m4dSaveFullDto.setCreateDate(DateUtils.getCurrentDate());
        M4d m4d = convertMrpaRegistryToEntity(m4dSaveFullDto.mrpaRegistry);
        m4d.setCreateDate(DateUtils.convertStringToLocalDateTime(DateUtils.getCurrentDate()));
        OrganizationDto organisation = convertOrganisationToEntity(m4dSaveFullDto.organisation);
        Principal principal = convertPrincipalToEntity(m4dSaveFullDto.principal);
        PrincipalDetail principalDetail = convertPrincipalDetailToEntity(m4dSaveFullDto.principalDetail);
        Representative representative = convertRepresentativeToEntity(m4dSaveFullDto.representative);
        RepresentativeDetail representativeDetail = convertRepresentativeDetailToEntity(m4dSaveFullDto.representativeDetail);
        M4dFullXML m4dFullXML = this.m4dService.convertM4dRequestDtoToFullXml(m4dSaveFullDto);
        String xmlString = XMLUtil.marshall(m4dFullXML, M4dFullXML.class);
        Long m4dSaved;
        try {
            m4dSaved = this.m4dService.saveAllDataMrpaRegistry(authentication, m4d, organisation, principal, principalDetail, representative, representativeDetail, null, List.of(m4dSaveFullDto.getCredentials()), xmlString);
        } catch (ConstraintViolationException e) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
        return findMrpaRegistryById(authentication, m4dSaved);
    }

    @Operation(summary = CONTROLLER_MRPA_FIND_BY_ID_SUMMARY,
            description = CONTROLLER_MRPA_FIND_BY_ID_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> findMrpaRegistryById(Authentication authentication, @PathVariable @Parameter(name = "id",
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
            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_IS_NULL), HttpStatus.NOT_FOUND);
        }
        M4dRegistryResponseDto m4DRegistryResponseDto = this.m4dService.convertM4dToM4dResponseDto(authentication, m4d, true, false, true, false);
        return new ResponseEntity<>(m4DRegistryResponseDto, HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_MRPA_FIND_BY_GUID_SUMMARY,
            description = CONTROLLER_MRPA_FIND_BY_GUID_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @GetMapping(value = "/guid/{guid}", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> findMrpaRegistryByGuid(Authentication authentication, @PathVariable @Parameter(name = "guid",
            description = PARAMETER_MRPA_GUID_DESCRIPTION, example = "1") String guid) throws ParseException {
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
            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_IS_NULL), HttpStatus.NOT_FOUND);
        }
        M4dRegistryResponseDto m4DRegistryResponseDto = this.m4dService.convertM4dToM4dResponseDto(authentication, m4d, true, false, true, false);
        return new ResponseEntity<>(m4DRegistryResponseDto, HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_MRPA_FIND_BY_REPRESENTATIVE_ID_SUMMARY,
            description = CONTROLLER_MRPA_FIND_BY_REPRESENTATIVE_ID_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @GetMapping(value = "/representative/{inn}", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> findMrpaRegistryByRepresentativeInn(Authentication authentication, @PathVariable @Parameter(name = "inn",
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
            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_IS_NULL), HttpStatus.NOT_FOUND);
        }
        List<M4dRegistryResponseDto> m4DRegistryResponseDtoList = new ArrayList<>();
        for (M4d m : m4d) {
            M4dRegistryResponseDto m4DRegistryResponseDto = this.m4dService.convertM4dToM4dResponseDto(authentication, m, false, false, false, false);
            m4DRegistryResponseDto.credentials = m4dService.getCredentialByMrpaRegistryId(m.getId());
            m4DRegistryResponseDto.setStatus(this.m4dService.getStatusM4d(m.getId()));
            m4DRegistryResponseDtoList.add(m4DRegistryResponseDto);
        }
        return new ResponseEntity<>(m4DRegistryResponseDtoList, HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_MRPA_FIND_BY_ID_AND_REPRESENTATIVE_ID_SUMMARY,
            description = CONTROLLER_MRPA_FIND_BY_ID_AND_REPRESENTATIVE_ID_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @GetMapping(value = "/{id}/representative/{inn}", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> findMrpaRegistryByIdAndRepresentativeInn(Authentication authentication, @PathVariable @Parameter(name = "id",
            description = PARAMETER_MRPA_ID_DESCRIPTION, example = "1") Long id, @PathVariable @Parameter(name = "inn",
            description = PARAMETER_REPRESENTATIVE_INN_DESCRIPTION, example = "1") String inn) {
        if (isNull(id) || isNull(inn)) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(ID_IS_NULL + " or " + INN_IS_NULL), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(m4dService.findM4dByIdAndRepresentativeInn(authentication, inn, id), HttpStatus.OK);
    }


    @Operation(summary = CONTROLLER_MRPA_UPDATE_SUMMARY, description = CONTROLLER_MRPA_UPDATE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @PutMapping(produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> updateAllDataMrpaRegistry(Authentication authentication, @RequestBody M4dRegistryRequestDto m4dSaveFullDto) throws ParseException {
        M4d m4d = this.m4dService.findById(m4dSaveFullDto.mrpaRegistry.getId());
        m4d.setStartDate(DateUtils.convertStringToLocalDateTime(m4dSaveFullDto.mrpaRegistry.getStartDate()));
        m4d.setEndDate(DateUtils.convertStringToLocalDateTime(m4dSaveFullDto.mrpaRegistry.getEndDate()));
        OrganizationDto organisation = convertOrganisationToEntity(m4dSaveFullDto.organisation);
        Principal principal = convertPrincipalToEntity(m4dSaveFullDto.principal);
        PrincipalDetail principalDetail = convertPrincipalDetailToEntity(m4dSaveFullDto.principalDetail);
        Representative representative = convertRepresentativeToEntity(m4dSaveFullDto.representative);
        RepresentativeDetail representativeDetail = convertRepresentativeDetailToEntity(m4dSaveFullDto.representativeDetail);
        Long m4dUpdated;
        try {
            m4dUpdated = m4dService.updateAllDataMrpaRegistry(authentication, m4d, organisation, principal, principalDetail, representative, representativeDetail, null, List.of(m4dSaveFullDto.getCredentials()));
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(MRPA_IS_NULL);
        }
        return findMrpaRegistryById(authentication, m4dUpdated);
    }

    @Operation(summary = CONTROLLER_MRPA_DELETE_SUMMARY, description = CONTROLLER_MRPA_DELETE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @DeleteMapping(value = "{id}", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> deleteMrpaRegistry(@PathVariable @Parameter(name = "id",
            description = PARAMETER_MRPA_ID_DESCRIPTION, example = "1") Long id) {
        if (id == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(ID_IS_NULL), HttpStatus.BAD_REQUEST);
        }
        M4d m4d = this.m4dService.findById(id);
        if (m4d == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_IS_NULL), HttpStatus.NOT_FOUND);
        }
        if (!m4d.getStatus().equals("NEW")){
            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_NOT_DELETE), HttpStatus.NOT_FOUND);
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
                                       @RequestPart("container") MultipartFile signature) throws Exception {
        if (id == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(ID_IS_NULL), HttpStatus.BAD_REQUEST);
        }
        if (signature == null || signature.isEmpty()) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(SIG_IS_NULL), HttpStatus.BAD_REQUEST);
        }
        String extension = FilenameUtils.getExtension(signature.getOriginalFilename());
        if (!Objects.equals(extension, "sig") && !Objects.equals(extension, "dscp")) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_REGISTRY_WRONG_FORMAT), HttpStatus.BAD_REQUEST);
        }
        M4d m4d = this.m4dService.findById(id);
        if (m4d == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_IS_NULL), HttpStatus.BAD_REQUEST);
        }
        M4d m4dUpdate;
        if (Objects.equals(extension, "dscp")) {
            M4dFile m4dFile = this.m4dFileService.findM4dFileById(m4d.getXmlFile().getId());
            MultipartFile sigFile = this.containerService.getSignatureFromContainer(authentication, signature, m4dFile.getFilename());
            m4dUpdate = this.m4dService.uploadSig(authentication, id, sigFile);
        } else {
            m4dUpdate = this.m4dService.uploadSig(authentication, id, signature);
        }
        // отправка архива с xml-файлом мчд и подписью на регистрацию в РР МЧД
        M4dConfirm m4dConfirm = this.m4dConfirmService.findM4dConfirmByM4dId(id);
        String res = this.pvvService.sendM4dToRR(authentication, m4dUpdate, m4dConfirm);
        if (res.isEmpty()) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(SENDING_RESPONSE_TO_PVV_ERROR), HttpStatus.BAD_REQUEST);
        }
        m4d.setStatus(StatusM4dTypeEnum.ACTIVE.toString());
        m4d.setStatusInRr(StatusM4dInRrTypeEnum.ONCHECK.toString());
        M4d m4dSaved;
        try {
            m4dSaved = this.m4dService.updateM4d(m4d);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(e.getMessage());
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Request-ID", res);

        return new ResponseEntity<>(findMrpaRegistryById(authentication, id).getBody(), headers, HttpStatus.OK);
    }

    //TODO: @helix82 Временный эндпоинт для Дениса
    @Operation(summary = CONTROLLER_MRPA_CLEAR_SIG_SUMMARY, description = CONTROLLER_MRPA_CLEAR_SIG_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @PutMapping(value = "/{id}/clear-sig", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> clearSig(Authentication authentication, @PathVariable @Parameter(name = "id",
            description = PARAMETER_MRPA_ID_DESCRIPTION, example = "1") Long id) throws ParseException {
        if (id == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(ID_IS_NULL), HttpStatus.BAD_REQUEST);
        }
        M4d mrpaRegistry = this.m4dService.findById(id);
        if (mrpaRegistry == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_IS_NULL), HttpStatus.BAD_REQUEST);
        }
        if (mrpaRegistry.getSigFile() != null) {
            Long fileId = mrpaRegistry.getSigFile().getId();
            mrpaRegistry.setSigFile(null);
            mrpaRegistry.setStatus(StatusM4dTypeEnum.NEW.toString());
            M4d mrpaRegistrySaved;
            try {
                mrpaRegistrySaved = this.m4dService.saveMrpaRegistry(mrpaRegistry);
                this.m4dFileService.deleteM4dFile(fileId);
            } catch (ConstraintViolationException e) {
                return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
            }
            return findMrpaRegistryById(authentication, mrpaRegistrySaved.getId());
        }
        return new ResponseEntity<>(getCustomErrorJsonFromString(SIG_NOT_FOUND), HttpStatus.BAD_REQUEST);
    }

    @Operation(summary = CONTROLLER_MRPA_CHECK_XML_SUMMARY, description = CONTROLLER_MRPA_CHECK_XML_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @PostMapping(value = "/check", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> check(Authentication authentication, @RequestPart("srcFile") MultipartFile sourceFile) throws Exception {
        if (FileUtils.isNotXmlFile(sourceFile)) {
            throw new BadRequestException(NO_XML);
        }
        ReportValidSchemeDto reportValidSchemeDto = m4dService.validXmlM4dScheme(sourceFile, "EMCHD_1");
        if (Objects.equals(reportValidSchemeDto.getResult(), "false")) {
            throw new BadRequestException(XML_NOT_SCHEMA_EMCHD_1);
        }
        M4dXml m4dXml = this.m4dService.convertM4dFileToM4dXml(sourceFile);
        String hashXml = this.m4dService.getHashXmlM4d(sourceFile);
// - поиск МЧД в ПЭП по номеру
        ReportCheckStatusM4dDto reportM4d = m4dService.checkM4dInPep(authentication, m4dXml);
        if (reportM4d != null) { //мчд в пэп найдена
            return new ResponseEntity<>(reportM4d, HttpStatus.OK);
        } else { //мчд в пэп не найдена
// - поиск ранее сформированного запроса в РР МЧД на проверку статуса МЧД (по номеру и хеш)
            ReportCheckStatusM4dDto reportM4dRequestSearch = m4dService.getReportM4dRequestSearchForFindRR(m4dXml, hashXml);
            if (reportM4dRequestSearch != null) { // запрос найден, возвращаем отчёт
                return new ResponseEntity<>(reportM4dRequestSearch, HttpStatus.OK);
            } else { //ранее запрос не формировался, создаем новый запрос
                ReportCheckStatusM4dDto reportCreateM4dRequestSearch = m4dService.createM4dRequestSearchForFindRR(m4dXml, hashXml);
                // отправка запроса на получение статуса мчд в РР МЧД
                String res = this.pvvService.sendRequestStatusM4dToRR(m4dXml.document.powerAttorneys.mrpa.getGuid());
                if (res.isEmpty()) {
                    return new ResponseEntity<>(getCustomErrorJsonFromString(SENDING_RESPONSE_TO_PVV_ERROR), HttpStatus.BAD_REQUEST);
                }
                HttpHeaders headers = new HttpHeaders();
                headers.set("X-Request-ID", res);

                return new ResponseEntity<>(reportCreateM4dRequestSearch, headers, HttpStatus.OK);
            }
        }
    }

    @Operation(summary = CONTROLLER_MRPA_CHECK_BY_GUID_SUMMARY, description = CONTROLLER_MRPA_CHECK_BY_GUID_SUMMARY)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @PostMapping(value = "/check-by-guid/{guid}", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> checkByGuid(Authentication authentication, @PathVariable @Parameter(name = "guid",
            description = PARAMETER_MRPA_GUID_DESCRIPTION) String guid) throws Exception {
        if (guid == null || guid.isEmpty()) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(GUID_IS_NULL), HttpStatus.BAD_REQUEST);
        }
// - поиск МЧД в ПЭП по номеру
        ReportCheckStatusM4dDto reportM4d = m4dService.checkM4dInPepByGuid(authentication, guid);
        if (reportM4d != null) { //мчд в пэп найдена
            return new ResponseEntity<>(reportM4d, HttpStatus.OK);
        } else { //мчд в пэп не найдена
// - поиск ранее сформированного запроса в РР МЧД на проверку статуса МЧД (по номеру)
            ReportCheckStatusM4dDto reportM4dRequestSearch = m4dService.getReportM4dRequestSearchForFindRRByGuid(guid);
            if (reportM4dRequestSearch != null) { // запрос найден, возвращаем отчёт
                return new ResponseEntity<>(reportM4dRequestSearch, HttpStatus.OK);
            } else { //ранее запрос не формировался, создаем новый запрос
                ReportCheckStatusM4dDto reportCreateM4dRequestSearch = m4dService.createM4dRequestSearchForFindRRByGuid(guid);
                // отправка запроса на получение статуса мчд в РР МЧД
                String res = this.pvvService.sendRequestStatusM4dToRR(guid);
                if (res.isEmpty()) {
                    return new ResponseEntity<>(getCustomErrorJsonFromString(SENDING_RESPONSE_TO_PVV_ERROR), HttpStatus.BAD_REQUEST);
                }
                HttpHeaders headers = new HttpHeaders();
                headers.set("X-Request-ID", res);

                return new ResponseEntity<>(reportCreateM4dRequestSearch, headers, HttpStatus.OK);
            }
        }
    }

    @Operation(summary = CONTROLLER_MRPA_CHECK_BY_GUID_FULL_SUMMARY, description = CONTROLLER_MRPA_CHECK_BY_GUID_FULL_SUMMARY)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @PostMapping(value = "/check-by-guid-full", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> checkByGuidFull(Authentication authentication, @RequestBody CheckByGuidRequestDto requestDto) throws Exception {
        if (requestDto == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(CHECK_REQUEST_IS_NULL), HttpStatus.BAD_REQUEST);
        }
        if (requestDto.getGuid() == null || requestDto.getGuid().isEmpty()) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(CHECK_REQUEST_IS_NULL), HttpStatus.BAD_REQUEST);
        }
        if (requestDto.getPrincipalInn() == null || requestDto.getPrincipalInn().isEmpty()) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(CHECK_REQUEST_IS_NULL), HttpStatus.BAD_REQUEST);
        }
        if (requestDto.getRepresentativeInn() == null || requestDto.getRepresentativeInn().isEmpty()) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(CHECK_REQUEST_IS_NULL), HttpStatus.BAD_REQUEST);
        }
// - поиск МЧД в ПЭП
        ReportCheckStatusM4dDto reportM4d = m4dService.checkM4dInPepByGuidAndInns(authentication, requestDto);
        if (reportM4d != null) { //мчд в пэп найдена
            return new ResponseEntity<>(reportM4d, HttpStatus.OK);
        } else { //мчд в пэп не найдена
// - поиск ранее сформированного запроса в РР МЧД на проверку статуса МЧД (по номеру)
            ReportCheckStatusM4dDto reportM4dRequestSearch = m4dService.getReportM4dRequestSearchForFindRRByGuid(requestDto.getGuid());
            if (reportM4dRequestSearch != null) { // запрос найден, возвращаем отчёт
                return new ResponseEntity<>(reportM4dRequestSearch, HttpStatus.OK);
            } else {
                //ранее запрос не формировался, создаем новый запрос
                ReportCheckStatusM4dDto reportCreateM4dRequestSearch = m4dService.createM4dRequestSearchForFindRRByGuidAndInns(requestDto);
                // отправка запроса на получение данных по мчд в РР МЧД
                String res = this.pvvService.sendRequestStatusM4dToRRByGuidAndInns(requestDto);
                if (res.isEmpty()) {
                    return new ResponseEntity<>(getCustomErrorJsonFromString(SENDING_RESPONSE_TO_PVV_ERROR), HttpStatus.BAD_REQUEST);
                }
                HttpHeaders headers = new HttpHeaders();
                headers.set("X-Request-ID", res);
                return new ResponseEntity<>(reportCreateM4dRequestSearch, headers, HttpStatus.OK);
            }
        }
    }

    @Operation(summary = CONTROLLER_MRPA_SEND_REQUEST_IN_PVV_SUMMARY, description = CONTROLLER_MRPA_SEND_REQUEST_IN_PVV_SUMMARY)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @PostMapping(value = "/send-request-status-in-pvv/{guid}", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> sendRequestStatusM4dInPvv(Authentication authentication, @PathVariable @Parameter(name = "guid",
            description = PARAMETER_MRPA_GUID_DESCRIPTION) String guid) {
        if (guid == null || guid.isEmpty()) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(GUID_IS_NULL), HttpStatus.BAD_REQUEST);
        }
            // отправка запроса на получение статуса мчд в РР МЧД
            String res = this.pvvService.sendRequestStatusM4dToRR(guid);
            if (res.isEmpty()) {
                return new ResponseEntity<>(getCustomErrorJsonFromString(SENDING_RESPONSE_TO_PVV_ERROR), HttpStatus.BAD_REQUEST);
            }
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Request-ID", res);

            return new ResponseEntity<>(SEND_REQUEST_IN_PVV, headers, HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_MRPA_SEND_REQUEST_IN_PVV_SUMMARY, description = CONTROLLER_MRPA_SEND_REQUEST_IN_PVV_SUMMARY)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @PostMapping(value = "/send-request-data-in-pvv", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> sendRequestFullM4dInPvv(Authentication authentication, @RequestBody CheckByGuidRequestDto requestDto) {
        if (requestDto == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(CHECK_REQUEST_IS_NULL), HttpStatus.BAD_REQUEST);
        }
        if (requestDto.getGuid() == null || requestDto.getGuid().isEmpty()) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(CHECK_REQUEST_IS_NULL), HttpStatus.BAD_REQUEST);
        }
        if (requestDto.getPrincipalInn() == null || requestDto.getPrincipalInn().isEmpty()) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(CHECK_REQUEST_IS_NULL), HttpStatus.BAD_REQUEST);
        }
        if (requestDto.getRepresentativeInn() == null || requestDto.getRepresentativeInn().isEmpty()) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(CHECK_REQUEST_IS_NULL), HttpStatus.BAD_REQUEST);
        }
        // отправка запроса на получение данных по мчд в РР МЧД
        String res = this.pvvService.sendRequestStatusM4dToRRByGuidAndInns(requestDto);
        if (res.isEmpty()) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(SENDING_RESPONSE_TO_PVV_ERROR), HttpStatus.BAD_REQUEST);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Request-ID", res);
        return new ResponseEntity<>(SEND_REQUEST_DATA_IN_PVV, headers, HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_MRPA_UPDATE_REQUEST_STATUS_IN_RR_SUMMARY, description = CONTROLLER_MRPA_UPDATE_REQUEST_STATUS_IN_RR_SUMMARY)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @PostMapping(value = "/update-request-status-in-rr", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> updateRequestStatusM4dInRR(Authentication authentication) {
        List<M4dRequestSearch> m4dRequestSearchList;
        try {
            m4dRequestSearchList = this.m4dRequestService.findAllM4dRequestSearch();
        } catch (ConstraintViolationException e) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
        Integer count = 0;
        for (M4dRequestSearch m4dRequestSearch : m4dRequestSearchList) {
            if (m4dRequestSearch.getGuid() != null &&  (m4dRequestSearch.getStatus().equals("ONCHECK") || m4dRequestSearch.getStatus().equals("ERROR"))) {
                if (m4dRequestSearch.getPrincipalInn() != null && m4dRequestSearch.getRepresentativeInn() != null){
                    CheckByGuidRequestDto requestDto = new CheckByGuidRequestDto();
                    requestDto.setGuid(m4dRequestSearch.getGuid());
                    requestDto.setPrincipalInn(m4dRequestSearch.getPrincipalInn());
                    requestDto.setRepresentativeInn(m4dRequestSearch.getRepresentativeInn());
                    this.sendRequestFullM4dInPvv(authentication, requestDto);
                } else {
                    this.pvvService.sendRequestStatusM4dToRR(m4dRequestSearch.getGuid());
                }
                count++;
            }
        }
        Map<String, String> body = new HashMap<>();
        body.put("result", String.format(COUNT_SEND_REQUEST_DATA_IN_PVV, count));

        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    // TODO: me paginated (Данияр)
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
            @RequestPart(value="sigFile", required = false) MultipartFile sourceSig) throws ParseException, IOException {
        if (sourceFile.isEmpty()) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(XML_IS_NULL), HttpStatus.BAD_REQUEST);
        }
        if (FileUtils.isNotXmlFile(sourceFile)) {
            throw new BadRequestException(NO_XML);
        }
        Long m4dId = null;
        //upload m4d !!!!!!!!!!!!!!!!!!!!!!!!
        try {
            M4dParseResponseDto m4dParseResponseDto = this.m4dService.parse(sourceFile);
            if (m4dParseResponseDto != null) {
                try {
                    m4dId = this.m4dService.saveAllDataMrpaRegistry(authentication, m4dParseResponseDto.getM4d(), m4dParseResponseDto.getOrganisation(),
                            m4dParseResponseDto.getPrincipal(), m4dParseResponseDto.getPrincipalDetail(), m4dParseResponseDto.getRepresentative(),
                            m4dParseResponseDto.getRepresentativeDetail(), m4dParseResponseDto.getCredentialList(), null, m4dParseResponseDto.getXmlString());
                } catch (ConstraintViolationException e) {
                    throw new BadRequestException(e.getMessage());
                }
            }

        } catch (Exception e) {
            log.error(MRPA_REGISTRY_IMPORT_ERROR + ": {}", e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_REGISTRY_IMPORT_ERROR + ": " + e.getMessage()), HttpStatus.BAD_REQUEST);
        }
        if (m4dId == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_REGISTRY_IMPORT_ERROR), HttpStatus.BAD_REQUEST);
        }
        String res = null;
        M4d m4dUpdate;
        if (sourceSig != null) {
            //upload sig for m4d !!!!!!!!!!!!!!!!!!!!!!
            try {
                m4dUpdate = this.m4dService.uploadSig(authentication, m4dId, sourceSig);
                // отправка архива с xml-файлом мчд и подписью на регистрацию в РР МЧД
                M4dConfirm m4dConfirm = this.m4dConfirmService.findM4dConfirmByM4dId(m4dId);
                res = this.pvvService.sendM4dToRR(authentication, m4dUpdate, m4dConfirm);
                if (res.isEmpty()) {
                    return new ResponseEntity<>(getCustomErrorJsonFromString(SENDING_RESPONSE_TO_PVV_ERROR), HttpStatus.BAD_REQUEST);
                }
                M4d m4d = this.m4dService.findById(m4dId);
                if (m4d == null) {
                    return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_IS_NULL), HttpStatus.BAD_REQUEST);
                }
                m4d.setStatus(StatusM4dTypeEnum.ACTIVE.toString());
                m4d.setStatusInRr(StatusM4dInRrTypeEnum.ONCHECK.toString());
                M4d m4dSaved;
                try {
                    m4dSaved = this.m4dService.updateM4d(m4d);
                } catch (ConstraintViolationException e) {
                    throw new BadRequestException(e.getMessage());
                }
            } catch (Exception e) {
                log.error(MRPA_REGISTRY_UPLOAD_SIG_ERROR + ": {}", e.getMessage());
                e.printStackTrace();
                return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_REGISTRY_UPLOAD_SIG_ERROR + ": " + e.getMessage()), HttpStatus.BAD_REQUEST);
            }
        }
        if (res != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Request-ID", res);

            return new ResponseEntity<>(findMrpaRegistryById(authentication, m4dId).getBody(), headers, HttpStatus.OK);
        }

        return findMrpaRegistryById(authentication, m4dId);
    }

    @Operation(summary = CONTROLLER_MRPA_AS_ARCHIVE_SUMMARY, description = CONTROLLER_MRPA_AS_ARCHIVE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @GetMapping(value = "/{id}/archive", produces = "application/zip")
    public ResponseEntity<?> archive(Authentication authentication, HttpServletResponse response,
                                     @PathVariable @Parameter(name = "id", description = PARAMETER_MRPA_ID_DESCRIPTION, example = "1") Long id) throws IOException, ParseException {
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
            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_IS_NULL), HttpStatus.NOT_FOUND);
        }
        M4dConfirm m4dConfirm;
        try {
            m4dConfirm = this.m4dConfirmService.findM4dConfirmByM4dId(m4d.getId());
        } catch (ConstraintViolationException e) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
        }

        String filenameSource = m4d.getXmlFile().getFilename();
        int lastDotIndex = filenameSource.lastIndexOf('.');
        if (lastDotIndex > 0) {
            filenameSource = filenameSource.substring(0, lastDotIndex);
        }
        if (m4d.getSigFile() == null && m4dConfirm == null) {
            M4dRegistryResponseDto m4DRegistryResponseDto = this.m4dService.convertM4dToM4dResponseDto(authentication, m4d, true, false, true, false);
            String xmlString = m4DRegistryResponseDto.getXmlFile().getContent();
            try {
                response.setHeader("Content-Disposition", "attachment; filename=\"" + filenameSource + ".xml\"");

                return new ResponseEntity<>(
                        xmlString,
                        HttpStatus.OK
                );
            } catch (EntityNotFoundException e) {
                return ResponseEntity.noContent().build();
            }
        } else {
            List<Long> longList = new ArrayList<>();
            longList.add(m4d.getXmlFile().getId());
            if (m4d.getSigFile() != null) {
                longList.add(m4d.getSigFile().getId());
            }
            if (m4dConfirm != null) {
                if (m4dConfirm.getXmlFileRegisteredPep() != null) {
                    longList.add(m4dConfirm.getXmlFileRegisteredPep().getId());
                }
                if (m4dConfirm.getSigFileRegisteredPep() != null) {
                    longList.add(m4dConfirm.getSigFileRegisteredPep().getId());
                }
            }
            ByteArrayResource resp = new ByteArrayResource(Files.readAllBytes((this.m4dFileService.createArchive(authentication, filenameSource, longList)).toPath()));
            try {
                response.setHeader("Content-Disposition", "attachment; filename=\"" + filenameSource + ".zip\"");

                return new ResponseEntity<>(
                        resp,
                        HttpStatus.OK
                );
            } catch (EntityNotFoundException e) {
                return ResponseEntity.noContent().build();
            }
        }

    }

    @Operation(summary = CONTROLLER_MRPA_VALID_SCHEME_SUMMARY, description = CONTROLLER_MRPA_VALID_SCHEME_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @PostMapping(value = "/valid-scheme", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> validScheme(@RequestPart("srcFile") MultipartFile sourceFile,
                                         @RequestParam(name = "reqScheme", required = false) String reqScheme) throws IOException, SAXException {
        return new ResponseEntity<>(m4dService.validXmlM4dScheme(sourceFile, reqScheme), HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_MRPA_UPDATE_STATUS_SUMMARY, description = CONTROLLER_MRPA_UPDATE_STATUS_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @PutMapping(value = "/status", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateStatusM4dRequestSearch(@RequestParam("file") MultipartFile zipFile) throws IOException {
        if (FileUtils.isNotZipFile(zipFile)) {
            throw new BadRequestException(NO_ZIP);
        }
        ReportCheckStatusM4dDto report;
        report = m4dService.updateStatusM4dRequestSearch(zipFile);
        if (report == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_REGISTRY_UPDATE_STATUS_ERROR), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(report, HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_MRPA_UPDATE_STATUS_BY_MESSAGE_ID_SUMMARY, description = CONTROLLER_MRPA_UPDATE_STATUS_BY_MESSAGE_ID_SUMMARY)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @PutMapping(value = "/status/{messageId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateStatusM4dRequestSearchByMessageId
            (@PathVariable @Parameter(name = "messageId", description = PARAMETER_MESSAGE_ID_DESCRIPTION, example = "1") String messageId) throws IOException {
        if (messageId == null || messageId.isEmpty()) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(MESSAGE_ID_IS_NULL), HttpStatus.BAD_REQUEST);
        }
        // получение zip-архива по messageId
        ByteArrayResource res = this.pvvService.receiveResponseM4dToRR(messageId);
        if (res == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(PVV_MESSAGE_ID_NOT_FOUND+messageId);
        }
        File tempZipFile = FileUtils.byteArrayResourceToTempFile(res, messageId, ".zip");
        MultipartFile zipFile = new CustomMultipartFile(tempZipFile);

        if (FileUtils.isNotZipFile(zipFile)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(NO_ZIP);
        }
        ReportCheckStatusM4dDto report;
        report = m4dService.updateStatusM4dRequestSearch(zipFile);
        if (report == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_REGISTRY_UPDATE_STATUS_ERROR), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(report, HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_MRPA_UPLOAD_RESULT_SENDING_TO_RR_DESCRIPTION, description = CONTROLLER_MRPA_UPLOAD_RESULT_SENDING_TO_RR_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @PutMapping(value = "/upload-result-sending-to-rr", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> uploadResultSendM4dToRR(@RequestBody M4dResponseInfo m4dResponseInfo) {
        String error;
        try {
            error = m4dService.uploadResultSendM4dToRR(m4dResponseInfo);
        } catch (ConstraintViolationException e) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
        if (error != null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(error), HttpStatus.BAD_REQUEST);
        }
        Map<String, String> body = new HashMap<>();
        body.put("result", MRPA_REGISTRY_UPLOAD_RESULT_SEND_RR_MRPA);
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_MRPA_UPLOAD_RESULT_SENDING_TO_RR_BY_MESSAGE_ID_DESCRIPTION,
            description = CONTROLLER_MRPA_UPLOAD_RESULT_SENDING_TO_RR_BY_MESSAGE_ID_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @PutMapping(value = "/upload-result-sending-to-rr/{messageId}", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> uploadResultSendM4dToRRByMessageId(
            @PathVariable @Parameter(name = "messageId", description = PARAMETER_MESSAGE_ID_DESCRIPTION, example = "1") String messageId) {
        if (messageId == null || messageId.isEmpty()) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(MESSAGE_ID_IS_NULL), HttpStatus.BAD_REQUEST);
        }
        M4dResponseInfo m4dResponseInfo;
        // получение json-ответа по messageId
        m4dResponseInfo = this.pvvService.receiveJsonResponseM4dToRR(messageId);
        if (m4dResponseInfo == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(PVV_MESSAGE_ID_NOT_FOUND+messageId);
        }
        String error;
        try {
            error = m4dService.uploadResultSendM4dToRR(m4dResponseInfo);
        } catch (ConstraintViolationException e) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
        if ( error != null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(error), HttpStatus.BAD_REQUEST);
        }
        Map<String, String> body = new HashMap<>();
        body.put("result", MRPA_REGISTRY_UPLOAD_RESULT_SEND_RR_MRPA);
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_MRPA_SEND_REGISTER_IN_PVV_SUMMARY, description = CONTROLLER_MRPA_SEND_REGISTER_IN_PVV_SUMMARY)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @PutMapping(value = "send-register-in-rr/{id}", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> sendRegisterInRr(Authentication authentication,
                                       @PathVariable @Parameter(name = "id", description = PARAMETER_MRPA_ID_DESCRIPTION, example = "1") Long id) throws Exception {
        if (id == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(ID_IS_NULL), HttpStatus.BAD_REQUEST);
        }
        M4d m4d = this.m4dService.findById(id);
        if (m4d == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_IS_NULL), HttpStatus.BAD_REQUEST);
        }
        // отправка архива с xml-файлом мчд и подписью на регистрацию в РР МЧД
        M4dConfirm m4dConfirm = this.m4dConfirmService.findM4dConfirmByM4dId(id);
        String res = this.pvvService.sendM4dToRR(authentication, m4d, m4dConfirm);
        if (res.isEmpty()) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(SENDING_RESPONSE_TO_PVV_ERROR), HttpStatus.BAD_REQUEST);
        }
        m4d.setStatusInRr(StatusM4dInRrTypeEnum.ONCHECK.toString());
        M4d m4dSaved;
        try {
            m4dSaved = this.m4dService.updateM4d(m4d);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(e.getMessage());
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Request-ID", res);

        return new ResponseEntity<>(findMrpaRegistryById(authentication, id).getBody(), headers, HttpStatus.OK);
    }

    @GetMapping("/me/{status}/count")
    public M4dCountAndRevokeCountResponseDto getM4dCountAndRevokeCountForUser(Authentication authentication, @PathVariable String status) {
        return m4dService.countM4dAndRevoke(authentication, status);
    }

    private M4d convertMrpaRegistryToEntity(M4dRegistryRequestDto.MrpaRegistry mrpaRegistryDto) {
        M4d m4d = new M4d();
        m4d.setStartDate(DateUtils.convertStringToLocalDateTime(mrpaRegistryDto.getStartDate()));
        m4d.setEndDate(DateUtils.convertStringToLocalDateTime(mrpaRegistryDto.getEndDate()));
        m4d.setGuid(mrpaRegistryDto.getGuid());
        return m4d;
    }

    private OrganizationDto convertOrganisationToEntity(M4dRegistryRequestDto.Organisation organisationDto) {
        return modelMapper.map(organisationDto, OrganizationDto.class);
    }

    private Principal convertPrincipalToEntity(M4dRegistryRequestDto.Principal principalDto) {
        return modelMapper.map(principalDto, Principal.class);
    }

    private PrincipalDetail convertPrincipalDetailToEntity(M4dRegistryRequestDto.PrincipalDetail principalDetailDto) {
        return modelMapper.map(principalDetailDto, PrincipalDetail.class);
    }

    private Representative convertRepresentativeToEntity(M4dRegistryRequestDto.Representative representativeDto) {
        Representative representative = new Representative();
        representative.setId(representativeDto.getId());
        representative.setInn(representativeDto.getInn());
        representative.setSnils(representativeDto.getSnils());
        representative.setBirthDate(DateUtils.convertStringToLocalDateTime(representativeDto.getBirthDate()));
        return representative;
    }

    private RepresentativeDetail convertRepresentativeDetailToEntity(M4dRegistryRequestDto.RepresentativeDetail representativeDetailDto) {
        RepresentativeDetail representativeDetail = new RepresentativeDetail();
        representativeDetail.setId(representativeDetailDto.getId());
        representativeDetail.setSurname(representativeDetailDto.getSurname());
        representativeDetail.setName(representativeDetailDto.getName());
        representativeDetail.setPatronymic(representativeDetailDto.getPatronymic());
        representativeDetail.setSeriesNumber(representativeDetailDto.getSeriesNumber());
        representativeDetail.setDateIssue(DateUtils.convertStringToLocalDateTime(representativeDetailDto.getDateIssue()));
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
}
