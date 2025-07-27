package su.opencode.digsigcik.m4d.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import su.opencode.digsigcik.m4d.dto.M4dRegistryResponseDto;
import su.opencode.digsigcik.m4d.dto.M4dRevokeRequestDto;
import su.opencode.digsigcik.m4d.dto.M4dRevokeXml;
import su.opencode.digsigcik.m4d.dto.response.M4dRevokeResponseInfo;
import su.opencode.digsigcik.m4d.enums.StatusM4dRevokeTypeEnum;
import su.opencode.digsigcik.m4d.model.M4d;
import su.opencode.digsigcik.m4d.model.M4dFile;
import su.opencode.digsigcik.m4d.model.M4dRecall;
import su.opencode.digsigcik.m4d.model.M4dRecallConfirm;
import su.opencode.digsigcik.m4d.service.*;
import su.opencode.digsigcik.m4d.util.ConvertUtils;
import su.opencode.digsigcik.m4d.util.DateUtils;
import su.opencode.digsigcik.m4d.util.XMLUtil;
import su.opencode.digsigcik.specification.SpecificationRequest;
import su.opencode.digsigcik.specification.annotation.EnablePagination;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.nio.file.Files;
import java.text.ParseException;
import java.util.*;

import static su.opencode.digsigcik.m4d.util.Constants.*;


@Slf4j
@RestController
@EnablePagination
@RequiredArgsConstructor
@RequestMapping("/api/v1/m4d/revoke")
@Tag(name = "M4d Revoke Controller")
public class M4dRevokeController {

    private final ModelMapper modelMapper;
    private final M4dService m4dService;
    private final M4dRecallService m4dRecallService;
    private final M4dRecallConfirmService m4dRecallConfirmService;
    private final M4dFileService m4dFileService;
    private final ContainerService containerService;

    @Operation(summary = CONTROLLER_MRPA_RECALL_FIND_ALL_SUMMARY,
            description = CONTROLLER_MRPA_RECALL_FIND_ALL_DESCRIPTION,
            deprecated = true)
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

    @Operation(summary = CONTROLLER_MRPA_RECALL_FIND_ALL_PAGINATED_SUMMARY)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @GetMapping(value = "/paginated", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> findAllMrpaRecallPaginated(
            @Parameter(in = ParameterIn.QUERY, style = ParameterStyle.DEEPOBJECT, explode = Explode.TRUE, required = true)
            @ParameterObject SpecificationRequest request,
            Authentication authentication
    ) {
        try {
            var response = m4dRecallService.findAllMrpaRegistriesWithRecall(authentication, request);

            for (M4dRegistryResponseDto m : response.getData()) {
                m.credentials = this.m4dService.getCredentialByMrpaRegistryId(m.getId());
                m.setStatus(this.m4dService.getStatusM4d(m.getId()));
            }

            return ResponseEntity.ok(response);

        } catch (ConstraintViolationException | ParseException e) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
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
            m4dRecall = this.m4dRecallService.findById(id);
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
            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_NOT_FOUND), HttpStatus.NOT_FOUND);
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
            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_ID_IS_NULL), HttpStatus.BAD_REQUEST);
        }
        M4dRecall m4dRecall = this.m4dRecallService.findMrpaRecallByM4dId(mrpaRegistryId);
        if (m4dRecall != null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_RECALL_IS_EXISTS), HttpStatus.BAD_REQUEST);
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
        M4dRecall existsMrpaRecall = this.m4dRecallService.findById(mrpaRecall.getId());
        if (existsMrpaRecall == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_RECALL_IS_NULL), HttpStatus.NOT_FOUND);
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
            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_RECALL_IS_NULL), HttpStatus.NOT_FOUND);
        }
        if (!mrpaRecall.getStatus().equals("NEW")) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_RECALL_NOT_DELETE), HttpStatus.BAD_REQUEST);
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

    // TODO: me paginated (Данияр)
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
            return new ResponseEntity<>(getCustomErrorJsonFromString(SIG_IS_NULL), HttpStatus.BAD_REQUEST);
        }
        String extension = FilenameUtils.getExtension(signature.getOriginalFilename());
        if(!Objects.equals(extension, "sig") && !Objects.equals(extension, "dscp")) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_REGISTRY_WRONG_FORMAT), HttpStatus.BAD_REQUEST);
        }
        M4dRecall mrpaRecall = this.m4dRecallService.findById(id);
        String res;
        if (Objects.equals(extension, "dscp")) {
            M4dFile m4dFile = this.m4dFileService.findM4dFileById(mrpaRecall.getXmlFile().getId());
            MultipartFile sigFile = this.containerService.getSignatureFromContainer(authentication, signature, m4dFile.getFilename());
            res = this.m4dRecallService.uploadSig(authentication, id, sigFile);
        } else { res = this.m4dRecallService.uploadSig(authentication, id, signature); }
        if (res != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Request-ID", res);
            return new ResponseEntity<>(findMrpaRecallById(authentication, id).getBody(), headers, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(findMrpaRecallById(authentication, id).getBody(), HttpStatus.OK);
        }
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
            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_NOT_FOUND), HttpStatus.NOT_FOUND);
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

    @Operation(summary = CONTROLLER_MRPA_RECALL_UPLOAD_RESULT_SENDING_TO_RR_DESCRIPTION, description = CONTROLLER_MRPA_RECALL_UPLOAD_RESULT_SENDING_TO_RR_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @PostMapping(value = "/upload-result-sending-to-rr")
    public ResponseEntity<?> uploadResultSendM4dRevokeToRR(@RequestBody String json) throws JsonProcessingException {
        M4dRevokeResponseInfo m4dRevokeResponseInfo = ConvertUtils.convertStringToClass(json, M4dRevokeResponseInfo.class);
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
        body.put("result", MRPA_RECALL_UPLOAD_RESULT_SEND_RR_MRPA);
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_MRPA_RECALL_UPLOAD_RESULT_SENDING_TO_RR_BY_MESSAGE_ID_DESCRIPTION, description = CONTROLLER_MRPA_RECALL_UPLOAD_RESULT_SENDING_TO_RR_DESCRIPTION,
            deprecated = true)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @PutMapping(value = "/upload-result-sending-to-rr/{messageId}", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> uploadResultSendM4dRevokeToRRByMessageId(
            @PathVariable @Parameter(name = "messageId", description = PARAMETER_MESSAGE_ID_DESCRIPTION, example = "1") String messageId) {
        if (messageId == null || messageId.isEmpty()) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(MESSAGE_ID_IS_NULL), HttpStatus.BAD_REQUEST);
        }
        String error = this.m4dRecallService.uploadResultSendM4dRevokeToRRByMessageId(messageId);
        if ( error != null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(error), HttpStatus.BAD_REQUEST);
        }
        Map<String, String> body = new HashMap<>();
        body.put("result", MRPA_RECALL_UPLOAD_RESULT_SEND_RR_MRPA);
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_MRPA_RECALL_SEND_REGISTER_IN_PVV_SUMMARY, description = CONTROLLER_MRPA_RECALL_SEND_REGISTER_IN_PVV_SUMMARY)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "404", description = API_RESPONSE_CODE_404_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @PutMapping(value = "send-register-in-rr/{id}", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> sendRegisterInRr(Authentication authentication, @PathVariable @Parameter(name = "id",
            description = PARAMETER_MRPA_RECALL_ID_DESCRIPTION, example = "1") Long id) throws Exception {
        if (id == null){
            return new ResponseEntity<>(getCustomErrorJsonFromString(ID_IS_NULL), HttpStatus.BAD_REQUEST);
        }
        String res = this.m4dRecallService.sendRegisterInRr(authentication, id);
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Request-ID", res);
        return new ResponseEntity<>(findMrpaRecallById(authentication, id).getBody(), headers, HttpStatus.OK);
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
        return modelMapper.map(mrpaRecallRequestDto, M4dRecall.class);
    }
}
