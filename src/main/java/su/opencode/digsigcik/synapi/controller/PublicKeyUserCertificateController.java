package su.opencode.digsigcik.synapi.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import su.opencode.digsigcik.synapi.annotation.MchdExceptionHandler;
import su.opencode.digsigcik.synapi.dto.PublicKeyUserCertificateDto;
import su.opencode.digsigcik.synapi.exception.BadRequestException;
import su.opencode.digsigcik.synapi.model.PublicKeyUserCertificate;
import su.opencode.digsigcik.synapi.service.PublicKeyUserCertificateService;

import javax.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static su.opencode.digsigcik.synapi.util.Constant.ID_IS_NULL;
import static su.opencode.digsigcik.synapi.util.Constants.*;
import static su.opencode.digsigcik.synapi.util.Constants.CONTROLLER_PUBLIC_KEY_USER_CERTIFICATE_SAVE_DESCRIPTION;


@RestController
@MchdExceptionHandler
@RequestMapping("/api/v1/user_certificate")
@AllArgsConstructor
public class PublicKeyUserCertificateController {
    private final PublicKeyUserCertificateService service;
    private final ModelMapper modelMapper;

    @Operation(summary = CONTROLLER_PUBLIC_KEY_USER_CERTIFICATE_FIND_ALL_SUMMARY, description = CONTROLLER_PUBLIC_KEY_USER_CERTIFICATE_FIND_ALL_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @GetMapping()
    public ResponseEntity<?> findAllPublicKeyUserCertificate() {
        List<PublicKeyUserCertificate> response;
        try {
            response = service.findAllPublicKeyUserCertificate();
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(PUBLIC_KEY_USER_CERTIFICATE_IS_NULL);
        }
        return new ResponseEntity<>(response.stream().map(this::convertToDto).collect(Collectors.toList()), HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_PUBLIC_KEY_USER_CERTIFICATE_SAVE_SUMMARY, description = CONTROLLER_PUBLIC_KEY_USER_CERTIFICATE_SAVE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @PostMapping("save")
    public ResponseEntity<?> savePublicKeyUserCertificate(@RequestBody PublicKeyUserCertificateDto publicKeyUserCertificateDto) {
        PublicKeyUserCertificate publicKeyUserCertificate = convertToEntity(publicKeyUserCertificateDto);
        PublicKeyUserCertificate response;
        try {
            response = service.savePublicKeyUserCertificate(publicKeyUserCertificate);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(PUBLIC_KEY_USER_CERTIFICATE_IS_NULL);
        }
        return new ResponseEntity<>(convertToDto(response), HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_PUBLIC_KEY_USER_CERTIFICATE_FIND_BY_ID_SUMMARY, description = CONTROLLER_PUBLIC_KEY_USER_CERTIFICATE_FIND_BY_ID_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> findByPublicKeyUserCertificateId(@PathVariable @Parameter(name = "id",
            description = PARAMETER_PUBLIC_KEY_USER_CERTIFICATE_ID_DESCRIPTION, example = "1" ) Long id) {
        PublicKeyUserCertificate response;
        try {
            response = service.findByPublicKeyUserCertificateId(id);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(PUBLIC_KEY_USER_CERTIFICATE_IS_NULL);
        }
        return new ResponseEntity<>(convertToDto(response), HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_PUBLIC_KEY_USER_CERTIFICATE_UPDATE_SUMMARY, description = CONTROLLER_PUBLIC_KEY_USER_CERTIFICATE_UPDATE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @PutMapping("update")
    public ResponseEntity<?> updatePublicKeyUserCertificate(@RequestBody PublicKeyUserCertificateDto publicKeyUserCertificateDto) {
        PublicKeyUserCertificate publicKeyUserCertificate = convertToEntity(publicKeyUserCertificateDto);
        PublicKeyUserCertificate response;
        try {
            response = service.updatePublicKeyUserCertificate(publicKeyUserCertificate);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(PUBLIC_KEY_USER_CERTIFICATE_IS_NULL);
        }
        return new ResponseEntity<>(convertToDto(response), HttpStatus.OK);
    }


    @Operation(summary = CONTROLLER_PUBLIC_KEY_USER_CERTIFICATE_DELETE_SUMMARY, description = CONTROLLER_PUBLIC_KEY_USER_CERTIFICATE_DELETE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @DeleteMapping("delete/{id}")
    public ResponseEntity<?> deletePublicKeyUserCertificate(@PathVariable @Parameter(name = "id",
            description = PARAMETER_PUBLIC_KEY_USER_CERTIFICATE_ID_DESCRIPTION, example = "1" ) Long id) {
        try {
            service.deletePublicKeyUserCertificate(id);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(ID_IS_NULL);
        }
        Map<String, String> body = new HashMap<>();
        body.put("result", "true");

        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    private PublicKeyUserCertificateDto convertToDto(PublicKeyUserCertificate publicKeyUserCertificate){
        PublicKeyUserCertificateDto publicKeyUserCertificateDto = modelMapper.map(publicKeyUserCertificate, PublicKeyUserCertificateDto.class);
        return publicKeyUserCertificateDto;
    }

    private PublicKeyUserCertificate convertToEntity(PublicKeyUserCertificateDto publicKeyUserCertificateDto){
        PublicKeyUserCertificate publicKeyUserCertificate = modelMapper.map(publicKeyUserCertificateDto, PublicKeyUserCertificate.class);
        return publicKeyUserCertificate;
    }
}
