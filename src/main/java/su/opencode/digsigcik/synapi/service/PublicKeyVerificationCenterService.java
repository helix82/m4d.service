package su.opencode.digsigcik.synapi.service;

import su.opencode.digsigcik.synapi.model.PublicKeyVerificationCenter;

import javax.validation.constraints.NotNull;
import java.util.List;

public interface PublicKeyVerificationCenterService {
    List<PublicKeyVerificationCenter> findAllPublicKeyVerificationCenter();

    PublicKeyVerificationCenter savePublicKeyVerificationCenter(@NotNull PublicKeyVerificationCenter publicKeyVerificationCenter);

    PublicKeyVerificationCenter findByPublicKeyVerificationCenterId(@NotNull Long id);

    PublicKeyVerificationCenter updatePublicKeyVerificationCenter(@NotNull PublicKeyVerificationCenter publicKeyVerificationCenter);

    void deletePublicKeyVerificationCenter(@NotNull Long id);

}
