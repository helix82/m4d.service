package su.opencode.digsigcik.synapi.service;

import su.opencode.digsigcik.synapi.dto.PublicKeyStatementDto;
import su.opencode.digsigcik.synapi.dto.PublicKeyStatementRequestDto;
import su.opencode.digsigcik.synapi.dto.PublicKeyStatementUpdateRequestDto;
import su.opencode.digsigcik.synapi.model.PublicKeyStatement;

import javax.validation.constraints.NotNull;
import java.util.List;

public interface PublicKeyStatementService {
    List<PublicKeyStatement> findAllPublicKeyStatement();

    PublicKeyStatement savePublicKeyStatement(@NotNull PublicKeyStatement publicKeyStatement);

    PublicKeyStatement findByPublicKeyStatementId(@NotNull Long id);

    PublicKeyStatement updatePublicKeyStatement(@NotNull PublicKeyStatement publicKeyStatement);

    void deletePublicKeyStatement(@NotNull Long id);

    List<PublicKeyStatement> findAllPublicKeyStatementByLogin(@NotNull String login);

    PublicKeyStatementDto createPublicKeyStatementForUser(@NotNull PublicKeyStatementRequestDto dto, String login);

    PublicKeyStatementDto updatePublicKeyStatementForUserById(@NotNull PublicKeyStatementUpdateRequestDto dto, Long id, String login);

    boolean deletePublicKeyStatementForUserById(Long id, String login);
    PublicKeyStatementDto findPublicKeyStatementForUserById (Long id, String login);
}
