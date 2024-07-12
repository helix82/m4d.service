package su.opencode.digsigcik.synapi.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import su.opencode.digsigcik.synapi.annotation.MchdExceptionHandler;
import su.opencode.digsigcik.synapi.dto.JwtPayloadDto;
import su.opencode.digsigcik.synapi.dto.UserDto;
import su.opencode.digsigcik.synapi.exception.BadRequestException;
import su.opencode.digsigcik.synapi.model.PublicKeyUserCertificate;
import su.opencode.digsigcik.synapi.service.ESUserService;
import su.opencode.digsigcik.synapi.service.PublicKeyUserCertificateService;
import su.opencode.digsigcik.synapi.util.JwtTokenUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static su.opencode.digsigcik.synapi.util.Constant.JWT_USER_NOT_FOUND;
import static su.opencode.digsigcik.synapi.util.Constants.*;

@RestController
@MchdExceptionHandler
@RequestMapping("/api/v1/about_me" )
@AllArgsConstructor
@Slf4j
public class AboutMeController {
    private final JwtTokenUtils jwtTokenUtils;
    private final ESUserService esUserService;
    private final PublicKeyUserCertificateService userCertificateService;

    @Operation(summary = CONTROLLER_ABOUT_ME_SUMMARY, description = CONTROLLER_ABOUT_ME_DESCRIPTION )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @GetMapping()
    public ResponseEntity<?> getUserData(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || authHeader.trim()
                .isEmpty()) {
            throw new BadRequestException(API_RESPONSE_CODE_400_MESSAGE);
        }
        JwtPayloadDto jwtPayloadDto = jwtTokenUtils.parseJwtPayload(authHeader);
        UserDto userDto = esUserService.getESSUserByLogin(jwtPayloadDto.getPreferredUsername());
        if (userDto == null) {
            throw new BadRequestException(JWT_USER_NOT_FOUND);
        }
        String login = userDto.getLogin();
        List<PublicKeyUserCertificate> publicKeyUserCertificates = userCertificateService.findAllPublicKeyUserCertificateByLogin(login);

        return new ResponseEntity<>(userDto.getLogin(), HttpStatus.OK);
    }
}
