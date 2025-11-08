package su.opencode.digsigcik.m4d.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.audit.AuditLogEventEnum;
import su.opencode.digsigcik.audit.AuditLogThis;
import su.opencode.digsigcik.common.exceptions.exception.custom.BadRequestException;
import su.opencode.digsigcik.m4d.dto.CredentialJsonDto;
import su.opencode.digsigcik.m4d.enums.FileLogicalTypeEnum;
import su.opencode.digsigcik.m4d.enums.FileMimeTypeEnum;
import su.opencode.digsigcik.m4d.model.Credential;
import su.opencode.digsigcik.m4d.model.M4dCredential;
import su.opencode.digsigcik.m4d.model.M4dFile;
import su.opencode.digsigcik.m4d.repository.CredentialRepository;
import su.opencode.digsigcik.m4d.repository.M4dCredentialRepository;
import su.opencode.digsigcik.m4d.service.CredentialService;
import su.opencode.digsigcik.m4d.service.M4dFileService;
import su.opencode.digsigcik.specification.SpecificationRequest;
import su.opencode.digsigcik.specification.SpecificationResponse;
import su.opencode.digsigcik.specification.service.SpecificationService;

import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static su.opencode.digsigcik.m4d.util.Constants.*;
import static su.opencode.digsigcik.m4d.util.ConvertUtils.convertStringToClass;

@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class CredentialServiceImpl implements CredentialService {

    private final CredentialRepository repository;
    private final SpecificationService specificationService;
    private final M4dFileService m4dFileService;
    private final M4dCredentialRepository m4dCredentialRepository;

    @Override
    public Iterable<Credential> findAllCredential() {
        return this.repository.findAll();
    }

    @Override
    public SpecificationResponse<Credential> findAllCredential(SpecificationRequest request) {
        return specificationService.getUsingSpecification(repository, request, Credential.class);
    }

    @Override
    @AuditLogThis(event = AuditLogEventEnum.M4D_CREDENTIAL_CREATE)
    public Credential saveCredential(@NotNull Credential credential) {
        return this.repository.save(credential);
    }

    @Override
    public Credential findByCredentialId(@NotNull Long id) {
        return this.repository.findCredentialById(id);
    }

    @Override
    public List<Credential> findCredentialByCodeOrName(@NotNull String codeOrName) {
        List<Credential> credentialList = new ArrayList<>();
        List<Credential> credential = this.repository.findCredentialByCodeContainingIgnoreCase(codeOrName);
        List<Credential> credential1 = this.repository.findCredentialByNameContainingIgnoreCase(codeOrName);
        if (credential != null){ credentialList.addAll(credential); }
        if (credential1 != null){ credentialList.addAll(credential1); }
        return credentialList;
    }

    @Override
    public List<Credential> findCredentialByCode(@NotNull String code) {
        return this.repository.findCredentialByCodeIgnoreCase(code);
    }

    @Override
    public List<Credential> findCredentialByMrpaId(@NotNull Long mrpaId) {
        return this.repository.findCredentialByMrpaId(mrpaId);
    }

    @Override
    @AuditLogThis(event = AuditLogEventEnum.M4D_CREDENTIAL_UPDATE)
    public Credential updateCredential(@NotNull Credential credential) {
        return this.repository.save(credential);
    }

    @Override
    @AuditLogThis(event = AuditLogEventEnum.M4D_CREDENTIAL_DELETE, deleteByIdRepository = CredentialRepository.class)
    public void deleteCredential(@NotNull Long id) {
        this.repository.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class}, isolation = Isolation.READ_UNCOMMITTED)
    @AuditLogThis(event = AuditLogEventEnum.M4D_CREDENTIAL_UPLOAD)
    public Integer uploadFromJson(MultipartFile sourceFile) throws IOException {
        CredentialJsonDto[] credentials = convertStringToClass(new String(sourceFile.getBytes()), CredentialJsonDto[].class);

        int credentials_import = 0;

        if(credentials == null) {
            throw new BadRequestException(CREDENTIAL_NOT_FOUND);
        }

        for(CredentialJsonDto credential : credentials){
            List<Credential> credentialList = this.repository.findCredentialByCodeIgnoreCase(credential.getCode());
            if (credentialList != null && !credentialList.isEmpty()) {
                if (credentialList.size() > 1) {
                    Credential firstCredential = credentialList.get(0);
                    for (int i = 1; i < credentialList.size(); i++) {
                        try {
                            List<M4dCredential> m4dCredentialList = this.m4dCredentialRepository.findM4dCredentialByCredentialId(credentialList.get(i).getId());
                            if (m4dCredentialList != null && !m4dCredentialList.isEmpty()) {
                                for (M4dCredential m4dCredential: m4dCredentialList) {
                                    m4dCredential.setCredentialId(firstCredential.getId());
                                    this.m4dCredentialRepository.save(m4dCredential);
                                }
                            }
                            this.repository.delete(credentialList.get(i));
                            log.info(DUPLICATE_DELETE + ": {}", credentialList.get(i).getId());
                        } catch (Exception e) {
                            log.error(DUPLICATE_DELETE_ERROR + ": {}", e.getMessage());
                        }
                    }
                    if (!firstCredential.getName().equals(credential.getDescription())) {
                        firstCredential.setName(credential.getDescription());
                        this.repository.save(firstCredential);
                    }
                }
            } else {
                Credential credentialNew = new Credential();
                credentialNew.setCode(credential.getCode());
                credentialNew.setName(credential.getDescription());
                try {
                    this.repository.save(credentialNew);
                    credentials_import++;
                } catch (Exception e) {
                    log.error(CREDENTIAL_SAVE_ERROR + ": {}", e.getMessage());
                }
            }
        }

        try {
            this.saveJsonFile(sourceFile);
            //notificationService.notifyIokUpdateToPvv(sourceFile);
        } catch (Exception e) {
            log.error(FILE_SAVE_CREDENTIAL_ERROR + ": {}", e.getMessage());
        }
        return credentials_import;
    }

    private void saveJsonFile(MultipartFile multipartFile) throws IOException {
        M4dFile file = this.m4dFileService.findM4dFileByFilename("authority.json");
        if (file != null) {
            this.m4dFileService.deleteM4dFile(file.getId());
        }
        M4dFile m4dFile = this.m4dFileService.fillM4dFile(FileMimeTypeEnum.JSON, FileLogicalTypeEnum.DOCUMENT, multipartFile.getOriginalFilename(), multipartFile.getBytes());
        M4dFile m4dFileSaved;
        try {
            m4dFileSaved = this.m4dFileService.saveM4dFile(m4dFile);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(e.getMessage());
        }
        if (m4dFileSaved == null) {
            log.error(FILE_SAVE_CREDENTIAL_ERROR);
        }
    }
}
