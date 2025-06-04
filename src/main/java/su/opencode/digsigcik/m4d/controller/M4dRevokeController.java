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
import su.opencode.digsigcik.m4d.dto.response.M4dRevokeResponseInfo;
import su.opencode.digsigcik.m4d.enums.StatusM4dRevokeTypeEnum;
import su.opencode.digsigcik.m4d.enums.StatusM4dTypeEnum;
import su.opencode.digsigcik.m4d.model.M4d;
import su.opencode.digsigcik.m4d.model.M4dFile;
import su.opencode.digsigcik.m4d.model.M4dRecall;
import su.opencode.digsigcik.m4d.model.M4dRecallConfirm;
import su.opencode.digsigcik.m4d.service.*;
import su.opencode.digsigcik.m4d.util.DateUtils;
import su.opencode.digsigcik.m4d.util.XMLUtil;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.text.ParseException;
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
    private final M4dRecallConfirmService m4dRecallConfirmService;
    private final M4dFileService m4dFileService;
    private final ContainerService containerService;

    public M4dRevokeController(ModelMapper modelMapper, M4dService m4dService, M4dRecallService m4dRecallService, M4dRecallConfirmService m4dRecallConfirmService, M4dFileService m4dFileService, ContainerService containerService) {
        this.modelMapper = modelMapper;
        this.m4dService = m4dService;
        this.m4dRecallService = m4dRecallService;
        this.m4dRecallConfirmService = m4dRecallConfirmService;
        this.m4dFileService = m4dFileService;
        this.containerService = containerService;
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
        mrpaRecall.setM4d(mrpaRegistry);
        mrpaRecall.setCreateDate(DateUtils.convertStringToLocalDateTime(DateUtils.getCurrentDate()));
        mrpaRecall.setStatus(StatusM4dRevokeTypeEnum.NEW.toString());
        M4dRegistryResponseDto m4DRegistryResponseDto = this.m4dService.convertM4dToM4dResponseDto(authentication, mrpaRegistry, false, true, false, true);
        M4dRegistryResponseDto.RecallDto recallDto = new M4dRegistryResponseDto.RecallDto();
        recallDto.setArgument(mrpaRecallRequestDto.getArgument());
        recallDto.setCreateDate(DateUtils.getCurrentDate());
        m4DRegistryResponseDto.setRecall(recallDto);
        M4dRevokeXml mrpaRecallXml = this.m4dRecallService.convertMrpaRecallToXml(m4DRegistryResponseDto);
        String xmlRecallString = XMLUtil.marshall(mrpaRecallXml, M4dRevokeXml.class);

        M4dRecall m4dRecallSaved;
        try {
            m4dRecallSaved = this.m4dRecallService.saveM4dRecall(authentication, mrpaRecall, xmlRecallString);
        } catch (ConstraintViolationException e) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
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
            description = PARAMETER_MRPA_RECALL_ID_DESCRIPTION, example = "1") Long id, @RequestPart("container") MultipartFile signature) throws Exception {
        if (id == null){
            return new ResponseEntity<>(getCustomErrorJsonFromString(ID_IS_NULL), HttpStatus.BAD_REQUEST);
        }
        if (signature == null || signature.isEmpty()){
            return new ResponseEntity<>(getCustomErrorJsonFromString("Signature is null"), HttpStatus.BAD_REQUEST);
        }
        String extension = FilenameUtils.getExtension(signature.getOriginalFilename());
        if(!Objects.equals(extension, "sig") && !Objects.equals(extension, "dscp")) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_REGISTRY_WRONG_FORMAT), HttpStatus.BAD_REQUEST);
        }
        M4dRecall mrpaRecall = this.m4dRecallService.findById(id);
        if (Objects.equals(extension, "dscp")) {
            M4dFile m4dFile = this.m4dFileService.findM4dFileById(mrpaRecall.getXmlFile().getId());
            MultipartFile sigFile = this.containerService.getSignatureFromContainer(authentication, signature, m4dFile.getFilename());
            Long sigFileId = this.m4dRecallService.uploadSig(authentication, id, sigFile);
        } else { Long sigFileId = this.m4dRecallService.uploadSig(authentication, id, signature); }
        // TODO: @helix82 Отправляем архив с xml-файлом отзыва мчд и подписью на РР МЧД для отзыва
        this.m4dRecallService.updateStatusMrpaRecallById(id, StatusM4dRevokeTypeEnum.ACTIVE.toString());
        this.m4dService.updateStatusM4d(mrpaRecall.getM4d().getId(), StatusM4dTypeEnum.REVOKED.toString());
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
        M4dRecallConfirm m4dRecallConfirm;
        try {
            m4dRecallConfirm = this.m4dRecallConfirmService.findM4dRecallConfirmByM4dRecallId(m4dRecall.getId());
        } catch (ConstraintViolationException e) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
        }

        String filenameSource = m4dRecall.getXmlFile().getFilename();
        int lastDotIndex = filenameSource.lastIndexOf('.');
        if (lastDotIndex > 0) {
            filenameSource = filenameSource.substring(0, lastDotIndex);
        }
        if (m4dRecall.getSigFile()==null  && m4dRecallConfirm==null){
            M4dRegistryResponseDto m4dResponseDto = this.m4dService.convertM4dToM4dResponseDto(authentication, m4d, false, true, false, true);
            String xmlString = m4dResponseDto.recall.getXmlFile().getContent();
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
            longList.add(m4dRecall.getXmlFile().getId());
            if (m4dRecall.getSigFile()!=null) { longList.add(m4dRecall.getSigFile().getId()); }
            if (m4dRecallConfirm!=null) {
                if (m4dRecallConfirm.getXmlFileRegisteredPep()!=null) { longList.add(m4dRecallConfirm.getXmlFileRegisteredPep().getId()); }
                if (m4dRecallConfirm.getSigFileRegisteredPep()!=null) { longList.add(m4dRecallConfirm.getSigFileRegisteredPep().getId()); }
            }
             ByteArrayResource resp = this.m4dFileService.createArchive(authentication, filenameSource, longList);
            if (resp == null) {
                return new ResponseEntity<>(getCustomErrorJsonFromString("Ошибка создания архива"), HttpStatus.NOT_FOUND);
            }
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

    @Operation(summary = CONTROLLER_MRPA_RECALL_UPLOAD_RESULT_SENDING_TO_RR_DESCRIPTION, description = CONTROLLER_MRPA_RECALL_UPLOAD_RESULT_SENDING_TO_RR_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @PutMapping(value = "/upload-result-sending-to-rr", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> uploadResultSendM4dRevokeToRR(@RequestBody M4dRevokeResponseInfo m4dRevokeResponseInfo) {
        String error;
        try {
            error = m4dRecallService.uploadResultSendM4dRevokeToRR(m4dRevokeResponseInfo);
        } catch (ConstraintViolationException e) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
        if ( error != null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(error), HttpStatus.BAD_REQUEST);
        }
        Map<String, String> body = new HashMap<>();
        body.put("result", "Результат отправки отзыва МЧД на регистрацию в РР МЧД обработан.");
        return new ResponseEntity<>(body, HttpStatus.OK);
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
