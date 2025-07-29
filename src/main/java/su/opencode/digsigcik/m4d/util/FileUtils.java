package su.opencode.digsigcik.m4d.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import su.opencode.digsigcik.common.exception.custom.BadRequestException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.zip.ZipFile;

import static su.opencode.digsigcik.m4d.util.Constants.CREATE_TEMP_FOLDER_ERROR;

@Slf4j
@UtilityClass
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

    public static MultipartFile mapStringToMultipart(String content, String filename) throws IOException {
        String tempSourceFile;
        FileOutputStream fo1;
        String formattedDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String tempPath = "/tmp/" + formattedDateTime;
        createDirectoryIfSheNotExist(tempPath);
        tempSourceFile = tempPath + "/" + filename;
        fo1 = new FileOutputStream(tempSourceFile);
        fo1.write(content.getBytes());
        fo1.close();
        return new CustomMultipartFile(new File(tempSourceFile));
    }

    public static void createDirectoryIfSheNotExist(String directoryPathName) {
        Path directoryPath = Paths.get(directoryPathName);
        if (!Files.exists(directoryPath)) {
            File file = new File(directoryPathName);
            boolean res = file.mkdirs();
            if (!res) {
                throw new BadRequestException(CREATE_TEMP_FOLDER_ERROR);
            }
        }
    }

    public static String convertToBase64(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            return Base64.getEncoder().encodeToString(bytes);
        } catch (IOException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    public static File byteArrayResourceToTempFile(ByteArrayResource resource, String prefix, String suffix) throws IOException {
        String formattedDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String tempPath = "/tmp/" + formattedDateTime;
        createDirectoryIfSheNotExist(tempPath);
        String tempSourceFile = tempPath + "/" + prefix + suffix;
        File file = new File(tempSourceFile);
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(resource.getByteArray());
        }
        return file;
    }

    public static File convertMultipartFileToFile(MultipartFile multipartFile) throws IOException {
        String formattedDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String tempPath = "/tmp/" + formattedDateTime;
        createDirectoryIfSheNotExist(tempPath);
        String tempSourceFile = tempPath + "/" + multipartFile.getOriginalFilename();
        FileOutputStream fo1;
        fo1 = new FileOutputStream(tempSourceFile);
        fo1.write(multipartFile.getBytes());
        fo1.close();
        return new File(tempSourceFile);
    }

    public static boolean isNotZipFile(MultipartFile file) throws IOException {
        File tempFile = FileUtils.convertMultipartFileToFile(file);
        try (ZipFile ignored = new ZipFile(tempFile)) {
            return false;
        } catch (IOException e) {
            return true;
        }
    }

    public static boolean isNotXmlFile(MultipartFile file) throws IOException {
        File tempFile = FileUtils.convertMultipartFileToFile(file);
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.parse(tempFile);
            return false;
        } catch (Exception e) {
            return true;
        }
    }
}
