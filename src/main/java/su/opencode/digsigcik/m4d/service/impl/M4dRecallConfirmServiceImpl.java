package su.opencode.digsigcik.m4d.service.impl;

import org.springframework.context.annotation.Primary;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.m4d.dto.M4dRevokeConfirmDto;
import su.opencode.digsigcik.m4d.enums.FileLogicalTypeEnum;
import su.opencode.digsigcik.m4d.enums.FileMimeTypeEnum;
import su.opencode.digsigcik.m4d.exception.BadRequestException;
import su.opencode.digsigcik.m4d.model.M4dFile;
import su.opencode.digsigcik.m4d.model.M4dRecall;
import su.opencode.digsigcik.m4d.model.M4dRecallConfirm;
import su.opencode.digsigcik.m4d.repository.M4dRecallConfirmRepository;
import su.opencode.digsigcik.m4d.service.ContainerService;
import su.opencode.digsigcik.m4d.service.M4dFileService;
import su.opencode.digsigcik.m4d.service.M4dRecallConfirmService;
import su.opencode.digsigcik.m4d.util.DateUtils;
import su.opencode.digsigcik.m4d.util.XMLUtil;

import javax.validation.ConstraintViolationException;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;

import static su.opencode.digsigcik.m4d.util.Constants.MRPA_RECALL_CONFIRM_IS_NULL;

@Service
@Primary
public class M4dRecallConfirmServiceImpl implements M4dRecallConfirmService {
    private final M4dRecallConfirmRepository m4dRecallConfirmRepository;
    private final M4dFileService m4dFileService;
    private final ContainerService containerService;

    public M4dRecallConfirmServiceImpl(M4dRecallConfirmRepository m4dRecallConfirmRepository, M4dFileService m4dFileService, ContainerService containerService) {
        this.m4dRecallConfirmRepository = m4dRecallConfirmRepository;
        this.m4dFileService = m4dFileService;
        this.containerService = containerService;
    }

    @Override
    public M4dRecallConfirm saveM4dRecallConfirm(M4dRecallConfirm m4dRecallConfirm) {
        return this.m4dRecallConfirmRepository.save(m4dRecallConfirm);
    }

    @Override
    public M4dRecallConfirm findM4dRecallConfirmByM4dId(Long m4dId) {
        return this.m4dRecallConfirmRepository.findM4dRecallConfirmByM4dId(m4dId);
    }

    @Override
    public M4dRecallConfirm updateM4dRecallConfirm(M4dRecallConfirm m4dRecallConfirm) {
        return this.m4dRecallConfirmRepository.save(m4dRecallConfirm);
    }

    @Override
    public void deleteM4dRecallConfirmById(Long id) {
        this.m4dRecallConfirmRepository.deleteById(id);
    }

    @Override
    public Long uploadSigRegisteredPep(Authentication authentication, Long m4dId, MultipartFile container, boolean verify) throws IOException {
        M4dRecallConfirm m4dRecallConfirm = this.findM4dRecallConfirmByM4dId(m4dId);
        if (m4dRecallConfirm==null) {
            throw new BadRequestException(MRPA_RECALL_CONFIRM_IS_NULL);
        }
        if (m4dRecallConfirm.getSigFileRegisteredPep()!=null) {
            throw new BadRequestException("Подтверждение регистрации отзыва МЧД в ПЭП уже подписано");
        }
        if (verify) {
            containerService.verify(authentication, container, m4dRecallConfirm.getXmlFileRegisteredPep().getContent());
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
            m4dRecallConfirm.setSigFileRegisteredPep(m4dFile);
        } else {
            m4dRecallConfirm.setSigFileRegisteredPep(existsM4dFile);
        }
        M4dRecallConfirm m4dRecallConfirmSaved;
        try {
            m4dRecallConfirmSaved = this.saveM4dRecallConfirm(m4dRecallConfirm);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(e.getMessage());
        }
        return m4dRecallConfirmSaved.getSigFileRegisteredPep().getId();
    }

    @Override
    public Long uploadSigRegisteredRr(Authentication authentication, Long m4dId, MultipartFile container, boolean verify) throws IOException {
        M4dRecallConfirm m4dRecallConfirm = this.findM4dRecallConfirmByM4dId(m4dId);
        if (m4dRecallConfirm==null) {
            throw new BadRequestException(MRPA_RECALL_CONFIRM_IS_NULL);
        }
        if (m4dRecallConfirm.getSigFileRegisteredRr()!=null) {
            throw new BadRequestException("Подтверждение регистрации отзыва МЧД в РР уже подписано");
        }
        if (verify) {
            containerService.verify(authentication, container, m4dRecallConfirm.getXmlFileRegisteredRr().getContent());
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
            m4dRecallConfirm.setSigFileRegisteredRr(m4dFile);
        } else {
            m4dRecallConfirm.setSigFileRegisteredRr(existsM4dFile);
        }
        M4dRecallConfirm m4dRecallConfirmSaved;
        try {
            m4dRecallConfirmSaved = this.saveM4dRecallConfirm(m4dRecallConfirm);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(e.getMessage());
        }
        return m4dRecallConfirmSaved.getSigFileRegisteredRr().getId();
    }

    @Override
    public Long uploadXmlRegisteredRr(Authentication authentication, Long m4dId, String xmlContent) throws IOException {
        M4dRecallConfirm m4dRecallConfirm = this.findM4dRecallConfirmByM4dId(m4dId);
        if (m4dRecallConfirm==null) {
            throw new BadRequestException(MRPA_RECALL_CONFIRM_IS_NULL);
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
            m4dRecallConfirm.setSigFileRegisteredRr(m4dFile);
        } else {
            m4dRecallConfirm.setSigFileRegisteredRr(existsM4dFile);
        }
        M4dRecallConfirm m4dRecallConfirmSaved;
        try {
            m4dRecallConfirmSaved = this.saveM4dRecallConfirm(m4dRecallConfirm);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(e.getMessage());
        }
        return m4dRecallConfirmSaved.getSigFileRegisteredRr().getId();
    }

    @Override
    public M4dRevokeConfirmDto getXml(String guid, String hashFile) {
        //TODO: @helix82 предустановленные данные, надо вынести в параметры в хэлмы или устанавливать в качестве переменных в базу
        String orgId = "de8d2875-283a-4f59-9246-e1879e256bc2";
        String orgName = "ЦЕНТРАЛЬНАЯ ИЗБИРАТЕЛЬНАЯ КОМИССИЯ РОССИЙСКОЙ ФЕДЕРАЦИИ";
        String orgTradeName = "ЦИК РФ";
        String orgInn = "7710010990";
        String orgOgrn = "1027700466640";
        String orgInfoSystem = "sep.cik";
        String orgVersionSystem = "sep.cik 2.9";
        //
        String guidConfirm = UUID.randomUUID().toString();
        M4dRevokeConfirmDto m4dRevokeConfirmDto = new M4dRevokeConfirmDto();
        m4dRevokeConfirmDto.setVersionFormat("ПодтвФНС003");
        m4dRevokeConfirmDto.setTypeConfirm("ПодтвЗаявлОтмена");
        m4dRevokeConfirmDto.setIdFile("Квит003_ФНС_ЗаявОтм_"+guidConfirm); //Квит003_ФНС_ЗаявОтм_eecbffec-e37f-4aa4-a135-896241dd8d71
        m4dRevokeConfirmDto.setGuid(guidConfirm);
        m4dRevokeConfirmDto.setRegisteredDate(DateUtils.getCreatedDate());
        m4dRevokeConfirmDto.setOperatorId("4M2EFnszPAT1yr45SgWVDLxfYCa4AYvDGns");
        M4dRevokeConfirmDto.Organisation organisation = new M4dRevokeConfirmDto.Organisation();
        organisation.setId(orgId);
        organisation.setName(orgName);
        organisation.setTradeName(orgTradeName);
        organisation.setInn(orgInn);
        organisation.setOgrn(orgOgrn);
        organisation.setInfoSystem(orgInfoSystem);
        organisation.setVersionSystem(orgVersionSystem);
        m4dRevokeConfirmDto.setOrganisation(organisation);
        M4dRevokeConfirmDto.M4dRevoke m4d = new M4dRevokeConfirmDto.M4dRevoke();
        m4d.setHashFile(hashFile);
        m4d.setGuid(guid);
        m4dRevokeConfirmDto.setM4d(m4d);
        return m4dRevokeConfirmDto;
    }

    @Override
    public M4dRecallConfirm createAndSignedM4dRecallConfirm(M4dRecall m4dRecall, String guid, String hashFile) throws JAXBException {
        //формируем подтверждение регистрации отзыва мчд в ПЭП
        M4dRevokeConfirmDto m4dRevokeConfirmDto = this.getXml(guid, hashFile);
        String xmlConfirmString = XMLUtil.marshall(m4dRevokeConfirmDto, M4dRevokeConfirmDto.class);
        M4dRecallConfirm m4dRecallConfirm = new M4dRecallConfirm();
        m4dRecallConfirm.setM4dRecall(m4dRecall);
        m4dRecallConfirm.setRegisteredPepDate(DateUtils.convertStringToLocalDateTime(DateUtils.getCreatedDate()));

        M4dFile m4dFile = new M4dFile();
        m4dFile.setFileMimeType(FileMimeTypeEnum.XML);
        m4dFile.setFileLogicalType(FileLogicalTypeEnum.DOCUMENT);
        m4dFile.setFilename(m4dRevokeConfirmDto.getIdFile()+".xml");
        String base64Content = Base64.getEncoder().encodeToString(xmlConfirmString.getBytes());
        m4dFile.setContent(base64Content);
        M4dFile existsM4dFile;
        try {
            existsM4dFile = this.m4dFileService.findM4dFileByContent(m4dFile.getContent());
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(e.getMessage());
        }
        m4dRecallConfirm.setXmlFileRegisteredPep(Objects.requireNonNullElse(existsM4dFile, m4dFile));
        M4dRecallConfirm m4dRecallConfirmSaved;
        try {
            m4dRecallConfirmSaved = this.saveM4dRecallConfirm(m4dRecallConfirm);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(e.getMessage());
        }
        //автоподписание и сохранение контейнера с подписью в базу
        return m4dRecallConfirmSaved;
    }

}
