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
import su.opencode.digsigcik.synapi.dto.AboutMeDto;
import su.opencode.digsigcik.synapi.dto.JwtPayloadDto;
import su.opencode.digsigcik.synapi.dto.MrpaRegistryFullDTO;
import su.opencode.digsigcik.synapi.dto.UserDto;
import su.opencode.digsigcik.synapi.enums.TypeStorageCertificateEnum;
import su.opencode.digsigcik.synapi.exception.BadRequestException;
import su.opencode.digsigcik.synapi.model.PublicKeyUserCertificate;
import su.opencode.digsigcik.synapi.model.UserProjection;
import su.opencode.digsigcik.synapi.repository.MrpaRegistryRepository;
import su.opencode.digsigcik.synapi.repository.PinCodeRepository;
import su.opencode.digsigcik.synapi.repository.UserProjectionRepository;
import su.opencode.digsigcik.synapi.service.ESUserService;
import su.opencode.digsigcik.synapi.service.MrpaRegistryService;
import su.opencode.digsigcik.synapi.service.PublicKeyUserCertificateService;
import su.opencode.digsigcik.synapi.util.JwtTokenUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

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
    private final MrpaRegistryService mrpaRegistryService;
    private final MrpaRegistryRepository mrpaRegistryRepository;
    private final UserProjectionRepository userProjectionRepository;
    private final PinCodeRepository pinCodeRepository;

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
        AboutMeDto aboutMeDto = new AboutMeDto();
        aboutMeDto.setName(userDto.getFirstname());
        aboutMeDto.setSurname(userDto.getLastname());
        aboutMeDto.setPatronymic(userDto.getPatronymic());
        aboutMeDto.setRole(userDto.getRole());
        UserProjection userProjection = userProjectionRepository.getUserProjectionByLogin(login);
        aboutMeDto.setSes(false);
        if (userProjection != null) {
            if(pinCodeRepository.existsPinCodeByUserProjection(userProjection)){
                aboutMeDto.setSes(true);
            }
        }
        List<PublicKeyUserCertificate> publicKeyUserCertificates = userCertificateService.findAllPublicKeyUserCertificateByLogin(login);
        if(publicKeyUserCertificates != null){
            List<AboutMeDto.Certificates> certificatesList = new ArrayList<>();
            for(PublicKeyUserCertificate s : publicKeyUserCertificates){
                AboutMeDto.Certificates certificates = new AboutMeDto.Certificates();
                certificates.setType(TypeStorageCertificateEnum.LOCAL);
                certificates.setThumbprint(s.getImprint());
                certificates.setOwnerName(s.getFio());
                certificates.setInn(s.getInn());
                certificates.setOrganization(s.getOrganization());
                certificates.setIssuerWho(s.getIssuerWho());
                certificates.setKeyId(s.getKeyId());
                certificates.setSerialNumber(s.getSerialNumber());
                certificates.setSignType(s.getType());
                certificates.setValidFrom(s.getDateStart());
                certificates.setValidTo(s.getDateEnd());
                certificates.setDateStartPrivateKey(s.getDateStartPrivateKey());
                certificates.setDateEndPrivateKey(s.getDateEndPrivateKey());
                if(s.getInn() != null){
                    MrpaRegistryFullDTO mrpaRegistryFullDTO = mrpaRegistryRepository.findMrpaRegistriesFullByPrincipalInn(s.getInn());
                    if(mrpaRegistryFullDTO != null) {
                        mrpaRegistryFullDTO.credential = mrpaRegistryService.getCredentialByMrpaRegistryFullId(mrpaRegistryFullDTO.getId());
                        certificates.setM4d(mrpaRegistryFullDTO);
                    }
                }
                certificatesList.add(certificates);
            }
            aboutMeDto.setCertificates(certificatesList);
        }


        return new ResponseEntity<>(aboutMeDto, HttpStatus.OK);
    }
}
