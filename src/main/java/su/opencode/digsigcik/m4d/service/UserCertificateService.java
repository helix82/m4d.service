package su.opencode.digsigcik.m4d.service;

import org.springframework.security.core.Authentication;

import java.util.Set;

public interface UserCertificateService {
    Set<String> getUserCertificateINNs(Authentication authentication);
}
