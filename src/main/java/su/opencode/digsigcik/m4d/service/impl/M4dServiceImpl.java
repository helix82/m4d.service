package su.opencode.digsigcik.m4d.service.impl;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import su.opencode.digsigcik.m4d.audit.AuditLogEventEnum;
import su.opencode.digsigcik.m4d.audit.AuditLogThis;
import su.opencode.digsigcik.m4d.dto.CredentialDto;
import su.opencode.digsigcik.m4d.dto.M4dFullXML;
import su.opencode.digsigcik.m4d.dto.M4dResponseDto;
import su.opencode.digsigcik.m4d.exception.BadRequestException;
import su.opencode.digsigcik.m4d.exception.M4dException;
import su.opencode.digsigcik.m4d.model.*;
import su.opencode.digsigcik.m4d.repository.M4dCredentialRepository;
import su.opencode.digsigcik.m4d.repository.M4dRepository;
import su.opencode.digsigcik.m4d.service.*;

import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static su.opencode.digsigcik.m4d.util.Constants.*;

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
    private final CredentialService credentialService;
    private final OrganisationService organisationService;
    private final PrincipalService principalService;
    private final RepresentativeService representativeService;
    private final RepresentativePassportService representativePassportService;
    private final UserCertificateService userCertificateService;

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
                    || !existsOrganisation.getLegalAddress().equals(organisation.getLegalAddress()) || !existsOrganisation.getRegionCode().equals(organisation.getRegionCode())) {
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

            for (Long x : credList) {
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
            this.m4dCredentialRepository.deleteAllByMrpaRegistryId(m4dUpdated.getId());
            for (Long x : credList) {
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
            if (!withM4dSigFile) {
                m4dFile.setContent(null);
            }
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
            if (!withRecallSigFile) {
                m4dFileRecall.setContent(null);
            }
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

    private M4dResponseDto.SigFileDTO convertSigFileToDto(M4dFile m4dFile) {
        M4dResponseDto.SigFileDTO sigFileDto = modelMapper.map(m4dFile, M4dResponseDto.SigFileDTO.class);
        return sigFileDto;
    }

    private M4dResponseDto.RecallDTO.SigFileDTO convertSigFileRecallToDto(M4dFile m4dFile) {
        M4dResponseDto.RecallDTO.SigFileDTO sigFileDto = modelMapper.map(m4dFile, M4dResponseDto.RecallDTO.SigFileDTO.class);
        return sigFileDto;
    }

    private M4dResponseDto.OrganizationDTO convertOrganisationToDto(Organisation organisation) {
        M4dResponseDto.OrganizationDTO organisationDto = modelMapper.map(organisation, M4dResponseDto.OrganizationDTO.class);
        return organisationDto;
    }

    private M4dResponseDto.PrincipalDTO convertPrincipalToDto(Principal principal) {
        M4dResponseDto.PrincipalDTO principalDto = modelMapper.map(principal, M4dResponseDto.PrincipalDTO.class);
        return principalDto;
    }

    private M4dResponseDto.RepresentativeDTO convertRepresentativeToDto(Representative representative) {
        M4dResponseDto.RepresentativeDTO representativeDto = modelMapper.map(representative, M4dResponseDto.RepresentativeDTO.class);
        representativeDto.representativePassport = modelMapper.map(representative.getRepresentativePassports(), M4dResponseDto.RepresentativePassportDTO.class);
        return representativeDto;
    }

    private M4dResponseDto.RepresentativePassportDTO convertRepresentativePassportToDto(RepresentativePassport representativePassport) {
        M4dResponseDto.RepresentativePassportDTO representativePassportDto = modelMapper.map(representativePassport, M4dResponseDto.RepresentativePassportDTO.class);
        return representativePassportDto;
    }
}

