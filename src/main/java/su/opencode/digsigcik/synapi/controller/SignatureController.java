package su.opencode.digsigcik.synapi.controller;

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
import su.opencode.digsigcik.synapi.enums.SignatureType;
import su.opencode.digsigcik.synapi.exception.BadRequestException;
import su.opencode.digsigcik.synapi.service.ESUserService;
import su.opencode.digsigcik.synapi.service.SignatureService;
import su.opencode.digsigcik.synapi.util.JwtTokenUtils;

import java.io.IOException;
import java.time.LocalDateTime;

import static su.opencode.digsigcik.synapi.controller.ApiControllerV1.DEFAULT_FILENAME;
import static su.opencode.digsigcik.synapi.dscp.ContainerTypeEnum.SIMPLE_ATTACHED;
import static su.opencode.digsigcik.synapi.dscp.ContainerTypeEnum.SIMPLE_DETACHED;
import static su.opencode.digsigcik.synapi.dscp.SignatureTypeEnum.QUALIFIED;
import static su.opencode.digsigcik.synapi.enums.SignatureType.CAdES_BES;
import static su.opencode.digsigcik.synapi.util.CommonUtils.filename;
import static su.opencode.digsigcik.synapi.util.Constants.API_RESPONSE_CODE_400_MESSAGE;
import static su.opencode.digsigcik.synapi.util.Constants.JWT_USER_NOT_FOUND;
import static su.opencode.digsigcik.synapi.util.FileUtils.resourceOctetStreamOrInternal;

@RestController
@SesExceptionHandler
@RequiredArgsConstructor
@RequestMapping("/api/v1/signtature")
public class SignatureController {
    private final SignatureService signatureService;
    private final JwtTokenUtils jwtTokenUtils;
    private final ESUserService esUserService;

    @PostMapping(value = "/sign-by-pkcs12",
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> createSignatureFromPkcs12(@RequestHeader(HttpHeaders.AUTHORIZATION) String authToken,
                                                              @RequestParam String passwordPkcs12,
                                                              SignatureType signatureType,
                                                              HashAlgorithm hashAlgorithm,
                                                              @RequestParam(defaultValue = "true") boolean signatureDetached,
                                                              @RequestPart("file") MultipartFile file,
                                                              @RequestPart("filePkcs12") MultipartFile filePkcs12
    ) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException(API_RESPONSE_CODE_400_MESSAGE);
        }
        if (filePkcs12 == null || filePkcs12.isEmpty()) {
            throw new BadRequestException(API_RESPONSE_CODE_400_MESSAGE);
        }
        JwtPayloadDto jwtPayloadDto = extractAuthToken(authToken);

        LocalDateTime timestamp = LocalDateTime.now();
        ContainerInputInfoDto containerInputInfo = ContainerInputInfoDto.builder()
                .login(jwtPayloadDto.getPreferredUsername())
                .firstname(jwtPayloadDto.getFirstName())
                .lastname(jwtPayloadDto.getLastName())
                .patronymic(jwtPayloadDto.getPatronymic())
                .timestamp(timestamp)
                .containerType(signatureDetached ? SIMPLE_DETACHED : SIMPLE_ATTACHED)
                .signatureTypeEnum(QUALIFIED)
                .signatureType(signatureType != null ? signatureType : CAdES_BES)
                .signatureDetached(signatureDetached)
                .hashAlgorithm(hashAlgorithm != null ? hashAlgorithm.getAlgorithmName() :
                        HashAlgorithm.GOST3411_2012_256.getAlgorithmName())
                .build();

        var res = signatureService.createSignatureResource(file, filePkcs12, passwordPkcs12, containerInputInfo);
        if (res == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
        return resourceOctetStreamOrInternal(res, filename(res, DEFAULT_FILENAME));
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

}

