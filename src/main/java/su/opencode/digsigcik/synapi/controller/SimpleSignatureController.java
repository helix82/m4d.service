package su.opencode.digsigcik.synapi.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import su.opencode.digsigcik.synapi.config.WebConfigProperties;
import java.io.IOException;
import su.opencode.digsigcik.synapi.dto.*;
import su.opencode.digsigcik.synapi.enums.HashAlgorithm;
import su.opencode.digsigcik.synapi.exception.BadRequestException;
import su.opencode.digsigcik.synapi.model.ContainerFile;
import su.opencode.digsigcik.synapi.model.EncryptionKeyEntity;
import su.opencode.digsigcik.synapi.model.UserProjection;
import su.opencode.digsigcik.synapi.repository.ContainerFileRepository;
import su.opencode.digsigcik.synapi.repository.EncryptionKeyRepository;
import su.opencode.digsigcik.synapi.repository.UserProjectionRepository;
import su.opencode.digsigcik.synapi.service.ESUserService;
import su.opencode.digsigcik.synapi.service.SimpleSignatureService;
import su.opencode.digsigcik.synapi.util.DateUtils;
import su.opencode.digsigcik.synapi.util.JwtTokenUtils;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.util.UUID;

import static su.opencode.digsigcik.synapi.controller.ApiControllerV1.*;
import static su.opencode.digsigcik.synapi.dscp.ContainerTypeEnum.SIMPLE_DETACHED;
import static su.opencode.digsigcik.synapi.dscp.SignatureTypeEnum.SIMPLE;
import static su.opencode.digsigcik.synapi.util.CommonUtils.filename;
import static su.opencode.digsigcik.synapi.util.Constants.*;
import static su.opencode.digsigcik.synapi.util.FileUtils.resourceOctetStreamOrInternal;

@RestController
@SesExceptionHandler
@RequiredArgsConstructor
@RequestMapping("/api/v1/ses")
public class SimpleSignatureController {
    private final SimpleSignatureService simpleSignatureService;
    private final JwtTokenUtils jwtTokenUtils;
    private final ESUserService esUserService;
    private final ContainerFileRepository containerFileRepository;
    private final WebConfigProperties webConfigProperties;
    private final UserProjectionRepository userProjectionRepository;
    private final EncryptionKeyRepository encryptionKeyRepository;

    @Operation(summary = CONTROLLER_SIMPLE_SIGNATURE_CONTAINER_CREATE,
            description = CONTROLLER_SIMPLE_SIGNATURE_CONTAINER_CREATE)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 400, message = API_RESPONSE_CODE_400_MESSAGE)
    })
    @PostMapping(value = "/create",
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> createCustomSimpleSignatureContainer(@RequestHeader(HttpHeaders.AUTHORIZATION) String authToken,
                                                                         @RequestParam String pinCode,
                                                                         @RequestPart("file") MultipartFile file) throws JsonProcessingException {
        return createCustomSimpleSignatureContainerWithCustomHash(authToken, HashAlgorithm.SHA256, pinCode, file);
    }

    @Operation(summary = CONTROLLER_SIMPLE_SIGNATURE_CONTAINER_CREATE_CUSTOM_HASH,
            description = CONTROLLER_SIMPLE_SIGNATURE_CONTAINER_CREATE_CUSTOM_HASH)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 400, message = API_RESPONSE_CODE_400_MESSAGE)
    })
    @PostMapping(value = "/create-custom-ses-container-custom-hash",
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> createCustomSimpleSignatureContainerWithCustomHash(@RequestHeader(HttpHeaders.AUTHORIZATION) String authToken,
                                                                                       HashAlgorithm hashAlgorithm,
                                                                                       @RequestParam String pinCode,
                                                                                       @RequestPart("file") MultipartFile file) throws JsonProcessingException {
        checkIfPinCodeAndFileEmpty(pinCode, file);
        JwtPayloadDto jwtPayloadDto = extractAuthToken(authToken);
        LocalDateTime timestamp = LocalDateTime.now();
        ReportDTO reportDTO = ReportDTO.builder()
                .Message("Простая подпись валидна")
                .MessageDetail("Простая подпись пользователя " + jwtPayloadDto.getPatronymic() + " " + jwtPayloadDto.getFirstName() + " " + jwtPayloadDto.getLastName() + " валидна. Дата подписи: " + timestamp.toString())
                .build();
        ObjectMapper objectMapper = new ObjectMapper();
        String signCheckJsonString = objectMapper.writeValueAsString(reportDTO);
        byte[] signCheck = signCheckJsonString.getBytes(StandardCharsets.UTF_8);
        byte[] signReport = new byte[0];
        ContainerInputInfoDto containerInputInfo = ContainerInputInfoDto.builder()
                .login(jwtPayloadDto.getPreferredUsername())
                .firstname(jwtPayloadDto.getFirstName())
                .lastname(jwtPayloadDto.getLastName())
                .patronymic(jwtPayloadDto.getPatronymic())
                .timestamp(timestamp)
                .signatureAlgorithm("CUSTOM")
                .containerType(SIMPLE_DETACHED)
                .signatureType(SIMPLE)
                .hashAlgorithm(hashAlgorithm.getAlgorithmName())
                .pinCode(pinCode)
                .signCheck(signCheck)
                .signReport(signReport)
                .build();

        var res = simpleSignatureService.createSimpleCustomSignatureResource(file, containerInputInfo);
        if (res == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
        return resourceOctetStreamOrInternal(res, filename(res, DEFAULT_FILENAME));
    }

    @Operation(summary = CONTROLLER_SIMPLE_SIGNATURE_CREATE,
            description = CONTROLLER_SIMPLE_SIGNATURE_CREATE)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 400, message = API_RESPONSE_CODE_400_MESSAGE)
    })
    @PostMapping(value = "/create-custom-ses")
    public ResponseEntity<?> createCustomSimpleSignature(@RequestHeader(HttpHeaders.AUTHORIZATION) String authToken,
                                                         @RequestParam String pinCode,
                                                         @RequestPart("file") MultipartFile file) throws JsonProcessingException {

        return createCustomSimpleSignatureWithCustomHash(authToken, HashAlgorithm.SHA256, pinCode, file);
    }


    @Operation(summary = CONTROLLER_SIMPLE_SIGNATURE_CREATE_CUSTOM_HASH,
            description = CONTROLLER_SIMPLE_SIGNATURE_CREATE_CUSTOM_HASH)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 400, message = API_RESPONSE_CODE_400_MESSAGE)
    })
    @PostMapping(value = "/create-custom-ses-custom-hash")
    public ResponseEntity<?> createCustomSimpleSignatureWithCustomHash(@RequestHeader(HttpHeaders.AUTHORIZATION) String authToken,
                                                                       HashAlgorithm hashAlgorithm,
                                                                       @RequestParam String pinCode,
                                                                       @RequestPart("file") MultipartFile file) throws JsonProcessingException {

        checkIfPinCodeAndFileEmpty(pinCode, file);
        JwtPayloadDto jwtPayloadDto = extractAuthToken(authToken);
        LocalDateTime timestamp = LocalDateTime.now();
        ReportDTO reportDTO = ReportDTO.builder()
                .Message("Простая подпись валидна")
                .MessageDetail("Простая подпись пользователя " + jwtPayloadDto.getPatronymic() + " " + jwtPayloadDto.getFirstName() + " " + jwtPayloadDto.getLastName() + " валидна. Дата подписи: " + timestamp.toString())
                .build();
        ObjectMapper objectMapper = new ObjectMapper();
        String signCheckJsonString = objectMapper.writeValueAsString(reportDTO);
        byte[] signCheck = signCheckJsonString.getBytes(StandardCharsets.UTF_8);
        byte[] signReport = new byte[0];
        ContainerInputInfoDto containerInputInfo = ContainerInputInfoDto.builder()
                .login(jwtPayloadDto.getPreferredUsername())
                .firstname(jwtPayloadDto.getFirstName())
                .lastname(jwtPayloadDto.getLastName())
                .patronymic(jwtPayloadDto.getPatronymic())
                .timestamp(timestamp)
                .signatureAlgorithm("CUSTOM")
                .containerType(SIMPLE_DETACHED)
                .signatureType(SIMPLE)
                .hashAlgorithm(hashAlgorithm.getAlgorithmName())
                .pinCode(pinCode)
                .signCheck(signCheck)
                .signReport(signReport)
                .build();

        var res = simpleSignatureService.createSimpleCustomSignatureResource(file, containerInputInfo);
        if (res == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }

        String containerPath = String.format("%s%scontainer-%s.dscp", ROOT_FILE_STORAGE_PATH, DSCP_DIRECTORY_PATH,
                DateUtils.formatDate(containerInputInfo.getTimestamp())
        );
        UUID xmlUUID = UUID.randomUUID();
        containerFileRepository.save(ContainerFile.builder()
                .uuid(xmlUUID.toString())
                .filePath(containerPath)
                .build());

        SesResponseDto sesResponseDto = SesResponseDto.builder()
                .uri(webConfigProperties.getServer()
                        .getUri() + API_V_1_SES_FILES + GET_CONTAINER + "?uuid=" + xmlUUID)
                .UUID(xmlUUID.toString())
                .sigType("simple")
                .firstName(containerInputInfo.getFirstname())
                .lastName(containerInputInfo.getLastname())
                .patronymic(containerInputInfo.getPatronymic())
                .build();

        return new ResponseEntity<>(sesResponseDto, HttpStatus.OK);
    }


    @Operation(summary = CONTROLLER_SIMPLE_SIGNATURE_VERIFY,
            description = CONTROLLER_SIMPLE_SIGNATURE_VERIFY)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 400, message = API_RESPONSE_CODE_400_MESSAGE)
    })

    @PostMapping(value = "/create-ses-container-custom-hash",
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> createSimpleSignatureContainerWithCustomHash(@RequestHeader(HttpHeaders.AUTHORIZATION) String authToken,
                                                                                 HashAlgorithm hashAlgorithm,
                                                                                 @RequestPart("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException(API_RESPONSE_CODE_400_MESSAGE);
        }
        JwtPayloadDto jwtPayloadDto = extractAuthToken(authToken);
        UserProjection userProjection =
                userProjectionRepository.getUserProjectionByLogin(jwtPayloadDto.getPreferredUsername());
        if (userProjection == null) {
            throw new BadRequestException(USER_PROJECTION_NOT_FOUND);
        }
        EncryptionKeyEntity keyEntity =
                encryptionKeyRepository.getEncryptionKeyEntitiesByUserProjection(userProjection);
        if (keyEntity == null) {
            throw new RuntimeException("keyEntity not found");
        } else {
            LocalDateTime timestamp = LocalDateTime.now();
            ReportDTO reportDTO = ReportDTO.builder()
                    .Message("Простая подпись валидна")
                    .MessageDetail("Простая подпись пользователя " + jwtPayloadDto.getPatronymic() + " " + jwtPayloadDto.getFirstName() + " " + jwtPayloadDto.getLastName() + " валидна. Дата подписи: " + timestamp.toString())
                    .build();
            ObjectMapper objectMapper = new ObjectMapper();
            String signCheckJsonString = objectMapper.writeValueAsString(reportDTO);
            byte[] signCheck = signCheckJsonString.getBytes(StandardCharsets.UTF_8);
            byte[] signReport = new byte[0];
            KeyPair keyPair = getKeyPairByEncryptionKeyEntity(keyEntity);
            ContainerInputInfoDto containerInputInfo = ContainerInputInfoDto.builder()
                    .login(jwtPayloadDto.getPreferredUsername())
                    .firstname(jwtPayloadDto.getFirstName())
                    .lastname(jwtPayloadDto.getLastName())
                    .patronymic(jwtPayloadDto.getPatronymic())
                    .timestamp(timestamp)
                    .signatureAlgorithm(keyEntity
                            .getAlgorithm()
                            .getAlgorithmName())
                    .containerType(SIMPLE_DETACHED)
                    .signatureType(SIMPLE)
                    .hashAlgorithm(hashAlgorithm.getAlgorithmName())
                    .privateKey(keyPair.getPrivate())
                    .publicKey(keyPair.getPublic())
                    .signCheck(signCheck)
                    .signReport(signReport)
                    .build();

            var res = simpleSignatureService.createSimpleSignatureResource(file, containerInputInfo);
            if (res == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(null);
            }
            return resourceOctetStreamOrInternal(res, filename(res, DEFAULT_FILENAME));
        }
    }

    @Operation(summary = CONTROLLER_UNQUALIFIED_SIGNATURE_CREATE,
            description = CONTROLLER_UNQUALIFIED_SIGNATURE_CREATE)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 400, message = API_RESPONSE_CODE_400_MESSAGE)
    })
    @PostMapping(value = "/create-ses-container")
    public ResponseEntity<Resource> createSimpleSignatureContainer(@RequestHeader(HttpHeaders.AUTHORIZATION) String authToken,
                                                                   @RequestPart("file") MultipartFile file) throws IOException {
        return createSimpleSignatureContainerWithCustomHash(authToken, HashAlgorithm.SHA256, file);
    }

    @Operation(summary = CONTROLLER_UNQUALIFIED_SIGNATURE_CREATE_WITH_CUSTOM_HASH,
            description = CONTROLLER_UNQUALIFIED_SIGNATURE_CREATE_WITH_CUSTOM_HASH)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 400, message = API_RESPONSE_CODE_400_MESSAGE)
    })
    @PostMapping(value = "/create-ses-custom-hash")
    public ResponseEntity<?> createSimpleSignatureCustomHash(@RequestHeader(HttpHeaders.AUTHORIZATION) String authToken, HashAlgorithm hashAlgorithm, @RequestPart("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException(API_RESPONSE_CODE_400_MESSAGE);
        }
        JwtPayloadDto jwtPayloadDto = extractAuthToken(authToken);

        UserProjection userProjection =
                userProjectionRepository.getUserProjectionByLogin(jwtPayloadDto.getPreferredUsername());
        if (userProjection == null) {
            throw new BadRequestException(USER_PROJECTION_NOT_FOUND);
        }
        EncryptionKeyEntity keyEntity =
                encryptionKeyRepository.getEncryptionKeyEntitiesByUserProjection(userProjection);
        if (keyEntity == null) {
            throw new BadRequestException("keyEntity not found");
        } else {
            LocalDateTime timestamp = LocalDateTime.now();
            ReportDTO reportDTO = ReportDTO.builder()
                    .Message("Простая подпись валидна")
                    .MessageDetail("Простая подпись пользователя " + jwtPayloadDto.getPatronymic() + " " + jwtPayloadDto.getFirstName() + " " + jwtPayloadDto.getLastName() + " валидна. Дата подписи: " + timestamp.toString())
                    .build();
            ObjectMapper objectMapper = new ObjectMapper();
            String signCheckJsonString = objectMapper.writeValueAsString(reportDTO);
            byte[] signCheck = signCheckJsonString.getBytes(StandardCharsets.UTF_8);
            byte[] signReport = new byte[0];
            KeyPair keyPair = getKeyPairByEncryptionKeyEntity(keyEntity);
            ContainerInputInfoDto containerInputInfo = ContainerInputInfoDto.builder()
                    .login(jwtPayloadDto.getPreferredUsername())
                    .firstname(jwtPayloadDto.getFirstName())
                    .lastname(jwtPayloadDto.getLastName())
                    .patronymic(jwtPayloadDto.getPatronymic())
                    .timestamp(timestamp)
                    .signatureAlgorithm(keyEntity
                            .getAlgorithm()
                            .getAlgorithmName())
                    .containerType(SIMPLE_DETACHED)
                    .signatureType(SIMPLE)
                    .hashAlgorithm(hashAlgorithm.getAlgorithmName())
                    .privateKey(keyPair.getPrivate())
                    .publicKey(keyPair.getPublic())
                    .signCheck(signCheck)
                    .signReport(signReport)
                    .build();

            var res = simpleSignatureService.createSimpleSignatureResource(file, containerInputInfo);
            if (res == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(null);
            }


            String containerPath = String.format("%s%scontainer-%s.dscp", ROOT_FILE_STORAGE_PATH, DSCP_DIRECTORY_PATH,
                    DateUtils.formatDate(containerInputInfo.getTimestamp())
            );
            UUID xmlUUID = UUID.randomUUID();
            containerFileRepository.save(ContainerFile.builder()
                    .uuid(xmlUUID.toString())
                    .filePath(containerPath)
                    .build());

            SesResponseDto sesResponseDto = SesResponseDto.builder()
                    .uri(webConfigProperties.getServer()
                            .getUri() + API_V_1_SES_FILES + GET_CONTAINER + "?uuid=" + xmlUUID)
                    .UUID(xmlUUID.toString())
                    .sigType(keyEntity
                            .getAlgorithm()
                            .getAlgorithmName())
                    .firstName(containerInputInfo.getFirstname())
                    .lastName(containerInputInfo.getLastname())
                    .patronymic(containerInputInfo.getPatronymic())
                    .build();

            return new ResponseEntity<>(sesResponseDto, HttpStatus.OK);
        }
    }


    @Operation(summary = CONTROLLER_UNQUALIFIED_SIGNATURE_CREATE,
            description = CONTROLLER_UNQUALIFIED_SIGNATURE_CREATE)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 400, message = API_RESPONSE_CODE_400_MESSAGE)
    })
    @PostMapping(value = "/create-ses")
    public ResponseEntity<?> createSimpleSignature(@RequestHeader(HttpHeaders.AUTHORIZATION) String authToken,
                                                   @RequestPart("file") MultipartFile file) throws IOException {
        return createSimpleSignatureCustomHash(authToken, HashAlgorithm.SHA256, file);
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


    private KeyPair getKeyPairByEncryptionKeyEntity(EncryptionKeyEntity keyEntity) {
        PrivateKey privateKey = simpleSignatureService.getPrivateKey(keyEntity
                .getPrivateKey(), keyEntity
                .getAlgorithm()
                .getAlgorithmName());

        if (privateKey == null) {
            throw new RuntimeException("privateKey not found");
        }
        PublicKey publicKey = simpleSignatureService.getPublicKey(keyEntity
                .getPublicKey(), keyEntity
                .getAlgorithm()
                .getAlgorithmName());
        if (publicKey == null) {
            throw new RuntimeException("publicKey not found");
        }
        return new KeyPair(publicKey, privateKey);
    }

}

