package su.opencode.digsigcik.m4d.service;

import su.opencode.digsigcik.m4d.model.M4dRequestSearch;

import javax.validation.constraints.NotNull;
import java.util.List;

public interface M4dRequestService {
    List<M4dRequestSearch> findAllM4dRequestSearch();
    M4dRequestSearch findM4dRequestByGuidAndHash(@NotNull String guid, @NotNull String hash);
    M4dRequestSearch findM4dRequestByGuid(@NotNull String guid);
    M4dRequestSearch saveM4dRequestSearch(M4dRequestSearch m4dRequestSearch);
    M4dRequestSearch updateM4dRequestSearch(M4dRequestSearch m4dRequestSearch);
    void deleteM4dRequestSearch(@NotNull String guid);
}
