package su.opencode.digsigcik.synapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import su.opencode.digsigcik.synapi.dto.MrpaRegistryFullDTO;
import su.opencode.digsigcik.synapi.dto.MrpaRegistryWithFioDTO;
import su.opencode.digsigcik.synapi.dto.MrpaRegistryWithFioListDto;
import su.opencode.digsigcik.synapi.dto.m4d.MrpaRegistryFullXML;
import su.opencode.digsigcik.synapi.exception.BadRequestException;
import su.opencode.digsigcik.synapi.exception.MchdException;
import su.opencode.digsigcik.synapi.model.*;
import su.opencode.digsigcik.synapi.repository.*;

import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static su.opencode.digsigcik.synapi.util.Constants.*;

@Service
@Primary
@RequiredArgsConstructor
@EnableTransactionManagement
public class MrpaRegistryServiceImpl implements MrpaRegistryService {
    private final MrpaRegistryRepository mrpaRegistryRepository;
    private final OrganisationRepository organisationRepository;
    private final PrincipalRepository principalRepository;
    private final RepresentativeRepository representativeRepository;
    private final RepresentativePassportRepository representativePassportRepository;
    private final CredentialRepository credentialRepository;
    private final MrpaRegistryCredentialRepository mrpaRegistryCredentialRepository;
    private final PublicKeyUserCertificateService publicKeyUserCertificateService;

    @Override
    public List<MrpaRegistryWithFioListDto> findAllMrpaRegistry() {
        return this.mrpaRegistryRepository.findAllMrpaRegistries();
    }

    @Override
    public MrpaRegistry saveMrpaRegistry(@NotNull MrpaRegistry mrpaRegistry) {
        throwExceptionIfRecordWithIdNotExist(mrpaRegistry, "create");
        return this.mrpaRegistryRepository.save(mrpaRegistry);
    }

    @Transactional(rollbackFor = {Exception.class}, isolation = Isolation.READ_UNCOMMITTED)
    public MrpaRegistry saveAllDataMrpaRegistry(MrpaRegistry mrpaRegistry, Organisation organisation, Principal principal, Representative representative, RepresentativePassport representativePassport, List<Credential> credentialList, List<Long> credentials) {
        Long orgId;
        Long principId;
        Long represId;
        //------------------- save organisation ----------------
        Organisation existsOrganisation = this.organisationRepository.findOrganisationByInn(organisation.getInn());
        if (existsOrganisation == null) {
            Organisation respOrganisation;
            try {
                respOrganisation = this.organisationRepository.save(organisation);
            } catch (ConstraintViolationException e) {
                throw new BadRequestException(ORGANISATION_IS_NULL);
            }
            orgId = respOrganisation.getId();
        } else {
            orgId = existsOrganisation.getId();
        }
        //------------------- end save organisation ----------------

        //------------------- save principal ----------------
        Principal existsPrincipal = this.principalRepository.findPrincipalByInn(principal.getInn());
        if (existsPrincipal == null) {
            Principal respPrincipal;
            try {
                respPrincipal = this.principalRepository.save(principal);
            } catch (ConstraintViolationException e) {
                throw new BadRequestException(PRINCIPAL_IS_NULL);
            }
            principId = respPrincipal.getId();
        } else {
            principId = existsPrincipal.getId();
        }
        //------------------- end save principal ----------------

        //------------------- save representative and passport ----------------
        Representative existsRepresentative = this.representativeRepository.findRepresentativeByInn(representative.getInn());
        if (existsRepresentative == null) {
            Representative respRepresentative;
            try {
                respRepresentative = this.representativeRepository.save(representative);
            } catch (ConstraintViolationException e) {
                throw new BadRequestException(REPRESENTATIVE_IS_NULL);
            }
            try {
                representativePassport.setRepresentativeId(respRepresentative.getId());
                this.representativePassportRepository.save(representativePassport);
            } catch (ConstraintViolationException e) {
                throw new BadRequestException(REPRESENTATIVE_PASSPORT_IS_NULL);
            }
            represId = respRepresentative.getId();
        } else {
            represId = existsRepresentative.getId();
        }
        //------------------- end save representative and passport ----------------

        if (orgId == null || principId == null || represId == null) {
            throw new BadRequestException(MRPA_REGISTRY_NOT_FULL);
        }
        mrpaRegistry.setOrganisationId(orgId);
        mrpaRegistry.setPrincipalId(principId);
        mrpaRegistry.setRepresentativeId(represId);

        MrpaRegistry mrpaRegistrySaved;
        try {
            mrpaRegistrySaved = this.mrpaRegistryRepository.save(mrpaRegistry);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(MRPA_REGISTRY_IS_NULL);
        }
        List<Long> credList = new ArrayList<>();
        if (credentialList != null) {
            for (Credential c : credentialList) {
                List<Credential> existsCredentials = this.credentialRepository.findCredentialByCodeIgnoreCase(c.getCode());
                if (existsCredentials.isEmpty()) {
                    Credential newCredential = new Credential();
                    newCredential.setCode(c.getCode());
                    newCredential.setName(c.getName());
                    Credential credentialSave;
                    try {
                        credentialSave = this.credentialRepository.save(newCredential);
                    } catch (ConstraintViolationException e) {
                        throw new BadRequestException(CREDENTIAL_IS_NULL);
                    }
                    credList.add(credentialSave.getId());
                } else {
                    for (Credential crd : existsCredentials) {
                        credList.add(crd.getId());
                    }
                }
            }
        } else if(credentials != null) {
            credList = credentials;
        }

        if (!credList.isEmpty()) {
            for (Long x : credList) {
                MrpaRegistryCredential mrpaRegistryCredential = new MrpaRegistryCredential();
                mrpaRegistryCredential.setMrpaRegistryId(mrpaRegistrySaved.getId());
                mrpaRegistryCredential.setCredentialId(x);
                try {
                    this.mrpaRegistryCredentialRepository.save(mrpaRegistryCredential);
                } catch (ConstraintViolationException e) {
                    throw new BadRequestException(MRPA_REGISTRY_CREDENTIAL_IS_NULL);
                }
            }
        }

        return mrpaRegistrySaved;
    }

    @Transactional(rollbackFor = {Exception.class}, isolation = Isolation.READ_UNCOMMITTED)
    public MrpaRegistry updateAllDataMrpaRegistry(MrpaRegistry mrpaRegistry, Organisation organisation, Principal principal, Representative representative, RepresentativePassport representativePassport, List<Credential> credentialList, List<Long> credentials) {
        Long orgId;
        Long principId;
        Long represId;
        if (mrpaRegistry.getId() == null) {
            throw new BadRequestException("Can`t update mrpa registry because id null!");
        }
        MrpaRegistryWithFioDTO existMrpaRegistry = this.mrpaRegistryRepository.findMrpaRegistryById(mrpaRegistry.getId());
        if (existMrpaRegistry == null) {
            throw new MchdException(String.format("Can`t update. Mrpa registry with id=%d %s", mrpaRegistry.getId(), NOT_FOUND));
        }
        //------------------- save organisation ----------------
        Organisation existsOrganisation = this.organisationRepository.findOrganisationByInn(organisation.getInn());
        if (existsOrganisation != null) {
            organisation.setId(existsOrganisation.getId());
        }
        if(!existsOrganisation.equals(organisation)){
            Organisation respOrganisation;
            try {
                respOrganisation = this.organisationRepository.save(organisation);
            } catch (ConstraintViolationException e) {
                throw new BadRequestException(ORGANISATION_IS_NULL);
            }
            orgId = respOrganisation.getId();
        } else {
            orgId = existsOrganisation.getId();
        }


        //------------------- end save organisation ----------------

        //------------------- save principal ----------------
        Principal existsPrincipal = this.principalRepository.findPrincipalByInn(principal.getInn());
        if (existsPrincipal != null) {
            principal.setId(existsPrincipal.getId());
        }
        Principal respPrincipal;
        try {
            respPrincipal = this.principalRepository.save(principal);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(PRINCIPAL_IS_NULL);
        }
        principId = respPrincipal.getId();
        //------------------- end save principal ----------------

        //------------------- save representative and passport ----------------
        Representative existsRepresentative = this.representativeRepository.findRepresentativeByInn(representative.getInn());
        if (existsRepresentative != null) {
            representative.setId(existsRepresentative.getId());
        }
        Representative respRepresentative;
        try {
            respRepresentative = this.representativeRepository.save(representative);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(REPRESENTATIVE_IS_NULL);
        }
        RepresentativePassport existsRepresentativePassport = this.representativePassportRepository.findRepresentativePassportBySeriesNumber(representativePassport.getSeriesNumber());
        if (existsRepresentativePassport != null) {
            representativePassport.setId(existsRepresentativePassport.getId());
        }
        try {
            representativePassport.setRepresentativeId(respRepresentative.getId());
            this.representativePassportRepository.save(representativePassport);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(REPRESENTATIVE_PASSPORT_IS_NULL);
        }
        represId = respRepresentative.getId();
        //------------------- end save representative and passport ----------------

        if (orgId == null || principId == null || represId == null) {
            throw new BadRequestException(MRPA_REGISTRY_NOT_FULL);
        }
        if(!mrpaRegistry.getOrganisationId().equals(orgId)){
            mrpaRegistry.setOrganisationId(orgId);
        }
        if(!mrpaRegistry.getPrincipalId().equals(principId)) {
            mrpaRegistry.setPrincipalId(principId);
        }
        if(!mrpaRegistry.getRepresentativeId().equals(represId)) {
            mrpaRegistry.setRepresentativeId(represId);
        }
        MrpaRegistry mrpaRegistryUpdated;
        try {
            mrpaRegistryUpdated = this.mrpaRegistryRepository.save(mrpaRegistry);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(MRPA_REGISTRY_IS_NULL);
        }
        List<Long> credList = new ArrayList<>();
        if (credentialList != null) {
            for (Credential c : credentialList) {
                List<Credential> existsCredentials = this.credentialRepository.findCredentialByCodeIgnoreCase(c.getCode());
                if (existsCredentials.isEmpty()) {
                    Credential newCredential = new Credential();
                    newCredential.setCode(c.getCode());
                    newCredential.setName(c.getName());
                    Credential credentialSave;
                    try {
                        credentialSave = this.credentialRepository.save(newCredential);
                    } catch (ConstraintViolationException e) {
                        throw new BadRequestException(CREDENTIAL_IS_NULL);
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

        if(!credList.isEmpty()){
            this.mrpaRegistryCredentialRepository.deleteAllByMrpaRegistryId(mrpaRegistryUpdated.getId());
            for (Long x : credList) {
                MrpaRegistryCredential mrpaRegistryCredential = new MrpaRegistryCredential();
                mrpaRegistryCredential.setMrpaRegistryId(mrpaRegistryUpdated.getId());
                mrpaRegistryCredential.setCredentialId(x);
                try {
                    this.mrpaRegistryCredentialRepository.save(mrpaRegistryCredential);
                } catch (ConstraintViolationException e) {
                    throw new BadRequestException(MRPA_REGISTRY_CREDENTIAL_IS_NULL);
                }
            }
        }
        return mrpaRegistryUpdated;
    }

    @Override
    public MrpaRegistryWithFioDTO findByMrpaRegistryId(@NotNull Long id) {
        MrpaRegistryWithFioDTO mrpaRegistry = this.mrpaRegistryRepository.findMrpaRegistryById(id);
        if (mrpaRegistry == null) {
            throw new MchdException(String.format("Mrpa registry %s", NOT_FOUND));
        }
        return mrpaRegistry;
    }

    @Override
    public Integer findMrpaRegistryByGuid(@NotNull String guid) {
        return this.mrpaRegistryRepository.findMrpaRegistryByGuid(guid);
    }

    @Override
    public MrpaRegistryFullDTO findMrpaRegistryFullById(@NotNull Long id) {
        MrpaRegistryFullDTO mrpaRegistryFullDTO = this.mrpaRegistryRepository.findMrpaRegistryFullById(id);
        if (mrpaRegistryFullDTO == null) {
            throw new MchdException(String.format("Mrpa registry %s", NOT_FOUND));
        }
        return mrpaRegistryFullDTO;
    }

    @Override
    public MrpaRegistry findById(@NotNull Long id) {
        return this.mrpaRegistryRepository.findMrpaById(id);
    }

    @Transactional
    @Override
    public MrpaRegistry updateMrpaRegistry(@NotNull MrpaRegistry mrpaRegistry) {
        throwExceptionIfRecordWithIdNotExist(mrpaRegistry, "update");
        Long mrpaRegistryId = mrpaRegistry.getId();

        if (mrpaRegistryId == null) {
            throw new BadRequestException("Can`t update mrpa registry because id null!");
        }

        MrpaRegistryWithFioDTO existMrpaRegistry = this.mrpaRegistryRepository.findMrpaRegistryById(mrpaRegistryId);
        if (existMrpaRegistry == null) {
            throw new MchdException(String.format("Can`t update. Mrpa registry with id=%d %s", mrpaRegistryId, NOT_FOUND));
        }

        return this.mrpaRegistryRepository.save(mrpaRegistry);
    }

    @Override
    public void deleteMrpaRegistry(@NotNull Long id) {
        MrpaRegistryWithFioDTO mrpaRegistry = this.mrpaRegistryRepository.findMrpaRegistryById(id);
        if (mrpaRegistry == null) {
            throw new MchdException(String.format("Can`t delete mrpa registry because mrpa registry %s", NOT_FOUND));
        }
        this.mrpaRegistryRepository.deleteById(id);
    }

    @Override
    public MrpaRegistryWithFioDTO findByMrpaRegistryPrincipalInn(String inn) {
        return this.mrpaRegistryRepository.findMrpaRegistriesByPrincipalInn(inn);
    }

    private void throwExceptionIfRecordWithIdNotExist(MrpaRegistry mrpaRegistry, String operationType) {
        Long organisationId = mrpaRegistry.getOrganisationId();
        Long principalId = mrpaRegistry.getPrincipalId();
        Long representativeId = mrpaRegistry.getRepresentativeId();

        String errorMessage = "Can`t %s mrpa registry because %s id null!";
        if (this.organisationRepository.findOrganisationById(organisationId) == null) {
            throw new BadRequestException(String.format(errorMessage, operationType, "organisation", organisationId));
        }
        if (this.principalRepository.findPrincipalById(principalId) == null) {
            throw new BadRequestException(String.format(errorMessage, operationType, "principal", principalId));
        }
        if (this.representativeRepository.findRepresentativeById(representativeId) == null) {
            throw new BadRequestException(String.format(errorMessage, operationType, "representative", representativeId));
        }
    }

    @Override
    public void recallMrpaRegistryById(Long id) {
        MrpaRegistryWithFioDTO mrpaRegistry = this.mrpaRegistryRepository.findMrpaRegistryById(id);
        if (mrpaRegistry == null) {
            throw new MchdException(String.format("Can`t recall mrpa registry because mrpa registry %s", NOT_FOUND));
        }
        this.mrpaRegistryRepository.recallMrpaRegistryById(id);
    }

    @Override
    public List<MrpaRegistryWithFioDTO.CredentialDTO> getCredentialByMrpaRegistryId(Long mrpaId) {

        List<Credential> credential = this.credentialRepository.findCredentialByMrpaId(mrpaId);
        List<MrpaRegistryWithFioDTO.CredentialDTO> credentialDTOList = new ArrayList<>();

        for (Credential c : credential) {
            MrpaRegistryWithFioDTO.CredentialDTO credentialDTO = new MrpaRegistryWithFioDTO.CredentialDTO();
            credentialDTO.setId(c.getId());
            credentialDTO.setName(c.getName());
            credentialDTO.setCode(c.getCode());
            credentialDTOList.add(credentialDTO);
        }
        return credentialDTOList;
    }

    @Override
    public List<MrpaRegistryFullDTO.CredentialDTO> getCredentialByMrpaRegistryFullId(Long mrpaId) {

        List<Credential> credential = this.credentialRepository.findCredentialByMrpaId(mrpaId);
        List<MrpaRegistryFullDTO.CredentialDTO> credentialDTOList = new ArrayList<>();

        for (Credential c : credential) {
            MrpaRegistryFullDTO.CredentialDTO credentialDTO = new MrpaRegistryFullDTO.CredentialDTO();
            credentialDTO.setId(c.getId());
            credentialDTO.setName(c.getName());
            credentialDTO.setCode(c.getCode());
            credentialDTOList.add(credentialDTO);
        }
        return credentialDTOList;
    }

    @Override
    public MrpaRegistryFullXML ConvertMrpaRegistryFullDtoToFullXml(MrpaRegistryFullDTO mrpaRegistryFullDTO) {
        DateFormat dateFormatShort = new SimpleDateFormat("yyyyMMdd");
        Date date = new Date();
        String idFile = "ON_EMCHD_" + dateFormatShort.format(date) + "_" + mrpaRegistryFullDTO.getGuid();

        MrpaRegistryFullXML mrpaRegistryFullXML = new MrpaRegistryFullXML();
        mrpaRegistryFullXML.setXmlnsXsi("http://www.w3.org/2001/XMLSchema-instance");
        mrpaRegistryFullXML.setXmlnsXsd("http://www.w3.org/2001/XMLSchema");
        mrpaRegistryFullXML.setXmlns("urn://x-artefacts/EMCHD_1");
        mrpaRegistryFullXML.setVersionFormat("EMCHD_1");
        mrpaRegistryFullXML.setElementsFormat("00000000");
        mrpaRegistryFullXML.setIdFile(idFile); //ON_EMCHD_20240516_75655b80-cc58-4a2b-b244-8334eb895a2f


        MrpaRegistryFullXML.Document.PowerAttorneys.Mrpa mrpas = new MrpaRegistryFullXML.Document.PowerAttorneys.Mrpa();
        mrpas.setTypePowerAttorney("1");
        mrpas.setRetrust("3");

        if (Objects.isNull(mrpaRegistryFullDTO.getInternalNumber())) {
            mrpas.setInternalNumber("б/н");
        } else mrpas.setInternalNumber(mrpaRegistryFullDTO.getInternalNumber());

        mrpas.setGuid(mrpaRegistryFullDTO.getGuid());

        mrpas.setStartDate(convertDateIsoInstantToMchdXml(mrpaRegistryFullDTO.getStartDate()));
        mrpas.setEndDate(convertDateIsoInstantToMchdXml(mrpaRegistryFullDTO.getEndDate()));
        mrpas.setInfoSystem("sep.cik");

        MrpaRegistryFullXML.Document document = new MrpaRegistryFullXML.Document();
        mrpaRegistryFullXML.setDocument(document);

        MrpaRegistryFullXML.Document.PowerAttorneys powerAttorneys = new MrpaRegistryFullXML.Document.PowerAttorneys();
        document.setPowerAttorneys(powerAttorneys);
        powerAttorneys.setMrpa(mrpas);
        MrpaRegistryFullXML.Document.PowerAttorneys.InfoPrincipals infoPrincipal = new MrpaRegistryFullXML.Document.PowerAttorneys.InfoPrincipals();
        infoPrincipal.setTypePrincipal("1");
        powerAttorneys.setPrincipals(infoPrincipal);
        MrpaRegistryFullXML.Document.PowerAttorneys.InfoPrincipals.InfoPrincipal infoPrincipal1 = new MrpaRegistryFullXML.Document.PowerAttorneys.InfoPrincipals.InfoPrincipal();
        infoPrincipal.setPrincipal(infoPrincipal1);
        MrpaRegistryFullXML.Document.PowerAttorneys.InfoPrincipals.InfoPrincipal.LegalEntity legalEntity = new MrpaRegistryFullXML.Document.PowerAttorneys.InfoPrincipals.InfoPrincipal.LegalEntity();
        legalEntity.setManagementCompany("0");
        legalEntity.setIndividual("1");
        legalEntity.setIndividualEntrepreneur("0");
        infoPrincipal1.setLegalEntity(legalEntity);
        MrpaRegistryFullXML.Document.PowerAttorneys.InfoPrincipals.InfoPrincipal.LegalEntity.Organisation organisation = new MrpaRegistryFullXML.Document.PowerAttorneys.InfoPrincipals.InfoPrincipal.LegalEntity.Organisation();
        organisation.setName(mrpaRegistryFullDTO.organization.getName());
        organisation.setInn(mrpaRegistryFullDTO.organization.getInn());
        organisation.setKpp(mrpaRegistryFullDTO.organization.getKpp());
        organisation.setOgrn(mrpaRegistryFullDTO.organization.getOgrn());
        legalEntity.setOrganisation(organisation);
        MrpaRegistryFullXML.Document.PowerAttorneys.InfoPrincipals.InfoPrincipal.LegalEntity.Organisation.AddressRegion addressRegion = new MrpaRegistryFullXML.Document.PowerAttorneys.InfoPrincipals.InfoPrincipal.LegalEntity.Organisation.AddressRegion();
        addressRegion.setRegionCode(mrpaRegistryFullDTO.organization.getRegionCode());
        addressRegion.setLegalAddress(mrpaRegistryFullDTO.organization.getLegalAddress());
        organisation.setAddressRegion(addressRegion);
        MrpaRegistryFullXML.Document.PowerAttorneys.InfoPrincipals.InfoPrincipal.LegalEntity.PersonPrincipal personPrincipal = new MrpaRegistryFullXML.Document.PowerAttorneys.InfoPrincipals.InfoPrincipal.LegalEntity.PersonPrincipal();
        personPrincipal.setTypePrincipal("1");
        legalEntity.setPrincipal(personPrincipal);
        MrpaRegistryFullXML.Document.PowerAttorneys.InfoPrincipals.InfoPrincipal.LegalEntity.PersonPrincipal.Principal principal = new MrpaRegistryFullXML.Document.PowerAttorneys.InfoPrincipals.InfoPrincipal.LegalEntity.PersonPrincipal.Principal();
        principal.setInn(mrpaRegistryFullDTO.principal.getInn());
        principal.setSnils(mrpaRegistryFullDTO.principal.getSnils());
        principal.setJobTitle(mrpaRegistryFullDTO.principal.getJobTitle());
        personPrincipal.setPrincipal(principal);
        MrpaRegistryFullXML.Document.PowerAttorneys.InfoPrincipals.InfoPrincipal.LegalEntity.PersonPrincipal.Principal.FioPrincipal fioPrincipal = new MrpaRegistryFullXML.Document.PowerAttorneys.InfoPrincipals.InfoPrincipal.LegalEntity.PersonPrincipal.Principal.FioPrincipal();
        principal.setFioPrincipal(fioPrincipal);
        MrpaRegistryFullXML.Document.PowerAttorneys.InfoPrincipals.InfoPrincipal.LegalEntity.PersonPrincipal.Principal.FioPrincipal.Fio fio = new MrpaRegistryFullXML.Document.PowerAttorneys.InfoPrincipals.InfoPrincipal.LegalEntity.PersonPrincipal.Principal.FioPrincipal.Fio();
        fio.setName(mrpaRegistryFullDTO.principal.getName());
        fio.setPatronymic(mrpaRegistryFullDTO.principal.getPatronymic());
        fio.setSurname(mrpaRegistryFullDTO.principal.getSurname());
        fioPrincipal.setFio(fio);
        MrpaRegistryFullXML.Document.PowerAttorneys.InfoRepresentative infoRepresentative = new MrpaRegistryFullXML.Document.PowerAttorneys.InfoRepresentative();
        infoRepresentative.setTypeRepresentative("3");
        powerAttorneys.setInfoRepresentative(infoRepresentative);
        MrpaRegistryFullXML.Document.PowerAttorneys.InfoRepresentative.Representative representative = new MrpaRegistryFullXML.Document.PowerAttorneys.InfoRepresentative.Representative();
        infoRepresentative.setRepresentative(representative);
        MrpaRegistryFullXML.Document.PowerAttorneys.InfoRepresentative.Representative.DetailRepresentative detailRepresentative = new MrpaRegistryFullXML.Document.PowerAttorneys.InfoRepresentative.Representative.DetailRepresentative();
        representative.setDetailRepresentative(detailRepresentative);
        detailRepresentative.setInn(mrpaRegistryFullDTO.representative.getInn());
        detailRepresentative.setSnils(mrpaRegistryFullDTO.representative.getSnils());
        MrpaRegistryFullXML.Document.PowerAttorneys.InfoRepresentative.Representative.DetailRepresentative.InfoIndividual infoIndividual = new MrpaRegistryFullXML.Document.PowerAttorneys.InfoRepresentative.Representative.DetailRepresentative.InfoIndividual();
        detailRepresentative.setInfoIndividual(infoIndividual);
        infoIndividual.setBirthDate(convertDateIsoInstantToMchdXml(mrpaRegistryFullDTO.representative.getBirthDate()));
        MrpaRegistryFullXML.Document.PowerAttorneys.InfoRepresentative.Representative.DetailRepresentative.InfoIndividual.FioRepresentative fioRepresentative = new MrpaRegistryFullXML.Document.PowerAttorneys.InfoRepresentative.Representative.DetailRepresentative.InfoIndividual.FioRepresentative();
        infoIndividual.setFioRepresentative(fioRepresentative);
        fioRepresentative.setName(mrpaRegistryFullDTO.representative.getName());
        fioRepresentative.setSurname(mrpaRegistryFullDTO.representative.getSurname());
        fioRepresentative.setPatronymic(mrpaRegistryFullDTO.representative.getPatronymic());
        MrpaRegistryFullXML.Document.PowerAttorneys.InfoRepresentative.Representative.DetailRepresentative.InfoIndividual.RepresentativePassport representativePassport = new MrpaRegistryFullXML.Document.PowerAttorneys.InfoRepresentative.Representative.DetailRepresentative.InfoIndividual.RepresentativePassport();
        infoIndividual.setRepresentativePassport(representativePassport);
        representativePassport.setTypeDocument("21");
        representativePassport.setSeriesNumber(mrpaRegistryFullDTO.representative.representativePassport.getSeriesNumber());
        representativePassport.setDateIssue(convertDateIsoInstantToMchdXml(mrpaRegistryFullDTO.representative.representativePassport.getDateIssue()));
        representativePassport.setWhoIssue(mrpaRegistryFullDTO.representative.representativePassport.getWhoIssue());
        representativePassport.setDepartmentCode(mrpaRegistryFullDTO.representative.representativePassport.getDepartmentCode());
        MrpaRegistryFullXML.Document.PowerAttorneys.Credentials credentials = new MrpaRegistryFullXML.Document.PowerAttorneys.Credentials();
        credentials.setTypeCredential("1");
        credentials.setImplCredential("1");
        credentials.setLossCredential("1");
        ArrayList<MrpaRegistryFullXML.Document.PowerAttorneys.Credentials.Credential> credentialList = new ArrayList<>();
        for (MrpaRegistryFullDTO.CredentialDTO credentialDTO : mrpaRegistryFullDTO.credentials) {
            MrpaRegistryFullXML.Document.PowerAttorneys.Credentials.Credential credential = new MrpaRegistryFullXML.Document.PowerAttorneys.Credentials.Credential();
            credential.setCode(credentialDTO.getCode());
            credential.setName(credentialDTO.getName());
            credentialList.add(credential);
        }
        credentials.setCredentials(credentialList);
        powerAttorneys.setCredential(credentials);

        return mrpaRegistryFullXML;
    }

    private static String convertDateIsoInstantToMchdXml(String date) {
        Date myDate = Date.from(Instant.parse(date));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = formatter.format(myDate);
        return formattedDate;
    }

    @Override
    public void saveMrpaXml(Long id, String xmlData, String xmlFilename) {
        MrpaRegistryWithFioDTO mrpaRegistry = this.mrpaRegistryRepository.findMrpaRegistryById(id);
        if (mrpaRegistry == null) {
            throw new MchdException(String.format("Can`t save xml mrpa registry because mrpa registry %s", NOT_FOUND));
        }
        this.mrpaRegistryRepository.saveMrpaXml(id, xmlData, xmlFilename);
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
    public List<MrpaRegistryWithFioDTO> findAllMrpaRegistryByLogin(String login) {
        return publicKeyUserCertificateService.findAllPublicKeyUserCertificateByLogin(login)
                .stream()
                .map(PublicKeyUserCertificate::getInn)
                .distinct()
                .flatMap(inn -> mrpaRegistryRepository.findMrpaRegistriesByPrincipalInnLikePrincipalAndRepresentative(inn).stream())
                .distinct()
                .collect(Collectors.toList());
    }
}

