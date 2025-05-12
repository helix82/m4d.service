package su.opencode.digsigcik.m4d.service;

import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.m4d.dto.M4dRevokeConfirmDto;
import su.opencode.digsigcik.m4d.model.M4dRecall;
import su.opencode.digsigcik.m4d.model.M4dRecallConfirm;

import javax.xml.bind.JAXBException;
import java.io.IOException;

public interface M4dRecallConfirmService {
    M4dRecallConfirm saveM4dRecallConfirm(M4dRecallConfirm m4dConfirm);
    M4dRecallConfirm findM4dRecallConfirmByM4dRecallId(Long m4dRecallId);
    M4dRecallConfirm updateM4dRecallConfirm(M4dRecallConfirm m4dConfirm);
    void deleteM4dRecallConfirmById(Long id);
    Long uploadSigRegisteredPep(Long m4dRecallId, MultipartFile signature);
    Long uploadSigRegisteredRr(Long m4dRecallId, MultipartFile signature);
    Long uploadXmlRegisteredRr(Long m4dRecallId, String xmlContent);
    M4dRevokeConfirmDto getXml(String guid, String hashFile);
    M4dRecallConfirm createAndSignedM4dRecallConfirm(Authentication authentication, M4dRecall m4dRecall, String guid, String hashFile) throws JAXBException, IOException;
}
