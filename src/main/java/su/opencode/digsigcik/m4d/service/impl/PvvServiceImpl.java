package su.opencode.digsigcik.m4d.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.common.exceptions.exception.custom.BadRequestException;
import su.opencode.digsigcik.m4d.dto.CheckByGuidRequestDto;
import su.opencode.digsigcik.m4d.model.M4d;
import su.opencode.digsigcik.m4d.model.M4dConfirm;
import su.opencode.digsigcik.m4d.model.M4dRecall;
import su.opencode.digsigcik.m4d.model.M4dRecallConfirm;
import su.opencode.digsigcik.m4d.service.M4dFileService;
import su.opencode.digsigcik.m4d.service.PvvService;
import su.opencode.digsigcik.m4d.util.CustomMultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static su.opencode.digsigcik.m4d.util.Constants.MRPA_RECALL_IS_NULL;
import static su.opencode.digsigcik.m4d.util.Constants.ZIP_CREATE_ERROR;

@Slf4j
@Service
@Primary
@RequiredArgsConstructor
@EnableTransactionManagement
public class PvvServiceImpl implements PvvService {
    private final M4dFileService m4dFileService;
    private final RestTemplate restTemplate;

    @Value("${pvv.fns-send-registration-request-m4d}")
    private URI FNS_SEND_REGISTRATION_REQUEST_M4D_URI;
    @Value("${pvv.fns-send-revoke-request-m4d}")
    private URI FNS_SEND_REVOKE_REQUEST_M4D_URI;
    @Value("${pvv.fns-send-status-request-m4d}")
    private URI FNS_SEND_STATUS_REQUEST_M4D_URI;

    @Override
    @Transactional(rollbackFor = {Exception.class}, isolation = Isolation.READ_UNCOMMITTED)
    public String sendM4dToRR(Authentication authentication, M4d m4d, M4dConfirm m4dConfirm) throws IOException {
        String filenameSource = m4d.getXmlFile().getFilename();
        int lastDotIndex = filenameSource.lastIndexOf('.');
        if (lastDotIndex > 0) {
            filenameSource = filenameSource.substring(0, lastDotIndex);
        }
        List<Long> longList = new ArrayList<>();
        longList.add(m4d.getXmlFile().getId());
        if (m4d.getSigFile()!=null) { longList.add(m4d.getSigFile().getId()); }
        if (m4dConfirm!=null) {
            if (m4dConfirm.getXmlFileRegisteredPep() != null) { longList.add(m4dConfirm.getXmlFileRegisteredPep().getId()); }
            if (m4dConfirm.getSigFileRegisteredPep() != null) { longList.add(m4dConfirm.getSigFileRegisteredPep().getId()); }
        }
        File resp = this.m4dFileService.createArchive(authentication, filenameSource, longList);
        if (resp == null) {
            throw new BadRequestException(ZIP_CREATE_ERROR);
        }

        MultipartFile requestTempFile = new CustomMultipartFile(resp);
        String res = registrationRequestM4dFnsViaPvv(requestTempFile, m4d.getGuid(), FNS_SEND_REGISTRATION_REQUEST_M4D_URI);
        log.info(res);
        return res;
    }

    @Override
    @Transactional(rollbackFor = {Exception.class}, isolation = Isolation.READ_UNCOMMITTED)
    public String sendM4dRevokeToRR(Authentication authentication, M4dRecall m4dRecall, M4dRecallConfirm m4dRecallConfirm) throws IOException {
        if (m4dRecall == null) {
            throw new BadRequestException(MRPA_RECALL_IS_NULL);
        }
        String filenameSource = m4dRecall.getXmlFile().getFilename();
        int lastDotIndex = filenameSource.lastIndexOf('.');
        if (lastDotIndex > 0) {
            filenameSource = filenameSource.substring(0, lastDotIndex);
        }
        List<Long> longList = new ArrayList<>();
        longList.add(m4dRecall.getXmlFile().getId());
        if (m4dRecall.getSigFile()!=null) { longList.add(m4dRecall.getSigFile().getId()); }
        if (m4dRecallConfirm!=null) {
            if (m4dRecallConfirm.getXmlFileRegisteredPep() != null) { longList.add(m4dRecallConfirm.getXmlFileRegisteredPep().getId()); }
            if (m4dRecallConfirm.getSigFileRegisteredPep() != null) { longList.add(m4dRecallConfirm.getSigFileRegisteredPep().getId()); }
        }
        File resp = this.m4dFileService.createArchive(authentication, filenameSource, longList);
        if (resp == null) {
            throw new BadRequestException(ZIP_CREATE_ERROR);
        }
        MultipartFile requestTempFile = new CustomMultipartFile(resp);
        String res = registrationRequestM4dFnsViaPvv(requestTempFile, m4dRecall.getM4d().getGuid(), FNS_SEND_REVOKE_REQUEST_M4D_URI);
        log.info(res);
        return res;
    }

    public String sendRequestStatusM4dToRR(String guid) {
        String res = sendRequestM4dFnsViaPvv(guid, FNS_SEND_STATUS_REQUEST_M4D_URI);
        log.info(res);
        return res;
    }

    public String sendRequestStatusM4dToRRByGuidAndInns(CheckByGuidRequestDto requestDto) {
        String res = sendRequestFullM4dFnsViaPvv(requestDto, FNS_SEND_STATUS_REQUEST_M4D_URI);
        log.info(res);
        return res;
    }

    private String registrationRequestM4dFnsViaPvv(MultipartFile file, String guid, URI uri) {
        log.info("Отправка {} в ПВВ (МЧД/Отзыв МЧД): {}...", guid, uri);

        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add("common-id", guid);
        headers.add("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE);
        try {
            return restTemplate.postForEntity(
                            uri,
                            new HttpEntity<>(file.getBytes(), headers),
                            String.class
                    )
                    .getBody();
        } catch (Exception ex) {
            log.error("Ошибка при отправке запроса в ПВВ: {}", ex.getMessage());
        }
        return null;
    }

    private String sendRequestM4dFnsViaPvv(String guid, URI uri) {
        String body = "{\"guid\":\"" + guid + "\"}";
        log.debug("Отправка запроса на получение статуса МЧД {} в ПВВ: {}...", guid, uri);
        try {
            MultiValueMap<String, String> headers = new HttpHeaders();
            headers.add("common-id", guid);
            headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);

            HttpEntity<String> entity = new HttpEntity<>(body, headers);
            return restTemplate.postForEntity(
                            uri,
                            entity,
                            String.class
                    )
                    .getBody();
        } catch (Exception ex) {
            log.error("Ошибка при отправке запроса на получение статуса МЧД в ПВВ: {}", ex.getMessage());
        }
        return null;
    }

    private String sendRequestFullM4dFnsViaPvv(CheckByGuidRequestDto requestDto, URI uri) {
        String body = requestDto.toString();
        log.debug("Отправка запроса на получение статуса МЧД {} в ПВВ: {}...", body, uri);
        try {
            MultiValueMap<String, String> headers = new HttpHeaders();
            headers.add("common-id", requestDto.getGuid());
            headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);

            HttpEntity<String> entity = new HttpEntity<>(body, headers);
            return restTemplate.postForEntity(
                            uri,
                            entity,
                            String.class
                    )
                    .getBody();
        } catch (Exception ex) {
            log.error("Ошибка при отправке запроса на получение статуса МЧД в ПВВ: {}", ex.getMessage());
        }
        return null;
    }

}
