package su.opencode.digsigcik.synapi.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.api.model.ContainerPart;
import su.opencode.digsigcik.synapi.api.model.ContainerSelector;
import su.opencode.digsigcik.synapi.api.model.RegistrationData;
import su.opencode.digsigcik.synapi.dscp.*;
import su.opencode.digsigcik.synapi.dto.ContainerInputInfoDto;
import su.opencode.digsigcik.synapi.util.CommonUtils;
import su.opencode.digsigcik.synapi.util.DateUtils;
import su.opencode.digsigcik.synapi.util.FileUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static su.opencode.digsigcik.synapi.util.Constants.DSCP_DIRECTORY_PATH;
import static su.opencode.digsigcik.synapi.util.Constants.ROOT_FILE_STORAGE_PATH;
import static su.opencode.digsigcik.synapi.util.FileUtils.toBytes;

@Service
@AllArgsConstructor
@Slf4j
public class ContainerServiceImpl implements ContainerService {
    private final StampService stampService;

    private final Map<ContainerSelectorCode, ResultTypeEnum> SELECTOR_MAP = new EnumMap<>(ContainerSelectorCode.class);

    @PostConstruct
    void init() {
        for (var sc : ContainerSelectorCode.values()) {
            var code = sc.getValue();
            var rt = InvariantUtils.getValueByCode(code, ResultTypeEnum.class);
            SELECTOR_MAP.put(sc, rt);
        }
    }

    @Override
    public Resource createContainer(@NotNull MultipartFile srcFile, @NotNull MultipartFile sigFile,
                                    @NotNull ContainerInputInfoDto containerInputInfo) {
        Map<ResultTypeEnum, byte[]> params = new HashMap<>();
        String fileName = srcFile.getOriginalFilename();
        String contentTypeStr = srcFile.getContentType();
        MediaType contentType = StringUtils.isBlank(contentTypeStr)
                || MediaType.APPLICATION_OCTET_STREAM_VALUE.equals(contentTypeStr)
                ? null
                : CommonUtils.toMediaType(contentTypeStr);

        byte[] sourceFileContent = getFileContent(srcFile);
        byte[] sigFileContent = getFileContent(sigFile);

        Container container = ContainerUtils.createContainer(containerInputInfo.getContainerType(), sourceFileContent,
                fileName, contentType, containerInputInfo.getHashAlgorithm());

        byte[] login = containerInputInfo.getLogin().getBytes(StandardCharsets.UTF_8);
        byte[] firstname = containerInputInfo.getFirstname().getBytes(StandardCharsets.UTF_8);
        byte[] lastname = containerInputInfo.getLastname().getBytes(StandardCharsets.UTF_8);
        byte[] patronymic = containerInputInfo.getPatronymic().getBytes(StandardCharsets.UTF_8);
        byte[] timestamp = containerInputInfo.getTimestamp().toString().getBytes(StandardCharsets.UTF_8);
        byte[] signatureAlgorithm = containerInputInfo.getSignatureAlgorithm().getBytes(StandardCharsets.UTF_8);
        byte[] signatureType = containerInputInfo.getSignatureType().getCode().getBytes(StandardCharsets.UTF_8);
        byte[] previewPdf = createPdfPreview(srcFile, containerInputInfo);
        byte[] previewPng = createPngPreview(srcFile, containerInputInfo);
        byte[] signCheck = containerInputInfo.getSignCheck();
        byte[] signReport = containerInputInfo.getSignReport();


        params.put(ResultTypeEnum.DETACHED_SIGN, sigFileContent);
        params.put(ResultTypeEnum.LOGIN, login);
        params.put(ResultTypeEnum.FIRSTNAME, firstname);
        params.put(ResultTypeEnum.LASTNAME, lastname);
        params.put(ResultTypeEnum.PATRONYMIC, patronymic);
        params.put(ResultTypeEnum.TIMESTAMP, timestamp);
        params.put(ResultTypeEnum.SIGNATURE_ALGORITHM, signatureAlgorithm);
        params.put(ResultTypeEnum.SIGNATURE_TYPE, signatureType);
        if (previewPdf!=null) {
            params.put(ResultTypeEnum.PREVIEW_PDF, previewPdf);
        }
        if (previewPng!=null) {
            params.put(ResultTypeEnum.PREVIEW_PNG, previewPng);
        }
        if (signCheck!=null) {
            params.put(ResultTypeEnum.SIGN_CHECK, signCheck);
        }
        if (signReport!=null) {
            params.put(ResultTypeEnum.SIGN_REPORT, signReport);
        }
        ContainerUtils.fillContainerByResult(container, params);

        byte[] containerBytes = ContainerUtils.containerContent(container);
        String dscpDirectoryPathName = String.format("%s%s", ROOT_FILE_STORAGE_PATH, DSCP_DIRECTORY_PATH);
        String containerPath = String.format("%scontainer-%s.dscp", dscpDirectoryPathName,
                DateUtils.formatDate(containerInputInfo.getTimestamp()));
        FileUtils.createDirectoryIfSheNotExist(dscpDirectoryPathName);
        FileUtils.createFile(containerPath, containerBytes);

        return new ByteArrayResource(containerBytes);
    }

    private byte[] getFileContent(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot get file content", e);
        }
    }


    private byte[] createPdfPreview(MultipartFile srcFile, ContainerInputInfoDto containerInputInfo) {
        try {
            return stampService.createPdfPreviewWithStamp(srcFile, containerInputInfo);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create pdf preview", e);
        }
    }

    private byte[] createPngPreview(MultipartFile srcFile, ContainerInputInfoDto containerInputInfo) {
        try {
            return stampService.createPngPreviewWithStamp(srcFile, containerInputInfo);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create png preview", e);
        }
    }

    @Override
    public Resource updateContainerRegData(@NotNull MultipartFile containerFile,
                                           @NotNull RegistrationData regData) {
        throw new IllegalArgumentException("Operation not supported yet");
    }

    @Override
    public Map<String, ContainerPart> getContainerData(@NotNull MultipartFile containerFile,
                                                       @NotNull List<ContainerSelector> selectors,
                                                       boolean reverify) {
        var rds = getRds(containerFile, selectors, reverify);
        var res = new HashMap<String, ContainerPart>();
        for (var rd : rds.entrySet()) {
            var resultType = rd.getValue().getResultType();
            var contentType = rd.getValue().getContentType();
            var fileName = rd.getValue().getFileName();
            var data = rd.getValue().getContent();
            var containerPart = new ContainerPart()
                    .contentType(contentType.toString())
                    .fileName(fileName)
                    .data(data);
            res.put(resultType.getCode(), containerPart);
        }
        return res;
    }

    private Map<ResultTypeEnum, ResultFileDescription> getRds(@NotNull MultipartFile containerFile,
                                                              @NotNull List<ContainerSelector> selectors,
                                                              boolean reverify) {
        byte[] containerBytes = toBytes(containerFile);
        var container = ContainerUtils.readContainer(containerBytes);
        if (reverify && shouldVerify(selectors)) {
            doReverify(container);
        }

        var returnParts = new ArrayList<ResultTypeEnum>(selectors.size());
        for (var s : selectors) {
            var rt = SELECTOR_MAP.get(s.getCode());
            if (rt == null) {
                throw new IllegalArgumentException("Invalid selector code " + s.getCode());
            }
            returnParts.add(rt);
        }

        return ContainerUtils.getFromContainer(container, returnParts, false);
    }

    @Override
    public Map<String, String> getContainerDataJson(@NotNull MultipartFile containerFile,
                                                    @NotNull List<ContainerSelector> selectors,
                                                    boolean reverify) {
        var rds = getRds(containerFile, selectors, reverify);
        var res = new HashMap<String, String>();
        for (var rd : rds.entrySet()) {
            var resultType = rd.getValue().getResultType();
            var data = rd.getValue().getContent();
            if (rd.getValue().getContentType().equals(MediaType.APPLICATION_OCTET_STREAM)
                    || rd.getValue().getContentType().equals(MediaType.APPLICATION_PDF)
                    || rd.getValue().getContentType().equals(MediaType.IMAGE_PNG)) {
                res.put(resultType.getCode(), Base64.getEncoder().encodeToString(data));
            } else {
                res.put(resultType.getCode(), new String(data));
            }
        }
        return res;
    }

    @Override
    public ContainerPart getContainerPart(@NotNull MultipartFile containerFile,
                                          @NotNull ContainerSelector selector,
                                          boolean reverify) {
        var res = getContainerData(containerFile, Collections.singletonList(selector), reverify);
        return res.get(selector.getCode().getValue());
    }

    @Override
    public Map<String, String> getContainerPartJson(@NotNull MultipartFile containerFile,
                                                    @NotNull ContainerSelector selector,
                                                    boolean reverify) {
        var res = new HashMap<String, String>();
        var containerData = getContainerData(containerFile, Collections.singletonList(selector), reverify);
        String contentType = containerData.get(selector.getCode().getValue()).getContentType();
        if (contentType.equals(MediaType.APPLICATION_OCTET_STREAM.toString())
                || contentType.equals(MediaType.APPLICATION_PDF.toString())
                || contentType.equals(MediaType.IMAGE_PNG.toString())) {
            res.put(selector.getCode().getValue(), Base64.getEncoder()
                    .encodeToString(containerData.get(selector.getCode().getValue()).getData()));
        } else {
            res.put(selector.getCode().getValue(), new String(containerData.get(selector.getCode().getValue()).getData()));
        }
        return res;
    }

    private boolean shouldVerify(List<ContainerSelector> selectors) {
        // TODO: return false if irrelevant to the selectors to avoid
        //  unnecessary reverification.
        return true;
    }

    private Container doReverify(Container container) {
        // TODO:
        throw new IllegalArgumentException("Operation not supported yet");
    }


    // FIXME: no validation
    private static Resource getStaticPdf() {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources;
        try {
            resources = resolver.getResources("file:files/dscp/*");
            if (resources.length > 0) {
                return resources[0];
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
