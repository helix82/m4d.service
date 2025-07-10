package su.opencode.digsigcik.m4d.service;

import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.m4d.dto.M4dConfirmDto;
import su.opencode.digsigcik.m4d.model.M4d;
import su.opencode.digsigcik.m4d.model.M4dConfirm;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;

public interface M4dConfirmService {
    M4dConfirm saveM4dConfirm(M4dConfirm m4dConfirm);
    M4dConfirm findM4dConfirmByM4dId(Long m4dId);
    M4dConfirm updateM4dConfirm(M4dConfirm m4dConfirm);
    void deleteM4dConfirmByM4dId(Long id);
    Long uploadSigRegisteredPep(Long m4dId, MultipartFile signature) throws IOException;
    Long uploadSigRegisteredRr(Long m4dId, MultipartFile container) throws IOException;
    Long uploadXmlRegisteredRr(Long m4dId, String xmlContent);
    M4dConfirmDto getXml(String idFile, String guid, String internalNumber, String startDate, String endDate);
    M4dConfirm createAndSignedM4dConfirm(Authentication authentication, M4d m4d, String idFile, String guid, String internalNumber, String startDate, String endDate) throws JAXBException, IOException;
    M4dConfirm saveConfirmAndSignatureRr(Long m4dId, File xmlFileKvitM4d, File sigFileKvitM4d);
}
