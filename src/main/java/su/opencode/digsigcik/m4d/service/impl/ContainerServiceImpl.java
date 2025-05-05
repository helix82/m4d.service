package su.opencode.digsigcik.m4d.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.plexus.util.StringUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.m4d.dto.ReportDto;
import su.opencode.digsigcik.m4d.enums.HashAlgorithm;
import su.opencode.digsigcik.m4d.exception.BadRequestException;
import su.opencode.digsigcik.m4d.model.M4dFile;
import su.opencode.digsigcik.m4d.service.ContainerService;
import su.opencode.digsigcik.m4d.service.M4dFileService;
import su.opencode.digsigcik.m4d.service.SynapiClientService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static su.opencode.digsigcik.m4d.util.Constants.MRPA_REGISTRY_NOT_VERIFY_CONTENT;
import static su.opencode.digsigcik.m4d.util.Constants.MRPA_REGISTRY_NOT_VERIFY_REPORT;
import static su.opencode.digsigcik.m4d.util.JwtTokenUtils.getTokenValue;

@Service
@Primary
@Slf4j
public class ContainerServiceImpl implements ContainerService {
    private final SynapiClientService synapiClientService;
    private final M4dFileService m4dFileService;

    public ContainerServiceImpl(SynapiClientService synapiClientService, M4dFileService m4dFileService) {
        this.synapiClientService = synapiClientService;
        this.m4dFileService = m4dFileService;
    }

    public boolean verify(Authentication authentication, MultipartFile container, String content) {
        Boolean resultVerify = false;
        String bearerToken = getTokenValue(authentication);
        List<String> tempFileNames = new ArrayList<>();
        String tempFileName;
        FileOutputStream fo;
        ReportDto reportDto;
        ByteArrayResource res = null;
        try {
            tempFileName = "/tmp/" + container.getOriginalFilename();

            tempFileNames.add(tempFileName);
            fo = new FileOutputStream(tempFileName);
            fo.write(container.getBytes());
            fo.close();
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> resultMap = this.synapiClientService.getReportContainer(tempFileName, bearerToken);
            reportDto = objectMapper.readValue(resultMap.get("signCheck"), ReportDto.class);

            assert reportDto != null;
            if (reportDto.getResults() != null && !reportDto.getResults().isEmpty()) {
                reportDto.getResults().stream()
                        .flatMap(result -> result.getVerificationResults().stream())
                        .findFirst()
                        .ifPresent(verificationResult -> {
                            boolean isResultTrue = verificationResult.isResult();
                            System.out.println("Result value: " + isResultTrue);
                            if (!isResultTrue) {
                                throw new BadRequestException(MRPA_REGISTRY_NOT_VERIFY_REPORT);
                            }
                        });
            } else {
                log.error(reportDto.getError() + ": {}", reportDto.getErrorDescription());
                throw new BadRequestException(reportDto.getError());
            }

            res = this.synapiClientService.getPartContainer(tempFileName, "sourceFile", bearerToken);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String fileName : tempFileNames) {
            File f = new File(fileName);
            f.delete();
        }

        assert res != null;
        byte[] resource = res.getByteArray();
        String normalizedResource = new String(resource, StandardCharsets.UTF_8).replaceAll("\\s+", "");
        String normalizedXmlString = content.replaceAll("\\s+", "");
        if (!StringUtils.difference(normalizedResource, normalizedXmlString).isEmpty()) {
            throw new BadRequestException(MRPA_REGISTRY_NOT_VERIFY_CONTENT);
        }
        resultVerify = true;
        return resultVerify;
    }

    public ByteArrayResource createContainer(Authentication authentication,MultipartFile sourceFile, MultipartFile sourceSig, HashAlgorithm hashAlgorithm) throws IOException {
        String bearerToken = getTokenValue(authentication);
        String tempFileSource;
        String tempFileSig;
        FileOutputStream foSource;
        FileOutputStream foSig;
        ByteArrayResource res;

        tempFileSource = "/tmp/" + sourceFile.getOriginalFilename();
        foSource = new FileOutputStream(tempFileSource);
        foSource.write(sourceFile.getBytes());
        foSource.close();

        tempFileSig = "/tmp/" + sourceSig.getOriginalFilename();
        foSig = new FileOutputStream(tempFileSig);
        foSig.write(sourceSig.getBytes());
        foSig.close();

        res = synapiClientService.createContainer(tempFileSource, tempFileSig, hashAlgorithm, bearerToken);
        File f1 = new File(tempFileSource);
        f1.delete();
        File f2 = new File(tempFileSig);
        f2.delete();
        return res;
    }

    public ByteArrayResource createArchiveSourceSig(Authentication authentication, Long sourceFileId, Long confirmFileId, String filenameSource, String filenameConfirm) throws IOException {
        String bearerToken = getTokenValue(authentication);
        M4dFile m4dFile;
        try {
            m4dFile = this.m4dFileService.findM4dFileById(sourceFileId);
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }
        M4dFile m4dConfirmFile;
        try {
            m4dConfirmFile = this.m4dFileService.findM4dFileById(confirmFileId);
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }
        List<String> tempFileNames = new ArrayList<>();
        String tempContainerFile;
        String tempSourceFile;
        String tempSigFile;
        String tempConfirmContainerFile;
        String tempConfirmSourceFile;
        String tempConfirmSigFile;
        String tempZipFile = "";
        FileOutputStream fo;
        ByteArrayResource resSource;
        ByteArrayResource resSig;
        ByteArrayResource resConfirmSource;
        ByteArrayResource resConfirmSig;
        try {
            tempContainerFile = "/tmp/" + m4dFile.getFilename();
            tempFileNames.add(tempContainerFile);
            fo = new FileOutputStream(tempContainerFile);
            byte[] bytes = Base64.getDecoder().decode(m4dFile.getContent());
            fo.write(bytes);
            fo.close();

            resSource = this.synapiClientService.getPartContainer(tempContainerFile, "sourceFile", bearerToken);
            tempSourceFile = "/tmp/" + filenameSource + ".xml";
            tempFileNames.add(tempSourceFile);
            fo = new FileOutputStream(tempSourceFile);
            fo.write(resSource.getByteArray());
            fo.close();

            resSig = this.synapiClientService.getPartContainer(tempContainerFile, "detachedSign", bearerToken);
            String signatureData = "";
            if (!isBase64(resSig.toString())) {
                signatureData = Base64.getEncoder().encodeToString(resSig.getByteArray()).replaceAll("\\s+", "");
            } else {
                signatureData = resSig.toString().replaceAll("\\s+", "");
            }
            tempSigFile = "/tmp/" + filenameSource + "_detached.sig";
            tempFileNames.add(tempSigFile);
            fo = new FileOutputStream(tempSigFile);
            fo.write(signatureData.getBytes());
            fo.close();

            tempConfirmContainerFile = "/tmp/" + m4dConfirmFile.getFilename();
            tempFileNames.add(tempConfirmContainerFile);
            fo = new FileOutputStream(tempConfirmContainerFile);
            byte[] bytesConfirm = Base64.getDecoder().decode(m4dFile.getContent());
            fo.write(bytesConfirm);
            fo.close();

            resConfirmSource = this.synapiClientService.getPartContainer(tempConfirmContainerFile, "sourceFile", bearerToken);
            tempConfirmSourceFile = "/tmp/" + filenameConfirm + ".xml";
            tempFileNames.add(tempConfirmSourceFile);
            fo = new FileOutputStream(tempConfirmSourceFile);
            fo.write(resConfirmSource.getByteArray());
            fo.close();

            resConfirmSig = this.synapiClientService.getPartContainer(tempConfirmContainerFile, "detachedSign", bearerToken);
            String signatureConfirmData = "";
            if (!isBase64(resConfirmSig.toString())) {
                signatureConfirmData = Base64.getEncoder().encodeToString(resConfirmSig.getByteArray()).replaceAll("\\s+", "");
            } else {
                signatureConfirmData = resConfirmSig.toString().replaceAll("\\s+", "");
            }
            tempConfirmSigFile = "/tmp/" + filenameConfirm + "_detached.sig";
            tempFileNames.add(tempConfirmSigFile);
            fo = new FileOutputStream(tempConfirmSigFile);
            fo.write(signatureConfirmData.getBytes());
            fo.close();

            tempZipFile = "/tmp" + filenameSource + ".zip";
            createZip(tempZipFile, tempSourceFile, tempSigFile, tempConfirmSourceFile, tempConfirmSigFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String fileName : tempFileNames) {
            File f = new File(fileName);
            f.delete();
        }
        return new ByteArrayResource(Files.readAllBytes((new File(tempZipFile)).toPath()));
    }

    public boolean isBase64(String str) {
        Pattern BASE64_PATTERN = Pattern.compile("^[A-Za-z0-9+/]+={0,2}$");
        if (!BASE64_PATTERN.matcher(str).matches()) {
            return false;
        }
        try {
            Base64.getDecoder().decode(str);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static void createZip(String zipFilePath, String... files) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(zipFilePath);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            for (String filePath : files) {
                File file = new File(filePath);
                if (!file.exists()) {
                    System.err.println("Файл не найден: " + filePath);
                    continue;
                }

                try (FileInputStream fis = new FileInputStream(file)) {
                    ZipEntry zipEntry = new ZipEntry(file.getName());
                    zos.putNextEntry(zipEntry);

                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fis.read(buffer)) >= 0) {
                        zos.write(buffer, 0, length);
                    }

                    zos.closeEntry();
                }
            }
        }
    }
}
