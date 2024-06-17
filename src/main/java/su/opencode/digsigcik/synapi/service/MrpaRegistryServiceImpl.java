package su.opencode.digsigcik.synapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.synapi.dto.MrpaRegistryFullDTO;
import su.opencode.digsigcik.synapi.dto.MrpaRegistryFullXML;
import su.opencode.digsigcik.synapi.dto.MrpaRegistryWithFioDTO;
import su.opencode.digsigcik.synapi.exception.BadRequestException;
import su.opencode.digsigcik.synapi.exception.MchdException;
import su.opencode.digsigcik.synapi.model.Credential;
import su.opencode.digsigcik.synapi.model.MrpaRegistry;
import su.opencode.digsigcik.synapi.repository.*;

import javax.validation.constraints.NotNull;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static su.opencode.digsigcik.synapi.util.Constant.NOT_FOUND;

@Service
@Primary
@RequiredArgsConstructor
public class MrpaRegistryServiceImpl implements MrpaRegistryService {
    private final MrpaRegistryRepository mrpaRegistryRepository;
    private final OrganisationRepository organisationRepository;
    private final PrincipalRepository principalRepository;
    private final RepresentativeRepository representativeRepository;
    private final CredentialRepository credentialRepository;

    @Override
    public List<MrpaRegistryWithFioDTO> findAllMrpaRegistry() {
        return this.mrpaRegistryRepository.findAllMrpaRegistries();
    }

    @Override
    public MrpaRegistry saveMrpaRegistry(@NotNull MrpaRegistry mrpaRegistry) {
        throwExceptionIfRecordWithIdNotExist(mrpaRegistry, "create");
        return this.mrpaRegistryRepository.save(mrpaRegistry);
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
        if (organisationRepository.findOrganisationById(organisationId) == null) {
            throw new BadRequestException(String.format(errorMessage, operationType, "organisation", organisationId));
        }
        if (principalRepository.findPrincipalById(principalId) == null) {
            throw new BadRequestException(String.format(errorMessage, operationType, "principal", principalId));
        }
        if (representativeRepository.findRepresentativeById(representativeId) == null) {
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

        for(Credential c : credential) {
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

        for(Credential c : credential) {
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
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        String idFile = "ON_EMCHD_"+ dateFormatShort.format(date)+"_"+mrpaRegistryFullDTO.getGuid();

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

        if(Objects.isNull(mrpaRegistryFullDTO.getInternalNumber())){
            mrpas.setInternalNumber("б/н"); }
        else mrpas.setInternalNumber(mrpaRegistryFullDTO.getInternalNumber());

        mrpas.setGuid(mrpaRegistryFullDTO.getGuid());

        mrpas.setStartDate(mrpaRegistryFullDTO.getStartDate());
        mrpas.setEndDate(mrpaRegistryFullDTO.getEndDate());
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
        personPrincipal.setTypeCredential("1");
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
        infoIndividual.setBirthDate(mrpaRegistryFullDTO.representative.getBirthDate());
        MrpaRegistryFullXML.Document.PowerAttorneys.InfoRepresentative.Representative.DetailRepresentative.InfoIndividual.FioRepresentative fioRepresentative = new MrpaRegistryFullXML.Document.PowerAttorneys.InfoRepresentative.Representative.DetailRepresentative.InfoIndividual.FioRepresentative();
        infoIndividual.setFioRepresentative(fioRepresentative);
        fioRepresentative.setName(mrpaRegistryFullDTO.representative.getName());
        fioRepresentative.setSurname(mrpaRegistryFullDTO.representative.getSurname());
        fioRepresentative.setPatronymic(mrpaRegistryFullDTO.representative.getPatronymic());
        MrpaRegistryFullXML.Document.PowerAttorneys.InfoRepresentative.Representative.DetailRepresentative.InfoIndividual.RepresentativePassport representativePassport = new MrpaRegistryFullXML.Document.PowerAttorneys.InfoRepresentative.Representative.DetailRepresentative.InfoIndividual.RepresentativePassport();
        infoIndividual.setRepresentativePassport(representativePassport);
        representativePassport.setTypeDocument("21");
        representativePassport.setSeriesNumber(mrpaRegistryFullDTO.representative.representativePassport.getSeriesNumber());
        representativePassport.setDateIssue(mrpaRegistryFullDTO.representative.representativePassport.getDateIssue());
        representativePassport.setWhoIssue(mrpaRegistryFullDTO.representative.representativePassport.getWhoIssue());
        representativePassport.setDepartmentCode(mrpaRegistryFullDTO.representative.representativePassport.getDepartmentCode());
        MrpaRegistryFullXML.Document.PowerAttorneys.Credential credential = new MrpaRegistryFullXML.Document.PowerAttorneys.Credential();
        credential.setTypeCredential("0");
        credential.setImplCredential("1");
        credential.setLossCredential("1");

        List<String> credentialList = new ArrayList<>();
        for(MrpaRegistryFullDTO.CredentialDTO credentialDTO: mrpaRegistryFullDTO.credential){
            credentialList.add(credentialDTO.getName());
        }
        credential.setName(credentialList);
        powerAttorneys.setCredential(credential);

        return mrpaRegistryFullXML;
    }

}

