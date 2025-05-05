package su.opencode.digsigcik.m4d.service;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.m4d.enums.HashAlgorithm;

import java.io.IOException;

public interface ContainerService {
    boolean verify(Authentication authentication, MultipartFile container, String content);
    ByteArrayResource createContainer(Authentication authentication, MultipartFile sourceFile, MultipartFile sourceSig, HashAlgorithm hashAlgorithm) throws IOException;
    ByteArrayResource createArchiveSourceSig(Authentication authentication, Long sourceFileId, Long confirmFileId, String filenameSource, String filenameConfirm) throws IOException;
}
