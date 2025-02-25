package su.opencode.digsigcik.m4d.service;

import su.opencode.digsigcik.m4d.model.M4dRequestSearch;

import javax.validation.constraints.NotNull;

public interface M4dRequestService {
    M4dRequestSearch findM4dRequestByGuidAndHash(@NotNull String guid, @NotNull String hash);
}
