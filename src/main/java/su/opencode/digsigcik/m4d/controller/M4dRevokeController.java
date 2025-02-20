package su.opencode.digsigcik.m4d.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.io.FilenameUtils;
import org.codehaus.plexus.util.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.m4d.annotation.M4dExceptionHandler;
import su.opencode.digsigcik.m4d.dto.M4dResponseDto;
import su.opencode.digsigcik.m4d.dto.M4dRevokeRequestDto;
import su.opencode.digsigcik.m4d.dto.M4dRevokeXml;
import su.opencode.digsigcik.m4d.dto.ReportDto;
import su.opencode.digsigcik.m4d.enums.FileLogicalTypeEnum;
import su.opencode.digsigcik.m4d.enums.FileMimeTypeEnum;
import su.opencode.digsigcik.m4d.exception.BadRequestException;
import su.opencode.digsigcik.m4d.model.M4d;
import su.opencode.digsigcik.m4d.model.M4dFile;
import su.opencode.digsigcik.m4d.model.M4dRecall;
import su.opencode.digsigcik.m4d.service.M4dFileService;
import su.opencode.digsigcik.m4d.service.M4dRecallService;
import su.opencode.digsigcik.m4d.service.M4dService;
import su.opencode.digsigcik.m4d.util.XMLUtil;

import javax.validation.ConstraintViolationException;
import javax.xml.bind.JAXBException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.*;

import static su.opencode.digsigcik.m4d.util.Constants.*;
import static su.opencode.digsigcik.m4d.util.JwtTokenUtils.getTokenValue;

@RestController
@M4dExceptionHandler
@RequestMapping("/api/v1/m4d/revoke")
@Tag(name = "M4d Revoke Controller")
public class M4dRevokeController {

    @Value("${synapi.container.get-part.uri}")
    private String CONTAINER_PART_URI;
    @Value("${synapi.container.get-part-json.uri}")
    private String CONTAINER_PART_JSON_URI;

    private final ModelMapper modelMapper;
    private final M4dService m4dService;
    private final M4dRecallService m4dRecallService;
    private final M4dFileService m4dFileService;
    private final RestTemplate restTemplate;

    public M4dRevokeController(ModelMapper modelMapper, M4dService m4dService, M4dRecallService m4dRecallService, M4dFileService m4dFileService, RestTemplate restTemplate) {
        this.modelMapper = modelMapper;
        this.m4dService = m4dService;
        this.m4dRecallService = m4dRecallService;
        this.m4dFileService = m4dFileService;
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
            description = PARAMETER_MRPA_RECALL_ID_DESCRIPTION, example = "1") Long id) throws ParseException {
        if (id == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(ID_IS_NULL), HttpStatus.BAD_REQUEST);
        }
        M4dRecall m4dRecall;
        try {
            m4dRecall = this.m4dRecallService.findMrpaRecallById(id);
        } catch (ConstraintViolationException e) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
        M4d m4d;
        try {
            m4d = this.m4dService.findById(m4dRecall.getM4d().getId());
        } catch (ConstraintViolationException e) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
        if (m4d == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_REGISTRY_IS_NULL), HttpStatus.NOT_FOUND);
        }
        M4dResponseDto m4dResponseDto = this.m4dService.convertM4dToM4dResponseDto(m4d, false, true, false, true);
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
        if (mrpaRecall.getSigFile() != null) {
            mrpaRecall.setSigFile(null);
        }
        Long mrpaRegistryId = mrpaRecallRequestDto.getMrpaRegistryId();
        if (mrpaRegistryId == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(ID_IS_NULL), HttpStatus.BAD_REQUEST);
        }
        M4d mrpaRegistry = this.m4dService.findById(mrpaRegistryId);
        mrpaRecall.setM4d(mrpaRegistry);
        mrpaRecall.setCreateDate(LocalDateTime.now());
        mrpaRecall.setStatus("CREATED");
        M4dResponseDto m4dResponseDto = this.m4dService.convertM4dToM4dResponseDto(mrpaRegistry, false, true, false, true);
        M4dResponseDto.RecallDto recallDto = new M4dResponseDto.RecallDto();
        recallDto.setArgument(mrpaRecallRequestDto.getArgument());
        recallDto.setCreateDate(String.valueOf(LocalDateTime.now()));
        m4dResponseDto.setRecall(recallDto);
        M4dRevokeXml mrpaRecallXml = this.m4dRecallService.convertMrpaRecallToXml(m4dResponseDto);
        String xmlRecallString = XMLUtil.marshall(mrpaRecallXml, M4dRevokeXml.class);

        M4dRecall m4dRecallSaved;
        try {
            m4dRecallSaved = this.m4dRecallService.saveM4dRecall(mrpaRecall, xmlRecallString);
        } catch (ConstraintViolationException e) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
        return findMrpaRecallById(m4dRecallSaved.getId());
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
        if(mrpaRecall.getSigFile() != null) {
            if (!mrpaRecall.getSigFile().getId().equals(existsMrpaRecall.getSigFile().getId())) {
                existsMrpaRecall.setSigFile(mrpaRecall.getSigFile());
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
        if (id == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(ID_IS_NULL), HttpStatus.BAD_REQUEST);
        }
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
        if (id == null){
            return new ResponseEntity<>(getCustomErrorJsonFromString(ID_IS_NULL), HttpStatus.BAD_REQUEST);
        }
        if (container == null || container.isEmpty()){
            return new ResponseEntity<>(getCustomErrorJsonFromString(ID_IS_NULL), HttpStatus.BAD_REQUEST);
        }
        String bearerToken = getTokenValue(authentication);
        String extension = FilenameUtils.getExtension(container.getOriginalFilename());
        if(!Objects.equals(extension, "dscp")) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_REGISTRY_WRONG_FORMAT), HttpStatus.BAD_REQUEST);
        }
        M4dRecall mrpaRecall = this.m4dRecallService.findById(id);
        if (mrpaRecall==null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_RECALL_IS_NULL), HttpStatus.BAD_REQUEST);
        }
        if (mrpaRecall.getSigFile()!=null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString("Заявление на отзыв МЧД уже подписано"), HttpStatus.BAD_REQUEST);
        }
        List<String> tempFileNames = new ArrayList<>();
        String tempFileName;
        FileOutputStream fo;
        ReportDto reportDto;
        ByteArrayResource res = null;
        try {
            tempFileName = "/tmp/" + container.getOriginalFilename();
            tempFileNames.add(tempFileName);
            fo = new FileOutputStream(tempFileName);
            fo.write(container.getBytes());
            fo.close();

            LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
            map.add("container", new FileSystemResource(tempFileName));
            map.add("reverify",true);

            HttpHeaders headers = createHttpHeaders(bearerToken);
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);
            reportDto = restTemplate.postForObject(CONTAINER_PART_JSON_URI, requestEntity, ReportDto.class);

            assert reportDto != null;
            reportDto.getResults().stream()
                    .flatMap(result -> result.getVerificationResults().stream())
                    .findFirst()
                    .ifPresent(verificationResult -> {
                        boolean isResultTrue = verificationResult.isResult();
                        System.out.println("Result value: " + isResultTrue);
                        if (!isResultTrue) {
                            throw new BadRequestException(MRPA_REGISTRY_NOT_VERIFY_REPORT);
                        }
                    });

            LinkedMultiValueMap<String, Object> map1 = new LinkedMultiValueMap<>();
            map1.add("container", new FileSystemResource(tempFileName));
            map1.add("selector","sourceFile");
            HttpEntity<MultiValueMap<String, Object>> requestEntity1 = new HttpEntity<>(map1, headers);
            res = restTemplate.postForObject(CONTAINER_PART_URI, requestEntity1, ByteArrayResource.class);

        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String fileName : tempFileNames) {
            File f = new File(fileName);
            f.delete();
        }
        assert res != null;
        var resource = new ByteArrayResource(res.getByteArray());
        M4dRecall m4dRecall;
        try {
            m4dRecall = this.m4dRecallService.findMrpaRecallById(id);
        } catch (ConstraintViolationException e) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
        M4d mrpaRegistryFind;
        try {
            mrpaRegistryFind = this.m4dService.findById(m4dRecall.getM4d().getId());
        } catch (ConstraintViolationException e) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
        if (mrpaRegistryFind == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_REGISTRY_IS_NULL), HttpStatus.BAD_REQUEST);
        }
        M4dResponseDto m4dResponseDto = this.m4dService.convertM4dToM4dResponseDto(mrpaRegistryFind, false, false, false, true);
        String normalizedResource = new String(resource.getByteArray(), StandardCharsets.UTF_8).replaceAll("\\s+", "");
        String normalizedXmlString = m4dResponseDto.recall.getXmlFile().getContent().replaceAll("\\s+", "");
        if (!StringUtils.difference(normalizedResource, normalizedXmlString).isEmpty()) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_REGISTRY_NOT_VERIFY_CONTENT), HttpStatus.BAD_REQUEST);
        }
        M4dFile m4dFile = new M4dFile();
        m4dFile.setFileMimeType(FileMimeTypeEnum.fromValue(container.getContentType()));
        m4dFile.setFileLogicalType(FileLogicalTypeEnum.CONTAINER);
        m4dFile.setFilename(container.getOriginalFilename());

        try (InputStream inputStream = container.getInputStream();
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            m4dFile.setContent(Arrays.toString(byteArrayOutputStream.toByteArray()));
        }
        M4dFile existsM4dFile;
        M4dFile m4dFileSaved;
        Long fileId;
        try {
            existsM4dFile = this.m4dFileService.findM4dFileByContent(m4dFile.getContent());
        } catch (ConstraintViolationException e) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
        if (existsM4dFile == null){
            try {
                m4dFileSaved = this.m4dFileService.saveM4dFile(m4dFile);
            } catch (ConstraintViolationException e) {
                return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
            }
            fileId = m4dFileSaved.getId();
        } else {
            fileId = existsM4dFile.getId();
        }
        try {
            this.m4dRecallService.updateMrpaRecallById(id, fileId);
        } catch (ConstraintViolationException e) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
        }

        return findMrpaRecallById(id);
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
