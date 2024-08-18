package su.opencode.digsigcik.m4d.controller;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.val;
import org.apache.commons.io.FilenameUtils;
import org.joda.time.Instant;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
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
import su.opencode.digsigcik.m4d.enums.ContainerSelectorCode;
import su.opencode.digsigcik.m4d.enums.StatusTypeEnum;
import su.opencode.digsigcik.m4d.exception.BadRequestException;
import su.opencode.digsigcik.m4d.model.*;
import su.opencode.digsigcik.m4d.service.M4dService;
import su.opencode.digsigcik.m4d.util.DateUtils;
import su.opencode.digsigcik.m4d.util.XMLUtil;
import su.opencode.digsigcik.m4d.util.XmlValidator;

import javax.validation.ConstraintViolationException;
import javax.xml.bind.JAXBException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.*;

import static org.springframework.http.ResponseEntity.noContent;
import static su.opencode.digsigcik.m4d.util.Constants.*;
import static su.opencode.digsigcik.m4d.util.JwtTokenUtils.getAuthenticationEmail;
import static su.opencode.digsigcik.m4d.util.XMLUtil.convertStringToDocument;

@RestController
@M4dExceptionHandler
@RequestMapping("/api/v1/mrpa_registry" )
public class M4dController {
    @Value("${m4d.handbook.uri}")
    private String HANDBOOK_URI;
    private final ModelMapper modelMapper;
    private final M4dService m4dService;
    @Autowired
    private RestTemplate restTemplate;

    public M4dController(ModelMapper modelMapper, M4dService m4dService) {
        this.modelMapper = modelMapper;
        this.m4dService = m4dService;
    }

    @Operation(summary = CONTROLLER_MRPA_FIND_ALL_SUMMARY,
            description = CONTROLLER_MRPA_FIND_ALL_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE)
    })
    @GetMapping(produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> findAllMrpaRegistry(){
        List<M4dResponseDto> mrpaRegistryFindAll;
        try {
            mrpaRegistryFindAll = this.m4dService.findAllMrpaRegistry();
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(MRPA_REGISTRY_IS_NULL);
        }
        for(M4dResponseDto m: mrpaRegistryFindAll){
            m.credentials = this.m4dService.getCredentialByMrpaRegistryId(m.getId());
        }
        return new ResponseEntity<>(mrpaRegistryFindAll, HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_MRPA_SAVE_SUMMARY, description = CONTROLLER_MRPA_SAVE_DESCRIPTION )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE)
    })
    @PostMapping(value = "save_all",produces = "application/json; charset=UTF-8" )
    public ResponseEntity<?> saveAllDataMrpaRegistry(@RequestBody M4dRequestSaveFullDto m4dSaveFullDto) throws JAXBException, ParseException {
        System.out.println(m4dSaveFullDto);
        M4d m4d = convertMrpaRegistryToEntity(m4dSaveFullDto.mrpaRegistry);
        m4d.setStatus(StatusTypeEnum.NOT_SIGNED);
        m4d.setGuid();
        m4d.setCreateDate(String.valueOf(Instant.now()));
        OrganisationResponse organisation = convertOrganisationToEntity(m4dSaveFullDto.organisation);
        PrincipalResponse principal = convertPrincipalToEntity(m4dSaveFullDto.principal);
        RepresentativeResponse representative = convertRepresentativeToEntity(m4dSaveFullDto.representative);
        RepresentativePassportResponse representativePassport = convertRepresentativePassportToEntity(m4dSaveFullDto.representative.representativePassport);
        M4d m4dSaved;
        try {
            m4dSaved = this.m4dService.saveAllDataMrpaRegistry(m4d, organisation, principal, representative, representativePassport, null, List.of(m4dSaveFullDto.getCredentials()));
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(MRPA_REGISTRY_IS_NULL);
        }
        return new ResponseEntity<>(findMrpaRegistryByIdForResponse(m4dSaved.getId()), HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_MRPA_FIND_BY_ID_SUMMARY,
            description = CONTROLLER_MRPA_FIND_BY_ID_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE)
    })
    @GetMapping(value = "/{id}", produces = "application/json; charset=UTF-8" )
    public ResponseEntity<?> findMrpaRegistryById(@PathVariable @Parameter(name = "id",
            description = PARAMETER_MRPA_ID_DESCRIPTION, example = "1" ) Long id) throws ParseException, JAXBException {
        return new ResponseEntity<>(findMrpaRegistryByIdForResponse(id), HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_MRPA_UPDATE_SUMMARY, description = CONTROLLER_MRPA_UPDATE_DESCRIPTION )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE)
    })
    @PostMapping(value = "update_all",produces = "application/json; charset=UTF-8" )
    public ResponseEntity<?> updateAllDataMrpaRegistry(@RequestBody M4dRequestSaveFullDto m4dSaveFullDto) throws JAXBException, ParseException {
        M4d m4d = this.m4dService.findById(m4dSaveFullDto.mrpaRegistry.getId());
        m4d.setStartDate(m4dSaveFullDto.mrpaRegistry.getStartDate());
        m4d.setEndDate(m4dSaveFullDto.mrpaRegistry.getEndDate());
        if(m4dSaveFullDto.mrpaRegistry.getInternalNumber() != null){
            m4d.setInternalNumber(m4dSaveFullDto.mrpaRegistry.getInternalNumber());
        }

        OrganisationResponse organisation = convertOrganisationToEntity(m4dSaveFullDto.organisation);
        PrincipalResponse principal = convertPrincipalToEntity(m4dSaveFullDto.principal);
        RepresentativeResponse representative = convertRepresentativeToEntity(m4dSaveFullDto.representative);
        RepresentativePassportResponse representativePassport = convertRepresentativePassportToEntity(m4dSaveFullDto.representative.representativePassport);
        M4d m4dUpdated;
        try {
            m4dUpdated = m4dService.updateAllDataMrpaRegistry(m4d, organisation, principal, representative, representativePassport, null, List.of(m4dSaveFullDto.getCredentials()));
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(MRPA_REGISTRY_IS_NULL);
        }
        return new ResponseEntity<>(findMrpaRegistryByIdForResponse(m4dUpdated.getId()), HttpStatus.OK);
    }

    //TODO: @helix82 При удалении физически не удаляем а ставим пометку об удалении
    @Operation(summary = CONTROLLER_MRPA_DELETE_SUMMARY, description = CONTROLLER_MRPA_DELETE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE)
    })
    @DeleteMapping(value = "delete/{id}", produces = "application/json; charset=UTF-8" )
    public ResponseEntity<?> deleteMrpaRegistry(@PathVariable @Parameter(name = "id",
            description = PARAMETER_MRPA_ID_DESCRIPTION, example = "1" ) Long id) {
        M4d m4d = this.m4dService.findById(id);
        if (m4d == null){
            throw new BadRequestException(MRPA_IS_NULL);
        }
        try {
            this.m4dService.deleteMrpaRegistry(id);
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
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE)
    })
    @GetMapping(value = "principal_inn/{inn}", produces = "application/json; charset=UTF-8" )
    public ResponseEntity<?> findMrpaRegistryByPrincipalInn(@PathVariable @Parameter(name = "inn",
            description = PARAMETER_PRINCIPAL_INN_DESCRIPTION, example = "1" ) String inn) {
        M4dResponseDto mrpaRegistryFindByInn;
        try {
            mrpaRegistryFindByInn = this.m4dService.findByMrpaRegistryPrincipalInn(inn);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(MRPA_REGISTRY_IS_NULL);
        }
        mrpaRegistryFindByInn.credentials = this.m4dService.getCredentialByMrpaRegistryId(mrpaRegistryFindByInn.getId());
        return new ResponseEntity<>(mrpaRegistryFindByInn, HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_MRPA_UPLOAD_SUMMARY, description = CONTROLLER_MRPA_UPLOAD_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE)
    })
    @PostMapping(value = "/{id}/upload_dscp", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> uploadSig(@PathVariable @Parameter(name = "id",
            description = PARAMETER_MRPA_ID_DESCRIPTION, example = "1" ) Long id, @RequestPart("container") MultipartFile container) throws IOException, ParseException, JAXBException {
        String extension = FilenameUtils.getExtension(container.getOriginalFilename());
        if(!Objects.equals(extension, "dscp")) {
            throw new BadRequestException(MRPA_REGISTRY_WRONG_FORMAT);
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

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);
            reportDto = restTemplate.postForObject(HANDBOOK_URI+"/get-container-part-json", requestEntity, ReportDto.class);


//        (restTemplate.getForObject("http://localhost:8080/api/v1/get-container-part-json", container, ReportDto.class)).getBody()
//        ReportDto reportDto = restTemplate.getForObject("localhost:8080/api/v1/get-container-part-json", ReportDto.class, container);

            assert reportDto != null;
            reportDto.getResults().stream()
                    .flatMap(result -> result.getVerificationResults().stream())
                    .findFirst()
                    .ifPresent(verificationResult -> {
                        boolean isResultTrue = verificationResult.isResult();
                        System.out.println("Result value: " + isResultTrue);
                        if(!isResultTrue){
                            throw new BadRequestException(MRPA_REGISTRY_NOT_VERIFY);
                        }
                    });

            val selector = new ContainerSelectorResponse();
            selector.setCode(ContainerSelectorCode.SOURCE_FILE);
            //map.add("selector", selector);

            HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity1 = new HttpEntity<>(map, headers);

            res = restTemplate.postForObject(HANDBOOK_URI+"/get-container-part", requestEntity1, ContainerPartResponse.class);
            //res = restTemplate.getForObject("http://localhost:8080/api/v1/get-container-part", container, selector, ContainerPartResponse.class);


        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String fileName : tempFileNames) {
            File f = new File(fileName);
            f.delete();
        }

        assert res != null;
        var resource = new ByteArrayResource(res.getData());
        m4d = this.m4dService.findById(id);
        if (m4d == null) {
            throw new BadRequestException(MRPA_REGISTRY_IS_NULL);
        }
        //----------------- XML M4D ----------------------
        M4dResponseDto m4dFullDTO = this.m4dService.findMrpaRegistryFullById(id);
        m4dFullDTO.credentials = this.m4dService.getCredentialByMrpaRegistryId(id);
        M4dFullXML m4dFullXML = this.m4dService.ConvertMrpaRegistryFullDtoToFullXml(m4dFullDTO);
        String xmlString = XMLUtil.marshall(m4dFullXML, M4dFullXML.class);
        //-------------------------------------------------
        if(!Arrays.equals(resource.getByteArray(), xmlString.getBytes())){
            throw new BadRequestException(MRPA_REGISTRY_NOT_VERIFY);
        }

        m4d.setStatus(StatusTypeEnum.VALID);
        m4d.setSigPath(Arrays.toString(container.getBytes()));
        M4d m4dSaved;
        try {
            m4dSaved = this.m4dService.saveMrpaRegistry(m4d);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(MRPA_REGISTRY_IS_NULL);
        }
        return new ResponseEntity<>(findMrpaRegistryByIdForResponse(m4dSaved.getId()), HttpStatus.OK);

    }

    @Operation(summary = CONTROLLER_MRPA_RECALL_SUMMARY,
            description = CONTROLLER_MRPA_RECALL_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE)
    })
    @PutMapping(value = "recall/{id}", produces = "application/json; charset=UTF-8" )
    public ResponseEntity<?> recallMrpaRegistryById(@PathVariable @Parameter(name = "id",
            description = PARAMETER_MRPA_ID_DESCRIPTION, example = "1" ) Long id) throws JAXBException, ParseException {
        try {
            this.m4dService.recallMrpaRegistryById(id);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(ID_IS_NULL);
        }
        return new ResponseEntity<>(findMrpaRegistryByIdForResponse(id), HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_MRPA_UPLOAD_XML_SUMMARY, description = CONTROLLER_MRPA_UPLOAD_XML_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE)
    })
    @PostMapping(value = "/upload", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> upload(@RequestPart("srcFile") MultipartFile sourceFile) throws IOException, ParseException, SAXException, JAXBException {
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
            guid = this.m4dService.findMrpaRegistryByGuid(m4dXml.document.powerAttorneys.getMrpa().getGuid());
            if (guid > 0) {
                throw new BadRequestException(MRPA_REGISTRY_IS_EXISTS);
            }

            M4d m4d = new M4d();
            m4d.setGuid(m4dXml.document.powerAttorneys.mrpa.getGuid());
            m4d.setInternalNumber(m4dXml.document.powerAttorneys.mrpa.getInternalNumber());
            m4d.setStartDate(DateUtils.parse(m4dXml.document.powerAttorneys.mrpa.getStartDate()).toInstant().toString());
            m4d.setEndDate(DateUtils.parse(m4dXml.document.powerAttorneys.mrpa.getEndDate()).toInstant().toString());
            m4d.setMrpaXmlFilename(sourceFile.getOriginalFilename());
            m4d.setStatus(StatusTypeEnum.NOT_SIGNED);

            OrganisationResponse organisation = new OrganisationResponse();
            organisation.setInn(m4dXml.document.powerAttorneys.principals.principal.legalEntity.organisation.getInn());
            organisation.setKpp(m4dXml.document.powerAttorneys.principals.principal.legalEntity.organisation.getKpp());
            organisation.setOgrn(m4dXml.document.powerAttorneys.principals.principal.legalEntity.organisation.getOgrn());
            organisation.setName(m4dXml.document.powerAttorneys.principals.principal.legalEntity.organisation.getName());
            organisation.setRegionCode(m4dXml.document.powerAttorneys.principals.principal.legalEntity.organisation.addressRegion.getRegionCode());
            organisation.setLegalAddress(m4dXml.document.powerAttorneys.principals.principal.legalEntity.organisation.addressRegion.getLegalAddress());
            if (organisation.getInn() == null || organisation.getInn().isEmpty()) {
                throw new BadRequestException(ORGANISATION_IS_NULL);
            }
            PrincipalResponse principal = new PrincipalResponse();
            principal.setInn(m4dXml.document.powerAttorneys.principals.principal.legalEntity.principal.principal.getInn());
            principal.setSnils(m4dXml.document.powerAttorneys.principals.principal.legalEntity.principal.principal.getSnils());
            principal.setJobTitle(m4dXml.document.powerAttorneys.principals.principal.legalEntity.principal.principal.getJobTitle());
            principal.setName(m4dXml.document.powerAttorneys.principals.principal.legalEntity.principal.principal.fioPrincipal.fio.getName());
            principal.setSurname(m4dXml.document.powerAttorneys.principals.principal.legalEntity.principal.principal.fioPrincipal.fio.getSurname());
            principal.setPatronymic(m4dXml.document.powerAttorneys.principals.principal.legalEntity.principal.principal.fioPrincipal.fio.getPatronymic());
            if (principal.getInn() == null || principal.getInn().isEmpty()) {
                throw new BadRequestException(PRINCIPAL_IS_NULL);
            }
            RepresentativeResponse representative = new RepresentativeResponse();
            representative.setInn(m4dXml.document.powerAttorneys.infoRepresentative.representative.detailRepresentative.getInn());
            representative.setSnils(m4dXml.document.powerAttorneys.infoRepresentative.representative.detailRepresentative.getSnils());
            representative.setBirthDate(DateUtils.parse(m4dXml.document.powerAttorneys.infoRepresentative.representative.detailRepresentative.infoIndividual.getBirthDate()).toInstant().toString());
            representative.setName(m4dXml.document.powerAttorneys.infoRepresentative.representative.detailRepresentative.infoIndividual.fioRepresentative.getName());
            representative.setSurname(m4dXml.document.powerAttorneys.infoRepresentative.representative.detailRepresentative.infoIndividual.fioRepresentative.getSurname());
            representative.setPatronymic(m4dXml.document.powerAttorneys.infoRepresentative.representative.detailRepresentative.infoIndividual.fioRepresentative.getPatronymic());
            if (representative.getInn() == null || representative.getInn().isEmpty()) {
                throw new BadRequestException(REPRESENTATIVE_IS_NULL);
            }
            RepresentativePassportResponse represPassport = new RepresentativePassportResponse();
            represPassport.setSeriesNumber(m4dXml.document.powerAttorneys.infoRepresentative.representative.detailRepresentative.infoIndividual.representativePassport.getSeriesNumber());
            represPassport.setDateIssue(DateUtils.parse(m4dXml.document.powerAttorneys.infoRepresentative.representative.detailRepresentative.infoIndividual.representativePassport.getDateIssue()).toInstant().toString());
            represPassport.setWhoIssue(m4dXml.document.powerAttorneys.infoRepresentative.representative.detailRepresentative.infoIndividual.representativePassport.getWhoIssue());
            represPassport.setDepartmentCode(m4dXml.document.powerAttorneys.infoRepresentative.representative.detailRepresentative.infoIndividual.representativePassport.getDepartmentCode());

            List<CredentialResponse> credentialList = new ArrayList<>();
            if (m4dXml.document.powerAttorneys.credential.credentials != null) {
                for (M4dXml.Document.PowerAttorneys.Credentials.Credential c : m4dXml.document.powerAttorneys.credential.credentials) {
                    CredentialResponse newCredential = new CredentialResponse();
                    newCredential.setCode(c.getCode());
                    newCredential.setName(c.getName());
                    credentialList.add(newCredential);
                }
            }

            M4d m4dSaved;
            try {
                m4dSaved = this.m4dService.saveAllDataMrpaRegistry(m4d, organisation, principal, representative, represPassport, credentialList, null);
            } catch (ConstraintViolationException e) {
                throw new BadRequestException(MRPA_REGISTRY_IS_NULL);
            }

            return new ResponseEntity<>(findMrpaRegistryByIdForResponse(m4dSaved.getId()), HttpStatus.OK);
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
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE)
    })
    @GetMapping(value = "/me",produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> findAllMrpaRegistryForUser(Authentication authentication){
        List<M4dResponseDto> listMrpaRegistry = m4dService.findAllMrpaRegistryByLogin(getAuthenticationEmail(authentication));

        return listMrpaRegistry.isEmpty() ?
                noContent().build() :
                new ResponseEntity<>(listMrpaRegistry, HttpStatus.OK);
    }

    private M4dResponseDto findMrpaRegistryByIdForResponse(Long id) throws ParseException, JAXBException {
        M4dResponseDto mrpaRegistryFind;
        try {
            mrpaRegistryFind = this.m4dService.findMrpaRegistryFullById(id);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(MRPA_REGISTRY_IS_NULL);
        }
        M4dResponseDto mrpaRegistryFullDTO = this.m4dService.findMrpaRegistryFullById(mrpaRegistryFind.getId());
        mrpaRegistryFullDTO.credentials = this.m4dService.getCredentialByMrpaRegistryId(mrpaRegistryFind.getId());
        M4dFullXML mrpaRegistryFullXML = this.m4dService.ConvertMrpaRegistryFullDtoToFullXml(mrpaRegistryFullDTO);
        String xmlString = XMLUtil.marshall(mrpaRegistryFullXML, M4dFullXML.class);
        mrpaRegistryFind.setMrpaXml(xmlString);
        mrpaRegistryFind.credentials = this.m4dService.getCredentialByMrpaRegistryId(mrpaRegistryFind.getId());
        return mrpaRegistryFind;
    }

    private M4d convertMrpaRegistryToEntity(M4dRequestSaveFullDto.MrpaRegistry mrpaRegistryDto){
        M4d m4d = modelMapper.map(mrpaRegistryDto, M4d.class);
        return m4d;
    }

    private OrganisationResponse convertOrganisationToEntity(M4dRequestSaveFullDto.Organisation organisationDto){
        OrganisationResponse organisation = modelMapper.map(organisationDto, OrganisationResponse.class);
        return organisation;
    }

    private PrincipalResponse convertPrincipalToEntity(M4dRequestSaveFullDto.Principal principalDto){
        PrincipalResponse principal = modelMapper.map(principalDto, PrincipalResponse.class);
        return principal;
    }

    private RepresentativeResponse convertRepresentativeToEntity(M4dRequestSaveFullDto.Representative representativeDto){
        RepresentativeResponse representative = modelMapper.map(representativeDto, RepresentativeResponse.class);
        return representative;
    }

    private RepresentativePassportResponse convertRepresentativePassportToEntity(M4dRequestSaveFullDto.RepresentativePassport representativePassportDto){
        RepresentativePassportResponse representativePassport = modelMapper.map(representativePassportDto, RepresentativePassportResponse.class);
        return representativePassport;
    }
}
