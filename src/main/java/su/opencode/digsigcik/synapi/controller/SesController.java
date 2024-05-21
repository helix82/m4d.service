package su.opencode.digsigcik.synapi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.service.SimpleElectronicSigService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ses")
public class SesController {
    private final SimpleElectronicSigService sigService;

    @PostMapping("/create")
    public ResponseEntity<?> createSes(@RequestParam String pinCode, @RequestPart("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.OK).body(sigService.getOperationId(pinCode, file));
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifySes(@RequestParam String login) {
        return ResponseEntity.status(HttpStatus.OK).body(sigService.verifySes(login));
    }
}
