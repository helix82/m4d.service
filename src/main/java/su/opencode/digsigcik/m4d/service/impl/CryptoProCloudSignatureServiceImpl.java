package su.opencode.digsigcik.m4d.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.common.exception.custom.BadRequestException;
import su.opencode.digsigcik.m4d.dto.CryptoProCloudBufferDto;
import su.opencode.digsigcik.m4d.enums.HashAlgorithm;
import su.opencode.digsigcik.m4d.mapper.CryptoProxyEventMapper;
import su.opencode.digsigcik.m4d.service.CryptoProCloudSignatureService;
import su.opencode.digsigcik.m4d.service.CryptoProxyClientService;

import java.nio.charset.StandardCharsets;

import static su.opencode.digsigcik.m4d.util.Constants.MAX_SIZE_CONTENT_EXCEPTION;
import static su.opencode.digsigcik.m4d.util.EncodingUtils.encodeMultipartFileToBase64;
import static su.opencode.digsigcik.m4d.util.JwtTokenUtils.getAuthenticationUsername;
import static su.opencode.digsigcik.m4d.util.JwtTokenUtils.getTokenValue;

@Service
@Slf4j
@RequiredArgsConstructor
public class CryptoProCloudSignatureServiceImpl implements CryptoProCloudSignatureService {
    private final CryptoProxyClientService cryptoProxyClientService;
    private final CryptoProxyEventMapper cryptoProxyEventMapper;
    public static final int MAX_BASE64_SIZE_BYTES = 10 * 1024 * 1024; // 10 МБ

    @Override
    public String initializeSignature(Authentication authentication,
                                                 MultipartFile sourceFile,
                                                 Long certificateId,
                                                 String userLogin,
                                                 String pinCode,
                                                 boolean isDetached,
                                                 String sigType,
                                                 HashAlgorithm hashAlgorithm) {

        log.info("Инициализация процесса облачной подписи для пользователя: {}", getAuthenticationUsername(authentication));

        return cryptoProxyClientService.requestFileCadesSignature(
                getTokenValue(authentication),
                cryptoProxyEventMapper.mapToCAdESSignEvent(
                        new CryptoProCloudBufferDto(certificateId, userLogin, pinCode, isDetached),
                        sigType,
                        validateContentSize(encodeMultipartFileToBase64(sourceFile))));
    }

    private String validateContentSize(String content) {
        int contentSizeInBytes = content.getBytes(StandardCharsets.UTF_8).length;
        if (contentSizeInBytes > MAX_BASE64_SIZE_BYTES) {
            throw new BadRequestException(String.format(MAX_SIZE_CONTENT_EXCEPTION, MAX_BASE64_SIZE_BYTES));
        }
        return content;
    }

}
