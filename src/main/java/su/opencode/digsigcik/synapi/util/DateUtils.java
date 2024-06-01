package su.opencode.digsigcik.synapi.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {
    public static String formatDate(LocalDateTime operationDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm-ss.SSSSSSSSS");
        return operationDate.format(formatter);
    }
}
