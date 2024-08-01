package su.opencode.digsigcik.synapi.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.annotation.SesExceptionHandler;
import su.opencode.digsigcik.synapi.dto.ContainerInputInfoDto;
import su.opencode.digsigcik.synapi.dto.JwtPayloadDto;
import su.opencode.digsigcik.synapi.dto.UserDto;
import su.opencode.digsigcik.synapi.enums.HashAlgorithm;
import su.opencode.digsigcik.synapi.enums.KeySize;
import su.opencode.digsigcik.synapi.enums.SimpleSignatureAlgorithm;
import su.opencode.digsigcik.synapi.exception.BadRequestException;
import su.opencode.digsigcik.synapi.model.EncryptionKeyEntity;
import su.opencode.digsigcik.synapi.model.PinCode;
import su.opencode.digsigcik.synapi.model.UserProjection;
import su.opencode.digsigcik.synapi.repository.PinCodeRepository;
import su.opencode.digsigcik.synapi.repository.UserProjectionRepository;
import su.opencode.digsigcik.synapi.service.ESUserService;
import su.opencode.digsigcik.synapi.service.SimpleSignatureService;
import su.opencode.digsigcik.synapi.util.Constants;
import su.opencode.digsigcik.synapi.util.JwtTokenUtils;

import javax.transaction.Transactional;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.LocalDateTime;

import static su.opencode.digsigcik.synapi.controller.ApiControllerV1.DEFAULT_FILENAME;
import static su.opencode.digsigcik.synapi.dscp.ContainerTypeEnum.SIMPLE_ATTACHED;
import static su.opencode.digsigcik.synapi.dscp.ContainerTypeEnum.SIMPLE_DETACHED;
import static su.opencode.digsigcik.synapi.dscp.SignatureTypeEnum.SIMPLE;
import static su.opencode.digsigcik.synapi.util.CommonUtils.filename;
import static su.opencode.digsigcik.synapi.util.Constants.*;
import static su.opencode.digsigcik.synapi.util.FileUtils.resourceOctetStreamOrInternal;
import static su.opencode.digsigcik.synapi.util.HashUtils.generateHash;

@RestController
@SesExceptionHandler
@RequiredArgsConstructor
@RequestMapping("/api/v1/ses")
public class SimpleSignatureController {
    private final SimpleSignatureService simpleSignatureService;
    private final JwtTokenUtils jwtTokenUtils;
    private final ESUserService esUserService;
    private final UserProjectionRepository userProjectionRepository;
    private final PinCodeRepository pinCodeRepository;

    @Operation(summary = CONTROLLER_SIMPLE_SIGNATURE_CONTAINER_CREATE,
            description = CONTROLLER_SIMPLE_SIGNATURE_CONTAINER_CREATE)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 400, message = API_RESPONSE_CODE_400_MESSAGE)
    })
    @PostMapping(value = "/create",
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> createSimpleSignature(@RequestHeader(HttpHeaders.AUTHORIZATION) String authToken,
                                                          @RequestParam String pinCode,
                                                          HashAlgorithm hashAlgorithm,
                                                          SimpleSignatureAlgorithm simpleSignatureAlgorithm,
                                                          KeySize keySize,
                                                          @RequestParam(defaultValue = "true") boolean signatureDetached,
                                                          @RequestPart("file") MultipartFile file) throws IOException {
        checkIfPinCodeAndFileEmpty(pinCode, file);
        JwtPayloadDto jwtPayloadDto = extractAuthToken(authToken);
        UserProjection userProjection =
                userProjectionRepository.getUserProjectionByLogin(jwtPayloadDto.getPreferredUsername());
        if (userProjection == null) {
            throw new BadRequestException(USER_PROJECTION_NOT_FOUND);
        }
        PinCode pinCodeByUserProjection = pinCodeRepository.getPinCodeByUserProjection(userProjection);

        String pinCodeHash = generateHash(pinCode
                .getBytes(StandardCharsets.UTF_8), pinCodeByUserProjection.getAlgorithm());
        if (!pinCodeByUserProjection.getPinCodeHash()
                .equals(pinCodeHash)) {
            throw new BadRequestException(Constants.PIN_CODE_ERROR_MESSAGE);
        }

        LocalDateTime timestamp = LocalDateTime.now();


        ContainerInputInfoDto containerInputInfo = ContainerInputInfoDto.builder()
                .login(jwtPayloadDto.getPreferredUsername())
                .firstname(jwtPayloadDto.getFirstName())
                .lastname(jwtPayloadDto.getLastName())
                .patronymic(jwtPayloadDto.getPatronymic())
                .timestamp(timestamp)
                .signatureAlgorithm(simpleSignatureAlgorithm != null ? simpleSignatureAlgorithm.getAlgorithmName() :
                        SimpleSignatureAlgorithm.SHA256withRSA.getAlgorithmName())
                .keySize(keySize != null ? keySize : KeySize.BIT_512)
                .containerType(signatureDetached ? SIMPLE_DETACHED : SIMPLE_ATTACHED)
                .signatureTypeEnum(SIMPLE)
                .pinCode(pinCode)
                .userProjection(userProjection)
                .signatureDetached(signatureDetached)
                .hashAlgorithm(hashAlgorithm != null ? hashAlgorithm.getAlgorithmName() :
                        HashAlgorithm.SHA256.getAlgorithmName())
                .build();

        var res = simpleSignatureService.createSimpleSignatureResource(file, containerInputInfo);
        if (res == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
        return resourceOctetStreamOrInternal(res, filename(res, DEFAULT_FILENAME));
    }

    @Operation(summary = CONTROLLER_ENCRYPTION_KEY_DELETE,
            description = CONTROLLER_ENCRYPTION_KEY_DELETE)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 400, message = API_RESPONSE_CODE_400_MESSAGE)
    })
    @DeleteMapping(value = "/", produces = "application/json; charset=UTF-8")
    @Transactional
    public void deleteEncryptionKey(@RequestParam String login) {
        if (login == null || login.trim()
                .isEmpty()) {
            throw new BadRequestException(API_RESPONSE_CODE_400_MESSAGE);
        }
        UserProjection userProjection = userProjectionRepository.getUserProjectionByLogin(login);
        if (userProjection != null) {
             simpleSignatureService.deleteKeyEntity(userProjection);
        }
    }


    private JwtPayloadDto extractAuthToken(String authToken) {
        if (authToken == null || authToken.trim()
                .isEmpty()) {
            throw new BadRequestException(API_RESPONSE_CODE_400_MESSAGE);
        }
        JwtPayloadDto jwtPayloadDto = jwtTokenUtils.parseJwtPayload(authToken);
        UserDto userDto = esUserService.getESSUserByLogin(jwtPayloadDto.getPreferredUsername());
        if (userDto == null) {
            throw new BadRequestException(JWT_USER_NOT_FOUND);
        }
        return jwtPayloadDto;
    }

    private void checkIfPinCodeAndFileEmpty(String pinCode, MultipartFile file) {
        if (pinCode == null || pinCode.trim()
                .isEmpty()) {
            throw new BadRequestException(API_RESPONSE_CODE_400_MESSAGE);
        }

        if (file == null || file.isEmpty()) {
            throw new BadRequestException(API_RESPONSE_CODE_400_MESSAGE);
        }
    }


}

