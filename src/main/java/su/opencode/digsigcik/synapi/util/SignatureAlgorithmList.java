package su.opencode.digsigcik.synapi.util;

import java.security.Provider;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.List;

public class SignatureAlgorithmList {
    public static List<String> getSignatureAlgorithmList() {
        for (Provider provider: Security.getProviders()) {
            System.out.println(provider.getName());
            for (String key: provider.stringPropertyNames())
                if (provider.getProperty(key).contains("Signature")) {
                    System.out.println("\t" + key + "\t" + provider.getProperty(key));
                }
        }
        return null;
    }

    public static void main(String[] args) {
        List<String> algorithms = getSignatureAlgorithmList();
        for (String algorithm : algorithms) {
             //   System.out.println(algorithm);
        }
    }
}


//Signature.SHA1withRSA
//Signature.SHA512/256withRSA
//Signature.MD5withRSA
//Signature.SHA224withRSA
//Signature.SHA512withRSA
//Signature.RSASSA-PSS
//Signature.SHA384withRSA
//Signature.SHA256withRSA
//Signature.MD2withRSA
//Signature.SHA512/224withRSA
//Signature.SHA256withECDSAinP1363Format
//Signature.SHA1withECDSAinP1363Format
//Signature.SHA512withECDSA
//Signature.SHA256withECDSA
//Signature.SHA512withECDSAinP1363Format
//Signature.SHA384withECDSA
//Signature.SHA384withECDSAinP1363Format
//Signature.NONEwithECDSAinP1363Format
//Signature.SHA224withECDSAinP1363Format
//Signature.NONEwithECDSA
//Signature.SHA1withECDSA
//Signature.SHA224withECDSA
//Signature.MD5andSHA1withRSA
//Signature.SHA1withRSA
//Signature.SHA512/256withRSA
//Signature.MD5withRSA
//Signature.SHA224withRSA
//Signature.SHA512withRSA
//Signature.RSASSA-PSS
//Signature.SHA384withRSA
//Signature.SHA256withRSA
//Signature.MD2withRSA
//Signature.SHA512/224withRSA
//Signature.SHA1withRSA
//Signature.SHA512withECDSA
//Signature.SHA256withECDSA
//Signature.MD5withRSA
//Signature.SHA512withRSA
//Signature.SHA384withECDSA
//Signature.RSASSA-PSS
//Signature.SHA384withRSA
//Signature.SHA256withRSA
//Signature.NONEwithRSA
//Signature.MD2withRSA
//Signature.SHA1withECDSA
//Signature.SHA224withECDSA