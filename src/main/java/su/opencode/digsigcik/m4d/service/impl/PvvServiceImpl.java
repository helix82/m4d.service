package su.opencode.digsigcik.m4d.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.common.exception.custom.BadRequestException;
import su.opencode.digsigcik.m4d.dto.response.M4dResponseInfo;
import su.opencode.digsigcik.m4d.dto.response.M4dRevokeResponseInfo;
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
    @Value("${pvv.fns-receive-response-m4d}")
    private URI FNS_RECEIVE_RESPONSE_M4D_URI;
    @Value("${pvv.fns-receive-json-response-m4d}")
    private URI FNS_RECEIVE_JSON_RESPONSE_M4D_URI;

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
        String res = registrationRequestM4dFnsViaPvv(requestTempFile, FNS_SEND_REGISTRATION_REQUEST_M4D_URI);
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
        String res = registrationRequestM4dFnsViaPvv(requestTempFile, FNS_SEND_REVOKE_REQUEST_M4D_URI);
        log.info(res);
        return res;
    }

    public String sendRequestStatusM4dToRR(String guid) {
        String res = sendRequestM4dFnsViaPvv(guid, FNS_SEND_STATUS_REQUEST_M4D_URI);
        log.info(res);
        return res;
    }

    private String registrationRequestM4dFnsViaPvv(MultipartFile file, URI uri) {
        try {
            log.debug("Отправка файла {}...", file);
            return restTemplate.postForEntity(
                            uri,
                            new HttpEntity<>(file.getBytes()),
                            String.class
                    )
                    .getBody();
        } catch (Exception ex) {
            log.error("Ошибка при отправке файла для межконтурного обновления: {} по адресу '{}'", ex.getMessage(),
                    uri);
        }
        return null;
    }

    private String sendRequestM4dFnsViaPvv(String guid, URI uri) {
        String body = "{\"guid\":\"" + guid + "\"}";
        try {
            log.debug("Отправка запроса на получение статуса МЧД {}...", guid);
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            headers.setContentType(MediaType.APPLICATION_JSON); // если тело JSON

            HttpEntity<String> entity = new HttpEntity<>(body, headers);
            return restTemplate.postForEntity(
                            uri,
                            entity,
                            String.class
                    )
                    .getBody();
        } catch (Exception ex) {
            log.error("Ошибка при отправке запроса на получение статуса МЧД для межконтурного обновления: {} по адресу '{}'", ex.getMessage(),
                    uri);
        }
        return null;
    }

    public ByteArrayResource receiveResponseM4dToRR(String messageId) {
        ByteArrayResource res = receiveResponseFnsViaPvv(messageId, FNS_RECEIVE_RESPONSE_M4D_URI);
        log.info(messageId);
        log.info(String.valueOf(res));
        return res;
    }

    public M4dResponseInfo receiveJsonResponseM4dToRR(String messageId) {
        M4dResponseInfo res = receiveJsonResponseM4dFnsViaPvv(messageId, FNS_RECEIVE_JSON_RESPONSE_M4D_URI);
        log.info(messageId);
        log.info(String.valueOf(res));
        return res;
    }

    public M4dRevokeResponseInfo receiveJsonResponseM4dRevokeToRR(String messageId) {
        M4dRevokeResponseInfo res = receiveJsonResponseM4dRevokeFnsViaPvv(messageId, FNS_RECEIVE_JSON_RESPONSE_M4D_URI);
        log.info(messageId);
        log.info(String.valueOf(res));
        return res;
    }

    private ByteArrayResource receiveResponseFnsViaPvv(String messageId, URI uri) {
        try {
            log.debug("Получение ответа {}...", messageId);
            return restTemplate.getForEntity(
                            uri+messageId,
                            ByteArrayResource.class
                    )
                    .getBody();
        } catch (Exception ex) {
            log.error("Ошибка при получении ответа для межконтурного обновления: {} по адресу '{}'", ex.getMessage(),
                    uri);
        }
        return null;
    }

    private M4dResponseInfo receiveJsonResponseM4dFnsViaPvv(String messageId, URI uri) {
        try {
            log.debug("Получение json-ответа {}...", messageId);
            return restTemplate.getForEntity(
                            uri+messageId,
                            M4dResponseInfo.class
                    )
                    .getBody();
        } catch (Exception ex) {
            log.error("Ошибка при получении json-ответа для межконтурного обновления: {} по адресу '{}'", ex.getMessage(),
                    uri);
        }
        return null;
    }

    private M4dRevokeResponseInfo receiveJsonResponseM4dRevokeFnsViaPvv(String messageId, URI uri) {
        try {
            log.debug("Получение json-ответа {}...", messageId);
            return restTemplate.getForEntity(
                            uri+messageId,
                            M4dRevokeResponseInfo.class
                    )
                    .getBody();
        } catch (Exception ex) {
            log.error("Ошибка при получении json-ответа для межконтурного обновления: {} по адресу '{}'", ex.getMessage(),
                    uri);
        }
        return null;
    }

}
