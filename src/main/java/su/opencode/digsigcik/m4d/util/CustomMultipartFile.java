package su.opencode.digsigcik.m4d.util;

import lombok.SneakyThrows;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class CustomMultipartFile implements MultipartFile {

    private final FileSystemResource fileSystemResource;

    public CustomMultipartFile(String filePath) {
        this.fileSystemResource = new FileSystemResource(filePath);
    }

    @Override
    public String getName() {
        return fileSystemResource.getFilename();
    }

    @Override
    public String getOriginalFilename() {
        return fileSystemResource.getFilename();
    }

    @Override
    public String getContentType() {
//        try {
//            return Files.probeContentType(fileSystemResource.getFile().toPath());
//        } catch (IOException e) {
//            return null; // или выбросьте исключение
//        }
        String filename = getOriginalFilename();
        if (filename != null) {
            String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
            switch (extension) {
                case "txt":
                    return "text/plain";
                case "jpg":
                case "jpeg":
                    return "image/jpeg";
                case "png":
                    return "image/png";
                case "pdf":
                    return "application/pdf";
                case "zip":
                    return "application/x-zip-compressed";
                case "dscp":
                    return "application/octet-stream";
                // Добавьте другие типы по мере необходимости
                default:
                    return "application/octet-stream"; // Тип по умолчанию
            }
        }
        return null;
    }

    @Override
    public boolean isEmpty() {
        try {
            return fileSystemResource.contentLength() == 0;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows
    @Override
    public long getSize() {
        return fileSystemResource.contentLength();
    }

    @Override
    public byte[] getBytes() throws IOException {
        return Files.readAllBytes(fileSystemResource.getFile().toPath());
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(fileSystemResource.getFile());
    }

    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {
        Files.copy(fileSystemResource.getFile().toPath(), dest.toPath());
    }
}