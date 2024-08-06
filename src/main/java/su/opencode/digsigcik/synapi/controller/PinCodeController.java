package su.opencode.digsigcik.synapi.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import su.opencode.digsigcik.synapi.dto.PinCodeDto;
import su.opencode.digsigcik.synapi.enums.HashAlgorithm;
import su.opencode.digsigcik.synapi.exception.BadRequestException;
import su.opencode.digsigcik.synapi.model.PinCode;
import su.opencode.digsigcik.synapi.model.UserProjection;
import su.opencode.digsigcik.synapi.repository.PinCodeRepository;
import su.opencode.digsigcik.synapi.repository.UserProjectionRepository;
import su.opencode.digsigcik.synapi.service.PinCodeService;
import su.opencode.digsigcik.synapi.util.HashUtils;

import javax.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static su.opencode.digsigcik.synapi.util.Constants.*;

@RestController
@RequestMapping("/api/v1/pincode")
@AllArgsConstructor
public class PinCodeController {
    private final PinCodeRepository pinCodeRepository;
    private final PinCodeService pinCodeService;
    private final UserProjectionRepository userProjectionRepository;


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
    @PostMapping(value = "save", produces = "application/json; charset=UTF-8")
    public ResponseEntity<PinCode> createPinCode(@RequestBody PinCodeDto pinCodeDto) {
        return createPinCodeWithCustomHash(HashAlgorithm.SHA256, pinCodeDto.getLogin(), pinCodeDto.getPinCode());
    }

    @Operation(summary = CONTROLLER_PIN_CODE_CREATE_WITH_CUSTOM_HASH,
            description = CONTROLLER_PIN_CODE_CREATE_WITH_CUSTOM_HASH)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 400, message = API_RESPONSE_CODE_400_MESSAGE)

    })
    @PostMapping(value = "/customize", produces = "application/json; charset=UTF-8")
    public ResponseEntity<PinCode> createPinCodeWithCustomHash(@RequestParam @Parameter(name = "hashAlgorithm") HashAlgorithm hashAlgorithm,
                                                               @RequestParam @Parameter(name = "login") final String login,
                                                               @RequestParam @Parameter(name = "pinCode") final String pinCode) {
        if (login == null || pinCode == null || login.trim()
                .isEmpty() || pinCode.trim()
                .isEmpty()) {
            throw new BadRequestException(API_RESPONSE_CODE_400_MESSAGE);
        }
        UserProjection userProjection = userProjectionRepository.getUserProjectionByLogin(login);

        if (userProjection == null) {
            userProjection = userProjectionRepository.save(UserProjection.builder()
                    .login(login)
                    .build());
        }
        if (!pinCodeRepository.existsPinCodeByUserProjection(userProjection)) {
            return new ResponseEntity<>(pinCodeService.createPinCode(PinCode.builder()
                    .userProjection(userProjection)
                    .algorithm(hashAlgorithm)
                    .pinCodeHash(HashUtils.generateHash(pinCode.getBytes(StandardCharsets.UTF_8), hashAlgorithm))
                    .build()), HttpStatus.OK);
        } else
            throw new BadRequestException(API_RESPONSE_CODE_400_MESSAGE);

    }

    @Operation(summary = CONTROLLER_PIN_CODE_UPDATE,
            description = CONTROLLER_PIN_CODE_UPDATE)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 400, message = API_RESPONSE_CODE_400_MESSAGE)
    })
    @PutMapping(value = "update", produces = "application/json; charset=UTF-8")
    public ResponseEntity<PinCode> updatePinCode(@RequestBody PinCodeDto pinCodeDto) {
        return updatePinCodeWithCustomHash(HashAlgorithm.SHA256, pinCodeDto.getId(), pinCodeDto.getPinCode());

    }

    @Operation(summary = CONTROLLER_PIN_CODE_UPDATE_WITH_CUSTOM_HASH,
            description = CONTROLLER_PIN_CODE_UPDATE_WITH_CUSTOM_HASH)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 400, message = API_RESPONSE_CODE_400_MESSAGE)
    })
    @PutMapping(value = "/customize", produces = "application/json; charset=UTF-8")
    public ResponseEntity<PinCode> updatePinCodeWithCustomHash(@RequestParam @Parameter(name = "hashAlgorithm") HashAlgorithm hashAlgorithm,
                                                               @RequestParam @Parameter(name = "id") final Long id,
                                                               @RequestParam @Parameter(name = "pinCode") String pinCode) {
        if (id == null || pinCode == null || pinCode.trim().isEmpty()) {
            throw new BadRequestException(API_RESPONSE_CODE_400_MESSAGE);
        }
        UserProjection userProjection = userProjectionRepository.getUserProjectionById(id);
        if (userProjection != null) {
            PinCode pinCode1 = pinCodeRepository.getPinCodeByUserProjection(userProjection);
            if (pinCode1 != null) {
                pinCode1.setAlgorithm(hashAlgorithm);
                pinCode1.setPinCodeHash(HashUtils.generateHash(pinCode.getBytes(StandardCharsets.UTF_8),
                        hashAlgorithm));
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
    @DeleteMapping(value = "delete/{id}", produces = "application/json; charset=UTF-8")
    @Transactional
    public ResponseEntity<?> deletePinCode(@PathVariable @Parameter(name = "id") Long id) {
        if (id == null) {
            throw new BadRequestException(API_RESPONSE_CODE_400_MESSAGE);
        }

        UserProjection userProjection = userProjectionRepository.getUserProjectionById(id);
        if (userProjection != null) {
            PinCode pinCode1 = pinCodeRepository.getPinCodeByUserProjection(userProjection);
            if (pinCode1 != null) {
                pinCodeRepository.deletePinCodeByUserProjection(userProjection);
                Map<String, String> body = new HashMap<>();
                body.put("result", "true");
                return new ResponseEntity<>(body, HttpStatus.OK);
            }
        }
        throw new BadRequestException(API_RESPONSE_CODE_400_MESSAGE);
    }

}