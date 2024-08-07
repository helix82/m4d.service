package su.opencode.digsigcik.synapi.service;

import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpSession;
import java.util.Map;

public interface CertificateService {

    byte[] certificateOrderCSR(String domain, Map<String, Object> data);

    byte[] createRootCertificate(String notAfter, Map<String, Object> data);
}
