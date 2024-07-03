package su.opencode.digsigcik.synapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.synapi.exception.BadRequestException;
import su.opencode.digsigcik.synapi.exception.MchdException;
import su.opencode.digsigcik.synapi.model.PublicKeyVerificationCenter;
import su.opencode.digsigcik.synapi.repository.PublicKeyVerificationCenterRepository;

import javax.validation.constraints.NotNull;
import java.util.List;

import static su.opencode.digsigcik.synapi.util.Constant.NOT_FOUND;
import static su.opencode.digsigcik.synapi.util.Constant.NULL;

@Service
@Primary
@RequiredArgsConstructor
public class PublicKeyVerificationCenterServiceImpl implements PublicKeyVerificationCenterService {
    private final PublicKeyVerificationCenterRepository repository;

    @Override
    public List<PublicKeyVerificationCenter> findAllPublicKeyVerificationCenter() {
        return this.repository.findAll();
    }

    @Override
    public PublicKeyVerificationCenter savePublicKeyVerificationCenter(@NotNull PublicKeyVerificationCenter publicKeyVerificationCenter) {
        return this.repository.save(publicKeyVerificationCenter);
    }

    @Override
    public PublicKeyVerificationCenter findByPublicKeyVerificationCenterId(@NotNull Long id) {
        PublicKeyVerificationCenter publicKeyVerificationCenter = this.repository.findPublicKeyVerificationCenterById(id);
        if (publicKeyVerificationCenter == null) {
            throw new MchdException(String.format("Template %s", NOT_FOUND));
        }
        return publicKeyVerificationCenter;
    }

    @Override
    public PublicKeyVerificationCenter updatePublicKeyVerificationCenter(@NotNull PublicKeyVerificationCenter publicKeyVerificationCenter) {
        Long publicKeyVerificationCenterId = publicKeyVerificationCenter.getId();
        if (publicKeyVerificationCenterId == null) {
            throw new BadRequestException(String.format("Can`t update Verification Center because id %s!", NULL));
        }

        PublicKeyVerificationCenter existPublicKeyVerificationCenter = this.repository.findPublicKeyVerificationCenterById(publicKeyVerificationCenterId);
        if (existPublicKeyVerificationCenter == null) {
            throw new MchdException(String.format("Can`t update. Verification Center with id=%d %s", publicKeyVerificationCenterId, NOT_FOUND));
        }
        return this.repository.save(publicKeyVerificationCenter);
    }

    @Override
    public void deletePublicKeyVerificationCenter(@NotNull Long id) {
        PublicKeyVerificationCenter publicKeyVerificationCenter = this.repository.findPublicKeyVerificationCenterById(id);
        if (publicKeyVerificationCenter == null) {
            throw new MchdException(String.format("Can`t delete Verification Center because Verification Center %s", NOT_FOUND));
        }
        this.repository.deleteById(id);
    }
}
