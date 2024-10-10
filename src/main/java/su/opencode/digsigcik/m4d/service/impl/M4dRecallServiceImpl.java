package su.opencode.digsigcik.m4d.service.impl;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import su.opencode.digsigcik.m4d.dto.M4dRecallXml;
import su.opencode.digsigcik.m4d.dto.M4dResponseDto;
import su.opencode.digsigcik.m4d.dto.response.*;
import su.opencode.digsigcik.m4d.exception.M4dException;
import su.opencode.digsigcik.m4d.model.M4d;
import su.opencode.digsigcik.m4d.model.M4dRecall;
import su.opencode.digsigcik.m4d.repository.M4dRecallRepository;
import su.opencode.digsigcik.m4d.repository.M4dRepository;
import su.opencode.digsigcik.m4d.service.M4dRecallService;
import su.opencode.digsigcik.m4d.service.M4dService;

import javax.validation.constraints.NotNull;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static su.opencode.digsigcik.m4d.util.Constants.NOT_FOUND;

@Service
@Primary
public class M4dRecallServiceImpl implements M4dRecallService {
    @Value("${m4d.handbook.uri}")
    private String HANDBOOK_URI;
    private final M4dRecallRepository mrpaRecallRepository;
    private final M4dRepository m4dRepository;
    private final RestTemplate restTemplate;
    private final M4dService m4dService;

    public M4dRecallServiceImpl(M4dRecallRepository mrpaRecallRepository, M4dRepository m4dRepository, RestTemplate restTemplate, @Lazy M4dService m4dService) {
        this.mrpaRecallRepository = mrpaRecallRepository;
        this.m4dRepository = m4dRepository;
        this.restTemplate = restTemplate;
        this.m4dService = m4dService;
    }

    @Override
    public List<M4dResponseDto> findAllMrpaRegistriesWithRecall() throws ParseException {
        List<M4d> m4ds = m4dRepository.findAll().stream().filter(c->c.getMrpaRecall() != null).collect(Collectors.toList());
        List<M4dResponseDto> m4dResponseDtoList = new ArrayList<>();
        for (M4d m4d: m4ds) {
            m4dResponseDtoList.add(m4dService.convertM4dToM4dResponseDto(m4d, false, false));
        }
        return m4dResponseDtoList;
    }

    @Override
    public M4d findMrpaRecallById(@NotNull Long id) {
        return this.m4dRepository.findM4dByMrpaRecallId(id);
    }

    public M4dRecall findById(Long id) {
        return this.mrpaRecallRepository.findM4dRecallById(id);
    }

    @Override
    public void deleteMrpaRecallById(Long id) {
        M4d mrpaRegistry = this.m4dRepository.findM4dByMrpaRecallId(id);
        mrpaRegistry.setMrpaRecall(null);
        this.m4dRepository.save(mrpaRegistry);
        this.mrpaRecallRepository.deleteById(id);
    }

    @Override
    public M4dRecall updateMrpaRecall(M4dRecall mrpaRecall) {
        return this.mrpaRecallRepository.save(mrpaRecall);
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
    public List<M4dResponseDto> findAllMrpaRecallByLogin(String login) throws ParseException {
        List<M4dResponseDto> m4dResponseDtoList = this.findAllMrpaRegistriesWithRecall();
        return Stream.of(Objects.requireNonNull((restTemplate.getForEntity(HANDBOOK_URI+"/user_certificate/me", PublicKeyUserCertificateResponse[].class)).getBody())).filter(Objects::nonNull)
                .map(PublicKeyUserCertificateResponse::getInn)
                .flatMap(inn -> m4dResponseDtoList.stream().filter(c -> Objects.equals(c.representative.getInn(), inn) && Objects.equals(c.principal.getInn(), inn)))
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public M4dRecallXml ConvertMrpaRecallToXml(M4dResponseDto mrpaRegistryResponseDto) {
        M4dRecallXml mrpaRecallXml = new M4dRecallXml();
        mrpaRecallXml.setXmlnsXsi("http://www.w3.org/2001/XMLSchema-instance");
        mrpaRecallXml.setXmlnsXsd("http://www.w3.org/2001/XMLSchema");
        mrpaRecallXml.setVersionFormat("001");

        M4dRecallXml.Document.RecallInfo recallInfo = new M4dRecallXml.Document.RecallInfo();
        recallInfo.setGuid(mrpaRegistryResponseDto.getGuid());
        recallInfo.setArgument(mrpaRegistryResponseDto.getRecall().getArgument());

        M4dRecallXml.Document document = new M4dRecallXml.Document();
        mrpaRecallXml.setDocument(document);
        document.setRecallInfo(recallInfo);

        M4dRecallXml.Document.RecallWho recallWho = new M4dRecallXml.Document.RecallWho();
        document.setRecallWho(recallWho);

        if (mrpaRegistryResponseDto.recall.getAuthor().equals("PRINCIPAL")) {
            M4dRecallXml.Document.RecallWho.Principal principal = new M4dRecallXml.Document.RecallWho.Principal();
            recallWho.setPrincipal(principal);
            M4dRecallXml.Document.RecallWho.Principal.LegalEntity legalEntity = new M4dRecallXml.Document.RecallWho.Principal.LegalEntity();
            legalEntity.setName(mrpaRegistryResponseDto.organization.getName());
            legalEntity.setInn(mrpaRegistryResponseDto.organization.getInn());
            legalEntity.setKpp(mrpaRegistryResponseDto.organization.getKpp());
            legalEntity.setOgrn(mrpaRegistryResponseDto.organization.getOgrn());
            principal.setLegalEntity(legalEntity);
        } else if (mrpaRegistryResponseDto.recall.getAuthor().equals("REPRESENTATIVE")) {
            M4dRecallXml.Document.RecallWho.Representative representative = new M4dRecallXml.Document.RecallWho.Representative();
            recallWho.setRepresentative(representative);
            M4dRecallXml.Document.RecallWho.Representative.DetailRepresentative detailRepresentative = new M4dRecallXml.Document.RecallWho.Representative.DetailRepresentative();
            detailRepresentative.setInn(mrpaRegistryResponseDto.representative.getInn());
            detailRepresentative.setSnils(mrpaRegistryResponseDto.representative.getSnils());
            M4dRecallXml.Document.RecallWho.Representative.DetailRepresentative.FioRepresentative fioRepresentative = new M4dRecallXml.Document.RecallWho.Representative.DetailRepresentative.FioRepresentative();
            fioRepresentative.setName(mrpaRegistryResponseDto.representative.getName());
            fioRepresentative.setSurname(mrpaRegistryResponseDto.representative.getSurname());
            fioRepresentative.setPatronymic(mrpaRegistryResponseDto.representative.getPatronymic());
            representative.setDetailRepresentative(detailRepresentative);
            detailRepresentative.setFioRepresentative(fioRepresentative);
        }

        return mrpaRecallXml;
    }
}
