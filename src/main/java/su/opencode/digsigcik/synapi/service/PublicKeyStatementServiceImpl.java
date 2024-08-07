package su.opencode.digsigcik.synapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import su.opencode.digsigcik.synapi.dto.PublicKeyStatementDto;
import su.opencode.digsigcik.synapi.dto.PublicKeyStatementRequestDto;
import su.opencode.digsigcik.synapi.dto.PublicKeyStatementUpdateRequestDto;
import su.opencode.digsigcik.synapi.exception.BadRequestException;
import su.opencode.digsigcik.synapi.exception.MchdException;
import su.opencode.digsigcik.synapi.mapper.PublicKeyStatementMapper;
import su.opencode.digsigcik.synapi.model.PublicKeyStatement;
import su.opencode.digsigcik.synapi.repository.PublicKeyStatementRepository;

import javax.persistence.EntityNotFoundException;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

import static su.opencode.digsigcik.synapi.util.Constants.*;

@Service
@Transactional
@Primary
@RequiredArgsConstructor
public class PublicKeyStatementServiceImpl implements PublicKeyStatementService {
    private final PublicKeyStatementRepository repository;
    private final PublicKeyStatementMapper mapper;
    private final static String UPDATE_FAIL  = "Can`t update. Statement with id=%d %s";

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

    @Override
    public List<PublicKeyStatement> findAllPublicKeyStatementByLogin(String login) {
        return repository.findAllByLogin(login);
    }

    @Override
    public PublicKeyStatementDto createPublicKeyStatementForUser(@NotNull PublicKeyStatementRequestDto dto, String login) {
        return Optional.of(dto)
                .map(dto1 -> mapper.map(dto1, login))
                .map(repository::save)
                .map(mapper::mapToDto)
                .orElseThrow(() -> new DataIntegrityViolationException(CERTIFICATE_FAILED_TO_SAVE));
    }

    @Override
    public PublicKeyStatementDto updatePublicKeyStatementForUserById(@NotNull PublicKeyStatementUpdateRequestDto dto,
                                                                  Long id,
                                                                  String login) {
       return repository.findByIdAndLogin(id, login)
                .map(entity -> mapper.updateMap(entity, dto))
                .map(repository::saveAndFlush)
                .map(mapper::mapToDto)
                .orElseThrow(() -> new EntityNotFoundException(String.format(UPDATE_FAIL, id, NOT_FOUND)));
    }

    @Override
    public boolean deletePublicKeyStatementForUserById (Long id, String login){
        return repository.findByIdAndLogin(id, login)
                .map(entity->{
                    repository.delete(entity);
                    repository.flush();
                    return true;
                }).orElse(false);
    }

    @Override
    public PublicKeyStatementDto findPublicKeyStatementForUserById (Long id, String login) {
        return repository.findByIdAndLogin(id,login)
                .map(mapper::mapToDto)
                .orElseThrow(()-> new EntityNotFoundException(NOT_FOUND));
    }
}
