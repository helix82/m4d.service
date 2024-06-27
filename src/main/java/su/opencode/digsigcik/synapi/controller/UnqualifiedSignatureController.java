package su.opencode.digsigcik.synapi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.annotation.SesExceptionHandler;
import su.opencode.digsigcik.synapi.dto.SesResponseDto;
import su.opencode.digsigcik.synapi.dto.SesVerifyJsonDto;
import su.opencode.digsigcik.synapi.enums.HashAlgorithm;
import su.opencode.digsigcik.synapi.service.SesService;
import su.opencode.digsigcik.synapi.service.UnqualifiedSignatureService;

import java.io.IOException;

@RestController
@SesExceptionHandler
@RequiredArgsConstructor
@RequestMapping("/api/v1/ues")
public class UnqualifiedSignatureController {
    private final UnqualifiedSignatureService unqualifiedSignatureService;

    @PostMapping(value = "/create")
    public ResponseEntity<?> createSes(@RequestHeader(HttpHeaders.AUTHORIZATION) String authToken, HashAlgorithm hashAlgorithm, @RequestPart("file") MultipartFile file) {
        SesResponseDto sesResponseDto = null;
        try {
            sesResponseDto = unqualifiedSignatureService.getContainerPath(file, authToken, hashAlgorithm);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new ResponseEntity<>(sesResponseDto, HttpStatus.OK);
    }

    @PostMapping(value = "/verify")
    public ResponseEntity<?> verifySes(@RequestPart("file") MultipartFile file) {
        SesVerifyJsonDto sesVerifyResponse = unqualifiedSignatureService.verifySes(file);

        return new ResponseEntity<>(sesVerifyResponse, HttpStatus.OK);
    }
}
