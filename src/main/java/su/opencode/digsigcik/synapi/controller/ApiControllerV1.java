package su.opencode.digsigcik.synapi.controller;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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
import su.opencode.digsigcik.synapi.api.model.ContainerSelector;
import su.opencode.digsigcik.synapi.api.model.RegistrationData;
import su.opencode.digsigcik.synapi.dscp.ContainerSelectorCode;
import su.opencode.digsigcik.synapi.dto.ContainerUserInfoDto;
import su.opencode.digsigcik.synapi.dto.JwtPayloadDto;
import su.opencode.digsigcik.synapi.enums.HashAlgorithm;
import su.opencode.digsigcik.synapi.enums.SignatureAlgorithm;
import su.opencode.digsigcik.synapi.exception.BadRequestException;
import su.opencode.digsigcik.synapi.service.ContainerService;
import su.opencode.digsigcik.synapi.util.JwtTokenUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static su.opencode.digsigcik.synapi.util.Constants.API_RESPONSE_CODE_400_MESSAGE;

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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
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
            return ResponseEntity.internalServerError()
                    .build();
        }
        return resourceOctetStreamOrInternal(res, filename(res, DEFAULT_FILENAME));
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class ContainerDataDTO {
        private MultipartFile container;
        private List<ContainerSelectorCode> selectors;
        private boolean reverify = false;
    }

    @PostMapping(value = "/get-container-data", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> getContainerData(@ModelAttribute ContainerDataDTO containerDataDTO
    ) {

        var res = containerService.getContainerData(
                containerDataDTO.getContainer(), containerDataDTO.getSelectors().stream()
                        .map(ApiControllerV1::createContainerSelector)
                        .collect(Collectors.toList()), containerDataDTO.isReverify());
        if (res == null) {
            return ResponseEntity.internalServerError()
                    .build();
        }
        return ResponseEntity.ok(res);
    }

    @PostMapping(value = "/get-container-data-json", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = "application/json")
    @ResponseBody
    public Map<String, String> getContainerDataJson(@ModelAttribute ContainerDataDTO containerDataDTO
    ) {

        Map<String, String> res = containerService.getContainerDataJson(
                containerDataDTO.getContainer(), containerDataDTO.getSelectors().stream()
                        .map(ApiControllerV1::createContainerSelector)
                        .collect(Collectors.toList()), containerDataDTO.isReverify());
        if (res == null) {
            throw new BadRequestException(API_RESPONSE_CODE_400_MESSAGE);
        }
        return res;
    }

    @PostMapping(value = "/get-container-part")
    public ResponseEntity<?> getContainerPart(
            @RequestPart("container") MultipartFile container,
            ContainerSelectorCode selector,
            @RequestPart(value = "reverify", required = false) Boolean reverify
    ) {
        ContainerSelector containerSelector = createContainerSelector(selector);
        if (reverify == null)
            reverify = Boolean.FALSE;
        var res = containerService.getContainerPart(
                container, containerSelector, reverify);
        if (res == null) {
            return ResponseEntity.internalServerError()
                    .build();
        }
        var resource = new ByteArrayResource(res.getData());
        var contentType = mediaType(res.getContentType());
        var filename = res.getFileName();
        if (filename == null) {
            return ResponseEntity.internalServerError()
                    .build();
        }
        return resourceOctetStreamOrInternal(resource, contentType, filename);
    }
    @PostMapping(value = "/get-container-part-json")
    @ResponseBody
    public Map<String, String> getContainerPartJson(
            @RequestPart("container") MultipartFile container,
            ContainerSelectorCode selector,
            @RequestPart(value = "reverify", required = false) Boolean reverify
    ) {
        ContainerSelector containerSelector = createContainerSelector(selector);
        if (reverify == null)
            reverify = Boolean.FALSE;
        var res = containerService.getContainerPartJson(
                container, containerSelector, reverify);
        if (res == null) {
            throw new BadRequestException(API_RESPONSE_CODE_400_MESSAGE);
        }
        return res;
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
            return ResponseEntity.internalServerError()
                    .build();
        }
    }

    @SuppressWarnings("SameParameterValue")
    public static String filename(@NotNull Resource resource,
                                  @NotNull String fallback) {
        return Optional.ofNullable(resource.getFilename())
                .orElse(fallback);
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

    private static ContainerSelector createContainerSelector(ContainerSelectorCode containerSelectorCode) {
        val selector = new ContainerSelector();
        selector.setCode(containerSelectorCode);
        return selector;
    }

}
