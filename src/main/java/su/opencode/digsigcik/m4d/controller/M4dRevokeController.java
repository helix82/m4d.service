package su.opencode.digsigcik.m4d.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.io.FilenameUtils;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.m4d.annotation.M4dExceptionHandler;
import su.opencode.digsigcik.m4d.dto.M4dRegistryResponseDto;
import su.opencode.digsigcik.m4d.dto.M4dRevokeRequestDto;
import su.opencode.digsigcik.m4d.dto.M4dRevokeXml;
import su.opencode.digsigcik.m4d.dto.ReportCheckStatusM4dDto;
import su.opencode.digsigcik.m4d.dto.response.M4dFnsDataResponse;
import su.opencode.digsigcik.m4d.model.M4d;
import su.opencode.digsigcik.m4d.model.M4dRecall;
import su.opencode.digsigcik.m4d.service.M4dFileService;
import su.opencode.digsigcik.m4d.service.M4dRecallService;
import su.opencode.digsigcik.m4d.service.M4dService;
import su.opencode.digsigcik.m4d.service.SynapiClientService;
import su.opencode.digsigcik.m4d.util.CustomMultipartFile;
import su.opencode.digsigcik.m4d.util.XMLUtil;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.*;

import static su.opencode.digsigcik.m4d.util.Constants.*;

@RestController
@M4dExceptionHandler
@RequestMapping("/api/v1/m4d/revoke")
@Tag(name = "M4d Revoke Controller")
public class M4dRevokeController {
    private final ModelMapper modelMapper;
    private final M4dService m4dService;
    private final M4dRecallService m4dRecallService;
    private final M4dFileService m4dFileService;
    private final SynapiClientService synapiClientService;

    public M4dRevokeController(ModelMapper modelMapper, M4dService m4dService, M4dRecallService m4dRecallService, M4dFileService m4dFileService, SynapiClientService synapiClientService) {
        this.modelMapper = modelMapper;
        this.m4dService = m4dService;
        this.m4dRecallService = m4dRecallService;
        this.m4dFileService = m4dFileService;
        this.synapiClientService = synapiClientService;
    }

    @Operation(summary = CONTROLLER_MRPA_RECALL_FIND_ALL_SUMMARY,
            description = CONTROLLER_MRPA_RECALL_FIND_ALL_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @GetMapping(produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> findAllMrpaRecall(Authentication authentication) throws ParseException {
        List<M4dRegistryResponseDto> mrpaRegistryFindAllRecall;
        try {
            mrpaRegistryFindAllRecall = this.m4dRecallService.findAllMrpaRegistriesWithRecall(authentication);
        } catch (ConstraintViolationException e) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
        for (M4dRegistryResponseDto m : mrpaRegistryFindAllRecall) {
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
    public ResponseEntity<?> findMrpaRecallById(Authentication authentication, @PathVariable @Parameter(name = "id",
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
        M4dRegistryResponseDto m4DRegistryResponseDto = this.m4dService.convertM4dToM4dResponseDto(authentication, m4d, false, true, false, true);
        return new ResponseEntity<>(m4DRegistryResponseDto, HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_MRPA_RECALL_SAVE_SUMMARY, description = CONTROLLER_MRPA_RECALL_SAVE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @PostMapping()
    public ResponseEntity<?> saveMrpaRecall(Authentication authentication, @RequestBody M4dRevokeRequestDto mrpaRecallRequestDto) throws Exception {
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
        mrpaRegistry.setStatus("ONCANCEL");
        mrpaRecall.setM4d(mrpaRegistry);
        mrpaRecall.setCreateDate(LocalDateTime.now());
        mrpaRecall.setStatus("ONCANCEL");
        M4dRegistryResponseDto m4DRegistryResponseDto = this.m4dService.convertM4dToM4dResponseDto(authentication, mrpaRegistry, false, true, false, true);
        M4dRegistryResponseDto.RecallDto recallDto = new M4dRegistryResponseDto.RecallDto();
        recallDto.setArgument(mrpaRecallRequestDto.getArgument());
        recallDto.setCreateDate(String.valueOf(LocalDateTime.now()));
        m4DRegistryResponseDto.setRecall(recallDto);
        M4dRevokeXml mrpaRecallXml = this.m4dRecallService.convertMrpaRecallToXml(m4DRegistryResponseDto);
        String xmlRecallString = XMLUtil.marshall(mrpaRecallXml, M4dRevokeXml.class);

        M4dRecall m4dRecallSaved;
        try {
            m4dRecallSaved = this.m4dRecallService.saveM4dRecall(mrpaRecall, xmlRecallString);
        } catch (ConstraintViolationException e) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
        }

        String xmlString;
        if (this.m4dService.isBase64(m4dRecallSaved.getM4d().getXmlFile().getContent())) {
            xmlString = new String(Base64.getDecoder().decode(m4dRecallSaved.getM4d().getXmlFile().getContent()));
        } else {
            xmlString = m4dRecallSaved.getM4d().getXmlFile().getContent();
        }
        List<String> tempFileNames = new ArrayList<>();
        String hash;
        String tempSourceFile;
        FileOutputStream fo;
        String filename = m4dRecallSaved.getM4d().getGuid();

        tempSourceFile = "/tmp/" + filename + ".xml";
        tempFileNames.add(tempSourceFile);
        fo = new FileOutputStream(tempSourceFile);
        fo.write(xmlString.getBytes());
        fo.close();
        MultipartFile sourceFile = new CustomMultipartFile(new File(tempSourceFile));
        hash = this.m4dService.getHashXmlM4d(sourceFile);
        for (String fileName : tempFileNames) {
            File f = new File(fileName);
            f.delete();
        }
        M4dFnsDataResponse m4dFnsDataResponse = new M4dFnsDataResponse();
        m4dFnsDataResponse.setGuid(m4dRecallSaved.getM4d().getGuid());
        m4dFnsDataResponse.setId(hash);
        m4dFnsDataResponse.setStatus("ONCANCEL");
        m4dFnsDataResponse.setStatusDateTime(LocalDateTime.now().toString());
        m4dFnsDataResponse.setStartDateTime(m4dRecallSaved.getM4d().getStartDate().toString());
        m4dFnsDataResponse.setEndDateTime(m4dRecallSaved.getM4d().getEndDate().toString());
        ReportCheckStatusM4dDto report = this.m4dService.updateStatusM4dRequestSearch(m4dFnsDataResponse);
        return findMrpaRecallById(authentication, m4dRecallSaved.getId());
    }

    @Operation(summary = CONTROLLER_MRPA_RECALL_UPDATE_SUMMARY, description = CONTROLLER_MRPA_RECALL_UPDATE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @PutMapping()
    public ResponseEntity<?> updateMrpaRecall(Authentication authentication, @RequestBody M4dRevokeRequestDto mrpaRecallRequestDto) throws ParseException {
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
        return findMrpaRecallById(authentication, response.getId());
    }

    @Operation(summary = CONTROLLER_MRPA_RECALL_DELETE_SUMMARY, description = CONTROLLER_MRPA_RECALL_DELETE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @DeleteMapping("{id}")
    public ResponseEntity<?> deleteMrpaRecall(@PathVariable @Parameter(name = "id",
            description = PARAMETER_MRPA_RECALL_ID_DESCRIPTION, example = "1") Long id) {
        if (id == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(ID_IS_NULL), HttpStatus.BAD_REQUEST);
        }
        M4dRecall mrpaRecall = this.m4dRecallService.findById(id);
        if (mrpaRecall == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(String.format("M4d Recall %s", NOT_FOUND)), HttpStatus.NOT_FOUND);
        }
        if (mrpaRecall.getSigFile() != null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_RECALL_SIGN_CANNOT_DELETE), HttpStatus.BAD_REQUEST);
        }
        try {
            this.m4dRecallService.deleteMrpaRecallById(id);
        } catch (ConstraintViolationException e) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
        this.m4dService.updateStatusM4d(mrpaRecall.getM4d().getId(), "ACTIVE");
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
            description = PARAMETER_MRPA_RECALL_ID_DESCRIPTION, example = "1") Long id, @RequestPart("container") MultipartFile container,
                                             @RequestParam(value = "verify", required = false, defaultValue = "true") boolean verify) throws IOException, ParseException {
        if (id == null){
            return new ResponseEntity<>(getCustomErrorJsonFromString(ID_IS_NULL), HttpStatus.BAD_REQUEST);
        }
        if (container == null || container.isEmpty()){
            return new ResponseEntity<>(getCustomErrorJsonFromString(ID_IS_NULL), HttpStatus.BAD_REQUEST);
        }
        //String bearerToken = getTokenValue(authentication);
        String extension = FilenameUtils.getExtension(container.getOriginalFilename());
        if(!Objects.equals(extension, "dscp")) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_REGISTRY_WRONG_FORMAT), HttpStatus.BAD_REQUEST);
        }
        Long sigFileId = this.m4dRecallService.uploadSig(authentication, id, container, verify);
        M4dRecall mrpaRecall = this.m4dRecallService.findById(id);
//        if (mrpaRecall==null) {
//            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_RECALL_IS_NULL), HttpStatus.BAD_REQUEST);
//        }
//        if (mrpaRecall.getSigFile()!=null) {
//            return new ResponseEntity<>(getCustomErrorJsonFromString("Заявление на отзыв МЧД уже подписано"), HttpStatus.BAD_REQUEST);
//        }
//        List<String> tempFileNames = new ArrayList<>();
//        String tempFileName;
//        FileOutputStream fo;
//        ReportDto reportDto;
//        ByteArrayResource res = null;
//        try {
//            tempFileName = "/tmp/" + container.getOriginalFilename();
//            tempFileNames.add(tempFileName);
//            fo = new FileOutputStream(tempFileName);
//            fo.write(container.getBytes());
//            fo.close();
//            ObjectMapper objectMapper = new ObjectMapper();
//            Map<String, String> resultMap = this.synapiClientService.getReportContainer(tempFileName, bearerToken);
//            reportDto = objectMapper.readValue(resultMap.get("signCheck"), ReportDto.class);
//            assert reportDto != null;
//            reportDto.getResults().stream()
//                    .flatMap(result -> result.getVerificationResults().stream())
//                    .findFirst()
//                    .ifPresent(verificationResult -> {
//                        boolean isResultTrue = verificationResult.isResult();
//                        System.out.println("Result value: " + isResultTrue);
//                        if (!isResultTrue) {
//                            throw new BadRequestException(MRPA_REGISTRY_NOT_VERIFY_REPORT);
//                        }
//                    });
//            res = this.synapiClientService.getPartContainer(tempFileName, "sourceFile", bearerToken);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        for (String fileName : tempFileNames) {
//            File f = new File(fileName);
//            f.delete();
//        }
//        assert res != null;
//        byte[] resource = res.getByteArray();
//        M4d mrpaRegistryFind;
//        try {
//            mrpaRegistryFind = this.m4dService.findById(mrpaRecall.getM4d().getId());
//        } catch (ConstraintViolationException e) {
//            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
//        }
//        if (mrpaRegistryFind == null) {
//            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_REGISTRY_IS_NULL), HttpStatus.BAD_REQUEST);
//        }
//        M4dRegistryResponseDto m4DRegistryResponseDto = this.m4dService.convertM4dToM4dResponseDto(authentication, mrpaRegistryFind, false, false, false, true);
//        String normalizedResource = new String(resource, StandardCharsets.UTF_8).replaceAll("\\s+", "");
//        String normalizedXmlString = m4DRegistryResponseDto.recall.getXmlFile().getContent().replaceAll("\\s+", "");
//        if (!StringUtils.difference(normalizedResource, normalizedXmlString).isEmpty()) {
//            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_REGISTRY_NOT_VERIFY_CONTENT), HttpStatus.BAD_REQUEST);
//        }
//        M4dFile m4dFile = new M4dFile();
//        m4dFile.setFileMimeType(FileMimeTypeEnum.fromValue(container.getContentType()));
//        m4dFile.setFileLogicalType(FileLogicalTypeEnum.CONTAINER);
//        m4dFile.setFilename(container.getOriginalFilename());
//        String base64Content = Base64.getEncoder().encodeToString(container.getBytes());
//        m4dFile.setContent(base64Content);
//        M4dFile existsM4dFile;
//        M4dFile m4dFileSaved;
//        Long fileId;
//        try {
//            existsM4dFile = this.m4dFileService.findM4dFileByContent(Base64.getEncoder().encodeToString(m4dFile.getContent().getBytes()));
//        } catch (ConstraintViolationException e) {
//            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
//        }
//        if (existsM4dFile == null){
//            try {
//                m4dFileSaved = this.m4dFileService.saveM4dFile(m4dFile);
//            } catch (ConstraintViolationException e) {
//                return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
//            }
//            fileId = m4dFileSaved.getId();
//        } else {
//            fileId = existsM4dFile.getId();
//        }
//        try {
//            this.m4dRecallService.updateMrpaRecallById(id, fileId);
//        } catch (ConstraintViolationException e) {
//            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
//        }
        // TODO: @helix82 Отправляем архив с xml-файлом отзыва мчд и подписью на РР МЧД для отзыва
        this.m4dRecallService.updateStatusMrpaRecallById(id, "REVOKED");
        this.m4dService.updateStatusM4d(mrpaRecall.getM4d().getId(), "REVOKED");
        return findMrpaRecallById(authentication, id);
    }

    @Operation(summary = CONTROLLER_MRPA_RECALL_AS_ARCHIVE_SUMMARY, description = CONTROLLER_MRPA_RECALL_AS_ARCHIVE_SUMMARY)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @GetMapping(value = "/{id}/archive", produces = "application/zip")
    public ResponseEntity<?> archive(Authentication authentication, HttpServletResponse response,
                                     @PathVariable @Parameter(name = "id", description = PARAMETER_MRPA_RECALL_ID_DESCRIPTION, example = "1") Long id) throws IOException, ParseException {
        if (id == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(ID_IS_NULL), HttpStatus.BAD_REQUEST);
        }
        M4dRecall m4dRecall;
        try {
            m4dRecall = this.m4dRecallService.findById(id);
        } catch (ConstraintViolationException e) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
        if (m4dRecall == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_RECALL_IS_NULL), HttpStatus.NOT_FOUND);
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

        String filename = m4dRecall.getXmlFile().getFilename();
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            filename = filename.substring(0, lastDotIndex);
        }
        if (m4dRecall.getSigFile()==null){
            M4dRegistryResponseDto m4dResponseDto = this.m4dService.convertM4dToM4dResponseDto(authentication, m4d, false, true, false, true);
            String xmlString = m4dResponseDto.recall.getXmlFile().getContent();
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
            ByteArrayResource resp = this.m4dService.createArchiveM4dSig(authentication, m4dRecall.getSigFile().getId(), filename);
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
