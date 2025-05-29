package su.opencode.digsigcik.m4d.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.m4d.dto.M4dRevokeConfirmDto;
import su.opencode.digsigcik.m4d.enums.FileLogicalTypeEnum;
import su.opencode.digsigcik.m4d.enums.FileMimeTypeEnum;
import su.opencode.digsigcik.m4d.enums.HashAlgorithm;
import su.opencode.digsigcik.m4d.exception.BadRequestException;
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
import java.io.IOException;
import java.util.UUID;

import static su.opencode.digsigcik.m4d.util.Constants.MRPA_RECALL_CONFIRM_IS_NULL;

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
    public void deleteM4dRecallConfirmById(Long id) {
        this.m4dRecallConfirmRepository.deleteById(id);
    }

    @Override
    public Long uploadSigRegisteredPep(Long m4dRecallId, MultipartFile signature) throws IOException {
        M4dRecallConfirm m4dRecallConfirm = this.findM4dRecallConfirmByM4dRecallId(m4dRecallId);
        if (m4dRecallConfirm==null) {
            throw new BadRequestException(MRPA_RECALL_CONFIRM_IS_NULL);
        }
        if (m4dRecallConfirm.getSigFileRegisteredPep()!=null) {
            throw new BadRequestException("Подтверждение регистрации отзыва МЧД в ПЭП уже подписано");
        }
        String filename = signature.getOriginalFilename();
        M4dFile m4dFile = this.m4dFileService.fillM4dFile(FileMimeTypeEnum.SIG, FileLogicalTypeEnum.SIGNATURE, filename, signature.getBytes());
        m4dRecallConfirm.setSigFileRegisteredPep(m4dFile);
        M4dRecallConfirm m4dRecallConfirmSaved;
        try {
            m4dRecallConfirmSaved = this.saveM4dRecallConfirm(m4dRecallConfirm);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(e.getMessage());
        }
        return m4dRecallConfirmSaved.getSigFileRegisteredPep().getId();
    }

    @Override
    public Long uploadSigRegisteredRr(Long m4dRecallId, MultipartFile signature) throws IOException {
        M4dRecallConfirm m4dRecallConfirm = this.findM4dRecallConfirmByM4dRecallId(m4dRecallId);
        if (m4dRecallConfirm==null) {
            throw new BadRequestException(MRPA_RECALL_CONFIRM_IS_NULL);
        }
        if (m4dRecallConfirm.getSigFileRegisteredRr()!=null) {
            throw new BadRequestException("Подтверждение регистрации отзыва МЧД в РР уже подписано");
        }
        String filename = signature.getOriginalFilename();
        M4dFile m4dFile = this.m4dFileService.fillM4dFile(FileMimeTypeEnum.SIG, FileLogicalTypeEnum.SIGNATURE, filename, signature.getBytes());
        m4dRecallConfirm.setSigFileRegisteredRr(m4dFile);
        M4dRecallConfirm m4dRecallConfirmSaved;
        try {
            m4dRecallConfirmSaved = this.saveM4dRecallConfirm(m4dRecallConfirm);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(e.getMessage());
        }
        return m4dRecallConfirmSaved.getSigFileRegisteredRr().getId();
    }

    @Override
    public Long uploadXmlRegisteredRr(Long m4dRecallId, String xmlContent) {
        M4dRecallConfirm m4dRecallConfirm = this.findM4dRecallConfirmByM4dRecallId(m4dRecallId);
        if (m4dRecallConfirm==null) {
            throw new BadRequestException(MRPA_RECALL_CONFIRM_IS_NULL);
        }

        String filenameXml = XMLUtil.getSubstringFromXml(xmlContent, "ИдФайл=\"")+".xml";
        M4dFile m4dFile = this.m4dFileService.fillM4dFile(FileMimeTypeEnum.XML, FileLogicalTypeEnum.DOCUMENT, filenameXml, xmlContent.getBytes());
        m4dRecallConfirm.setXmlFileRegisteredRr(m4dFile);

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
        m4dRevokeConfirmDto.setRegisteredDate(DateUtils.getCurrentDate());
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

        String filenameSig = null;
        if (Boolean.parseBoolean(IS_DETACHED)) {
            filenameSig = m4dRevokeConfirmDto.getIdFile()+"_detached.sig";
        } else filenameSig = m4dRevokeConfirmDto.getIdFile()+".sig";
        M4dFile m4dFileSig = this.m4dFileService.fillM4dFile(FileMimeTypeEnum.SIG, FileLogicalTypeEnum.SIGNATURE, filenameSig, resource.getBytes());
        m4dRecallConfirm.setSigFileRegisteredPep(m4dFileSig);

        M4dRecallConfirm m4dRecallConfirmSaved;
        try {
            m4dRecallConfirmSaved = this.saveM4dRecallConfirm(m4dRecallConfirm);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(e.getMessage());
        }
        return m4dRecallConfirmSaved;
    }
}
