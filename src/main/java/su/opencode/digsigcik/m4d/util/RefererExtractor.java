package su.opencode.digsigcik.m4d.util;

import lombok.experimental.UtilityClass;
import su.opencode.digsigcik.m4d.exception.BadRequestException;

import javax.servlet.http.HttpServletRequest;
import java.net.URL;

@UtilityClass
public class RefererExtractor {

    public static String extractDomainFromReferer(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isEmpty()) {
            try {
                URL url = new URL(referer);
                return url.getProtocol() + "://" + url.getHost();
            } catch (Exception e) {
                throw new BadRequestException("Referer заголовок отсутствует или URL");
            }
        }
        throw new BadRequestException("Referer заголовок отсутствует");
    }
}

