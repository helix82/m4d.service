package su.opencode.digsigcik.m4d.service.impl;

import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import su.opencode.digsigcik.audit.AuditLogEventEnum;
import su.opencode.digsigcik.audit.AuditLogThis;
import su.opencode.digsigcik.m4d.dto.M4dRegistryResponseDto;
import su.opencode.digsigcik.m4d.dto.M4dRevokeXml;
import su.opencode.digsigcik.m4d.enums.FileLogicalTypeEnum;
import su.opencode.digsigcik.m4d.enums.FileMimeTypeEnum;
import su.opencode.digsigcik.m4d.exception.BadRequestException;
import su.opencode.digsigcik.m4d.exception.M4dException;
import su.opencode.digsigcik.m4d.model.M4d;
import su.opencode.digsigcik.m4d.model.M4dFile;
import su.opencode.digsigcik.m4d.model.M4dRecall;
import su.opencode.digsigcik.m4d.repository.M4dRecallRepository;
import su.opencode.digsigcik.m4d.repository.M4dRepository;
import su.opencode.digsigcik.m4d.service.M4dFileService;
import su.opencode.digsigcik.m4d.service.M4dRecallService;
import su.opencode.digsigcik.m4d.service.M4dService;
import su.opencode.digsigcik.m4d.service.UserCertificateService;

import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Primary
public class M4dRecallServiceImpl implements M4dRecallService {
    private final M4dRecallRepository m4dRevokeRepository;
    private final M4dRepository m4dRepository;
    private final M4dService m4dService;
    private final M4dFileService m4dFileService;
    private final UserCertificateService userCertificateService;

    public M4dRecallServiceImpl(M4dRecallRepository m4dRevokeRepository, M4dRepository m4dRepository,
                                @Lazy M4dService m4dService, UserCertificateService userCertificateService, M4dFileService m4dFileService) {
        this.m4dRevokeRepository = m4dRevokeRepository;
        this.m4dRepository = m4dRepository;
        this.m4dService = m4dService;
        this.m4dFileService = m4dFileService;
        this.userCertificateService = userCertificateService;
    }

    @Override
    @AuditLogThis(event = AuditLogEventEnum.M4D_RECALL_CREATE)
    public M4dRecall saveM4dRecall(M4dRecall m4dRecall, String xmlString) {

        String idFile="";
        String start = "НомДовер=\"";
        int startIndex = xmlString.indexOf(start);
        if (startIndex != -1) {
            startIndex += start.length();
            int endIndex = xmlString.indexOf("\"", startIndex);
            if (endIndex != -1) {
                idFile = xmlString.substring(startIndex, endIndex);
                System.out.println("Найдена подстрока: " + idFile);
            } else {
                System.out.println("Закрывающая кавычка не найдена.");
            }
        } else {
            System.out.println("Подстрока не найдена.");
        }

        M4dFile m4dFile = new M4dFile();
        m4dFile.setFileMimeType(FileMimeTypeEnum.XML);
        m4dFile.setFileLogicalType(FileLogicalTypeEnum.DOCUMENT);
        m4dFile.setFilename("ON_OFFDOVER_" + idFile + ".xml");
        m4dFile.setContent(xmlString);
        M4dFile existsM4dFile;
        try {
            existsM4dFile = this.m4dFileService.findM4dFileByContent(m4dFile.getContent());
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(e.getMessage());
        }
        m4dRecall.setXmlFile(Objects.requireNonNullElse(existsM4dFile, m4dFile));
        return this.m4dRevokeRepository.save(m4dRecall);
    }

    @Override
    public List<M4dRegistryResponseDto> findAllMrpaRegistriesWithRecall() throws ParseException {
        List<M4dRecall> m4dRecalls = m4dRevokeRepository.findAll().stream().filter(c -> c.getM4d() != null).collect(Collectors.toList());
        List<M4dRegistryResponseDto> m4DRegistryResponseDtos = new ArrayList<>();
        for (M4dRecall x : m4dRecalls) {
            M4d m4d = m4dRepository.findM4dById(x.getM4d().getId());
            m4DRegistryResponseDtos.add(m4dService.convertM4dToM4dResponseDto(m4d, false, false, false, false));
        }
        return m4DRegistryResponseDtos;
    }

    @Override
    public M4dRecall findMrpaRecallById(@NotNull Long id) {
        return this.m4dRevokeRepository.findM4dRecallById(id);
    }

    @Override
    public M4dRecall findMrpaRecallByM4dId(@NotNull Long m4dId) {
        return this.m4dRevokeRepository.findM4dRecallByM4dId(m4dId);
    }

    public M4dRecall findById(Long id) {
        return this.m4dRevokeRepository.findM4dRecallById(id);
    }

    @Override
    public void deleteMrpaRecallById(Long id) {
        this.m4dRevokeRepository.deleteById(id);
    }

    @Override
    public void updateMrpaRecallById(Long id, Long fileId) {
        this.m4dRevokeRepository.updateMrpaRecallById(id, fileId);
    }

    @Override
    public M4dRecall updateMrpaRecall(M4dRecall mrpaRecall) {
        return this.m4dRevokeRepository.save(mrpaRecall);
    }

    @Transactional(readOnly = true)
    @Override
    public List<M4dRegistryResponseDto> findAllM4dRevokeUsers(Authentication authentication) {
        Set<String> setINNs = userCertificateService.getUserCertificateINNs(authentication);

        return m4dRevokeRepository.findListM4dRevokeBySetInn(setINNs).stream()
                .peek(m4d -> {
                    try {
                        Long id = m4d.getId();
                        m4d.setStatus(m4dService.getStatusM4d(id));
                        m4d.setCredentials(m4dService.getCredentialByMrpaRegistryId(id));
                    } catch (ParseException e) {
                        throw new M4dException(e.getMessage());
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public M4dRevokeXml convertMrpaRecallToXml(M4dRegistryResponseDto mrpaRegistryResponseDto) {
        M4dRevokeXml mrpaRecallXml = new M4dRevokeXml();
        mrpaRecallXml.setXmlnsXsi("http://www.w3.org/2001/XMLSchema-instance");
        mrpaRecallXml.setXmlnsXsd("http://www.w3.org/2001/XMLSchema");
        mrpaRecallXml.setVersionFormat("001");

        M4dRevokeXml.Document.RecallInfo recallInfo = new M4dRevokeXml.Document.RecallInfo();
        recallInfo.setGuid(mrpaRegistryResponseDto.getGuid());
        recallInfo.setArgument(mrpaRegistryResponseDto.getRecall().getArgument());

        M4dRevokeXml.Document document = new M4dRevokeXml.Document();
        mrpaRecallXml.setDocument(document);
        document.setRecallInfo(recallInfo);

        M4dRevokeXml.Document.RecallWho recallWho = new M4dRevokeXml.Document.RecallWho();
        document.setRecallWho(recallWho);

//        if (mrpaRegistryResponseDto.recall.getAuthor().equals("PRINCIPAL")) {
            M4dRevokeXml.Document.RecallWho.Principal principal = new M4dRevokeXml.Document.RecallWho.Principal();
            recallWho.setPrincipal(principal);
            M4dRevokeXml.Document.RecallWho.Principal.LegalEntity legalEntity = new M4dRevokeXml.Document.RecallWho.Principal.LegalEntity();
            legalEntity.setName(mrpaRegistryResponseDto.organization.getName());
            legalEntity.setInn(mrpaRegistryResponseDto.organization.getInn());
            legalEntity.setKpp(mrpaRegistryResponseDto.organization.getKpp());
            legalEntity.setOgrn(mrpaRegistryResponseDto.organization.getOgrn());
            principal.setLegalEntity(legalEntity);
//        } else if (mrpaRegistryResponseDto.recall.getAuthor().equals("REPRESENTATIVE")) {
//            M4dRevokeXml.Document.RecallWho.Representative representative = new M4dRevokeXml.Document.RecallWho.Representative();
//            recallWho.setRepresentative(representative);
//            M4dRevokeXml.Document.RecallWho.Representative.DetailRepresentative detailRepresentative = new M4dRevokeXml.Document.RecallWho.Representative.DetailRepresentative();
//            detailRepresentative.setInn(mrpaRegistryResponseDto.representative.getInn());
//            detailRepresentative.setSnils(mrpaRegistryResponseDto.representative.getSnils());
//            M4dRevokeXml.Document.RecallWho.Representative.DetailRepresentative.FioRepresentative fioRepresentative = new M4dRevokeXml.Document.RecallWho.Representative.DetailRepresentative.FioRepresentative();
//            fioRepresentative.setName(mrpaRegistryResponseDto.representativeDetail.getName());
//            fioRepresentative.setSurname(mrpaRegistryResponseDto.representativeDetail.getSurname());
//            fioRepresentative.setPatronymic(mrpaRegistryResponseDto.representativeDetail.getPatronymic());
//            representative.setDetailRepresentative(detailRepresentative);
//            detailRepresentative.setFioRepresentative(fioRepresentative);
//        }

        return mrpaRecallXml;
    }
}
