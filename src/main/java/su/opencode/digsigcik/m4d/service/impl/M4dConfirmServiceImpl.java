package su.opencode.digsigcik.m4d.service.impl;

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

import static su.opencode.digsigcik.m4d.util.Constants.*;

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
    public Long uploadSigRegisteredPep(Long m4dId, MultipartFile signature) throws IOException {
        M4dConfirm m4dConfirm = this.findM4dConfirmByM4dId(m4dId);
        if (m4dConfirm==null) {
            throw new BadRequestException(MRPA_CONFIRM_IS_NULL);
        }
        if (m4dConfirm.getSigFileRegisteredPep()!=null) {
            throw new BadRequestException(MRPA_CONFIRM_PEP_SIG);
        }
        String filename = signature.getOriginalFilename();
        M4dFile m4dFile = this.m4dFileService.fillM4dFile(FileMimeTypeEnum.SIG, FileLogicalTypeEnum.SIGNATURE, filename, signature.getBytes());
        m4dConfirm.setSigFileRegisteredPep(m4dFile);
        M4dConfirm m4dConfirmSaved;
        try {
            m4dConfirmSaved = this.saveM4dConfirm(m4dConfirm);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(e.getMessage());
        }
        return m4dConfirmSaved.getSigFileRegisteredPep().getId();
    }

    @Override
    public Long uploadSigRegisteredRr(Long m4dId, MultipartFile signature) throws IOException {
        M4dConfirm m4dConfirm = this.findM4dConfirmByM4dId(m4dId);
        if (m4dConfirm==null) {
            throw new BadRequestException(MRPA_CONFIRM_IS_NULL);
        }
        if (m4dConfirm.getSigFileRegisteredRr()!=null) {
            throw new BadRequestException(MRPA_CONFIRM_RR_SIG);
        }
        String filenameXml = signature.getOriginalFilename();
        M4dFile m4dFile = this.m4dFileService.fillM4dFile(FileMimeTypeEnum.SIG, FileLogicalTypeEnum.SIGNATURE, filenameXml, signature.getBytes());
        m4dConfirm.setSigFileRegisteredRr(m4dFile);
        M4dConfirm m4dConfirmSaved;
        try {
            m4dConfirmSaved = this.saveM4dConfirm(m4dConfirm);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(e.getMessage());
        }
        return m4dConfirmSaved.getSigFileRegisteredRr().getId();
    }

    @Override
    public Long uploadXmlRegisteredRr(Long m4dId, String xmlContent) {
        M4dConfirm m4dConfirm = this.findM4dConfirmByM4dId(m4dId);
        if (m4dConfirm==null) {
            throw new BadRequestException(MRPA_CONFIRM_IS_NULL);
        }

        String filenameXml = XMLUtil.getSubstringFromXml(xmlContent, "ИдФайл=\"")+".xml";
        M4dFile m4dFile = this.m4dFileService.fillM4dFile(FileMimeTypeEnum.XML, FileLogicalTypeEnum.DOCUMENT, filenameXml, xmlContent.getBytes());
        m4dConfirm.setXmlFileRegisteredRr(m4dFile);
        M4dConfirm m4dConfirmSaved;
        try {
            m4dConfirmSaved = this.saveM4dConfirm(m4dConfirm);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(e.getMessage());
        }
        return m4dConfirmSaved.getSigFileRegisteredRr().getId();
    }

    @Override
    public M4dConfirmDto getXml(String idFile, String guid,String internalNumber, String startDate, String endDate) {
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
        M4dConfirmDto m4dConfirmDto = new M4dConfirmDto();
        m4dConfirmDto.setVersionFormat("ПодтвФНС003");
        m4dConfirmDto.setTypeConfirm("ПодтвДовер");
        m4dConfirmDto.setIdFile("Квит003_ФНС_"+idFile); //Квит003_ФНС_ON_EMCHD_20240516_75655b80-cc58-4a2b-b244-8334eb895a2f
        m4dConfirmDto.setGuid(guidConfirm);
        m4dConfirmDto.setRegisteredDate(DateUtils.getCurrentDate());
        m4dConfirmDto.setOperatorId("4M2EFnszPAT1yr45SgWVDLxfYCa4AYvDGns");
        M4dConfirmDto.Organisation organisation = new M4dConfirmDto.Organisation();
        organisation.setId(orgId);
        organisation.setName(orgName);
        organisation.setTradeName(orgTradeName);
        organisation.setInn(orgInn);
        organisation.setOgrn(orgOgrn);
        organisation.setInfoSystem(orgInfoSystem);
        organisation.setVersionSystem(orgVersionSystem);
        m4dConfirmDto.setOrganisation(organisation);
        M4dConfirmDto.M4d m4d = new M4dConfirmDto.M4d();
        m4d.setIdFile(idFile);
        m4d.setGuid(guid);
        m4d.setInternalNumber(internalNumber);
        m4d.setStartDate(startDate);
        m4d.setEndDate(endDate);
        m4d.setInfoSystem(orgInfoSystem);
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

        String filenameSig = null;
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
            e.printStackTrace();
        }
        return m4dConfirmUpdate;
    }
}
