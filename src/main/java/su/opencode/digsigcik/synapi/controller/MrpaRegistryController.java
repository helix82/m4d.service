package su.opencode.digsigcik.synapi.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import su.opencode.digsigcik.synapi.annotation.MchdExceptionHandler;
import su.opencode.digsigcik.synapi.api.model.ContainerPart;
import su.opencode.digsigcik.synapi.api.model.ContainerSelector;
import su.opencode.digsigcik.synapi.dscp.ContainerSelectorCode;
import su.opencode.digsigcik.synapi.dto.*;
import su.opencode.digsigcik.synapi.dto.m4d.MrpaRegistryFullXML;
import su.opencode.digsigcik.synapi.enums.StatusTypeEnum;
import su.opencode.digsigcik.synapi.exception.BadRequestException;
import su.opencode.digsigcik.synapi.exception.MchdException;
import su.opencode.digsigcik.synapi.model.*;
import su.opencode.digsigcik.synapi.model.m4d.M4dXml;
import su.opencode.digsigcik.synapi.service.*;
import su.opencode.digsigcik.synapi.util.DateUtils;
import su.opencode.digsigcik.synapi.util.XMLUtil;

import javax.validation.ConstraintViolationException;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.*;

import static su.opencode.digsigcik.synapi.controller.ApiControllerV1.createContainerSelector;
import static su.opencode.digsigcik.synapi.util.Constants.*;
import static su.opencode.digsigcik.synapi.util.XMLUtil.convertStringToDocument;

@RestController
@MchdExceptionHandler
@RequestMapping("/api/v1/mrpa_registry" )
public class MrpaRegistryController {
    private final MrpaRegistryService mrpaRegistryService;
    private final MrpaRegistryCredentialService mrpaRegistryCredentialService;
    private final ContainerVerificationService containerVerificationService;
    private final ContainerService containerService;
    private final OrganisationService organisationService;
    private final PrincipalService principalService;
    private final RepresentativeService representativeService;
    private final RepresentativePassportService representativePassportService;
    private final CredentialService credentialService;

    public MrpaRegistryController(MrpaRegistryService mrpaRegistryService, MrpaRegistryCredentialService mrpaRegistryCredentialService, ContainerVerificationService containerVerificationService, ContainerService containerService, OrganisationService organisationService, PrincipalService principalService, RepresentativeService representativeService, RepresentativePassportService representativePassportService, CredentialService credentialService) {
        this.mrpaRegistryService = mrpaRegistryService;
        this.mrpaRegistryCredentialService = mrpaRegistryCredentialService;
        this.containerVerificationService = containerVerificationService;
        this.containerService = containerService;
        this.organisationService = organisationService;
        this.principalService = principalService;
        this.representativeService = representativeService;
        this.representativePassportService = representativePassportService;
        this.credentialService = credentialService;
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
            mrpaRegistryFindAll = mrpaRegistryService.findAllMrpaRegistry();
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(MRPA_REGISTRY_IS_NULL);
        }
        for(MrpaRegistryWithFioListDto m: mrpaRegistryFindAll){
            m.credentials = mrpaRegistryService.getCredentialByMrpaRegistryId(m.getId());
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
        mrpaRegistryFullDTO.credentials = mrpaRegistryService.getCredentialByMrpaRegistryFullId(mrpaRegistrySaved.getId());
        MrpaRegistryFullXML mrpaRegistryFullXML = mrpaRegistryService.ConvertMrpaRegistryFullDtoToFullXml(mrpaRegistryFullDTO);
        String xmlString = XMLUtil.marshall(mrpaRegistryFullXML, MrpaRegistryFullXML.class);
        try {
            mrpaRegistryService.saveMrpaXml(mrpaRegistrySaved.getId(), xmlString, mrpaRegistryFullXML.getIdFile());
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(MRPA_REGISTRY_XML_IS_NULL);
        }
//--------------------


        MrpaRegistryWithFioDTO mrpaRegistryWithFioDTOS = mrpaRegistryService.findByMrpaRegistryId(mrpaRegistrySaved.getId());
        mrpaRegistryWithFioDTOS.credentials = mrpaRegistryService.getCredentialByMrpaRegistryId(mrpaRegistryWithFioDTOS.getId());
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
        mrpaRegistryFind.credentials = mrpaRegistryService.getCredentialByMrpaRegistryFullId(mrpaRegistryFind.getId());
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
        mrpaRegistryWithFioDTOS.credentials = mrpaRegistryService.getCredentialByMrpaRegistryId(mrpaRegistryUpdated.getId());
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
        mrpaRegistryFindByInn.credentials = mrpaRegistryService.getCredentialByMrpaRegistryId(mrpaRegistryFindByInn.getId());
        return new ResponseEntity<>(mrpaRegistryFindByInn, HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_MRPA_UPLOAD_SUMMARY, description = CONTROLLER_MRPA_UPLOAD_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @PostMapping(value = "/{id}/upload_dscp", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> uploadSig(@PathVariable @Parameter(name = "id",
            description = PARAMETER_MRPA_ID_DESCRIPTION, example = "1" ) Long id, @RequestPart("container") MultipartFile container) throws IOException {
        String extension = FilenameUtils.getExtension(container.getOriginalFilename());
        if(!Objects.equals(extension, "dscp")) {
            throw new BadRequestException(MRPA_REGISTRY_WRONG_FORMAT);
        }

        ReportDTO reportDTO = containerVerificationService.verifyContainer(container);

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
        ContainerPart res = containerService.getContainerPart(container, containerSelector, false);
        var resource = new ByteArrayResource(res.getData());
        MrpaRegistry mrpaRegistry = mrpaRegistryService.findById(id);
        if (mrpaRegistry == null) {
            throw new BadRequestException(MRPA_REGISTRY_IS_NULL);
        }
        if(!Arrays.equals(resource.getByteArray(), mrpaRegistry.getMrpaXml().getBytes())){
            throw new BadRequestException(MRPA_REGISTRY_NOT_VERIFY);
        }

        mrpaRegistry.setStatus(StatusTypeEnum.VALID);
        mrpaRegistry.setSigPath(Arrays.toString(container.getBytes()));
        MrpaRegistry mrpaRegistrySaved;
        try {
            mrpaRegistrySaved = mrpaRegistryService.saveMrpaRegistry(mrpaRegistry);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(MRPA_REGISTRY_IS_NULL);
        }
        MrpaRegistryWithFioDTO mrpaRegistryWithFioDTOS = mrpaRegistryService.findByMrpaRegistryId(mrpaRegistrySaved.getId());
        mrpaRegistryWithFioDTOS.credentials = mrpaRegistryService.getCredentialByMrpaRegistryId(mrpaRegistrySaved.getId());
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
        mrpaRegistryWithFioDTOS.credentials = mrpaRegistryService.getCredentialByMrpaRegistryId(id);
        return new ResponseEntity<>(mrpaRegistryWithFioDTOS, HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_MRPA_UPLOAD_XML_SUMMARY, description = CONTROLLER_MRPA_UPLOAD_XML_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @PostMapping(value = "/upload", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> upload(@RequestPart("srcFile") MultipartFile sourceFile) throws IOException, JAXBException, ParseException {
        String extension = FilenameUtils.getExtension(sourceFile.getOriginalFilename());
        if(!Objects.equals(extension, "xml")) {
            throw new BadRequestException(MRPA_REGISTRY_WRONG_FORMAT_XML);
        }
        String content;

        byte[] bom = new byte[3];
        try (InputStream is = sourceFile.getInputStream()) {
            // read 3 bytes of a file.
            is.read(bom);
            // BOM encoded as ef bb bf
            String cont = new String(Hex.encodeHex(bom));
            if ("efbbbf".equalsIgnoreCase(cont)) {
                byte[] bytes = sourceFile.getBytes();
                ByteBuffer bb = ByteBuffer.wrap(bytes);
                System.out.println("Found BOM!");
                // get the first 3 bytes
                bb.get(bom, 0, bom.length);
                // remaining
                byte[] contentAfterFirst3Bytes = new byte[bytes.length - 3];
                bb.get(contentAfterFirst3Bytes, 0, contentAfterFirst3Bytes.length);
                System.out.println("Remove the first 3 bytes, and overwrite the file!");
                // override the same path
                System.out.println(new String(contentAfterFirst3Bytes, StandardCharsets.UTF_8));
                content = new String(contentAfterFirst3Bytes, StandardCharsets.UTF_8);
            } else {
                content = new String(sourceFile.getBytes());
            }
        }

        M4dXml m4dXml;
        try{
            Document document = convertStringToDocument(content);
            assert document != null;
            m4dXml = XMLUtil.unmarshal(document, M4dXml.class);
        } catch (Exception e) {
            throw new BadRequestException(MRPA_REGISTRY_WRONG_FORMAT_XML);
        }

        Long orgId;
        Long principId;
        Long represId;
        if(m4dXml == null) {
            throw new BadRequestException(MRPA_IS_NULL);
        }

        int guid;
        guid = mrpaRegistryService.findMrpaRegistryByGuid(m4dXml.document.powerAttorneys.getMrpa().getGuid());
        if(guid>0){
            throw new BadRequestException(MRPA_REGISTRY_IS_EXISTS);
        }

        MrpaRegistry mrpaRegistry = new MrpaRegistry();
        mrpaRegistry.setGuid(m4dXml.document.powerAttorneys.mrpa.getGuid());
        mrpaRegistry.setInternalNumber(m4dXml.document.powerAttorneys.mrpa.getInternalNumber());
        mrpaRegistry.setStartDate(DateUtils.parse(m4dXml.document.powerAttorneys.mrpa.getStartDate()).toInstant().toString());
        mrpaRegistry.setEndDate(DateUtils.parse(m4dXml.document.powerAttorneys.mrpa.getEndDate()).toInstant().toString());
        mrpaRegistry.setMrpaXml(content);
        mrpaRegistry.setMrpaXmlFilename(sourceFile.getOriginalFilename());
        mrpaRegistry.setStatus(StatusTypeEnum.NOT_SIGNED);

        Organisation organisation = new Organisation();
        organisation.setInn(m4dXml.document.powerAttorneys.principals.principal.legalEntity.organisation.getInn());
        organisation.setKpp(m4dXml.document.powerAttorneys.principals.principal.legalEntity.organisation.getKpp());
        organisation.setOgrn(m4dXml.document.powerAttorneys.principals.principal.legalEntity.organisation.getOgrn());
        organisation.setName(m4dXml.document.powerAttorneys.principals.principal.legalEntity.organisation.getName());
        organisation.setRegionCode(m4dXml.document.powerAttorneys.principals.principal.legalEntity.organisation.addressRegion.getRegionCode());
        organisation.setLegalAddress(m4dXml.document.powerAttorneys.principals.principal.legalEntity.organisation.addressRegion.getLegalAddress());
        if(organisation.getInn() == null || organisation.getInn().isEmpty()){
            throw new BadRequestException(ORGANISATION_IS_NULL);
        }
        Principal principal = new Principal();
        principal.setInn(m4dXml.document.powerAttorneys.principals.principal.legalEntity.principal.principal.getInn());
        principal.setSnils(m4dXml.document.powerAttorneys.principals.principal.legalEntity.principal.principal.getSnils());
        principal.setJobTitle(m4dXml.document.powerAttorneys.principals.principal.legalEntity.principal.principal.getJobTitle());
        principal.setName(m4dXml.document.powerAttorneys.principals.principal.legalEntity.principal.principal.fioPrincipal.fio.getName());
        principal.setSurname(m4dXml.document.powerAttorneys.principals.principal.legalEntity.principal.principal.fioPrincipal.fio.getSurname());
        principal.setPatronymic(m4dXml.document.powerAttorneys.principals.principal.legalEntity.principal.principal.fioPrincipal.fio.getPatronymic());
        if(principal.getInn() == null || principal.getInn().isEmpty()){
            throw new BadRequestException(PRINCIPAL_IS_NULL);
        }
        Representative representative = new Representative();
        representative.setInn(m4dXml.document.powerAttorneys.infoRepresentative.representative.detailRepresentative.getInn());
        representative.setSnils(m4dXml.document.powerAttorneys.infoRepresentative.representative.detailRepresentative.getSnils());
        representative.setBirthDate(DateUtils.parse(m4dXml.document.powerAttorneys.infoRepresentative.representative.detailRepresentative.infoIndividual.getBirthDate()).toInstant().toString());
        representative.setName(m4dXml.document.powerAttorneys.infoRepresentative.representative.detailRepresentative.infoIndividual.fioRepresentative.getName());
        representative.setSurname(m4dXml.document.powerAttorneys.infoRepresentative.representative.detailRepresentative.infoIndividual.fioRepresentative.getSurname());
        representative.setPatronymic(m4dXml.document.powerAttorneys.infoRepresentative.representative.detailRepresentative.infoIndividual.fioRepresentative.getPatronymic());
        if(representative.getInn() == null || representative.getInn().isEmpty()){
            throw new BadRequestException(REPRESENTATIVE_IS_NULL);
        }
        RepresentativePassport represPassport = new RepresentativePassport();
        represPassport.setSeriesNumber(m4dXml.document.powerAttorneys.infoRepresentative.representative.detailRepresentative.infoIndividual.representativePassport.getSeriesNumber());
        represPassport.setDateIssue(DateUtils.parse(m4dXml.document.powerAttorneys.infoRepresentative.representative.detailRepresentative.infoIndividual.representativePassport.getDateIssue()).toInstant().toString());
        represPassport.setWhoIssue(m4dXml.document.powerAttorneys.infoRepresentative.representative.detailRepresentative.infoIndividual.representativePassport.getWhoIssue());
        represPassport.setDepartmentCode(m4dXml.document.powerAttorneys.infoRepresentative.representative.detailRepresentative.infoIndividual.representativePassport.getDepartmentCode());

        List<Long> credentials = new ArrayList<>();
        if(m4dXml.document.powerAttorneys.credential.credentials != null){
            for(M4dXml.Document.PowerAttorneys.Credentials.Credential  c : m4dXml.document.powerAttorneys.credential.credentials) {
                List<Credential> credentialList = credentialService.findCredentialByCodeOrName(c.getCode());
                if (credentialList == null) {
                    Credential newCredential = null;
                    newCredential.setCode(c.getCode());
                    newCredential.setName(c.getName());
                    Credential credentialSave;
                    try {
                        credentialSave = credentialService.saveCredential(newCredential);
                    } catch (ConstraintViolationException e) {
                        throw new BadRequestException(CREDENTIAL_IS_NULL);
                    }
                    credentials.add(credentialSave.getId());
                } else {
                    for (Credential crd : credentialList) {
                        credentials.add(crd.getId());
                    }
                }
            }
        }

        //------------------- save organisation ----------------
        Organisation existsOrganisation = organisationService.findByOrganisationInn(organisation.getInn());
        if (existsOrganisation == null) {
            Organisation respOrganisation;
            try {
                respOrganisation = organisationService.saveOrganisation(organisation);
            } catch (ConstraintViolationException e) {
                throw new BadRequestException(ORGANISATION_IS_NULL);
            }
            orgId = respOrganisation.getId();
        } else {
            orgId = existsOrganisation.getId();
        }
        //------------------- end save organisation ----------------

        //------------------- save principal ----------------
        Principal existsPrincipal;
        try{
            existsPrincipal = principalService.findPrincipalByInn(principal.getInn());
            principId = existsPrincipal.getId();
        } catch (MchdException ignore) {
            Principal respPrincipal;
            try {
                respPrincipal = principalService.savePrincipal(principal);
            } catch (ConstraintViolationException e) {
                throw new BadRequestException(PRINCIPAL_IS_NULL);
            }
            principId = respPrincipal.getId();
        }
        //------------------- end save principal ----------------

        //------------------- save representative and passport ----------------
        Representative existsRepresentative;
        try {
            existsRepresentative = representativeService.findRepresentativeByInn(representative.getInn());
            represId = existsRepresentative.getId();
        } catch (MchdException ignore) {
            Representative respRepresentative;
            try {
                respRepresentative = representativeService.saveRepresentative(representative);
            } catch (ConstraintViolationException e) {
                throw new BadRequestException(REPRESENTATIVE_IS_NULL);
            }
            try {
                represPassport.setRepresentativeId(respRepresentative.getId());
                representativePassportService.saveRepresentativePassport(represPassport);
            } catch (ConstraintViolationException e) {
                throw new BadRequestException(REPRESENTATIVE_PASSPORT_IS_NULL);
            }
            represId = respRepresentative.getId();
        }
        //------------------- end save representative and passport ----------------

        if (orgId == null || principId == null || represId == null){
            throw new BadRequestException(MRPA_REGISTRY_NOT_FULL);
        }
        mrpaRegistry.setOrganisationId(orgId);
        mrpaRegistry.setPrincipalId(principId);
        mrpaRegistry.setRepresentativeId(represId);

        MrpaRegistry mrpaRegistrySaved;
        try {
            mrpaRegistrySaved = mrpaRegistryService.saveMrpaRegistry(mrpaRegistry);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(MRPA_REGISTRY_IS_NULL);
        }

        for(Long x : credentials){
            MrpaRegistryCredential mrpaRegistryCredential = null;
            mrpaRegistryCredential.setMrpaRegistryId(mrpaRegistrySaved.getId());
            mrpaRegistryCredential.setCredentialId(x);
            try{
                mrpaRegistryCredentialService.saveMrpaRegistryCredential(mrpaRegistryCredential);
            } catch (ConstraintViolationException e) {
                throw new BadRequestException(MRPA_REGISTRY_CREDENTIAL_IS_NULL);
            }
        }

        MrpaRegistryWithFioDTO mrpaRegistryWithFioDTOS = mrpaRegistryService.findByMrpaRegistryId(mrpaRegistrySaved.getId());
        mrpaRegistryWithFioDTOS.credentials = mrpaRegistryService.getCredentialByMrpaRegistryId(mrpaRegistrySaved.getId());
        return new ResponseEntity<>(mrpaRegistryWithFioDTOS, HttpStatus.OK);
    }
}
