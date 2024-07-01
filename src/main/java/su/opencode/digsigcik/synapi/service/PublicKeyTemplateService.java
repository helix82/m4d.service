package su.opencode.digsigcik.synapi.service;

import su.opencode.digsigcik.synapi.model.PublicKeyTemplate;

import javax.validation.constraints.NotNull;
import java.util.List;

public interface PublicKeyTemplateService {
    List<PublicKeyTemplate> findAllPublicKeyTemplate();

    PublicKeyTemplate savePublicKeyTemplate(@NotNull PublicKeyTemplate publicKeyTemplate);

    PublicKeyTemplate findByPublicKeyTemplateId(@NotNull Long id);

    PublicKeyTemplate updatePublicKeyTemplate(@NotNull PublicKeyTemplate publicKeyTemplate);

    void deletePublicKeyTemplate(@NotNull Long id);
}
