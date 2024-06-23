package su.opencode.digsigcik.synapi.service;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.api.model.ContainerPart;
import su.opencode.digsigcik.synapi.api.model.ContainerSelector;
import su.opencode.digsigcik.synapi.api.model.RegistrationData;
import su.opencode.digsigcik.synapi.api.model.StampPosition;
import su.opencode.digsigcik.synapi.dto.ContainerUserInfoDto;

import java.util.List;
import java.util.Map;

public interface ContainerService {

    Resource createPdfContainer(
	    @NotNull MultipartFile srcFile,
	    @NotNull MultipartFile sigFile,
        @NotNull MultipartFile pubFile,
        @NotNull ContainerUserInfoDto containerUserInfo,
        @NotNull String containerHashType);

    Resource updatePdfContainerRegData(
            @NotNull MultipartFile containerFile,
            @NotNull RegistrationData regData);

    Map<String, ContainerPart> getContainerData(
            @NotNull MultipartFile containerFile,
            @NotNull List<ContainerSelector> selectors,
            boolean reverify);

    ContainerPart getContainerPart(
            @NotNull MultipartFile containerFile,
            @NotNull ContainerSelector selector,
            boolean reverify);
}
