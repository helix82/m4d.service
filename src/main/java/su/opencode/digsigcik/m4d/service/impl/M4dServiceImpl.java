package su.opencode.digsigcik.m4d.service.impl;

import com.nimbusds.jose.proc.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import su.opencode.digsigcik.m4d.dto.*;
import su.opencode.digsigcik.m4d.exception.M4dException;
import su.opencode.digsigcik.m4d.model.*;
import su.opencode.digsigcik.m4d.repository.*;
import su.opencode.digsigcik.m4d.dto.response.*;
import su.opencode.digsigcik.m4d.service.M4dService;

import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.time.Instant.parse;
import static su.opencode.digsigcik.m4d.util.Constants.*;

@Service
@Primary
@RequiredArgsConstructor
@EnableTransactionManagement
public class M4dServiceImpl implements M4dService {
    @Value("${m4d.handbook.uri}")
    private String HANDBOOK_URI;
    private final ModelMapper modelMapper;
    private final M4dRepository m4dRepository;
    private final M4dRecallRepository m4dRecallRepository;
    private final M4dCredentialRepository m4dCredentialRepository;
    private final RestTemplate restTemplate;

    @Override
    public List<M4dResponseDto> findAllMrpaRegistry() throws ParseException {
        List<M4d> m4ds = m4dRepository.findAll();
        List<M4dResponseDto> m4dResponseDtoList = new ArrayList<>();
        for (M4d m4d: m4ds) {
            m4dResponseDtoList.add(convertM4dToM4dResponseDto(m4d));
        }
        return m4dResponseDtoList;
    }

    @Override
    public M4d saveMrpaRegistry(@NotNull M4d m4d) {
        return this.m4dRepository.save(m4d);
    }

    @Override
    //@AuditLogThis(event = AuditLogEventEnum.M4D_RECALL_CREATE)
    public M4d updateMrpaRegistry(@NotNull M4d mrpaRegistry) {
        return this.m4dRepository.save(mrpaRegistry);
    }

    @Transactional(rollbackFor = {Exception.class}, isolation = Isolation.READ_UNCOMMITTED)
    public ResponseEntity<?> saveAllDataMrpaRegistry(M4d m4d, OrganisationResponse organisation, PrincipalResponse principal, RepresentativeResponse representative, RepresentativePassportResponse representativePassport, List<CredentialResponse> credentialList, List<Long> credentials) {
        Long orgId;
        Long principId;
        Long represId;
        //------------------- save organisation ----------------
        OrganisationResponse existsOrganisation = (restTemplate.getForEntity(HANDBOOK_URI+"/organisation/inn/"+organisation.getInn(), OrganisationResponse.class)).getBody();
        assert existsOrganisation != null;
        if (existsOrganisation.getId() == null) {
            OrganisationResponse respOrganisation;
            try {
                respOrganisation = restTemplate.postForObject(HANDBOOK_URI+"/organisation/save", organisation, OrganisationResponse.class);
            } catch (ConstraintViolationException e) {
                return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
            }
            assert respOrganisation != null;
            orgId = respOrganisation.getId();
        } else {
            orgId = existsOrganisation.getId();
        }
        //------------------- end save organisation ----------------

        //------------------- save principal ----------------
        PrincipalResponse existsPrincipal = (restTemplate.getForEntity(HANDBOOK_URI+"/principal/inn/"+principal.getInn(), PrincipalResponse.class)).getBody();
        assert existsPrincipal != null;
        if (existsPrincipal.getId() == null) {
            PrincipalResponse respPrincipal;
            try {
                respPrincipal = restTemplate.postForObject(HANDBOOK_URI+"/principal/save", principal, PrincipalResponse.class);
            } catch (ConstraintViolationException e) {
                return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
            }
            assert respPrincipal != null;
            principId = respPrincipal.getId();
        } else {
            principId = existsPrincipal.getId();
        }
        //------------------- end save principal ----------------

        //------------------- save representative and passport ----------------
        RepresentativeResponse existsRepresentative = (restTemplate.getForEntity(HANDBOOK_URI+"/representative/inn/"+representative.getInn(), RepresentativeResponse.class)).getBody();
        assert existsRepresentative != null;
        if (existsRepresentative.getId() == null) {
            RepresentativeResponse respRepresentative;
            try {
                respRepresentative = restTemplate.postForObject(HANDBOOK_URI+"/representative/save", representative, RepresentativeResponse.class);
            } catch (ConstraintViolationException e) {
                return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
            }
            assert respRepresentative != null;
            if (respRepresentative.getId() != null) {
                representativePassport.setRepresentativeId(respRepresentative.getId());
                try {
                    restTemplate.postForObject(HANDBOOK_URI+"/representative_passport/save", representativePassport, RepresentativePassportResponse.class);
                } catch (ConstraintViolationException e) {
                    return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
                }
            }

            represId = respRepresentative.getId();
        } else {
            represId = existsRepresentative.getId();
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
            for (CredentialResponse c : credentialList) {
                CredentialResponse[] existsCredentials = (restTemplate.getForEntity(HANDBOOK_URI+"/credential/find_by_code_or_name/"+c.getCode(), CredentialResponse[].class)).getBody();
                assert existsCredentials != null;
                if (existsCredentials.length == 0) {
                    CredentialResponse newCredential = new CredentialResponse();
                    newCredential.setCode(c.getCode());
                    newCredential.setName(c.getName());
                    CredentialResponse credentialSave;
                    try {
                        credentialSave = restTemplate.postForObject(HANDBOOK_URI+"/credential/save", newCredential, CredentialResponse.class);
                    } catch (ConstraintViolationException e) {
                        return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
                    }
                    assert credentialSave != null;
                    credList.add(credentialSave.getId());
                } else {
                    for (CredentialResponse crd : existsCredentials) {
                        credList.add(crd.getId());
                    }
                }
            }
        } else if(credentials != null) {
            credList = credentials;
        }

        if (!credList.isEmpty()) {
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

    @Transactional(rollbackFor = {Exception.class}, isolation = Isolation.READ_UNCOMMITTED)
    public ResponseEntity<?> updateAllDataMrpaRegistry(M4d m4d, OrganisationResponse organisation, PrincipalResponse principal, RepresentativeResponse representative, RepresentativePassportResponse representativePassport, List<CredentialResponse> credentialList, List<Long> credentials) {
        Long orgId;
        Long principId;
        Long represId;
        if (m4d.getId() == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString("Can`t update mrpa registry because id null!"), HttpStatus.BAD_REQUEST);
        }
        M4d existMrpaRegistry = this.findById(m4d.getId());
        if (existMrpaRegistry == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(String.format("Can`t update. Mrpa registry with id=%d %s", m4d.getId(), NOT_FOUND)), HttpStatus.NOT_FOUND);
        }
        //------------------- save organisation ----------------
        OrganisationResponse existsOrganisation = (restTemplate.getForEntity(HANDBOOK_URI+"/organisation/inn/"+organisation.getInn(), OrganisationResponse.class)).getBody();
        if (existsOrganisation != null) {
            organisation.setId(existsOrganisation.getId());
        }
        assert existsOrganisation != null;
        if(!existsOrganisation.equals(organisation)){
            OrganisationResponse respOrganisation;
            try {
                respOrganisation = restTemplate.postForObject(HANDBOOK_URI+"/organisation/save", organisation, OrganisationResponse.class);
            } catch (ConstraintViolationException e) {
                return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
            }
            assert respOrganisation != null;
            orgId = respOrganisation.getId();
        } else {
            orgId = existsOrganisation.getId();
        }


        //------------------- end save organisation ----------------

        //------------------- save principal ----------------
        PrincipalResponse existsPrincipal = (restTemplate.getForEntity(HANDBOOK_URI+"/principal/inn/"+principal.getInn(), PrincipalResponse.class)).getBody();
        if (existsPrincipal != null) {
            principal.setId(existsPrincipal.getId());
        }
        PrincipalResponse respPrincipal;
        try {
            respPrincipal = restTemplate.postForObject(HANDBOOK_URI+"/principal/save", principal, PrincipalResponse.class);
        } catch (ConstraintViolationException e) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
        assert respPrincipal != null;
        principId = respPrincipal.getId();
        //------------------- end save principal ----------------

        //------------------- save representative and passport ----------------

        RepresentativeResponse existsRepresentative = (restTemplate.getForEntity(HANDBOOK_URI+"/representative/inn/"+representative.getInn(), RepresentativeResponse.class)).getBody();
        if (existsRepresentative != null) {
            representative.setId(existsRepresentative.getId());
        }
        RepresentativeResponse respRepresentative;
        try {

            respRepresentative = restTemplate.postForObject(HANDBOOK_URI+"/representative/save", representative, RepresentativeResponse.class);
        } catch (ConstraintViolationException e) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
        }

        RepresentativePassportResponse existsRepresentativePassport = (restTemplate.getForEntity(HANDBOOK_URI+"/representative_passport/seriesNumber/"+representativePassport.getSeriesNumber(), RepresentativePassportResponse.class)).getBody();
        if (existsRepresentativePassport != null) {
            representativePassport.setId(existsRepresentativePassport.getId());
        }
        try {
            assert respRepresentative != null;
            representativePassport.setRepresentativeId(respRepresentative.getId());
            restTemplate.postForObject(HANDBOOK_URI+"/representative_passport/save", representativePassport, RepresentativePassportResponse.class);
        } catch (ConstraintViolationException e) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
        represId = respRepresentative.getId();
        //------------------- end save representative and passport ----------------

        if (orgId == null || principId == null || represId == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(MRPA_REGISTRY_NOT_FULL), HttpStatus.BAD_REQUEST);
        }
        if(!m4d.getOrganisationId().equals(orgId)){
            m4d.setOrganisationId(orgId);
        }
        if(!m4d.getPrincipalId().equals(principId)) {
            m4d.setPrincipalId(principId);
        }
        if(!m4d.getRepresentativeId().equals(represId)) {
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
            for (CredentialResponse c : credentialList) {
                CredentialResponse[] existsCredentials = (restTemplate.getForEntity(HANDBOOK_URI+"/credential/find_by_code_or_name/"+c.getCode(), CredentialResponse[].class)).getBody();
                assert existsCredentials != null;
                if (existsCredentials.length == 0) {
                    CredentialResponse newCredential = new CredentialResponse();
                    newCredential.setCode(c.getCode());
                    newCredential.setName(c.getName());
                    CredentialResponse credentialSave;
                    try {
                        credentialSave = restTemplate.postForObject(HANDBOOK_URI+"/credential/save", newCredential, CredentialResponse.class);
                    } catch (ConstraintViolationException e) {
                        return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
                    }
                    assert credentialSave != null;
                    credList.add(credentialSave.getId());
                } else {
                    for (CredentialResponse crd : existsCredentials) {
                        credList.add(crd.getId());
                    }
                }
            }
        } else if (credentials != null) {
            credList = credentials;
        }

        if(!credList.isEmpty()){
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
    public Integer findMrpaRegistryByGuid(@NotNull String guid) {
        return this.m4dRepository.countByGuid(guid);
    }

    @Override
    public M4d findById(@NotNull Long id) {
        return this.m4dRepository.findM4dById(id);
    }

    @Override
    public void deleteMrpaRegistry(@NotNull Long id) {
        this.m4dRepository.deleteById(id);
    }

    @Override
    public List<M4dResponseDto.CredentialDTO> getCredentialByMrpaRegistryId(Long mrpaId) {
        CredentialResponse[] credentials = (restTemplate.getForEntity(HANDBOOK_URI+"/credential/mrpaId/"+mrpaId, CredentialResponse[].class)).getBody();
        List<M4dResponseDto.CredentialDTO> credentialDTOList = new ArrayList<>();
        assert credentials != null;
        for (CredentialResponse c : credentials) {
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
        String idFile = "ON_EMCHD_" + convertDateIsoInstantToMchdXmlNotDash(m4dFullDTO.getCreateDate()) + "_" + m4dFullDTO.getGuid();
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
            mrpas.setStartDate(convertDateIsoInstantToMchdXml(m4dFullDTO.getStartDate()));
        }
        if (m4dFullDTO.getEndDate() != null) {
            mrpas.setEndDate(convertDateIsoInstantToMchdXml(m4dFullDTO.getEndDate()));
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
            infoIndividual.setBirthDate(convertDateIsoInstantToMchdXml(m4dFullDTO.representative.getBirthDate()));
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
            representativePassport.setDateIssue(convertDateIsoInstantToMchdXml(m4dFullDTO.representative.representativePassport.getDateIssue()));
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

    private static String convertDateIsoInstantToMchdXml(String date) {
        Date myDate = Date.from(parse(date));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = formatter.format(myDate);
        return formattedDate;
    }

    private static String convertDateIsoInstantToMchdXmlNotDash(String date) {
        Date myDate = Date.from(parse(date));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC")); //GMT
        String formattedDate = formatter.format(myDate);
        return formattedDate;
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
    public List<M4dResponseDto> findAllMrpaRegistryByLogin(String login) throws ParseException {
        List<M4dResponseDto> m4dResponseDtoList = findAllMrpaRegistry();
        List<PublicKeyUserCertificateResponse> publicKeyUserCertificateResponse = List.of(Objects.requireNonNull((restTemplate.getForEntity(HANDBOOK_URI + "/user_certificate/me", PublicKeyUserCertificateResponse[].class)).getBody()));
        System.out.println(publicKeyUserCertificateResponse.stream().map(PublicKeyUserCertificateResponse::getInn).collect(Collectors.toList()));
//m4dResponseDtoList.stream().filter(c -> Objects.equals(c.representative.getInn(), "1234") && Objects.equals(c.principal.getInn(), "123")).collect(Collectors.toList());
//return Stream.of(Objects.requireNonNull((restTemplate.getForEntity(HANDBOOK_URI+"/user_certificate/me", PublicKeyUserCertificateResponse[].class)).getBody())).filter(Objects::nonNull)
//                .map(PublicKeyUserCertificateResponse::getInn)
//                .flatMap(inn -> m4dRepository.findMrpaRegistriesByPrincipalInnLikePrincipalAndRepresentative(inn).stream().map(
//                        results -> M4dResponseDto.fill((String) results.get("id"), (String) results.get("guid"), (String) results.get("internal_number"), (String) results.get("start_date"), (String) results.get("end_date"),
//                                (String) results.get("create_date"), (String) results.get("p_id"), (String) results.get("p_inn"), (String) results.get("p_snils"), (String) results.get("p_job_title"), (String) results.get("p_name"), (String) results.get("p_surname"), (String) results.get("p_patronymic"),
//                                (String) results.get("r_id"), (String) results.get("r_inn"), (String) results.get("r_snils"), (String) results.get("r_birth_date"), (String) results.get("r_name"), (String) results.get("r_surname"), (String) results.get("r_patronymic")
//                        )
//                ))
//                .distinct()
//                .collect(Collectors.toList());
        return Stream.of(Objects.requireNonNull((restTemplate.getForEntity(HANDBOOK_URI+"/user_certificate/me", PublicKeyUserCertificateResponse[].class)).getBody())).filter(Objects::nonNull)
                .map(PublicKeyUserCertificateResponse::getInn)
                .flatMap(inn -> m4dResponseDtoList.stream().filter(c -> Objects.equals(c.representative.getInn(), inn) && Objects.equals(c.principal.getInn(), inn)))
                .distinct()
                .collect(Collectors.toList());
    }

    public Map<String, String> getCustomErrorJsonFromString(String text) {
        Map<String, String> body = new HashMap<>();
        body.put("error", text);
        return body;
    }

    public String getStatusM4d(Long id) throws ParseException {
        M4d mrpaRegistry = this.m4dRepository.findM4dById(id);
        Date currentDT = Date.from(Instant.now());
        Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse(mrpaRegistry.getStartDate());
        Date endDate = new SimpleDateFormat("yyyy-MM-dd").parse(mrpaRegistry.getEndDate());
        if (currentDT.before(startDate) && currentDT.after(endDate)) {
            return "EXPIRED";
        }
        if (mrpaRegistry.getMrpaRecall() != null) {
            M4dRecall mrpaRecall = this.m4dRecallRepository.findM4dRecallById(mrpaRegistry.getMrpaRecall().getId());
            if (mrpaRecall.getSignature() != null) {
                return "WITHDRAWN";
            } else {
                return "WITHDRAWAL";
            }
        } else {
            if (mrpaRegistry.getSigPath() != null) {
                return "VALID";
            } else {
                return "NOT_SIGNED";
            }
        }
    }

    public M4dResponseDto convertM4dToM4dResponseDto(M4d m4d) throws ParseException {
        M4dResponseDto m4dResponseDto = modelMapper.map(m4d, M4dResponseDto.class);
        OrganisationResponse organisation = (restTemplate.getForEntity(HANDBOOK_URI+"/organisation/"+m4d.getOrganisationId(), OrganisationResponse.class)).getBody();
        if (organisation != null) {
            m4dResponseDto.setOrganization(convertOrganisationToDto(organisation));
        }
        PrincipalResponse principal = (restTemplate.getForEntity(HANDBOOK_URI+"/principal/"+m4d.getPrincipalId(), PrincipalResponse.class)).getBody();
        if (principal != null) {
            m4dResponseDto.setPrincipal(convertPrincipalToDto(principal));
        }
        RepresentativeResponse representative = (restTemplate.getForEntity(HANDBOOK_URI+"/representative/"+m4d.getRepresentativeId(), RepresentativeResponse.class)).getBody();
        if (representative != null) {
            m4dResponseDto.setRepresentative(convertRepresentativeToDto(representative));
            m4dResponseDto.representative.setRepresentativePassport(convertRepresentativePassportToDto(representative.getRepresentativePassports().get(0)));
        }
        m4dResponseDto.setCredentials(this.getCredentialByMrpaRegistryId(m4d.getId()));
        m4dResponseDto.setStatus(this.getStatusM4d(m4dResponseDto.getId()));
        return m4dResponseDto;
    }

    private M4dResponseDto.OrganizationDTO convertOrganisationToDto(OrganisationResponse organisation){
        M4dResponseDto.OrganizationDTO organisationDto = modelMapper.map(organisation, M4dResponseDto.OrganizationDTO.class);
        return organisationDto;
    }

    private M4dResponseDto.PrincipalDTO convertPrincipalToDto(PrincipalResponse principal){
        M4dResponseDto.PrincipalDTO principalDto = modelMapper.map(principal, M4dResponseDto.PrincipalDTO.class);
        return principalDto;
    }

    private M4dResponseDto.RepresentativeDTO convertRepresentativeToDto(RepresentativeResponse representative){
        M4dResponseDto.RepresentativeDTO representativeDto = modelMapper.map(representative, M4dResponseDto.RepresentativeDTO.class);
        representativeDto.representativePassport = modelMapper.map(representative.getRepresentativePassports(), M4dResponseDto.RepresentativePassportDTO.class);
        return representativeDto;
    }

    private M4dResponseDto.RepresentativePassportDTO convertRepresentativePassportToDto(RepresentativePassportResponse representativePassport){
        M4dResponseDto.RepresentativePassportDTO representativePassportDto = modelMapper.map(representativePassport, M4dResponseDto.RepresentativePassportDTO.class);
        return representativePassportDto;
    }
}

