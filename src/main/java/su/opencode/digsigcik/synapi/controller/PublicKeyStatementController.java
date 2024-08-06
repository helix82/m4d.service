package su.opencode.digsigcik.synapi.controller;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import su.opencode.digsigcik.synapi.annotation.MchdExceptionHandler;
import su.opencode.digsigcik.synapi.dto.PublicKeyStatementDto;
import su.opencode.digsigcik.synapi.dto.PublicKeyStatementRequestDto;
import su.opencode.digsigcik.synapi.dto.PublicKeyStatementUpdateRequestDto;
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
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.notFound;
import static su.opencode.digsigcik.synapi.util.Constants.*;
import static su.opencode.digsigcik.synapi.util.JwtTokenUtils.*;

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
        Instant instant = Instant.now();
        int nano = instant.getNano();
        publicKeyStatement.setDtCreate(instant.minusNanos(nano).toString());
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
            description = PARAMETER_PUBLIC_KEY_STATEMENT_ID_DESCRIPTION, example = "1") Long id) {
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
            description = PARAMETER_PUBLIC_KEY_STATEMENT_ID_DESCRIPTION, example = "1") Long id) {
        try {
            service.deletePublicKeyStatement(id);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(ID_IS_NULL);
        }
        Map<String, String> body = new HashMap<>();
        body.put("result", "true");

        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_PUBLIC_KEY_STATEMENT_DOWNLOAD_TEMPLATE_SUMMARY, description = CONTROLLER_PUBLIC_KEY_STATEMENT_DOWNLOAD_TEMPLATE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @GetMapping(value = "/download/{id}", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<?> download(@PathVariable @Parameter(name = "id",
            description = PARAMETER_PUBLIC_KEY_STATEMENT_ID_DESCRIPTION, example = "1") Long id) {
        PublicKeyStatement pkStatement;
        try {
            pkStatement = service.findByPublicKeyStatementId(id);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(PUBLIC_KEY_STATEMENT_IS_NULL);
        }
        PublicKeyVerificationCenter downloadTemplate;
        try {
            downloadTemplate = publicKeyVerificationCenterService.findByPublicKeyVerificationCenterId(Long.parseLong(pkStatement.getVerificationCenterId()));
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(PUBLIC_KEY_VERIFICATION_CENTER_IS_NULL);
        }
        ByteArrayResource resource = null;
        MediaType contentType = null;
        String filename = "";
        if (pkStatement.getType().equals(TypeStatementEnum.RELEASE)) {
            if (downloadTemplate.getTemplateRelease() == null) {
                return new ResponseEntity<>(TEMPLATE_RELEASE_IS_NULL, HttpStatus.NOT_FOUND);
            }
            resource = new ByteArrayResource(downloadTemplate.getTemplateRelease());
            contentType = MediaType.valueOf(downloadTemplate.getContentTypeRelease());
            filename = URLEncoder.encode(STATEMENT_RELEASE_FILE, StandardCharsets.UTF_8);
        } else if (pkStatement.getType().equals(TypeStatementEnum.RECALL)) {
            if (downloadTemplate.getTemplateRelease() == null) {
                return new ResponseEntity<>(TEMPLATE_RECALL_IS_NULL, HttpStatus.NOT_FOUND);
            }
            resource = new ByteArrayResource(downloadTemplate.getTemplateRecall());
            contentType = MediaType.valueOf(downloadTemplate.getContentTypeRecall());
            filename = URLEncoder.encode(STATEMENT_RECALL_FILE, StandardCharsets.UTF_8);
        }

        if (resource == null || filename == "" || contentType == null) {
            return new ResponseEntity<>(TEMPLATE_IS_NULL, HttpStatus.NOT_FOUND);
        }

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
            description = PARAMETER_PUBLIC_KEY_STATEMENT_ID_DESCRIPTION, example = "1") Long id) {
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

    private PublicKeyStatementDto convertToDto(PublicKeyStatement publicKeyStatement) {
        PublicKeyStatementDto publicKeyStatementDto = modelMapper.map(publicKeyStatement, PublicKeyStatementDto.class);
        PublicKeyVerificationCenter publicKeyVerificationCenter = publicKeyVerificationCenterService.findByPublicKeyVerificationCenterId(Long.parseLong(publicKeyStatement.getVerificationCenterId()));
        publicKeyStatementDto.setVerificationCenter(publicKeyVerificationCenter.getName());
        return publicKeyStatementDto;
    }

    private PublicKeyStatement convertToEntity(PublicKeyStatementDto publicKeyStatementDto) {
        PublicKeyStatement publicKeyStatement = modelMapper.map(publicKeyStatementDto, PublicKeyStatement.class);
        return publicKeyStatement;
    }

    @Operation(summary = CONTROLLER_PUBLIC_KEY_STATEMENT_FIND_ALL_FOR_USER_SUMMARY, description = CONTROLLER_PUBLIC_KEY_STATEMENT_FIND_ALL_FOR_USER_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = API_RESPONSE_CODE_204_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer token for authorization", required = true, dataType = "string", paramType = "header")
    })
    @GetMapping("/me")
    public ResponseEntity<?> findAllPublicKeyStatementForUser(Authentication authentication) {
        List<PublicKeyStatementDto> response =
                service.findAllPublicKeyStatementByLogin(getAuthenticationEmail(authentication))
                        .stream()
                        .map(this::convertToDto)
                        .collect(Collectors.toList());

        return response.isEmpty() ?
                noContent().build() :
                new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_PUBLIC_KEY_STATEMENT_SAVE_FOR_USER_SUMMARY, description = CONTROLLER_PUBLIC_KEY_STATEMENT_SAVE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = API_RESPONSE_CODE_201_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer token for authorization", required = true, dataType = "string", paramType = "header")
    })
    @PostMapping("me")
    public ResponseEntity<?> savePublicKeyStatementForUser(@RequestBody PublicKeyStatementRequestDto dto,
                                                           Authentication authentication) {
        PublicKeyStatementDto statement = service.createPublicKeyStatementForUser(dto, getAuthenticationEmail(authentication));
        setVerificationCenter(statement, Long.valueOf(statement.getVerificationCenterId()));
        return new ResponseEntity<>(statement, HttpStatus.CREATED);
    }

    @Operation(summary = CONTROLLER_PUBLIC_KEY_STATEMENT_UPDATE_FOR_USER_SUMMARY, description = CONTROLLER_PUBLIC_KEY_STATEMENT_UPDATE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer token for authorization", required = true, dataType = "string", paramType = "header")
    })
    @PutMapping("me")
    public ResponseEntity<?> updatePublicKeyStatementForUser(@RequestBody PublicKeyStatementUpdateRequestDto dto,
                                                             Authentication authentication) {
        PublicKeyStatementDto statement = service.updatePublicKeyStatementForUserById(dto, dto.getId(), getAuthenticationEmail(authentication));
        setVerificationCenter(statement, Long.valueOf(statement.getVerificationCenterId()));
        return new ResponseEntity<>(statement, HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_PUBLIC_KEY_STATEMENT_DELETE_FOR_USER_SUMMARY, description = CONTROLLER_PUBLIC_KEY_STATEMENT_DELETE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer token for authorization", required = true, dataType = "string", paramType = "header")
    })
    @DeleteMapping("me/{id}")
    public ResponseEntity<?> deletePublicKeyStatementForUser(@PathVariable @Parameter(name = "id",
            description = PARAMETER_PUBLIC_KEY_STATEMENT_ID_DESCRIPTION, example = "1") Long id, Authentication authentication) {
        Map<String, String> body = new HashMap<>();
        body.put("result", "true");

        return service.deletePublicKeyStatementForUserById(id, getAuthenticationEmail(authentication)) ?
                new ResponseEntity<>(body, HttpStatus.OK) :
                notFound().build();
    }

    @Operation(summary = CONTROLLER_PUBLIC_KEY_STATEMENT_FIND_BY_ID_FOR_USER_SUMMARY, description = CONTROLLER_PUBLIC_KEY_STATEMENT_FIND_BY_ID_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer token for authorization", required = true, dataType = "string", paramType = "header")
    })
    @GetMapping("me/{id}")
    public ResponseEntity<?> getPublicKeyStatementForUser(@PathVariable @Parameter(name = "id",
            description = PARAMETER_PUBLIC_KEY_STATEMENT_ID_DESCRIPTION, example = "1") Long id, Authentication authentication) {
        PublicKeyStatementDto response = service.findPublicKeyStatementForUserById(id, getAuthenticationEmail(authentication));
        setVerificationCenter(response, Long.valueOf(response.getVerificationCenterId()));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // TODO: убрать на рефакторинге, когда будет более конкретно ясна связь между сущностями
    private void setVerificationCenter(PublicKeyStatementDto dto, Long centerId) {
        dto.setVerificationCenter(publicKeyVerificationCenterService.findByPublicKeyVerificationCenterId(centerId).getName());
    }
}
