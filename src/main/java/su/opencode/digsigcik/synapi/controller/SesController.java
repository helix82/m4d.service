package su.opencode.digsigcik.synapi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.dto.SesResponseDto;
import su.opencode.digsigcik.synapi.service.SesService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ses")
public class SesController {
    private final SesService sesService;

    @PostMapping("/create")
    public ResponseEntity<?> createSes(@RequestHeader(HttpHeaders.AUTHORIZATION) String authToken,
                                       @RequestParam String pinCode, @RequestPart("file") MultipartFile file) {
        SesResponseDto sesResponseDto = sesService.generateSesXmlFile(pinCode, file, authToken);
        return sesResponseDto.getResponseSesXml() == null ?
                new ResponseEntity<>(sesResponseDto.getError(), sesResponseDto.getStatus()) :
                new ResponseEntity<>(sesResponseDto.getResponseSesXml(), HttpStatus.OK);
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifySes(@RequestParam String operationId, @RequestPart("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.OK).body(sesService.verifySes(operationId, file));
    }
}
