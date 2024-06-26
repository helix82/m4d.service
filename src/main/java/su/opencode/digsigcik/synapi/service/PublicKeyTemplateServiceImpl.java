package su.opencode.digsigcik.synapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.synapi.exception.BadRequestException;
import su.opencode.digsigcik.synapi.exception.MchdException;
import su.opencode.digsigcik.synapi.model.PublicKeyTemplate;
import su.opencode.digsigcik.synapi.repository.PublicKeyTemplateRepository;

import javax.validation.constraints.NotNull;
import java.util.List;

import static su.opencode.digsigcik.synapi.util.Constant.NOT_FOUND;
import static su.opencode.digsigcik.synapi.util.Constant.NULL;

@Service
@Primary
@RequiredArgsConstructor
public class PublicKeyTemplateServiceImpl implements PublicKeyTemplateService{
    private final PublicKeyTemplateRepository repository;

    @Override
    public List<PublicKeyTemplate> findAllPublicKeyTemplate() {
        return this.repository.findAll();
    }

    @Override
    public PublicKeyTemplate savePublicKeyTemplate(@NotNull PublicKeyTemplate publicKeyTemplate) {
        return this.repository.save(publicKeyTemplate);
    }

    @Override
    public PublicKeyTemplate findByPublicKeyTemplateId(@NotNull Long id) {
        PublicKeyTemplate publicKeyTemplate = this.repository.findPublicKeyTemplateById(id);
        if (publicKeyTemplate == null) {
            throw new MchdException(String.format("Template %s", NOT_FOUND));
        }
        return publicKeyTemplate;
    }

    @Override
    public PublicKeyTemplate updatePublicKeyTemplate(@NotNull PublicKeyTemplate publicKeyTemplate) {
        Long publicKeyTemplateId = publicKeyTemplate.getId();
        if (publicKeyTemplateId == null) {
            throw new BadRequestException(String.format("Can`t update Template because id %s!", NULL));
        }

        PublicKeyTemplate existPublicKeyTemplate = this.repository.findPublicKeyTemplateById(publicKeyTemplateId);
        if (existPublicKeyTemplate == null) {
            throw new MchdException(String.format("Can`t update. Template with id=%d %s", publicKeyTemplateId, NOT_FOUND));
        }
        return this.repository.save(publicKeyTemplate);
    }

    @Override
    public void deletePublicKeyTemplate(@NotNull Long id) {
        PublicKeyTemplate publicKeyTemplate = this.repository.findPublicKeyTemplateById(id);
        if (publicKeyTemplate == null) {
            throw new MchdException(String.format("Can`t delete Template because Template %s", NOT_FOUND));
        }
        this.repository.deleteById(id);
    }
}
