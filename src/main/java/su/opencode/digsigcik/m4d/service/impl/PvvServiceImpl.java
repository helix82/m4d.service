package su.opencode.digsigcik.m4d.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.m4d.exception.BadRequestException;
import su.opencode.digsigcik.m4d.model.M4d;
import su.opencode.digsigcik.m4d.model.M4dConfirm;
import su.opencode.digsigcik.m4d.model.M4dRecall;
import su.opencode.digsigcik.m4d.model.M4dRecallConfirm;
import su.opencode.digsigcik.m4d.service.*;
import su.opencode.digsigcik.m4d.util.CustomMultipartFile;

import javax.validation.ConstraintViolationException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static su.opencode.digsigcik.m4d.util.Constants.MRPA_IS_NULL;
import static su.opencode.digsigcik.m4d.util.Constants.MRPA_RECALL_IS_NULL;

@Slf4j
@Service
@Primary
@RequiredArgsConstructor
@EnableTransactionManagement
public class PvvServiceImpl implements PvvService {
    private final M4dService m4dService;
    private final M4dRecallService m4dRecallService;
    private final M4dRecallConfirmService m4dRecallConfirmService;
    private final M4dConfirmService m4dConfirmService;
    private final M4dFileService m4dFileService;
    private final RestTemplate restTemplate;

    @Value("${pvv.fns-send-registration-request-m4d}")
    private URI FNS_SEND_REGISTRATION_REQUEST_M4D_URI;
    @Value("${pvv.fns-send-revoke-request-m4d}")
    private URI FNS_SEND_REVOKE_REQUEST_M4D_URI;
    @Value("${pvv.fns-send-status-request-m4d}")
    private URI FNS_SEND_STATUS_REQUEST_M4D_URI;

    @Override
    public String sendM4dToRR(Authentication authentication, Long m4dId) throws IOException {
        M4d m4d;
        try {
            m4d = this.m4dService.findById(m4dId);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(e.getMessage());
        }
        if (m4d == null) {
            throw new BadRequestException(MRPA_IS_NULL);
        }
        M4dConfirm m4dConfirm;
        try {
            m4dConfirm = this.m4dConfirmService.findM4dConfirmByM4dId(m4d.getId());
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(e.getMessage());
        }
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
            throw new BadRequestException("Ошибка создания архива");
        }

        MultipartFile requestTempFile = new CustomMultipartFile(resp);
        String res = registrationRequestM4dFnsViaPvv(requestTempFile, FNS_SEND_REGISTRATION_REQUEST_M4D_URI);
        log.info(res);
        return res;
    }

    @Override
    public String sendM4dRevokeToRR(Authentication authentication, Long m4dRevokeId) throws IOException {
        M4dRecall m4dRecall;
        try {
            m4dRecall = this.m4dRecallService.findById(m4dRevokeId);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(e.getMessage());
        }
        if (m4dRecall == null) {
            throw new BadRequestException(MRPA_RECALL_IS_NULL);
        }
        M4dRecallConfirm m4dRecallConfirm;
        try {
            m4dRecallConfirm = this.m4dRecallConfirmService.findM4dRecallConfirmByM4dRecallId(m4dRecall.getId());
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(e.getMessage());
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
            throw new BadRequestException("Ошибка создания архива");
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
            return restTemplate.postForEntity(
                            uri,
                            new HttpEntity<>(body),
                            String.class
                    )
                    .getBody();
        } catch (Exception ex) {
            log.error("Ошибка при отправке запроса на получение статуса МЧД для межконтурного обновления: {} по адресу '{}'", ex.getMessage(),
                    uri);
        }
        return null;
    }
}
