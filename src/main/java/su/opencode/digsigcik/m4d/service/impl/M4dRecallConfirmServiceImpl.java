package su.opencode.digsigcik.m4d.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.common.exceptions.exception.custom.BadRequestException;
import su.opencode.digsigcik.m4d.dto.M4dRevokeConfirmDto;
import su.opencode.digsigcik.m4d.enums.FileLogicalTypeEnum;
import su.opencode.digsigcik.m4d.enums.FileMimeTypeEnum;
import su.opencode.digsigcik.m4d.enums.HashAlgorithm;
import su.opencode.digsigcik.m4d.model.M4dFile;
import su.opencode.digsigcik.m4d.model.M4dRecall;
import su.opencode.digsigcik.m4d.model.M4dRecallConfirm;
import su.opencode.digsigcik.m4d.repository.M4dRecallConfirmRepository;
import su.opencode.digsigcik.m4d.service.CryptoProCloudSignatureService;
import su.opencode.digsigcik.m4d.service.M4dFileService;
import su.opencode.digsigcik.m4d.service.M4dRecallConfirmService;
import su.opencode.digsigcik.m4d.util.DateUtils;
import su.opencode.digsigcik.m4d.util.FileUtils;
import su.opencode.digsigcik.m4d.util.XMLUtil;

import javax.validation.ConstraintViolationException;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@Primary
public class M4dRecallConfirmServiceImpl implements M4dRecallConfirmService {
    @Value("${autosign.user-login}")
    private String USER_LOGIN;
    @Value("${autosign.pincode}")
    private String PINCODE;
    @Value("${autosign.is-detached}")
    private String IS_DETACHED;
    @Value("${autosign.sig-type}")
    private String SIG_TYPE;

    @Value("${confirm.version-format}")
    private String CONFIRM_VERSION_FORMAT;
    @Value("${confirm.legal-entity.id}")
    private String CONFIRM_LEGAL_ENTITY_ID;
    @Value("${confirm.legal-entity.name}")
    private String CONFIRM_LEGAL_ENTITY_NAME;
    @Value("${confirm.legal-entity.trade-name}")
    private String CONFIRM_LEGAL_ENTITY_TRADE_NAME;
    @Value("${confirm.legal-entity.inn}")
    private String CONFIRM_LEGAL_ENTITY_INN;
    @Value("${confirm.legal-entity.ogrn}")
    private String CONFIRM_LEGAL_ENTITY_OGRN;
    @Value("${confirm.operator.adr-nod}")
    private String CONFIRM_OPERATOR_ADR_NOD;
    @Value("${confirm.operator.product-url}")
    private String CONFIRM_OPERATOR_PRODUCT_URL;
    @Value("${confirm.operator.product-version}")
    private String CONFIRM_OPERATOR_PRODUCT_VERSION;
    @Value("${confirm.revoke.type}")
    private String CONFIRM_REVOKE_TYPE;

    private final M4dRecallConfirmRepository m4dRecallConfirmRepository;
    private final M4dFileService m4dFileService;
    private final CryptoProCloudSignatureService cryptoProCloudSignatureService;

    public M4dRecallConfirmServiceImpl(M4dRecallConfirmRepository m4dRecallConfirmRepository, M4dFileService m4dFileService, CryptoProCloudSignatureService cryptoProCloudSignatureService) {
        this.m4dRecallConfirmRepository = m4dRecallConfirmRepository;
        this.m4dFileService = m4dFileService;
        this.cryptoProCloudSignatureService = cryptoProCloudSignatureService;
    }

    @Override
    public M4dRecallConfirm saveM4dRecallConfirm(M4dRecallConfirm m4dRecallConfirm) {
        return this.m4dRecallConfirmRepository.save(m4dRecallConfirm);
    }

    @Override
    public M4dRecallConfirm findM4dRecallConfirmByM4dRecallId(Long m4dRecallId) {
        return this.m4dRecallConfirmRepository.findM4dRecallConfirmByM4dRecallId(m4dRecallId);
    }

    @Override
    public M4dRecallConfirm updateM4dRecallConfirm(M4dRecallConfirm m4dRecallConfirm) {
        return this.m4dRecallConfirmRepository.save(m4dRecallConfirm);
    }

    @Override
    public void deleteM4dRecallConfirmByM4dRevokeId(Long m4dRevokeId) {
        this.m4dRecallConfirmRepository.deleteM4dRecallConfirmByM4dRecallId(m4dRevokeId);
    }

    @Override
    public M4dRevokeConfirmDto getXml(String guid, String hashFile) {
        String guidConfirm = UUID.randomUUID().toString();
        M4dRevokeConfirmDto m4dRevokeConfirmDto = new M4dRevokeConfirmDto();
        m4dRevokeConfirmDto.setVersionFormat(CONFIRM_VERSION_FORMAT);
        m4dRevokeConfirmDto.setTypeConfirm(CONFIRM_REVOKE_TYPE);
        m4dRevokeConfirmDto.setIdFile("Квит003_ФНС_ЗаявОтм_"+guidConfirm);
        m4dRevokeConfirmDto.setGuid(guidConfirm);
        m4dRevokeConfirmDto.setRegisteredDate(DateUtils.getCurrentDate());
        m4dRevokeConfirmDto.setOperatorId(CONFIRM_OPERATOR_ADR_NOD);
        M4dRevokeConfirmDto.Organisation organisation = new M4dRevokeConfirmDto.Organisation();
        organisation.setId(CONFIRM_LEGAL_ENTITY_ID);
        organisation.setName(CONFIRM_LEGAL_ENTITY_NAME);
        organisation.setTradeName(CONFIRM_LEGAL_ENTITY_TRADE_NAME);
        organisation.setInn(CONFIRM_LEGAL_ENTITY_INN);
        organisation.setOgrn(CONFIRM_LEGAL_ENTITY_OGRN);
        organisation.setInfoSystem(CONFIRM_OPERATOR_PRODUCT_URL);
        organisation.setVersionSystem(CONFIRM_OPERATOR_PRODUCT_VERSION);
        m4dRevokeConfirmDto.setOrganisation(organisation);
        M4dRevokeConfirmDto.M4dRevoke m4d = new M4dRevokeConfirmDto.M4dRevoke();
        m4d.setHashFile(hashFile);
        m4d.setGuid(guid);
        m4dRevokeConfirmDto.setM4d(m4d);
        return m4dRevokeConfirmDto;
    }

    @Override
    public M4dRecallConfirm createAndSignedM4dRecallConfirm(Authentication authentication, M4dRecall m4dRecall, String guid, String hashFile) throws JAXBException, IOException {
        M4dRevokeConfirmDto m4dRevokeConfirmDto = this.getXml(guid, hashFile);
        String xmlConfirmString = XMLUtil.marshall(m4dRevokeConfirmDto, M4dRevokeConfirmDto.class);
        M4dRecallConfirm m4dRecallConfirm = new M4dRecallConfirm();
        m4dRecallConfirm.setM4dRecall(m4dRecall);
        m4dRecallConfirm.setRegisteredPepDate(DateUtils.convertStringToLocalDateTime(DateUtils.getCurrentDate()));

        String filenameXml = m4dRevokeConfirmDto.getIdFile()+".xml";
        M4dFile m4dFileXml = this.m4dFileService.fillM4dFile(FileMimeTypeEnum.XML, FileLogicalTypeEnum.DOCUMENT, filenameXml, xmlConfirmString.getBytes());
        m4dRecallConfirm.setXmlFileRegisteredPep(m4dFileXml);
        //автоподписание
        MultipartFile sourceFile = FileUtils.mapStringToMultipart(xmlConfirmString, m4dFileXml.getFilename());
        String resource = cryptoProCloudSignatureService.initializeSignature(authentication, sourceFile,
                0L, USER_LOGIN, PINCODE, Boolean.parseBoolean(IS_DETACHED), SIG_TYPE, HashAlgorithm.GOST3411_2012_256);

        String filenameSig;
        if (Boolean.parseBoolean(IS_DETACHED)) {
            filenameSig = m4dRevokeConfirmDto.getIdFile()+"_detached.sig";
        } else filenameSig = m4dRevokeConfirmDto.getIdFile()+".sig";
        M4dFile m4dFileSig = this.m4dFileService.fillM4dFile(FileMimeTypeEnum.SIG, FileLogicalTypeEnum.SIGNATURE, filenameSig, resource.getBytes());
        m4dRecallConfirm.setSigFileRegisteredPep(m4dFileSig);

        M4dRecallConfirm m4dRecallConfirmSaved;
        try {
            this.deleteM4dRecallConfirmByM4dRevokeId(m4dRecall.getId());
            m4dRecallConfirmSaved = this.saveM4dRecallConfirm(m4dRecallConfirm);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(e.getMessage());
        }
        return m4dRecallConfirmSaved;
    }

    @Override
    public M4dRecallConfirm saveConfirmAndSignatureRr(Long m4dRevokeId, File xmlFileKvitM4dRevoke, File sigFileKvitM4dRevoke) {
        M4dRecallConfirm m4dRecallConfirm = this.findM4dRecallConfirmByM4dRecallId(m4dRevokeId);
        M4dFile m4dFileXmlKvitM4dRevoke = this.m4dFileService.fillM4dFile(FileMimeTypeEnum.XML, FileLogicalTypeEnum.DOCUMENT, xmlFileKvitM4dRevoke.getName(), FileUtils.getFileContentByPath(xmlFileKvitM4dRevoke.getPath()));
        M4dFile m4dFileSigKvitM4dRevoke = this.m4dFileService.fillM4dFile(FileMimeTypeEnum.SIG, FileLogicalTypeEnum.SIGNATURE, sigFileKvitM4dRevoke.getName(), FileUtils.getFileContentByPath(sigFileKvitM4dRevoke.getPath()));
        String datetimeRevokeRegisteredRR = XMLUtil.getSubstringFromXml(new String(FileUtils.getFileContentByPath(xmlFileKvitM4dRevoke.getPath())), "ДатаВремВыдПодтв=\"");
        m4dRecallConfirm.setRegisteredRrDate(DateUtils.convertStringToLocalDateTime(datetimeRevokeRegisteredRR));
        m4dRecallConfirm.setXmlFileRegisteredRr(m4dFileXmlKvitM4dRevoke);
        m4dRecallConfirm.setSigFileRegisteredRr(m4dFileSigKvitM4dRevoke);
        M4dRecallConfirm m4dRecallConfirmUpdate = null;
        try {
            m4dRecallConfirmUpdate = this.updateM4dRecallConfirm(m4dRecallConfirm);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return m4dRecallConfirmUpdate;
    }
}
