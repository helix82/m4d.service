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
import su.opencode.digsigcik.m4d.annotation.M4dExceptionHandler;
import su.opencode.digsigcik.m4d.dto.M4dResponseDto;
import su.opencode.digsigcik.m4d.dto.M4dRevokeRequestDto;
import su.opencode.digsigcik.m4d.dto.M4dRevokeXml;
import su.opencode.digsigcik.m4d.dto.ReportDto;
import su.opencode.digsigcik.m4d.dto.response.ContainerPartResponse;
import su.opencode.digsigcik.m4d.enums.FileLogicalTypeEnum;
import su.opencode.digsigcik.m4d.enums.FileMimeTypeEnum;
import su.opencode.digsigcik.m4d.exception.BadRequestException;
import su.opencode.digsigcik.m4d.model.M4d;
import su.opencode.digsigcik.m4d.model.M4dFile;
import su.opencode.digsigcik.m4d.model.M4dRecall;
import su.opencode.digsigcik.m4d.service.M4dRecallService;
import su.opencode.digsigcik.m4d.service.M4dService;
import su.opencode.digsigcik.m4d.util.XMLUtil;

import javax.validation.ConstraintViolationException;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

import static su.opencode.digsigcik.m4d.util.Constants.*;
import static su.opencode.digsigcik.m4d.util.Constants.MRPA_REGISTRY_NOT_VERIFY;
import static su.opencode.digsigcik.m4d.util.JwtTokenUtils.getTokenValue;

@RestController
@M4dExceptionHandler
@RequestMapping("/api/v1/m4d/m4d-revoke")
@Tag(name = "M4d Revoke Controller")
public class M4dRevokeController {
    @Value("${m4d.handbook.uri}")
    private String HANDBOOK_URI;
    private final ModelMapper modelMapper;
    private final M4dService m4dService;
    private final M4dRecallService m4dRecallService;
    private final RestTemplate restTemplate;

    public M4dRevokeController(ModelMapper modelMapper, M4dService m4dService, M4dRecallService m4dRecallService, RestTemplate restTemplate) {
        this.modelMapper = modelMapper;
        this.m4dService = m4dService;
        this.m4dRecallService = m4dRecallService;
        this.restTemplate = restTemplate;
    }

    private static @NotNull HttpHeaders createHttpHeaders(String bearerToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(bearerToken);
        return headers;
    }

    @Operation(summary = CONTROLLER_MRPA_RECALL_FIND_ALL_SUMMARY,
            description = CONTROLLER_MRPA_RECALL_FIND_ALL_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @GetMapping(value = "", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> findAllMrpaRecall() throws ParseException {
        List<M4dResponseDto> mrpaRegistryFindAllRecall;
        try {
            mrpaRegistryFindAllRecall = this.m4dRecallService.findAllMrpaRegistriesWithRecall();
        } catch (ConstraintViolationException e) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
        for (M4dResponseDto m : mrpaRegistryFindAllRecall) {
            m.credentials = this.m4dService.getCredentialByMrpaRegistryId(m.getId());
            m.setStatus(this.m4dService.getStatusM4d(m.getId()));
        }
        return new ResponseEntity<>(mrpaRegistryFindAllRecall, HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_MRPA_RECALL_FIND_BY_ID_SUMMARY,
            description = CONTROLLER_MRPA_RECALL_FIND_BY_ID_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @GetMapping(value = "{id}", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> findMrpaRecallById(@PathVariable @Parameter(name = "id",
            description = PARAMETER_MRPA_RECALL_ID_DESCRIPTION, example = "1") Long id) throws JAXBException, ParseException {
        if (id == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(ID_IS_NULL), HttpStatus.BAD_REQUEST);
        }
        M4d m4d;
        try {
            m4d = this.m4dRecallService.findMrpaRecallById(id);
        } catch (ConstraintViolationException e) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
        if (m4d == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_REGISTRY_IS_NULL), HttpStatus.NOT_FOUND);
        }
        M4dResponseDto m4dResponseDto = this.m4dService.convertM4dToM4dResponseDto(m4d, false, true);
        M4dRevokeXml mrpaRecallXml = this.m4dRecallService.ConvertMrpaRecallToXml(m4dResponseDto);
        String xmlRecallString = XMLUtil.marshall(mrpaRecallXml, M4dRevokeXml.class);
        m4dResponseDto.setRecallXml(xmlRecallString);
        return new ResponseEntity<>(m4dResponseDto, HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_MRPA_RECALL_SAVE_SUMMARY, description = CONTROLLER_MRPA_RECALL_SAVE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @PostMapping("save")
    public ResponseEntity<?> saveMrpaRecall(@RequestBody M4dRevokeRequestDto mrpaRecallRequestDto) throws JAXBException, ParseException {
        M4dRecall mrpaRecall = convertMrpaRecallRequestDtoToEntity(mrpaRecallRequestDto);
        if (mrpaRecall == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_RECALL_ERROR_CONVERT), HttpStatus.BAD_REQUEST);
        }
        if (mrpaRecall.getM4dFile() != null) {
            mrpaRecall.setM4dFile(null);
        }
        Long mrpaRegistryId = mrpaRecallRequestDto.getMrpaRegistryId();
        if (mrpaRegistryId == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(ID_IS_NULL), HttpStatus.BAD_REQUEST);
        }
        if (mrpaRecall.getAuthor() == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_REGISTRY_NOT_FULL), HttpStatus.BAD_REQUEST);
        }
        M4d mrpaRegistry = this.m4dService.findById(mrpaRegistryId);
        mrpaRegistry.setMrpaRecall(mrpaRecall);
        M4d mrpaRegistryUpdated;
        try {
            mrpaRegistryUpdated = this.m4dService.updateMrpaRegistry(mrpaRegistry);
        } catch (ConstraintViolationException e) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
        return findMrpaRecallById(mrpaRegistryUpdated.getMrpaRecall().getId());
    }

    @Operation(summary = CONTROLLER_MRPA_RECALL_UPDATE_SUMMARY, description = CONTROLLER_MRPA_RECALL_UPDATE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @PutMapping("update")
    public ResponseEntity<?> updateMrpaRecall(@RequestBody M4dRevokeRequestDto mrpaRecallRequestDto) throws JAXBException, ParseException {
        M4dRecall mrpaRecall = convertMrpaRecallRequestDtoToEntity(mrpaRecallRequestDto);
        if (mrpaRecall == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_RECALL_ERROR_CONVERT), HttpStatus.BAD_REQUEST);
        }
        Long mrpaRecallId = mrpaRecall.getId();
        if(mrpaRecallId == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString("Can`t update M4d recall, because id null!"), HttpStatus.BAD_REQUEST);
        }
        M4dRecall existsMrpaRecall = this.m4dRecallService.findById(mrpaRecall.getId());
        if (existsMrpaRecall == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(String.format("M4d Recall %s", NOT_FOUND)), HttpStatus.NOT_FOUND);
        }
        if(mrpaRecall.getArgument() != null && !mrpaRecall.getArgument().equals(existsMrpaRecall.getArgument())) {
            existsMrpaRecall.setArgument(mrpaRecall.getArgument());
        }
        if(mrpaRecall.getAuthor() != null && mrpaRecall.getAuthor() != existsMrpaRecall.getAuthor()) {
            existsMrpaRecall.setAuthor(mrpaRecall.getAuthor());
        }
        if(mrpaRecall.getM4dFile() != null) {
            if (!mrpaRecall.getM4dFile().getId().equals(existsMrpaRecall.getM4dFile().getId())) {
                existsMrpaRecall.setM4dFile(mrpaRecall.getM4dFile());
            }
        }
        M4dRecall response;
        try {
            response = this.m4dRecallService.updateMrpaRecall(existsMrpaRecall);
        } catch (ConstraintViolationException e) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
        return findMrpaRecallById(response.getId());
    }

    @Operation(summary = CONTROLLER_MRPA_RECALL_DELETE_SUMMARY, description = CONTROLLER_MRPA_RECALL_DELETE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @DeleteMapping("delete/{id}")
    public ResponseEntity<?> deleteMrpaRecall(@PathVariable @Parameter(name = "id",
            description = PARAMETER_MRPA_RECALL_ID_DESCRIPTION, example = "1") Long id) {
        M4dRecall mrpaRecall = this.m4dRecallService.findById(id);
        if (mrpaRecall == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(String.format("M4d Recall %s", NOT_FOUND)), HttpStatus.NOT_FOUND);
        }
        try {
            this.m4dRecallService.deleteMrpaRecallById(id);
        } catch (ConstraintViolationException e) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
        Map<String, String> body = new HashMap<>();
        body.put("result", "true");

        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_MRPA_RECALL_FIND_ALL_FOR_USER_SUMMARY,
            description = CONTROLLER_MRPA_RECALL_FIND_ALL_FOR_USER_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = API_RESPONSE_CODE_204_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @GetMapping(value = "me", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> findAllM4dRevokeForUser(Authentication authentication) {
        return new ResponseEntity<>(m4dRecallService.findAllM4dRevokeUsers(authentication), HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_MRPA_RECALL_UPLOAD_SUMMARY, description = CONTROLLER_MRPA_RECALL_UPLOAD_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @PutMapping(value = "{id}/upload-dscp", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> uploadSigRecall(Authentication authentication, @PathVariable @Parameter(name = "id",
            description = PARAMETER_MRPA_RECALL_ID_DESCRIPTION, example = "1") Long id, @RequestPart("container") MultipartFile container) throws IOException, JAXBException, ParseException {
        String bearerToken = getTokenValue(authentication);
        String extension = FilenameUtils.getExtension(container.getOriginalFilename());
        if(!Objects.equals(extension, "dscp")) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_REGISTRY_WRONG_FORMAT), HttpStatus.BAD_REQUEST);
        }
        if (id==null){
            return new ResponseEntity<>(getCustomErrorJsonFromString(ID_IS_NULL), HttpStatus.BAD_REQUEST);
        }
        M4dRecall mrpaRecall = this.m4dRecallService.findById(id);
        if (mrpaRecall==null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_RECALL_IS_NULL), HttpStatus.BAD_REQUEST);
        }
        LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        List<String> tempFileNames = new ArrayList<>();
        String tempFileName;
        FileOutputStream fo;
        ReportDto reportDto;
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
        M4d mrpaRegistryFind = this.m4dRecallService.findMrpaRecallById(id);
        if (mrpaRegistryFind == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_REGISTRY_IS_NULL), HttpStatus.BAD_REQUEST);
        }
        M4dResponseDto m4dResponseDto = this.m4dService.convertM4dToM4dResponseDto(mrpaRegistryFind, false, false);
        //----------------- XML RECALL ----------------------
        M4dRevokeXml mrpaRecallXml = this.m4dRecallService.ConvertMrpaRecallToXml(m4dResponseDto);
        String xmlRecallString = XMLUtil.marshall(mrpaRecallXml, M4dRevokeXml.class);
        //-------------------------------------------------
        if (!Arrays.equals(resource.getByteArray(), xmlRecallString.getBytes())) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_REGISTRY_NOT_VERIFY), HttpStatus.BAD_REQUEST);
        }
        M4dFile m4dFile = new M4dFile();
        m4dFile.setContent(Arrays.toString(container.getBytes()));
        m4dFile.setFileMimeType(FileMimeTypeEnum.fromValue(container.getContentType()));
        m4dFile.setFileLogicalType(FileLogicalTypeEnum.CONTAINER);
        m4dFile.setFilename(container.getOriginalFilename());

        mrpaRecall.setM4dFile(m4dFile);

        M4d mrpaRegistry = this.m4dService.findById(mrpaRegistryFind.getId());
        mrpaRegistry.setMrpaRecall(mrpaRecall);
        M4d mrpaRegistrySaved;
        try {
            mrpaRegistrySaved = this.m4dService.saveMrpaRegistry(mrpaRegistry);
        } catch (ConstraintViolationException e) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
        return findMrpaRecallById(mrpaRegistrySaved.getMrpaRecall().getId());
    }

    public Map<String, String> getCustomErrorJsonFromString(String text) {
        Map<String, String> body = new HashMap<>();
        body.put("error", text);
        return body;
    }

    private M4dRecall convertMrpaRecallRequestDtoToEntity(M4dRevokeRequestDto mrpaRecallRequestDto) {
        if (mrpaRecallRequestDto.getSignature() == null || Objects.equals(mrpaRecallRequestDto.getSignature().trim(), "")) {
            mrpaRecallRequestDto.setSignature(null);
        }
        M4dRecall mrpaRecall = modelMapper.map(mrpaRecallRequestDto, M4dRecall.class);
        return mrpaRecall;
    }
}
