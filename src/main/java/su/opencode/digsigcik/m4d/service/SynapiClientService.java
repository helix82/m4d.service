package su.opencode.digsigcik.m4d.service;

import org.springframework.core.io.ByteArrayResource;

public interface SynapiClientService {
    ByteArrayResource getPartContainer(String tempFileContainer, String selector, String bearerToken);
}
