package su.opencode.digsigcik.synapi.service;

import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.api.model.ContainerPart;
import su.opencode.digsigcik.synapi.api.model.ContainerSelector;
import su.opencode.digsigcik.synapi.dscp.ContainerSelectorCode;
import su.opencode.digsigcik.synapi.dscp.SignatureTypeEnum;
import su.opencode.digsigcik.synapi.dto.ReportDTO;
import su.opencode.digsigcik.synapi.exception.BadRequestException;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class ContainerVerificationServiceImpl implements ContainerVerificationService {

    private final ContainerService containerService;
    private final SignatureVerificationService signatureVerificationService;
    private final SignatureVerificationService signatureVerificationServiceBC;

    public ContainerVerificationServiceImpl(ContainerService containerService,
                                            @Qualifier("cryptopro-keylab") SignatureVerificationService signatureVerificationService, @Qualifier("bouncy-castle") SignatureVerificationService signatureVerificationServiceBC) {
        this.containerService = containerService;
        this.signatureVerificationService = signatureVerificationService;
        this.signatureVerificationServiceBC = signatureVerificationServiceBC;
    }


    public ReportDTO verifyContainer(@NotNull MultipartFile container) {
        List<ContainerSelector> containerSelectors = createContainerSelectors();
        try {
            var containerData = containerService.getContainerData(container, containerSelectors, false);

            if (containerData == null && !containerData.isEmpty()) {
                throw new BadRequestException("Verify ses error!");
            }

            String signatureType = getStringData(containerData, "signatureType");

            if (isQualifiedSignature(signatureType)) {
                return handleQualifiedSignature(containerData);
            } else if (isSimpleSignature(signatureType)) {
                return handleSimpleSignature(containerData);
            }
            return ReportDTO.builder()
                    .error("Не удалось определить тип подписи.")
                    .build();
        } catch (Exception e) {
            throw new BadRequestException("Verify ses error!", e);
        }
    }

    private List<ContainerSelector> createContainerSelectors() {
        return new ArrayList<>(List.of(
                createContainerSelector(ContainerSelectorCode.USERFIRSTNAME),
                createContainerSelector(ContainerSelectorCode.USERLASTNAME),
                createContainerSelector(ContainerSelectorCode.USERPATRONYMIC),
                createContainerSelector(ContainerSelectorCode.USERLOGIN),
                createContainerSelector(ContainerSelectorCode.TIMESTAMP),
                createContainerSelector(ContainerSelectorCode.SOURCE_FILE),
                createContainerSelector(ContainerSelectorCode.DETACHEDSIGN),
                createContainerSelector(ContainerSelectorCode.ATTACHEDSIGN),
                createContainerSelector(ContainerSelectorCode.HASHTYPE),
                createContainerSelector(ContainerSelectorCode.SIGNATUREALGORITHM),
                createContainerSelector(ContainerSelectorCode.SIGNATURE_TYPE),
                createContainerSelector(ContainerSelectorCode.SOURCE_FILE_FULL_NAME)
        ));
    }

    private String getStringData(Map<String, ContainerPart> containerData,
                                 String key) {
        return new String(containerData.get(key)
                .getData(), StandardCharsets.UTF_8);
    }

    private boolean isQualifiedSignature(String signatureType) {
        return signatureType.equals(SignatureTypeEnum.UNQUALIFIED.getCode())
                || signatureType.equals(SignatureTypeEnum.QUALIFIED.getCode())
                || signatureType.equals(SignatureTypeEnum.UNKNOWN.getCode());
    }

    private boolean isSimpleSignature(String signatureType) {
        return signatureType.equals(SignatureTypeEnum.SIMPLE.getCode());
    }
    private ReportDTO handleSimpleSignature(Map<String, ContainerPart> containerData) throws Exception {
        return verifySignature(containerData, signatureVerificationServiceBC);
    }
    private ReportDTO handleQualifiedSignature(Map<String, ContainerPart> containerData) throws Exception {
        return verifySignature(containerData, signatureVerificationService);
    }

    private ReportDTO verifySignature(Map<String, ContainerPart> containerData, SignatureVerificationService signatureVerificationService) throws Exception {
        byte[] detachedSign = getByteArrayData(containerData, "detachedSign");
        byte[] attachedSign = getByteArrayData(containerData, "attachedSign");
        byte[] srcFile = getByteArrayData(containerData, "sourceFile");
        String sourceFileFullName = getStringData(containerData, "sourceFileFullName");

        if (detachedSign.length > 0) {
            return signatureVerificationService.verifyDetached(srcFile, detachedSign, sourceFileFullName);
        } else if (attachedSign.length > 0) {
            return signatureVerificationService.verifyAttached(attachedSign, sourceFileFullName);
        } else {
            throw new IllegalArgumentException("Both detachedSign and attachedSign are empty");
        }
    }

    private byte[] getByteArrayData(Map<String, ContainerPart> containerData, String key) {
        if (containerData.containsKey(key)) {
            return containerData.get(key)
                    .getData();
        }
        return new byte[0];
    }

    private static ContainerSelector createContainerSelector(ContainerSelectorCode containerSelectorCode) {
        val selector = new ContainerSelector();
        selector.setCode(containerSelectorCode);
        return selector;
    }
}
