package su.opencode.digsigcik.synapi.controller;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.apache.commons.io.FilenameUtils;
import org.joda.time.Instant;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import su.opencode.digsigcik.synapi.annotation.MchdExceptionHandler;
import su.opencode.digsigcik.synapi.api.model.ContainerPart;
import su.opencode.digsigcik.synapi.api.model.ContainerSelector;
import su.opencode.digsigcik.synapi.dscp.ContainerSelectorCode;
import su.opencode.digsigcik.synapi.dto.*;
import su.opencode.digsigcik.synapi.dto.m4d.MrpaRegistryFullXML;
import su.opencode.digsigcik.synapi.enums.StatusTypeEnum;
import su.opencode.digsigcik.synapi.exception.BadRequestException;
import su.opencode.digsigcik.synapi.model.*;
import su.opencode.digsigcik.synapi.model.m4d.M4dXml;
import su.opencode.digsigcik.synapi.service.ContainerService;
import su.opencode.digsigcik.synapi.service.ContainerVerificationService;
import su.opencode.digsigcik.synapi.service.MrpaRegistryCredentialService;
import su.opencode.digsigcik.synapi.service.MrpaRegistryService;
import su.opencode.digsigcik.synapi.util.DateUtils;
import su.opencode.digsigcik.synapi.util.XMLUtil;
import su.opencode.digsigcik.synapi.util.XmlValidator;

import javax.validation.ConstraintViolationException;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.*;

import static org.springframework.http.ResponseEntity.noContent;
import static su.opencode.digsigcik.synapi.controller.ApiControllerV1.createContainerSelector;
import static su.opencode.digsigcik.synapi.util.Constants.*;
import static su.opencode.digsigcik.synapi.util.JwtTokenUtils.getAuthenticationEmail;
import static su.opencode.digsigcik.synapi.util.XMLUtil.convertStringToDocument;

@RestController
@MchdExceptionHandler
@RequestMapping("/api/v1/mrpa_registry" )
public class MrpaRegistryController {
    private final ModelMapper modelMapper;
    private final MrpaRegistryService mrpaRegistryService;
    private final MrpaRegistryCredentialService mrpaRegistryCredentialService;
    private final ContainerVerificationService containerVerificationService;
    private final ContainerService containerService;

    public MrpaRegistryController(ModelMapper modelMapper, MrpaRegistryService mrpaRegistryService, MrpaRegistryCredentialService mrpaRegistryCredentialService, ContainerVerificationService containerVerificationService, ContainerService containerService) {
        this.modelMapper = modelMapper;
        this.mrpaRegistryService = mrpaRegistryService;
        this.mrpaRegistryCredentialService = mrpaRegistryCredentialService;
        this.containerVerificationService = containerVerificationService;
        this.containerService = containerService;
    }

    @Operation(summary = CONTROLLER_MRPA_FIND_ALL_SUMMARY,
            description = CONTROLLER_MRPA_FIND_ALL_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @GetMapping(produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> findAllMrpaRegistry(){
        List<MrpaRegistryWithFioListDto> mrpaRegistryFindAll;
        try {
            mrpaRegistryFindAll = this.mrpaRegistryService.findAllMrpaRegistry();
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(MRPA_REGISTRY_IS_NULL);
        }
        for(MrpaRegistryWithFioListDto m: mrpaRegistryFindAll){
            m.credentials = this.mrpaRegistryService.getCredentialByMrpaRegistryId(m.getId());
        }
        return new ResponseEntity<>(mrpaRegistryFindAll, HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_MRPA_SAVE_SUMMARY, description = CONTROLLER_MRPA_SAVE_DESCRIPTION )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
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
        mrpaRegistry.setCreateDate(String.valueOf(Instant.now()));
        MrpaRegistry mrpaRegistrySaved;
        try {
            mrpaRegistrySaved = this.mrpaRegistryService.saveMrpaRegistry(mrpaRegistry);
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
                mrpaRegistryCredentialSaved = this.mrpaRegistryCredentialService.saveMrpaRegistryCredential(mrpaRegistryCredential);
            } catch (ConstraintViolationException e) {
                throw new BadRequestException(MRPA_REGISTRY_CREDENTIAL_IS_NULL);
            }
            System.out.println(mrpaRegistryCredentialSaved);
        }
////--- Generate and save XML M4D ---
//        MrpaRegistryFullDTO mrpaRegistryFullDTO = this.mrpaRegistryService.findMrpaRegistryFullById(mrpaRegistrySaved.getId());
//        mrpaRegistryFullDTO.credentials = this.mrpaRegistryService.getCredentialByMrpaRegistryFullId(mrpaRegistrySaved.getId());
//        MrpaRegistryFullXML mrpaRegistryFullXML = this.mrpaRegistryService.ConvertMrpaRegistryFullDtoToFullXml(mrpaRegistryFullDTO);
//        String xmlString = XMLUtil.marshall(mrpaRegistryFullXML, MrpaRegistryFullXML.class);
//        try {
//            this.mrpaRegistryService.saveMrpaXml(mrpaRegistrySaved.getId(), xmlString, mrpaRegistryFullXML.getIdFile());
//        } catch (ConstraintViolationException e) {
//            throw new BadRequestException(MRPA_REGISTRY_XML_IS_NULL);
//        }
////--------------------


        MrpaRegistryWithFioDTO mrpaRegistryWithFioDTOS = this.mrpaRegistryService.findByMrpaRegistryId(mrpaRegistrySaved.getId());
        mrpaRegistryWithFioDTOS.credentials = this.mrpaRegistryService.getCredentialByMrpaRegistryId(mrpaRegistryWithFioDTOS.getId());
        return new ResponseEntity<>(mrpaRegistryWithFioDTOS, HttpStatus.OK);
    }


    @Operation(summary = CONTROLLER_MRPA_SAVE_SUMMARY, description = CONTROLLER_MRPA_SAVE_DESCRIPTION )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @PostMapping(value = "save_all",produces = "application/json; charset=UTF-8" )
    public ResponseEntity<?> saveAllDataMrpaRegistry(@RequestBody MrpaRegistrySaveFullDto mrpaRegistrySaveFullDto) throws ParseException, JAXBException {
        System.out.println(mrpaRegistrySaveFullDto);
        MrpaRegistry mrpaRegistry = convertMrpaRegistryToEntity(mrpaRegistrySaveFullDto.mrpaRegistry);
        mrpaRegistry.setStatus(StatusTypeEnum.NOT_SIGNED);
        mrpaRegistry.setGuid();
        mrpaRegistry.setCreateDate(String.valueOf(Instant.now()));
        Organisation organisation = convertOrganisationToEntity(mrpaRegistrySaveFullDto.organisation);
        Principal principal = convertPrincipalToEntity(mrpaRegistrySaveFullDto.principal);
        Representative representative = convertRepresentativeToEntity(mrpaRegistrySaveFullDto.representative);
        RepresentativePassport representativePassport = convertRepresentativePassportToEntity(mrpaRegistrySaveFullDto.representative.representativePassport);
        MrpaRegistry mrpaRegistrySaved;
        try {
            mrpaRegistrySaved = this.mrpaRegistryService.saveAllDataMrpaRegistry(mrpaRegistry, organisation, principal, representative, representativePassport, null, List.of(mrpaRegistrySaveFullDto.getCredentials()));
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(MRPA_REGISTRY_IS_NULL);
        }

//        //--- Generate and save XML M4D ---
//        MrpaRegistryFullDTO mrpaRegistryFullDTO = this.mrpaRegistryService.findMrpaRegistryFullById(mrpaRegistrySaved.getId());
//        mrpaRegistryFullDTO.credentials = this.mrpaRegistryService.getCredentialByMrpaRegistryFullId(mrpaRegistrySaved.getId());
//        MrpaRegistryFullXML mrpaRegistryFullXML = this.mrpaRegistryService.ConvertMrpaRegistryFullDtoToFullXml(mrpaRegistryFullDTO);
//        String xmlString = XMLUtil.marshall(mrpaRegistryFullXML, MrpaRegistryFullXML.class);
//        try {
//            this.mrpaRegistryService.saveMrpaXml(mrpaRegistrySaved.getId(), xmlString, mrpaRegistryFullXML.getIdFile());
//        } catch (ConstraintViolationException e) {
//            throw new BadRequestException(MRPA_REGISTRY_XML_IS_NULL);
//        }
//        //--------------------

        MrpaRegistryWithFioDTO mrpaRegistryWithFioDTOS = this.mrpaRegistryService.findByMrpaRegistryId(mrpaRegistrySaved.getId());
        mrpaRegistryWithFioDTOS.credentials = this.mrpaRegistryService.getCredentialByMrpaRegistryId(mrpaRegistrySaved.getId());
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
            description = PARAMETER_MRPA_ID_DESCRIPTION, example = "1" ) Long id) throws ParseException, JAXBException {
        MrpaRegistryFullDTO mrpaRegistryFind;
        try {
            mrpaRegistryFind = this.mrpaRegistryService.findMrpaRegistryFullById(id);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(MRPA_REGISTRY_IS_NULL);
        }

        MrpaRegistryFullDTO mrpaRegistryFullDTO = this.mrpaRegistryService.findMrpaRegistryFullById(mrpaRegistryFind.getId());
        mrpaRegistryFullDTO.credentials = this.mrpaRegistryService.getCredentialByMrpaRegistryFullId(mrpaRegistryFind.getId());
        MrpaRegistryFullXML mrpaRegistryFullXML = this.mrpaRegistryService.ConvertMrpaRegistryFullDtoToFullXml(mrpaRegistryFullDTO);
        String xmlString = XMLUtil.marshall(mrpaRegistryFullXML, MrpaRegistryFullXML.class);

        mrpaRegistryFind.setMrpaXml(xmlString);
        mrpaRegistryFind.credentials = this.mrpaRegistryService.getCredentialByMrpaRegistryFullId(mrpaRegistryFind.getId());

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
            mrpaRegistryUpdated = this.mrpaRegistryService.updateMrpaRegistry(mrpaRegistry);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(MRPA_REGISTRY_IS_NULL);
        }
        MrpaRegistryWithFioDTO mrpaRegistryWithFioDTOS = this.mrpaRegistryService.findByMrpaRegistryId(mrpaRegistryUpdated.getId());
        mrpaRegistryWithFioDTOS.credentials = this.mrpaRegistryService.getCredentialByMrpaRegistryId(mrpaRegistryUpdated.getId());
        return new ResponseEntity<>(mrpaRegistryWithFioDTOS, HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_MRPA_UPDATE_SUMMARY, description = CONTROLLER_MRPA_UPDATE_DESCRIPTION )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @PostMapping(value = "update_all",produces = "application/json; charset=UTF-8" )
    public ResponseEntity<?> updateAllDataMrpaRegistry(@RequestBody MrpaRegistrySaveFullDto mrpaRegistrySaveFullDto) {
        MrpaRegistry mrpaRegistry = this.mrpaRegistryService.findById(mrpaRegistrySaveFullDto.mrpaRegistry.getId());
        mrpaRegistry.setStartDate(mrpaRegistrySaveFullDto.mrpaRegistry.getStartDate());
        mrpaRegistry.setEndDate(mrpaRegistrySaveFullDto.mrpaRegistry.getEndDate());
        if(mrpaRegistrySaveFullDto.mrpaRegistry.getInternalNumber() != null){
            mrpaRegistry.setInternalNumber(mrpaRegistrySaveFullDto.mrpaRegistry.getInternalNumber());
        }

        Organisation organisation = convertOrganisationToEntity(mrpaRegistrySaveFullDto.organisation);
        Principal principal = convertPrincipalToEntity(mrpaRegistrySaveFullDto.principal);
        Representative representative = convertRepresentativeToEntity(mrpaRegistrySaveFullDto.representative);
        RepresentativePassport representativePassport = convertRepresentativePassportToEntity(mrpaRegistrySaveFullDto.representative.representativePassport);
        MrpaRegistry mrpaRegistryUpdated;
        try {
            mrpaRegistryUpdated = mrpaRegistryService.updateAllDataMrpaRegistry(mrpaRegistry, organisation, principal, representative, representativePassport, null, List.of(mrpaRegistrySaveFullDto.getCredentials()));
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(MRPA_REGISTRY_IS_NULL);
        }

        MrpaRegistryWithFioDTO mrpaRegistryWithFioDTOS = this.mrpaRegistryService.findByMrpaRegistryId(mrpaRegistryUpdated.getId());
        mrpaRegistryWithFioDTOS.credentials = this.mrpaRegistryService.getCredentialByMrpaRegistryId(mrpaRegistryUpdated.getId());
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
            this.mrpaRegistryService.deleteMrpaRegistry(id);
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
            mrpaRegistryFindByInn = this.mrpaRegistryService.findByMrpaRegistryPrincipalInn(inn);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(MRPA_REGISTRY_IS_NULL);
        }
        mrpaRegistryFindByInn.credentials = this.mrpaRegistryService.getCredentialByMrpaRegistryId(mrpaRegistryFindByInn.getId());
        return new ResponseEntity<>(mrpaRegistryFindByInn, HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_MRPA_UPLOAD_SUMMARY, description = CONTROLLER_MRPA_UPLOAD_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @PostMapping(value = "/{id}/upload_dscp", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> uploadSig(@PathVariable @Parameter(name = "id",
            description = PARAMETER_MRPA_ID_DESCRIPTION, example = "1" ) Long id, @RequestPart("container") MultipartFile container) throws IOException, ParseException, JAXBException {
        String extension = FilenameUtils.getExtension(container.getOriginalFilename());
        if(!Objects.equals(extension, "dscp")) {
            throw new BadRequestException(MRPA_REGISTRY_WRONG_FORMAT);
        }

        ReportDTO reportDTO = this.containerVerificationService.verifyContainer(container);

        reportDTO.getResults().stream()
                .flatMap(result -> result.getVerificationResults().stream())
                .findFirst()
                .ifPresent(verificationResult -> {
                    boolean isResultTrue = verificationResult.isResult();
                    System.out.println("Result value: " + isResultTrue);
                    if(!isResultTrue){
                        throw new BadRequestException(MRPA_REGISTRY_NOT_VERIFY);
                    }
                });

        ContainerSelector containerSelector = createContainerSelector(ContainerSelectorCode.SOURCE_FILE);
        ContainerPart res = this.containerService.getContainerPart(container, containerSelector, false);
        var resource = new ByteArrayResource(res.getData());
        MrpaRegistry mrpaRegistry = this.mrpaRegistryService.findById(id);
        if (mrpaRegistry == null) {
            throw new BadRequestException(MRPA_REGISTRY_IS_NULL);
        }
        //----------------- XML M4D ----------------------
        MrpaRegistryFullDTO mrpaRegistryFullDTO = this.mrpaRegistryService.findMrpaRegistryFullById(id);
        mrpaRegistryFullDTO.credentials = this.mrpaRegistryService.getCredentialByMrpaRegistryFullId(id);
        MrpaRegistryFullXML mrpaRegistryFullXML = this.mrpaRegistryService.ConvertMrpaRegistryFullDtoToFullXml(mrpaRegistryFullDTO);
        String xmlString = XMLUtil.marshall(mrpaRegistryFullXML, MrpaRegistryFullXML.class);
        //-------------------------------------------------
        if(!Arrays.equals(resource.getByteArray(), xmlString.getBytes())){
            throw new BadRequestException(MRPA_REGISTRY_NOT_VERIFY);
        }

        mrpaRegistry.setStatus(StatusTypeEnum.VALID);
        mrpaRegistry.setSigPath(Arrays.toString(container.getBytes()));
        MrpaRegistry mrpaRegistrySaved;
        try {
            mrpaRegistrySaved = this.mrpaRegistryService.saveMrpaRegistry(mrpaRegistry);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(MRPA_REGISTRY_IS_NULL);
        }
        MrpaRegistryWithFioDTO mrpaRegistryWithFioDTOS = this.mrpaRegistryService.findByMrpaRegistryId(mrpaRegistrySaved.getId());
        mrpaRegistryWithFioDTOS.credentials = this.mrpaRegistryService.getCredentialByMrpaRegistryId(mrpaRegistrySaved.getId());
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
            this.mrpaRegistryService.recallMrpaRegistryById(id);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(ID_IS_NULL);
        }
        MrpaRegistryWithFioDTO mrpaRegistryWithFioDTOS = this.mrpaRegistryService.findByMrpaRegistryId(id);
        mrpaRegistryWithFioDTOS.credentials = this.mrpaRegistryService.getCredentialByMrpaRegistryId(id);
        return new ResponseEntity<>(mrpaRegistryWithFioDTOS, HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_MRPA_UPLOAD_XML_SUMMARY, description = CONTROLLER_MRPA_UPLOAD_XML_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @PostMapping(value = "/upload", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> upload(@RequestPart("srcFile") MultipartFile sourceFile) throws IOException, JAXBException, ParseException, SAXException {
        String extension = FilenameUtils.getExtension(sourceFile.getOriginalFilename());
        if(!Objects.equals(extension, "xml")) {
            throw new BadRequestException(MRPA_REGISTRY_WRONG_FORMAT_XML);
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
                throw new BadRequestException(MRPA_REGISTRY_WRONG_FORMAT_XML);
            }

            if (m4dXml == null) {
                throw new BadRequestException(MRPA_IS_NULL);
            }

            int guid;
            guid = this.mrpaRegistryService.findMrpaRegistryByGuid(m4dXml.document.powerAttorneys.getMrpa().getGuid());
            if (guid > 0) {
                throw new BadRequestException(MRPA_REGISTRY_IS_EXISTS);
            }

            MrpaRegistry mrpaRegistry = new MrpaRegistry();
            mrpaRegistry.setGuid(m4dXml.document.powerAttorneys.mrpa.getGuid());
            mrpaRegistry.setInternalNumber(m4dXml.document.powerAttorneys.mrpa.getInternalNumber());
            mrpaRegistry.setStartDate(DateUtils.parse(m4dXml.document.powerAttorneys.mrpa.getStartDate()).toInstant().toString());
            mrpaRegistry.setEndDate(DateUtils.parse(m4dXml.document.powerAttorneys.mrpa.getEndDate()).toInstant().toString());
            //mrpaRegistry.setMrpaXml(content);
            mrpaRegistry.setMrpaXmlFilename(sourceFile.getOriginalFilename());
            mrpaRegistry.setStatus(StatusTypeEnum.NOT_SIGNED);

            Organisation organisation = new Organisation();
            organisation.setInn(m4dXml.document.powerAttorneys.principals.principal.legalEntity.organisation.getInn());
            organisation.setKpp(m4dXml.document.powerAttorneys.principals.principal.legalEntity.organisation.getKpp());
            organisation.setOgrn(m4dXml.document.powerAttorneys.principals.principal.legalEntity.organisation.getOgrn());
            organisation.setName(m4dXml.document.powerAttorneys.principals.principal.legalEntity.organisation.getName());
            organisation.setRegionCode(m4dXml.document.powerAttorneys.principals.principal.legalEntity.organisation.addressRegion.getRegionCode());
            organisation.setLegalAddress(m4dXml.document.powerAttorneys.principals.principal.legalEntity.organisation.addressRegion.getLegalAddress());
            if (organisation.getInn() == null || organisation.getInn().isEmpty()) {
                throw new BadRequestException(ORGANISATION_IS_NULL);
            }
            Principal principal = new Principal();
            principal.setInn(m4dXml.document.powerAttorneys.principals.principal.legalEntity.principal.principal.getInn());
            principal.setSnils(m4dXml.document.powerAttorneys.principals.principal.legalEntity.principal.principal.getSnils());
            principal.setJobTitle(m4dXml.document.powerAttorneys.principals.principal.legalEntity.principal.principal.getJobTitle());
            principal.setName(m4dXml.document.powerAttorneys.principals.principal.legalEntity.principal.principal.fioPrincipal.fio.getName());
            principal.setSurname(m4dXml.document.powerAttorneys.principals.principal.legalEntity.principal.principal.fioPrincipal.fio.getSurname());
            principal.setPatronymic(m4dXml.document.powerAttorneys.principals.principal.legalEntity.principal.principal.fioPrincipal.fio.getPatronymic());
            if (principal.getInn() == null || principal.getInn().isEmpty()) {
                throw new BadRequestException(PRINCIPAL_IS_NULL);
            }
            Representative representative = new Representative();
            representative.setInn(m4dXml.document.powerAttorneys.infoRepresentative.representative.detailRepresentative.getInn());
            representative.setSnils(m4dXml.document.powerAttorneys.infoRepresentative.representative.detailRepresentative.getSnils());
            representative.setBirthDate(DateUtils.parse(m4dXml.document.powerAttorneys.infoRepresentative.representative.detailRepresentative.infoIndividual.getBirthDate()).toInstant().toString());
            representative.setName(m4dXml.document.powerAttorneys.infoRepresentative.representative.detailRepresentative.infoIndividual.fioRepresentative.getName());
            representative.setSurname(m4dXml.document.powerAttorneys.infoRepresentative.representative.detailRepresentative.infoIndividual.fioRepresentative.getSurname());
            representative.setPatronymic(m4dXml.document.powerAttorneys.infoRepresentative.representative.detailRepresentative.infoIndividual.fioRepresentative.getPatronymic());
            if (representative.getInn() == null || representative.getInn().isEmpty()) {
                throw new BadRequestException(REPRESENTATIVE_IS_NULL);
            }
            RepresentativePassport represPassport = new RepresentativePassport();
            represPassport.setSeriesNumber(m4dXml.document.powerAttorneys.infoRepresentative.representative.detailRepresentative.infoIndividual.representativePassport.getSeriesNumber());
            represPassport.setDateIssue(DateUtils.parse(m4dXml.document.powerAttorneys.infoRepresentative.representative.detailRepresentative.infoIndividual.representativePassport.getDateIssue()).toInstant().toString());
            represPassport.setWhoIssue(m4dXml.document.powerAttorneys.infoRepresentative.representative.detailRepresentative.infoIndividual.representativePassport.getWhoIssue());
            represPassport.setDepartmentCode(m4dXml.document.powerAttorneys.infoRepresentative.representative.detailRepresentative.infoIndividual.representativePassport.getDepartmentCode());

            List<Credential> credentialList = new ArrayList<>();
            if (m4dXml.document.powerAttorneys.credential.credentials != null) {
                for (M4dXml.Document.PowerAttorneys.Credentials.Credential c : m4dXml.document.powerAttorneys.credential.credentials) {
                    Credential newCredential = new Credential();
                    newCredential.setCode(c.getCode());
                    newCredential.setName(c.getName());
                    credentialList.add(newCredential);
                }
            }

            MrpaRegistry mrpaRegistrySaved;
            try {
                mrpaRegistrySaved = this.mrpaRegistryService.saveAllDataMrpaRegistry(mrpaRegistry, organisation, principal, representative, represPassport, credentialList, null);
            } catch (ConstraintViolationException e) {
                throw new BadRequestException(MRPA_REGISTRY_IS_NULL);
            }

            MrpaRegistryWithFioDTO mrpaRegistryWithFioDTOS = this.mrpaRegistryService.findByMrpaRegistryId(mrpaRegistrySaved.getId());
            mrpaRegistryWithFioDTOS.credentials = this.mrpaRegistryService.getCredentialByMrpaRegistryId(mrpaRegistrySaved.getId());
            return new ResponseEntity<>(mrpaRegistryWithFioDTOS, HttpStatus.OK);
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
            @ApiResponse(code = 204, message = API_RESPONSE_CODE_204_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer token for authorization", required = true, dataType = "string", paramType = "header")
    })
    @GetMapping(value = "/me",produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> findAllMrpaRegistryForUser(Authentication authentication){
        List<MrpaRegistryWithFioListDto> listMrpaRegistry = mrpaRegistryService.findAllMrpaRegistryByLogin(getAuthenticationEmail(authentication));

        return listMrpaRegistry.isEmpty() ?
                noContent().build() :
                new ResponseEntity<>(listMrpaRegistry, HttpStatus.OK);
    }

    private MrpaRegistry convertMrpaRegistryToEntity(MrpaRegistrySaveFullDto.MrpaRegistry mrpaRegistryDto){
        MrpaRegistry mrpaRegistry = modelMapper.map(mrpaRegistryDto, MrpaRegistry.class);
        return mrpaRegistry;
    }

    private Organisation convertOrganisationToEntity(MrpaRegistrySaveFullDto.Organisation organisationDto){
        Organisation organisation = modelMapper.map(organisationDto, Organisation.class);
        return organisation;
    }

    private Principal convertPrincipalToEntity(MrpaRegistrySaveFullDto.Principal principalDto){
        Principal principal = modelMapper.map(principalDto, Principal.class);
        return principal;
    }

    private Representative convertRepresentativeToEntity(MrpaRegistrySaveFullDto.Representative representativeDto){
        Representative representative = modelMapper.map(representativeDto, Representative.class);
        return representative;
    }

    private RepresentativePassport convertRepresentativePassportToEntity(MrpaRegistrySaveFullDto.RepresentativePassport representativePassportDto){
        RepresentativePassport representativePassport = modelMapper.map(representativePassportDto, RepresentativePassport.class);
        return representativePassport;
    }
}
