package su.opencode.digsigcik.synapi.util;

import org.apache.commons.codec.digest.DigestUtils;

public class HashUtils {
    public static String getSha1Hash(byte[] sourceBytes) {
        return DigestUtils.sha1Hex(sourceBytes);
    }
}
