package su.opencode.digsigcik.m4d.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.m4d.enums.FileLogicalTypeEnum;
import su.opencode.digsigcik.m4d.enums.FileMimeTypeEnum;
import su.opencode.digsigcik.common.exception.custom.BadRequestException;
import su.opencode.digsigcik.m4d.model.M4dFile;
import su.opencode.digsigcik.m4d.repository.M4dFileRepository;
import su.opencode.digsigcik.m4d.service.M4dFileService;
import su.opencode.digsigcik.m4d.service.SynapiClientService;

import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static su.opencode.digsigcik.m4d.util.EncodingUtils.isBase64;
import static su.opencode.digsigcik.m4d.util.JwtTokenUtils.getTokenValue;

@Service
@Primary
@RequiredArgsConstructor
public class M4dFileServiceImpl implements M4dFileService {
    private final SynapiClientService synapiClientService;
    private final M4dFileRepository repository;

    @Override
    public M4dFile findM4dFileById(@NotNull Long id) {
        return this.repository.findM4dFileById(id);
    }

    @Override
    public void deleteM4dFile(@NotNull Long id) {
        this.repository.deleteById(id);
    }

    @Override
    public M4dFile saveM4dFile(@NotNull M4dFile m4dFile) {
        return this.repository.save(m4dFile);
    }

    @Override
    public M4dFile findM4dFileByContent(@NotNull String content) {
        return this.repository.findFirstByContent(content);
    }

    @Override
    public M4dFile fillM4dFile(FileMimeTypeEnum fileMimeTypeEnum, FileLogicalTypeEnum fileLogicalTypeEnum, String filename, byte[] content) {
        M4dFile m4dFile = new M4dFile();
        m4dFile.setFileMimeType(fileMimeTypeEnum);
        m4dFile.setFileLogicalType(fileLogicalTypeEnum);
        m4dFile.setFilename(filename);
        if (!isBase64(new String(content))) {
            m4dFile.setContent(Base64.getEncoder().encodeToString(content));
        } else {
            m4dFile.setContent(new String(content));
        }
        M4dFile existsM4dFile;
        try {
            existsM4dFile = this.findM4dFileByContent(m4dFile.getContent());
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(e.getMessage());
        }
        return Objects.requireNonNullElse(existsM4dFile, m4dFile);
    }

    @Override
    public File createArchive(Authentication authentication, String filename, List<Long> values) throws IOException {
        String bearerToken = getTokenValue(authentication);
        List<String> tempFileNames = new ArrayList<>();
        String tempContainerFile;
        String tempFile;
        String tempZipFile;
        FileOutputStream fo;
        for (Long value : values) {
            M4dFile m4dFile;
            try {
                m4dFile = this.findM4dFileById(value);
            } catch (Exception e) {
                throw new BadRequestException(e.getMessage());
            }
            if (m4dFile.getFilename().endsWith(".dscp")) {
                tempContainerFile = "/tmp/" + m4dFile.getFilename();
                fo = new FileOutputStream(tempContainerFile);
                byte[] bytes = Base64.getDecoder().decode(m4dFile.getContent());
                fo.write(bytes);
                fo.close();
                ByteArrayResource resSig = this.synapiClientService.getPartContainer(tempContainerFile, "detachedSign", bearerToken);
                String signatureData = "";
                if (!isBase64(resSig.toString())) {
                    signatureData = Base64.getEncoder().encodeToString(resSig.getByteArray()).replaceAll("\\s+", "");
                } else {
                    signatureData = resSig.toString().replaceAll("\\s+", "");
                }
                tempFile = "/tmp/" + filename + "_detached.sig";
                tempFileNames.add(tempFile);
                fo = new FileOutputStream(tempFile);
                fo.write(signatureData.getBytes());
                fo.close();
            } else if (m4dFile.getFilename().endsWith(".sig")) {
                tempFile = "/tmp/" + m4dFile.getFilename();
                tempFileNames.add(tempFile);
                fo = new FileOutputStream(tempFile);
                byte[] bytes = m4dFile.getContent().getBytes();
                fo.write(bytes);
                fo.close();
            } else {
                tempFile = "/tmp/" + m4dFile.getFilename();
                tempFileNames.add(tempFile);
                fo = new FileOutputStream(tempFile);
                byte[] bytes = Base64.getDecoder().decode(m4dFile.getContent());
                fo.write(bytes);
                fo.close();
            }
        }

        tempZipFile = "/tmp/" + filename + ".zip";
        createZip(tempZipFile, tempFileNames);

        for (String fileName : tempFileNames) {
            File f = new File(fileName);
            f.delete();
        }
        return new File(tempZipFile);
    }

    private static void createZip(String zipFilePath, List<String> files) throws IOException {
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
