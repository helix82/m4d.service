package su.opencode.digsigcik.m4d.service.impl;

import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
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
import su.opencode.digsigcik.m4d.service.*;

import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

import static su.opencode.digsigcik.m4d.util.Constants.MRPA_RECALL_IS_NULL;
import static su.opencode.digsigcik.m4d.util.Constants.MRPA_REGISTRY_IS_NULL;

@Service
@Primary
public class M4dRecallServiceImpl implements M4dRecallService {
    private final M4dRecallRepository m4dRevokeRepository;
    private final M4dRepository m4dRepository;
    private final M4dService m4dService;
    private final M4dFileService m4dFileService;
    private final UserCertificateService userCertificateService;
    private final ContainerService containerService;

    public M4dRecallServiceImpl(M4dRecallRepository m4dRevokeRepository, M4dRepository m4dRepository,
                                @Lazy M4dService m4dService, UserCertificateService userCertificateService, M4dFileService m4dFileService, ContainerService containerService) {
        this.m4dRevokeRepository = m4dRevokeRepository;
        this.m4dRepository = m4dRepository;
        this.m4dService = m4dService;
        this.m4dFileService = m4dFileService;
        this.userCertificateService = userCertificateService;
        this.containerService = containerService;
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
        //m4dFile.setContent(xmlString);
        String base64Content = Base64.getEncoder().encodeToString(xmlString.getBytes());
        m4dFile.setContent(base64Content);
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
    public List<M4dRegistryResponseDto> findAllMrpaRegistriesWithRecall(Authentication authentication) throws ParseException {
        List<M4dRecall> m4dRecalls = m4dRevokeRepository.findAll().stream().filter(c -> c.getM4d() != null).collect(Collectors.toList());
        List<M4dRegistryResponseDto> m4DRegistryResponseDtos = new ArrayList<>();
        for (M4dRecall x : m4dRecalls) {
            M4d m4d = m4dRepository.findM4dById(x.getM4d().getId());
            m4DRegistryResponseDtos.add(m4dService.convertM4dToM4dResponseDto(authentication,  m4d, false, false, false, false));
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
    @AuditLogThis(event = AuditLogEventEnum.M4D_RECALL_DELETE)
    public void deleteMrpaRecallById(Long id) {
        this.m4dRevokeRepository.deleteById(id);
    }

    @Override
    public void updateMrpaRecallById(Long id, Long fileId) {
        this.m4dRevokeRepository.updateMrpaRecallById(id, fileId);
    }

    @Override
    public void updateStatusMrpaRecallById(Long id, String status) {
        this.m4dRevokeRepository.updateStatusMrpaRecallById(id, status);
    }

    @Override
    @AuditLogThis(event = AuditLogEventEnum.M4D_RECALL_UPDATE)
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
        M4dRevokeXml.Document.RecallWho.Principal principal = new M4dRevokeXml.Document.RecallWho.Principal();
        recallWho.setPrincipal(principal);
        M4dRevokeXml.Document.RecallWho.Principal.LegalEntity legalEntity = new M4dRevokeXml.Document.RecallWho.Principal.LegalEntity();
        legalEntity.setName(mrpaRegistryResponseDto.organization.getName());
        legalEntity.setInn(mrpaRegistryResponseDto.organization.getInn());
        legalEntity.setKpp(mrpaRegistryResponseDto.organization.getKpp());
        legalEntity.setOgrn(mrpaRegistryResponseDto.organization.getOgrn());
        principal.setLegalEntity(legalEntity);

        return mrpaRecallXml;
    }

    @Override
    @AuditLogThis(event = AuditLogEventEnum.M4D_RECALL_SIGNATURE)
    public Long uploadSig(Authentication authentication, Long id, MultipartFile container, boolean verify) throws IOException, ParseException {
        //String bearerToken = getTokenValue(authentication);
        M4dRecall mrpaRecall = this.findById(id);
        if (mrpaRecall==null) {
            throw new BadRequestException(MRPA_RECALL_IS_NULL);
        }
        if (mrpaRecall.getSigFile()!=null) {
            throw new BadRequestException("Заявление на отзыв МЧД уже подписано");
        }
//        List<String> tempFileNames = new ArrayList<>();
//        String tempFileName;
//        FileOutputStream fo;
//        ReportDto reportDto;
//        ByteArrayResource res = null;
//        if (verify) {
//            try {
//                tempFileName = "/tmp/" + container.getOriginalFilename();
//                tempFileNames.add(tempFileName);
//                fo = new FileOutputStream(tempFileName);
//                fo.write(container.getBytes());
//                fo.close();
//                ObjectMapper objectMapper = new ObjectMapper();
//                Map<String, String> resultMap = this.synapiClientService.getReportContainer(tempFileName, bearerToken);
//                reportDto = objectMapper.readValue(resultMap.get("signCheck"), ReportDto.class);
//                assert reportDto != null;
//                reportDto.getResults().stream()
//                        .flatMap(result -> result.getVerificationResults().stream())
//                        .findFirst()
//                        .ifPresent(verificationResult -> {
//                            boolean isResultTrue = verificationResult.isResult();
//                            System.out.println("Result value: " + isResultTrue);
//                            if (!isResultTrue) {
//                                throw new BadRequestException(MRPA_REGISTRY_NOT_VERIFY_REPORT);
//                            }
//                        });
//                res = this.synapiClientService.getPartContainer(tempFileName, "sourceFile", bearerToken);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            for (String fileName : tempFileNames) {
//                File f = new File(fileName);
//                f.delete();
//            }
//            assert res != null;
//            byte[] resource = res.getByteArray();
            M4d mrpaRegistryFind;
            try {
                mrpaRegistryFind = this.m4dService.findById(mrpaRecall.getM4d().getId());
            } catch (ConstraintViolationException e) {
                throw new BadRequestException(e.getMessage());
            }
            if (mrpaRegistryFind == null) {
                throw new BadRequestException(MRPA_REGISTRY_IS_NULL);

            }
            M4dRegistryResponseDto m4DRegistryResponseDto = this.m4dService.convertM4dToM4dResponseDto(authentication, mrpaRegistryFind, false, false, false, true);
//            String normalizedResource = new String(resource, StandardCharsets.UTF_8).replaceAll("\\s+", "");
//            String normalizedXmlString = m4DRegistryResponseDto.recall.getXmlFile().getContent().replaceAll("\\s+", "");
//            if (!StringUtils.difference(normalizedResource, normalizedXmlString).isEmpty()) {
//                throw new BadRequestException(MRPA_RECALL_NOT_VERIFY_CONTENT);
//            }
//        }
        if (verify) {
            containerService.verify(authentication, container, m4DRegistryResponseDto.recall.getXmlFile().getContent());
        }
        M4dFile m4dFile = new M4dFile();
        m4dFile.setFileMimeType(FileMimeTypeEnum.fromValue(container.getContentType()));
        m4dFile.setFileLogicalType(FileLogicalTypeEnum.CONTAINER);
        m4dFile.setFilename(container.getOriginalFilename());
        String base64Content = Base64.getEncoder().encodeToString(container.getBytes());
        m4dFile.setContent(base64Content);
        M4dFile existsM4dFile;
        M4dFile m4dFileSaved;
        Long fileId;
        try {
            existsM4dFile = this.m4dFileService.findM4dFileByContent(m4dFile.getContent());
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(e.getMessage());
        }
        if (existsM4dFile == null){
            try {
                m4dFileSaved = this.m4dFileService.saveM4dFile(m4dFile);
            } catch (ConstraintViolationException e) {
                throw new BadRequestException(e.getMessage());
            }
            fileId = m4dFileSaved.getId();
        } else {
            fileId = existsM4dFile.getId();
        }
        try {
            this.updateMrpaRecallById(id, fileId);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(e.getMessage());
        }
        return fileId;
    }
}
