package su.opencode.digsigcik.synapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.synapi.exception.BadRequestException;
import su.opencode.digsigcik.synapi.exception.MchdException;
import su.opencode.digsigcik.synapi.model.PublicKeyStatement;
import su.opencode.digsigcik.synapi.repository.PublicKeyStatementRepository;

import javax.validation.constraints.NotNull;
import java.util.List;

import static su.opencode.digsigcik.synapi.util.Constants.NOT_FOUND;
import static su.opencode.digsigcik.synapi.util.Constants.NULL;

@Service
@Primary
@RequiredArgsConstructor
public class PublicKeyStatementServiceImpl implements PublicKeyStatementService{
    private final PublicKeyStatementRepository repository;

    @Override
    public List<PublicKeyStatement> findAllPublicKeyStatement() {
        return this.repository.findAll();
    }

    @Override
    public PublicKeyStatement savePublicKeyStatement(@NotNull PublicKeyStatement publicKeyStatement) {
        return this.repository.save(publicKeyStatement);
    }

    @Override
    public PublicKeyStatement findByPublicKeyStatementId(@NotNull Long id) {
        PublicKeyStatement publicKeyStatement = this.repository.findPublicKeyStatementById(id);
        if (publicKeyStatement == null) {
            throw new MchdException(String.format("Statement %s", NOT_FOUND));
        }
        return publicKeyStatement;
    }

    @Override
    public PublicKeyStatement updatePublicKeyStatement(@NotNull PublicKeyStatement publicKeyStatement) {
        Long publicKeyStatementId = publicKeyStatement.getId();
        if (publicKeyStatementId == null) {
            throw new BadRequestException(String.format("Can`t update Statement because id %s!", NULL));
        }

        PublicKeyStatement existPublicKeyStatement = this.repository.findPublicKeyStatementById(publicKeyStatementId);
        if (existPublicKeyStatement == null) {
            throw new MchdException(String.format("Can`t update. Statement with id=%d %s", publicKeyStatementId, NOT_FOUND));
        }
        return this.repository.save(publicKeyStatement);
    }

    @Override
    public void deletePublicKeyStatement(@NotNull Long id) {
        PublicKeyStatement publicKeyStatement = this.repository.findPublicKeyStatementById(id);
        if (publicKeyStatement == null) {
            throw new MchdException(String.format("Can`t delete Statement because Statement %s", NOT_FOUND));
        }
        this.repository.deleteById(id);
    }
}
