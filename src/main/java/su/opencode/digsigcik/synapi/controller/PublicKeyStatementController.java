package su.opencode.digsigcik.synapi.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import su.opencode.digsigcik.synapi.annotation.MchdExceptionHandler;
import su.opencode.digsigcik.synapi.dto.PublicKeyStatementDto;
import su.opencode.digsigcik.synapi.enums.StatusStatementEnum;
import su.opencode.digsigcik.synapi.enums.TypeStatementEnum;
import su.opencode.digsigcik.synapi.exception.BadRequestException;
import su.opencode.digsigcik.synapi.model.PublicKeyStatement;
import su.opencode.digsigcik.synapi.model.PublicKeyVerificationCenter;
import su.opencode.digsigcik.synapi.service.PublicKeyStatementService;
import su.opencode.digsigcik.synapi.service.PublicKeyVerificationCenterService;

import javax.validation.ConstraintViolationException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static su.opencode.digsigcik.synapi.util.Constant.ID_IS_NULL;
import static su.opencode.digsigcik.synapi.util.Constants.*;

@RestController
@MchdExceptionHandler
@RequestMapping("/api/v1/statement")
@AllArgsConstructor
public class PublicKeyStatementController {
    private final PublicKeyStatementService service;
    private final PublicKeyVerificationCenterService publicKeyVerificationCenterService;
    private final ModelMapper modelMapper;

    @Operation(summary = CONTROLLER_PUBLIC_KEY_STATEMENT_FIND_ALL_SUMMARY, description = CONTROLLER_PUBLIC_KEY_STATEMENT_FIND_ALL_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @GetMapping()
    public ResponseEntity<?> findAllPublicKeyStatement() {
        List<PublicKeyStatement> response;
        try {
            response = service.findAllPublicKeyStatement();
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(PUBLIC_KEY_STATEMENT_IS_NULL);
        }
        return new ResponseEntity<>(response.stream().map(this::convertToDto).collect(Collectors.toList()), HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_PUBLIC_KEY_STATEMENT_SAVE_SUMMARY, description = CONTROLLER_PUBLIC_KEY_STATEMENT_SAVE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @PostMapping("save")
    public ResponseEntity<?> savePublicKeyStatement(@RequestBody PublicKeyStatementDto publicKeyStatementDto) {
        PublicKeyStatement publicKeyStatement = convertToEntity(publicKeyStatementDto);
        publicKeyStatement.setStatus(StatusStatementEnum.IN_PROGRESS);
        publicKeyStatement.setType(TypeStatementEnum.RELEASE);
        PublicKeyStatement response;
        try {
            response = service.savePublicKeyStatement(publicKeyStatement);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(PUBLIC_KEY_STATEMENT_IS_NULL);
        }
        return new ResponseEntity<>(convertToDto(response), HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_PUBLIC_KEY_STATEMENT_FIND_BY_ID_SUMMARY, description = CONTROLLER_PUBLIC_KEY_STATEMENT_FIND_BY_ID_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> findByPublicKeyStatementId(@PathVariable @Parameter(name = "id",
            description = PARAMETER_PUBLIC_KEY_STATEMENT_ID_DESCRIPTION, example = "1" ) Long id) {
        PublicKeyStatement response;
        try {
            response = service.findByPublicKeyStatementId(id);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(PUBLIC_KEY_STATEMENT_IS_NULL);
        }
        return new ResponseEntity<>(convertToDto(response), HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_PUBLIC_KEY_STATEMENT_UPDATE_SUMMARY, description = CONTROLLER_PUBLIC_KEY_STATEMENT_UPDATE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @PutMapping("update")
    public ResponseEntity<?> updatePublicKeyStatement(@RequestBody PublicKeyStatementDto publicKeyStatementDto) {
        PublicKeyStatement publicKeyStatement = convertToEntity(publicKeyStatementDto);
        PublicKeyStatement response;
        try {
            response = service.updatePublicKeyStatement(publicKeyStatement);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(PUBLIC_KEY_STATEMENT_IS_NULL);
        }
        return new ResponseEntity<>(convertToDto(response), HttpStatus.OK);
    }


    @Operation(summary = CONTROLLER_PUBLIC_KEY_STATEMENT_DELETE_SUMMARY, description = CONTROLLER_PUBLIC_KEY_STATEMENT_DELETE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @DeleteMapping("delete/{id}")
    public ResponseEntity<?> deletePublicKeyStatement(@PathVariable @Parameter(name = "id",
            description = PARAMETER_PUBLIC_KEY_STATEMENT_ID_DESCRIPTION, example = "1" ) Long id) {
        try {
            service.deletePublicKeyStatement(id);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(ID_IS_NULL);
        }
        Map<String, String> body = new HashMap<>();
        body.put("result", "true");

        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_PUBLIC_KEY_VERIFICATION_CENTER_DOWNLOAD_TEMPLATE_RELEASE_SUMMARY, description = CONTROLLER_PUBLIC_KEY_VERIFICATION_CENTER_DOWNLOAD_TEMPLATE_RELEASE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @GetMapping(value = "/download_template_release/{id}", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<?> downloadTemplateRelease(@PathVariable @Parameter(name = "id",
            description = PARAMETER_PUBLIC_KEY_STATEMENT_ID_DESCRIPTION, example = "1" ) Long id) {
                PublicKeyStatement pkStatement;
        try {
            pkStatement = service.findByPublicKeyStatementId(id);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(PUBLIC_KEY_STATEMENT_IS_NULL);
        }
        PublicKeyVerificationCenter downloadTempl;
        try {
            downloadTempl = publicKeyVerificationCenterService.findByPublicKeyVerificationCenterId(pkStatement.getId());
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(PUBLIC_KEY_VERIFICATION_CENTER_IS_NULL);
        }
        if(downloadTempl.getTemplateRelease() == null) {
            return new ResponseEntity<>(TEMPLATE_RELEASE_IS_NULL, HttpStatus.NOT_FOUND);
        }
        var resource = new ByteArrayResource(downloadTempl.getTemplateRelease());
        var contentType = MediaType.valueOf(downloadTempl.getContentTypeRelease());
        var filename = URLEncoder.encode(STATEMENT_RELEASE_FILE, StandardCharsets.UTF_8);
        var disposition = ContentDisposition.attachment()
                .filename(filename)
                .build();
        return ResponseEntity.ok()
                .contentLength(resource.contentLength())
                .contentType(contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(resource);
    }

    @Operation(summary = CONTROLLER_PUBLIC_KEY_VERIFICATION_CENTER_DOWNLOAD_TEMPLATE_RECALL_SUMMARY, description = CONTROLLER_PUBLIC_KEY_VERIFICATION_CENTER_DOWNLOAD_TEMPLATE_RECALL_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @GetMapping(value = "/download_template_recall/{id}", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<?> downloadTemplateRecall(@PathVariable @Parameter(name = "id",
            description = PARAMETER_PUBLIC_KEY_STATEMENT_ID_DESCRIPTION, example = "1" ) Long id) {
        PublicKeyStatement pkStatement;
        try {
            pkStatement = service.findByPublicKeyStatementId(id);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(PUBLIC_KEY_STATEMENT_IS_NULL);
        }
        PublicKeyVerificationCenter downloadTempl;
        try {
            downloadTempl = publicKeyVerificationCenterService.findByPublicKeyVerificationCenterId(pkStatement.getId());
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(PUBLIC_KEY_VERIFICATION_CENTER_IS_NULL);
        }
        if(downloadTempl.getTemplateRecall() == null) {
            return new ResponseEntity<>(TEMPLATE_RECALL_IS_NULL, HttpStatus.NOT_FOUND);
        }
        var resource = new ByteArrayResource(downloadTempl.getTemplateRecall());
        var contentType = MediaType.valueOf(downloadTempl.getContentTypeRecall());
        var filename = URLEncoder.encode(STATEMENT_RECALL_FILE, StandardCharsets.UTF_8);
        var disposition = ContentDisposition.attachment()
                .filename(filename)
                .build();
        return ResponseEntity.ok()
                .contentLength(resource.contentLength())
                .contentType(contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(resource);
    }

    @Operation(summary = CONTROLLER_PUBLIC_KEY_STATEMENT_CHECK_PROCESSED_SUMMARY, description = CONTROLLER_PUBLIC_KEY_STATEMENT_CHECK_PROCESSED_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @PutMapping("check_processed/{id}")
    public ResponseEntity<?> checkPublicKeyStatementProcessed(@PathVariable @Parameter(name = "id",
            description = PARAMETER_PUBLIC_KEY_STATEMENT_ID_DESCRIPTION, example = "1" ) Long id) {
        PublicKeyStatement pkStatement;
        try {
            pkStatement = service.findByPublicKeyStatementId(id);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(PUBLIC_KEY_STATEMENT_IS_NULL);
        }
        pkStatement.setStatus(StatusStatementEnum.PROCESSED);
        PublicKeyStatement response;
        try {
            response = service.updatePublicKeyStatement(pkStatement);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(PUBLIC_KEY_STATEMENT_IS_NULL);
        }
        return new ResponseEntity<>(convertToDto(response), HttpStatus.OK);
    }

    private PublicKeyStatementDto convertToDto(PublicKeyStatement publicKeyStatement){
        PublicKeyStatementDto publicKeyStatementDto = modelMapper.map(publicKeyStatement, PublicKeyStatementDto.class);
        PublicKeyVerificationCenter publicKeyVerificationCenter = publicKeyVerificationCenterService.findByPublicKeyVerificationCenterId(Long.parseLong(publicKeyStatement.getVerificationCenterId()));
        publicKeyStatementDto.setVerificationCenter(publicKeyVerificationCenter.getName());
        return publicKeyStatementDto;
    }

    private PublicKeyStatement convertToEntity(PublicKeyStatementDto publicKeyStatementDto){
        PublicKeyStatement publicKeyStatement = modelMapper.map(publicKeyStatementDto, PublicKeyStatement.class);
        return publicKeyStatement;
    }
}
