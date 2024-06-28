package su.opencode.digsigcik.synapi.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.annotation.SesExceptionHandler;
import su.opencode.digsigcik.synapi.dto.JwtPayloadDto;
import su.opencode.digsigcik.synapi.dto.SesResponseDto;
import su.opencode.digsigcik.synapi.dto.SesVerifyJsonDto;
import su.opencode.digsigcik.synapi.dto.UserDto;
import su.opencode.digsigcik.synapi.exception.BadRequestException;
import su.opencode.digsigcik.synapi.exception.SesException;
import su.opencode.digsigcik.synapi.service.ESUserService;
import su.opencode.digsigcik.synapi.service.SimpleSignatureService;
import su.opencode.digsigcik.synapi.util.JwtTokenUtils;

import static su.opencode.digsigcik.synapi.util.Constant.JWT_USER_NOT_FOUND;
import static su.opencode.digsigcik.synapi.util.Constants.*;

@RestController
@SesExceptionHandler
@RequiredArgsConstructor
@RequestMapping("/api/v1/ses")
public class SimpleSignatureController {
    private final SimpleSignatureService simpleSignatureService;
    private final JwtTokenUtils jwtTokenUtils;
    private final ESUserService esUserService;

    @Operation(summary = CONTROLLER_SIMPLE_SIGNATURE_CREATE,
            description = CONTROLLER_SIMPLE_SIGNATURE_CREATE)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 400, message = API_RESPONSE_CODE_400_MESSAGE)
    })
    @PostMapping(value = "/create")
    public ResponseEntity<?> createSimpleSignature(@RequestHeader(HttpHeaders.AUTHORIZATION) String authToken,
                                       @RequestParam String pinCode, @RequestPart("file") MultipartFile file) {

        if (authToken == null || authToken.trim()
                .isEmpty()) {
            throw new BadRequestException(API_RESPONSE_CODE_400_MESSAGE);
        }

        if (pinCode == null || pinCode.trim()
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

        SesResponseDto sesResponseDto = simpleSignatureService.createSimpleSignature(pinCode, file, jwtPayloadDto);

        return new ResponseEntity<>(sesResponseDto, HttpStatus.OK);
    }
    @Operation(summary = CONTROLLER_SIMPLE_SIGNATURE_VERIFY,
            description = CONTROLLER_SIMPLE_SIGNATURE_VERIFY)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 400, message = API_RESPONSE_CODE_400_MESSAGE)
    })
    @PostMapping(value = "/verify")
    public ResponseEntity<?> verifySimpleSignature(@RequestPart("file") MultipartFile file) {

        SesVerifyJsonDto sesVerifyResponse;
        try {
            sesVerifyResponse = simpleSignatureService.verifySimpleSignature(file);
        } catch (Exception e) {
            throw new BadRequestException(API_RESPONSE_CODE_400_MESSAGE);
        }

        return new ResponseEntity<>(sesVerifyResponse, HttpStatus.OK);
    }
}
