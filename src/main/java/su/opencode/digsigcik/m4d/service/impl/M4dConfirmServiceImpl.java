package su.opencode.digsigcik.m4d.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.common.exception.custom.BadRequestException;
import su.opencode.digsigcik.m4d.dto.M4dConfirmDto;
import su.opencode.digsigcik.m4d.enums.FileLogicalTypeEnum;
import su.opencode.digsigcik.m4d.enums.FileMimeTypeEnum;
import su.opencode.digsigcik.m4d.enums.HashAlgorithm;
import su.opencode.digsigcik.m4d.model.M4d;
import su.opencode.digsigcik.m4d.model.M4dConfirm;
import su.opencode.digsigcik.m4d.model.M4dFile;
import su.opencode.digsigcik.m4d.repository.M4dConfirmRepository;
import su.opencode.digsigcik.m4d.service.CryptoProCloudSignatureService;
import su.opencode.digsigcik.m4d.service.M4dConfirmService;
import su.opencode.digsigcik.m4d.service.M4dFileService;
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
public class M4dConfirmServiceImpl implements M4dConfirmService {
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
    @Value("${confirm.m4d.type}")
    private String CONFIRM_M4D_TYPE;

    private final M4dConfirmRepository m4dConfirmRepository;
    private final M4dFileService m4dFileService;
    private final CryptoProCloudSignatureService cryptoProCloudSignatureService;

    public M4dConfirmServiceImpl(M4dConfirmRepository m4dConfirmRepository, M4dFileService m4dFileService, CryptoProCloudSignatureService cryptoProCloudSignatureService) {
        this.m4dConfirmRepository = m4dConfirmRepository;
        this.m4dFileService = m4dFileService;
        this.cryptoProCloudSignatureService = cryptoProCloudSignatureService;
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
    public void deleteM4dConfirmByM4dId(Long m4dId) {
        this.m4dConfirmRepository.deleteM4dConfirmsByM4dId(m4dId);
    }

    @Override
    public M4dConfirmDto getXml(String idFile, String guid,String internalNumber, String startDate, String endDate) {
        String guidConfirm = UUID.randomUUID().toString();
        M4dConfirmDto m4dConfirmDto = new M4dConfirmDto();
        m4dConfirmDto.setVersionFormat(CONFIRM_VERSION_FORMAT);
        m4dConfirmDto.setTypeConfirm(CONFIRM_M4D_TYPE);
        m4dConfirmDto.setIdFile("Квит003_ФНС_"+idFile);
        m4dConfirmDto.setGuid(guidConfirm);
        m4dConfirmDto.setRegisteredDate(DateUtils.getCurrentDate());
        m4dConfirmDto.setOperatorId(CONFIRM_OPERATOR_ADR_NOD);
        M4dConfirmDto.Organisation organisation = new M4dConfirmDto.Organisation();
        organisation.setId(CONFIRM_LEGAL_ENTITY_ID);
        organisation.setName(CONFIRM_LEGAL_ENTITY_NAME);
        organisation.setTradeName(CONFIRM_LEGAL_ENTITY_TRADE_NAME);
        organisation.setInn(CONFIRM_LEGAL_ENTITY_INN);
        organisation.setOgrn(CONFIRM_LEGAL_ENTITY_OGRN);
        organisation.setInfoSystem(CONFIRM_OPERATOR_PRODUCT_URL);
        organisation.setVersionSystem(CONFIRM_OPERATOR_PRODUCT_VERSION);
        m4dConfirmDto.setOrganisation(organisation);
        M4dConfirmDto.M4d m4d = new M4dConfirmDto.M4d();
        m4d.setIdFile(idFile);
        m4d.setGuid(guid);
        m4d.setInternalNumber(internalNumber);
        m4d.setStartDate(startDate);
        m4d.setEndDate(endDate);
        m4d.setInfoSystem(CONFIRM_OPERATOR_PRODUCT_URL);
        m4dConfirmDto.setM4d(m4d);
        return m4dConfirmDto;
    }

    @Override
    public M4dConfirm createAndSignedM4dConfirm(Authentication authentication, M4d m4d, String idFile, String guid, String internalNumber, String startDate, String endDate) throws JAXBException, IOException {
        M4dConfirmDto m4dConfirmDto = this.getXml(idFile, guid, internalNumber, startDate, endDate);
        String xmlConfirmString = XMLUtil.marshall(m4dConfirmDto, M4dConfirmDto.class);
        M4dConfirm m4dConfirm = new M4dConfirm();
        m4dConfirm.setM4d(m4d);
        m4dConfirm.setRegisteredPepDate(DateUtils.convertStringToLocalDateTime(DateUtils.getCurrentDate()));

        String filenameXml = m4dConfirmDto.getIdFile()+".xml";
        M4dFile m4dFileXml = this.m4dFileService.fillM4dFile(FileMimeTypeEnum.XML, FileLogicalTypeEnum.DOCUMENT, filenameXml, xmlConfirmString.getBytes());
        m4dConfirm.setXmlFileRegisteredPep(m4dFileXml);
        //автоподписание
        MultipartFile sourceFile = FileUtils.mapStringToMultipart(xmlConfirmString, m4dFileXml.getFilename());
        String resource = cryptoProCloudSignatureService.initializeSignature(authentication, sourceFile,
                0L, USER_LOGIN, PINCODE, Boolean.parseBoolean(IS_DETACHED), SIG_TYPE, HashAlgorithm.GOST3411_2012_256);

        String filenameSig;
        if (Boolean.parseBoolean(IS_DETACHED)) {
            filenameSig = m4dConfirmDto.getIdFile()+"_detached.sig";
        } else filenameSig = m4dConfirmDto.getIdFile()+".sig";
        M4dFile m4dFileSig = this.m4dFileService.fillM4dFile(FileMimeTypeEnum.SIG, FileLogicalTypeEnum.SIGNATURE, filenameSig, resource.getBytes());
        m4dConfirm.setSigFileRegisteredPep(m4dFileSig);

        M4dConfirm m4dConfirmSaved;
        try {
            this.deleteM4dConfirmByM4dId(m4d.getId());
            m4dConfirmSaved = this.saveM4dConfirm(m4dConfirm);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(e.getMessage());
        }
        return m4dConfirmSaved;
    }

    @Override
    public M4dConfirm saveConfirmAndSignatureRr(Long m4dId, File xmlFileKvitM4d, File sigFileKvitM4d) {
        M4dConfirm m4dConfirm = this.findM4dConfirmByM4dId(m4dId);
        M4dFile m4dFileXmlKvitM4d = this.m4dFileService.fillM4dFile(FileMimeTypeEnum.XML, FileLogicalTypeEnum.DOCUMENT, xmlFileKvitM4d.getName(), FileUtils.getFileContentByPath(xmlFileKvitM4d.getPath()));
        M4dFile m4dFileSigKvitM4d = this.m4dFileService.fillM4dFile(FileMimeTypeEnum.SIG, FileLogicalTypeEnum.SIGNATURE, sigFileKvitM4d.getName(), FileUtils.getFileContentByPath(sigFileKvitM4d.getPath()));
        String datetimeRegisteredRR = XMLUtil.getSubstringFromXml(new String(FileUtils.getFileContentByPath(xmlFileKvitM4d.getPath())), "ДатаВремВыдПодтв=\"");
        m4dConfirm.setRegisteredRrDate(DateUtils.convertStringToLocalDateTime(datetimeRegisteredRR));
        m4dConfirm.setXmlFileRegisteredRr(m4dFileXmlKvitM4d);
        m4dConfirm.setSigFileRegisteredRr(m4dFileSigKvitM4d);
        M4dConfirm m4dConfirmUpdate = null;
        try {
            m4dConfirmUpdate = this.updateM4dConfirm(m4dConfirm);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return m4dConfirmUpdate;
    }
}
