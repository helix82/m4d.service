package su.opencode.digsigcik.m4d.service;

import su.opencode.digsigcik.m4d.model.M4dRequestSearch;
import su.opencode.digsigcik.m4d.model.M4dRequestSearchDetail;

import javax.validation.constraints.NotNull;

public interface M4dRequestService {
    M4dRequestSearch findM4dRequestByGuidAndHash(@NotNull String guid, @NotNull String hash);
    M4dRequestSearchDetail findM4dRequestDetailByM4dRequestId(@NotNull Long id);
    M4dRequestSearch saveM4dRequestSearch(M4dRequestSearch m4dRequestSearch);
    M4dRequestSearchDetail saveM4dRequestSearchDetail(M4dRequestSearchDetail m4dRequestSearchDetail);
    M4dRequestSearchDetail updateM4dRequestSearchDetail(M4dRequestSearchDetail m4dRequestSearchDetail);
}
