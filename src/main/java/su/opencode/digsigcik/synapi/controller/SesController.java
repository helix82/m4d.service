package su.opencode.digsigcik.synapi.controller;

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
import su.opencode.digsigcik.synapi.dto.SesResponseDto;
import su.opencode.digsigcik.synapi.dto.SesVerifyJsonDto;
import su.opencode.digsigcik.synapi.service.SesService;

@RestController
@SesExceptionHandler
@RequiredArgsConstructor
@RequestMapping("/api/v1/ses")
public class SesController {
    private final SesService sesService;

    @PostMapping(value = "/create")
    public ResponseEntity<?> createSes(@RequestHeader(HttpHeaders.AUTHORIZATION) String authToken,
                                       @RequestParam String pinCode, @RequestPart("file") MultipartFile file) {
        SesResponseDto sesResponseDto = sesService.getContainerPath(pinCode, file, authToken);

        return new ResponseEntity<>(sesResponseDto, HttpStatus.OK);
    }

    @PostMapping(value = "/verify")
    public ResponseEntity<?> verifySes(@RequestPart("file") MultipartFile file) {
        SesVerifyJsonDto sesVerifyResponse = sesService.verifySes(file);

        return new ResponseEntity<>(sesVerifyResponse, HttpStatus.OK);
    }
}
