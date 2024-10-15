package su.opencode.digsigcik.m4d.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import su.opencode.digsigcik.m4d.annotation.M4dExceptionHandler;
import su.opencode.digsigcik.m4d.dto.*;
import su.opencode.digsigcik.m4d.dto.response.*;
import su.opencode.digsigcik.m4d.enums.FileLogicalTypeEnum;
import su.opencode.digsigcik.m4d.enums.FileMimeTypeEnum;
import su.opencode.digsigcik.m4d.exception.BadRequestException;
import su.opencode.digsigcik.m4d.model.*;
import su.opencode.digsigcik.m4d.service.M4dFileService;
import su.opencode.digsigcik.m4d.service.M4dRecallService;
import su.opencode.digsigcik.m4d.service.M4dService;
import su.opencode.digsigcik.m4d.util.DateUtils;
import su.opencode.digsigcik.m4d.util.XMLUtil;
import su.opencode.digsigcik.m4d.util.XmlValidator;

import javax.validation.ConstraintViolationException;
import javax.xml.bind.JAXBException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static su.opencode.digsigcik.m4d.util.Constants.*;
import static su.opencode.digsigcik.m4d.util.JwtTokenUtils.getTokenValue;
import static su.opencode.digsigcik.m4d.util.XMLUtil.convertStringToDocument;

@RestController
@M4dExceptionHandler
@RequestMapping("/api/v1/m4d")
@Tag(name = "M4d Controller")
public class M4dController {
    @Value("${m4d.handbook.uri}")
    private String HANDBOOK_URI;
    private final ModelMapper modelMapper;
    private final M4dService m4dService;
    private final M4dFileService m4dFileService;
    private final RestTemplate restTemplate;

    public M4dController(ModelMapper modelMapper, M4dService m4dService, M4dFileService m4dFileService, RestTemplate restTemplate) {
        this.modelMapper = modelMapper;
        this.m4dService = m4dService;
        this.m4dFileService = m4dFileService;
        this.restTemplate = restTemplate;
    }

    @Operation(summary = CONTROLLER_MRPA_FIND_ALL_SUMMARY,
            description = CONTROLLER_MRPA_FIND_ALL_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @GetMapping(produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> findAllMrpaRegistry(){
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
        M4d m4d = convertMrpaRegistryToEntity(m4dSaveFullDto.mrpaRegistry);
        m4d.setGuid();
        m4d.setCreateDate(LocalDateTime.now());
        Organisation organisation = convertOrganisationToEntity(m4dSaveFullDto.organisation);
        Principal principal = convertPrincipalToEntity(m4dSaveFullDto.principal);
        Representative representative = convertRepresentativeToEntity(m4dSaveFullDto.representative);
        RepresentativePassport representativePassport = convertRepresentativePassportToEntity(m4dSaveFullDto.representative.representativePassport);
        ResponseEntity<?> m4dSaved;
        try {
            m4dSaved = this.m4dService.saveAllDataMrpaRegistry(m4d, organisation, principal, representative, representativePassport, null, List.of(m4dSaveFullDto.getCredentials()));
        } catch (ConstraintViolationException e) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
        Long m4dSavedId;
        try {
            m4dSavedId = Long.valueOf(Objects.requireNonNull(m4dSaved.getBody()).toString());
        } catch (NumberFormatException e) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
        return findMrpaRegistryById(m4dSavedId);
    }

    @Operation(summary = CONTROLLER_MRPA_FIND_BY_ID_SUMMARY,
            description = CONTROLLER_MRPA_FIND_BY_ID_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @GetMapping(value = "/{id}", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> findMrpaRegistryById(@PathVariable @Parameter(name = "id",
            description = PARAMETER_MRPA_ID_DESCRIPTION, example = "1") Long id) throws ParseException, JAXBException {
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
        M4dResponseDto m4dResponseDto = this.m4dService.convertM4dToM4dResponseDto(m4d, true, false);
        M4dFullXML mrpaRegistryFullXML = this.m4dService.ConvertMrpaRegistryFullDtoToFullXml(m4dResponseDto);
        String xmlString = XMLUtil.marshall(mrpaRegistryFullXML, M4dFullXML.class);
        m4dResponseDto.setMrpaXml(xmlString);
        return new ResponseEntity<>(m4dResponseDto, HttpStatus.OK);
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
        if (m4dSaveFullDto.mrpaRegistry.getInternalNumber() != null) {
            m4d.setInternalNumber(m4dSaveFullDto.mrpaRegistry.getInternalNumber());
        }

        Organisation organisation = convertOrganisationToEntity(m4dSaveFullDto.organisation);
        Principal principal = convertPrincipalToEntity(m4dSaveFullDto.principal);
        Representative representative = convertRepresentativeToEntity(m4dSaveFullDto.representative);
        RepresentativePassport representativePassport = convertRepresentativePassportToEntity(m4dSaveFullDto.representative.representativePassport);
        ResponseEntity<?> m4dUpdated;
        try {
            m4dUpdated = m4dService.updateAllDataMrpaRegistry(m4d, organisation, principal, representative, representativePassport, null, List.of(m4dSaveFullDto.getCredentials()));
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(MRPA_REGISTRY_IS_NULL);
        }
        return findMrpaRegistryById((Long) m4dUpdated.getBody());
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
        if (m4d == null){
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
                                       @PathVariable @Parameter(name = "id",
                                               description = PARAMETER_MRPA_ID_DESCRIPTION, example = "1") Long id, @RequestPart("container") MultipartFile container) throws IOException, ParseException, JAXBException {
        String bearerToken = getTokenValue(authentication);
        if (id == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(ID_IS_NULL), HttpStatus.BAD_REQUEST);
        }
        String extension = FilenameUtils.getExtension(container.getOriginalFilename());
        if(!Objects.equals(extension, "dscp")) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_REGISTRY_WRONG_FORMAT), HttpStatus.BAD_REQUEST);
        }
        LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        List<String> tempFileNames = new ArrayList<>();
        String tempFileName;
        FileOutputStream fo;
        ReportDto reportDto;
        M4d m4d;
        ContainerPartResponse res = null;
        try {
            tempFileName = "/tmp/" + container.getOriginalFilename();
            tempFileNames.add(tempFileName);
            fo = new FileOutputStream(tempFileName);
            fo.write(container.getBytes());
            fo.close();
            map.add("files", new FileSystemResource(tempFileName));

            HttpHeaders headers = createHttpHeaders(bearerToken);

            HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);
            reportDto = restTemplate.postForObject(HANDBOOK_URI + "/get-container-part-json", requestEntity, ReportDto.class);

            assert reportDto != null;
            reportDto.getResults().stream()
                    .flatMap(result -> result.getVerificationResults().stream())
                    .findFirst()
                    .ifPresent(verificationResult -> {
                        boolean isResultTrue = verificationResult.isResult();
                        System.out.println("Result value: " + isResultTrue);
                        if (!isResultTrue) {
                            throw new BadRequestException(MRPA_REGISTRY_NOT_VERIFY);
                        }
                    });
            HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity1 = new HttpEntity<>(map, headers);
            res = restTemplate.postForObject(HANDBOOK_URI + "/get-container-part", requestEntity1, ContainerPartResponse.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String fileName : tempFileNames) {
            File f = new File(fileName);
            f.delete();
        }

        assert res != null;
        var resource = new ByteArrayResource(res.getData());
        try {
            m4d = this.m4dService.findById(id);
        } catch (ConstraintViolationException e) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
        if (m4d == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_REGISTRY_IS_NULL), HttpStatus.NOT_FOUND);
        }
        //----------------- XML M4D ----------------------
        M4dResponseDto m4dResponseDto = this.m4dService.convertM4dToM4dResponseDto(m4d, false, false);
        M4dFullXML mrpaRegistryFullXML = this.m4dService.ConvertMrpaRegistryFullDtoToFullXml(m4dResponseDto);
        String xmlString = XMLUtil.marshall(mrpaRegistryFullXML, M4dFullXML.class);
        //-------------------------------------------------
        if(!Arrays.equals(resource.getByteArray(), xmlString.getBytes())){
            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_REGISTRY_NOT_VERIFY), HttpStatus.BAD_REQUEST);
        }
        M4dFile m4dFile = new M4dFile();
        m4dFile.setContent(Arrays.toString(container.getBytes()));
        m4dFile.setFileMimeType(FileMimeTypeEnum.fromValue(container.getContentType()));
        m4dFile.setFileLogicalType(FileLogicalTypeEnum.CONTAINER);
        m4dFile.setFilename(container.getOriginalFilename());

        m4d.setM4dFile(m4dFile);
        M4d m4dSaved;
        try {
            m4dSaved = this.m4dService.saveMrpaRegistry(m4d);
        } catch (ConstraintViolationException e) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
        return findMrpaRegistryById(m4dSaved.getId());
    }

    private static @NotNull HttpHeaders createHttpHeaders(String bearerToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(bearerToken);
        return headers;
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
        if(mrpaRegistry.getM4dFile() != null) {
            Long fileId = mrpaRegistry.getM4dFile().getId();
            mrpaRegistry.setM4dFile(null);
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

    @Operation(summary = CONTROLLER_MRPA_UPLOAD_XML_SUMMARY, description = CONTROLLER_MRPA_UPLOAD_XML_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> upload(@RequestPart("srcFile") MultipartFile sourceFile) throws IOException, ParseException, SAXException, JAXBException {
        String extension = FilenameUtils.getExtension(sourceFile.getOriginalFilename());
        if(!Objects.equals(extension, "xml")) {
            return new ResponseEntity<>(MRPA_REGISTRY_WRONG_FORMAT_XML, HttpStatus.BAD_REQUEST);
        }

        List<SAXParseException> res = new XmlValidator("/app/xml/validation/ON_EMCHD_1_928_00_01_01_01.xsd", sourceFile.getInputStream()).listParsingExceptions();
        System.out.println(res.size());
        String content;
        if(res.isEmpty()) {
            try (InputStream is = sourceFile.getInputStream()) {
                byte[] bom = new byte[3];
                int n = is.read(bom, 0, bom.length);
                Reader reader;
                if (n == 3 && (bom[0] & 0xFF) == 0xEF && (bom[1] & 0xFF) == 0xBB && (bom[2] & 0xFF) == 0xBF) {
                    reader = new InputStreamReader(is, StandardCharsets.UTF_8);
                } else {
                    reader = new InputStreamReader(sourceFile.getInputStream(), StandardCharsets.UTF_8);
                }
                StringBuilder cont = new StringBuilder();
                char[] buffer = new char[1024];
                int numRead;
                while ((numRead = reader.read(buffer)) != -1) {
                    cont.append(buffer, 0, numRead);
                }
                content = cont.toString();
            }

            M4dXml m4dXml;
            try {
                Document document = convertStringToDocument(content);
                assert document != null;
                m4dXml = XMLUtil.unmarshal(document, M4dXml.class);
            } catch (Exception e) {
                return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_REGISTRY_WRONG_FORMAT_XML), HttpStatus.BAD_REQUEST);
            }

            if (m4dXml == null) {
                return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_IS_NULL), HttpStatus.BAD_REQUEST);
            }

            int guid;
            guid = this.m4dService.findMrpaRegistryByGuid(m4dXml.document.powerAttorneys.getMrpa().getGuid());
            if (guid > 0) {
                return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_REGISTRY_IS_EXISTS), HttpStatus.BAD_REQUEST);
            }

            M4d m4d = new M4d();
            m4d.setGuid(m4dXml.document.powerAttorneys.mrpa.getGuid());
            m4d.setInternalNumber(m4dXml.document.powerAttorneys.mrpa.getInternalNumber());
            m4d.setStartDate(this.convertStringToLocalDateTime(m4dXml.document.powerAttorneys.mrpa.getStartDate()));
            m4d.setEndDate(this.convertStringToLocalDateTime(m4dXml.document.powerAttorneys.mrpa.getEndDate()));

            Organisation organisation = new Organisation();
            organisation.setInn(m4dXml.document.powerAttorneys.principals.principal.legalEntity.organisation.getInn());
            organisation.setKpp(m4dXml.document.powerAttorneys.principals.principal.legalEntity.organisation.getKpp());
            organisation.setOgrn(m4dXml.document.powerAttorneys.principals.principal.legalEntity.organisation.getOgrn());
            organisation.setName(m4dXml.document.powerAttorneys.principals.principal.legalEntity.organisation.getName());
            organisation.setRegionCode(m4dXml.document.powerAttorneys.principals.principal.legalEntity.organisation.addressRegion.getRegionCode());
            organisation.setLegalAddress(m4dXml.document.powerAttorneys.principals.principal.legalEntity.organisation.addressRegion.getLegalAddress());
            if (organisation.getInn() == null || organisation.getInn().isEmpty()) {
                return new ResponseEntity<>(getCustomErrorJsonFromString(ORGANISATION_IS_NULL), HttpStatus.BAD_REQUEST);
            }
            Principal principal = new Principal();
            principal.setInn(m4dXml.document.powerAttorneys.principals.principal.legalEntity.principal.principal.getInn());
            principal.setSnils(m4dXml.document.powerAttorneys.principals.principal.legalEntity.principal.principal.getSnils());
            principal.setJobTitle(m4dXml.document.powerAttorneys.principals.principal.legalEntity.principal.principal.getJobTitle());
            principal.setName(m4dXml.document.powerAttorneys.principals.principal.legalEntity.principal.principal.fioPrincipal.fio.getName());
            principal.setSurname(m4dXml.document.powerAttorneys.principals.principal.legalEntity.principal.principal.fioPrincipal.fio.getSurname());
            principal.setPatronymic(m4dXml.document.powerAttorneys.principals.principal.legalEntity.principal.principal.fioPrincipal.fio.getPatronymic());
            if (principal.getInn() == null || principal.getInn().isEmpty()) {
                return new ResponseEntity<>(getCustomErrorJsonFromString(PRINCIPAL_IS_NULL), HttpStatus.BAD_REQUEST);
            }
            Representative representative = new Representative();
            representative.setInn(m4dXml.document.powerAttorneys.infoRepresentative.representative.detailRepresentative.getInn());
            representative.setSnils(m4dXml.document.powerAttorneys.infoRepresentative.representative.detailRepresentative.getSnils());
            representative.setBirthDate(Timestamp.valueOf(DateUtils.parse(m4dXml.document.powerAttorneys.infoRepresentative.representative.detailRepresentative.infoIndividual.getBirthDate()).toInstant().toString()).toLocalDateTime());
            representative.setName(m4dXml.document.powerAttorneys.infoRepresentative.representative.detailRepresentative.infoIndividual.fioRepresentative.getName());
            representative.setSurname(m4dXml.document.powerAttorneys.infoRepresentative.representative.detailRepresentative.infoIndividual.fioRepresentative.getSurname());
            representative.setPatronymic(m4dXml.document.powerAttorneys.infoRepresentative.representative.detailRepresentative.infoIndividual.fioRepresentative.getPatronymic());
            if (representative.getInn() == null || representative.getInn().isEmpty()) {
                return new ResponseEntity<>(getCustomErrorJsonFromString(REPRESENTATIVE_IS_NULL), HttpStatus.BAD_REQUEST);
            }
            RepresentativePassport represPassport = new RepresentativePassport();
            represPassport.setSeriesNumber(m4dXml.document.powerAttorneys.infoRepresentative.representative.detailRepresentative.infoIndividual.representativePassport.getSeriesNumber());
            represPassport.setDateIssue(Timestamp.valueOf(DateUtils.parse(m4dXml.document.powerAttorneys.infoRepresentative.representative.detailRepresentative.infoIndividual.representativePassport.getDateIssue()).toInstant().toString()).toLocalDateTime());
            represPassport.setWhoIssue(m4dXml.document.powerAttorneys.infoRepresentative.representative.detailRepresentative.infoIndividual.representativePassport.getWhoIssue());
            represPassport.setDepartmentCode(m4dXml.document.powerAttorneys.infoRepresentative.representative.detailRepresentative.infoIndividual.representativePassport.getDepartmentCode());

            List<CredentialDto> credentialList = new ArrayList<>();
            if (m4dXml.document.powerAttorneys.credential.credentials != null) {
                for (M4dXml.Document.PowerAttorneys.Credentials.Credential c : m4dXml.document.powerAttorneys.credential.credentials) {
                    CredentialDto newCredential = new CredentialDto();
                    newCredential.setCode(c.getCode());
                    newCredential.setName(c.getName());
                    credentialList.add(newCredential);
                }
            }

            ResponseEntity<?> m4dSaved;
            try {
                m4dSaved = this.m4dService.saveAllDataMrpaRegistry(m4d, organisation, principal, representative, represPassport, credentialList, null);
            } catch (ConstraintViolationException e) {
                return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
            }

            return findMrpaRegistryById((Long) m4dSaved.getBody());
        }
        Map<String, String> body = new HashMap<>();
        List<String> errs = new ArrayList<>();
        res.forEach(e -> errs.add(String.format("Line number: %s, Column number: %s. %s", e.getLineNumber(), e.getColumnNumber(), e.getMessage())));
        body.put("details", String.join(System.lineSeparator(),errs));

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
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

    private M4d convertMrpaRegistryToEntity(M4dRequestDto.MrpaRegistry mrpaRegistryDto){
        M4d m4d = new M4d();
        if(mrpaRegistryDto.getInternalNumber() != null) {
            m4d.setInternalNumber(mrpaRegistryDto.getInternalNumber());
        }
        m4d.setStartDate(this.convertStringToLocalDateTime(mrpaRegistryDto.getStartDate()));
        m4d.setEndDate(this.convertStringToLocalDateTime(mrpaRegistryDto.getEndDate()));
        return m4d;
    }

    private Organisation convertOrganisationToEntity(M4dRequestDto.Organisation organisationDto) {
        Organisation organisation = modelMapper.map(organisationDto, Organisation.class);
        return organisation;
    }

    private Principal convertPrincipalToEntity(M4dRequestDto.Principal principalDto) {
        Principal principal = modelMapper.map(principalDto, Principal.class);
        return principal;
    }

    private Representative convertRepresentativeToEntity(M4dRequestDto.Representative representativeDto) {
        Representative representative = new Representative();
        representative.setId(representativeDto.getId());
        representative.setInn(representativeDto.getInn());
        representative.setSnils(representativeDto.getSnils());
        representative.setBirthDate(this.convertStringToLocalDateTime(representativeDto.getBirthDate()));
        representative.setSurname(representativeDto.getSurname());
        representative.setName(representativeDto.getName());
        representative.setPatronymic(representativeDto.getPatronymic());
        return representative;
    }

    private RepresentativePassport convertRepresentativePassportToEntity(M4dRequestDto.RepresentativePassport representativePassportDto) {
        RepresentativePassport representativePassport = new RepresentativePassport();
        representativePassport.setId(representativePassportDto.getId());
        representativePassport.setSeriesNumber(representativePassportDto.getSeriesNumber());
        representativePassport.setDateIssue(this.convertStringToLocalDateTime(representativePassportDto.getDateIssue()));
        representativePassport.setWhoIssue(representativePassportDto.getWhoIssue());
        representativePassport.setDepartmentCode(representativePassportDto.getDepartmentCode());
        representativePassport.setRepresentativeId(representativePassportDto.getRepresentativeId());
        return representativePassport;
    }

    //------------------ M4D RECALL ---------------------------------------

//    @Operation(summary = CONTROLLER_MRPA_RECALL_FIND_ALL_SUMMARY,
//            description = CONTROLLER_MRPA_RECALL_FIND_ALL_DESCRIPTION)
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
//            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
//            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
//    })
//    @GetMapping(value = "revoke", produces = "application/json; charset=UTF-8")
//    public ResponseEntity<?> findAllMrpaRecall() throws ParseException {
//        List<M4dResponseDto> mrpaRegistryFindAllRecall;
//        try {
//            mrpaRegistryFindAllRecall = this.m4dRecallService.findAllMrpaRegistriesWithRecall();
//        } catch (ConstraintViolationException e) {
//            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
//        }
//        for (M4dResponseDto m : mrpaRegistryFindAllRecall) {
//            m.credentials = this.m4dService.getCredentialByMrpaRegistryId(m.getId());
//            m.setStatus(this.m4dService.getStatusM4d(m.getId()));
//        }
//        return new ResponseEntity<>(mrpaRegistryFindAllRecall, HttpStatus.OK);
//    }
//
//    @Operation(summary = CONTROLLER_MRPA_RECALL_FIND_BY_ID_SUMMARY,
//            description = CONTROLLER_MRPA_RECALL_FIND_BY_ID_DESCRIPTION)
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
//            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
//            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
//    })
//    @GetMapping(value = "revoke/{id}", produces = "application/json; charset=UTF-8")
//    public ResponseEntity<?> findMrpaRecallById(@PathVariable @Parameter(name = "id",
//            description = PARAMETER_MRPA_RECALL_ID_DESCRIPTION, example = "1") Long id) throws JAXBException, ParseException {
//        if (id == null) {
//            return new ResponseEntity<>(getCustomErrorJsonFromString(ID_IS_NULL), HttpStatus.BAD_REQUEST);
//        }
//        M4d m4d;
//        try {
//            m4d = this.m4dRecallService.findMrpaRecallById(id);
//        } catch (ConstraintViolationException e) {
//            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
//        }
//        if (m4d == null) {
//            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_REGISTRY_IS_NULL), HttpStatus.NOT_FOUND);
//        }
//        M4dResponseDto m4dResponseDto = this.m4dService.convertM4dToM4dResponseDto(m4d, false, true);
//        M4dRevokeXml mrpaRecallXml = this.m4dRecallService.ConvertMrpaRecallToXml(m4dResponseDto);
//        String xmlRecallString = XMLUtil.marshall(mrpaRecallXml, M4dRevokeXml.class);
//        m4dResponseDto.setRecallXml(xmlRecallString);
//        return new ResponseEntity<>(m4dResponseDto, HttpStatus.OK);
//    }
//
//    @Operation(summary = CONTROLLER_MRPA_RECALL_SAVE_SUMMARY, description = CONTROLLER_MRPA_RECALL_SAVE_DESCRIPTION)
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
//            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
//            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
//    })
//    @PostMapping("revoke/save")
//    public ResponseEntity<?> saveMrpaRecall(@RequestBody M4dRevokeRequestDto mrpaRecallRequestDto) throws JAXBException, ParseException {
//        M4dRecall mrpaRecall = convertMrpaRecallRequestDtoToEntity(mrpaRecallRequestDto);
//        if (mrpaRecall == null) {
//            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_RECALL_ERROR_CONVERT), HttpStatus.BAD_REQUEST);
//        }
//        if (mrpaRecall.getM4dFile().getId() != null) {
//            mrpaRecall.setM4dFile(null);
//        }
//        Long mrpaRegistryId = mrpaRecallRequestDto.getMrpaRegistryId();
//        if (mrpaRegistryId == null) {
//            return new ResponseEntity<>(getCustomErrorJsonFromString(ID_IS_NULL), HttpStatus.BAD_REQUEST);
//        }
//        if (mrpaRecall.getAuthor() == null) {
//            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_REGISTRY_NOT_FULL), HttpStatus.BAD_REQUEST);
//        }
//        M4d mrpaRegistry = this.m4dService.findById(mrpaRegistryId);
//        mrpaRegistry.setMrpaRecall(mrpaRecall);
//        M4d mrpaRegistryUpdated;
//        try {
//            mrpaRegistryUpdated = this.m4dService.updateMrpaRegistry(mrpaRegistry);
//        } catch (ConstraintViolationException e) {
//            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
//        }
//        return findMrpaRecallById(mrpaRegistryUpdated.getMrpaRecall().getId());
//    }
//
//    @Operation(summary = CONTROLLER_MRPA_RECALL_UPDATE_SUMMARY, description = CONTROLLER_MRPA_RECALL_UPDATE_DESCRIPTION)
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
//            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
//            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
//    })
//    @PutMapping("revoke/update")
//    public ResponseEntity<?> updateMrpaRecall(@RequestBody M4dRevokeRequestDto mrpaRecallRequestDto) throws JAXBException, ParseException {
//        M4dRecall mrpaRecall = convertMrpaRecallRequestDtoToEntity(mrpaRecallRequestDto);
//        if (mrpaRecall == null) {
//            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_RECALL_ERROR_CONVERT), HttpStatus.BAD_REQUEST);
//        }
//        Long mrpaRecallId = mrpaRecall.getId();
//        if(mrpaRecallId == null) {
//            return new ResponseEntity<>(getCustomErrorJsonFromString("Can`t update M4d recall, because id null!"), HttpStatus.BAD_REQUEST);
//        }
//        M4dRecall existsMrpaRecall = this.m4dRecallService.findById(mrpaRecall.getId());
//        if (existsMrpaRecall == null) {
//            return new ResponseEntity<>(getCustomErrorJsonFromString(String.format("M4d Recall %s", NOT_FOUND)), HttpStatus.NOT_FOUND);
//        }
//        if(mrpaRecall.getArgument() != null && !mrpaRecall.getArgument().equals(existsMrpaRecall.getArgument())) {
//            existsMrpaRecall.setArgument(mrpaRecall.getArgument());
//        }
//        if(mrpaRecall.getAuthor() != null && mrpaRecall.getAuthor() != existsMrpaRecall.getAuthor()) {
//            existsMrpaRecall.setAuthor(mrpaRecall.getAuthor());
//        }
//        if(mrpaRecall.getM4dFile().getId() != null && !mrpaRecall.getM4dFile().getId().equals(existsMrpaRecall.getM4dFile().getId())) {
//            existsMrpaRecall.setM4dFile(mrpaRecall.getM4dFile());
//        }
//        M4dRecall response;
//        try {
//            response = this.m4dRecallService.updateMrpaRecall(existsMrpaRecall);
//        } catch (ConstraintViolationException e) {
//            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
//        }
//        return findMrpaRecallById(response.getId());
//    }
//
//    @Operation(summary = CONTROLLER_MRPA_RECALL_DELETE_SUMMARY, description = CONTROLLER_MRPA_RECALL_DELETE_DESCRIPTION)
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
//            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
//            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
//    })
//    @DeleteMapping("revoke/delete/{id}")
//    public ResponseEntity<?> deleteMrpaRecall(@PathVariable @Parameter(name = "id",
//            description = PARAMETER_MRPA_RECALL_ID_DESCRIPTION, example = "1") Long id) {
//        M4dRecall mrpaRecall = this.m4dRecallService.findById(id);
//        if (mrpaRecall == null) {
//            return new ResponseEntity<>(getCustomErrorJsonFromString(String.format("M4d Recall %s", NOT_FOUND)), HttpStatus.NOT_FOUND);
//        }
//        try {
//            this.m4dRecallService.deleteMrpaRecallById(id);
//        } catch (ConstraintViolationException e) {
//            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
//        }
//        Map<String, String> body = new HashMap<>();
//        body.put("result", "true");
//
//        return new ResponseEntity<>(body, HttpStatus.OK);
//    }
//
//    @Operation(summary = CONTROLLER_MRPA_RECALL_FIND_ALL_FOR_USER_SUMMARY,
//            description = CONTROLLER_MRPA_RECALL_FIND_ALL_FOR_USER_DESCRIPTION)
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "204", description = API_RESPONSE_CODE_204_MESSAGE),
//            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
//            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
//    })
//    @GetMapping(value = "revoke/me", produces = "application/json; charset=UTF-8")
//    public ResponseEntity<?> findAllM4dRevokeForUser(Authentication authentication) {
//        return new ResponseEntity<>(m4dRecallService.findAllM4dRevokeUsers(authentication), HttpStatus.OK);
//    }
//
//    @Operation(summary = CONTROLLER_MRPA_RECALL_UPLOAD_SUMMARY, description = CONTROLLER_MRPA_RECALL_UPLOAD_DESCRIPTION)
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
//            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
//            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
//    })
//    @PutMapping(value = "revoke/{id}/upload-dscp", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = "application/json; charset=UTF-8")
//    public ResponseEntity<?> uploadSigRecall(Authentication authentication, @PathVariable @Parameter(name = "id",
//            description = PARAMETER_MRPA_RECALL_ID_DESCRIPTION, example = "1") Long id, @RequestPart("container") MultipartFile container) throws IOException, JAXBException, ParseException {
//        String bearerToken = getTokenValue(authentication);
//        String extension = FilenameUtils.getExtension(container.getOriginalFilename());
//        if(!Objects.equals(extension, "dscp")) {
//            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_REGISTRY_WRONG_FORMAT), HttpStatus.BAD_REQUEST);
//        }
//        if (id==null){
//            return new ResponseEntity<>(getCustomErrorJsonFromString(ID_IS_NULL), HttpStatus.BAD_REQUEST);
//        }
//        M4dRecall mrpaRecall = this.m4dRecallService.findById(id);
//        if (mrpaRecall==null) {
//            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_RECALL_IS_NULL), HttpStatus.BAD_REQUEST);
//        }
//        LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
//        List<String> tempFileNames = new ArrayList<>();
//        String tempFileName;
//        FileOutputStream fo;
//        ReportDto reportDto;
//        ContainerPartResponse res = null;
//
//        try {
//            tempFileName = "/tmp/" + container.getOriginalFilename();
//            tempFileNames.add(tempFileName);
//            fo = new FileOutputStream(tempFileName);
//            fo.write(container.getBytes());
//            fo.close();
//            map.add("files", new FileSystemResource(tempFileName));
//
//            HttpHeaders headers = createHttpHeaders(bearerToken);
//
//            HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);
//            reportDto = restTemplate.postForObject(HANDBOOK_URI + "/get-container-part-json", requestEntity, ReportDto.class);
//
//            assert reportDto != null;
//            reportDto.getResults().stream()
//                    .flatMap(result -> result.getVerificationResults().stream())
//                    .findFirst()
//                    .ifPresent(verificationResult -> {
//                        boolean isResultTrue = verificationResult.isResult();
//                        System.out.println("Result value: " + isResultTrue);
//                        if (!isResultTrue) {
//                            throw new BadRequestException(MRPA_REGISTRY_NOT_VERIFY);
//                        }
//                    });
//
//            HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity1 = new HttpEntity<>(map, headers);
//
//            res = restTemplate.postForObject(HANDBOOK_URI + "/get-container-part", requestEntity1, ContainerPartResponse.class);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        for (String fileName : tempFileNames) {
//            File f = new File(fileName);
//            f.delete();
//        }
//        assert res != null;
//        var resource = new ByteArrayResource(res.getData());
//        M4d mrpaRegistryFind = this.m4dRecallService.findMrpaRecallById(id);
//        if (mrpaRegistryFind == null) {
//            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_REGISTRY_IS_NULL), HttpStatus.BAD_REQUEST);
//        }
//        M4dResponseDto m4dResponseDto = this.m4dService.convertM4dToM4dResponseDto(mrpaRegistryFind, false, false);
//        //----------------- XML RECALL ----------------------
//        M4dRevokeXml mrpaRecallXml = this.m4dRecallService.ConvertMrpaRecallToXml(m4dResponseDto);
//        String xmlRecallString = XMLUtil.marshall(mrpaRecallXml, M4dRevokeXml.class);
//        //-------------------------------------------------
//        if (!Arrays.equals(resource.getByteArray(), xmlRecallString.getBytes())) {
//            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_REGISTRY_NOT_VERIFY), HttpStatus.BAD_REQUEST);
//        }
//        M4dFile m4dFile = new M4dFile();
//        m4dFile.setContent(Arrays.toString(container.getBytes()));
//        m4dFile.setFileMimeType(FileMimeTypeEnum.fromValue(container.getContentType()));
//        m4dFile.setFileLogicalType(FileLogicalTypeEnum.CONTAINER);
//        m4dFile.setFilename(container.getOriginalFilename());
//
//        mrpaRecall.setM4dFile(m4dFile);
//
//        M4d mrpaRegistry = this.m4dService.findById(mrpaRegistryFind.getId());
//        mrpaRegistry.setMrpaRecall(mrpaRecall);
//        M4d mrpaRegistrySaved;
//        try {
//            mrpaRegistrySaved = this.m4dService.saveMrpaRegistry(mrpaRegistry);
//        } catch (ConstraintViolationException e) {
//            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
//        }
//        return findMrpaRecallById(mrpaRegistrySaved.getMrpaRecall().getId());
//    }
//
//    private M4dRecall convertMrpaRecallRequestDtoToEntity(M4dRevokeRequestDto mrpaRecallRequestDto) {
//        if (mrpaRecallRequestDto.getSignature() == null || Objects.equals(mrpaRecallRequestDto.getSignature().trim(), "")) {
//            mrpaRecallRequestDto.setSignature(null);
//        }
//        M4dRecall mrpaRecall = modelMapper.map(mrpaRecallRequestDto, M4dRecall.class);
//        return mrpaRecall;
//    }

    public Map<String, String> getCustomErrorJsonFromString(String text) {
        Map<String, String> body = new HashMap<>();
        body.put("error", text);
        return body;
    }

    private LocalDateTime convertStringToLocalDateTime(String date) {
        DateTimeFormatter formatter;
        if(date.length()>20){
            formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        } else {
            formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX");
        }
        return LocalDateTime.from(formatter.parse(date));
    }

}
