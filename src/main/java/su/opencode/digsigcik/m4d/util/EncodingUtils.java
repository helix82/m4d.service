package su.opencode.digsigcik.m4d.util;

import lombok.experimental.UtilityClass;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.common.exception.custom.BadRequestException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.isNull;
import static su.opencode.digsigcik.m4d.util.Constants.FILE_CONVERT_BASE64_EXCEPTION;

@UtilityClass
public class EncodingUtils {

    public static String encodeFileToBase64(String fileUri) throws IOException {
        try (InputStream inputStream = Files.newInputStream(Path.of(fileUri))) {
            return Base64.getEncoder().encodeToString(inputStream.readAllBytes());
        }
    }

    public static String encodeFileToBase64(byte[] fileBytes) throws IOException {
//        try (InputStream inputStream = new ByteArrayInputStream(fileBytes)) {
//            return Base64.getEncoder().encodeToString(inputStream.readAllBytes());
//        }

        return Base64.getEncoder().encodeToString(fileBytes);
    }

    public static void decodeFileFromBase64(String url, String base64) throws IOException {

        try (var fileStream = new FileOutputStream(Path.of(url).toFile(), false)) {
            fileStream.write(
                    Base64.getDecoder().decode(base64)
            );
        }

        System.out.println(url);
    }

    public static byte[] decodeFileFromBase64(String contentBase64) throws IllegalArgumentException {
        return Base64.getDecoder().decode( validateBase64String(contentBase64) );
    }

    public static String validateBase64String(String base64) throws IllegalArgumentException {

        Matcher matcher = Pattern.compile("^.*?(?<b64>[A-Za-z0-9+/]+={0,2}).*$")
                .matcher(base64);

        if (matcher.matches())
            return matcher.group("b64");
        else
            throw new IllegalArgumentException("Not a legal Base64 content");
    }

    public static String encodeMultipartFileToBase64(MultipartFile file) {
        String content = FileUtils.convertToBase64(file);
        if (isNull(content) || content.isEmpty()) {
            throw new BadRequestException(FILE_CONVERT_BASE64_EXCEPTION);
        }
        return content;
    }

    public static String makeBasicAuthToken(String clientId, String secret) {
        return Base64.getEncoder().encodeToString((clientId + ":" + secret).getBytes(StandardCharsets.UTF_8));
    }

    public static boolean isBase64(String str) {
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
}
