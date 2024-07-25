package su.opencode.digsigcik.synapi.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.bouncycastle.asn1.cms.CMSObjectIdentifiers;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.asn1.cms.SignedData;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.api.model.ContainerSelector;
import su.opencode.digsigcik.synapi.api.model.RegistrationData;
import su.opencode.digsigcik.synapi.dscp.ContainerSelectorCode;
import su.opencode.digsigcik.synapi.dscp.ContainerTypeEnum;
import su.opencode.digsigcik.synapi.dscp.SignatureTypeEnum;
import su.opencode.digsigcik.synapi.dto.ContainerInputInfoDto;
import su.opencode.digsigcik.synapi.dto.JwtPayloadDto;
import su.opencode.digsigcik.synapi.dto.ReportContainerDTO;
import su.opencode.digsigcik.synapi.dto.m4d.MrpaRegistryFullXML;
import su.opencode.digsigcik.synapi.enums.HashAlgorithm;
import su.opencode.digsigcik.synapi.enums.SignatureAlgorithm;
import su.opencode.digsigcik.synapi.exception.BadRequestException;
import su.opencode.digsigcik.synapi.service.ContainerService;
import su.opencode.digsigcik.synapi.service.ContainerVerificationService;
import su.opencode.digsigcik.synapi.service.SignatureVerificationService;
import su.opencode.digsigcik.synapi.util.JwtTokenUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static su.opencode.digsigcik.synapi.dscp.ContainerTypeEnum.*;
import static su.opencode.digsigcik.synapi.dscp.SignatureTypeEnum.QUALIFIED;
import static su.opencode.digsigcik.synapi.dscp.SignatureTypeEnum.UNQUALIFIED;
import static su.opencode.digsigcik.synapi.util.CommonUtils.filename;
import static su.opencode.digsigcik.synapi.util.CommonUtils.mediaType;
import static su.opencode.digsigcik.synapi.util.Constants.API_RESPONSE_CODE_400_MESSAGE;
import static su.opencode.digsigcik.synapi.util.FileUtils.resourceOctetStreamOrInternal;

@RestController
@RequestMapping("/api/v1")

@Slf4j
public class ApiControllerV1 {
    public static final String DEFAULT_FILENAME = "container.dscp";
    private final ContainerService containerService;
    private final ContainerVerificationService containerVerificationService;
    private final SignatureVerificationService signatureVerificationService;
    private final JwtTokenUtils jwtTokenUtils;

    public ApiControllerV1(ContainerService containerService,
                           ContainerVerificationService containerVerificationService,
                           @Qualifier("cryptopro-keylab") SignatureVerificationService signatureVerificationService,
                           JwtTokenUtils jwtTokenUtils) {
        this.containerService = containerService;
        this.containerVerificationService = containerVerificationService;
        this.signatureVerificationService = signatureVerificationService;
        this.jwtTokenUtils = jwtTokenUtils;
    }

    @PostMapping(value = "/create-pdf-container", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> createPdfContainer(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestPart("srcFile") MultipartFile sourceFile,
            @RequestPart("sigFile") MultipartFile sourceSig,
            SignatureTypeEnum signatureType,
            SignatureAlgorithm signatureAlgorithm,
            HashAlgorithm hashAlgorithm) {
        boolean signatureDetached;
        ContainerTypeEnum containerType;
        // Проверить, пустые ли файлы
        if (sourceFile == null || sourceFile.isEmpty()) {
            throw new BadRequestException(API_RESPONSE_CODE_400_MESSAGE);
        }
        if (sourceSig == null || sourceSig.isEmpty()) {
            throw new BadRequestException(API_RESPONSE_CODE_400_MESSAGE);
        }

        // Проверить, является ли подпись отсоединенной
        if (isDetachedSignature(sourceSig)) {
            signatureDetached = true;
            containerType = SIMPLE_DETACHED;
            if (isSourceFilePoa(sourceFile)) {
                containerType = POA_DETACHED;
            }
            if (isValidPdf(sourceFile)) {
                containerType = PDF_DETACHED;
            }
            if (isValidDocx(sourceFile)) {
                containerType = DOCX_DETACHED;
            }
        } else {
            signatureDetached = false;
            containerType = SIMPLE_ATTACHED;
            if (isSourceFilePoa(sourceFile)) {
                containerType = POA_ATTACHED;
            }
            if (isValidPdf(sourceFile)) {
                containerType = PDF_ATTACHED;
            }
            if (isValidDocx(sourceFile)) {
                containerType = DOCX_ATTACHED;
            }
        }

        // Выполнить проверку подписи
        ReportContainerDTO reportContainerDTO;
        try {
            if (signatureDetached) {
                reportContainerDTO = signatureVerificationService.verifyDetached(sourceFile, sourceSig);
            } else {
                reportContainerDTO = signatureVerificationService.verifyAttached(sourceSig);
            }
        } catch (ResourceAccessException | IOException | URISyntaxException e) {
            log.warn("Не удалось подключиться к сервису верификации: {}",e.getMessage());
            reportContainerDTO = ReportContainerDTO.builder()
                    .signReport(null)
                    .signCheck(null)
                    .build();
        } catch (Exception e) {
            log.warn("Не удалось подключиться к сервису верификации: {}",e.getMessage());
            reportContainerDTO = ReportContainerDTO.builder()
                    .signReport(null)
                    .signCheck(null)
                    .build();
        }

        // Получить информацию о токене JWT
        JwtPayloadDto jwtPayloadDto = jwtTokenUtils.parseJwtPayload(token);

        // Создать информацию о контейнере
        ContainerInputInfoDto containerInputInfo = ContainerInputInfoDto.builder()
                .login(jwtPayloadDto.getPreferredUsername())
                .firstname(jwtPayloadDto.getFirstName())
                .lastname(jwtPayloadDto.getLastName())
                .patronymic(jwtPayloadDto.getPatronymic())
                .timestamp(LocalDateTime.now())
                .signatureAlgorithm(signatureAlgorithm != null ? signatureAlgorithm.getAlgorithmName() : "NOT " +
                        "IMPLEMENTED")
                .hashAlgorithm(hashAlgorithm != null ? hashAlgorithm.getAlgorithmName() :
                        HashAlgorithm.GOST3411_2012_256.getAlgorithmName())
                .containerType(containerType)
                .signatureType(reportContainerDTO.isValid() ? QUALIFIED : UNQUALIFIED)
                .signCheck(reportContainerDTO.getSignCheck())
                .signReport(reportContainerDTO.getSignReport())
                .signatureDetached(signatureDetached)
                .build();

        // Создать контейнер
        var res = containerService.createContainer(sourceFile, sourceSig, containerInputInfo);

        return (res != null) ? resourceOctetStreamOrInternal(res, filename(res, DEFAULT_FILENAME))
                : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(null);
    }

    @PutMapping(value = "/update-pdf-container-regdata", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> updatePdfContainerRegData(
            @RequestPart("container") MultipartFile container,
            @RequestPart("regData") RegistrationData regData) {
        // Проверить, пустой ли файл
        if (container == null || container.isEmpty()) {
            throw new BadRequestException(API_RESPONSE_CODE_400_MESSAGE);
        }
        var res = containerService.updateContainerRegData(container, regData);

        return (res != null) ? resourceOctetStreamOrInternal(res, filename(res, DEFAULT_FILENAME))
                : ResponseEntity.internalServerError()
                .build();
    }


    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class ContainerDataDTO {
        private MultipartFile container;
        private List<ContainerSelectorCode> selectors;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class ContainerDataDTOWithReverify extends ContainerDataDTO {
        private boolean reverify = false;
    }

    @PostMapping(value = "/get-container-data", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE, MediaType.TEXT_PLAIN_VALUE,
                    MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> getContainerData(@ModelAttribute ContainerDataDTO containerDataDTO) {
        // Проверить, пустой ли файл
        if (containerDataDTO.getContainer() == null || containerDataDTO.getContainer()
                .isEmpty()) {
            throw new BadRequestException(API_RESPONSE_CODE_400_MESSAGE);
        }
        // Проверить, пустые ли селекторы
        if (containerDataDTO.getSelectors() == null) {
            throw new BadRequestException(API_RESPONSE_CODE_400_MESSAGE);
        }
        var res = containerService.getContainerData(
                containerDataDTO.getContainer(),
                containerDataDTO.getSelectors()
                        .stream()
                        .map(ApiControllerV1::createContainerSelector)
                        .collect(Collectors.toList()),
                false);

        return (res != null) ? ResponseEntity.ok(res)
                : ResponseEntity.internalServerError()
                .build();
    }


    @PostMapping(value = "/get-container-data-json", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces =
            MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getContainerDataJson(@ModelAttribute ContainerDataDTOWithReverify containerDataDTOWithReverify) {
        // Проверить, пустой ли файл
        if (containerDataDTOWithReverify.getContainer() == null || containerDataDTOWithReverify.getContainer()
                .isEmpty()) {
            throw new BadRequestException(API_RESPONSE_CODE_400_MESSAGE);
        }
        if (containerDataDTOWithReverify.isReverify()) {
            try {
                return new ResponseEntity<>(containerVerificationService.verifyContainer(containerDataDTOWithReverify.getContainer()),
                        HttpStatus.OK);
            } catch (Exception e) {
                throw new BadRequestException(API_RESPONSE_CODE_400_MESSAGE);
            }
        } else {
            // Проверить, пустые ли селекторы
            if (containerDataDTOWithReverify.getSelectors() == null) {
                throw new BadRequestException(API_RESPONSE_CODE_400_MESSAGE);
            }
        }

        Map<String, String> res = containerService.getContainerDataJson(
                containerDataDTOWithReverify.getContainer(),
                containerDataDTOWithReverify.getSelectors()
                        .stream()
                        .map(ApiControllerV1::createContainerSelector)
                        .collect(Collectors.toList()),
                containerDataDTOWithReverify.isReverify());

        if (res == null) {
            throw new BadRequestException(API_RESPONSE_CODE_400_MESSAGE);
        }

        return new ResponseEntity<>(res, HttpStatus.OK);
    }


    @PostMapping(value = "/get-container-data-zip", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE, MediaType.TEXT_PLAIN_VALUE,
                    MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> getContainerDataZip(@ModelAttribute ContainerDataDTO containerDataDTO) {
        // Проверить, пустой ли файл
        if (containerDataDTO.getContainer() == null || containerDataDTO.getContainer()
                .isEmpty()) {
            throw new BadRequestException(API_RESPONSE_CODE_400_MESSAGE);
        }
        // Проверить, пустые ли селекторы
        if (containerDataDTO.getSelectors() == null) {
            throw new BadRequestException(API_RESPONSE_CODE_400_MESSAGE);
        }
        Map<String, su.opencode.digsigcik.synapi.api.model.ContainerPart> res = containerService.getContainerData(
                containerDataDTO.getContainer(),
                containerDataDTO.getSelectors()
                        .stream()
                        .map(ApiControllerV1::createContainerSelector)
                        .collect(Collectors.toList()),
                false);

        if (res == null) {
            return ResponseEntity.internalServerError()
                    .build();
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            try (ZipOutputStream zipOutputStream = new ZipOutputStream(baos)) {
                addResourcesToZip(zipOutputStream, res);
            }

            byte[] zipBytes = baos.toByteArray();
            Resource zipResource = new InputStreamResource(new ByteArrayInputStream(zipBytes));

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=container.zip")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(zipResource);
        } catch (IOException e) {
            // Handle exception
            throw new RuntimeException("Error creating ZIP archive", e);
        }
    }


    @PostMapping(value = "/get-container-part")
    public ResponseEntity<Resource> getContainerPart(
            @RequestPart("container") MultipartFile container,
            ContainerSelectorCode selector) {
        // Проверить, пустой ли файл
        if (container == null || container.isEmpty()) {
            throw new BadRequestException(API_RESPONSE_CODE_400_MESSAGE);
        }
        // Проверить, пустые ли селекторы
        if (selector == null) {
            throw new BadRequestException(API_RESPONSE_CODE_400_MESSAGE);
        }
        ContainerSelector containerSelector = createContainerSelector(selector);
        var res = containerService.getContainerPart(container, containerSelector, false);

        if (res == null) {
            return ResponseEntity.noContent()
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
    public ResponseEntity<?> getContainerPartJson(
            @RequestPart("container") MultipartFile container,
            ContainerSelectorCode selector,
            Boolean reverify) {
        // Проверить, пустой ли файл
        if (container == null || container.isEmpty()) {
            throw new BadRequestException(API_RESPONSE_CODE_400_MESSAGE);
        }


        if (reverify == null) {
            reverify = Boolean.FALSE;
        }

        if (reverify) {
            try {
                return new ResponseEntity<>(containerVerificationService.verifyContainer(container), HttpStatus.OK);
            } catch (Exception e) {
                throw new BadRequestException(API_RESPONSE_CODE_400_MESSAGE);
            }
        } else {
            // Проверить, пустые ли селекторы
            if (selector == null) {
                throw new BadRequestException(API_RESPONSE_CODE_400_MESSAGE);
            }
        }

        ContainerSelector containerSelector = createContainerSelector(selector);
        var res = containerService.getContainerPartJson(container, containerSelector, reverify);

        if (res == null) {
            throw new BadRequestException(API_RESPONSE_CODE_400_MESSAGE);
        }

        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    static ContainerSelector createContainerSelector(ContainerSelectorCode containerSelectorCode) {
        val selector = new ContainerSelector();
        selector.setCode(containerSelectorCode);
        return selector;
    }

    private static void addResourcesToZip(ZipOutputStream zipOutputStream, Map<String,
            su.opencode.digsigcik.synapi.api.model.ContainerPart> res) throws IOException {
        for (var part : res.values()) {
            addResourceToZip(zipOutputStream, new ByteArrayResource(part.getData()), part.getFileName());
        }
    }

    private static void addResourceToZip(ZipOutputStream zipOutputStream, Resource resource, String filename) throws IOException {
        ZipEntry zipEntry = new ZipEntry(filename);
        zipOutputStream.putNextEntry(zipEntry);

        try (InputStream inputStream = resource.getInputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                zipOutputStream.write(buffer, 0, bytesRead);
            }
        }

        zipOutputStream.closeEntry();
    }

    private static boolean isSourceFilePoa(MultipartFile sourceFile) {
        return sourceFile.getOriginalFilename()
                .endsWith(".xml") && isMrpaRegistryFullXml(sourceFile);
    }

    private static boolean isMrpaRegistryFullXml(MultipartFile sourceFile) {
        try {
            JAXBContext context = JAXBContext.newInstance(MrpaRegistryFullXML.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            unmarshaller.unmarshal(sourceFile.getInputStream());
            return true;
        } catch (JAXBException | IOException e) {
            return false;
        }
    }

    public static boolean isDetachedSignature(MultipartFile multipartFile) {
        try {
            byte[] bytes = multipartFile.getBytes();
            ContentInfo contentInfo = ContentInfo.getInstance(bytes);
            if (!contentInfo.getContentType()
                    .equals(CMSObjectIdentifiers.signedData)) {
                throw new RuntimeException("Плохой файл подписи");
            }
            SignedData signedData = SignedData.getInstance(contentInfo.getContent());
            return signedData.getEncapContentInfo()
                    .getContent() == null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isValidPdf(MultipartFile multipartFile) {
        try {
            byte[] bytes = multipartFile.getBytes();
            if (bytes.length < 100) {
                return false;
            }
            if (!new String(bytes, 0, 5).equals("%PDF-")) {
                return false;
            }
            if (!new String(bytes, bytes.length - 5, 5).equals("%%EOF")) {
                return false;
            }
            PDDocument document = PDDocument.load(bytes);
            document.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean isValidDocx(MultipartFile multipartFile) {
        try {
            byte[] bytes = multipartFile.getBytes();
            OPCPackage opcPackage = OPCPackage.open(new ByteArrayInputStream(bytes));
            opcPackage.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
