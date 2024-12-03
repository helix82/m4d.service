package su.opencode.digsigcik.m4d.cert;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class CertificateContentWrapper {

    private static final Pattern FORMATTED = Pattern.compile("\\A\\s*(-----BEGIN CERTIFICATE-----[\r\n]{1,2}([\\p{all}]*?)[\r\n]{1,2}-----END CERTIFICATE-----)\\s*?\\z");
    private static final Pattern UNFORMATTED_BASE64_NO_ESCAPES = Pattern.compile("\\A\\s*([-A-Za-z0-9+/]*={0,3})\\s*?\\z");
    private static final Pattern UNFORMATTED_BASE64_WITH_ESCAPES = Pattern.compile("\\A\\s*([-A-Za-z0-9+/\r\n]*={0,3})\\s*?\\z");
    private static final Pattern PEM = Pattern.compile("\\A\\s*(Certificate:\\p{all}*)\\s*?\\z");

    private static CertificateFactory factory;

    static {
        try {
            factory = CertificateFactory.getInstance("X.509");
        } catch (CertificateException ignored) {}
    }

    @Getter
    private final byte[] content;
    @Getter private final CertificateFileType type;
    @Getter private String group;                           /* effectively final */


    public CertificateContentWrapper(String content) {
        this(content, false);
    }

    public CertificateContentWrapper(String content, boolean decodeFromBase64) {
        this(content.getBytes(StandardCharsets.UTF_8), decodeFromBase64);
    }

    public CertificateContentWrapper(byte[] content) {
        this(content, false);
    }

    public CertificateContentWrapper(byte[] content, boolean decodeFromBase64) {

        this.content = decodeFromBase64
                ? Base64.getDecoder().decode(content)
                : content;

        this.type = detectCertContentType(
                new String(content, StandardCharsets.UTF_8)
        );
    }

    public X509Certificate decodeToX509() throws CertificateParsingException {

        byte[] actualParseableCertificateContent = contentIsPrettyAndVeryParseable() ? content :
                encloseInFormattingTags(group).getBytes(StandardCharsets.UTF_8);

        try {
            return (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(
                    actualParseableCertificateContent
            ));
        } catch (CertificateException e) {
            throw new CertificateParsingException(
                    String.format("Декодируемые сертификат невалиден: [%s..%s]", group.substring(0, 10), group.substring(group.length() - 10)),
                    e
            );
        }
    }

    public boolean contentIsPrettyAndVeryParseable() {
        return type == CertificateFileType.BINARY || type == CertificateFileType.PEM || type == CertificateFileType.FORMATTED;
    }

    // Очень ленивая проверка, не валидирует 100%
    private CertificateFileType detectCertContentType(String content) {

        Matcher matcher;
        matcher = FORMATTED.matcher(content);
        if (matcher.find()) {
            group = matcher.group();
            return CertificateFileType.FORMATTED;
        }

        matcher = UNFORMATTED_BASE64_NO_ESCAPES.matcher(content);
        if (matcher.find()) {
            group = matcher.group();
            return CertificateFileType.BASE64_LINE_NOT_ESCAPED;
        }

        matcher = UNFORMATTED_BASE64_WITH_ESCAPES.matcher(content);
        if (matcher.find()) {
            group = matcher.group();
            return CertificateFileType.BASE64_LINE_ESCAPED;
        }

        matcher = PEM.matcher(content);
        if (matcher.find()) {
            group = matcher.group();
            return CertificateFileType.PEM;
        }

        group = content.strip();
        return CertificateFileType.BINARY;
    }

    public static byte[] tryParseBase64(String content) {
        return tryParseBase64(content.getBytes(StandardCharsets.ISO_8859_1));
    }

    public static byte[] tryParseBase64(byte[] content) {
        try {
            return Base64.getDecoder().decode(content);
        } catch (IllegalArgumentException isNotBase64Encoded) {
            return null;
        }
    }

    public String encloseInFormattingTags(byte[] content) {
        return encloseInFormattingTags(new String(content, StandardCharsets.UTF_8));
    }

    public String encloseInFormattingTags(String content) {
        String beginningTag = "-----BEGIN CERTIFICATE-----";
        String endingTag = "-----END CERTIFICATE-----";

        return content.startsWith(beginningTag)
                ? content
                : String.format("%s\n%s\n%s", beginningTag, content, endingTag);
    }
}
