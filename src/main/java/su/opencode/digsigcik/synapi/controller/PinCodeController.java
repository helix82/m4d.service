package su.opencode.digsigcik.synapi.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import su.opencode.digsigcik.synapi.exception.BadRequestException;
import su.opencode.digsigcik.synapi.model.ESSUser;
import su.opencode.digsigcik.synapi.model.PinCode;
import su.opencode.digsigcik.synapi.repository.ESUserRepository;
import su.opencode.digsigcik.synapi.repository.PinCodeRepository;
import su.opencode.digsigcik.synapi.service.PinCodeService;
import su.opencode.digsigcik.synapi.util.HashUtils;

import javax.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static su.opencode.digsigcik.synapi.util.Constants.*;

@RestController
@RequestMapping("/api/v1/pincode")
public class PinCodeController {
    private final PinCodeRepository pinCodeRepository;
    private final PinCodeService pinCodeService;
    private final ESUserRepository esUserRepository;

    public PinCodeController(PinCodeService pinCodeService, ESUserRepository esUserRepository,
                             PinCodeRepository pinCodeRepository) {
        this.pinCodeService = pinCodeService;
        this.esUserRepository = esUserRepository;
        this.pinCodeRepository = pinCodeRepository;
    }


    @Operation(summary = CONTROLLER_PIN_CODE_GET_ALL,
            description = CONTROLLER_PIN_CODE_GET_ALL)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
    })
    @GetMapping("/")
    public List<PinCode> getAllPinCodes() {
        return pinCodeService.getAllPinCodes();
    }

    @Operation(summary = CONTROLLER_PIN_CODE_CREATE,
            description = CONTROLLER_PIN_CODE_CREATE)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 400, message = API_RESPONSE_CODE_400_MESSAGE)

    })
    @PostMapping(value = "/", produces = "application/json; charset=UTF-8")
    public ResponseEntity<PinCode> createPinCode(final String login, final String pinCode) {
        if (login == null || pinCode == null || login.trim()
                .isEmpty() || pinCode.trim()
                .isEmpty()) {
            throw new BadRequestException(API_RESPONSE_CODE_400_MESSAGE);
        }
        ESSUser essUser = esUserRepository.findESSUserByLogin(login);
        if (essUser != null) {
            PinCode pinCode1 = pinCodeRepository.getPinCodeByUser(essUser);
            if (pinCode1 == null) {
                pinCode1 = PinCode.builder()
                        .user(essUser)
                        .pinCodeHash(HashUtils.getSha256Hash(pinCode.getBytes(StandardCharsets.UTF_8)))
                        .build();
                return new ResponseEntity<>(pinCodeService.createPinCode(pinCode1), HttpStatus.OK);
            } else
                throw new BadRequestException(API_RESPONSE_CODE_400_MESSAGE);

        } else
            throw new BadRequestException(API_RESPONSE_CODE_400_MESSAGE);

    }

    @Operation(summary = CONTROLLER_PIN_CODE_UPDATE,
            description = CONTROLLER_PIN_CODE_UPDATE)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 400, message = API_RESPONSE_CODE_400_MESSAGE)
    })
    @PutMapping(value = "/", produces = "application/json; charset=UTF-8")
    public ResponseEntity<PinCode> updatePinCode(final String login, String pinCode) {
        if (login == null || pinCode == null || login.trim()
                .isEmpty() || pinCode.trim()
                .isEmpty()) {
            throw new BadRequestException(API_RESPONSE_CODE_400_MESSAGE);
        }
        ESSUser essUser = esUserRepository.findESSUserByLogin(login);
        if (essUser != null) {
            PinCode pinCode1 = pinCodeRepository.getPinCodeByUser(essUser);
            if (pinCode1 != null) {
                pinCode1.setPinCodeHash(HashUtils.getSha256Hash(pinCode.getBytes(StandardCharsets.UTF_8)));
                return new ResponseEntity<>(pinCodeService.updatePinCode(pinCode1), HttpStatus.OK);
            } else
                throw new BadRequestException(API_RESPONSE_CODE_400_MESSAGE);
        } else
            throw new BadRequestException(API_RESPONSE_CODE_400_MESSAGE);

    }

    @Operation(summary = CONTROLLER_PIN_CODE_DELETE,
            description = CONTROLLER_PIN_CODE_DELETE)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 400, message = API_RESPONSE_CODE_400_MESSAGE)
    })
    @DeleteMapping(value = "/", produces = "application/json; charset=UTF-8")
    @Transactional
    public ResponseEntity<?> deletePinCode(String login) {
        if (login == null || login.trim()
                .isEmpty()) {
            throw new BadRequestException(API_RESPONSE_CODE_400_MESSAGE);
        }
        ESSUser essUser = esUserRepository.findESSUserByLogin(login);
        if (essUser != null) {
            PinCode pinCode1 = pinCodeRepository.getPinCodeByUser(essUser);
            if (pinCode1 != null) {
                pinCodeService.deleteByUser(essUser);
                return new ResponseEntity<>("", HttpStatus.OK);
            } else
                throw new BadRequestException(API_RESPONSE_CODE_400_MESSAGE);
        } else
            throw new BadRequestException(API_RESPONSE_CODE_400_MESSAGE);
    }

}