package su.opencode.digsigcik.synapi.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.dto.VerifyDto;
import su.opencode.digsigcik.synapi.exception.BadRequestException;
import su.opencode.digsigcik.synapi.service.VerificationService;

import static su.opencode.digsigcik.synapi.util.Constants.*;

@RestController
@RequestMapping("/api/verify")
@Slf4j
@AllArgsConstructor
public class VerificationController {
    VerificationService verificationService;

    @Operation(summary = CONTROLLER_SIGNATURE_VERIFY,
            description = CONTROLLER_SIGNATURE_VERIFY)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 400, message = API_RESPONSE_CODE_400_MESSAGE)
    })
    @PostMapping()
    public ResponseEntity<?> verify(@RequestPart("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("Container is empty!");
        }
        VerifyDto sesVerifyResponse;
        try {
            sesVerifyResponse = verificationService.verify(file);
        } catch (Exception e) {
            throw new BadRequestException(API_RESPONSE_CODE_400_MESSAGE);
        }

        return new ResponseEntity<>(sesVerifyResponse, HttpStatus.OK);
    }

}