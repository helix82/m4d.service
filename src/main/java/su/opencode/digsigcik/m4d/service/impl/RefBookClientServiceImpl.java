package su.opencode.digsigcik.m4d.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import su.opencode.digsigcik.m4d.dto.OrganizationDto;
import su.opencode.digsigcik.m4d.exception.BadRequestException;
import su.opencode.digsigcik.m4d.service.RefBookClientService;
import su.opencode.digsigcik.m4d.service.RestTemplateService;

import javax.validation.constraints.NotNull;
import java.util.Optional;

import static su.opencode.digsigcik.m4d.util.Constants.ORGANISATION_READ_ERROR;
import static su.opencode.digsigcik.m4d.util.Constants.ORGANISATION_SAVE_ERROR;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefBookClientServiceImpl implements RefBookClientService {
    @Value("${ref-book.organization-create.uri}")
    private String ORGANIZATION_CREATE_URI;
    @Value("${ref-book.organization-find-by-inn.uri}")
    private String ORGANIZATION_FIND_BY_INN_URI;
    private final RestTemplateService restTemplateService;

    @Override
    public OrganizationDto saveOrganization(@NotNull OrganizationDto organizationDto, String bearerToken) {
        log.info("Отправка запроса на сохранение организации в БД");
        try {
            ResponseEntity<OrganizationDto> responseEntity =
                    restTemplateService.fetchServiceResponse(
                            ORGANIZATION_CREATE_URI,
                            OrganizationDto.class,
                            bearerToken,
                            HttpMethod.POST,
                            organizationDto
                    );

            return Optional.of(responseEntity)
                    .filter(ResponseEntity::hasBody)
                    .map(ResponseEntity::getBody)
                    .orElseThrow(() -> new BadRequestException(ORGANISATION_SAVE_ERROR));

        } catch (HttpClientErrorException ex) {
            throw new BadRequestException(ORGANISATION_SAVE_ERROR);
        }
    }

    @Override
    public OrganizationDto findByOrganisationInn(String inn, String bearerToken) {
        log.info("Отправка запроса на получение организации по ИНН");
        try {
            ResponseEntity<OrganizationDto> responseEntity =
                    restTemplateService.fetchServiceResponse(
                            String.format(ORGANIZATION_FIND_BY_INN_URI+"/"+inn),
                            OrganizationDto.class,
                            bearerToken,
                            HttpMethod.GET
                    );

            return Optional.of(responseEntity)
                    .filter(ResponseEntity::hasBody)
                    .map(ResponseEntity::getBody)
                    .orElseThrow(() -> new BadRequestException(ORGANISATION_READ_ERROR));

        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode().value() == 400) {
                return null;
            } else
                throw new BadRequestException(ORGANISATION_READ_ERROR);
        }
    }
}
