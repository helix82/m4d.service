package su.opencode.digsigcik.synapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.synapi.exception.BadRequestException;
import su.opencode.digsigcik.synapi.exception.MchdException;
import su.opencode.digsigcik.synapi.model.PublicKeyUserCertificate;
import su.opencode.digsigcik.synapi.repository.PublicKeyUserCertificateRepository;

import javax.validation.constraints.NotNull;
import java.util.List;

import static su.opencode.digsigcik.synapi.util.Constant.NOT_FOUND;
import static su.opencode.digsigcik.synapi.util.Constant.NULL;

@Service
@Primary
@RequiredArgsConstructor
public class PublicKeyUserCertificateServiceImpl implements PublicKeyUserCertificateService{
    private final PublicKeyUserCertificateRepository repository;

    @Override
    public List<PublicKeyUserCertificate> findAllPublicKeyUserCertificate() {
        return this.repository.findAll();
    }

    @Override
    public PublicKeyUserCertificate savePublicKeyUserCertificate(@NotNull PublicKeyUserCertificate publicKeyUserCertificate) {
        return this.repository.save(publicKeyUserCertificate);
    }

    @Override
    public PublicKeyUserCertificate findByPublicKeyUserCertificateId(@NotNull Long id) {
        PublicKeyUserCertificate publicKeyUserCertificate = this.repository.findPublicKeyUserCertificateById(id);
        if (publicKeyUserCertificate == null) {
            throw new MchdException(String.format("User certificate %s", NOT_FOUND));
        }
        return publicKeyUserCertificate;
    }

    @Override
    public PublicKeyUserCertificate updatePublicKeyUserCertificate(@NotNull PublicKeyUserCertificate publicKeyUserCertificate) {
        Long publicKeyUserCertificateId = publicKeyUserCertificate.getId();
        if (publicKeyUserCertificateId == null) {
            throw new BadRequestException(String.format("Can`t update user certificate because id %s!", NULL));
        }

        PublicKeyUserCertificate existPublicKeyUserCertificate = this.repository.findPublicKeyUserCertificateById(publicKeyUserCertificateId);
        if (existPublicKeyUserCertificate == null) {
            throw new MchdException(String.format("Can`t update. User certificate with id=%d %s", publicKeyUserCertificateId, NOT_FOUND));
        }
        return this.repository.save(publicKeyUserCertificate);
    }

    @Override
    public Integer countPublicKeyUserCertificateByImprint(String imprint){
        return this.repository.countPublicKeyUserCertificateByImprint(imprint);
    }

}
