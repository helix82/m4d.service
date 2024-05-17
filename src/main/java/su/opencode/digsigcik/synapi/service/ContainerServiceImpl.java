package su.opencode.digsigcik.synapi.service;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.api.model.ContainerPart;
import su.opencode.digsigcik.synapi.api.model.ContainerSelector;
import su.opencode.digsigcik.synapi.api.model.ContainerSelectorCode;
import su.opencode.digsigcik.synapi.api.model.RegistrationData;
import su.opencode.digsigcik.synapi.api.model.StampPosition;
import su.opencode.digsigcik.synapi.dscp.*;
import su.opencode.digsigcik.synapi.util.CommonUtils;
import su.opencode.digsigcik.synapi.util.StubUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
	public Resource createPdfContainer(
		@NotNull MultipartFile srcFile, @NotNull MultipartFile sigFile,
		boolean createSigStamp, @Nullable StampPosition signPosition, @Nullable StampPosition regPosition) {

		String fileName = srcFile.getOriginalFilename();
		String contentTypeStr = srcFile.getContentType();
		MediaType contentType = StringUtils.isBlank(contentTypeStr)
			|| MediaType.APPLICATION_OCTET_STREAM_VALUE.equals(contentTypeStr)
			? null
			: CommonUtils.toMediaType(contentTypeStr);
		byte[] sourceFileContent;
		try {
			sourceFileContent = srcFile.getBytes();
		} catch (IOException e) {
			throw new IllegalStateException("Cannot get source file content", e);
		}
		byte[] sigFileContent;
		try {
			sigFileContent = sigFile.getBytes();
		} catch (IOException e) {
			throw new IllegalStateException("Cannot get sig file content", e);
		}


		Container container = ContainerUtils.createContainer(ContainerTypeEnum.PDF_DETACHED, sourceFileContent, fileName, contentType);
		if (signPosition == null) signPosition = StubUtils.createStampPosition(); //TODO: stub must be replaced
		assert signPosition != null;
		if (regPosition == null) regPosition = StubUtils.createStampPosition(); //TODO: stub must be replaced
		assert regPosition != null;
		byte[] certData = StubUtils.getStubFileContent("source/lastCertData.json"); //TODO: stub must be replaced
		assert certData != null && certData.length > 0;
		byte[] signCheckData = StubUtils.getStubFileContent("source/lastSignCheck.json"); //TODO: stub must be replaced
		assert signCheckData != null && signCheckData.length > 0;

		Map<ResultTypeEnum, byte[]> params = Map.of(
			ResultTypeEnum.DETACHED_SIGN, sigFileContent,
			ResultTypeEnum.STAMP_SIGN_PAGE, String.valueOf(signPosition.getPage()).getBytes(StandardCharsets.UTF_8),
			ResultTypeEnum.STAMP_SIGN_X, String.valueOf(signPosition.getTopLeftX()).getBytes(StandardCharsets.UTF_8),
			ResultTypeEnum.STAMP_SIGN_Y, String.valueOf(signPosition.getTopLeftY()).getBytes(StandardCharsets.UTF_8),
			ResultTypeEnum.STAMP_REG_PAGE, String.valueOf(regPosition.getPage()).getBytes(StandardCharsets.UTF_8),
			ResultTypeEnum.STAMP_REG_X, String.valueOf(regPosition.getTopLeftX()).getBytes(StandardCharsets.UTF_8),
			ResultTypeEnum.STAMP_REG_Y, String.valueOf(regPosition.getTopLeftY()).getBytes(StandardCharsets.UTF_8),
			ResultTypeEnum.SIGN_CHECK, signCheckData,
			ResultTypeEnum.CERT_DATA, certData
		);
		ContainerUtils.fillContainerByResult(container, params);

		return new ByteArrayResource(ContainerUtils.containerContent(container));
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

	    var rds = ContainerUtils.getFromContainer(container, returnParts, false);

	    var res = new HashMap<String, ContainerPart>();
	    for (var rd : rds.entrySet()) {

		    var resultType = rd.getValue().getResultType();
		    var contentType = rd.getValue().getContentType();
		    var data = rd.getValue().getContent();
		    var containerPart = new ContainerPart()
			    .contentType(contentType.toString())
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
