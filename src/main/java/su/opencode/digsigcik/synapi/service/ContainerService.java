package su.opencode.digsigcik.synapi.service;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.api.model.ContainerSelector;
import su.opencode.digsigcik.synapi.api.model.RegistrationData;
import su.opencode.digsigcik.synapi.api.model.StampPosition;

import java.util.List;
import java.util.Map;

public interface ContainerService {

    Resource createPdfContainer(
            @NotNull MultipartFile sourceFile,
            @NotNull MultipartFile sourceSig,
            boolean createSigStamp,
            @Nullable StampPosition stampPosition);

    Resource updatePdfContainerRegData(
            @NotNull MultipartFile containerFile,
            @NotNull RegistrationData regData);

    Map<String, byte[]> getContainerData(
            @NotNull MultipartFile containerFile,
            @NotNull List<ContainerSelector> selectors,
            boolean reverify);

    byte[] getContainerData(
            @NotNull MultipartFile containerFile,
            @NotNull ContainerSelector selector,
            boolean reverify);
}
