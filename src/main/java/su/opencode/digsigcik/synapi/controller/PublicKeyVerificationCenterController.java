package su.opencode.digsigcik.synapi.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.annotation.MchdExceptionHandler;
import su.opencode.digsigcik.synapi.dto.PublicKeyVerificationCenterInputDto;
import su.opencode.digsigcik.synapi.dto.PublicKeyVerificationCenterOutputDto;
import su.opencode.digsigcik.synapi.exception.BadRequestException;
import su.opencode.digsigcik.synapi.model.PublicKeyVerificationCenter;
import su.opencode.digsigcik.synapi.service.PublicKeyVerificationCenterService;

import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
@RequestMapping("/api/v1/verification_center")
@AllArgsConstructor
public class PublicKeyVerificationCenterController {
    private final PublicKeyVerificationCenterService service;

    @SneakyThrows
    @Operation(summary = CONTROLLER_PUBLIC_KEY_VERIFICATION_CENTER_FIND_ALL_SUMMARY, description = CONTROLLER_PUBLIC_KEY_VERIFICATION_CENTER_FIND_ALL_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @GetMapping()
    public ResponseEntity<?> findAllPublicKeyVerificationCenter() {
        List<PublicKeyVerificationCenter> response;
        try {
            response = service.findAllPublicKeyVerificationCenter();
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(PUBLIC_KEY_VERIFICATION_CENTER_IS_NULL);
        }
        return new ResponseEntity<>(response.stream().map(this::convertToOutputDto).collect(Collectors.toList()), HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_PUBLIC_KEY_VERIFICATION_CENTER_SAVE_SUMMARY, description = CONTROLLER_PUBLIC_KEY_VERIFICATION_CENTER_SAVE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @PostMapping(value = "save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> savePublicKeyVerificationCenter(@ModelAttribute PublicKeyVerificationCenterInputDto publicKeyVerificationCenterInputDto) throws IOException {

        PublicKeyVerificationCenter publicKeyVerificationCenter = convertToEntity(publicKeyVerificationCenterInputDto);
        if(publicKeyVerificationCenterInputDto.getTemplateRelease() != null) {
            publicKeyVerificationCenter.setContentTypeRelease(publicKeyVerificationCenterInputDto.getTemplateRelease().getContentType());
        }
        if(publicKeyVerificationCenterInputDto.getTemplateRecall() != null) {
            publicKeyVerificationCenter.setContentTypeRecall(publicKeyVerificationCenterInputDto.getTemplateRecall().getContentType());
        }
        PublicKeyVerificationCenter response;
        try {
            response = service.savePublicKeyVerificationCenter(publicKeyVerificationCenter);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(PUBLIC_KEY_VERIFICATION_CENTER_IS_NULL);
        }
        return new ResponseEntity<>(convertToOutputDto(response), HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_PUBLIC_KEY_VERIFICATION_CENTER_FIND_BY_ID_SUMMARY, description = CONTROLLER_PUBLIC_KEY_VERIFICATION_CENTER_FIND_BY_ID_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> findByPublicKeyVerificationCenterId(@PathVariable @Parameter(name = "id",
            description = PARAMETER_PUBLIC_KEY_VERIFICATION_CENTER_ID_DESCRIPTION, example = "1" ) Long id) throws IOException {
        PublicKeyVerificationCenter response;
        try {
            response = service.findByPublicKeyVerificationCenterId(id);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(PUBLIC_KEY_VERIFICATION_CENTER_IS_NULL);
        }
        return new ResponseEntity<>(convertToOutputDto(response), HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_PUBLIC_KEY_VERIFICATION_CENTER_UPDATE_SUMMARY, description = CONTROLLER_PUBLIC_KEY_VERIFICATION_CENTER_UPDATE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @PutMapping(value = "update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updatePublicKeyVerificationCenter(
            @RequestPart( "id" ) String id,
            @RequestPart( "name" ) String name,
            @RequestPart( "templateRelease" ) MultipartFile templateRelease,
            @RequestPart( "templateRecall" ) MultipartFile templateRecall) throws IOException {
        PublicKeyVerificationCenterInputDto publicKeyVerificationCenterInputDto = new PublicKeyVerificationCenterInputDto();
        publicKeyVerificationCenterInputDto.setId(Long.parseLong(id));
        publicKeyVerificationCenterInputDto.setName(name);
        publicKeyVerificationCenterInputDto.setTemplateRelease(templateRelease);
        publicKeyVerificationCenterInputDto.setTemplateRecall(templateRecall);

        PublicKeyVerificationCenter publicKeyVerificationCenter = convertToEntity(publicKeyVerificationCenterInputDto);
        if(publicKeyVerificationCenterInputDto.getTemplateRelease() != null) {
            publicKeyVerificationCenter.setContentTypeRelease(publicKeyVerificationCenterInputDto.getTemplateRelease().getContentType());
        }
        if(publicKeyVerificationCenterInputDto.getTemplateRecall() != null) {
            publicKeyVerificationCenter.setContentTypeRecall(publicKeyVerificationCenterInputDto.getTemplateRecall().getContentType());
        }
        PublicKeyVerificationCenter response;
        try {
            response = service.updatePublicKeyVerificationCenter(publicKeyVerificationCenter);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(PUBLIC_KEY_VERIFICATION_CENTER_IS_NULL);
        }
        return new ResponseEntity<>(convertToOutputDto(response), HttpStatus.OK);
    }


    @Operation(summary = CONTROLLER_PUBLIC_KEY_VERIFICATION_CENTER_DELETE_SUMMARY, description = CONTROLLER_PUBLIC_KEY_VERIFICATION_CENTER_DELETE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @DeleteMapping("delete/{id}")
    public ResponseEntity<?> deletePublicKeyVerificationCenter(@PathVariable @Parameter(name = "id",
            description = PARAMETER_PUBLIC_KEY_VERIFICATION_CENTER_ID_DESCRIPTION, example = "1" ) Long id) {
        try {
            service.deletePublicKeyVerificationCenter(id);
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
            description = PARAMETER_PUBLIC_KEY_VERIFICATION_CENTER_ID_DESCRIPTION, example = "1" ) Long id) throws UnsupportedEncodingException {
        PublicKeyVerificationCenter downloadTempl;
        try {
            downloadTempl = service.findByPublicKeyVerificationCenterId(id);
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
            description = PARAMETER_PUBLIC_KEY_VERIFICATION_CENTER_ID_DESCRIPTION, example = "1" ) Long id) {
        PublicKeyVerificationCenter downloadTempl;
        try {
            downloadTempl = service.findByPublicKeyVerificationCenterId(id);
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

    private PublicKeyVerificationCenterOutputDto convertToOutputDto(PublicKeyVerificationCenter publicKeyVerificationCenter) {
        PublicKeyVerificationCenterOutputDto publicKeyVerificationCenterOutputDto = new PublicKeyVerificationCenterOutputDto();
        publicKeyVerificationCenterOutputDto.setId(publicKeyVerificationCenter.getId());
        publicKeyVerificationCenterOutputDto.setName(publicKeyVerificationCenter.getName());
        return publicKeyVerificationCenterOutputDto;
    }

    private PublicKeyVerificationCenter convertToEntity(PublicKeyVerificationCenterInputDto publicKeyVerificationCenterInputDto) throws IOException {
        PublicKeyVerificationCenter publicKeyVerificationCenter = new PublicKeyVerificationCenter();
        publicKeyVerificationCenter.setId(publicKeyVerificationCenterInputDto.getId());
        publicKeyVerificationCenter.setName(publicKeyVerificationCenterInputDto.getName());
        if(publicKeyVerificationCenterInputDto.getTemplateRelease() != null) {
            byte[] bytes = publicKeyVerificationCenterInputDto.getTemplateRelease().getBytes();
            publicKeyVerificationCenter.setTemplateRelease(bytes);
        }
        if(publicKeyVerificationCenterInputDto.getTemplateRecall() != null){
            byte[] bytes1 = publicKeyVerificationCenterInputDto.getTemplateRecall().getBytes();
            publicKeyVerificationCenter.setTemplateRecall(bytes1);
        }
        return publicKeyVerificationCenter;
    }
}
