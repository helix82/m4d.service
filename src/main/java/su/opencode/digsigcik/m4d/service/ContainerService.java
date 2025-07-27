package su.opencode.digsigcik.m4d.service;

import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ContainerService {
    MultipartFile getSignatureFromContainer(Authentication authentication, MultipartFile container, String filename) throws IOException;
}
