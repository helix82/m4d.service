package su.opencode.digsigcik.synapi.service;

import su.opencode.digsigcik.synapi.model.PublicKeyStatement;

import javax.validation.constraints.NotNull;
import java.util.List;

public interface PublicKeyStatementService {
    List<PublicKeyStatement> findAllPublicKeyStatement();

    PublicKeyStatement savePublicKeyStatement(@NotNull PublicKeyStatement publicKeyStatement);

    PublicKeyStatement findByPublicKeyStatementId(@NotNull Long id);

    PublicKeyStatement updatePublicKeyStatement(@NotNull PublicKeyStatement publicKeyStatement);

    void deletePublicKeyStatement(@NotNull Long id);
}
