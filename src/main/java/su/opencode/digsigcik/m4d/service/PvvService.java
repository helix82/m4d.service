package su.opencode.digsigcik.m4d.service;

import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;

import java.io.IOException;

public interface PvvService {
    String sendM4dToRR(Authentication authentication, Long m4dId) throws IOException;
    String sendM4dRevokeToRR(Authentication authentication, Long m4dRevokeId) throws IOException;
    String sendRequestStatusM4dToRR(String guid);
    Resource receiveResponseM4dToRR(String messageId);
}
