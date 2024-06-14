package su.opencode.digsigcik.synapi.util;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.IOUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import su.opencode.digsigcik.synapi.dscp.DscpContainerException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
}
