package su.opencode.digsigcik.synapi.util;

import lombok.Getter;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HashUtils {
    public static String getSha1Hash(byte[] sourceBytes) {
        //        try {
//            MessageDigest msdDigest = MessageDigest.getInstance("SHA-1");
//            msdDigest.update(input.getBytes(StandardCharsets.UTF_8));
//            sha1 = DatatypeConverter.printHexBinary(msdDigest.digest());
//        } catch (NoSuchAlgorithmException e) {
//            Logger.getLogger(HashUtils.class.getName()).log(Level.SEVERE, null, e);
//        }
        return DigestUtils.sha1Hex(sourceBytes);
    }
}
