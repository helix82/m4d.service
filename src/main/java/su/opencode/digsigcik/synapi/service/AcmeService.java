package su.opencode.digsigcik.synapi.service;

import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpSession;
import java.util.Map;

public interface AcmeService {
    Map login(HttpSession httpSession, Long id);
    Map register(HttpSession httpSession,Long id);
    Map logout(HttpSession httpSession, Long id);
    ResponseEntity<Map> certificate(HttpSession httpSession, String domainName, String notAfter
    );
    Map certificate(HttpSession httpSession, Long domainId
    );
    Map revoke(HttpSession httpSession, Long id
    );
}
