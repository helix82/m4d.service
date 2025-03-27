package su.opencode.digsigcik.m4d.service;

import org.springframework.core.io.ByteArrayResource;
import su.opencode.digsigcik.m4d.enums.HashAlgorithm;

import java.util.Map;

public interface SynapiClientService {
    ByteArrayResource createContainer(String tempFileSource, String tempFileSig, HashAlgorithm hashAlgorithm, String bearerToken);
    Map<String, String> getReportContainer(String tempFileContainer, String bearerToken);
    ByteArrayResource getPartContainer(String tempFileContainer, String selector, String bearerToken);
}
