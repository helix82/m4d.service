package su.opencode.digsigcik.m4d.service.impl;

import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import su.opencode.digsigcik.m4d.dto.M4dRecallXml;
import su.opencode.digsigcik.m4d.dto.M4dResponseDto;
import su.opencode.digsigcik.m4d.dto.response.M4dRevokeResponseDto;
import su.opencode.digsigcik.m4d.mapper.M4dRevokeMapper;
import su.opencode.digsigcik.m4d.model.M4d;
import su.opencode.digsigcik.m4d.model.M4dRecall;
import su.opencode.digsigcik.m4d.repository.M4dRecallRepository;
import su.opencode.digsigcik.m4d.repository.M4dRepository;
import su.opencode.digsigcik.m4d.service.M4dRecallService;
import su.opencode.digsigcik.m4d.service.M4dService;
import su.opencode.digsigcik.m4d.service.UserCertificateService;

import javax.validation.constraints.NotNull;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Primary
public class M4dRecallServiceImpl implements M4dRecallService {
    private final M4dRecallRepository m4dRevokeRepository;
    private final M4dRepository m4dRepository;
    private final M4dRevokeMapper m4dRevokeMapper;
    private final M4dService m4dService;
    private final UserCertificateService userCertificateService;

    public M4dRecallServiceImpl(M4dRecallRepository m4dRevokeRepository, M4dRepository m4dRepository, M4dRevokeMapper m4dRevokeMapper,
                                @Lazy M4dService m4dService, UserCertificateService userCertificateService) {
        this.m4dRevokeRepository = m4dRevokeRepository;
        this.m4dRepository = m4dRepository;
        this.m4dRevokeMapper = m4dRevokeMapper;
        this.m4dService = m4dService;
        this.userCertificateService = userCertificateService;
    }


    @Override
    public List<M4dResponseDto> findAllMrpaRegistriesWithRecall() throws ParseException {
        List<M4d> m4ds = m4dRepository.findAll().stream().filter(c -> c.getMrpaRecall() != null).collect(Collectors.toList());
        List<M4dResponseDto> m4dResponseDtoList = new ArrayList<>();
        for (M4d m4d : m4ds) {
            m4dResponseDtoList.add(m4dService.convertM4dToM4dResponseDto(m4d));
        }
        return m4dResponseDtoList;
    }

    @Override
    public M4d findMrpaRecallById(@NotNull Long id) {
        return this.m4dRepository.findM4dByMrpaRecallId(id);
    }

    public M4dRecall findById(Long id) {
        return this.m4dRevokeRepository.findM4dRecallById(id);
    }

    @Override
    public void deleteMrpaRecallById(Long id) {
        M4d mrpaRegistry = this.m4dRepository.findM4dByMrpaRecallId(id);
        mrpaRegistry.setMrpaRecall(null);
        this.m4dRepository.save(mrpaRegistry);
        this.m4dRevokeRepository.deleteById(id);
    }

    @Override
    public M4dRecall updateMrpaRecall(M4dRecall mrpaRecall) {
        return this.m4dRevokeRepository.save(mrpaRecall);
    }

    @Transactional(readOnly = true)
    @Override
    public List<M4dRevokeResponseDto> findAllM4dRevokeUsers(Authentication authentication) {
        Set<String> setINNs = userCertificateService.getUserCertificateINNs(authentication);

        return m4dRevokeRepository.findListM4dRevokeBySetInn(setINNs).stream()
                .map(m4dRevokeMapper::mapToRevokeResponseDto)
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
