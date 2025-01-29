package su.opencode.digsigcik.m4d.service;

import org.springframework.core.io.ByteArrayResource;
import su.opencode.digsigcik.m4d.dto.ReportDto;
import su.opencode.digsigcik.m4d.enums.HashAlgorithm;

public interface SynapiClientService {
    ByteArrayResource createContainer(String tempFileSource, String tempFileSig, HashAlgorithm hashAlgorithm, String bearerToken);
    ReportDto getReportContainer(String tempFileContainer, boolean reverify, String bearerToken);
    ByteArrayResource getSourceFileContainer(String tempFileContainer, String selector, String bearerToken);
}
