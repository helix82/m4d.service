package su.opencode.digsigcik.m4d.service.impl;

import org.springframework.context.annotation.Primary;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.m4d.enums.FileLogicalTypeEnum;
import su.opencode.digsigcik.m4d.enums.FileMimeTypeEnum;
import su.opencode.digsigcik.m4d.exception.BadRequestException;
import su.opencode.digsigcik.m4d.model.M4dConfirm;
import su.opencode.digsigcik.m4d.model.M4dFile;
import su.opencode.digsigcik.m4d.repository.M4dConfirmRepository;
import su.opencode.digsigcik.m4d.service.ContainerService;
import su.opencode.digsigcik.m4d.service.M4dConfirmService;
import su.opencode.digsigcik.m4d.service.M4dFileService;
import su.opencode.digsigcik.m4d.util.XMLUtil;

import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.util.Base64;

import static su.opencode.digsigcik.m4d.util.Constants.MRPA_CONFIRM_IS_NULL;

@Service
@Primary
public class M4dConfirmServiceImpl implements M4dConfirmService {
    private final M4dConfirmRepository m4dConfirmRepository;
    private final M4dFileService m4dFileService;
    private final ContainerService containerService;

    public M4dConfirmServiceImpl(M4dConfirmRepository m4dConfirmRepository, M4dFileService m4dFileService, ContainerService containerService) {
        this.m4dConfirmRepository = m4dConfirmRepository;
        this.m4dFileService = m4dFileService;
        this.containerService = containerService;
    }

    @Override
    public M4dConfirm saveM4dConfirm(M4dConfirm m4dConfirm) {
        return this.m4dConfirmRepository.save(m4dConfirm);
    }

    @Override
    public M4dConfirm findM4dConfirmByM4dId(Long m4dId) {
        return this.m4dConfirmRepository.findM4dConfirmByM4dId(m4dId);
    }

    @Override
    public M4dConfirm updateM4dConfirm(M4dConfirm m4dConfirm) {
        return this.m4dConfirmRepository.save(m4dConfirm);
    }

    @Override
    public void deleteM4dConfirmById(Long id) {
        this.m4dConfirmRepository.deleteById(id);
    }

    @Override
    public Long uploadSigRegisteredPep(Authentication authentication, Long m4dId, MultipartFile container, boolean verify) throws IOException {
        M4dConfirm m4dConfirm = this.findM4dConfirmByM4dId(m4dId);
        if (m4dConfirm==null) {
            throw new BadRequestException(MRPA_CONFIRM_IS_NULL);
        }
        if (m4dConfirm.getSigFileRegisteredPep()!=null) {
            throw new BadRequestException("Подтверждение регистрации МЧД в ПЭП уже подписано");
        }
        if (verify) {
            containerService.verify(authentication, container, m4dConfirm.getXmlFileRegisteredPep().getContent());
        }
        M4dFile m4dFile = new M4dFile();
        m4dFile.setFileMimeType(FileMimeTypeEnum.fromValue(container.getContentType()));
        m4dFile.setFileLogicalType(FileLogicalTypeEnum.CONTAINER);
        m4dFile.setFilename(container.getOriginalFilename());
        String base64Content = Base64.getEncoder().encodeToString(container.getBytes());
        m4dFile.setContent(base64Content);
        M4dFile existsM4dFile;
        try {
            existsM4dFile = this.m4dFileService.findM4dFileByContent(m4dFile.getContent());
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(e.getMessage());
        }
        if (existsM4dFile == null){
            m4dConfirm.setSigFileRegisteredPep(m4dFile);
        } else {
            m4dConfirm.setSigFileRegisteredPep(existsM4dFile);
        }
        M4dConfirm m4dConfirmSaved;
        try {
            m4dConfirmSaved = this.saveM4dConfirm(m4dConfirm);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(e.getMessage());
        }
        return m4dConfirmSaved.getSigFileRegisteredPep().getId();
    }

    @Override
    public Long uploadSigRegisteredRr(Authentication authentication, Long m4dId, MultipartFile container, boolean verify) throws IOException {
        M4dConfirm m4dConfirm = this.findM4dConfirmByM4dId(m4dId);
        if (m4dConfirm==null) {
            throw new BadRequestException(MRPA_CONFIRM_IS_NULL);
        }
        if (m4dConfirm.getSigFileRegisteredRr()!=null) {
            throw new BadRequestException("Подтверждение регистрации МЧД в РР уже подписано");
        }
        if (verify) {
            containerService.verify(authentication, container, m4dConfirm.getXmlFileRegisteredRr().getContent());
        }
        M4dFile m4dFile = new M4dFile();
        m4dFile.setFileMimeType(FileMimeTypeEnum.fromValue(container.getContentType()));
        m4dFile.setFileLogicalType(FileLogicalTypeEnum.CONTAINER);
        m4dFile.setFilename(container.getOriginalFilename());
        String base64Content = Base64.getEncoder().encodeToString(container.getBytes());
        m4dFile.setContent(base64Content);
        M4dFile existsM4dFile;
        try {
            existsM4dFile = this.m4dFileService.findM4dFileByContent(m4dFile.getContent());
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(e.getMessage());
        }
        if (existsM4dFile == null){
            m4dConfirm.setSigFileRegisteredRr(m4dFile);
        } else {
            m4dConfirm.setSigFileRegisteredRr(existsM4dFile);
        }
        M4dConfirm m4dConfirmSaved;
        try {
            m4dConfirmSaved = this.saveM4dConfirm(m4dConfirm);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(e.getMessage());
        }
        return m4dConfirmSaved.getSigFileRegisteredRr().getId();
    }

    @Override
    public Long uploadXmlRegisteredRr(Authentication authentication, Long m4dId, String xmlContent) throws IOException {
        M4dConfirm m4dConfirm = this.findM4dConfirmByM4dId(m4dId);
        if (m4dConfirm==null) {
            throw new BadRequestException(MRPA_CONFIRM_IS_NULL);
        }
        M4dFile m4dFile = new M4dFile();
        m4dFile.setFileMimeType(FileMimeTypeEnum.XML);
        m4dFile.setFileLogicalType(FileLogicalTypeEnum.DOCUMENT);
        m4dFile.setFilename(XMLUtil.getSubstringFromXml(xmlContent, "ИдФайл=\"")+".xml");
        String base64Content = Base64.getEncoder().encodeToString(xmlContent.getBytes());
        m4dFile.setContent(base64Content);
        M4dFile existsM4dFile;
        try {
            existsM4dFile = this.m4dFileService.findM4dFileByContent(m4dFile.getContent());
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(e.getMessage());
        }
        if (existsM4dFile == null){
            m4dConfirm.setSigFileRegisteredRr(m4dFile);
        } else {
            m4dConfirm.setSigFileRegisteredRr(existsM4dFile);
        }
        M4dConfirm m4dConfirmSaved;
        try {
            m4dConfirmSaved = this.saveM4dConfirm(m4dConfirm);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(e.getMessage());
        }
        return m4dConfirmSaved.getSigFileRegisteredRr().getId();
    }
}
