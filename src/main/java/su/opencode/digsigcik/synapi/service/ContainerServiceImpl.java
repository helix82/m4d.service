package su.opencode.digsigcik.synapi.service;

import lombok.AllArgsConstructor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.InvalidMimeTypeException;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.api.model.*;
import su.opencode.digsigcik.synapi.dscp.*;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;

// TODO: add proper error handling, likely with exception handlers
@Service
@AllArgsConstructor
public class ContainerServiceImpl implements ContainerService {
    private static final Log LOG = LogFactory.getLog(ContainerServiceImpl.class);

    private final Map<ContainerSelectorCode, ResultTypeEnum> SELECTOR_MAP =
            new EnumMap<>(ContainerSelectorCode.class);

    @PostConstruct
    void init() {
        for (var sc : ContainerSelectorCode.values()) {
            var code = sc.getValue();
            var rt = InvariantUtils.getValueByCode(code, ResultTypeEnum.class);
            SELECTOR_MAP.put(sc, rt);
        }
    }

    @Override
    public Resource createPdfContainer(@NotNull MultipartFile sourceFile,
                                       @NotNull MultipartFile sourceSig,
                                       boolean createSigStamp,
                                       @Nullable StampPosition stampPosition) {
        return getStaticPdf();
    }

    @Override
    public Resource updatePdfContainerRegData(@NotNull MultipartFile containerFile,
                                              @NotNull RegistrationData regData) {
        throw new IllegalArgumentException("Operation not supported yet");
    }

    @Override
    public Map<String, ContainerPart> getContainerData(
            @NotNull MultipartFile containerFile,
            @NotNull List<ContainerSelector> selectors,
            boolean reverify
    ) {
        var container = toContainer(containerFile);
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

        List<ResultFileDescription> rds;
        try {
            rds = ContainerUtils.getFromContainer(container, returnParts,false, null);
        } catch (DscpContainerException e) {
            LOG.error(e.getMessage(), e);
            throw new IllegalStateException(e);
        }

        var res = new HashMap<String, ContainerPart>();
        for (var rd : rds) {
            if (rd == null) continue; // mandatory=false semantics
            var resultType = rd.getResultType();
            var contentType = toContentType(resultType, container);
            var data = rd.getContent();
            var containerPart = new ContainerPart()
                    .contentType(contentType)
                    .data(data);
            res.put(resultType.getCode(), containerPart);
        }
        return res;
    }

    @Override
    public ContainerPart getContainerPart(
            @NotNull MultipartFile containerFile,
            @NotNull ContainerSelector selector,
            boolean reverify
    ) {
        var res = getContainerData(
                containerFile,
                Collections.singletonList(selector),
                reverify);
        return res.get(selector.getCode().getValue());
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

    private String toContentType(ResultTypeEnum resultType, Container container) {
        var defaultContentType = "application/octet-stream";
        try {
            var contentType = resultType.getContentType(container);
            return contentType != null ? contentType : defaultContentType;
        } catch (DscpContainerException | InvalidMimeTypeException e) {
            return defaultContentType;
        }
    }

    private static Container toContainer(MultipartFile file) {
        byte[] containerBytes = toBytes(file);
        try {
            return ContainerUtils.createContainer(containerBytes);
        } catch (DscpContainerException e) {
            LOG.error(e.getMessage(), e);
            throw new IllegalStateException(e.getMessage());
        }
    }

    private static byte[] toBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new IllegalStateException("Could not read file into memory");
        }
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
