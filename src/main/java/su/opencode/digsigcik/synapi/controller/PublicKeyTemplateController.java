package su.opencode.digsigcik.synapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.annotation.MchdExceptionHandler;
import su.opencode.digsigcik.synapi.dto.PublicKeyTemplateDto;
import su.opencode.digsigcik.synapi.exception.BadRequestException;
import su.opencode.digsigcik.synapi.model.PublicKeyTemplate;
import su.opencode.digsigcik.synapi.service.PublicKeyTemplateService;

import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static su.opencode.digsigcik.synapi.util.Constant.ID_IS_NULL;
import static su.opencode.digsigcik.synapi.util.Constants.*;

@RestController
@MchdExceptionHandler
@RequestMapping("/api/v1/template")
@AllArgsConstructor
public class PublicKeyTemplateController {
    private final PublicKeyTemplateService service;
    private final ModelMapper modelMapper;

    @Operation(summary = CONTROLLER_PUBLIC_KEY_TEMPLATE_FIND_ALL_SUMMARY, description = CONTROLLER_PUBLIC_KEY_TEMPLATE_FIND_ALL_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @GetMapping()
    public ResponseEntity<?> findAllPublicKeyTemplate() {
        List<PublicKeyTemplate> response;
        try {
            response = service.findAllPublicKeyTemplate();
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(PUBLIC_KEY_TEMPLATE_IS_NULL);
        }
        return new ResponseEntity<>(response.stream().map(this::convertToDto).collect(Collectors.toList()), HttpStatus.OK);
    }

    @PostMapping(value = "save_file", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<?> savePublicKey(@RequestBody String publicKeyTemplate, @RequestPart("srcFile") MultipartFile sourceFile) throws IOException {
        byte[] data = sourceFile.getBytes();
        ObjectMapper mapper = new ObjectMapper();
        PublicKeyTemplateDto publicKeyTemplateDto = mapper.readValue(publicKeyTemplate, PublicKeyTemplateDto.class);
    return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_PUBLIC_KEY_TEMPLATE_SAVE_SUMMARY, description = CONTROLLER_PUBLIC_KEY_TEMPLATE_SAVE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @PostMapping(value = "save")
    public ResponseEntity<?> savePublicKeyTemplate(@RequestPart("srcFile") MultipartFile sourceFile, @RequestBody PublicKeyTemplateDto publicKeyTemplateDto) throws IOException {
        byte[] data = sourceFile.getBytes();
        publicKeyTemplateDto.setTemplate(data);
        PublicKeyTemplate publicKeyTemplate = convertToEntity(publicKeyTemplateDto);
        PublicKeyTemplate response;
        try {
            response = service.savePublicKeyTemplate(publicKeyTemplate);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(PUBLIC_KEY_TEMPLATE_IS_NULL);
        }
        return new ResponseEntity<>(convertToDto(response), HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_PUBLIC_KEY_TEMPLATE_FIND_BY_ID_SUMMARY, description = CONTROLLER_PUBLIC_KEY_TEMPLATE_FIND_BY_ID_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> findByPublicKeyTemplateId(@PathVariable @Parameter(name = "id",
            description = PARAMETER_PUBLIC_KEY_TEMPLATE_ID_DESCRIPTION, example = "1" ) Long id) {
        PublicKeyTemplate response;
        try {
            response = service.findByPublicKeyTemplateId(id);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(PUBLIC_KEY_TEMPLATE_IS_NULL);
        }
        return new ResponseEntity<>(convertToDto(response), HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_PUBLIC_KEY_TEMPLATE_UPDATE_SUMMARY, description = CONTROLLER_PUBLIC_KEY_TEMPLATE_UPDATE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @PutMapping("update")
    public ResponseEntity<?> updatePublicKeyTemplate(@RequestBody PublicKeyTemplateDto publicKeyTemplateDto) {
        PublicKeyTemplate publicKeyTemplate = convertToEntity(publicKeyTemplateDto);
        PublicKeyTemplate response;
        try {
            response = service.updatePublicKeyTemplate(publicKeyTemplate);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(PUBLIC_KEY_TEMPLATE_IS_NULL);
        }
        return new ResponseEntity<>(convertToDto(response), HttpStatus.OK);
    }


    @Operation(summary = CONTROLLER_PUBLIC_KEY_TEMPLATE_DELETE_SUMMARY, description = CONTROLLER_PUBLIC_KEY_TEMPLATE_DELETE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @DeleteMapping("delete/{id}")
    public ResponseEntity<?> deletePublicKeyTemplate(@PathVariable @Parameter(name = "id",
            description = PARAMETER_PUBLIC_KEY_TEMPLATE_ID_DESCRIPTION, example = "1" ) Long id) {
        try {
            service.deletePublicKeyTemplate(id);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(ID_IS_NULL);
        }
        Map<String, String> body = new HashMap<>();
        body.put("result", "true");

        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    private PublicKeyTemplateDto convertToDto(PublicKeyTemplate publicKeyTemplate){
        PublicKeyTemplateDto publicKeyTemplateDto = modelMapper.map(publicKeyTemplate, PublicKeyTemplateDto.class);
        return publicKeyTemplateDto;
    }

    private PublicKeyTemplate convertToEntity(PublicKeyTemplateDto publicKeyTemplateDto){
        PublicKeyTemplate publicKeyTemplate = modelMapper.map(publicKeyTemplateDto, PublicKeyTemplate.class);
        return publicKeyTemplate;
    }
}
