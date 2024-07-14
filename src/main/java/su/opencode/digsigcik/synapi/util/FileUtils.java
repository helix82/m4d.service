package su.opencode.digsigcik.synapi.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import su.opencode.digsigcik.synapi.dscp.DscpContainerException;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class FileUtils {
    public static byte[] getFileContentByPath(String filePathName) {
        Path filePath = Paths.get(filePathName);
        byte[] fileContent;
        try {
            fileContent = Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return fileContent;
    }

    public static void createFile(String filePathName, byte[] content) {
        Path filePath = Paths.get(filePathName);
        try (OutputStream os = Files.newOutputStream(filePath)) {
            os.write(content);
        } catch (IOException e) {
            throw DscpContainerException.ioCause(e, false);
        }
    }

    public static MultipartFile mapFileToMultipart(String filePathName) {
        MultipartFile multipartFile;
        File file = new File(filePathName);
        try (InputStream input = new FileInputStream(file)) {
            FileItem fileItem = new DiskFileItem("file", Files.probeContentType(file.toPath()),
                    false, file.getName(), (int) file.length(), file.getParentFile());
            OutputStream outputStream = fileItem.getOutputStream();
            IOUtils.copy(input, outputStream);
            multipartFile = new CommonsMultipartFile(fileItem);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return multipartFile;
    }

    public static void createDirectoryIfSheNotExist(String directoryPathName) {
        Path directoryPath = Paths.get(directoryPathName);
        if (!Files.exists(directoryPath)) {
            File file = new File(directoryPathName);
            file.mkdirs();
        }
    }

    public static void deleteFile(String filePathName) {
        File file = new File(filePathName);
        file.delete();
    }

    public static void saveMultipartFile(MultipartFile file, String filePath) {
        try {
            String originalFileName = file.getOriginalFilename();
            Path targetLocation = Paths.get(filePath, originalFileName);
            Files.copy(file.getInputStream(), targetLocation);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при сохранении файла: " + e.getMessage());
        }
    }

    public static byte[] toBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new IllegalStateException("Could not read file into memory");
        }
    }

    public static ResponseEntity<Resource> resourceOctetStreamOrInternal(
            @NotNull Resource resource,
            @NotNull String filename
    ) {
        var contentType = MediaType.APPLICATION_OCTET_STREAM;
        return resourceOctetStreamOrInternal(resource, contentType, filename);
    }

    public static ResponseEntity<Resource> resourceOctetStreamOrInternal(
            @NotNull Resource resource,
            @NotNull MediaType contentType,
            @NotNull String filename
    ) {
        try {
            String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8.toString());
            var disposition = ContentDisposition.attachment()
                    .filename(encodedFilename)
                    .build();
            return ResponseEntity.ok()
                    .contentLength(resource.contentLength())
                    .contentType(contentType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                    .body(resource);
        } catch (IOException e) {
            log.error("Could not create octet-stream from resource: "
                    + "resource cannot be resolved", e);
            return ResponseEntity.internalServerError()
                    .build();
        }
    }
}
