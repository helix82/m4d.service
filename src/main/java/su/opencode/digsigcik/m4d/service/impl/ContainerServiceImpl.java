package su.opencode.digsigcik.m4d.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.m4d.service.ContainerService;
import su.opencode.digsigcik.m4d.service.SynapiClientService;
import su.opencode.digsigcik.m4d.util.FileUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;

import static su.opencode.digsigcik.m4d.util.EncodingUtils.isBase64;
import static su.opencode.digsigcik.m4d.util.JwtTokenUtils.getTokenValue;

@Service
@Slf4j
public class ContainerServiceImpl implements ContainerService {
    private final SynapiClientService synapiClientService;

    public ContainerServiceImpl(SynapiClientService synapiClientService) {
        this.synapiClientService = synapiClientService;
    }

    @Override
    public MultipartFile getSignatureFromContainer(Authentication authentication, MultipartFile container, String filename) throws IOException {
        String bearerToken = getTokenValue(authentication);
        String tempContainerFile;
        String tempFile;
        FileOutputStream fo;
        tempContainerFile = "/tmp/container.dscp";
        fo = new FileOutputStream(tempContainerFile);
        byte[] bytes = container.getBytes();
        fo.write(bytes);
        fo.close();
        ByteArrayResource resSig = this.synapiClientService.getPartContainer(tempContainerFile, "detachedSign", bearerToken);
        String signatureData;
        if (!isBase64(resSig.toString())) {
            signatureData = Base64.getEncoder().encodeToString(resSig.getByteArray()).replaceAll("\\s+", "");
        } else {
            signatureData = resSig.toString().replaceAll("\\s+", "");
        }
        tempFile = "/tmp/" + filename + "_detached.sig";
        fo = new FileOutputStream(tempFile);
        fo.write(signatureData.getBytes());
        fo.close();
        return FileUtils.mapFileToMultipart(tempFile);
    }
}
