package su.opencode.digsigcik.synapi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import su.opencode.digsigcik.synapi.service.SesContainerService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/sesContainer")
public class SesContainerController {
    private final SesContainerService sesContainerService;

    @GetMapping("/getContainerPath")
    public ResponseEntity<?> getXmlPathByUUID(@RequestParam String uuid) {
        return uuid == null || uuid.isEmpty() ?
                new ResponseEntity<>("UUID is null or empty!", HttpStatus.BAD_REQUEST) :
                new ResponseEntity<>(sesContainerService.getSesContainerPathByUUID(UUID.fromString(uuid)), HttpStatus.OK);
    }
}
