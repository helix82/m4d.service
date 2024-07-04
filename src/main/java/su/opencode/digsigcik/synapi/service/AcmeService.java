package su.opencode.digsigcik.synapi.service;

import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

public interface AcmeService {
    Map login(HttpSession httpSession, Long id);
    Map register(HttpSession httpSession,Long id);
    Map logout(HttpSession httpSession, Long id);
    ResponseEntity<Map> certificate(HttpSession httpSession, String domainName, String notAfter, Long providerId, Map data
    );
    ResponseEntity<Map> certificateOrder(HttpSession httpSession, String domainName, String notAfter, Long providerId, Map data
    );
    ResponseEntity<String> downloadChallengeFile(HttpServletResponse httpResponse, Long id
    );
    ResponseEntity<Map> certificateRelease(HttpSession httpSession, Long id
    );
    Map certificate(HttpSession httpSession, Long domainId
    );
    Map revoke(HttpSession httpSession, Long id
    );
}
