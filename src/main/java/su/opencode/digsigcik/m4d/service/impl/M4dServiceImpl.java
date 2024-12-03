package su.opencode.digsigcik.m4d.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
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
import su.opencode.digsigcik.m4d.dto.CredentialDto;
import su.opencode.digsigcik.m4d.dto.M4dFullXML;
import su.opencode.digsigcik.m4d.dto.M4dResponseDto;
import su.opencode.digsigcik.m4d.dto.response.AuthResponse;
import su.opencode.digsigcik.m4d.dto.response.M4dFnsDataResponse;
import su.opencode.digsigcik.m4d.exception.BadRequestException;
import su.opencode.digsigcik.m4d.exception.M4dException;
import su.opencode.digsigcik.m4d.model.*;
import su.opencode.digsigcik.m4d.repository.M4dCredentialRepository;
import su.opencode.digsigcik.m4d.repository.M4dRepository;
import su.opencode.digsigcik.m4d.repository.M4dVerifyStatusRepository;
import su.opencode.digsigcik.m4d.service.*;
import su.opencode.digsigcik.m4d.util.XMLUtil;
import su.opencode.digsigcik.m4d.util.XmlValidator;

import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;
import java.io.*;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static su.opencode.digsigcik.m4d.util.Constants.*;
import static su.opencode.digsigcik.m4d.util.XMLUtil.convertStringToDocument;

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
    private final RepresentativeService representativeService;
    private final RepresentativePassportService representativePassportService;
    private final UserCertificateService userCertificateService;
    private final RestTemplate restTemplate;

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
    public ResponseEntity<?> saveAllDataMrpaRegistry(M4d m4d, Organisation organisation, Principal principal, Representative representative, RepresentativePassport representativePassport, List<CredentialDto> credentialList, List<Long> credentials) {
        Long orgId;
        Long principId;
        Long represId;
        if (organisation.getInn() == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString("The organization's INN is not specified"), HttpStatus.BAD_REQUEST);
        }
        if (principal.getInn() == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString("The principal's INN is not specified"), HttpStatus.BAD_REQUEST);
        }
        if (representative.getInn() == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString("The representative's INN is not specified"), HttpStatus.BAD_REQUEST);
        }
        if (representativePassport.getSeriesNumber() == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString("The passport's SeriesNumber is not specified"), HttpStatus.BAD_REQUEST);
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
                    return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
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
                    return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
                }
                assert respOrganisation != null;
                orgId = respOrganisation.getId();
            } else {
                orgId = existsOrganisation.getId();
            }
        } catch (ConstraintViolationException e) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
        //------------------- end save organisation ----------------

        //------------------- save principal ----------------
        Principal existsPrincipal;
        try {
            existsPrincipal = this.principalService.findPrincipalByInn(principal.getInn());
            if (existsPrincipal == null) {
                Principal respPrincipal;
                try {
                    respPrincipal = this.principalService.savePrincipal(principal);
                } catch (ConstraintViolationException e) {
                    return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
                }
                assert respPrincipal != null;
                principId = respPrincipal.getId();
            } else if (!existsPrincipal.getInn().equals(principal.getInn()) || !existsPrincipal.getSnils().equals(principal.getSnils())
                    || !existsPrincipal.getSurname().equals(principal.getSurname()) || !existsPrincipal.getName().equals(principal.getName())
                    || !existsPrincipal.getPatronymic().equals(principal.getPatronymic()) || !existsPrincipal.getJobTitle().equals(principal.getJobTitle())) {
                Principal respPrincipal;
                try {
                    principal.setId(existsPrincipal.getId());
                    respPrincipal = this.principalService.savePrincipal(principal);
                } catch (ConstraintViolationException e) {
                    return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
                }
                assert respPrincipal != null;
                principId = respPrincipal.getId();
            } else {
                principId = existsPrincipal.getId();
            }
        } catch (ConstraintViolationException e) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
        }

        //------------------- end save principal ----------------

        //------------------- save representative and passport ----------------
        Representative existsRepresentative;
        try {
            existsRepresentative = this.representativeService.findRepresentativeByInn(representative.getInn());
            if (existsRepresentative == null) {
                Representative respRepresentative;
                try {
                    respRepresentative = this.representativeService.saveRepresentative(representative);
                } catch (ConstraintViolationException e) {
                    return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
                }
                if (respRepresentative.getId() != null) {
                    representativePassport.setRepresentativeId(respRepresentative.getId());
                    try {
                        this.representativePassportService.saveRepresentativePassport(representativePassport);
                    } catch (ConstraintViolationException e) {
                        return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
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
                    return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
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
                                return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
                            }
                        } else if (!existsRepresentativePassport.getSeriesNumber().equals(representativePassport.getSeriesNumber()) || !existsRepresentativePassport.getDateIssue().equals(representativePassport.getDateIssue())
                                || !existsRepresentativePassport.getWhoIssue().equals(representativePassport.getWhoIssue()) || !existsRepresentativePassport.getDepartmentCode().equals(representativePassport.getDepartmentCode())) {
                            representativePassport.setId(existsRepresentativePassport.getId());
                            representativePassport.setRepresentativeId(respRepresentative.getId());
                            try {
                                this.representativePassportService.saveRepresentativePassport(representativePassport);
                            } catch (ConstraintViolationException e) {
                                return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
                            }
                        }
                    } catch (ConstraintViolationException e) {
                        return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
                    }
                }
                represId = respRepresentative.getId();
            } else {
                represId = existsRepresentative.getId();
            }
        } catch (ConstraintViolationException e) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
        //------------------- end save representative and passport ----------------

        if (orgId == null || principId == null || represId == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_REGISTRY_NOT_FULL), HttpStatus.BAD_REQUEST);
        }
        m4d.setOrganisationId(orgId);
        m4d.setPrincipalId(principId);
        m4d.setRepresentativeId(represId);

        M4d m4dSaved;
        try {
            m4dSaved = this.m4dRepository.save(m4d);
        } catch (ConstraintViolationException e) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
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
                        return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
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
                    return new ResponseEntity<>(getCustomErrorJsonFromString(CREDENTIAL_NOT_FOUND), HttpStatus.BAD_REQUEST);
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
                    return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
                }
            }
        }

        return new ResponseEntity<>(m4dSaved.getId(), HttpStatus.OK);
    }

    @AuditLogThis(event = AuditLogEventEnum.M4D_UPDATE)
    @Transactional(rollbackFor = {Exception.class}, isolation = Isolation.READ_UNCOMMITTED)
    public ResponseEntity<?> updateAllDataMrpaRegistry(M4d m4d, Organisation organisation, Principal principal, Representative representative, RepresentativePassport representativePassport, List<CredentialDto> credentialList, List<Long> credentials) {
        Long orgId;
        Long principId;
        Long represId;
        if (m4d.getId() == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString("Can`t update mrpa registry because ID null!"), HttpStatus.BAD_REQUEST);
        }
        if (organisation.getInn() == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString("The organization's INN is not specified"), HttpStatus.BAD_REQUEST);
        }
        if (principal.getInn() == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString("The principal's INN is not specified"), HttpStatus.BAD_REQUEST);
        }
        if (representative.getInn() == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString("The representative's INN is not specified"), HttpStatus.BAD_REQUEST);
        }
        M4d existMrpaRegistry = this.findById(m4d.getId());
        if (existMrpaRegistry == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(String.format("Can`t update. Mrpa registry with id=%d %s", m4d.getId(), NOT_FOUND)), HttpStatus.NOT_FOUND);
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
                    return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
                }
                orgId = respOrganisation.getId();
            } else {
                orgId = existsOrganisation.getId();
            }
        } catch (ConstraintViolationException e) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
        //------------------- end save organisation ----------------

        //------------------- save principal ----------------
        Principal existsPrincipal;
        try {
            existsPrincipal = this.principalService.findPrincipalByInn(principal.getInn());
            if (existsPrincipal != null) {
                principal.setId(existsPrincipal.getId());
            }
            Principal respPrincipal;
            try {
                respPrincipal = this.principalService.savePrincipal(principal);
            } catch (ConstraintViolationException e) {
                return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
            }
            principId = respPrincipal.getId();
        } catch (ConstraintViolationException e) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
        //------------------- end save principal ----------------

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
                return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
            }
            RepresentativePassport existsRepresentativePassport = this.representativePassportService.findRepresentativePassportyByRepresentativeId(respRepresentative.getId());
            if (existsRepresentativePassport != null) {
                representativePassport.setId(existsRepresentativePassport.getId());
            }
            try {
                representativePassport.setRepresentativeId(respRepresentative.getId());
                this.representativePassportService.saveRepresentativePassport(representativePassport);
            } catch (ConstraintViolationException e) {
                return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
            }
            represId = respRepresentative.getId();
        } catch (ConstraintViolationException e) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
        //------------------- end save representative and passport ----------------

        if (orgId == null || principId == null || represId == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_REGISTRY_NOT_FULL), HttpStatus.BAD_REQUEST);
        }
        if (!m4d.getOrganisationId().equals(orgId)) {
            m4d.setOrganisationId(orgId);
        }
        if (!m4d.getPrincipalId().equals(principId)) {
            m4d.setPrincipalId(principId);
        }
        if (!m4d.getRepresentativeId().equals(represId)) {
            m4d.setRepresentativeId(represId);
        }
        M4d m4dUpdated;
        try {
            m4dUpdated = this.m4dRepository.save(m4d);
        } catch (ConstraintViolationException e) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
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
                        return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
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
                    return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
                }
            }
        }
        return new ResponseEntity<>(m4dUpdated.getId(), HttpStatus.OK);
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
    public List<M4dResponseDto.CredentialDTO> getCredentialByMrpaRegistryId(Long mrpaId) {
        List<Credential> credentials = this.credentialService.findCredentialByMrpaId(mrpaId);
        List<M4dResponseDto.CredentialDTO> credentialDTOList = new ArrayList<>();
        assert credentials != null;
        for (Credential c : credentials) {
            M4dResponseDto.CredentialDTO credentialDTO = new M4dResponseDto.CredentialDTO();
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
        principal.setJobTitle(m4dFullDTO.principal.getJobTitle());
        personPrincipal.setPrincipal(principal);
        M4dFullXML.Document.PowerAttorneys.InfoPrincipals.InfoPrincipal.LegalEntity.PersonPrincipal.Principal.FioPrincipal fioPrincipal = new M4dFullXML.Document.PowerAttorneys.InfoPrincipals.InfoPrincipal.LegalEntity.PersonPrincipal.Principal.FioPrincipal();
        principal.setFioPrincipal(fioPrincipal);
        M4dFullXML.Document.PowerAttorneys.InfoPrincipals.InfoPrincipal.LegalEntity.PersonPrincipal.Principal.FioPrincipal.Fio fio = new M4dFullXML.Document.PowerAttorneys.InfoPrincipals.InfoPrincipal.LegalEntity.PersonPrincipal.Principal.FioPrincipal.Fio();
        fio.setName(m4dFullDTO.principal.getName());
        fio.setPatronymic(m4dFullDTO.principal.getPatronymic());
        fio.setSurname(m4dFullDTO.principal.getSurname());
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
        for (M4dResponseDto.CredentialDTO credentialDTO : m4dFullDTO.credentials) {
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


    public Map<String, String> getCustomErrorJsonFromString(String text) {
        Map<String, String> body = new HashMap<>();
        body.put("error", text);
        return body;
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
                M4dResponseDto.SigFileDTO sigFileDTO = convertSigFileToDto(m4dFile);
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
                M4dResponseDto.RecallDTO.SigFileDTO sigFileDTO = convertSigFileRecallToDto(m4dFileRecall);
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

    public ResponseEntity<?> verifyInFns(Authentication authentication, MultipartFile sourceFile, MultipartFile sigFile) throws IOException, SAXException, CertificateException {

        String extension = FilenameUtils.getExtension(sourceFile.getOriginalFilename());
        if (!Objects.equals(extension, "xml")) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_REGISTRY_WRONG_FORMAT_XML), HttpStatus.BAD_REQUEST);
        }

        List<SAXParseException> res = new XmlValidator("/app/xml/validation/ON_EMCHD_1_928_00_01_01_01.xsd", sourceFile.getInputStream()).listParsingExceptions();
        System.out.println(res.size());
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
                return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_REGISTRY_WRONG_FORMAT_XML), HttpStatus.BAD_REQUEST);
            }

            if (m4dXml == null) {
                return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_IS_NULL), HttpStatus.BAD_REQUEST);
            }

            String guidXml;
            guidXml = m4dXml.document.powerAttorneys.getMrpa().getGuid();
            System.out.println(guidXml);

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
                // Обработка ошибок клиента, включая 404
                if (e.getStatusCode().value() == 404) {
                    System.out.println("Ресурс не найден (404): " + e.getResponseBodyAsString());
                    return new ResponseEntity<>(getCustomErrorJsonFromString("Не найдено МЧД с GUID=" + guidXml + " на РР МЧД ФНС"), HttpStatus.NOT_FOUND);
                } else {
                    System.out.println("Ошибка: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
                    return new ResponseEntity<>(getCustomErrorJsonFromString("Ошибка: " + e.getStatusCode() + " - " + e.getResponseBodyAsString()), HttpStatus.BAD_REQUEST);
                }
            } catch (ResourceAccessException e) {
                // Обработка ошибок доступа к ресурсу (например, UnknownHostException)
                System.out.println("Ошибка доступа к ресурсу: " + e.getMessage());
                if (e.getCause() instanceof UnknownHostException) {
                    System.out.println("Неизвестный хост: " + e.getCause().getMessage());
                    return new ResponseEntity<>(getCustomErrorJsonFromString("Сервис " + API_RR_FNS_URI + "/vst-oauth2/oauth/token недоступен"), HttpStatus.SERVICE_UNAVAILABLE);
                }
            } catch (Exception e) {
                // Обработка других исключений
                System.out.println("Произошла ошибка: " + e.getMessage());
                return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
            }

            if (authResponse == null) {
                return new ResponseEntity<>(getCustomErrorJsonFromString("Ошибка авторизации"), HttpStatus.BAD_REQUEST);
            }

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
                // Обработка ошибок клиента, включая 404
                if (e.getStatusCode().value() == 404) {
                    System.out.println("Ресурс не найден (404): " + e.getResponseBodyAsString());
                    return new ResponseEntity<>(getCustomErrorJsonFromString("Не найдено МЧД с GUID=" + guidXml + " на РР МЧД ФНС"), HttpStatus.NOT_FOUND);
                } else {
                    System.out.println("Ошибка: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
                    return new ResponseEntity<>(getCustomErrorJsonFromString("Ошибка: " + e.getStatusCode() + " - " + e.getResponseBodyAsString()), HttpStatus.BAD_REQUEST);
                }
            } catch (ResourceAccessException e) {
                // Обработка ошибок доступа к ресурсу (например, UnknownHostException)
                System.out.println("Ошибка доступа к ресурсу: " + e.getMessage());
                if (e.getCause() instanceof UnknownHostException) {
                    System.out.println("Неизвестный хост: " + e.getCause().getMessage());
                    return new ResponseEntity<>(getCustomErrorJsonFromString("Сервис " + API_RR_FNS_URI + "/poar-webapp/integration/poa/ недоступен"), HttpStatus.SERVICE_UNAVAILABLE);
                }
            } catch (Exception e) {
                // Обработка других исключений
                System.out.println("Произошла ошибка: " + e.getMessage());
            }
            if (m4dFnsDataResponse == null) {
                return new ResponseEntity<>(getCustomErrorJsonFromString("Ошибка получения данных по МЧД с сайта РР МЧД ФНС"), HttpStatus.BAD_REQUEST);
            }

            // сверка данных PublicKey, SnilsHash, OgrnHash из m4dFnsDataResponse с соответствующими данными из подписи sig-файла
//            Map<String, String> result = readSig(sigFile);
//            result.get("sigSnils");

            String status = m4dFnsDataResponse.getStatus();

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
                    return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
                }
            } else {
                existsM4dVerifyStatus.setVerifyDate(LocalDateTime.now());
                existsM4dVerifyStatus.setStatus(status);
                try {
                    m4dVerifyStatusSaved = this.m4dVerifyStatusRepository.save(existsM4dVerifyStatus);
                } catch (ConstraintViolationException e) {
                    return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
                }
            }
            return new ResponseEntity<>(m4dVerifyStatusSaved, HttpStatus.OK);
        }
        Map<String, String> body = new HashMap<>();
        List<String> errs = new ArrayList<>();
        res.forEach(e -> errs.add(String.format("Line number: %s, Column number: %s. %s", e.getLineNumber(), e.getColumnNumber(), e.getMessage())));
        body.put("details", String.join(System.lineSeparator(), errs));

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @Override
    public M4dVerifyStatus getStatusVerifyInFns(@NotNull String guid) {
        return this.m4dVerifyStatusRepository.findM4dVerifyStatusByGuidIgnoreCase(guid);
    }

    private Map<String, String> readSig(MultipartFile sigFile) throws IOException, CertificateException {
        String sigPublicKey;
        String sigSnils = "";
        String sigOgrn = "";

        String BEGIN_CERTIFICATE = "-----BEGIN CERTIFICATE-----";
        String END_CERTIFICATE = "-----END CERTIFICATE-----";
        StringBuilder updatedContent = new StringBuilder();

        try {
            String originalContent = new String(sigFile.getBytes());
            boolean hasBegin = originalContent.contains(BEGIN_CERTIFICATE);
            boolean hasEnd = originalContent.contains(END_CERTIFICATE);
            if (!hasBegin) {
                updatedContent.append(BEGIN_CERTIFICATE).append(System.lineSeparator());
            }
            updatedContent.append(originalContent);
            if (!hasEnd) {
                updatedContent.append(System.lineSeparator()).append(END_CERTIFICATE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
//            X509Certificate certificate = new CertificateContentWrapper(updatedContent.toString())
//                    .decodeToX509();

        String tempFileName;
        FileOutputStream fo;
        tempFileName = "/tmp/" + sigFile.getOriginalFilename();
        fo = new FileOutputStream(tempFileName);
        fo.write(updatedContent.toString().getBytes());
        fo.close();
        FileInputStream fis = new FileInputStream(tempFileName);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate) cf.generateCertificate(fis);

        sigPublicKey = certificate.getPublicKey().toString();
        var substrSubject = certificate.getSubjectDN().getName().split(", ");
        for (String s : substrSubject) {
            if (s.length() > 2) {
                if (s.startsWith("OID.1.2.643.100.3=")) {
                    String hexStr = s.substring(23);
                    StringBuilder output = new StringBuilder();
                    for (int i = 0; i < hexStr.length(); i += 2) {
                        String str1 = hexStr.substring(i, i + 2);
                        output.append((char) Integer.parseInt(str1, 16));
                    }
                    sigSnils = output.toString();
                }
                if (s.startsWith("OID.1.2.643.3.131.1.1=")) {
                    String hexStr = s.substring(27);
                    StringBuilder output = new StringBuilder();
                    for (int i = 0; i < hexStr.length(); i += 2) {
                        String str1 = hexStr.substring(i, i + 2);
                        output.append((char) Integer.parseInt(str1, 16));
                    }
                    sigOgrn = output.toString();
                }

            }
        }
        Map<String, String> result = new HashMap<>();
        result.put("sigSnils", sigSnils);
        result.put("sigOgrn", sigOgrn);
        result.put("sigPublicKey", sigPublicKey);
        return result;
    }

    private M4dResponseDto.SigFileDTO convertSigFileToDto(M4dFile m4dFile) {
        return modelMapper.map(m4dFile, M4dResponseDto.SigFileDTO.class);
    }

    private M4dResponseDto.RecallDTO.SigFileDTO convertSigFileRecallToDto(M4dFile m4dFile) {
        return modelMapper.map(m4dFile, M4dResponseDto.RecallDTO.SigFileDTO.class);
    }

    private M4dResponseDto.OrganizationDTO convertOrganisationToDto(Organisation organisation) {
        return modelMapper.map(organisation, M4dResponseDto.OrganizationDTO.class);
    }

    private M4dResponseDto.PrincipalDTO convertPrincipalToDto(Principal principal) {
        return modelMapper.map(principal, M4dResponseDto.PrincipalDTO.class);
    }

    private M4dResponseDto.RepresentativeDTO convertRepresentativeToDto(Representative representative) {
        M4dResponseDto.RepresentativeDTO representativeDto = modelMapper.map(representative, M4dResponseDto.RepresentativeDTO.class);
        representativeDto.representativePassport = modelMapper.map(representative.getRepresentativePassports(), M4dResponseDto.RepresentativePassportDTO.class);
        return representativeDto;
    }

    private M4dResponseDto.RepresentativePassportDTO convertRepresentativePassportToDto(RepresentativePassport representativePassport) {
        return modelMapper.map(representativePassport, M4dResponseDto.RepresentativePassportDTO.class);
    }
}

