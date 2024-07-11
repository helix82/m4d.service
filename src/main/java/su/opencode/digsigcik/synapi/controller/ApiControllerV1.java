package su.opencode.digsigcik.synapi.controller;

import lombok.AllArgsConstructor;
import lombok.val;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.api.model.ContainerPart;
import su.opencode.digsigcik.synapi.api.model.ContainerSelector;
import su.opencode.digsigcik.synapi.api.model.RegistrationData;
import su.opencode.digsigcik.synapi.dto.ContainerUserInfoDto;
import su.opencode.digsigcik.synapi.dto.JwtPayloadDto;
import su.opencode.digsigcik.synapi.enums.HashAlgorithm;
import su.opencode.digsigcik.synapi.enums.SignatureAlgorithm;
import su.opencode.digsigcik.synapi.service.ContainerService;
import su.opencode.digsigcik.synapi.util.JwtTokenUtils;

import java.io.IOException;
import java.lang.reflect.Array;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
@AllArgsConstructor
public class ApiControllerV1 {
    private static final Log log = LogFactory.getLog(ApiControllerV1.class);
    public static final String DEFAULT_FILENAME = "container.dscp";
    private final ContainerService containerService;
    private final JwtTokenUtils jwtTokenUtils;

    @PostMapping(value = "/create-pdf-container",
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> createPdfContainer(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestPart("srcFile") MultipartFile sourceFile,
            @RequestPart("sigFile") MultipartFile sourceSig) {
        JwtPayloadDto jwtPayloadDto = jwtTokenUtils.parseJwtPayload(token);

        ContainerUserInfoDto containerUserInfo = ContainerUserInfoDto.builder()
                .login(jwtPayloadDto.getPreferredUsername())
                .firstname(jwtPayloadDto.getFirstName())
                .lastname(jwtPayloadDto.getLastName())
                .patronymic(jwtPayloadDto.getPatronymic())
                .timestamp(LocalDateTime.now())
                .signatureAlgorithm("NOT IMPLEMENTED")
                .build();
        var res = containerService.createPdfContainer(sourceFile, sourceSig, containerUserInfo);
        if (res == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return resourceOctetStreamOrInternal(res, filename(res, DEFAULT_FILENAME));
    }

    @PostMapping(value = "/create-pdf-container-custom",
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> createPdfContainerCustom(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            SignatureAlgorithm signatureAlgorithm,
            HashAlgorithm hashAlgorithm,
            @RequestPart("srcFile") MultipartFile sourceFile,
            @RequestPart("sigFile") MultipartFile sourceSig) {
        JwtPayloadDto jwtPayloadDto = jwtTokenUtils.parseJwtPayload(token);

        ContainerUserInfoDto containerUserInfo = ContainerUserInfoDto.builder()
                .login(jwtPayloadDto.getPreferredUsername())
                .firstname(jwtPayloadDto.getFirstName())
                .lastname(jwtPayloadDto.getLastName())
                .patronymic(jwtPayloadDto.getPatronymic())
                .timestamp(LocalDateTime.now())
                .signatureAlgorithm(signatureAlgorithm.getAlgorithmName())
                .hashAlgorithm(hashAlgorithm.getAlgorithmName())
                .build();
        var res = containerService.createPdfContainer(sourceFile, sourceSig, containerUserInfo);
        if (res == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return resourceOctetStreamOrInternal(res, filename(res, DEFAULT_FILENAME));
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
        return resourceOctetStreamOrInternal(res, filename(res, DEFAULT_FILENAME));
    }

    @PostMapping(value = "/get-container-data",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, ContainerPart>> getContainerData(
            @RequestPart("container") MultipartFile container,
            @RequestPart("selectors") List<ContainerSelector> selectors,
            @RequestPart(value = "reverify", required = false) Boolean reverify
    ) {
        if (reverify == null) reverify = Boolean.FALSE;
        var res = containerService.getContainerData(
                container, selectors, reverify);
        if (res == null) {
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok(res);
    }

    @PostMapping(value = "/test")
    public ResponseEntity<Map<String, ContainerPart>> test(
            @RequestPart("container") MultipartFile container,
            @RequestPart("selectors") List<su.opencode.digsigcik.synapi.api.model.ContainerSelectorCode> selectors,
            @RequestParam(value = "reverify", required = false) Boolean reverify
    ) {

        if (reverify == null) reverify = Boolean.FALSE;
        var res = containerService.getContainerData(
                container, selectors.stream().map(ApiControllerV1::createContainerSelector).collect(Collectors.toList()), reverify);
        if (res == null) {
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok(res);
    }

    @PostMapping(value = "/get-container-part")
    public ResponseEntity<Resource> getContainerPart(
            @RequestPart("container") MultipartFile container,
           su.opencode.digsigcik.synapi.api.model.ContainerSelectorCode selector,
            @RequestPart(value = "reverify", required = false) Boolean reverify
    ) {
        ContainerSelector containerSelector = createContainerSelector(selector);
        if (reverify == null) reverify = Boolean.FALSE;
        var res = containerService.getContainerPart(
                container, containerSelector, reverify);
        if (res == null) {
            return ResponseEntity.internalServerError().build();
        }
        var resource = new ByteArrayResource(res.getData());
        var contentType = mediaType(res.getContentType());
        var filename = filename(container, containerSelector.getCode().getValue());
        return resourceOctetStreamOrInternal(resource, contentType, filename);
    }

    public static ResponseEntity<Resource> resourceOctetStreamOrInternal(
            @NotNull Resource resource,
            @NotNull String filename
    ) {
        var contentType = MediaType.APPLICATION_OCTET_STREAM;
        return resourceOctetStreamOrInternal(resource, contentType, filename);
    }

    public static ResponseEntity<Resource> resourceOctetStreamOrInternal(
            @NotNull Resource resource,
            @NotNull MediaType contentType,
            @NotNull String filename
    ) {
        try {
            var disposition = ContentDisposition.attachment()
                    .filename(filename)
                    .build();
            return ResponseEntity.ok()
                    .contentLength(resource.contentLength())
                    .contentType(contentType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                    .body(resource);
        } catch (IOException e) {
            log.error("Could not create octet-stream from resource: "
                    + "resource cannot be resolved", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @SuppressWarnings("SameParameterValue")
    public static String filename(@NotNull Resource resource,
                                   @NotNull String fallback) {
        return Optional.ofNullable(resource.getFilename()).orElse(fallback);
    }

    private static String filename(@NotNull MultipartFile file,
                                   @Nullable String prefix) {
        String s = Optional.ofNullable(file.getOriginalFilename())
                .orElse(file.getName());
        return addPrefix(s, prefix);
    }

    private static String addPrefix(@NotNull String s,
                                    @Nullable String prefix) {
        return prefix == null ? s : String.join("_", prefix, s);
    }

    private static MediaType mediaType(@NotNull String type) {
        try {
            return MediaType.valueOf(type);
        } catch (InvalidMediaTypeException e) {
            throw new IllegalStateException("Invalid media type: " + type);
        }
    }
    private static ContainerSelector createContainerSelector(su.opencode.digsigcik.synapi.api.model.ContainerSelectorCode containerSelectorCode) {
        val selector = new ContainerSelector();
        selector.setCode(containerSelectorCode);
        return selector;
    }

}
