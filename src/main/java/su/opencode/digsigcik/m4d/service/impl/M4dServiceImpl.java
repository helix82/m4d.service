package su.opencode.digsigcik.m4d.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.codehaus.plexus.util.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import su.opencode.digsigcik.audit.AuditLogEventEnum;
import su.opencode.digsigcik.audit.AuditLogThis;
import su.opencode.digsigcik.m4d.dto.*;
import su.opencode.digsigcik.m4d.dto.response.AuthResponse;
import su.opencode.digsigcik.m4d.dto.response.M4dFnsDataResponse;
import su.opencode.digsigcik.m4d.enums.FileLogicalTypeEnum;
import su.opencode.digsigcik.m4d.enums.FileMimeTypeEnum;
import su.opencode.digsigcik.m4d.enums.HashAlgorithm;
import su.opencode.digsigcik.m4d.exception.BadRequestException;
import su.opencode.digsigcik.m4d.exception.M4dException;
import su.opencode.digsigcik.m4d.model.*;
import su.opencode.digsigcik.m4d.repository.M4dCredentialRepository;
import su.opencode.digsigcik.m4d.repository.M4dRepository;
import su.opencode.digsigcik.m4d.repository.M4dVerifyStatusRepository;
import su.opencode.digsigcik.m4d.service.*;
import su.opencode.digsigcik.m4d.util.DateUtils;
import su.opencode.digsigcik.m4d.util.HashUtils;
import su.opencode.digsigcik.m4d.util.XMLUtil;
import su.opencode.digsigcik.m4d.util.XmlValidator;

import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;
import javax.xml.bind.JAXBException;
import java.io.*;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static su.opencode.digsigcik.m4d.util.Constants.*;
import static su.opencode.digsigcik.m4d.util.JwtTokenUtils.getTokenValue;
import static su.opencode.digsigcik.m4d.util.XMLUtil.convertStringToDocument;

@Slf4j
@Service
@Primary
@RequiredArgsConstructor
@EnableTransactionManagement
public class M4dServiceImpl implements M4dService {
    @Value("${m4d.api_rr_fns.uri}")
    private String API_RR_FNS_URI;
    private final ModelMapper modelMapper;
    private final M4dRepository m4dRepository;
    private final M4dVerifyStatusRepository m4dVerifyStatusRepository;
    private final M4dRecallService m4dRecallService;
    private final M4dCredentialRepository m4dCredentialRepository;
    private final M4dFileService m4dFileService;
    private final CredentialService credentialService;
    private final OrganisationService organisationService;
    private final PrincipalService principalService;
    private final PrincipalDetailService principalDetailService;
    private final RepresentativeService representativeService;
    private final RepresentativePassportService representativePassportService;
    private final UserCertificateService userCertificateService;
    private final RestTemplate restTemplate;
    private final SynapiClientService synapiClientService;

    @Override
    public List<M4dResponseDto> findAllMrpaRegistry() throws ParseException {
        List<M4d> m4ds = m4dRepository.findAll();
        List<M4dResponseDto> m4dResponseDtoList = new ArrayList<>();
        for (M4d m4d : m4ds) {
            m4dResponseDtoList.add(convertM4dToM4dResponseDto(m4d, false, false));
        }
        return m4dResponseDtoList;
    }

    @Override
    public M4d saveMrpaRegistry(@NotNull M4d m4d) {
        return this.m4dRepository.save(m4d);
    }

    @Override
    @AuditLogThis(event = AuditLogEventEnum.M4D_RECALL_CREATE)
    public M4d updateMrpaRegistry(@NotNull M4d mrpaRegistry) {
        return this.m4dRepository.save(mrpaRegistry);
    }

    @Transactional(rollbackFor = {Exception.class}, isolation = Isolation.READ_UNCOMMITTED)
    @AuditLogThis(event = AuditLogEventEnum.M4D_CREATE)
    public Long saveAllDataMrpaRegistry(M4d m4d, Organisation organisation, Principal principal, PrincipalDetail principalDetail, Representative representative, RepresentativePassport representativePassport, List<CredentialDto> credentialList, List<Long> credentials) {
        Long orgId;
        Long principId;
        Long principDetailId;
        Long represId;
        if (organisation.getInn() == null) {
            throw new BadRequestException("The organization's INN is not specified");
        }
        if (principal.getInn() == null) {
            throw new BadRequestException("The principal's INN is not specified");
        }
        if (representative.getInn() == null) {
            throw new BadRequestException("The representative's INN is not specified");
        }
        if (representativePassport.getSeriesNumber() == null) {
            throw new BadRequestException("The passport's SeriesNumber is not specified");
        }
        //------------------- save organisation ----------------
        Organisation existsOrganisation;
        try {
            existsOrganisation = this.organisationService.findByOrganisationInn(organisation.getInn());
            if (existsOrganisation == null) {
                Organisation respOrganisation;
                try {
                    respOrganisation = this.organisationService.saveOrganisation(organisation);
                } catch (ConstraintViolationException e) {
                    throw new BadRequestException(e.getMessage());
                }
                assert respOrganisation != null;
                orgId = respOrganisation.getId();
            } else if (!existsOrganisation.getInn().equals(organisation.getInn()) || !existsOrganisation.getKpp().equals(organisation.getKpp())
                    || !existsOrganisation.getOgrn().equals(organisation.getOgrn()) || !existsOrganisation.getName().equals(organisation.getName())
                    || !existsOrganisation.getRegionCode().equals(organisation.getRegionCode())) { //|| !existsOrganisation.getLegalAddress().equals(organisation.getLegalAddress())
                Organisation respOrganisation;
                try {
                    organisation.setId(existsOrganisation.getId());
                    respOrganisation = this.organisationService.saveOrganisation(organisation);
                } catch (ConstraintViolationException e) {
                    throw new BadRequestException(e.getMessage());
                }
                assert respOrganisation != null;
                orgId = respOrganisation.getId();
            } else {
                orgId = existsOrganisation.getId();
            }
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
            respPrincipalDetail = this.principalDetailService.savePrincipalDetail(principalDetail);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(e.getMessage());
        }
        assert respPrincipalDetail != null;
        principDetailId = respPrincipalDetail.getId();
        //------------------- end save principal detail ----------------


        //------------------- save representative and passport ----------------
        Representative existsRepresentative;
        try {
            existsRepresentative = this.representativeService.findRepresentativeByInn(representative.getInn());
            if (existsRepresentative == null) {
                Representative respRepresentative;
                try {
                    respRepresentative = this.representativeService.saveRepresentative(representative);
                } catch (ConstraintViolationException e) {
                    throw new BadRequestException(e.getMessage());
                }
                if (respRepresentative.getId() != null) {
                    representativePassport.setRepresentativeId(respRepresentative.getId());
                    try {
                        this.representativePassportService.saveRepresentativePassport(representativePassport);
                    } catch (ConstraintViolationException e) {
                        throw new BadRequestException(e.getMessage());
                    }
                }
                represId = respRepresentative.getId();
            } else if (!existsRepresentative.getInn().equals(representative.getInn()) || !existsRepresentative.getSnils().equals(representative.getSnils())
                    || !existsRepresentative.getSurname().equals(representative.getSurname()) || !existsRepresentative.getName().equals(representative.getName())
                    || !existsRepresentative.getPatronymic().equals(representative.getPatronymic()) || !existsRepresentative.getBirthDate().equals(representative.getBirthDate())) {
                Representative respRepresentative;
                try {
                    representative.setId(existsRepresentative.getId());
                    respRepresentative = this.representativeService.saveRepresentative(representative);
                } catch (ConstraintViolationException e) {
                    throw new BadRequestException(e.getMessage());
                }
                if (respRepresentative.getId() != null) {
                    RepresentativePassport existsRepresentativePassport;
                    try {
                        existsRepresentativePassport = this.representativePassportService.findRepresentativePassportyByRepresentativeId(respRepresentative.getId());
                        if (existsRepresentativePassport == null) {
                            representativePassport.setRepresentativeId(respRepresentative.getId());
                            try {
                                this.representativePassportService.saveRepresentativePassport(representativePassport);
                            } catch (ConstraintViolationException e) {
                                throw new BadRequestException(e.getMessage());
                            }
                        } else if (!existsRepresentativePassport.getSeriesNumber().equals(representativePassport.getSeriesNumber()) || !existsRepresentativePassport.getDateIssue().equals(representativePassport.getDateIssue())
                                || !existsRepresentativePassport.getWhoIssue().equals(representativePassport.getWhoIssue()) || !existsRepresentativePassport.getDepartmentCode().equals(representativePassport.getDepartmentCode())) {
                            representativePassport.setId(existsRepresentativePassport.getId());
                            representativePassport.setRepresentativeId(respRepresentative.getId());
                            try {
                                this.representativePassportService.saveRepresentativePassport(representativePassport);
                            } catch (ConstraintViolationException e) {
                                throw new BadRequestException(e.getMessage());
                            }
                        }
                    } catch (ConstraintViolationException e) {
                        throw new BadRequestException(e.getMessage());
                    }
                }
                represId = respRepresentative.getId();
            } else {
                represId = existsRepresentative.getId();
            }
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(e.getMessage());
        }
        //------------------- end save representative and passport ----------------

        if (orgId == null || principId == null || principDetailId == null || represId == null) {
            throw new BadRequestException(MRPA_REGISTRY_NOT_FULL);
        }
        m4d.setOrganisationId(orgId);
        m4d.setPrincipalId(principId);
        m4d.setPrincipalDetailId(principDetailId);
        m4d.setRepresentativeId(represId);

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

        return m4dSaved.getId();
    }

    @AuditLogThis(event = AuditLogEventEnum.M4D_UPDATE)
    @Transactional(rollbackFor = {Exception.class}, isolation = Isolation.READ_UNCOMMITTED)
    public Long updateAllDataMrpaRegistry(M4d m4d, Organisation organisation, Principal principal, PrincipalDetail principalDetail, Representative representative, RepresentativePassport representativePassport, List<CredentialDto> credentialList, List<Long> credentials) {
        Long orgId;
        Long principId;
        Long principDetailId;
        Long represId;
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
        Organisation existsOrganisation;
        try {
            existsOrganisation = this.organisationService.findByOrganisationInn(organisation.getInn());
            if (existsOrganisation != null) {
                organisation.setId(existsOrganisation.getId());
            }
            assert existsOrganisation != null;
            if (!existsOrganisation.equals(organisation)) {
                Organisation respOrganisation;
                try {
                    respOrganisation = this.organisationService.saveOrganisation(organisation);
                } catch (ConstraintViolationException e) {
                    throw new BadRequestException(e.getMessage());
                }
                orgId = respOrganisation.getId();
            } else {
                orgId = existsOrganisation.getId();
            }
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(e.getMessage());
        }
        //------------------- end save organisation ----------------

        //------------------- save principal ----------------
        Principal existsPrincipal;
        try {
            existsPrincipal = this.principalService.findByPrincipalId(m4d.getPrincipalId());
            if (existsPrincipal != null) {
                if (!existsPrincipal.getInn().equals(principal.getInn())) {
                    throw new BadRequestException("Переданная ИНН доверителя не соответствует хранящейся в ПЭП. Добавьте МЧД по новой.");
                }
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
                        respPrincipalDetail = this.principalDetailService.savePrincipalDetail(principalDetail);
                    } catch (ConstraintViolationException e) {
                        throw new BadRequestException(e.getMessage());
                    }
                    // set Active=false for old principal detail
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

        //------------------- save representative and passport ----------------

        Representative existsRepresentative;
        try {
            existsRepresentative = this.representativeService.findRepresentativeByInn(representative.getInn());
            if (existsRepresentative != null) {
                representative.setId(existsRepresentative.getId());
            }
            Representative respRepresentative;
            try {
                respRepresentative = this.representativeService.saveRepresentative(representative);
            } catch (ConstraintViolationException e) {
                throw new BadRequestException(e.getMessage());
            }
            RepresentativePassport existsRepresentativePassport = this.representativePassportService.findRepresentativePassportyByRepresentativeId(respRepresentative.getId());
            if (existsRepresentativePassport != null) {
                representativePassport.setId(existsRepresentativePassport.getId());
            }
            try {
                representativePassport.setRepresentativeId(respRepresentative.getId());
                this.representativePassportService.saveRepresentativePassport(representativePassport);
            } catch (ConstraintViolationException e) {
                throw new BadRequestException(e.getMessage());
            }
            represId = respRepresentative.getId();
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(e.getMessage());
        }
        //------------------- end save representative and passport ----------------

        if (orgId == null || principId == null || principDetailId == null || represId == null) {
            throw new BadRequestException(MRPA_REGISTRY_NOT_FULL);
        }
        if (!m4d.getOrganisationId().equals(orgId)) {
            m4d.setOrganisationId(orgId);
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
    @AuditLogThis(event = AuditLogEventEnum.M4D_DELETE, deleteByIdRepository = M4dRepository.class)
    public void deleteMrpaRegistry(@NotNull Long id) {
        this.m4dRepository.deleteById(id);
    }

    @Override
    public List<M4dResponseDto.CredentialDto> getCredentialByMrpaRegistryId(Long mrpaId) {
        List<Credential> credentials = this.credentialService.findCredentialByMrpaId(mrpaId);
        List<M4dResponseDto.CredentialDto> credentialDTOList = new ArrayList<>();
        assert credentials != null;
        for (Credential c : credentials) {
            M4dResponseDto.CredentialDto credentialDTO = new M4dResponseDto.CredentialDto();
            credentialDTO.setId(c.getId());
            credentialDTO.setName(c.getName());
            credentialDTO.setCode(c.getCode());
            credentialDTOList.add(credentialDTO);
        }
        return credentialDTOList;
    }

    @Override
    public M4dFullXML ConvertMrpaRegistryFullDtoToFullXml(M4dResponseDto m4dFullDTO) {
        String idFile = "ON_EMCHD_" + m4dFullDTO.getCreateDate().substring(0, 10).replace("-", "") + "_" + m4dFullDTO.getGuid();
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

        if (Objects.isNull(m4dFullDTO.getInternalNumber())) {
            mrpas.setInternalNumber("б/н");
        } else mrpas.setInternalNumber(m4dFullDTO.getInternalNumber());

        mrpas.setGuid(m4dFullDTO.getGuid());

        if (m4dFullDTO.getStartDate() != null) {
            mrpas.setStartDate(m4dFullDTO.getStartDate().substring(0, 10));
        }
        if (m4dFullDTO.getEndDate() != null) {
            mrpas.setEndDate(m4dFullDTO.getEndDate().substring(0, 10));
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
        organisation.setName(m4dFullDTO.organization.getName());
        organisation.setInn(m4dFullDTO.organization.getInn());
        organisation.setKpp(m4dFullDTO.organization.getKpp());
        organisation.setOgrn(m4dFullDTO.organization.getOgrn());
        legalEntity.setOrganisation(organisation);
        M4dFullXML.Document.PowerAttorneys.InfoPrincipals.InfoPrincipal.LegalEntity.Organisation.AddressRegion addressRegion = new M4dFullXML.Document.PowerAttorneys.InfoPrincipals.InfoPrincipal.LegalEntity.Organisation.AddressRegion();
        addressRegion.setRegionCode(m4dFullDTO.organization.getRegionCode());
        addressRegion.setLegalAddress(m4dFullDTO.organization.getLegalAddress());
        organisation.setAddressRegion(addressRegion);
        M4dFullXML.Document.PowerAttorneys.InfoPrincipals.InfoPrincipal.LegalEntity.PersonPrincipal personPrincipal = new M4dFullXML.Document.PowerAttorneys.InfoPrincipals.InfoPrincipal.LegalEntity.PersonPrincipal();
        personPrincipal.setTypePrincipal("1");
        legalEntity.setPrincipal(personPrincipal);
        M4dFullXML.Document.PowerAttorneys.InfoPrincipals.InfoPrincipal.LegalEntity.PersonPrincipal.Principal principal = new M4dFullXML.Document.PowerAttorneys.InfoPrincipals.InfoPrincipal.LegalEntity.PersonPrincipal.Principal();
        principal.setInn(m4dFullDTO.principal.getInn());
        principal.setSnils(m4dFullDTO.principal.getSnils());
        principal.setJobTitle(m4dFullDTO.principalDetail.getJobTitle());
        personPrincipal.setPrincipal(principal);
        M4dFullXML.Document.PowerAttorneys.InfoPrincipals.InfoPrincipal.LegalEntity.PersonPrincipal.Principal.FioPrincipal fioPrincipal = new M4dFullXML.Document.PowerAttorneys.InfoPrincipals.InfoPrincipal.LegalEntity.PersonPrincipal.Principal.FioPrincipal();
        principal.setFioPrincipal(fioPrincipal);
        M4dFullXML.Document.PowerAttorneys.InfoPrincipals.InfoPrincipal.LegalEntity.PersonPrincipal.Principal.FioPrincipal.Fio fio = new M4dFullXML.Document.PowerAttorneys.InfoPrincipals.InfoPrincipal.LegalEntity.PersonPrincipal.Principal.FioPrincipal.Fio();
        fio.setName(m4dFullDTO.principalDetail.getName());
        fio.setPatronymic(m4dFullDTO.principalDetail.getPatronymic());
        fio.setSurname(m4dFullDTO.principalDetail.getSurname());
        fioPrincipal.setFio(fio);
        M4dFullXML.Document.PowerAttorneys.InfoRepresentative infoRepresentative = new M4dFullXML.Document.PowerAttorneys.InfoRepresentative();
        infoRepresentative.setTypeRepresentative("3");
        powerAttorneys.setInfoRepresentative(infoRepresentative);
        M4dFullXML.Document.PowerAttorneys.InfoRepresentative.Representative representative = new M4dFullXML.Document.PowerAttorneys.InfoRepresentative.Representative();
        infoRepresentative.setRepresentative(representative);
        M4dFullXML.Document.PowerAttorneys.InfoRepresentative.Representative.DetailRepresentative detailRepresentative = new M4dFullXML.Document.PowerAttorneys.InfoRepresentative.Representative.DetailRepresentative();
        representative.setDetailRepresentative(detailRepresentative);
        detailRepresentative.setInn(m4dFullDTO.representative.getInn());
        detailRepresentative.setSnils(m4dFullDTO.representative.getSnils());
        M4dFullXML.Document.PowerAttorneys.InfoRepresentative.Representative.DetailRepresentative.InfoIndividual infoIndividual = new M4dFullXML.Document.PowerAttorneys.InfoRepresentative.Representative.DetailRepresentative.InfoIndividual();
        detailRepresentative.setInfoIndividual(infoIndividual);
        if (m4dFullDTO.representative.getBirthDate() != null) {
            infoIndividual.setBirthDate(m4dFullDTO.representative.getBirthDate().substring(0, 10));
        }
        M4dFullXML.Document.PowerAttorneys.InfoRepresentative.Representative.DetailRepresentative.InfoIndividual.FioRepresentative fioRepresentative = new M4dFullXML.Document.PowerAttorneys.InfoRepresentative.Representative.DetailRepresentative.InfoIndividual.FioRepresentative();
        infoIndividual.setFioRepresentative(fioRepresentative);
        fioRepresentative.setName(m4dFullDTO.representative.getName());
        fioRepresentative.setSurname(m4dFullDTO.representative.getSurname());
        fioRepresentative.setPatronymic(m4dFullDTO.representative.getPatronymic());
        M4dFullXML.Document.PowerAttorneys.InfoRepresentative.Representative.DetailRepresentative.InfoIndividual.RepresentativePassport representativePassport = new M4dFullXML.Document.PowerAttorneys.InfoRepresentative.Representative.DetailRepresentative.InfoIndividual.RepresentativePassport();
        infoIndividual.setRepresentativePassport(representativePassport);
        representativePassport.setTypeDocument("21");
        representativePassport.setSeriesNumber(m4dFullDTO.representative.representativePassport.getSeriesNumber());
        if (m4dFullDTO.representative.representativePassport.getDateIssue() != null) {
            representativePassport.setDateIssue(m4dFullDTO.representative.representativePassport.getDateIssue().substring(0, 10));
        }
        representativePassport.setWhoIssue(m4dFullDTO.representative.representativePassport.getWhoIssue());
        representativePassport.setDepartmentCode(m4dFullDTO.representative.representativePassport.getDepartmentCode());
        M4dFullXML.Document.PowerAttorneys.Credentials credentials = new M4dFullXML.Document.PowerAttorneys.Credentials();
        credentials.setTypeCredential("1");
        credentials.setImplCredential("1");
        credentials.setLossCredential("1");
        ArrayList<M4dFullXML.Document.PowerAttorneys.Credentials.Credential> credentialList = new ArrayList<>();
        for (M4dResponseDto.CredentialDto credentialDTO : m4dFullDTO.credentials) {
            M4dFullXML.Document.PowerAttorneys.Credentials.Credential credential = new M4dFullXML.Document.PowerAttorneys.Credentials.Credential();
            credential.setCode(credentialDTO.getCode());
            credential.setName(credentialDTO.getName());
            credentialList.add(credential);
        }
        credentials.setCredentials(credentialList);
        powerAttorneys.setCredential(credentials);

        return m4dFullXML;
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
    public List<M4dResponseDto> findAllM4dUsers(Authentication authentication) {
        Set<String> setINNs = userCertificateService.getUserCertificateINNs(authentication);

        // TODO: когда будут связи переделать запрос в части маппинга
        return m4dRepository.findListM4dBySetInn(setINNs).stream()
                .peek(m4d -> {
                    try {
                        Long id = m4d.getId();
                        m4d.setStatus(getStatusM4d(id));
                        m4d.setCredentials(getCredentialByMrpaRegistryId(id));
                    } catch (ParseException e) {
                        throw new M4dException(e.getMessage());
                    }
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<M4d> findM4dByRepresentativeInn(String inn) {
        return this.m4dRepository.findM4dByRepresentativeInn(inn);
    }

    @Transactional(readOnly = true)
    @Override
    public M4dResponseDto findM4dByIdAndRepresentativeInn(String inn, Long id) {
        return m4dRepository.findValidM4dByIdAndRepresentativeInn(inn, id)
                .map(m4d -> {
                    try {
                        M4dResponseDto m4dResponseDto = convertM4dToM4dResponseDto(m4d, true, false);
                        m4dResponseDto.credentials = getCredentialByMrpaRegistryId(m4dResponseDto.getId());
                        m4dResponseDto.setStatus(getStatusM4d(m4dResponseDto.getId()));
                        return m4dResponseDto;
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
        if (currentDT.before(startDate) && currentDT.after(endDate)) {
            return "EXPIRED";
        }
        if (mrpaRegistry.getMrpaRecall() != null) {
            M4dRecall mrpaRecall = this.m4dRecallService.findById(mrpaRegistry.getMrpaRecall().getId());
            if (mrpaRecall.getM4dFile() != null) {
                return "WITHDRAWN";
            } else {
                return "WITHDRAWAL";
            }
        } else {
            if (mrpaRegistry.getM4dFile() != null) {
                return "VALID";
            } else {
                return "NOT_SIGNED";
            }
        }
    }

    public M4dResponseDto convertM4dToM4dResponseDto(M4d m4d, Boolean withM4dSigFile, Boolean withRecallSigFile) throws ParseException {
        M4dResponseDto m4dResponseDto = modelMapper.map(m4d, M4dResponseDto.class);
        M4dFile m4dFile;
        try {
            m4dFile = this.m4dFileService.findM4dFileById(m4d.getM4dFile().getId());
            if (m4dFile != null) {
                M4dResponseDto.SigFileDto sigFileDTO = convertSigFileToDto(m4dFile);
                if (!withM4dSigFile) {
                    sigFileDTO.setContent(null);
                }
                m4dResponseDto.setSigFile(sigFileDTO);
            }
        } catch (NullPointerException e) {
            //
        }
        M4dFile m4dFileRecall;
        try {
            m4dFileRecall = this.m4dFileService.findM4dFileById(m4d.getMrpaRecall().getM4dFile().getId());
            if (m4dFileRecall != null) {
                M4dResponseDto.RecallDto.SigFileDto sigFileDTO = convertSigFileRecallToDto(m4dFileRecall);
                if (!withRecallSigFile) {
                    sigFileDTO.setContent(null);
                }
                m4dResponseDto.recall.setSigFile(sigFileDTO);
            }
        } catch (NullPointerException e) {
            //
        }
        Organisation organisation = this.organisationService.findByOrganisationId(m4d.getOrganisationId());
        if (organisation != null) {
            m4dResponseDto.setOrganization(convertOrganisationToDto(organisation));
        }
        Principal principal = this.principalService.findByPrincipalId(m4d.getPrincipalId());
        if (principal != null) {
            m4dResponseDto.setPrincipal(convertPrincipalToDto(principal));
        }
        PrincipalDetail principalDetail = this.principalDetailService.findByPrincipalDetailId(m4d.getPrincipalDetailId());
        if (principalDetail != null) {
            m4dResponseDto.setPrincipalDetail(convertPrincipalDetailToDto(principalDetail));
        }
        Representative representative = this.representativeService.findByRepresentativeId(m4d.getRepresentativeId());
        if (representative != null) {
            m4dResponseDto.setRepresentative(convertRepresentativeToDto(representative));
            RepresentativePassport representativePassport = this.representativePassportService.findRepresentativePassportyByRepresentativeId(representative.getId());
            m4dResponseDto.representative.setRepresentativePassport(convertRepresentativePassportToDto(representativePassport));
        }
        m4dResponseDto.setCredentials(this.getCredentialByMrpaRegistryId(m4d.getId()));
        m4dResponseDto.setStatus(this.getStatusM4d(m4dResponseDto.getId()));
        return m4dResponseDto;
    }

    public M4dVerifyStatusDto verify(MultipartFile sourceFile) throws Exception {

        String extension = FilenameUtils.getExtension(sourceFile.getOriginalFilename());
        if (!Objects.equals(extension, "xml")) {
            throw new BadRequestException("Ошибка: Формат файла не XML");
        }

        List<SAXParseException> res = new XmlValidator("/app/xml/validation/ON_EMCHD_1_928_00_01_01_01.xsd", sourceFile.getInputStream()).listParsingExceptions();
        String content;
        if (res.isEmpty()) {
            try (InputStream is = sourceFile.getInputStream()) {
                byte[] bom = new byte[3];
                int n = is.read(bom, 0, bom.length);
                Reader reader;
                if (n == 3 && (bom[0] & 0xFF) == 0xEF && (bom[1] & 0xFF) == 0xBB && (bom[2] & 0xFF) == 0xBF) {
                    reader = new InputStreamReader(is, StandardCharsets.UTF_8);
                } else {
                    reader = new InputStreamReader(sourceFile.getInputStream(), StandardCharsets.UTF_8);
                }
                StringBuilder cont = new StringBuilder();
                char[] buffer = new char[1024];
                int numRead;
                while ((numRead = reader.read(buffer)) != -1) {
                    cont.append(buffer, 0, numRead);
                }
                content = cont.toString();
            }

            M4dXml m4dXml;
            try {
                Document document = convertStringToDocument(content);
                assert document != null;
                m4dXml = XMLUtil.unmarshal(document, M4dXml.class);
            } catch (Exception e) {
                throw new BadRequestException("Ошибка: " + MRPA_REGISTRY_WRONG_FORMAT_XML);
            }

            if (m4dXml == null) {
                throw new BadRequestException(MRPA_IS_NULL);
            }
            if (m4dXml.document.powerAttorneys.getMrpa().getGuid() == null) {
                throw new BadRequestException("Ошибка: " + MRPA_REGISTRY_WRONG_FORMAT_XML);
            }
            String guidXml;
            guidXml = m4dXml.document.powerAttorneys.getMrpa().getGuid();

            M4d m4d;
            try {
                m4d = this.m4dRepository.findByGuid(guidXml);
            } catch (ConstraintViolationException e) {
                throw new BadRequestException(e.getMessage());
            }
            if (m4d != null) {
                M4dResponseDto m4dResponseDto = this.convertM4dToM4dResponseDto(m4d, false, false);
                M4dVerifyStatusDto m4dVerifyStatusDto = new M4dVerifyStatusDto();
                m4dVerifyStatusDto.setGuid(guidXml);
                m4dVerifyStatusDto.setStatus(m4dResponseDto.getStatus());
                m4dVerifyStatusDto.setVerifyDate(LocalDateTime.now());
                m4dVerifyStatusDto.setSource("Источник ПЭП");
                m4dVerifyStatusDto.setM4d(m4dResponseDto);

                return m4dVerifyStatusDto;
            } else {
                M4dResponseDto m4dResponseDto = this.parseM4dXml(m4dXml);
                String status = "";
                String source = "Источник РР МЧД ФНС";
                AuthResponse authResponse = null;
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setAccept(Collections.singletonList(MediaType.ALL));
                String username = "demo-client";
                String password = "secret";
                String auth = username + ":" + password;
                byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
                String authHeader = "Basic " + new String(encodedAuth);
                headers.set("Authorization", authHeader);
                String requestBody = "{ \"username\": \"operator\", \"password\": \"operator\", \"grant_type\": \"password\" }";
                HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);


                try {
                    ResponseEntity<AuthResponse> response = restTemplate.exchange(
                            API_RR_FNS_URI + "/vst-oauth2/oauth/token",
                            HttpMethod.POST,
                            requestEntity,
                            AuthResponse.class
                    );
                    authResponse = response.getBody();
                } catch (HttpClientErrorException e) {
                    if (e.getStatusCode().value() == 404) {
                        log.error("Ресурс не найден (404): " + e.getResponseBodyAsString());
                        status = "NOT_FOUND";
                    } else {
                        log.error("Ошибка: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
                        status = "CHECK_FALED";
                    }
                } catch (ResourceAccessException e) {
                    log.error("Ошибка доступа к ресурсу: " + e.getMessage());
                    if (e.getCause() instanceof UnknownHostException) {
                        System.out.println("Неизвестный хост: " + e.getCause().getMessage());
                        //если GUID из списка - присваиваем какой то статус
                        if (guidXml.equals("38f8ee5a-164c-4158-b71c-56004f6af738")) {
                            status = "VALID";
                        } else {
                            status = "CHECK_FALED"; //"Сервис РР МЧД ФНС недоступен";
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Произошла ошибка: " + e.getMessage());
                    status = "CHECK_FALED";
                }

                if (authResponse == null) {
                    if (status.isEmpty()){
                        System.out.println("Ошибка авторизации");
                        status = "CHECK_FALED";
                    }
                } else {
                    M4dFnsDataResponse m4dFnsDataResponse = null;
                    HttpHeaders headers1 = new HttpHeaders();
                    headers1.setAccept(Collections.singletonList(MediaType.ALL));
                    headers1.set("Authorization", "Bearer " + authResponse.getAccessToken());
                    HttpEntity<String> requestEntity1 = new HttpEntity<>(headers1);

                    try {
                        ResponseEntity<M4dFnsDataResponse> response1 = restTemplate.exchange(
                                API_RR_FNS_URI + "/poar-webapp/integration/poa/" + guidXml + "/public",
                                HttpMethod.GET,
                                requestEntity1,
                                M4dFnsDataResponse.class
                        );
                        m4dFnsDataResponse = response1.getBody();
                    } catch (HttpClientErrorException e) {
                        if (e.getStatusCode().value() == 404) {
                            System.out.println("Ресурс не найден (404): " + e.getResponseBodyAsString());
                            System.out.println("МЧД с GUID=" + guidXml + " не найдена ни в ПЭП ни на РР МЧД ФНС");
                            status = "NOT_FOUND";
                        } else {
                            System.out.println("Ошибка: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
                            status = "CHECK_FALED";
                        }
                    } catch (ResourceAccessException e) {
                        System.out.println("Ошибка доступа к ресурсу: " + e.getMessage());
                        if (e.getCause() instanceof UnknownHostException) {
                            System.out.println("Неизвестный хост: " + e.getCause().getMessage());
                            System.out.println("Сервис РР МЧД ФНС недоступен");
                            status = "CHECK_FALED";
                        }
                    } catch (Exception e) {
                        System.out.println("Произошла ошибка: " + e.getMessage());
                        status = "CHECK_FALED";
                    }
                    if (m4dFnsDataResponse == null) {
                        if (status.isEmpty()) {
                            System.out.println("Ошибка получения данных по МЧД с сайта РР МЧД ФНС");
                            status = "CHECK_FALED";
                        }
                    } else {
                        String hashM4dFns = m4dFnsDataResponse.getId();
                        String hashM4dFile = HashUtils.getHash(sourceFile.getBytes());
                        if (!hashM4dFns.equals(hashM4dFile)){
                            System.out.println("xml-файлы МЧД не совпадают");
                            status = "CHECK_FALED";
                        } else {
                            switch (m4dFnsDataResponse.getStatus()) {
                                case "ACTIVE": status = "VALID"; break;
                                case "CREATED": status = "NOT_SIGNED"; break;
                                case "REVOKED": status = "WITHDRAWN"; break;
                                case "EXPIRED": status = "EXPIRED"; break;
                                default: break;
                            }
                        }
                    }
                }
                M4dVerifyStatus m4dVerifyStatusSaved;
                M4dVerifyStatus existsM4dVerifyStatus = this.m4dVerifyStatusRepository.findM4dVerifyStatusByGuidIgnoreCase(guidXml);
                if (existsM4dVerifyStatus == null) {
                    M4dVerifyStatus m4dVerifyStatus = new M4dVerifyStatus();
                    m4dVerifyStatus.setGuid(guidXml);
                    m4dVerifyStatus.setStatus(status);
                    m4dVerifyStatus.setVerifyDate(LocalDateTime.now());
                    try {
                        m4dVerifyStatusSaved = this.m4dVerifyStatusRepository.save(m4dVerifyStatus);
                    } catch (ConstraintViolationException e) {
                        throw new BadRequestException(e.getMessage());
                    }
                } else {
                    existsM4dVerifyStatus.setVerifyDate(LocalDateTime.now());
                    existsM4dVerifyStatus.setStatus(status);
                    try {
                        m4dVerifyStatusSaved = this.m4dVerifyStatusRepository.save(existsM4dVerifyStatus);
                    } catch (ConstraintViolationException e) {
                        throw new BadRequestException(e.getMessage());
                    }
                }
                M4dVerifyStatusDto m4dVerifyStatusDto = modelMapper.map(m4dVerifyStatusSaved, M4dVerifyStatusDto.class);
                m4dVerifyStatusDto.setSource(source);
                m4dVerifyStatusDto.setM4d(m4dResponseDto);
                return m4dVerifyStatusDto;
            }
        }
        Map<String, String> body = new HashMap<>();
        List<String> errs = new ArrayList<>();
        res.forEach(e -> errs.add(String.format("Line number: %s, Column number: %s. %s", e.getLineNumber(), e.getColumnNumber(), e.getMessage())));
        body.put("details", String.join(System.lineSeparator(), errs));

        throw new BadRequestException(body.toString());
    }

    @Override
    public M4dVerifyStatus getStatusVerifyInFns(@NotNull String guid) {
        return this.m4dVerifyStatusRepository.findM4dVerifyStatusByGuidIgnoreCase(guid);
    }

    public M4dResponseDto parseM4dXml(@NotNull M4dXml m4dXml) throws ParseException {
        M4d m4d = new M4d();
        m4d.setGuid(m4dXml.document.powerAttorneys.mrpa.getGuid());
        m4d.setInternalNumber(m4dXml.document.powerAttorneys.mrpa.getInternalNumber());
        m4d.setCreateDate(LocalDateTime.now());
        m4d.setStartDate(this.convertStringToLocalDateTime(m4dXml.document.powerAttorneys.mrpa.getStartDate()));
        m4d.setEndDate(this.convertStringToLocalDateTime(m4dXml.document.powerAttorneys.mrpa.getEndDate()));

        Organisation organisation = new Organisation();
        organisation.setInn(m4dXml.document.powerAttorneys.principals.principal.legalEntity.organisation.getInn());
        organisation.setKpp(m4dXml.document.powerAttorneys.principals.principal.legalEntity.organisation.getKpp());
        organisation.setOgrn(m4dXml.document.powerAttorneys.principals.principal.legalEntity.organisation.getOgrn());
        organisation.setName(m4dXml.document.powerAttorneys.principals.principal.legalEntity.organisation.getName());
        organisation.setRegionCode(m4dXml.document.powerAttorneys.principals.principal.legalEntity.organisation.addressRegion.getRegionCode());
        organisation.setLegalAddress(m4dXml.document.powerAttorneys.principals.principal.legalEntity.organisation.addressRegion.getLegalAddress());

        Principal principal = new Principal();
        PrincipalDetail principalDetail = new PrincipalDetail();
        principal.setInn(m4dXml.document.powerAttorneys.principals.principal.legalEntity.principal.principal.getInn());
        principal.setSnils(m4dXml.document.powerAttorneys.principals.principal.legalEntity.principal.principal.getSnils());
        principalDetail.setJobTitle(m4dXml.document.powerAttorneys.principals.principal.legalEntity.principal.principal.getJobTitle());
        principalDetail.setName(m4dXml.document.powerAttorneys.principals.principal.legalEntity.principal.principal.fioPrincipal.fio.getName());
        principalDetail.setSurname(m4dXml.document.powerAttorneys.principals.principal.legalEntity.principal.principal.fioPrincipal.fio.getSurname());
        principalDetail.setPatronymic(m4dXml.document.powerAttorneys.principals.principal.legalEntity.principal.principal.fioPrincipal.fio.getPatronymic());

        Representative representative = new Representative();
        RepresentativePassport represPassport = new RepresentativePassport();
        for (M4dXml.Document.PowerAttorneys.InfoRepresentative c: m4dXml.document.powerAttorneys.infoRepresentative) {
            if (Objects.equals(c.getTypeRepresentative(), "3")) {
                if (c.representative.detailRepresentative != null) {
                    representative.setInn(c.representative.detailRepresentative.getInn());
                    representative.setSnils(c.representative.detailRepresentative.getSnils());
                    representative.setBirthDate(this.convertStringToLocalDateTime(DateUtils.parse(c.representative.detailRepresentative.infoIndividual.getBirthDate()).toInstant().toString()));
                    representative.setName(c.representative.detailRepresentative.infoIndividual.fioRepresentative.getName());
                    representative.setSurname(c.representative.detailRepresentative.infoIndividual.fioRepresentative.getSurname());
                    representative.setPatronymic(c.representative.detailRepresentative.infoIndividual.fioRepresentative.getPatronymic());

                    represPassport.setSeriesNumber(c.representative.detailRepresentative.infoIndividual.representativePassport.getSeriesNumber());
                    represPassport.setDateIssue(this.convertStringToLocalDateTime(DateUtils.parse(c.representative.detailRepresentative.infoIndividual.representativePassport.getDateIssue()).toInstant().toString()));
                    represPassport.setWhoIssue(c.representative.detailRepresentative.infoIndividual.representativePassport.getWhoIssue());
                    represPassport.setDepartmentCode(c.representative.detailRepresentative.infoIndividual.representativePassport.getDepartmentCode());
                }
            }
        }
        List<M4dResponseDto.CredentialDto> credentialList = new ArrayList<>();
        if (m4dXml.document.powerAttorneys.credential.credentials != null) {
            for (M4dXml.Document.PowerAttorneys.Credentials.Credential c : m4dXml.document.powerAttorneys.credential.credentials) {
                M4dResponseDto.CredentialDto newCredential = new M4dResponseDto.CredentialDto();
                newCredential.setCode(c.getCode());
                newCredential.setName(c.getName());
                credentialList.add(newCredential);
            }
        }
        M4dResponseDto m4dResponseDto = modelMapper.map(m4d, M4dResponseDto.class);
        M4dResponseDto.OrganizationDto organizationDTO = modelMapper.map(organisation, M4dResponseDto.OrganizationDto.class);
        M4dResponseDto.PrincipalDto principalDTO = modelMapper.map(principal, M4dResponseDto.PrincipalDto.class);
        M4dResponseDto.PrincipalDetailDto principalDetailDto = modelMapper.map(principalDetail, M4dResponseDto.PrincipalDetailDto.class);
        M4dResponseDto.RepresentativeDto representativeDTO = modelMapper.map(representative, M4dResponseDto.RepresentativeDto.class);
        M4dResponseDto.RepresentativePassportDto representativePassportDTO = modelMapper.map(represPassport, M4dResponseDto.RepresentativePassportDto.class);
        m4dResponseDto.setOrganization(organizationDTO);
        m4dResponseDto.setPrincipal(principalDTO);
        m4dResponseDto.setPrincipalDetail(principalDetailDto);
        m4dResponseDto.setRepresentative(representativeDTO);
        m4dResponseDto.representative.setRepresentativePassport(representativePassportDTO);
        m4dResponseDto.setCredentials(credentialList);
        return m4dResponseDto;
    }

    public ByteArrayResource createContainer(Authentication authentication,MultipartFile sourceFile, MultipartFile sourceSig, HashAlgorithm hashAlgorithm) throws IOException {
        String bearerToken = getTokenValue(authentication);
        String tempFileSource;
        String tempFileSig;
        FileOutputStream foSource;
        FileOutputStream foSig;
        ByteArrayResource res;

        tempFileSource = "/tmp/" + sourceFile.getOriginalFilename();
        foSource = new FileOutputStream(tempFileSource);
        foSource.write(sourceFile.getBytes());
        foSource.close();

        tempFileSig = "/tmp/" + sourceSig.getOriginalFilename();
        foSig = new FileOutputStream(tempFileSig);
        foSig.write(sourceSig.getBytes());
        foSig.close();

        res = synapiClientService.createContainer(tempFileSource, tempFileSig, hashAlgorithm, bearerToken);
        File f1 = new File(tempFileSource);
        f1.delete();
        File f2 = new File(tempFileSig);
        f2.delete();
        return res;
    }

    public Long uploadMrpaRegistry(MultipartFile sourceFile) throws IOException, SAXException {
        List<SAXParseException> res = new XmlValidator("/app/xml/validation/ON_EMCHD_1_928_00_01_01_01.xsd", sourceFile.getInputStream()).listParsingExceptions();
        String content;
        if (res.isEmpty()) {
            try (InputStream is = sourceFile.getInputStream()) {
                byte[] bom = new byte[3];
                int n = is.read(bom, 0, bom.length);
                Reader reader;
                if (n == 3 && (bom[0] & 0xFF) == 0xEF && (bom[1] & 0xFF) == 0xBB && (bom[2] & 0xFF) == 0xBF) {
                    reader = new InputStreamReader(is, StandardCharsets.UTF_8);
                } else {
                    reader = new InputStreamReader(sourceFile.getInputStream(), StandardCharsets.UTF_8);
                }
                StringBuilder cont = new StringBuilder();
                char[] buffer = new char[1024];
                int numRead;
                while ((numRead = reader.read(buffer)) != -1) {
                    cont.append(buffer, 0, numRead);
                }
                content = cont.toString();
            }

            M4dXml m4dXml;
            try {
                Document document = convertStringToDocument(content);
                assert document != null;
                m4dXml = XMLUtil.unmarshal(document, M4dXml.class);
            } catch (Exception e) {
                throw new BadRequestException(MRPA_REGISTRY_WRONG_FORMAT_XML);
            }

            if (m4dXml == null) {
                throw new BadRequestException(MRPA_IS_NULL);
            }

            int guid;
            guid = this.countM4dByGuid(m4dXml.document.powerAttorneys.getMrpa().getGuid());
            if (guid > 0) {
                throw new BadRequestException(MRPA_REGISTRY_IS_EXISTS);
            }

            M4d m4d = new M4d();
            //парсинг МЧД из xml
            m4d.setGuid(m4dXml.document.powerAttorneys.mrpa.getGuid());
            m4d.setInternalNumber(m4dXml.document.powerAttorneys.mrpa.getInternalNumber());
            String dateCreate = m4dXml.getIdFile().substring(9, 17);
            m4d.setCreateDate(convertStringToLocalDateTime(dateCreate));
            m4d.setStartDate(this.convertStringToLocalDateTime(m4dXml.document.powerAttorneys.mrpa.getStartDate()));
            m4d.setEndDate(this.convertStringToLocalDateTime(m4dXml.document.powerAttorneys.mrpa.getEndDate()));

            Organisation organisation = new Organisation();
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
            RepresentativePassport represPassport = new RepresentativePassport();
            for (M4dXml.Document.PowerAttorneys.InfoRepresentative c: m4dXml.document.powerAttorneys.infoRepresentative) {
                if (Objects.equals(c.getTypeRepresentative(), "3")) {
                    if (c.representative.detailRepresentative != null) {
                        representative.setInn(c.representative.detailRepresentative.getInn());
                        representative.setSnils(c.representative.detailRepresentative.getSnils());
                        representative.setBirthDate(this.convertStringToLocalDateTime(c.representative.detailRepresentative.infoIndividual.getBirthDate()));
                        representative.setName(c.representative.detailRepresentative.infoIndividual.fioRepresentative.getName());
                        representative.setSurname(c.representative.detailRepresentative.infoIndividual.fioRepresentative.getSurname());
                        representative.setPatronymic(c.representative.detailRepresentative.infoIndividual.fioRepresentative.getPatronymic());
                        if (representative.getInn() == null || representative.getInn().isEmpty()) {
                            throw new BadRequestException(REPRESENTATIVE_IS_NULL);
                        }
                        represPassport.setSeriesNumber(c.representative.detailRepresentative.infoIndividual.representativePassport.getSeriesNumber());
                        represPassport.setDateIssue(this.convertStringToLocalDateTime(c.representative.detailRepresentative.infoIndividual.representativePassport.getDateIssue()));
                        represPassport.setWhoIssue(c.representative.detailRepresentative.infoIndividual.representativePassport.getWhoIssue());
                        represPassport.setDepartmentCode(c.representative.detailRepresentative.infoIndividual.representativePassport.getDepartmentCode());
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
            Long m4dSaved;
            try {
                m4dSaved = this.saveAllDataMrpaRegistry(m4d, organisation, principal, principalDetail, representative, represPassport, credentialList, null);
            } catch (ConstraintViolationException e) {
                throw new BadRequestException(e.getMessage());
            }

            return m4dSaved;
        }
        Map<String, String> body = new HashMap<>();
        List<String> errs = new ArrayList<>();
        res.forEach(e -> errs.add(String.format("Line number: %s, Column number: %s. %s", e.getLineNumber(), e.getColumnNumber(), e.getMessage())));
        body.put("details", String.join(System.lineSeparator(), errs));
        throw new BadRequestException(body.toString());
    }

    public Long uploadSig(Authentication authentication, Long mrpaId, MultipartFile container, boolean verify) throws IOException, ParseException, JAXBException {
        String bearerToken = getTokenValue(authentication);
        M4d m4d;
        try {
            m4d = this.findById(mrpaId);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(e.getMessage());
        }
        if (m4d == null) {
            throw new BadRequestException(MRPA_REGISTRY_IS_NULL);
        }
        //----------------- XML M4D ----------------------
        M4dResponseDto m4dResponseDto = this.convertM4dToM4dResponseDto(m4d, false, false);
        M4dFullXML mrpaRegistryFullXML = this.ConvertMrpaRegistryFullDtoToFullXml(m4dResponseDto);
        String xmlString = XMLUtil.marshall(mrpaRegistryFullXML, M4dFullXML.class);
        //-------------------------------------------------
        List<String> tempFileNames = new ArrayList<>();
        String tempFileName;
        FileOutputStream fo;
        ReportDto reportDto;
        ByteArrayResource res = null;
        if (verify) {
            try {
                tempFileName = "/tmp/" + container.getOriginalFilename();

                tempFileNames.add(tempFileName);
                fo = new FileOutputStream(tempFileName);
                fo.write(container.getBytes());
                fo.close();

                reportDto = this.synapiClientService.getReportContainer(tempFileName, true, bearerToken);

                assert reportDto != null;
                if (reportDto.getResults() != null && !reportDto.getResults().isEmpty()) {
                    reportDto.getResults().stream()
                            .flatMap(result -> result.getVerificationResults().stream())
                            .findFirst()
                            .ifPresent(verificationResult -> {
                                boolean isResultTrue = verificationResult.isResult();
                                System.out.println("Result value: " + isResultTrue);
                                if (!isResultTrue) {
                                    throw new BadRequestException(MRPA_REGISTRY_NOT_VERIFY_REPORT);
                                }
                            });
                } else {
                    log.error(reportDto.getError() + ": {}", reportDto.getErrorDescription());
                    throw new BadRequestException(reportDto.getError());
                }

                res = this.synapiClientService.getPartContainer(tempFileName, "sourceFile", bearerToken);
            } catch (IOException e) {
                e.printStackTrace();
            }

            for (String fileName : tempFileNames) {
                File f = new File(fileName);
                f.delete();
            }

            assert res != null;
            byte[] resource = res.getByteArray();
            String normalizedResource = new String(resource, StandardCharsets.UTF_8).replaceAll("\\s+", "");
            String normalizedXmlString = xmlString.replaceAll("\\s+", "");
            if (!StringUtils.difference(normalizedResource, normalizedXmlString).isEmpty()) {
                throw new BadRequestException(MRPA_REGISTRY_NOT_VERIFY_CONTENT);
            }
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
        m4d.setM4dFile(Objects.requireNonNullElse(existsM4dFile, m4dFile));

        M4d m4dSaved;
        try {
            m4dSaved = this.saveMrpaRegistry(m4d);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(e.getMessage());
        }
        return m4dSaved.getId();
    }

    public ByteArrayResource createArchiveM4dSig(Authentication authentication, Long fileId, String filename) throws IOException {
        String bearerToken = getTokenValue(authentication);
        M4dFile m4dFile;
        try {
            m4dFile = this.m4dFileService.findM4dFileById(fileId);
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }
        List<String> tempFileNames = new ArrayList<>();
        String tempContainerFile;
        String tempSourceFile;
        String tempSigFile;
        String tempZipFile = "";
        FileOutputStream fo;
        ByteArrayResource resSource;
        ByteArrayResource resSig;
        try {
            tempContainerFile = "/tmp/" + m4dFile.getFilename();
            tempFileNames.add(tempContainerFile);
            fo = new FileOutputStream(tempContainerFile);
            byte[] bytes = Base64.getDecoder().decode(m4dFile.getContent());
            fo.write(bytes);
            fo.close();

            resSource = this.synapiClientService.getPartContainer(tempContainerFile, "sourceFile", bearerToken);
            tempSourceFile = "/tmp/" + filename + ".xml";
            tempFileNames.add(tempSourceFile);
            fo = new FileOutputStream(tempSourceFile);
            fo.write(resSource.getByteArray());
            fo.close();

            resSig = this.synapiClientService.getPartContainer(tempContainerFile, "detachedSign", bearerToken);
            tempSigFile = "/tmp/" + filename + "_detached.sig";
            tempFileNames.add(tempSigFile);
            fo = new FileOutputStream(tempSigFile);
            fo.write(resSig.getByteArray());
            fo.close();

            tempZipFile = "/tmp" + filename + ".zip";
            createZip(tempZipFile, tempSourceFile, tempSigFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String fileName : tempFileNames) {
            File f = new File(fileName);
            f.delete();
        }


        return new ByteArrayResource(Files.readAllBytes((new File(tempZipFile)).toPath()));
    }

    private static void createZip(String zipFilePath, String... files) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(zipFilePath);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            for (String filePath : files) {
                File file = new File(filePath);
                if (!file.exists()) {
                    System.err.println("Файл не найден: " + filePath);
                    continue;
                }

                try (FileInputStream fis = new FileInputStream(file)) {
                    ZipEntry zipEntry = new ZipEntry(file.getName());
                    zos.putNextEntry(zipEntry);

                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fis.read(buffer)) >= 0) {
                        zos.write(buffer, 0, length);
                    }

                    zos.closeEntry();
                }
            }
        }
    }

    private LocalDateTime convertStringToLocalDateTime(String date) {
        DateTimeFormatter formatter;
        LocalDateTime localDateTime;
        if (date.length() == 8) {
            formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            LocalDate localDate = LocalDate.parse(date, formatter);
            localDateTime = localDate.atStartOfDay();
        } else if (date.length() == 10) {
            formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate localDate = LocalDate.parse(date, formatter);
            localDateTime = localDate.atStartOfDay();
        } else if (date.length() > 20) {
            formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
            localDateTime = LocalDateTime.from(formatter.parse(date));
        } else if (date.length() == 16) {
            formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
            localDateTime = LocalDateTime.from(formatter.parse(date));
        } else {
            formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX");
            localDateTime = LocalDateTime.from(formatter.parse(date));
        }
        return localDateTime;
    }

    private M4dResponseDto.SigFileDto convertSigFileToDto(M4dFile m4dFile) {
        return modelMapper.map(m4dFile, M4dResponseDto.SigFileDto.class);
    }

    private M4dResponseDto.RecallDto.SigFileDto convertSigFileRecallToDto(M4dFile m4dFile) {
        return modelMapper.map(m4dFile, M4dResponseDto.RecallDto.SigFileDto.class);
    }

    private M4dResponseDto.OrganizationDto convertOrganisationToDto(Organisation organisation) {
        return modelMapper.map(organisation, M4dResponseDto.OrganizationDto.class);
    }

    private M4dResponseDto.PrincipalDto convertPrincipalToDto(Principal principal) {
        return modelMapper.map(principal, M4dResponseDto.PrincipalDto.class);
    }

    private M4dResponseDto.PrincipalDetailDto convertPrincipalDetailToDto(PrincipalDetail principalDetail) {
        return modelMapper.map(principalDetail, M4dResponseDto.PrincipalDetailDto.class);
    }

    private M4dResponseDto.RepresentativeDto convertRepresentativeToDto(Representative representative) {
        M4dResponseDto.RepresentativeDto representativeDto = modelMapper.map(representative, M4dResponseDto.RepresentativeDto.class);
        representativeDto.representativePassport = modelMapper.map(representative.getRepresentativePassports(), M4dResponseDto.RepresentativePassportDto.class);
        return representativeDto;
    }

    private M4dResponseDto.RepresentativePassportDto convertRepresentativePassportToDto(RepresentativePassport representativePassport) {
        return modelMapper.map(representativePassport, M4dResponseDto.RepresentativePassportDto.class);
    }
}

