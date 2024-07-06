package su.opencode.digsigcik.synapi.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.annotation.SesExceptionHandler;
import su.opencode.digsigcik.synapi.dto.JwtPayloadDto;
import su.opencode.digsigcik.synapi.dto.SesResponseDto;
import su.opencode.digsigcik.synapi.dto.SesVerifyJsonDto;
import su.opencode.digsigcik.synapi.dto.UserDto;
import su.opencode.digsigcik.synapi.enums.HashAlgorithm;
import su.opencode.digsigcik.synapi.exception.BadRequestException;
import su.opencode.digsigcik.synapi.exception.SesException;
import su.opencode.digsigcik.synapi.exception.SesVerifyException;
import su.opencode.digsigcik.synapi.service.ESUserService;
import su.opencode.digsigcik.synapi.service.UnqualifiedSignatureService;
import su.opencode.digsigcik.synapi.util.JwtTokenUtils;

import java.io.IOException;

import static su.opencode.digsigcik.synapi.util.Constant.JWT_USER_NOT_FOUND;
import static su.opencode.digsigcik.synapi.util.Constants.*;

@RestController
@SesExceptionHandler
@RequiredArgsConstructor
@RequestMapping("/api/v1/ues")
public class UnqualifiedSignatureController {
    private final UnqualifiedSignatureService unqualifiedSignatureService;
    private final JwtTokenUtils jwtTokenUtils;
    private final ESUserService esUserService;

    @Operation(summary = CONTROLLER_UNQUALIFIED_SIGNATURE_CREATE,
            description = CONTROLLER_UNQUALIFIED_SIGNATURE_CREATE)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 400, message = API_RESPONSE_CODE_400_MESSAGE)
    })
    @PostMapping(value = "/create")
    public ResponseEntity<?> createUnqualifiedSignature(@RequestHeader(HttpHeaders.AUTHORIZATION) String authToken,
                                                        @RequestPart("file") MultipartFile file) {
        return createUnqualifiedSignatureWithCustomHash(authToken, HashAlgorithm.SHA256, file);
    }

    @Operation(summary = CONTROLLER_UNQUALIFIED_SIGNATURE_CREATE_WITH_CUSTOM_HASH,
            description = CONTROLLER_UNQUALIFIED_SIGNATURE_CREATE_WITH_CUSTOM_HASH)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 400, message = API_RESPONSE_CODE_400_MESSAGE)
    })
    @PostMapping(value = "/customize")
    public ResponseEntity<?> createUnqualifiedSignatureWithCustomHash(@RequestHeader(HttpHeaders.AUTHORIZATION) String authToken, HashAlgorithm hashAlgorithm, @RequestPart("file") MultipartFile file) {

        if (authToken == null || authToken.trim()
                .isEmpty()) {
            throw new BadRequestException(API_RESPONSE_CODE_400_MESSAGE);
        }

        if (file == null || file.isEmpty()) {
            throw new BadRequestException(API_RESPONSE_CODE_400_MESSAGE);
        }

        //получаем поля токена из payload
        JwtPayloadDto jwtPayloadDto = jwtTokenUtils.parseJwtPayload(authToken);
        String login = jwtPayloadDto.getPreferredUsername();

        UserDto userDto = esUserService.getESSUserByLogin(login);
        if (userDto == null) {
            throw new SesException(JWT_USER_NOT_FOUND);
        }

        SesResponseDto sesResponseDto;
        try {
            sesResponseDto = unqualifiedSignatureService.createUnqualifiedSignature(file, jwtPayloadDto, hashAlgorithm);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new ResponseEntity<>(sesResponseDto, HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_UNQUALIFIED_SIGNATURE_VERIFY,
            description = CONTROLLER_UNQUALIFIED_SIGNATURE_VERIFY)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 400, message = API_RESPONSE_CODE_400_MESSAGE)
    })
    @PostMapping(value = "/verify")
    public ResponseEntity<?> verifyUnqualifiedSignature(@RequestPart("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new SesVerifyException("Container is empty!");
        }

        SesVerifyJsonDto sesVerifyResponse = unqualifiedSignatureService.verifyUnqualifiedSignature(file);
        return new ResponseEntity<>(sesVerifyResponse, HttpStatus.OK);
    }

}
