package su.opencode.digsigcik.m4d.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
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
import su.opencode.digsigcik.common.exceptions.exception.custom.BadRequestException;
import su.opencode.digsigcik.m4d.dto.*;
import su.opencode.digsigcik.m4d.dto.response.M4dCountAndRevokeCountResponseDto;
import su.opencode.digsigcik.m4d.dto.response.M4dFnsDataResponse;
import su.opencode.digsigcik.m4d.dto.response.M4dResponseInfo;
import su.opencode.digsigcik.m4d.enums.*;
import su.opencode.digsigcik.m4d.exception.M4dException;
import su.opencode.digsigcik.m4d.model.*;
import su.opencode.digsigcik.m4d.repository.M4dCredentialRepository;
import su.opencode.digsigcik.m4d.repository.M4dRepository;
import su.opencode.digsigcik.m4d.service.*;
import su.opencode.digsigcik.m4d.util.*;
import su.opencode.digsigcik.specification.SpecificationRequest;
import su.opencode.digsigcik.specification.SpecificationResponse;
import su.opencode.digsigcik.specification.service.SpecificationService;

import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;
import javax.xml.bind.JAXBException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static su.opencode.digsigcik.m4d.util.Constants.*;
import static su.opencode.digsigcik.m4d.util.EncodingUtils.isBase64;
import static su.opencode.digsigcik.m4d.util.FileUtils.createDirectoryIfSheNotExist;
import static su.opencode.digsigcik.m4d.util.FileUtils.mapFileToMultipart;
import static su.opencode.digsigcik.m4d.util.JwtTokenUtils.getTokenValue;
import static su.opencode.digsigcik.m4d.util.XMLUtil.convertStringToDocument;

@Slf4j
@Service
@Primary
@RequiredArgsConstructor
@EnableTransactionManagement
public class M4dServiceImpl implements M4dService {
    @Value("${m4d.soul}")
    private String M4D_SOUL;
    @Value("${audit.default-user}")
    private String DEFAULT_USER_NAME;
    @Value("${m4d.xml-version-format}")
    private String M4D_XML_VERSION_FORMAT;

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
    private final M4dRecallConfirmService m4dRecallConfirmService;
    private final SpecificationService specificationService;

    private final PvvService pvvService;
    private final ContainerService containerService;

    @Override
    public List<M4dRegistryResponseDto> findAllMrpaRegistry(Authentication authentication) throws ParseException {
        Iterable<M4d> m4ds = m4dRepository.findAll();
        List<M4dRegistryResponseDto> m4DRegistryResponseDtoList = new ArrayList<>();
        for (M4d m4d : m4ds) {
            m4DRegistryResponseDtoList.add(convertM4dToM4dResponseDto(authentication, m4d, false, false, false, false));
        }
        return m4DRegistryResponseDtoList;
    }

    @Override
    public SpecificationResponse<M4dRegistryResponseDto> findAllMrpaRegistry(Authentication authentication, SpecificationRequest request) throws ParseException {
        var r = specificationService.getUsingSpecification(m4dRepository, request, M4d.class);

        List<M4dRegistryResponseDto> m4DRegistryResponseDtoList = new ArrayList<>();
        for (M4d m4d : r.getData()) {
            m4DRegistryResponseDtoList.add(convertM4dToM4dResponseDto(authentication, m4d, false, false, false, false));
        }

        return SpecificationResponse
                .<M4dRegistryResponseDto> builder()
                .data(m4DRegistryResponseDtoList)
                .filters(r.getFilters())
                .sortBy(r.getSortBy())
                .pagination(r.getPagination())
                .build();
    }

    @Override
    public M4d saveMrpaRegistry(@NotNull M4d m4d) {
        return this.m4dRepository.save(m4d);
    }

    @Override
    @AuditLogThis(event = AuditLogEventEnum.M4D_UPDATE)
    public M4d updateM4d(@NotNull M4d m4d) {
        return this.m4dRepository.save(m4d);
    }

    @Override
    @AuditLogThis(event = AuditLogEventEnum.M4D_UPDATE)
    public void updateStatusM4d(Long id, String status) {
        this.m4dRepository.updateStatusM4d(id, status);
        this.m4dRepository.findM4dById(id);
    }

    @Transactional(rollbackFor = {Exception.class}, isolation = Isolation.READ_UNCOMMITTED)
    @AuditLogThis(event = AuditLogEventEnum.M4D_CREATE)
    public Long saveAllDataMrpaRegistry(Authentication authentication, M4d m4d, OrganizationDto organisation, Principal principal, PrincipalDetail principalDetail, Representative representative, RepresentativeDetail representativeDetail, List<CredentialDto> credentialList, List<Long> credentials, String xmlString) {
        String bearerToken = getTokenValue(authentication);
        String orgInn;
        Long principId;
        Long principDetailId;
        Long represId;
        Long represDetailId;
        if (organisation.getInn() == null) {
            throw new BadRequestException(ORGANIZATION_INN_IS_NULL);
        }
        if (principal.getInn() == null) {
            throw new BadRequestException(PRINCIPAL_INN_IS_NULL);
        }
        if (representative.getInn() == null) {
            throw new BadRequestException(REPRESENTATIVE_INN_IS_NULL);
        }
        if (representativeDetail.getSeriesNumber() == null) {
            throw new BadRequestException(REPRESENTATIVE_SERIAL_NUMBER_IS_NULL);
        }
        //------------------- save organisation ----------------
        OrganizationDto existsOrganisation;
        try {
            existsOrganisation = this.refBookClientService.findByOrganisationInn(organisation.getInn(), bearerToken);
            if (existsOrganisation == null) {
                throw new BadRequestException(ORGANISATION_IS_NULL);
            }
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
        if (respPrincipal == null) {
            throw new BadRequestException(PRINCIPAL_SAVE_ERROR);
        }
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
        if (respPrincipalDetail == null) {
            throw new BadRequestException(PRINCIPAL_DETAIL_SAVE_ERROR);
        }
        principDetailId = respPrincipalDetail.getId();
        //------------------- end save principal detail ----------------

        //------------------- save representative ----------------
        Representative respRepresentative;
        try {
            respRepresentative = this.representativeService.saveRepresentative(representative);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(e.getMessage());
        }
        if (respRepresentative == null) {
            throw new BadRequestException(REPRESENTATIVE_SAVE_ERROR);
        }
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
        if (respRepresDetail == null) {
            throw new BadRequestException(REPRESENTATIVE_DETAIL_SAVE_ERROR);
        }
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
        if (m4d.getStatus() == null) {
            m4d.setStatus(StatusM4dTypeEnum.NEW.toString());
        }
        if (m4d.getIsSubTrust() == null) {
            m4d.setIsSubTrust(false);
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
            throw new BadRequestException(ID_IS_NULL);
        }
        if (organisation.getInn() == null) {
            throw new BadRequestException(ORGANIZATION_INN_IS_NULL);
        }
        if (principal.getInn() == null) {
            throw new BadRequestException(PRINCIPAL_INN_IS_NULL);
        }
        if (representative.getInn() == null) {
            throw new BadRequestException(REPRESENTATIVE_INN_IS_NULL);
        }
        M4d existMrpaRegistry = this.findById(m4d.getId());
        if (existMrpaRegistry == null) {
            throw new BadRequestException(MRPA_NOT_FOUND);
        }
        //------------------- save organisation ----------------
        OrganizationDto existsOrganisation;
        try {
            existsOrganisation = this.refBookClientService.findByOrganisationInn(organisation.getInn(), bearerToken);
            if (existsOrganisation == null) {
                throw new BadRequestException(ORGANISATION_IS_NULL);
            }
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
                        principalDetail.setVersion(existsPrincipalDetail.getVersion() + 1);
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
                        || !existsRepresentativeDetail.getDepartmentCode().equals(representativeDetail.getDepartmentCode())) {
                    RepresentativeDetail respRepresentativeDetail;
                    try {
                        representativeDetail.setVersion(existsRepresentativeDetail.getVersion() + 1);
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
            this.m4dCredentialRepository.deleteAllByMrpaRegistryId(id);
            List<M4d> m4ds = this.m4dRepository.findByPrincipalId(m4d.getPrincipalId());
            if (m4ds.isEmpty()) {
                this.principalService.deletePrincipal(m4d.getPrincipalId());
                this.principalDetailService.deletePrincipalDetailByPrincipalId(m4d.getPrincipalId());
            }
            List<M4d> m4ds1 = this.m4dRepository.findByRepresentativeId(m4d.getRepresentativeId());
            if (m4ds1.isEmpty()) {
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
        if (!credentials.isEmpty()) {
            for (Credential c : credentials) {
                M4dRegistryResponseDto.CredentialDto credentialDTO = new M4dRegistryResponseDto.CredentialDto();
                credentialDTO.setId(c.getId());
                credentialDTO.setName(c.getName());
                credentialDTO.setCode(c.getCode());
                credentialDTOList.add(credentialDTO);
            }
            return credentialDTOList;
        }
        return null;
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

    private static boolean containsAny(Set<String> set, String ... elements) {

        for (String el : elements)
            if (set.contains(el))
                return true;

        return false;
    }

    @Transactional(readOnly = true)
    @Override
    public SpecificationResponse<M4dRegistryResponseDto> findAllM4dUsers(Authentication authentication, SpecificationRequest request) throws ParseException {

        // TODO: refactoring (Данияо)
        throw new NotImplementedException("await Refactoring");

        // Добавить фильтр по principal и representative
//        request.getFilters().removeIf(f -> f.getFieldName().equals("login"));

        // Получить пэджинированные данные
//        var r = specificationService.getUsingSpecification(m4dRepository, request, M4d.class);


        // замапить на M4dRegistryResponseDto
//        return SpecificationResponse.<M4dRegistryResponseDto> builder().data(m4DRegistryResponseDtoList).filters(r.getFilters()).sortBy(r.getSortBy()).pagination(r.getPagination()).build();
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

    @Override
    public M4dCountAndRevokeCountResponseDto countM4dAndRevoke(Authentication authentication, String status) {
        Set<String> setINNs = userCertificateService.getUserCertificateINNs(authentication);
        long m4dCount = m4dRepository.countM4dByStatusAndPrincipalInns(setINNs, status);
        long m4dRevokeCount = m4dRecallService.countM4dRevokeByInnsAndStatus(setINNs, status);

        return new M4dCountAndRevokeCountResponseDto(m4dCount, m4dRevokeCount);
    }

    public String getStatusM4d(Long id) throws ParseException {
        M4d mrpaRegistry = this.m4dRepository.findM4dById(id);
        Date currentDT = Date.from(Instant.now());
        Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse(String.valueOf(mrpaRegistry.getStartDate()));
        Date endDate = new SimpleDateFormat("yyyy-MM-dd").parse(String.valueOf(mrpaRegistry.getEndDate()));
        if (mrpaRegistry.getStatus().equals(StatusM4dTypeEnum.ACTIVE.toString())) {
            if (currentDT.before(startDate)) {
                return StatusM4dTypeEnum.NOT_ACTIVE.toString();
            } else if (currentDT.after(endDate)) {
                return StatusM4dTypeEnum.NOT_ACTIVE.toString();
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
        m4DRegistryResponseDto.setErrorInRrDetail(m4d.getErrorInRrDetail());
        m4DRegistryResponseDto.setId(m4d.getId());
        m4DRegistryResponseDto.setCreateDate(m4d.getCreateDate().toString());
        m4DRegistryResponseDto.setIsSubTrust(m4d.getIsSubTrust());
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
                recallDto.setStatusInRr(m4dRecall.getStatusInRr());
                recallDto.setErrorInRrDetail(m4dRecall.getErrorInRrDetail());
                m4DRegistryResponseDto.setRecall(recallDto);
                if (m4dRecall.getSigFile() != null) {
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
        // TODO: fkey, или подготовить вьюшку, чтобы можно было фильтровать по этому полю (Данияр)
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
        m4DRegistryResponseDto.setStatusInRr(m4d.getStatusInRr());
        return m4DRegistryResponseDto;
    }

    @Override
    @AuditLogThis(event = AuditLogEventEnum.M4D_REQUEST_SEARCH_FIND_RR_CREATE)
    public ReportCheckStatusM4dAndSendingPvvDto createM4dRequestSearchForFindRR(M4dXml m4dXml, String hashXml) {
        String guidXml = this.getGuidFromXml(m4dXml);
        this.m4dRequestService.deleteM4dRequestSearch(guidXml);
        // - формирование запроса в РР МЧД на проверку статуса МЧД (если результаты предыдущего отрицательные)
        M4dRequestSearch m4dRequestSearchNew = new M4dRequestSearch();
        m4dRequestSearchNew.setGuid(guidXml);
        m4dRequestSearchNew.setCreateDate(DateUtils.convertStringToLocalDateTime(DateUtils.getCurrentDate()));
        m4dRequestSearchNew.setHashXml(hashXml);
        m4dRequestSearchNew.setStatus(StatusRequestTypeEnum.ONCHECK.toString());
        m4dRequestSearchNew.setStatusDate(DateUtils.convertStringToLocalDateTime(DateUtils.getCurrentDate()));
        m4dRequestSearchNew.setStartDate(DateUtils.convertStringToLocalDateTime(getStartDateM4dFromXml(m4dXml)));
        m4dRequestSearchNew.setEndDate(DateUtils.convertStringToLocalDateTime(getEndDateM4dFromXml(m4dXml)));
        M4dRequestSearch m4dRequestSearchSaved = this.m4dRequestService.saveM4dRequestSearch(m4dRequestSearchNew);
        if (m4dRequestSearchSaved != null) {
            String res;
            res = this.pvvService.sendRequestStatusM4dToRR(m4dXml.document.powerAttorneys.mrpa.getGuid());
            if (res == null) {
                log.error(SENDING_RESPONSE_TO_PVV_ERROR);
            }
            ReportCheckStatusM4dDto report = new ReportCheckStatusM4dDto();
            report.setSource("m4d_rr");
            ReportCheckStatusM4dDto.Report rep = new ReportCheckStatusM4dDto.Report();
            report.setReport(rep);
            rep.setGuid(guidXml);
            rep.setStatusInRr(m4dRequestSearchSaved.getStatus());
            rep.setErrorInRrDetail(CHECK_STATUS_IN_PVV_ADAPTER);
            rep.setStartDate(getStartDateM4dFromXml(m4dXml));
            rep.setEndDate(getEndDateM4dFromXml(m4dXml));
            rep.setCredentials(getCredentialM4dFromXml(m4dXml));
            rep.setRepresentative(getRepresentativeM4dFromXml(m4dXml));
            rep.setPrincipal(getPrincipalM4dFromXml(m4dXml));
            rep.setInnOrganisation(getInnOrganizationFromXml(m4dXml));
            rep.setNameOrganisation(getNameOrganizationFromXml(m4dXml));
            ReportCheckStatusM4dAndSendingPvvDto reportCheckStatusM4dAndSendingPvvDto = new ReportCheckStatusM4dAndSendingPvvDto();
            reportCheckStatusM4dAndSendingPvvDto.setReportCheckStatusM4dDto(report);
            reportCheckStatusM4dAndSendingPvvDto.setRes(res);
            return reportCheckStatusM4dAndSendingPvvDto;
        }
        return null;
    }

    @Override
    @AuditLogThis(event = AuditLogEventEnum.M4D_REQUEST_SEARCH_FIND_RR_CREATE)
    public ReportCheckStatusM4dAndSendingPvvDto createM4dRequestSearchForFindRRByGuid(String guidXml) {
        this.m4dRequestService.deleteM4dRequestSearch(guidXml);
        // - формирование запроса в РР МЧД на проверку статуса МЧД (если результаты предыдущего отрицательные)
        M4dRequestSearch m4dRequestSearchNew = new M4dRequestSearch();
        m4dRequestSearchNew.setGuid(guidXml);
        m4dRequestSearchNew.setCreateDate(LocalDateTime.now());
        m4dRequestSearchNew.setStatus(StatusRequestTypeEnum.ONCHECK.toString());
        m4dRequestSearchNew.setStatusDate(LocalDateTime.now());
        M4dRequestSearch m4dRequestSearchSaved = this.m4dRequestService.saveM4dRequestSearch(m4dRequestSearchNew);
        if (m4dRequestSearchSaved != null) {
            String res;
            res = this.pvvService.sendRequestStatusM4dToRR(guidXml);
            if (res == null) {
                log.error(SENDING_RESPONSE_TO_PVV_ERROR);
            }
            ReportCheckStatusM4dDto report = new ReportCheckStatusM4dDto();
            report.setSource("m4d_rr");
            ReportCheckStatusM4dDto.Report rep = new ReportCheckStatusM4dDto.Report();
            report.setReport(rep);
            rep.setGuid(guidXml);
            rep.setStatusInRr(m4dRequestSearchSaved.getStatus());
            rep.setErrorInRrDetail(CHECK_STATUS_IN_PVV_ADAPTER);
            ReportCheckStatusM4dAndSendingPvvDto reportCheckStatusM4dAndSendingPvvDto = new ReportCheckStatusM4dAndSendingPvvDto();
            reportCheckStatusM4dAndSendingPvvDto.setReportCheckStatusM4dDto(report);
            reportCheckStatusM4dAndSendingPvvDto.setRes(res);
            return reportCheckStatusM4dAndSendingPvvDto;
        }
        return null;
    }

    @Override
    @AuditLogThis(event = AuditLogEventEnum.M4D_REQUEST_SEARCH_FIND_RR_CREATE)
    public ReportCheckStatusM4dAndSendingPvvDto createM4dRequestSearchForFindRRByGuidAndInns(CheckByGuidRequestDto requestDto) {
        this.m4dRequestService.deleteM4dRequestSearch(requestDto.getGuid());
        // - формирование запроса в РР МЧД на проверку статуса МЧД (если результаты предыдущего отрицательные)
        M4dRequestSearch m4dRequestSearchNew = new M4dRequestSearch();
        m4dRequestSearchNew.setGuid(requestDto.getGuid());
        m4dRequestSearchNew.setPrincipalInn(requestDto.getPrincipalInn());
        m4dRequestSearchNew.setRepresentativeInn(requestDto.getRepresentativeInn());
        m4dRequestSearchNew.setCreateDate(LocalDateTime.now());
        m4dRequestSearchNew.setStatus(StatusRequestTypeEnum.ONCHECK.toString());
        m4dRequestSearchNew.setStatusDate(LocalDateTime.now());
        M4dRequestSearch m4dRequestSearchSaved = this.m4dRequestService.saveM4dRequestSearch(m4dRequestSearchNew);
        if (m4dRequestSearchSaved != null) {
            String res;
            res = this.pvvService.sendRequestStatusM4dToRRByGuidAndInns(requestDto);
            if (res == null) {
                log.error(SENDING_RESPONSE_TO_PVV_ERROR);
            }
            ReportCheckStatusM4dDto report = new ReportCheckStatusM4dDto();
            report.setSource("m4d_rr");
            ReportCheckStatusM4dDto.Report rep = new ReportCheckStatusM4dDto.Report();
            report.setReport(rep);
            rep.setGuid(requestDto.getGuid());
            if (requestDto.getPrincipalInn() != null) {
                rep.setPrincipal(
                    Collections.singletonList(
                        new ReportCheckStatusM4dDto.Report.Principal() {{
                            setInn(requestDto.getPrincipalInn());
                        }}
                    )
                );
            }
            if (requestDto.getRepresentativeInn() != null) {
                rep.setRepresentative(
                    Collections.singletonList(
                        new ReportCheckStatusM4dDto.Report.Representative() {{
                            setInn(requestDto.getRepresentativeInn());
                        }}
                    )
                );
            }
            rep.setStatusInRr(m4dRequestSearchSaved.getStatus());
            rep.setErrorInRrDetail(CHECK_STATUS_IN_PVV_ADAPTER);
            ReportCheckStatusM4dAndSendingPvvDto reportCheckStatusM4dAndSendingPvvDto = new ReportCheckStatusM4dAndSendingPvvDto();
            reportCheckStatusM4dAndSendingPvvDto.setReportCheckStatusM4dDto(report);
            reportCheckStatusM4dAndSendingPvvDto.setRes(res);
            return reportCheckStatusM4dAndSendingPvvDto;
        }
        return null;
    }

    @Override
    @AuditLogThis(event = AuditLogEventEnum.M4D_CHECK_IN_REQUEST_SEARCH)
    public ReportCheckStatusM4dDto getReportM4dRequestSearchForFindRR(M4dXml m4dXml, String hashXml) throws IOException {
        String guidXml = this.getGuidFromXml(m4dXml);
        M4dRequestSearch m4dRequestSearch= this.m4dRequestService.findM4dRequestByGuidAndHash(guidXml, hashXml);
        if (m4dRequestSearch != null) {
            ReportCheckStatusM4dDto report = new ReportCheckStatusM4dDto();
            report.setSource("m4d_rr");
            ReportCheckStatusM4dDto.Report rep = new ReportCheckStatusM4dDto.Report();
            report.setReport(rep);
            rep.setGuid(m4dRequestSearch.getGuid());
            rep.setStatusInRr(m4dRequestSearch.getStatus());
            rep.setErrorInRrDetail(m4dRequestSearch.getErrorDetail());
            rep.setStartDate(getStartDateM4dFromXml(m4dXml));
            rep.setEndDate(getEndDateM4dFromXml(m4dXml));
            rep.setCredentials(getCredentialM4dFromXml(m4dXml));
            rep.setRepresentative(getRepresentativeM4dFromXml(m4dXml));
            rep.setPrincipal(getPrincipalM4dFromXml(m4dXml));
            rep.setInnOrganisation(getInnOrganizationFromXml(m4dXml));
            rep.setNameOrganisation(getNameOrganizationFromXml(m4dXml));
            return report;
        } else {
            M4dRequestSearch m4dRequestSearchByGuid;
            m4dRequestSearchByGuid = this.m4dRequestService.findM4dRequestByGuid(guidXml);
            if (m4dRequestSearchByGuid != null) {
                return this.getReportM4dRequestSearchForFindRRByGuid(guidXml);
            }
        }
        return null;
    }

    private MultipartFile getM4dFromArchiveRequest(Long m4dFileId) throws IOException {
        M4dFile m4dFile;
        try {
            m4dFile = this.m4dFileService.findM4dFileById(m4dFileId);
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }
        String tempZipFile;
        FileOutputStream fo;
        String formattedDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String tempPath = "/tmp/" + formattedDateTime;
        createDirectoryIfSheNotExist(tempPath);
        tempZipFile = tempPath + "/" + m4dFile.getFilename();
        fo = new FileOutputStream(tempZipFile);
        byte[] bytes = Base64.getDecoder().decode(m4dFile.getContent());
        fo.write(bytes);
        fo.close();
        MultipartFile zipFile = mapFileToMultipart(tempZipFile);
        // Создаем временную директорию для распаковки ZIP
        Path tempDir;
        try {
            tempDir = Files.createTempDirectory("mchdZip");
        } catch (IOException e) {
            throw new RuntimeException(CREATE_TEMP_FOLDER_ERROR, e);
        }

        File xmlFile = null;
        int xmlCount = 0;
        // Распаковка ZIP-архива и сохранение всех файлов
        try (InputStream is = zipFile.getInputStream();
             ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    File outFile = new File(tempDir.toFile(), entry.getName());
                    try (OutputStream os = new FileOutputStream(outFile)) {
                        byte[] buffer = new byte[4096];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            os.write(buffer, 0, len);
                        }
                    }
                    // Если расширение xml – увеличиваем счетчик и запоминаем файл
                    if (entry.getName()
                            .toLowerCase()
                            .endsWith(".xml")) {
                        if (entry.getName()
                                .toLowerCase()
                                .startsWith("on_emchd")) {
                            xmlCount++;
                            xmlFile = outFile;
                        }
                    }
                }
            }
        } catch (IOException e) {
            cleanupDirectory(tempDir);
            throw new RuntimeException(ZIP_UNPACK_ERROR + ": " + e.getMessage(), e);
        }

        if (xmlCount == 0) {
            cleanupDirectory(tempDir);
            throw new RuntimeException(ZIP_XML_NOT_FOUND);
        } else if (xmlCount > 1) {
            cleanupDirectory(tempDir);
            throw new RuntimeException(ZIP_XML_FOUND_MORE_ONE);
        }
        return mapFileToMultipart(String.valueOf(Paths.get(xmlFile.getPath())));
    }

    @Override
    @AuditLogThis(event = AuditLogEventEnum.M4D_CHECK_IN_REQUEST_SEARCH)
    public ReportCheckStatusM4dDto getReportM4dRequestSearchForFindRRByGuid(String guidXml) throws IOException {
        M4dRequestSearch m4dRequestSearch = this.m4dRequestService.findM4dRequestByGuid(guidXml);
        if (m4dRequestSearch != null) {
            if (m4dRequestSearch.getZipFile() != null) {
                MultipartFile file = getM4dFromArchiveRequest(m4dRequestSearch.getZipFile().getId());
                M4dXml m4dXml = this.convertM4dFileToM4dXml(file);
                ReportCheckStatusM4dDto report = new ReportCheckStatusM4dDto();
                report.setSource("m4d_rr");
                ReportCheckStatusM4dDto.Report rep = new ReportCheckStatusM4dDto.Report();
                report.setReport(rep);
                rep.setGuid(m4dRequestSearch.getGuid());
                rep.setStatusInRr(m4dRequestSearch.getStatus());
                rep.setErrorInRrDetail(m4dRequestSearch.getErrorDetail());
                rep.setStartDate(getStartDateM4dFromXml(m4dXml));
                rep.setEndDate(getEndDateM4dFromXml(m4dXml));
                rep.setCredentials(getCredentialM4dFromXml(m4dXml));
                rep.setRepresentative(getRepresentativeM4dFromXml(m4dXml));
                rep.setPrincipal(getPrincipalM4dFromXml(m4dXml));
                rep.setInnOrganisation(getInnOrganizationFromXml(m4dXml));
                rep.setNameOrganisation(getNameOrganizationFromXml(m4dXml));
                return report;
            } else {
                ReportCheckStatusM4dDto report = new ReportCheckStatusM4dDto();
                report.setSource("m4d_rr");
                ReportCheckStatusM4dDto.Report rep = new ReportCheckStatusM4dDto.Report();
                report.setReport(rep);
                rep.setGuid(m4dRequestSearch.getGuid());
                if (m4dRequestSearch.getPrincipalInn() != null) {
                    rep.setPrincipal(
                            Collections.singletonList(
                                    new ReportCheckStatusM4dDto.Report.Principal() {{
                                        setInn(m4dRequestSearch.getPrincipalInn());
                                    }}
                            )
                    );
                }
                if (m4dRequestSearch.getRepresentativeInn() != null) {
                    rep.setRepresentative(
                            Collections.singletonList(
                                    new ReportCheckStatusM4dDto.Report.Representative() {{
                                        setInn(m4dRequestSearch.getRepresentativeInn());
                                    }}
                            )
                    );
                }
                rep.setStatusInRr(m4dRequestSearch.getStatus());
                rep.setErrorInRrDetail(m4dRequestSearch.getErrorDetail());
                if (m4dRequestSearch.getStartDate() != null) {
                    rep.setStartDate(m4dRequestSearch.getStartDate().toString());
                }
                if (m4dRequestSearch.getEndDate() != null) {
                    rep.setEndDate(m4dRequestSearch.getEndDate().toString());
                }
                return report;
            }
        }
        return null;
    }

    @Override
    public ReportCheckStatusM4dDto checkM4dInPep(Authentication authentication, M4dXml m4dXml) throws ParseException {
        // - извлечение номера МЧД из XML
        String guidXml;
        guidXml = this.getGuidFromXml(m4dXml);
        return this.checkM4dInPepByGuid(authentication, guidXml);
    }

    @Override
    @AuditLogThis(event = AuditLogEventEnum.M4D_CHECK_IN_PEP)
    public ReportCheckStatusM4dDto checkM4dInPepByGuid(Authentication authentication, String guidXml) throws ParseException {
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
            rep.setGuid(m4dRegistryResponseDto.getGuid());
            rep.setStatus(getStatusM4d(m4d.getId()));
            rep.setStatusInRr(m4dRegistryResponseDto.getStatusInRr());
            rep.setErrorInRrDetail(m4dRegistryResponseDto.getErrorInRrDetail());
            rep.setStartDate(m4dRegistryResponseDto.getStartDate());
            rep.setEndDate(m4dRegistryResponseDto.getEndDate());
            rep.setCredentials(convertCredentialDtoToReportCredential(getCredentialByMrpaRegistryId(m4d.getId())));
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

    @Override
    @AuditLogThis(event = AuditLogEventEnum.M4D_CHECK_IN_PEP)
    public ReportCheckStatusM4dDto checkM4dInPepByGuidAndInns(Authentication authentication, CheckByGuidRequestDto requestDto) throws ParseException {
        M4d m4d;
        try {
            m4d = this.m4dRepository.findM4dByGuidAndPrincipalInnAndRepresentativeInn(requestDto.getGuid(), requestDto.getPrincipalInn(), requestDto.getRepresentativeInn());
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(e.getMessage());
        }
        if (m4d != null) { //мчд в пэп найдено
            ReportCheckStatusM4dDto report = new ReportCheckStatusM4dDto();
            M4dRegistryResponseDto m4dRegistryResponseDto = this.convertM4dToM4dResponseDto(authentication, m4d, true, false, false, false);
            report.setSource("m4d_pep");
            ReportCheckStatusM4dDto.Report rep = new ReportCheckStatusM4dDto.Report();
            report.setReport(rep);
            rep.setGuid(m4dRegistryResponseDto.getGuid());
            rep.setStatus(getStatusM4d(m4d.getId()));
            rep.setStatusInRr(m4dRegistryResponseDto.getStatusInRr());
            rep.setErrorInRrDetail(m4dRegistryResponseDto.getErrorInRrDetail());
            rep.setStartDate(m4dRegistryResponseDto.getStartDate());
            rep.setEndDate(m4dRegistryResponseDto.getEndDate());
            rep.setCredentials(convertCredentialDtoToReportCredential(getCredentialByMrpaRegistryId(m4d.getId())));
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
                throw new BadRequestException(ORGANIZATION_INN_IS_NULL);
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
                throw new BadRequestException(PRINCIPAL_INN_IS_NULL);
            }
            Representative representative = new Representative();
            RepresentativeDetail represDetail = new RepresentativeDetail();
            for (M4dXml.Document.PowerAttorneys.InfoRepresentative c : m4dXml.document.powerAttorneys.infoRepresentative) {
                if (Objects.equals(c.getTypeRepresentative(), "3")) {
                    if (c.representative.detailRepresentative != null) {
                        representative.setInn(c.representative.detailRepresentative.getInn());
                        representative.setSnils(c.representative.detailRepresentative.getSnils());
                        representative.setBirthDate(DateUtils.convertStringToLocalDateTime(c.representative.detailRepresentative.infoIndividual.getBirthDate()));
                        represDetail.setName(c.representative.detailRepresentative.infoIndividual.fioRepresentative.getName());
                        represDetail.setSurname(c.representative.detailRepresentative.infoIndividual.fioRepresentative.getSurname());
                        represDetail.setPatronymic(c.representative.detailRepresentative.infoIndividual.fioRepresentative.getPatronymic());
                        if (representative.getInn() == null || representative.getInn().isEmpty()) {
                            throw new BadRequestException(REPRESENTATIVE_INN_IS_NULL);
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
    @Transactional(rollbackFor = {Exception.class}, isolation = Isolation.READ_UNCOMMITTED)
    @AuditLogThis(event = AuditLogEventEnum.M4D_SIGNATURE)
    public String uploadSig(Authentication authentication, Long mrpaId, MultipartFile signature) throws IOException, ParseException, JAXBException {
        String extension = FilenameUtils.getExtension(signature.getOriginalFilename());
        if (!Objects.equals(extension, "sig") && !Objects.equals(extension, "dscp")) {
            throw new BadRequestException(MRPA_REGISTRY_WRONG_FORMAT);
        }
        M4d m4d;
        try {
            m4d = this.findById(mrpaId);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(e.getMessage());
        }
        if (m4d == null) {
            throw new BadRequestException(MRPA_NOT_FOUND);
        }
        if (m4d.getSigFile() != null) {
            throw new BadRequestException(MRPA_REGISTRY_SIG);
        }
        if (Objects.equals(extension, "dscp")) {
            M4dFile m4dFile = this.m4dFileService.findM4dFileById(m4d.getXmlFile().getId());
            signature = this.containerService.getSignatureFromContainer(authentication, signature, m4dFile.getFilename());
        }
        M4dFile m4dFile = this.m4dFileService.fillM4dFile(FileMimeTypeEnum.SIG, FileLogicalTypeEnum.SIGNATURE, signature.getOriginalFilename(), signature.getBytes());
        m4d.setSigFile(m4dFile);
        m4d.setStatus(StatusM4dTypeEnum.ACTIVE.toString());
        m4d.setStatusInRr(StatusM4dInRrTypeEnum.ONCHECK.toString());
        M4d m4dSaved;
        try {
            m4dSaved = this.saveMrpaRegistry(m4d);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(e.getMessage());
        }
        // формирование и автоподписание подтверждения регистрации мчд в ПЭП
        M4dRegistryResponseDto m4DRegistryResponseDto = this.convertM4dToM4dResponseDto(authentication, m4d, true, false, true, false);
        String xmlString = m4DRegistryResponseDto.getXmlFile().getContent();
        String idFile = XMLUtil.getSubstringFromXml(xmlString, "ИдФайл=\"");
        String internalNumber = XMLUtil.getSubstringFromXml(xmlString, "ВнНомДовер=\"");
        M4dConfirm m4dConfirm = this.m4dConfirmService.createAndSignedM4dConfirm(authentication, m4d, idFile, m4d.getGuid(), internalNumber, m4d.getStartDate().toString().substring(0, 10), m4d.getEndDate().toString().substring(0, 10));
            String res = this.pvvService.sendM4dToRR(authentication, m4dSaved, m4dConfirm);
            if (res == null) {
                log.error(SENDING_RESPONSE_TO_PVV_ERROR);
            }
            return res;
    }

    @Override
    public M4dFullXML convertM4dRequestDtoToFullXml(M4dRegistryRequestDto m4DRegistryRequestDto) {
        String idFile = "ON_EMCHD_" + m4DRegistryRequestDto.getCreateDate().substring(0, 10).replace("-", "") + "_" + m4DRegistryRequestDto.mrpaRegistry.getGuid();
        M4dFullXML m4dFullXML = new M4dFullXML();
        m4dFullXML.setXmlnsXsi("http://www.w3.org/2001/XMLSchema-instance");
        m4dFullXML.setXmlnsXsd("http://www.w3.org/2001/XMLSchema");
        m4dFullXML.setXmlns("urn://x-artefacts/EMCHD_1");
        m4dFullXML.setVersionFormat(M4D_XML_VERSION_FORMAT);
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

    private List<ReportCheckStatusM4dDto.Report.Credential> convertCredentialDtoToReportCredential(List<M4dRegistryResponseDto.CredentialDto> credentialDtoList) {
        List<ReportCheckStatusM4dDto.Report.Credential> credentialList = new ArrayList<>();
        for (M4dRegistryResponseDto.CredentialDto credentialDto : credentialDtoList) {
            ReportCheckStatusM4dDto.Report.Credential credential;
            credential = modelMapper.map(credentialDto, ReportCheckStatusM4dDto.Report.Credential.class);
            credentialList.add(credential);
        }
        return credentialList;
    }

    private String getArgumentFromXml(M4dFile m4dFile) throws JAXBException {
        String content;
        if (isBase64(m4dFile.getContent())) {
            content = new String(Base64.getDecoder().decode(m4dFile.getContent()));
        } else {
            content = m4dFile.getContent();
        }
        Document document = convertStringToDocument(content);
        if (document == null) {
            throw new BadRequestException(CONVERT_FILE_TO_DOCUMENT_ERROR);
        }
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
            if (x.representative.detailRepresentative != null) {
                ReportCheckStatusM4dDto.Report.Representative representative = new ReportCheckStatusM4dDto.Report.Representative();
                representative.setInn(x.representative.detailRepresentative.getInn());
                String fio = x.representative.detailRepresentative.infoIndividual.fioRepresentative.getSurname() + " "
                        + x.representative.detailRepresentative.infoIndividual.fioRepresentative.getName() + " "
                        + x.representative.detailRepresentative.infoIndividual.fioRepresentative.getPatronymic();
                representative.setFio(fio);
                representatives.add(representative);
            }
        }
        return representatives;
    }

    @Override
    public List<ReportCheckStatusM4dDto.Report.Principal> getPrincipalM4dFromXml(M4dXml m4dXml) {
        List<ReportCheckStatusM4dDto.Report.Principal> principals = new ArrayList<>();
        M4dXml.Document.PowerAttorneys.InfoPrincipals principal = m4dXml.document.powerAttorneys.getPrincipals();
        // TODO: helix82 Изначально почему то заложено что один доверитель с мчд, надо переделать
        if (principal.principal.legalEntity.principal.principal != null) {
            String fio = principal.principal.legalEntity.principal.principal.fioPrincipal.fio.getSurname() + " "
                    + principal.principal.legalEntity.principal.principal.fioPrincipal.fio.getName() + " "
                    + principal.principal.legalEntity.principal.principal.fioPrincipal.fio.getPatronymic();
            ReportCheckStatusM4dDto.Report.Principal principal1 = new ReportCheckStatusM4dDto.Report.Principal();
            principal1.setInn(principal.principal.legalEntity.principal.principal.getInn());
            principal1.setFio(fio);
            principals.add(principal1);
        }
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
            if (document == null) {
                throw new BadRequestException(CONVERT_FILE_TO_DOCUMENT_ERROR);
            }
            m4dXml = XMLUtil.unmarshal(document, M4dXml.class);
        } catch (Exception e) {
            log.error(MRPA_REGISTRY_WRONG_FORMAT_XML + ": " + UNMARSHALING_ERROR);
            throw new BadRequestException(MRPA_REGISTRY_WRONG_FORMAT_XML);
        }

        if (m4dXml == null) {
            throw new BadRequestException(MRPA_NOT_FOUND);
        }
        if (m4dXml.document.getPowerAttorneys() == null) {
            log.error(MRPA_REGISTRY_WRONG_FORMAT_XML + ": " + XML_MRPA_NOT_FOUND);
            throw new BadRequestException(MRPA_REGISTRY_WRONG_FORMAT_XML);
        }
        if (m4dXml.document.powerAttorneys.getMrpa().getGuid().isEmpty()) {
            log.error(MRPA_REGISTRY_WRONG_FORMAT_XML + ": " + XML_MRPA_GUID_NOT_FOUND);
            throw new BadRequestException(MRPA_REGISTRY_WRONG_FORMAT_XML);
        }
        return m4dXml;
    }

    @Override
    public String getHashXmlM4d(MultipartFile sourceFile) throws NoSuchAlgorithmException {
        return HashUtils.getHash(XMLUtil.readXmlToString(sourceFile).getBytes(), M4D_SOUL);
    }

    @Override
    public ReportValidSchemeDto validXmlM4dScheme(MultipartFile sourceFile, String reqScheme) throws IOException, SAXException {
        String scheme;
        ReportValidSchemeDto report = new ReportValidSchemeDto();
        report.setFileName(sourceFile.getOriginalFilename());
        if (reqScheme == null) {
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
    public ReportCheckStatusM4dDto updateStatusM4dRequestSearch(MultipartFile resource) throws IOException {
        UnpackedZipDto unpackedZipDto = this.unpackZip(resource);
        File jsonFile = unpackedZipDto.getJsonFile();
        File xmlFileKvitM4d = unpackedZipDto.getXmlFileKvitM4d();
        File xmlFileKvitM4dRevoke = unpackedZipDto.getXmlFileKvitM4dRevoke();
        File sigFileKvitM4d = unpackedZipDto.getSigFileKvitM4d();
        File sigFileKvitM4dRevoke = unpackedZipDto.getSigFileKvitM4dRevoke();
        File zipFile = unpackedZipDto.getZipFile();

        if (jsonFile == null) {
            throw new BadRequestException(ZIP_JSON_NOT_FOUND);
        }

        M4d m4d;
        M4dFnsDataResponse m4dFnsDataResponse = new M4dFnsDataResponse();

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            m4dFnsDataResponse = objectMapper.readValue(jsonFile, M4dFnsDataResponse.class);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        if (m4dFnsDataResponse == null) {
            throw new BadRequestException(JSON_READ_ERROR);
        }
        m4d = this.findMrpaRegistryByGuid(m4dFnsDataResponse.getGuid());


        if (m4d != null) {
            // мчд наша
            if (m4dFnsDataResponse.getError() != null) { //ошибка
                m4d.setStatusInRr(StatusM4dInRrTypeEnum.ERROR.toString());
                m4d.setErrorInRrDetail(getErrorStringFromM4dFnsDataResponse(m4dFnsDataResponse));
                M4d m4dSaved = null;
                try {
                    m4dSaved = this.updateM4d(m4d);
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
                if (m4dSaved == null) {
                    throw new BadRequestException(MRPA_REGISTRY_UPDATE_STATUS_ERROR + m4d.getGuid());
                }
                // сформировать отчет
                return this.getReportCheckStatusM4dPep(m4dSaved);
            } else {
                if (m4dFnsDataResponse.getStatus().equals(StatusRequestTypeEnum.ACTIVE.toString()) || m4dFnsDataResponse.getStatus().equals(StatusRequestTypeEnum.CREATED.toString()) || m4dFnsDataResponse.getStatus().equals(StatusRequestTypeEnum.EXPIRED.toString())) {
                    m4d.setStatusInRr(StatusM4dInRrTypeEnum.REGISTERED.toString());
                } else if (m4dFnsDataResponse.getStatus().equals(StatusRequestTypeEnum.REVOKED.toString())) {
                    m4d.setStatusInRr(StatusM4dInRrTypeEnum.REVOKED.toString());
                    M4dRecall m4dRecall = this.m4dRecallService.findMrpaRecallByM4dId(m4d.getId());
                    m4dRecall.setStatusInRr(StatusM4dRevokeInRrTypeEnum.SUCCESS.toString());
                    m4dRecall.setErrorInRrDetail(null);
                    M4dRecall m4dRecallUpdate = this.m4dRecallService.updateMrpaRecall(m4dRecall);
                    if (m4dRecallUpdate == null) {
                        log.error(String.format(MRPA_RECALL_UPDATE_STATUS_RR_ERROR, m4dRecall.getM4d().getGuid()));
                    }
                } else {
                    m4d.setStatusInRr(m4dFnsDataResponse.getStatus());
                }
                m4d.setErrorInRrDetail(null);
                M4d m4dSaved = null;
                try {
                    m4dSaved = this.updateM4d(m4d);
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
                if (m4dSaved == null) {
                    throw new BadRequestException(MRPA_REGISTRY_UPDATE_STATUS_ERROR + m4dFnsDataResponse.getGuid());
                }
                if (xmlFileKvitM4d != null && sigFileKvitM4d != null) {
                    // сохранить в бд квитанцию с подписью о регистрации мчд в рр
                    M4dConfirm m4dConfirmUpdate = this.m4dConfirmService.saveConfirmAndSignatureRr(m4d.getId(), xmlFileKvitM4d, sigFileKvitM4d);
                    if (m4dConfirmUpdate == null) {
                        throw new BadRequestException(String.format(MRPA_REGISTRY_CONFIRM_SAVE_ERROR, m4dFnsDataResponse.getGuid()));
                    }
                    if (xmlFileKvitM4dRevoke != null && sigFileKvitM4dRevoke != null) {
                        // сохранить в бд квитанцию с подписью о регистрации отзыва мчд в рр
                        M4dRecall m4dRecall = this.m4dRecallService.findMrpaRecallByM4dId(m4d.getId());
                        M4dRecallConfirm m4dRecallConfirmUpdate = this.m4dRecallConfirmService.saveConfirmAndSignatureRr(m4dRecall.getId(), xmlFileKvitM4dRevoke, sigFileKvitM4dRevoke);
                        if (m4dRecallConfirmUpdate == null) {
                            throw new BadRequestException(String.format(MRPA_RECALL_CONFIRM_SAVE_ERROR, m4dFnsDataResponse.getGuid()));
                        }
                    }
                } else {
                    if (zipFile != null) {
                        UnpackedZipDto unpackedZipDto1 = this.unpackZip(mapFileToMultipart(String.valueOf(Paths.get(zipFile.getPath()))));
                        File xmlFileKvitM4d1 = unpackedZipDto1.getXmlFileKvitM4d();
                        File xmlFileKvitM4dRevoke1 = unpackedZipDto1.getXmlFileKvitM4dRevoke();
                        File sigFileKvitM4d1 = unpackedZipDto1.getSigFileKvitM4d();
                        File sigFileKvitM4dRevoke1 = unpackedZipDto1.getSigFileKvitM4dRevoke();
                        if (xmlFileKvitM4d1 != null && sigFileKvitM4d1 != null) {
                            // сохранить в бд квитанцию с подписью о регистрации мчд в рр
                            M4dConfirm m4dConfirmUpdate = this.m4dConfirmService.saveConfirmAndSignatureRr(m4d.getId(), xmlFileKvitM4d1, sigFileKvitM4d1);
                            if (m4dConfirmUpdate == null) {
                                throw new BadRequestException(String.format(MRPA_REGISTRY_CONFIRM_SAVE_ERROR, m4dFnsDataResponse.getGuid()));
                            }
                            if (xmlFileKvitM4dRevoke1 != null && sigFileKvitM4dRevoke1 != null) {
                                // сохранить в бд квитанцию с подписью о регистрации отзыва мчд в рр
                                M4dRecall m4dRecall = this.m4dRecallService.findMrpaRecallByM4dId(m4d.getId());
                                M4dRecallConfirm m4dRecallConfirmUpdate = this.m4dRecallConfirmService.saveConfirmAndSignatureRr(m4dRecall.getId(), xmlFileKvitM4dRevoke1, sigFileKvitM4dRevoke1);
                                if (m4dRecallConfirmUpdate == null) {
                                    throw new BadRequestException(String.format(MRPA_RECALL_CONFIRM_SAVE_ERROR, m4dFnsDataResponse.getGuid()));
                                }
                            }
                        }
                    }
                }

                // сформировать отчет
                return this.getReportCheckStatusM4dPep(m4d);
            }
        } else {
            // мчд сторонняя
            if (m4dFnsDataResponse.getError() != null) { //ошибка
                M4dRequestSearch m4dRequestSearch = this.m4dRequestService.findM4dRequestByGuid(m4dFnsDataResponse.getGuid());
                if (m4dRequestSearch != null) {
                    if (m4dRequestSearch.getHashXml() != null && m4dFnsDataResponse.getId() != null && !m4dRequestSearch.getHashXml().equals(m4dFnsDataResponse.getId())) {
                        throw new BadRequestException(String.format(MRPA_REGISTRY_HASH_FILE_ERROR, m4dFnsDataResponse.getGuid()));
                    } else if (m4dRequestSearch.getHashXml() == null && m4dFnsDataResponse.getId() != null) {
                        m4dRequestSearch.setHashXml(m4dFnsDataResponse.getId());
                    }
                    m4dRequestSearch.setStatus(StatusRequestTypeEnum.ERROR.toString());
                    m4dRequestSearch.setErrorDetail(getErrorStringFromM4dFnsDataResponse(m4dFnsDataResponse));
                    m4dRequestSearch.setStatusDate(DateUtils.convertStringToLocalDateTime(DateUtils.getCurrentDate()));
                    // сохранить в бд архив с данными о мчд из рр
                    if (zipFile != null) {
                        M4dFile m4dFileZip = this.m4dFileService.fillM4dFile(FileMimeTypeEnum.ZIP, FileLogicalTypeEnum.ARCHIVE, zipFile.getName(), FileUtils.getFileContentByPath(zipFile.getPath()));
                        m4dRequestSearch.setZipFile(m4dFileZip);
                    }
                    M4dRequestSearch m4dRequestSearchSaved;
                    m4dRequestSearchSaved = this.m4dRequestService.updateM4dRequestSearch(m4dRequestSearch);
                    if (m4dRequestSearchSaved == null) {
                        throw new BadRequestException(MRPA_REGISTRY_UPDATE_STATUS_ERROR + m4dFnsDataResponse.getGuid());
                    }
                    return this.getReportCheckStatusM4dRr(m4dRequestSearchSaved);
                } else {
                    throw new BadRequestException(MRPA_REGISTRY_UPDATE_STATUS_ERROR + m4dFnsDataResponse.getGuid() + ". " + MRPA_REQUEST_SEARCH_NOT_FOUND);
                }
            } else { // нет ошибки
                M4dRequestSearch m4dRequestSearch = this.m4dRequestService.findM4dRequestByGuid(m4dFnsDataResponse.getGuid());
                if (m4dRequestSearch != null) {
                    m4dRequestSearch.setStatus(m4dFnsDataResponse.getStatus());
                    m4dRequestSearch.setErrorDetail(null);
                    if (m4dFnsDataResponse.getStatusDateTime() != null) {
                        m4dRequestSearch.setStatusDate(DateUtils.convertStringToLocalDateTime(m4dFnsDataResponse.getStatusDateTime()));
                    } else {
                        m4dRequestSearch.setStatusDate(DateUtils.convertStringToLocalDateTime(DateUtils.getCurrentDate()));
                    }
                    if (m4dFnsDataResponse.getCreatedDateTime() != null) {
                        m4dRequestSearch.setRegistrationDate(DateUtils.convertStringToLocalDateTime(m4dFnsDataResponse.getCreatedDateTime()));
                    }
                    if (m4dFnsDataResponse.getStartDateTime() != null) {
                        m4dRequestSearch.setStartDate(DateUtils.convertStringToLocalDateTime(m4dFnsDataResponse.getStartDateTime()));
                    }
                    if (m4dFnsDataResponse.getEndDateTime() != null) {
                        m4dRequestSearch.setEndDate(DateUtils.convertStringToLocalDateTime(m4dFnsDataResponse.getEndDateTime()));
                    }
                    // сохранить в бд архив с данными о мчд из рр
                    if (zipFile != null) {
                        M4dFile m4dFileZip = this.m4dFileService.fillM4dFile(FileMimeTypeEnum.ZIP, FileLogicalTypeEnum.ARCHIVE, zipFile.getName(), FileUtils.getFileContentByPath(zipFile.getPath()));
                        m4dRequestSearch.setZipFile(m4dFileZip);
                    }
                    M4dRequestSearch m4dRequestSearchSaved;
                    m4dRequestSearchSaved = this.m4dRequestService.updateM4dRequestSearch(m4dRequestSearch);
                    if (m4dRequestSearchSaved == null) {
                        throw new BadRequestException(MRPA_REGISTRY_UPDATE_STATUS_ERROR + m4dFnsDataResponse.getGuid());
                    }
                    //формируем отчет
                    ReportCheckStatusM4dDto report = this.getReportCheckStatusM4dRr(m4dRequestSearchSaved);
                    List<ReportCheckStatusM4dDto.Report.Representative> repList = report.getReport().getRepresentative();
                    if (repList.isEmpty()) {
                        ReportCheckStatusM4dDto.Report.Representative rep = new ReportCheckStatusM4dDto.Report.Representative();
                        rep.setInn(m4dRequestSearch.getRepresentativeInn());
                        repList.add(rep);
                    }
                    ReportCheckStatusM4dDto.Report.Principal principal = report.getReport().getPrincipal().get(0);
                    if (principal.getInn() == null) {
                        principal.setInn(m4dRequestSearch.getPrincipalInn());
                    }
                    return report;
                }
            }
        }
        return null;
    }

    @Override
    public String uploadResultSendM4dToRR(M4dResponseInfo m4dResponseInfo) {
        if (m4dResponseInfo.getError() != null) {
            String error = String.format(RESULT_SEND_ERROR_CODE_TITLE, m4dResponseInfo.getError().getStatus(), m4dResponseInfo.getError().getTitle());
            if (m4dResponseInfo.getError().getDetail() != null) {
                error = error + String.format(RESULT_SEND_ERROR_DETAIL, m4dResponseInfo.getError().getDetail());
            }
            M4d m4d = this.m4dRepository.findByGuid(m4dResponseInfo.getPoaNumber());
            if (m4d == null) {
                return String.format(MRPA_REGISTRY_UPLOAD_RESULT_SEND_RR_MRPA_NOT_FOUND, m4dResponseInfo.getPoaNumber());
            }
            m4d.setStatusInRr(StatusM4dInRrTypeEnum.ERROR.toString());
            m4d.setErrorInRrDetail(error);
            M4d m4dSaved;
            m4dSaved = this.updateM4d(m4d);
            if (m4dSaved == null) {
                return String.format(MRPA_REGISTRY_UPLOAD_RESULT_SEND_RR_UPDATE_STATUS_ERROR, m4dResponseInfo.getPoaNumber());
            }
        }
        return null;
    }

    private ReportCheckStatusM4dDto getReportCheckStatusM4dPep(M4d m4d) throws IOException {
        // сформировать отчет
        ReportCheckStatusM4dDto report = new ReportCheckStatusM4dDto();
        report.setSource("m4d_pep");
        ReportCheckStatusM4dDto.Report rep = new ReportCheckStatusM4dDto.Report();
        report.setReport(rep);
        rep.setGuid(m4d.getGuid());
        rep.setStatus(m4d.getStatus());
        rep.setStatusInRr(m4d.getStatusInRr());
        rep.setErrorInRrDetail(m4d.getErrorInRrDetail());
        rep.setStartDate(m4d.getStartDate().toString());
        rep.setEndDate(m4d.getEndDate().toString());

        MultipartFile sourceFile = FileUtils.mapStringToMultipart(new String(EncodingUtils.decodeFileFromBase64(m4d.getXmlFile().getContent())), m4d.getXmlFile().getFilename());
        M4dXml m4dXml = this.convertM4dFileToM4dXml(sourceFile);

        rep.setCredentials(getCredentialM4dFromXml(m4dXml));
        rep.setRepresentative(getRepresentativeM4dFromXml(m4dXml));
        rep.setPrincipal(getPrincipalM4dFromXml(m4dXml));
        rep.setInnOrganisation(getInnOrganizationFromXml(m4dXml));
        rep.setNameOrganisation(getNameOrganizationFromXml(m4dXml));
        return report;
    }
    private ReportCheckStatusM4dDto getReportCheckStatusM4dRr(M4dRequestSearch m4dRequestSearch) throws IOException {
        //формируем отчет
        ReportCheckStatusM4dDto report = new ReportCheckStatusM4dDto();
        report.setSource("m4d_rr");
        ReportCheckStatusM4dDto.Report rep = new ReportCheckStatusM4dDto.Report();
        report.setReport(rep);
        rep.setGuid(m4dRequestSearch.getGuid());
        rep.setStatusInRr(m4dRequestSearch.getStatus());
        rep.setErrorInRrDetail(m4dRequestSearch.getErrorDetail());
        if (m4dRequestSearch.getZipFile() != null) {
            MultipartFile file = getM4dFromArchiveRequest(m4dRequestSearch.getZipFile().getId());
            M4dXml m4dXml = this.convertM4dFileToM4dXml(file);
            rep.setStartDate(getStartDateM4dFromXml(m4dXml));
            rep.setEndDate(getEndDateM4dFromXml(m4dXml));
            rep.setCredentials(getCredentialM4dFromXml(m4dXml));
            rep.setRepresentative(getRepresentativeM4dFromXml(m4dXml));
            rep.setPrincipal(getPrincipalM4dFromXml(m4dXml));
            rep.setInnOrganisation(getInnOrganizationFromXml(m4dXml));
            rep.setNameOrganisation(getNameOrganizationFromXml(m4dXml));
        }
        return report;
    }

    private String getErrorStringFromM4dFnsDataResponse(M4dFnsDataResponse response) {
        String error;
        error = String.format(RESULT_SEND_ERROR_CODE_TITLE, response.getError().getStatus(), response.getError().getTitle());
        if (response.getError().getDetail() != null && !response.getError().getDetail().isEmpty()) {
            error = error + String.format(RESULT_SEND_ERROR_DETAIL, response.getError().getDetail());
        }
        return error;
    }

    private void cleanupDirectory(Path directory) {
        try {
            Files.walk(directory)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            log.warn("Ошибка очистки временной директории {}: {}", directory, e.getMessage());
        }
    }

    @Override
    public Integer updateStatusM4dInRR() {
        Iterable<M4d> m4ds;
        try {
            m4ds = m4dRepository.findAll();
        } catch (ConstraintViolationException e) {
            log.info(e.getMessage());
            return null;
        }
        Integer count = 0;
        for (M4d m4d : m4ds) {
            if (m4d.getGuid() != null && m4d.getStatusInRr() != null && (m4d.getStatusInRr().equals("ONCHECK") || m4d.getStatusInRr().equals("ERROR"))) {
                String res = this.pvvService.sendRequestStatusM4dToRR(m4d.getGuid());
                if (res == null) {
                    log.error(SENDING_RESPONSE_TO_PVV_ERROR);
                } else {
                    count++;
                }
            }
        }
        log.info(String.format(COUNT_SEND_REQUEST_IN_PVV, count));
        return count;
    }

    @Override
    public Integer updateRequestStatusM4dInRR() {
        List<M4dRequestSearch> m4dRequestSearchList;
        try {
            m4dRequestSearchList = this.m4dRequestService.findAllM4dRequestSearch();
        } catch (ConstraintViolationException e) {
            log.info(e.getMessage());
            return null;
        }
        Integer count = 0;
        String res;
        for (M4dRequestSearch m4dRequestSearch : m4dRequestSearchList) {
            if (m4dRequestSearch.getGuid() != null &&  (m4dRequestSearch.getStatus().equals("ONCHECK") || m4dRequestSearch.getStatus().equals("ERROR"))) {
                if (m4dRequestSearch.getPrincipalInn() != null && m4dRequestSearch.getRepresentativeInn() != null){
                    CheckByGuidRequestDto requestDto = new CheckByGuidRequestDto();
                    requestDto.setGuid(m4dRequestSearch.getGuid());
                    requestDto.setPrincipalInn(m4dRequestSearch.getPrincipalInn());
                    requestDto.setRepresentativeInn(m4dRequestSearch.getRepresentativeInn());
                    res = this.pvvService.sendRequestStatusM4dToRRByGuidAndInns(requestDto);
                    if (res == null) {
                        log.error(SENDING_RESPONSE_TO_PVV_ERROR);
                    } else {
                        count++;
                    }
                } else {
                    res = this.pvvService.sendRequestStatusM4dToRR(m4dRequestSearch.getGuid());
                    if (res == null) {
                        log.error(SENDING_RESPONSE_TO_PVV_ERROR);
                    } else {
                        count++;
                    }
                }
            }
        }
        log.info(String.format(COUNT_SEND_REQUEST_DATA_IN_PVV, count));
        return count;
    }

    @Override
    public void sendM4dAllRegisterInRr() throws IOException {
        Authentication authentication = createJwtAuthentication(DEFAULT_USER_NAME);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Iterable<M4d> m4ds;
        try {
            m4ds = m4dRepository.findAll();
        } catch (ConstraintViolationException e) {
            log.error(e.getMessage());
            return;
        }
        Integer count = 0;
        for (M4d m4d : m4ds) {
            if (m4d.getGuid() != null && m4d.getStatusInRr() != null && m4d.getStatusInRr().equals("ERROR") && (m4d.getErrorInRrDetail().startsWith("Код: 500") || m4d.getErrorInRrDetail().startsWith("Код: 404"))) {
                // отправка архива с xml-файлом мчд и подписью на регистрацию в РР МЧД
                M4dConfirm m4dConfirm = this.m4dConfirmService.findM4dConfirmByM4dId(m4d.getId());
                String res = this.pvvService.sendM4dToRR(authentication, m4d, m4dConfirm);
                if (res == null) {
                    log.error(String.format(SENDING_M4D_TO_PVV_ERROR, m4d.getGuid()));
                    continue;
                }
                m4d.setStatusInRr(StatusM4dInRrTypeEnum.ONCHECK.toString());
                m4d.setErrorInRrDetail(null);
                M4d m4dSaved;
                try {
                    m4dSaved = this.updateM4d(m4d);
                } catch (ConstraintViolationException e) {
                    log.error(e.getMessage());
                    continue;
                }
                if (m4dSaved == null) {
                    log.error(String.format(MRPA_REGISTRY_UPDATE_STATUS_RR_ERROR, m4d.getGuid()));
                }
                count++;
            }
        }
        SecurityContextHolder.clearContext();
        log.info(String.format(COUNT_REGISTER_M4D_IN_PVV, count));
    }

    @Override
    public void sendM4dRevokeAllRegisterInRr() throws IOException {
        Authentication authentication = createJwtAuthentication(DEFAULT_USER_NAME);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        List<M4dRecall> m4dRecalls;
        try {
            m4dRecalls = this.m4dRecallService.findAllRecall();
        } catch (ConstraintViolationException e) {
            log.error(e.getMessage());
            return;
        }
        Integer count = 0;
        for (M4dRecall m4dRecall : m4dRecalls) {
            if (m4dRecall.getStatusInRr() != null && m4dRecall.getStatusInRr().equals("ERROR")) {
                // отправка архива с xml-файлом мчд и подписью на регистрацию в РР МЧД
                M4dRecallConfirm m4dRecallConfirm = this.m4dRecallConfirmService.findM4dRecallConfirmByM4dRecallId(m4dRecall.getId());
                String res = this.pvvService.sendM4dRevokeToRR(authentication, m4dRecall, m4dRecallConfirm);
                if (res == null) {
                    log.error(String.format(SENDING_M4D_REVOKE_TO_PVV_ERROR, m4dRecall.getM4d().getGuid()));
                    continue;
                }
                m4dRecall.setStatusInRr(StatusM4dInRrTypeEnum.ONCHECK.toString());
                m4dRecall.setErrorInRrDetail(null);
                M4dRecall m4dRecallSaved;
                try {
                    m4dRecallSaved = this.m4dRecallService.updateMrpaRecall(m4dRecall);
                } catch (ConstraintViolationException e) {
                    log.error(e.getMessage());
                    continue;
                }
                if (m4dRecallSaved == null) {
                    log.error(String.format(MRPA_RECALL_UPDATE_STATUS_RR_ERROR, m4dRecall.getM4d().getGuid()));
                }
                count++;
            }
        }
        SecurityContextHolder.clearContext();
        log.info(String.format(COUNT_REGISTER_M4D_REVOKE_IN_PVV, count));
    }

    private Authentication createJwtAuthentication(String username) {
        Jwt jwt = new Jwt("tokenValue", Instant.now(), Instant.now().plusSeconds(3600),
                Collections.singletonMap("alg", "none"),
                Map.of("sub", username));
        return new JwtAuthenticationToken(jwt, Collections.emptyList());
    }

    @Override
    public String sendRequestStatusM4dInPvv(String guid) {
        String res = this.pvvService.sendRequestStatusM4dToRR(guid);
        if (res == null) {
            log.error(SENDING_RESPONSE_TO_PVV_ERROR);
            return null;
        }
        return res;
    }

    @Override
    public String sendRequestFullM4dInPvv(CheckByGuidRequestDto requestDto) {
        String res = this.pvvService.sendRequestStatusM4dToRRByGuidAndInns(requestDto);
        if (res == null) {
            log.error(SENDING_RESPONSE_TO_PVV_ERROR);
            return null;
        }
        return res;
    }

    @Override
    public String sendM4dRegisterInRr(Authentication authentication, Long id) throws IOException {
        M4d m4d = this.findById(id);
        if (m4d == null) {
            throw new BadRequestException(MRPA_NOT_FOUND);
        }
        // отправка архива с xml-файлом мчд и подписью на регистрацию в РР МЧД
        M4dConfirm m4dConfirm = this.m4dConfirmService.findM4dConfirmByM4dId(id);
        String res = this.pvvService.sendM4dToRR(authentication, m4d, m4dConfirm);
        if (res == null) {
            log.error(SENDING_RESPONSE_TO_PVV_ERROR);
            return null;
        }
        m4d.setStatusInRr(StatusM4dInRrTypeEnum.ONCHECK.toString());
        m4d.setErrorInRrDetail(null);
        M4d m4dSaved;
        try {
            m4dSaved = this.updateM4d(m4d);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(e.getMessage());
        }
        if (m4dSaved == null) {
            log.error(String.format(MRPA_REGISTRY_UPDATE_STATUS_RR_ERROR, m4d.getGuid()));
        }
        return res;
    }

    private UnpackedZipDto unpackZip(MultipartFile resource) {
        // Создаем временную директорию для распаковки ZIP
        Path tempDir;
        try {
            tempDir = Files.createTempDirectory("M4dRequestSearchZip");
        } catch (IOException e) {
            throw new BadRequestException(TEMP_DIR_CREATE_ERROR);
        }
        File jsonFile = null;
        File xmlFileM4d = null;
        File xmlFileM4dRevoke = null;
        File xmlFileKvitM4d = null;
        File xmlFileKvitM4dRevoke = null;
        File sigFileM4d = null;
        File sigFileM4dRevoke = null;
        File sigFileKvitM4d = null;
        File sigFileKvitM4dRevoke = null;
        File zipFile = null;
        int jsonCount = 0;
        int xmlCount = 0;
        int sigCount = 0;
        int zipCount = 0;
        // Распаковка ZIP-архива и сохранение всех файлов
        try (InputStream is = resource.getInputStream();
             ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    File outFile = new File(tempDir.toFile(), entry.getName());
                    try (OutputStream os = new FileOutputStream(outFile)) {
                        byte[] buffer = new byte[4096];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            os.write(buffer, 0, len);
                        }
                    }
                    // Если расширение json – увеличиваем счетчик и запоминаем файл
                    if (entry.getName()
                            .toLowerCase()
                            .endsWith(".json")) {
                        jsonCount++;
                        jsonFile = outFile;
                    }
                    // Если расширение zip – увеличиваем счетчик и запоминаем файл
                    if (entry.getName()
                            .toLowerCase()
                            .endsWith(".zip")) {
                        zipCount++;
                        zipFile = outFile;
                    }
                    // Если расширение xml – увеличиваем счетчик и запоминаем файл
                    if (entry.getName()
                            .toLowerCase()
                            .endsWith(".xml")) {
                        xmlCount++;
                        if (entry.getName()
                                .toLowerCase()
                                .startsWith("квит003_фнс_on_emchd")) {
                            xmlFileKvitM4d = outFile;
                        } else if (entry.getName()
                                .toLowerCase()
                                .startsWith("квит003_фнс_заявотм")) {
                            xmlFileKvitM4dRevoke = outFile;
                        } else if (entry.getName()
                                .toLowerCase()
                                .startsWith("on_emchd")) {
                            xmlFileM4d = outFile;
                        } else if (entry.getName()
                                .toLowerCase()
                                .startsWith("заявотм")) {
                            xmlFileM4dRevoke = outFile;
                        }
                    }
                    // Если расширение sig – увеличиваем счетчик и запоминаем файл
                    if (entry.getName()
                            .toLowerCase()
                            .endsWith(".sig")) {
                        sigCount++;
                        if (entry.getName()
                                .toLowerCase()
                                .startsWith("квит003_фнс_on_emchd")) {
                            sigFileKvitM4d = outFile;
                        } else if (entry.getName()
                                .toLowerCase()
                                .startsWith("квит003_фнс_заявотм")) {
                            sigFileKvitM4dRevoke = outFile;
                        } else if (entry.getName()
                                .toLowerCase()
                                .startsWith("on_emchd")) {
                            sigFileM4d = outFile;
                        } else if (entry.getName()
                                .toLowerCase()
                                .startsWith("заявотм")) {
                            sigFileM4dRevoke = outFile;
                        }
                    }
                }
            }
        } catch (IOException e) {
            cleanupDirectory(tempDir);
            throw new BadRequestException(ZIP_UNPACK_ERROR + ": " + e.getMessage());
        }
        if (zipCount > 1) {
            cleanupDirectory(tempDir);
            throw new BadRequestException(ZIP_ZIP_FOUND_MORE_ONE);
        }
        if (jsonCount > 1) {
            cleanupDirectory(tempDir);
            throw new BadRequestException(ZIP_JSON_FOUND_MORE_ONE);
        }
        if (xmlCount > 4) {
            cleanupDirectory(tempDir);
            throw new BadRequestException(ZIP_XML_FOUND_MORE_FOUR);
        }
        if (sigCount > 4) {
            cleanupDirectory(tempDir);
            throw new BadRequestException(ZIP_SIG_FOUND_MORE_FOUR);
        }
        UnpackedZipDto unpackedZipDto = new UnpackedZipDto();
        unpackedZipDto.setZipFile(zipFile);
        unpackedZipDto.setJsonFile(jsonFile);
        unpackedZipDto.setSigFileKvitM4d(sigFileKvitM4d);
        unpackedZipDto.setSigFileKvitM4dRevoke(sigFileKvitM4dRevoke);
        unpackedZipDto.setXmlFileKvitM4d(xmlFileKvitM4d);
        unpackedZipDto.setXmlFileKvitM4dRevoke(xmlFileKvitM4dRevoke);

        unpackedZipDto.setSigFileM4d(sigFileM4d);
        unpackedZipDto.setSigFileM4dRevoke(sigFileM4dRevoke);
        unpackedZipDto.setXmlFileM4d(xmlFileM4d);
        unpackedZipDto.setXmlFileM4dRevoke(xmlFileM4dRevoke);

        return unpackedZipDto;
    }

}

