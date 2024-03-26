package su.opencode.digsigcik.synapi.controller;

import lombok.AllArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.api.model.ContainerSelector;
import su.opencode.digsigcik.synapi.api.model.RegistrationData;
import su.opencode.digsigcik.synapi.api.model.StampPosition;
import su.opencode.digsigcik.synapi.service.ContainerService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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
        var headers = resourceOctetStreamHeaders(res);
        return resourceOrInternal(res, headers);
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
        var headers = resourceOctetStreamHeaders(res);
        return resourceOrInternal(res, headers);
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
        var headers = resourceOctetStreamHeaders(resource);
        return resourceOrInternal(resource, headers);
    }

    private static HttpHeaders resourceOctetStreamHeaders(Resource resource) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", resource.getFilename());
        return headers;
    }

    private static ResponseEntity<Resource> resourceOrInternal(
            Resource resource,
            HttpHeaders headers
    ) {
        try {
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(resource.contentLength())
                    .body(resource);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
