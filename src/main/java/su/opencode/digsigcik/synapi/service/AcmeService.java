package su.opencode.digsigcik.synapi.service;

import org.springframework.http.ResponseEntity;
import su.opencode.digsigcik.synapi.model.AcmeOrder;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

public interface AcmeService {
    ResponseEntity<Map> login(HttpSession httpSession, Long id);
    ResponseEntity<Map> register(HttpSession httpSession,Long id);
    ResponseEntity<Map> logout(HttpSession httpSession, Long id);
    ResponseEntity<Map> certificate(HttpSession httpSession, String domainName, String notAfter, Long providerId, Map data
    );
    ResponseEntity<Map> certificateOrder(HttpSession httpSession, String domainName, String notAfter, Long providerId, Map data
    );
    ResponseEntity<String> downloadChallengeFile(HttpServletResponse httpResponse, Long id
    );
    ResponseEntity<Map> certificateRelease(HttpSession httpSession, Long id
    );
    ResponseEntity<Map> certificate(HttpSession httpSession, Long domainId
    );
    ResponseEntity<Map> revoke(HttpSession httpSession, Long id
    );
    Map orderInfo(AcmeOrder order);
}
