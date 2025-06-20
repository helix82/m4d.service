package su.opencode.digsigcik.m4d.service;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.security.core.Authentication;
import su.opencode.digsigcik.m4d.dto.response.M4dResponseInfo;
import su.opencode.digsigcik.m4d.dto.response.M4dRevokeResponseInfo;

import java.io.IOException;

public interface PvvService {
    String sendM4dToRR(Authentication authentication, Long m4dId) throws IOException;
    String sendM4dRevokeToRR(Authentication authentication, Long m4dRevokeId) throws IOException;
    String sendRequestStatusM4dToRR(String guid);
    ByteArrayResource receiveResponseM4dToRR(String messageId);
    M4dResponseInfo receiveJsonResponseM4dToRR(String messageId);
    M4dRevokeResponseInfo receiveJsonResponseM4dRevokeToRR(String messageId);
}
