package su.opencode.digsigcik.synapi.controller;

import lombok.AllArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.api.model.ContainerSelector;
import su.opencode.digsigcik.synapi.api.model.RegistrationData;
import su.opencode.digsigcik.synapi.api.model.StampPosition;
import su.opencode.digsigcik.synapi.service.ContainerService;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1")
@AllArgsConstructor
public class ApiControllerV1 {

    private final ContainerService containerService;

    @PostMapping(value = "/create-pdf-container",
                 produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> createPdfContainer(
            @RequestPart("sourceFile") MultipartFile sourceFile,
            @RequestPart("sourceSig") MultipartFile sourceSig,
            @RequestPart("createSigStamp") boolean createSigStamp,
            @RequestPart(value = "sigStampPosition", required = false) StampPosition stampPosition
    ) {
        var res = containerService.createPdfContainer(
                sourceFile, sourceSig, createSigStamp, stampPosition);
        if (res == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return resourceOctetStreamOrInternal(res);
    }

    @PutMapping(value = "/update-pdf-container-regdata",
                produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> updatePdfContainerRegData(
            @RequestPart("container") MultipartFile container,
            @RequestPart("regData") RegistrationData regData
    ) {
        var res = containerService.updatePdfContainerRegData(container, regData);
        if (res == null) {
            return ResponseEntity.internalServerError().build();
        }
        return resourceOctetStreamOrInternal(res);
    }

    @PostMapping(value = "/get-container-data",
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, byte[]>> getContainerData(
            @RequestPart("container") MultipartFile container,
            @RequestPart("selectors") List<ContainerSelector> selectors,
            @RequestPart(value = "reverify", required = false) Boolean reverify
    ) {
        if (reverify == null) reverify = Boolean.FALSE;
        var res = containerService.getContainerData(container, selectors, reverify);
        if (res == null) {
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok(res);
    }

    @PostMapping(value = "/get-container-part",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource> getContainerData(
            @RequestPart("container") MultipartFile container,
            @RequestPart("selector") ContainerSelector selector,
            @RequestPart(value = "reverify", required = false) Boolean reverify
    ) {
        if (reverify == null) reverify = Boolean.FALSE;
        var res = containerService.getContainerData(container, selector, reverify);
        if (res == null) {
            return ResponseEntity.internalServerError().build();
        }
        var resource = new ByteArrayResource(res);
        return resourceOctetStreamOrInternal(resource);
    }

    private static ResponseEntity<Resource> resourceOctetStreamOrInternal(
            Resource resource
    ) {
        try {
            var filename = Objects.requireNonNull(resource.getFilename(),
                    "empty resource filename");
            var disposition = ContentDisposition.attachment()
                    .filename(filename)
                    .build();
            return ResponseEntity.ok()
                    .contentLength(resource.contentLength())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                    .body(resource);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
