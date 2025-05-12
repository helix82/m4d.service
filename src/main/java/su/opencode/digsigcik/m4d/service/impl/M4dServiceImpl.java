package su.opencode.digsigcik.m4d.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import su.opencode.digsigcik.audit.AuditLogEventEnum;
import su.opencode.digsigcik.audit.AuditLogThis;
import su.opencode.digsigcik.m4d.dto.*;
import su.opencode.digsigcik.m4d.dto.response.M4dFnsDataResponse;
import su.opencode.digsigcik.m4d.enums.FileLogicalTypeEnum;
import su.opencode.digsigcik.m4d.enums.FileMimeTypeEnum;
import su.opencode.digsigcik.m4d.enums.StatusRequestTypeEnum;
import su.opencode.digsigcik.m4d.exception.BadRequestException;
import su.opencode.digsigcik.m4d.exception.M4dException;
import su.opencode.digsigcik.m4d.model.*;
import su.opencode.digsigcik.m4d.repository.M4dCredentialRepository;
import su.opencode.digsigcik.m4d.repository.M4dRepository;
import su.opencode.digsigcik.m4d.service.*;
import su.opencode.digsigcik.m4d.util.*;

import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static su.opencode.digsigcik.m4d.util.Constants.*;
import static su.opencode.digsigcik.m4d.util.EncodingUtils.isBase64;
import static su.opencode.digsigcik.m4d.util.JwtTokenUtils.getTokenValue;
import static su.opencode.digsigcik.m4d.util.XMLUtil.convertStringToDocument;

@Slf4j
@Service
@Primary
@RequiredArgsConstructor
@EnableTransactionManagement
public class M4dServiceImpl implements M4dService {
    private final ModelMapper modelMapper;
    private final M4dRepository m4dRepository;
    private final M4dRecallService m4dRecallService;
    private final M4dCredentialRepository m4dCredentialRepository;
    private final M4dFileService m4dFileService;
    private final M4dRequestService m4dRequestService;
    private final CredentialService credentialService;
    private final PrincipalService principalService;
    private final PrincipalDetailService principalDetailService;
    private final RepresentativeService representativeService;
    private final RepresentativeDetailService representativeDetailService;
    private final UserCertificateService userCertificateService;
    private final RefBookClientService refBookClientService;
    private final M4dConfirmService m4dConfirmService;

    @Override
    public List<M4dRegistryResponseDto> findAllMrpaRegistry(Authentication authentication) throws ParseException {
        List<M4d> m4ds = m4dRepository.findAll();
        List<M4dRegistryResponseDto> m4DRegistryResponseDtoList = new ArrayList<>();
        for (M4d m4d : m4ds) {
            m4DRegistryResponseDtoList.add(convertM4dToM4dResponseDto(authentication, m4d, false, false, false, false));
        }
        return m4DRegistryResponseDtoList;
    }

    @Override
    public M4d saveMrpaRegistry(@NotNull M4d m4d) {
        return this.m4dRepository.save(m4d);
    }

    @Override
    public M4d updateM4d(@NotNull M4d m4d) {
        return this.m4dRepository.save(m4d);
    }

    @Override
    public M4d updateStatusM4d(Long id, String status) {
        this.m4dRepository.updateStatusM4d(id, status);
        return this.m4dRepository.findM4dById(id);
    }

    @Transactional(rollbackFor = {Exception.class}, isolation = Isolation.READ_UNCOMMITTED)
    @AuditLogThis(event = AuditLogEventEnum.M4D_CREATE)
    public Long saveAllDataMrpaRegistry(Authentication authentication, M4d m4d, OrganizationDto organisation, Principal principal, PrincipalDetail principalDetail, Representative representative, RepresentativeDetail representativeDetail, List<CredentialDto> credentialList, List<Long> credentials, String xmlString) throws JAXBException, IOException {
        String bearerToken = getTokenValue(authentication);
        String orgInn;
        Long principId;
        Long principDetailId;
        Long represId;
        Long represDetailId;
        if (organisation.getInn() == null) {
            throw new BadRequestException("The organization's INN is not specified");
        }
        if (principal.getInn() == null) {
            throw new BadRequestException("The principal's INN is not specified");
        }
        if (representative.getInn() == null) {
            throw new BadRequestException("The representative's INN is not specified");
        }
        if (representativeDetail.getSeriesNumber() == null) {
            throw new BadRequestException("The passport's SeriesNumber is not specified");
        }
        //------------------- save organisation ----------------
        OrganizationDto existsOrganisation;
        try {
            existsOrganisation = this.refBookClientService.findByOrganisationInn(organisation.getInn(), bearerToken);
            assert existsOrganisation != null;
            orgInn = existsOrganisation.getInn();
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(e.getMessage());
        }
        //------------------- end save organisation ----------------

        //------------------- save principal ----------------
        Principal respPrincipal;
        try {
            respPrincipal = this.principalService.savePrincipal(principal);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(e.getMessage());
        }
        assert respPrincipal != null;
        principId = respPrincipal.getId();
        //------------------- end save principal ----------------

        //------------------- save principal detail ----------------
        PrincipalDetail respPrincipalDetail;
        try {
            principalDetail.setVersion(1L);
            principalDetail.setActive(true);
            principalDetail.setPrincipalId(principId);
            respPrincipalDetail = this.principalDetailService.savePrincipalDetail(principalDetail);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(e.getMessage());
        }
        assert respPrincipalDetail != null;
        principDetailId = respPrincipalDetail.getId();
        //------------------- end save principal detail ----------------

        //------------------- save representative ----------------
        Representative respRepresentative;
        try {
            respRepresentative = this.representativeService.saveRepresentative(representative);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(e.getMessage());
        }
        assert respRepresentative != null;
        represId = respRepresentative.getId();
        //------------------- end save representative ----------------

        //------------------- save representative detail ----------------
        RepresentativeDetail respRepresDetail;
        try {
            representativeDetail.setVersion(1L);
            representativeDetail.setActive(true);
            representativeDetail.setRepresentativeId(represId);
            respRepresDetail = this.representativeDetailService.saveRepresentativeDetail(representativeDetail);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(e.getMessage());
        }
        assert respRepresDetail != null;
        represDetailId = respRepresDetail.getId();
        //------------------- end save representative detail ----------------

        if (orgInn == null || principId == null || principDetailId == null || represId == null) {
            throw new BadRequestException(MRPA_REGISTRY_NOT_FULL);
        }
        m4d.setOrganisationInn(orgInn);
        m4d.setPrincipalId(principId);
        m4d.setPrincipalDetailId(principDetailId);
        m4d.setRepresentativeId(represId);
        m4d.setRepresentativeDetailId(represDetailId);
        if (m4d.getStatus()==null){
            m4d.setStatus("DRAFT");
        }

        String idFile = XMLUtil.getSubstringFromXml(xmlString, "ИдФайл=\"");

        M4dFile m4dFile = new M4dFile();
        m4dFile.setFileMimeType(FileMimeTypeEnum.XML);
        m4dFile.setFileLogicalType(FileLogicalTypeEnum.DOCUMENT);
        m4dFile.setFilename(idFile + ".xml");
        String base64Content = Base64.getEncoder().encodeToString(xmlString.getBytes());
        m4dFile.setContent(base64Content);
        M4dFile existsM4dFile;
        try {
            existsM4dFile = this.m4dFileService.findM4dFileByContent(m4dFile.getContent());
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(e.getMessage());
        }
        m4d.setXmlFile(Objects.requireNonNullElse(existsM4dFile, m4dFile));
        M4d m4dSaved;
        try {
            m4dSaved = this.m4dRepository.save(m4d);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(e.getMessage());
        }
        List<Long> credList = new ArrayList<>();
        List<Long> credListNew;
        if (credentialList != null) {
            for (CredentialDto c : credentialList) {
                List<Credential> existsCredentials = this.credentialService.findCredentialByCodeOrName(c.getCode());
                assert existsCredentials != null;
                if (existsCredentials.isEmpty()) {
                    Credential newCredential = new Credential();
                    newCredential.setCode(c.getCode());
                    newCredential.setName(c.getName());
                    Credential credentialSave;
                    try {
                        credentialSave = this.credentialService.saveCredential(newCredential);
                    } catch (ConstraintViolationException e) {
                        throw new BadRequestException(e.getMessage());
                    }
                    credList.add(credentialSave.getId());
                } else {
                    for (Credential crd : existsCredentials) {
                        credList.add(crd.getId());
                    }
                }
            }
        } else if (credentials != null) {
            credList = credentials;
        }

        if (!credList.isEmpty()) {
            for (Long x : credList) {
                if (this.credentialService.findByCredentialId(x) == null) {
                    throw new BadRequestException(CREDENTIAL_NOT_FOUND);
                }
            }

            credListNew = credList.stream().distinct().collect(Collectors.toList());

            for (Long x : credListNew) {
                M4dCredential m4dCredential = new M4dCredential();
                m4dCredential.setMrpaRegistryId(m4dSaved.getId());
                m4dCredential.setCredentialId(x);
                try {
                    this.m4dCredentialRepository.save(m4dCredential);
                } catch (ConstraintViolationException e) {
                    throw new BadRequestException(e.getMessage());
                }
            }
        }
        String internalNumber = XMLUtil.getSubstringFromXml(xmlString, "ВнНомДовер=\"");
        M4dConfirm m4dConfirm = this.m4dConfirmService.createAndSignedM4dConfirm(authentication, m4d, idFile, m4dSaved.getGuid(), internalNumber, m4dSaved.getStartDate().toString().substring(0, 10), m4dSaved.getEndDate().toString().substring(0, 10));
        return m4dSaved.getId();
    }

    @AuditLogThis(event = AuditLogEventEnum.M4D_UPDATE)
    @Transactional(rollbackFor = {Exception.class}, isolation = Isolation.READ_UNCOMMITTED)
    public Long updateAllDataMrpaRegistry(Authentication authentication, M4d m4d, OrganizationDto organisation, Principal principal, PrincipalDetail principalDetail, Representative representative, RepresentativeDetail representativeDetail, List<CredentialDto> credentialList, List<Long> credentials) {
        String bearerToken = getTokenValue(authentication);
        String orgInn;
        Long principId;
        Long principDetailId;
        Long represId;
        Long represDetailId;
        if (m4d.getId() == null) {
            throw new BadRequestException("Can`t update mrpa registry because ID null!");
        }
        if (organisation.getInn() == null) {
            throw new BadRequestException("The organization's INN is not specified");
        }
        if (principal.getInn() == null) {
            throw new BadRequestException("The principal's INN is not specified");
        }
        if (representative.getInn() == null) {
            throw new BadRequestException("The representative's INN is not specified");
        }
        M4d existMrpaRegistry = this.findById(m4d.getId());
        if (existMrpaRegistry == null) {
            throw new BadRequestException(String.format("Can`t update. Mrpa registry with id=%d %s", m4d.getId(), NOT_FOUND));
        }
        //------------------- save organisation ----------------
        OrganizationDto existsOrganisation;
        try {
            existsOrganisation = this.refBookClientService.findByOrganisationInn(organisation.getInn(), bearerToken);
            assert existsOrganisation != null;
            orgInn = existsOrganisation.getInn();
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(e.getMessage());
        }
        //------------------- end save organisation ----------------

        //------------------- save principal ----------------
        Principal existsPrincipal;
        try {
            existsPrincipal = this.principalService.findByPrincipalId(m4d.getPrincipalId());
            if (existsPrincipal != null) {
                principal.setId(existsPrincipal.getId());
            }
            Principal respPrincipal;
            try {
                respPrincipal = this.principalService.savePrincipal(principal);
            } catch (ConstraintViolationException e) {
                throw new BadRequestException(e.getMessage());
            }
            principId = respPrincipal.getId();
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(e.getMessage());
        }
        //------------------- end save principal ----------------

        //------------------- save principal detail ----------------
        PrincipalDetail existsPrincipalDetail;
        try {
            existsPrincipalDetail = this.principalDetailService.findByPrincipalDetailId(m4d.getPrincipalDetailId());
            if (existsPrincipalDetail != null && existsPrincipalDetail.isActive()) {
                if (!existsPrincipalDetail.getJobTitle().equals(principalDetail.getJobTitle()) || !existsPrincipalDetail.getName().equals(principalDetail.getName()) ||
                !existsPrincipalDetail.getSurname().equals(principalDetail.getSurname()) || !existsPrincipalDetail.getPatronymic().equals(principalDetail.getPatronymic())) {
                    PrincipalDetail respPrincipalDetail;
                    try {
                        principalDetail.setVersion(existsPrincipalDetail.getVersion()+1);
                        principalDetail.setActive(true);
                        principalDetail.setPrincipalId(principId);
                        respPrincipalDetail = this.principalDetailService.savePrincipalDetail(principalDetail);
                    } catch (ConstraintViolationException e) {
                        throw new BadRequestException(e.getMessage());
                    }
                    try {
                        existsPrincipalDetail.setActive(false);
                        this.principalDetailService.savePrincipalDetail(existsPrincipalDetail);
                    } catch (ConstraintViolationException e) {
                        throw new BadRequestException(e.getMessage());
                    }
                    principDetailId = respPrincipalDetail.getId();
                } else principDetailId = existsPrincipalDetail.getId();
            } else {
                PrincipalDetail respPrincipalDetail;
                try {
                    principalDetail.setVersion(1L);
                    principalDetail.setActive(true);
                    principalDetail.setPrincipalId(principId);
                    respPrincipalDetail = this.principalDetailService.savePrincipalDetail(principalDetail);
                } catch (ConstraintViolationException e) {
                    throw new BadRequestException(e.getMessage());
                }
                principDetailId = respPrincipalDetail.getId();
            }
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(e.getMessage());
        }
        //------------------- end save principal detail ----------------

        //------------------- save representative ----------------
        Representative existsRepresentative;
        try {
            existsRepresentative = this.representativeService.findByRepresentativeId(m4d.getRepresentativeId());
            if (existsRepresentative != null) {
                representative.setId(existsRepresentative.getId());
            }
            Representative respRepresentative;
            try {
                respRepresentative = this.representativeService.saveRepresentative(representative);
            } catch (ConstraintViolationException e) {
                throw new BadRequestException(e.getMessage());
            }
            represId = respRepresentative.getId();
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(e.getMessage());
        }
        //------------------- end save representative ----------------

        //------------------- save representative detail ----------------
        RepresentativeDetail existsRepresentativeDetail;
        try {
            existsRepresentativeDetail = this.representativeDetailService.findByRepresentativeDetailId(m4d.getRepresentativeDetailId());
            if (existsRepresentativeDetail != null && existsRepresentativeDetail.isActive()) {
                if (!existsRepresentativeDetail.getName().equals(representativeDetail.getName()) || !existsRepresentativeDetail.getSurname().equals(representativeDetail.getSurname())
                        || !existsRepresentativeDetail.getPatronymic().equals(representativeDetail.getPatronymic()) || !existsRepresentativeDetail.getSeriesNumber().equals(representativeDetail.getSeriesNumber())
                        || !existsRepresentativeDetail.getDateIssue().equals(representativeDetail.getDateIssue()) || !existsRepresentativeDetail.getWhoIssue().equals(representativeDetail.getWhoIssue())
                        || !existsRepresentativeDetail.getDepartmentCode().equals(representativeDetail.getDepartmentCode())){
                    RepresentativeDetail respRepresentativeDetail;
                    try {
                        representativeDetail.setVersion(existsRepresentativeDetail.getVersion()+1);
                        representativeDetail.setActive(true);
                        representativeDetail.setRepresentativeId(represId);
                        respRepresentativeDetail = this.representativeDetailService.saveRepresentativeDetail(representativeDetail);
                    } catch (ConstraintViolationException e) {
                        throw new BadRequestException(e.getMessage());
                    }
                    try {
                        existsRepresentativeDetail.setActive(false);
                        this.representativeDetailService.saveRepresentativeDetail(existsRepresentativeDetail);
                    } catch (ConstraintViolationException e) {
                        throw new BadRequestException(e.getMessage());
                    }
                    represDetailId = respRepresentativeDetail.getId();
                } else represDetailId = existsRepresentativeDetail.getId();
            } else {
                RepresentativeDetail respRepresentativeDetail;
                try {
                    representativeDetail.setVersion(1L);
                    representativeDetail.setActive(true);
                    representativeDetail.setRepresentativeId(represId);
                    respRepresentativeDetail = this.representativeDetailService.saveRepresentativeDetail(representativeDetail);
                } catch (ConstraintViolationException e) {
                    throw new BadRequestException(e.getMessage());
                }
                represDetailId = respRepresentativeDetail.getId();
            }
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(e.getMessage());
        }
        //------------------- end save representative detail ----------------

        if (orgInn == null || principId == null || principDetailId == null || represId == null || represDetailId == null) {
            throw new BadRequestException(MRPA_REGISTRY_NOT_FULL);
        }
        if (!m4d.getOrganisationInn().equals(orgInn)) {
            m4d.setOrganisationInn(orgInn);
        }
        if (!m4d.getPrincipalId().equals(principId)) {
            m4d.setPrincipalId(principId);
        }
        if (!m4d.getPrincipalDetailId().equals(principDetailId)) {
            m4d.setPrincipalDetailId(principDetailId);
        }
        if (!m4d.getRepresentativeId().equals(represId)) {
            m4d.setRepresentativeId(represId);
        }
        if (!m4d.getRepresentativeDetailId().equals(represDetailId)) {
            m4d.setRepresentativeDetailId(represDetailId);
        }
        M4d m4dUpdated;
        try {
            m4dUpdated = this.m4dRepository.save(m4d);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(e.getMessage());
        }
        List<Long> credList = new ArrayList<>();
        List<Long> credListNew;
        if (credentialList != null) {
            for (CredentialDto c : credentialList) {
                List<Credential> existsCredentials = this.credentialService.findCredentialByCodeOrName(c.getCode());
                assert existsCredentials != null;
                if (existsCredentials.isEmpty()) {
                    Credential newCredential = new Credential();
                    newCredential.setCode(c.getCode());
                    newCredential.setName(c.getName());
                    Credential credentialSave;
                    try {
                        credentialSave = this.credentialService.saveCredential(newCredential);
                    } catch (ConstraintViolationException e) {
                        throw new BadRequestException(e.getMessage());
                    }
                    credList.add(credentialSave.getId());
                } else {
                    for (Credential crd : existsCredentials) {
                        credList.add(crd.getId());
                    }
                }
            }
        } else if (credentials != null) {
            credList = credentials;
        }

        credListNew = credList.stream().distinct().collect(Collectors.toList());

        if (!credList.isEmpty()) {
            this.m4dCredentialRepository.deleteAllByMrpaRegistryId(m4dUpdated.getId());
            for (Long x : credListNew) {
                M4dCredential m4dCredential = new M4dCredential();
                m4dCredential.setMrpaRegistryId(m4dUpdated.getId());
                m4dCredential.setCredentialId(x);
                try {
                    this.m4dCredentialRepository.save(m4dCredential);
                } catch (ConstraintViolationException e) {
                    throw new BadRequestException(e.getMessage());
                }
            }
        }
        return m4dUpdated.getId();
    }

    @Override
    public Integer countM4dByGuid(@NotNull String guid) {
        return this.m4dRepository.countByGuid(guid);
    }

    @Override
    public M4d findMrpaRegistryByGuid(@NotNull String guid) {
        return this.m4dRepository.findByGuid(guid);
    }

    @Override
    public M4d findById(@NotNull Long id) {
        return this.m4dRepository.findM4dById(id);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class}, isolation = Isolation.READ_UNCOMMITTED)
    @AuditLogThis(event = AuditLogEventEnum.M4D_DELETE, deleteByIdRepository = M4dRepository.class)
    public void deleteMrpaRegistry(@NotNull Long id) {
        try {
            M4d m4d = this.m4dRepository.findM4dById(id);
            M4dRecall m4dRecall = this.m4dRecallService.findMrpaRecallByM4dId(m4d.getId());
            if (m4dRecall != null) {
                this.m4dRecallService.deleteMrpaRecallById(m4dRecall.getId());
            }
            this.m4dRepository.deleteById(id);
            List<M4d> m4ds = this.m4dRepository.findByPrincipalId(m4d.getPrincipalId());
            if(m4ds.isEmpty()){
                this.principalService.deletePrincipal(m4d.getPrincipalId());
                this.principalDetailService.deletePrincipalDetailByPrincipalId(m4d.getPrincipalId());
            }
            List<M4d> m4ds1 = this.m4dRepository.findByRepresentativeId(m4d.getRepresentativeId());
            if(m4ds1.isEmpty()){
                this.representativeService.deleteRepresentative(m4d.getRepresentativeId());
                this.representativeDetailService.deleteRepresentativeDetailByRepresId(m4d.getRepresentativeId());
            }
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }

    }

    @Override
    public List<M4dRegistryResponseDto.CredentialDto> getCredentialByMrpaRegistryId(Long mrpaId) {
        List<Credential> credentials = this.credentialService.findCredentialByMrpaId(mrpaId);
        List<M4dRegistryResponseDto.CredentialDto> credentialDTOList = new ArrayList<>();
        assert credentials != null;
        for (Credential c : credentials) {
            M4dRegistryResponseDto.CredentialDto credentialDTO = new M4dRegistryResponseDto.CredentialDto();
            credentialDTO.setId(c.getId());
            credentialDTO.setName(c.getName());
            credentialDTO.setCode(c.getCode());
            credentialDTOList.add(credentialDTO);
        }
        return credentialDTOList;
    }

    /*
     * Берет ИННы сертификатов пользователя, потом по ИНН через ID представителей
     * и заявителей находит все МЧД пользователя (т.е. по всем ИНН (как в Представителях, так и в Заявителях) принадлежащим пользователю находит все МЧД)
     *
     * @param login Логин пользователя. Берется из JWT.
     * @return Возвращает все МЧД пользователя
     */
    @Transactional(readOnly = true)
    @Override
    public List<M4dRegistryResponseDto> findAllM4dUsers(Authentication authentication) throws ParseException {
        Set<String> setINNs = userCertificateService.getUserCertificateINNs(authentication);

        return this.findAllMrpaRegistry(authentication).stream()
                .filter(x -> setINNs.contains(x.principal.getInn()) || setINNs.contains(x.representative.getInn()))
                .peek(m4d -> {
                    try {
                        Long id = m4d.getId();
                        m4d.setStatus(getStatusM4d(id));
                        m4d.setCredentials(getCredentialByMrpaRegistryId(id));
                    } catch (ParseException e) {
                        throw new M4dException(e.getMessage());
                    }
                }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<M4d> findM4dByRepresentativeInn(String inn) {
        return this.m4dRepository.findM4dByRepresentativeInn(inn);
    }

    @Transactional(readOnly = true)
    @Override
    public M4dRegistryResponseDto findM4dByIdAndRepresentativeInn(Authentication authentication, String inn, Long id) {
        return m4dRepository.findValidM4dByIdAndRepresentativeInn(inn, id)
                .map(m4d -> {
                    try {
                        M4dRegistryResponseDto m4DRegistryResponseDto = convertM4dToM4dResponseDto(authentication, m4d, true, false, true, false);
                        m4DRegistryResponseDto.credentials = getCredentialByMrpaRegistryId(m4DRegistryResponseDto.getId());
                        m4DRegistryResponseDto.setStatus(getStatusM4d(m4DRegistryResponseDto.getId()));
                        return m4DRegistryResponseDto;
                    } catch (ParseException e) {
                        throw new BadRequestException(e.getMessage());
                    }
                })
                .orElseThrow(() -> new BadRequestException(String.format(M4D_EXCEPRION_NOT_FOUND, id)));
    }

    public String getStatusM4d(Long id) throws ParseException {
        M4d mrpaRegistry = this.m4dRepository.findM4dById(id);
        Date currentDT = Date.from(Instant.now());
        Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse(String.valueOf(mrpaRegistry.getStartDate()));
        Date endDate = new SimpleDateFormat("yyyy-MM-dd").parse(String.valueOf(mrpaRegistry.getEndDate()));
        if (Objects.equals(mrpaRegistry.getStatus(), "ACTIVE")) {
            if (currentDT.before(startDate)) {
                return "REGISTERED";
            } else if (currentDT.after(endDate)) {
                return "EXPIRED";
            } else {
                return mrpaRegistry.getStatus();
            }
        } else {
            return mrpaRegistry.getStatus();
        }
    }

    public M4dRegistryResponseDto convertM4dToM4dResponseDto(Authentication authentication, M4d m4d, Boolean withM4dSigFile, Boolean withRecallSigFile, Boolean withM4dXmlFile, Boolean withRecallXmlFile) throws ParseException {
        String bearerToken = getTokenValue(authentication);
        M4dRegistryResponseDto m4DRegistryResponseDto = new M4dRegistryResponseDto();
        m4DRegistryResponseDto.setGuid(m4d.getGuid());
        m4DRegistryResponseDto.setStartDate(m4d.getStartDate().toString());
        m4DRegistryResponseDto.setEndDate(m4d.getEndDate().toString());
        m4DRegistryResponseDto.setErrorDetail(m4d.getErrorDetail());
        m4DRegistryResponseDto.setId(m4d.getId());
        m4DRegistryResponseDto.setCreateDate(m4d.getCreateDate().toString());
        if (m4d.getSigFile() != null) {
            M4dFile sigFile;
            try {
                sigFile = this.m4dFileService.findM4dFileById(m4d.getSigFile().getId());
                if (sigFile != null) {
                    M4dRegistryResponseDto.SigFileDto sigFileDTO = convertSigFileToDto(sigFile);
                    if (isBase64(sigFileDTO.getContent())) {
                        sigFileDTO.setContent(new String(Base64.getDecoder().decode(sigFileDTO.getContent())));
                    } else {
                        sigFileDTO.setContent(sigFileDTO.getContent());
                    }
                    if (!withM4dSigFile) {
                        sigFileDTO.setContent(null);
                    }
                    m4DRegistryResponseDto.setSigFile(sigFileDTO);
                }
            } catch (Exception e) {
                throw new BadRequestException(e.getMessage());
            }
        }
        if (m4d.getXmlFile() != null) {
            M4dFile xmlFile;
            try {
                xmlFile = this.m4dFileService.findM4dFileById(m4d.getXmlFile().getId());
                if (xmlFile != null) {
                    M4dRegistryResponseDto.XmlFileDto xmlFileDTO = convertXmlFileToDto(xmlFile);
                    if (isBase64(xmlFileDTO.getContent())) {
                        xmlFileDTO.setContent(new String(Base64.getDecoder().decode(xmlFileDTO.getContent())));
                    } else {
                        xmlFileDTO.setContent(xmlFileDTO.getContent());
                    }
                    if (!withM4dXmlFile) {
                        xmlFileDTO.setContent(null);
                    }
                    m4DRegistryResponseDto.setXmlFile(xmlFileDTO);
                }
            } catch (Exception e) {
                throw new BadRequestException(e.getMessage());
            }
        }
        try {
            M4dRecall m4dRecall = this.m4dRecallService.findMrpaRecallByM4dId(m4d.getId());
            if (m4dRecall != null) {
                M4dRegistryResponseDto.RecallDto recallDto = new M4dRegistryResponseDto.RecallDto();
                recallDto.setCreateDate(m4dRecall.getCreateDate().toString());
                recallDto.setId(m4dRecall.getId());
                recallDto.setStatus(m4dRecall.getStatus());
                recallDto.setErrorDetail(m4dRecall.getErrorDetail());
                m4DRegistryResponseDto.setRecall(recallDto);
                if (m4dRecall.getSigFile() != null){
                    M4dFile m4dFileRecall;
                        m4dFileRecall = this.m4dFileService.findM4dFileById(m4dRecall.getSigFile().getId());
                        if (m4dFileRecall != null) {
                            M4dRegistryResponseDto.RecallDto.SigFileRecallDto sigFileDTO = convertSigFileRecallToDto(m4dFileRecall);
                            if (isBase64(sigFileDTO.getContent())) {
                                sigFileDTO.setContent(new String(Base64.getDecoder().decode(sigFileDTO.getContent())));
                            } else {
                                sigFileDTO.setContent(sigFileDTO.getContent());
                            }
                            if (!withRecallSigFile) {
                                sigFileDTO.setContent(null);
                            }
                            m4DRegistryResponseDto.recall.setSigFile(sigFileDTO);
                        }
                }
                if (m4dRecall.getXmlFile() != null) {
                    M4dFile xmlFile;
                        xmlFile = this.m4dFileService.findM4dFileById(m4dRecall.getXmlFile().getId());
                        if (xmlFile != null) {
                            M4dRegistryResponseDto.RecallDto.XmlFileRecallDto xmlFileDTO = convertXmlFileRecallToDto(xmlFile);
                            if (isBase64(xmlFileDTO.getContent())) {
                                xmlFileDTO.setContent(new String(Base64.getDecoder().decode(xmlFileDTO.getContent())));
                            } else {
                                xmlFileDTO.setContent(xmlFileDTO.getContent());
                            }
                            if (!withRecallXmlFile) {
                                xmlFileDTO.setContent(null);
                            }
                            m4DRegistryResponseDto.recall.setXmlFile(xmlFileDTO);
                            m4DRegistryResponseDto.recall.setArgument(getArgumentFromXml(xmlFile));
                        }
                }
            }
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }
        OrganizationDto organisation = this.refBookClientService.findByOrganisationInn(m4d.getOrganisationInn(), bearerToken);
        if (organisation != null) {
            m4DRegistryResponseDto.setOrganization(convertOrganisationToDto(organisation));
        }
        Principal principal = this.principalService.findByPrincipalId(m4d.getPrincipalId());
        if (principal != null) {
            m4DRegistryResponseDto.setPrincipal(convertPrincipalToDto(principal));
        }
        PrincipalDetail principalDetail = this.principalDetailService.findByPrincipalDetailId(m4d.getPrincipalDetailId());
        if (principalDetail != null) {
            m4DRegistryResponseDto.setPrincipalDetail(convertPrincipalDetailToDto(principalDetail));
        }
        Representative representative = this.representativeService.findByRepresentativeId(m4d.getRepresentativeId());
        if (representative != null) {
            m4DRegistryResponseDto.setRepresentative(convertRepresentativeToDto(representative));
            RepresentativeDetail representativeDetail = this.representativeDetailService.findRepresentativeDetailByRepresentativeId(representative.getId());
            m4DRegistryResponseDto.setRepresentativeDetail(convertRepresentativePassportToDto(representativeDetail));
        }
        m4DRegistryResponseDto.setCredentials(this.getCredentialByMrpaRegistryId(m4d.getId()));
        m4DRegistryResponseDto.setStatus(this.getStatusM4d(m4DRegistryResponseDto.getId()));
        return m4DRegistryResponseDto;
    }

    @Override
    @AuditLogThis(event = AuditLogEventEnum.M4D_REQUEST_SEARCH_FIND_RR_CREATE)
    public ReportCheckStatusM4dDto createM4dRequestSearchForFindRR(M4dXml m4dXml, String hashXml) {
        String guidXml = this.getGuidFromXml(m4dXml);
        // - формирование запроса в РР МЧД на проверку статуса МЧД (если результаты предыдущего отрицательные)
        M4dRequestSearch m4dRequestSearchNew = new M4dRequestSearch();
        m4dRequestSearchNew.setGuid(guidXml);
        m4dRequestSearchNew.setCreateDate(LocalDateTime.now());
        m4dRequestSearchNew.setHashXml(hashXml);
        M4dRequestSearch m4dRequestSearchSaved = this.m4dRequestService.saveM4dRequestSearch(m4dRequestSearchNew);
        M4dRequestSearchDetail m4dRequestSearchDetail = new M4dRequestSearchDetail();
        m4dRequestSearchDetail.setStatus(StatusRequestTypeEnum.ONCHECK.toString());
        m4dRequestSearchDetail.setStartDate(DateUtils.convertStringToLocalDateTime(getStartDateM4dFromXml(m4dXml)));
        m4dRequestSearchDetail.setEndDate(DateUtils.convertStringToLocalDateTime(getEndDateM4dFromXml(m4dXml)));
        m4dRequestSearchDetail.setVersion(1L);
        m4dRequestSearchDetail.setActive(true);
        m4dRequestSearchDetail.setRequestSearchId(m4dRequestSearchSaved.getId());
        M4dRequestSearchDetail m4dRequestSearchDetailSaved;
        m4dRequestSearchDetailSaved = this.m4dRequestService.saveM4dRequestSearchDetail(m4dRequestSearchDetail);
        if (m4dRequestSearchDetailSaved != null) {
            ReportCheckStatusM4dDto report = new ReportCheckStatusM4dDto();
            report.setSource("m4d_rr");
            ReportCheckStatusM4dDto.Report rep = new ReportCheckStatusM4dDto.Report();
            report.setReport(rep);
            rep.setGuid(guidXml);
            rep.setStatus(m4dRequestSearchDetail.getStatus());
            rep.setStartDate(getStartDateM4dFromXml(m4dXml));
            rep.setEndDate(getEndDateM4dFromXml(m4dXml));
            rep.setCredentials(getCredentialM4dFromXml(m4dXml));
            rep.setRepresentative(getRepresentativeM4dFromXml(m4dXml));
            rep.setPrincipal(getPrincipalM4dFromXml(m4dXml));
            rep.setInnOrganisation(getInnOrganizationFromXml(m4dXml));
            rep.setNameOrganisation(getNameOrganizationFromXml(m4dXml));
            rep.setErrorDetail("Для проверки статуса обратитесь к ПВВ адаптеру");
            return report;
        }
        return null;
    }

    @Override
    @AuditLogThis(event = AuditLogEventEnum.M4D_CHECK_IN_REQUEST_SEARCH)
    public ReportCheckStatusM4dDto getReportM4dRequestSearchForFindPep(M4dXml m4dXml, String hashXml) {
        String guidXml = this.getGuidFromXml(m4dXml);
        M4dRequestSearch m4dRequestSearch;
        m4dRequestSearch = this.m4dRequestService.findM4dRequestByGuidAndHash(guidXml, hashXml);
        if (m4dRequestSearch != null) {
            M4dRequestSearchDetail m4dRequestSearchDetail;
            m4dRequestSearchDetail = this.m4dRequestService.findM4dRequestDetailByM4dRequestId(m4dRequestSearch.getId());
            if(m4dRequestSearchDetail == null) {
                throw new BadRequestException("Активных деталей запроса статуса МЧД в РР МЧД не найдено");
            }
            ReportCheckStatusM4dDto report = new ReportCheckStatusM4dDto();
            report.setSource("m4d_rr");
            ReportCheckStatusM4dDto.Report rep = new ReportCheckStatusM4dDto.Report();
            report.setReport(rep);
            rep.setGuid(guidXml);
            rep.setStatus(m4dRequestSearchDetail.getStatus());
            rep.setStartDate(getStartDateM4dFromXml(m4dXml));
            rep.setEndDate(getEndDateM4dFromXml(m4dXml));
            rep.setCredentials(getCredentialM4dFromXml(m4dXml));
            rep.setRepresentative(getRepresentativeM4dFromXml(m4dXml));
            rep.setPrincipal(getPrincipalM4dFromXml(m4dXml));
            rep.setInnOrganisation(getInnOrganizationFromXml(m4dXml));
            rep.setNameOrganisation(getNameOrganizationFromXml(m4dXml));
            return report;
        }
        return null;
    }

    @Override
    @AuditLogThis(event = AuditLogEventEnum.M4D_CHECK_IN_PEP)
    public ReportCheckStatusM4dDto checkM4dInPep(Authentication authentication, M4dXml m4dXml) throws ParseException {
        // - извлечение номера МЧД из XML
        String guidXml;
        guidXml = this.getGuidFromXml(m4dXml);
        M4d m4d;
        try {
            m4d = this.findMrpaRegistryByGuid(guidXml);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(e.getMessage());
        }
        if (m4d != null) { //мчд в пэп найдено
            ReportCheckStatusM4dDto report = new ReportCheckStatusM4dDto();
            M4dRegistryResponseDto m4dRegistryResponseDto = this.convertM4dToM4dResponseDto(authentication, m4d, true, false, false, false);
            report.setSource("m4d_pep");
            ReportCheckStatusM4dDto.Report rep = new ReportCheckStatusM4dDto.Report();
            report.setReport(rep);
            rep.setGuid(getGuidFromXml(m4dXml));
            Date currentDT = Date.from(Instant.now());
            Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse(String.valueOf(m4dRegistryResponseDto.getStartDate()));
            Date endDate = new SimpleDateFormat("yyyy-MM-dd").parse(String.valueOf(m4dRegistryResponseDto.getEndDate()));
            if (Objects.equals(m4dRegistryResponseDto.getStatus(), "ACTIVE")) {
                if (currentDT.before(startDate)) {
                    rep.setStatus("REGISTERED");
                } else if (currentDT.after(endDate)) {
                    rep.setStatus("EXPIRED");
                } else {
                    rep.setStatus(m4dRegistryResponseDto.getStatus());
                }
            } else {
                rep.setStatus(m4dRegistryResponseDto.getStatus());
            }
            rep.setStartDate(getStartDateM4dFromXml(m4dXml));
            rep.setEndDate(getEndDateM4dFromXml(m4dXml));
            rep.setCredentials(getCredentialM4dFromXml(m4dXml));
            if (m4dRegistryResponseDto.getSigFile() != null) {
                ReportCheckStatusM4dDto.Report.SigFileDto sigFile = new ReportCheckStatusM4dDto.Report.SigFileDto();
                sigFile.setId(m4dRegistryResponseDto.getSigFile().getId());
                sigFile.setFileLogicalType(m4dRegistryResponseDto.getSigFile().getFileLogicalType().getValue());
                sigFile.setFileMimeType(m4dRegistryResponseDto.getSigFile().getFileMimeType().getValue());
                sigFile.setFilename(m4dRegistryResponseDto.getSigFile().getFilename());
                rep.setSigFile(sigFile);
            }
            List<ReportCheckStatusM4dDto.Report.Representative> representativeList = new ArrayList<>();
            ReportCheckStatusM4dDto.Report.Representative representative = new ReportCheckStatusM4dDto.Report.Representative();
            String fioRepres = m4dRegistryResponseDto.representativeDetail.getSurname() + " "
                    + m4dRegistryResponseDto.representativeDetail.getName() + " "
                    + m4dRegistryResponseDto.representativeDetail.getPatronymic();
            representative.setInn(m4dRegistryResponseDto.representative.getInn());
            representative.setFio(fioRepres);
            representativeList.add(representative);
            rep.setRepresentative(representativeList);


            List<ReportCheckStatusM4dDto.Report.Principal> principalList = new ArrayList<>();
            ReportCheckStatusM4dDto.Report.Principal principal = new ReportCheckStatusM4dDto.Report.Principal();
            String fioPrincipal = m4dRegistryResponseDto.principalDetail.getSurname() + " "
                    + m4dRegistryResponseDto.principalDetail.getName() + " "
                    + m4dRegistryResponseDto.principalDetail.getPatronymic();
            principal.setInn(m4dRegistryResponseDto.principal.getInn());
            principal.setFio(fioPrincipal);
            principalList.add(principal);
            rep.setPrincipal(principalList);

            rep.setInnOrganisation(m4dRegistryResponseDto.organization.getInn());
            rep.setNameOrganisation(m4dRegistryResponseDto.organization.getName());
            return report;
        }
        return null;
    }

    public M4dParseResponseDto parse(MultipartFile sourceFile) throws IOException, SAXException {
        List<SAXParseException> res = new XmlValidator(sourceFile.getInputStream(), "EMCHD_1").listParsingExceptions();
        if (res.isEmpty()) {
            int guid;
            M4dXml m4dXml = this.convertM4dFileToM4dXml(sourceFile);
            guid = this.countM4dByGuid(this.getGuidFromXml(m4dXml));
            if (guid > 0) {
                throw new BadRequestException(MRPA_REGISTRY_IS_EXISTS);
            }
            M4d m4d = new M4d();
            //парсинг МЧД из xml
            m4d.setGuid(m4dXml.document.powerAttorneys.mrpa.getGuid());
            String dateCreate = m4dXml.getIdFile().substring(9, 17);
            m4d.setCreateDate(DateUtils.convertStringToLocalDateTime(dateCreate));
            m4d.setStatus("DRAFT");
            m4d.setStartDate(DateUtils.convertStringToLocalDateTime(m4dXml.document.powerAttorneys.mrpa.getStartDate()));
            m4d.setEndDate(DateUtils.convertStringToLocalDateTime(m4dXml.document.powerAttorneys.mrpa.getEndDate()));

            OrganizationDto organisation = new OrganizationDto();
            organisation.setInn(m4dXml.document.powerAttorneys.principals.principal.legalEntity.organisation.getInn());
            organisation.setKpp(m4dXml.document.powerAttorneys.principals.principal.legalEntity.organisation.getKpp());
            organisation.setOgrn(m4dXml.document.powerAttorneys.principals.principal.legalEntity.organisation.getOgrn());
            organisation.setName(m4dXml.document.powerAttorneys.principals.principal.legalEntity.organisation.getName());
            organisation.setRegionCode(m4dXml.document.powerAttorneys.principals.principal.legalEntity.organisation.addressRegion.getRegionCode());
            organisation.setLegalAddress(m4dXml.document.powerAttorneys.principals.principal.legalEntity.organisation.addressRegion.getLegalAddress());
            if (organisation.getInn() == null || organisation.getInn().isEmpty()) {
                throw new BadRequestException(ORGANISATION_IS_NULL);
            }
            Principal principal = new Principal();
            principal.setInn(m4dXml.document.powerAttorneys.principals.principal.legalEntity.principal.principal.getInn());
            principal.setSnils(m4dXml.document.powerAttorneys.principals.principal.legalEntity.principal.principal.getSnils());
            PrincipalDetail principalDetail = new PrincipalDetail();
            principalDetail.setJobTitle(m4dXml.document.powerAttorneys.principals.principal.legalEntity.principal.principal.getJobTitle());
            principalDetail.setName(m4dXml.document.powerAttorneys.principals.principal.legalEntity.principal.principal.fioPrincipal.fio.getName());
            principalDetail.setSurname(m4dXml.document.powerAttorneys.principals.principal.legalEntity.principal.principal.fioPrincipal.fio.getSurname());
            principalDetail.setPatronymic(m4dXml.document.powerAttorneys.principals.principal.legalEntity.principal.principal.fioPrincipal.fio.getPatronymic());
            if (principal.getInn() == null || principal.getInn().isEmpty()) {
                throw new BadRequestException(PRINCIPAL_IS_NULL);
            }
            Representative representative = new Representative();
            RepresentativeDetail represDetail = new RepresentativeDetail();
            for (M4dXml.Document.PowerAttorneys.InfoRepresentative c: m4dXml.document.powerAttorneys.infoRepresentative) {
                if (Objects.equals(c.getTypeRepresentative(), "3")) {
                    if (c.representative.detailRepresentative != null) {
                        representative.setInn(c.representative.detailRepresentative.getInn());
                        representative.setSnils(c.representative.detailRepresentative.getSnils());
                        representative.setBirthDate(DateUtils.convertStringToLocalDateTime(c.representative.detailRepresentative.infoIndividual.getBirthDate()));
                        represDetail.setName(c.representative.detailRepresentative.infoIndividual.fioRepresentative.getName());
                        represDetail.setSurname(c.representative.detailRepresentative.infoIndividual.fioRepresentative.getSurname());
                        represDetail.setPatronymic(c.representative.detailRepresentative.infoIndividual.fioRepresentative.getPatronymic());
                        if (representative.getInn() == null || representative.getInn().isEmpty()) {
                            throw new BadRequestException(REPRESENTATIVE_IS_NULL);
                        }
                        represDetail.setSeriesNumber(c.representative.detailRepresentative.infoIndividual.representativePassport.getSeriesNumber());
                        represDetail.setDateIssue(DateUtils.convertStringToLocalDateTime(c.representative.detailRepresentative.infoIndividual.representativePassport.getDateIssue()));
                        represDetail.setWhoIssue(c.representative.detailRepresentative.infoIndividual.representativePassport.getWhoIssue());
                        represDetail.setDepartmentCode(c.representative.detailRepresentative.infoIndividual.representativePassport.getDepartmentCode());
                    }
                }
            }
            List<CredentialDto> credentialList = new ArrayList<>();
            if (m4dXml.document.powerAttorneys.credential.credentials != null) {
                for (M4dXml.Document.PowerAttorneys.Credentials.Credential c : m4dXml.document.powerAttorneys.credential.credentials) {
                    CredentialDto newCredential = new CredentialDto();
                    newCredential.setCode(c.getCode());
                    newCredential.setName(c.getName());
                    credentialList.add(newCredential);
                }
            }
            return new M4dParseResponseDto(m4d, organisation, principal, principalDetail, representative, represDetail, credentialList, XMLUtil.readXmlToString(sourceFile));
        }
        return null;
    }

    @Override
    @AuditLogThis(event = AuditLogEventEnum.M4D_SIGNATURE)
    public Long uploadSig(Long mrpaId, MultipartFile signature){
        M4d m4d;
        try {
            m4d = this.findById(mrpaId);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(e.getMessage());
        }
        if (m4d == null) {
            throw new BadRequestException(MRPA_REGISTRY_IS_NULL);
        }
        M4dFile m4dFile = this.m4dFileService.fillM4dFile(FileMimeTypeEnum.SIG, FileLogicalTypeEnum.SIGNATURE, signature.getOriginalFilename(), signature.toString());
        m4d.setSigFile(m4dFile);
        m4d.setStatus("ONCHECK");

        M4d m4dSaved;
        try {
            m4dSaved = this.saveMrpaRegistry(m4d);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(e.getMessage());
        }
        return m4dSaved.getSigFile().getId();
    }

    @Override
    public M4dFullXML convertM4dRequestDtoToFullXml(M4dRegistryRequestDto m4DRegistryRequestDto) {
        String idFile = "ON_EMCHD_" + m4DRegistryRequestDto.getCreateDate().substring(0, 10).replace("-", "") + "_" + m4DRegistryRequestDto.mrpaRegistry.getGuid();
        M4dFullXML m4dFullXML = new M4dFullXML();
        m4dFullXML.setXmlnsXsi("http://www.w3.org/2001/XMLSchema-instance");
        m4dFullXML.setXmlnsXsd("http://www.w3.org/2001/XMLSchema");
        m4dFullXML.setXmlns("urn://x-artefacts/EMCHD_1");
        m4dFullXML.setVersionFormat("EMCHD_1");
        m4dFullXML.setElementsFormat("00000000");
        m4dFullXML.setIdFile(idFile); //ON_EMCHD_20240516_75655b80-cc58-4a2b-b244-8334eb895a2f


        M4dFullXML.Document.PowerAttorneys.Mrpa mrpas = new M4dFullXML.Document.PowerAttorneys.Mrpa();
        mrpas.setTypePowerAttorney("1");
        mrpas.setRetrust("3");

        if (Objects.isNull(m4DRegistryRequestDto.mrpaRegistry.getInternalNumber())) {
            mrpas.setInternalNumber("б/н");
        } else mrpas.setInternalNumber(m4DRegistryRequestDto.mrpaRegistry.getInternalNumber());

        mrpas.setGuid(m4DRegistryRequestDto.mrpaRegistry.getGuid());

        if (m4DRegistryRequestDto.mrpaRegistry.getStartDate() != null) {
            mrpas.setStartDate(m4DRegistryRequestDto.mrpaRegistry.getStartDate().substring(0, 10));
        }
        if (m4DRegistryRequestDto.mrpaRegistry.getEndDate() != null) {
            mrpas.setEndDate(m4DRegistryRequestDto.mrpaRegistry.getEndDate().substring(0, 10));
        }
        mrpas.setInfoSystem("sep.cik");

        M4dFullXML.Document document = new M4dFullXML.Document();
        m4dFullXML.setDocument(document);

        M4dFullXML.Document.PowerAttorneys powerAttorneys = new M4dFullXML.Document.PowerAttorneys();
        document.setPowerAttorneys(powerAttorneys);
        powerAttorneys.setMrpa(mrpas);
        M4dFullXML.Document.PowerAttorneys.InfoPrincipals infoPrincipal = new M4dFullXML.Document.PowerAttorneys.InfoPrincipals();
        infoPrincipal.setTypePrincipal("1");
        powerAttorneys.setPrincipals(infoPrincipal);
        M4dFullXML.Document.PowerAttorneys.InfoPrincipals.InfoPrincipal infoPrincipal1 = new M4dFullXML.Document.PowerAttorneys.InfoPrincipals.InfoPrincipal();
        infoPrincipal.setPrincipal(infoPrincipal1);
        M4dFullXML.Document.PowerAttorneys.InfoPrincipals.InfoPrincipal.LegalEntity legalEntity = new M4dFullXML.Document.PowerAttorneys.InfoPrincipals.InfoPrincipal.LegalEntity();
        legalEntity.setManagementCompany("0");
        legalEntity.setIndividual("1");
        legalEntity.setIndividualEntrepreneur("0");
        infoPrincipal1.setLegalEntity(legalEntity);
        M4dFullXML.Document.PowerAttorneys.InfoPrincipals.InfoPrincipal.LegalEntity.Organisation organisation = new M4dFullXML.Document.PowerAttorneys.InfoPrincipals.InfoPrincipal.LegalEntity.Organisation();
        organisation.setName(m4DRegistryRequestDto.organisation.getName());
        organisation.setInn(m4DRegistryRequestDto.organisation.getInn());
        organisation.setKpp(m4DRegistryRequestDto.organisation.getKpp());
        organisation.setOgrn(m4DRegistryRequestDto.organisation.getOgrn());
        legalEntity.setOrganisation(organisation);
        M4dFullXML.Document.PowerAttorneys.InfoPrincipals.InfoPrincipal.LegalEntity.Organisation.AddressRegion addressRegion = new M4dFullXML.Document.PowerAttorneys.InfoPrincipals.InfoPrincipal.LegalEntity.Organisation.AddressRegion();
        addressRegion.setRegionCode(m4DRegistryRequestDto.organisation.getRegionCode());
        addressRegion.setLegalAddress(m4DRegistryRequestDto.organisation.getLegalAddress());
        organisation.setAddressRegion(addressRegion);
        M4dFullXML.Document.PowerAttorneys.InfoPrincipals.InfoPrincipal.LegalEntity.PersonPrincipal personPrincipal = new M4dFullXML.Document.PowerAttorneys.InfoPrincipals.InfoPrincipal.LegalEntity.PersonPrincipal();
        personPrincipal.setTypePrincipal("1");
        legalEntity.setPrincipal(personPrincipal);
        M4dFullXML.Document.PowerAttorneys.InfoPrincipals.InfoPrincipal.LegalEntity.PersonPrincipal.Principal principal = new M4dFullXML.Document.PowerAttorneys.InfoPrincipals.InfoPrincipal.LegalEntity.PersonPrincipal.Principal();
        principal.setInn(m4DRegistryRequestDto.principal.getInn());
        principal.setSnils(m4DRegistryRequestDto.principal.getSnils());
        principal.setJobTitle(m4DRegistryRequestDto.principalDetail.getJobTitle());
        personPrincipal.setPrincipal(principal);
        M4dFullXML.Document.PowerAttorneys.InfoPrincipals.InfoPrincipal.LegalEntity.PersonPrincipal.Principal.FioPrincipal fioPrincipal = new M4dFullXML.Document.PowerAttorneys.InfoPrincipals.InfoPrincipal.LegalEntity.PersonPrincipal.Principal.FioPrincipal();
        principal.setFioPrincipal(fioPrincipal);
        M4dFullXML.Document.PowerAttorneys.InfoPrincipals.InfoPrincipal.LegalEntity.PersonPrincipal.Principal.FioPrincipal.Fio fio = new M4dFullXML.Document.PowerAttorneys.InfoPrincipals.InfoPrincipal.LegalEntity.PersonPrincipal.Principal.FioPrincipal.Fio();
        fio.setName(m4DRegistryRequestDto.principalDetail.getName());
        fio.setPatronymic(m4DRegistryRequestDto.principalDetail.getPatronymic());
        fio.setSurname(m4DRegistryRequestDto.principalDetail.getSurname());
        fioPrincipal.setFio(fio);
        M4dFullXML.Document.PowerAttorneys.InfoRepresentative infoRepresentative = new M4dFullXML.Document.PowerAttorneys.InfoRepresentative();
        infoRepresentative.setTypeRepresentative("3");
        powerAttorneys.setInfoRepresentative(infoRepresentative);
        M4dFullXML.Document.PowerAttorneys.InfoRepresentative.Representative representative = new M4dFullXML.Document.PowerAttorneys.InfoRepresentative.Representative();
        infoRepresentative.setRepresentative(representative);
        M4dFullXML.Document.PowerAttorneys.InfoRepresentative.Representative.DetailRepresentative detailRepresentative = new M4dFullXML.Document.PowerAttorneys.InfoRepresentative.Representative.DetailRepresentative();
        representative.setDetailRepresentative(detailRepresentative);
        detailRepresentative.setInn(m4DRegistryRequestDto.representative.getInn());
        detailRepresentative.setSnils(m4DRegistryRequestDto.representative.getSnils());
        M4dFullXML.Document.PowerAttorneys.InfoRepresentative.Representative.DetailRepresentative.InfoIndividual infoIndividual = new M4dFullXML.Document.PowerAttorneys.InfoRepresentative.Representative.DetailRepresentative.InfoIndividual();
        detailRepresentative.setInfoIndividual(infoIndividual);
        if (m4DRegistryRequestDto.representative.getBirthDate() != null) {
            infoIndividual.setBirthDate(m4DRegistryRequestDto.representative.getBirthDate().substring(0, 10));
        }
        M4dFullXML.Document.PowerAttorneys.InfoRepresentative.Representative.DetailRepresentative.InfoIndividual.FioRepresentative fioRepresentative = new M4dFullXML.Document.PowerAttorneys.InfoRepresentative.Representative.DetailRepresentative.InfoIndividual.FioRepresentative();
        infoIndividual.setFioRepresentative(fioRepresentative);
        fioRepresentative.setName(m4DRegistryRequestDto.representativeDetail.getName());
        fioRepresentative.setSurname(m4DRegistryRequestDto.representativeDetail.getSurname());
        fioRepresentative.setPatronymic(m4DRegistryRequestDto.representativeDetail.getPatronymic());
        M4dFullXML.Document.PowerAttorneys.InfoRepresentative.Representative.DetailRepresentative.InfoIndividual.RepresentativePassport representativePassport = new M4dFullXML.Document.PowerAttorneys.InfoRepresentative.Representative.DetailRepresentative.InfoIndividual.RepresentativePassport();
        infoIndividual.setRepresentativePassport(representativePassport);
        representativePassport.setTypeDocument("21");
        representativePassport.setSeriesNumber(m4DRegistryRequestDto.representativeDetail.getSeriesNumber());
        if (m4DRegistryRequestDto.representativeDetail.getDateIssue() != null) {
            representativePassport.setDateIssue(m4DRegistryRequestDto.representativeDetail.getDateIssue().substring(0, 10));
        }
        representativePassport.setWhoIssue(m4DRegistryRequestDto.representativeDetail.getWhoIssue());
        representativePassport.setDepartmentCode(m4DRegistryRequestDto.representativeDetail.getDepartmentCode());
        M4dFullXML.Document.PowerAttorneys.Credentials credentials = new M4dFullXML.Document.PowerAttorneys.Credentials();
        credentials.setTypeCredential("1");
        credentials.setImplCredential("1");
        credentials.setLossCredential("1");
        ArrayList<M4dFullXML.Document.PowerAttorneys.Credentials.Credential> credentialList = new ArrayList<>();
        for (Long x : m4DRegistryRequestDto.getCredentials()) {

            Credential cred = this.credentialService.findByCredentialId(x);
            M4dFullXML.Document.PowerAttorneys.Credentials.Credential credential = new M4dFullXML.Document.PowerAttorneys.Credentials.Credential();
            credential.setCode(cred.getCode());
            credential.setName(cred.getName());
            credentialList.add(credential);
        }
        credentials.setCredentials(credentialList);
        powerAttorneys.setCredential(credentials);

        return m4dFullXML;
    }

    private M4dRegistryResponseDto.SigFileDto convertSigFileToDto(M4dFile m4dFile) {
        return modelMapper.map(m4dFile, M4dRegistryResponseDto.SigFileDto.class);
    }

    private M4dRegistryResponseDto.XmlFileDto convertXmlFileToDto(M4dFile m4dFile) {
        return modelMapper.map(m4dFile, M4dRegistryResponseDto.XmlFileDto.class);
    }

    private M4dRegistryResponseDto.RecallDto.SigFileRecallDto convertSigFileRecallToDto(M4dFile m4dFile) {
        return modelMapper.map(m4dFile, M4dRegistryResponseDto.RecallDto.SigFileRecallDto.class);
    }

    private M4dRegistryResponseDto.RecallDto.XmlFileRecallDto convertXmlFileRecallToDto(M4dFile m4dFile) {
        return modelMapper.map(m4dFile, M4dRegistryResponseDto.RecallDto.XmlFileRecallDto.class);
    }

    private M4dRegistryResponseDto.OrganizationDto convertOrganisationToDto(OrganizationDto organisation) {
        return modelMapper.map(organisation, M4dRegistryResponseDto.OrganizationDto.class);
    }

    private M4dRegistryResponseDto.PrincipalDto convertPrincipalToDto(Principal principal) {
        return modelMapper.map(principal, M4dRegistryResponseDto.PrincipalDto.class);
    }

    private M4dRegistryResponseDto.PrincipalDetailDto convertPrincipalDetailToDto(PrincipalDetail principalDetail) {
        return modelMapper.map(principalDetail, M4dRegistryResponseDto.PrincipalDetailDto.class);
    }

    private M4dRegistryResponseDto.RepresentativeDto convertRepresentativeToDto(Representative representative) {
        return modelMapper.map(representative, M4dRegistryResponseDto.RepresentativeDto.class);
    }

    private M4dRegistryResponseDto.RepresentativeDetailDto convertRepresentativePassportToDto(RepresentativeDetail representativeDetail) {
        return modelMapper.map(representativeDetail, M4dRegistryResponseDto.RepresentativeDetailDto.class);
    }

    private String getArgumentFromXml(M4dFile m4dFile) throws JAXBException {
        String content;
        if (isBase64(m4dFile.getContent())) {
            content = new String(Base64.getDecoder().decode(m4dFile.getContent()));
        } else {
            content = m4dFile.getContent();
        }
        Document document = convertStringToDocument(content);
        assert document != null;
        M4dRevokeXml m4dRevokeXml = XMLUtil.unmarshal(document, M4dRevokeXml.class);
        return m4dRevokeXml.getDocument().recallInfo.getArgument();
    }

    @Override
    public String getGuidFromXml(M4dXml m4dXml) {
        return m4dXml.document.powerAttorneys.getMrpa().getGuid();
    }

    @Override
    public String getStartDateM4dFromXml(M4dXml m4dXml) {
        return m4dXml.document.powerAttorneys.getMrpa().getStartDate();
    }

    @Override
    public String getEndDateM4dFromXml(M4dXml m4dXml) {
        return m4dXml.document.powerAttorneys.getMrpa().getEndDate();
    }

    @Override
    public List<ReportCheckStatusM4dDto.Report.Credential> getCredentialM4dFromXml(M4dXml m4dXml) {
        List<ReportCheckStatusM4dDto.Report.Credential> credentials = new ArrayList<>();
        List<M4dXml.Document.PowerAttorneys.Credentials.Credential> credentialList = m4dXml.document.powerAttorneys.credential.getCredentials();
        if (credentialList != null) {
            for (M4dXml.Document.PowerAttorneys.Credentials.Credential x : credentialList) {
                ReportCheckStatusM4dDto.Report.Credential credential = new ReportCheckStatusM4dDto.Report.Credential();
                credential.setCode(x.getCode());
                credential.setName(x.getName());
                credentials.add(credential);
            }
            return credentials;
        }
        return null;
    }

    @Override
    public List<ReportCheckStatusM4dDto.Report.Representative> getRepresentativeM4dFromXml(M4dXml m4dXml) {
        List<ReportCheckStatusM4dDto.Report.Representative> representatives = new ArrayList<>();
        List<M4dXml.Document.PowerAttorneys.InfoRepresentative> representativeList = m4dXml.document.powerAttorneys.getInfoRepresentative();
        for (M4dXml.Document.PowerAttorneys.InfoRepresentative x : representativeList) {
            ReportCheckStatusM4dDto.Report.Representative representative = new ReportCheckStatusM4dDto.Report.Representative();
            representative.setInn(x.representative.detailRepresentative.getInn());
            String fio = x.representative.detailRepresentative.infoIndividual.fioRepresentative.getSurname() + " "
                    + x.representative.detailRepresentative.infoIndividual.fioRepresentative.getName() + " "
                    + x.representative.detailRepresentative.infoIndividual.fioRepresentative.getPatronymic();
            representative.setFio(fio);
            representatives.add(representative);
        }
        return representatives;
    }

    @Override
    public List<ReportCheckStatusM4dDto.Report.Principal> getPrincipalM4dFromXml(M4dXml m4dXml) {
        List<ReportCheckStatusM4dDto.Report.Principal> principals = new ArrayList<>();
        M4dXml.Document.PowerAttorneys.InfoPrincipals principal = m4dXml.document.powerAttorneys.getPrincipals();
        // TODO: helix82 Изначально почему то заложено что один доверитель с мчд, надо переделать
        String fio = principal.principal.legalEntity.principal.principal.fioPrincipal.fio.getSurname() + " "
                + principal.principal.legalEntity.principal.principal.fioPrincipal.fio.getName() + " "
                + principal.principal.legalEntity.principal.principal.fioPrincipal.fio.getPatronymic();
        ReportCheckStatusM4dDto.Report.Principal principal1 = new ReportCheckStatusM4dDto.Report.Principal();
        principal1.setInn(principal.principal.legalEntity.principal.principal.getInn());
        principal1.setFio(fio);
        principals.add(principal1);
        return principals;
    }

    @Override
    public String getInnOrganizationFromXml(M4dXml m4dXml) {
        return m4dXml.document.powerAttorneys.principals.principal.legalEntity.organisation.getInn();
    }

    @Override
    public String getNameOrganizationFromXml(M4dXml m4dXml) {
        return m4dXml.document.powerAttorneys.principals.principal.legalEntity.organisation.getName();
    }

    @Override
    public M4dXml convertM4dFileToM4dXml(MultipartFile sourceFile) {
        M4dXml m4dXml;
        try {
            Document document = convertStringToDocument(XMLUtil.readXmlToString(sourceFile));
            assert document != null;
            m4dXml = XMLUtil.unmarshal(document, M4dXml.class);
        } catch (Exception e) {
            log.error(MRPA_REGISTRY_WRONG_FORMAT_XML + ": ошибка унмаршалинга");
            throw new BadRequestException(MRPA_REGISTRY_WRONG_FORMAT_XML);
        }

        if (m4dXml == null) {
            throw new BadRequestException(MRPA_IS_NULL);
        }
        if (m4dXml.document.getPowerAttorneys() == null) {
            log.error(MRPA_REGISTRY_WRONG_FORMAT_XML + ": не найдена запись о доверенности в xml-файле");
            throw new BadRequestException(MRPA_REGISTRY_WRONG_FORMAT_XML);
        }
        if (m4dXml.document.powerAttorneys.getMrpa().getGuid().isEmpty()) {
            log.error(MRPA_REGISTRY_WRONG_FORMAT_XML + ": не найден номер доверенности (guid) в xml-файле");
            throw new BadRequestException(MRPA_REGISTRY_WRONG_FORMAT_XML);
        }
        return m4dXml;
    }

    @Override
    public String getHashXmlM4d(MultipartFile sourceFile) throws NoSuchAlgorithmException {
        return HashUtils.getHash(XMLUtil.readXmlToString(sourceFile).getBytes());
    }

    @Override
    public ReportValidSchemeDto validXmlM4dScheme(MultipartFile sourceFile, String reqScheme) throws IOException, SAXException {
        String scheme;
        ReportValidSchemeDto report = new ReportValidSchemeDto();
        report.setFileName(sourceFile.getOriginalFilename());
        if (reqScheme.isEmpty()) {
            report.setReqSchema("none");
            String content = XMLUtil.readXmlToString(sourceFile);
            scheme = XMLUtil.getSubstringFromXml(content, "ВерсФорм=\"");
        } else {
            report.setReqSchema(reqScheme);
            scheme = reqScheme;
        }
        if (!scheme.equals("error") && !scheme.equals("not_found")) {
            if (scheme.equals("EMCHD_1")) {
                report.setOutSchema("EMCHD_1 (003)");
                List<SAXParseException> res = new XmlValidator(sourceFile.getInputStream(), scheme).listParsingExceptions();
                if (!res.isEmpty()) {
                    Map<String, String> body = new HashMap<>();
                    List<String> errs = new ArrayList<>();
                    res.forEach(e -> errs.add(String.format("Line number: %s, Column number: %s. %s", e.getLineNumber(), e.getColumnNumber(), e.getMessage())));
                    body.put("details", String.join(System.lineSeparator(), errs));
                    report.setResult("false");
                    report.setErrorDetail(body.toString());
                } else {
                    report.setResult("true");
                }
            }
        }
        return report;
    }

    @Override
    @AuditLogThis(event = AuditLogEventEnum.M4D_REQUEST_SEARCH_FIND_RR_UPDATE_STATUS)
    public ReportCheckStatusM4dDto updateStatusM4dRequestSearch(M4dFnsDataResponse m4dFnsDataResponse) {
        M4dRequestSearch m4dRequestSearch = this.m4dRequestService.findM4dRequestByGuidAndHash(m4dFnsDataResponse.getGuid(), m4dFnsDataResponse.getId());
        M4dRequestSearchDetail m4dRequestSearchDetail;
        if (m4dRequestSearch != null) {
            m4dRequestSearchDetail = this.m4dRequestService.findM4dRequestDetailByM4dRequestId(m4dRequestSearch.getId());
            M4dRequestSearchDetail m4dRequestSearchDetailNew = new M4dRequestSearchDetail();
            m4dRequestSearchDetailNew.setStatus(m4dFnsDataResponse.getStatus());
            if (m4dFnsDataResponse.getStatusDateTime() != null) {
                m4dRequestSearchDetailNew.setStatusDate(DateUtils.convertStringToLocalDateTime(m4dFnsDataResponse.getStatusDateTime()));
            }
            if (m4dFnsDataResponse.getCreatedDateTime() != null) {
                m4dRequestSearchDetailNew.setRegistrationDate(DateUtils.convertStringToLocalDateTime(m4dFnsDataResponse.getCreatedDateTime()));
            }
            if (m4dFnsDataResponse.getStartDateTime() != null) {
                m4dRequestSearchDetailNew.setStartDate(DateUtils.convertStringToLocalDateTime(m4dFnsDataResponse.getStartDateTime()));
            }
            if (m4dFnsDataResponse.getEndDateTime() != null) {
                m4dRequestSearchDetailNew.setEndDate(DateUtils.convertStringToLocalDateTime(m4dFnsDataResponse.getEndDateTime()));
            }
            m4dRequestSearchDetailNew.setActive(true);
            m4dRequestSearchDetailNew.setRequestSearchId(m4dRequestSearch.getId());
            if (m4dRequestSearchDetail != null) {
                m4dRequestSearchDetailNew.setVersion(m4dRequestSearchDetail.getVersion()+1);
                m4dRequestSearchDetail.setActive(false);
                this.m4dRequestService.updateM4dRequestSearchDetail(m4dRequestSearchDetail);
            } else { m4dRequestSearchDetailNew.setVersion(1L); }
            M4dRequestSearchDetail m4dRequestSearchDetailSaved;
            m4dRequestSearchDetailSaved = this.m4dRequestService.saveM4dRequestSearchDetail(m4dRequestSearchDetailNew);
                if(m4dRequestSearchDetailSaved == null) {
                    throw new BadRequestException("Активных деталей запроса статуса МЧД в РР МЧД не найдено");
                }
                M4d m4d;
                try {
                    m4d = this.findMrpaRegistryByGuid(m4dFnsDataResponse.getGuid());
                } catch (Exception e) {
                    throw new BadRequestException(e.getMessage());
                }
                String statusM4d = "";
                String statusDetailM4d = "";
                if (m4d != null) {
                    switch (m4dRequestSearchDetailSaved.getStatus()) {
                        case "ONCHECK":
                            statusM4d = "ONCHECK";
                            break;
                        case "ERROR":
                            statusM4d = "ERROR";
                            break;
                        case "PROCESSING":
                            statusM4d = "";
                            break;
                        case "REJECTED":
                            statusM4d = "ERROR";
                            statusDetailM4d = "МЧД не принята РР МЧД на хранение";
                            break;
                        case "CREATED":
                            statusM4d = "ACTIVE";
                            break;
                        case "ACTIVE":
                            statusM4d = "ACTIVE";
                            break;
                        case "EXPIRED":
                            statusM4d = "ACTIVE";
                            break;
                        case "REVOKED":
                            statusM4d = "REVOKED";
                            break;
                    }
                    if (!statusM4d.isEmpty()) {
                        m4d.setStatus(statusM4d);
                        if (statusM4d.equals("REVOKED")) {
                            M4dRecall m4dRecall = this.m4dRecallService.findMrpaRecallByM4dId(m4d.getId());
                            this.m4dRecallService.updateStatusMrpaRecallById(m4dRecall.getId(), "REVOKED");
                        }
                    }
                    if (!statusDetailM4d.isEmpty()) {
                        m4d.setErrorDetail(statusDetailM4d);
                    }
                    try {
                        this.updateM4d(m4d);
                    } catch (Exception e) {
                        throw new BadRequestException(e.getMessage());
                    }
                }
                ReportCheckStatusM4dDto report = new ReportCheckStatusM4dDto();
                report.setSource("m4d_rr");
                ReportCheckStatusM4dDto.Report rep = new ReportCheckStatusM4dDto.Report();
                report.setReport(rep);
                rep.setGuid(m4dFnsDataResponse.getGuid());
                rep.setStatus(m4dRequestSearchDetailSaved.getStatus());
                rep.setStartDate(m4dRequestSearchDetailSaved.getStartDate().toString());
                rep.setEndDate(m4dRequestSearchDetailSaved.getEndDate().toString());
//                rep.setCredentials(getCredentialM4dFromXml(sourceFile));
//                rep.setRepresentative(getRepresentativeM4dFromXml(sourceFile));
//                rep.setPrincipal(getPrincipalM4dFromXml(sourceFile));
//                rep.setInnOrganisation(getInnOrganizationFromXml(sourceFile));
//                rep.setNameOrganisation(getNameOrganizationFromXml(sourceFile));
                return report;
        }
        return null;
    }

    @Override
    public MultipartFile getMultipartFileByM4dId(Authentication authentication, Long id) throws ParseException, IOException {
        M4d m4d = this.findById(id);
        M4dRegistryResponseDto m4DRegistryResponseDto = this.convertM4dToM4dResponseDto(authentication, m4d, true, false, true, false);
        String xmlString = m4DRegistryResponseDto.getXmlFile().getContent();
        String filename = m4DRegistryResponseDto.getGuid()+".xml";
        return FileUtils.mapStringToMultipart(xmlString, filename);
    }

}

