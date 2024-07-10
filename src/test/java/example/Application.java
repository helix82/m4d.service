package example;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.reprov.RevCheck;
import ru.CryptoPro.ssl.Provider;
import su.opencode.digsigcik.synapi.client.RestClient;
import su.opencode.digsigcik.synapi.client.RestMethod;
import su.opencode.digsigcik.synapi.client.RestRequest;

import java.net.URI;
import java.security.Security;
import java.util.HashMap;

@Slf4j
public class Application {
    public static void main(String[] args) throws Exception {
        try {
            Security.addProvider(new BouncyCastleProvider());
            //   Security.addProvider(new JCSP()); // провайдер JCSP
            Security.addProvider(new JCP()); // провайдер JCP
            //  Security.addProvider(new JS()); // провайдер JCP
            Security.addProvider(new RevCheck());
            Security.addProvider(new Provider());
        } catch (Exception e) {
            System.err.println("Exception in initializing crypto pro : " + e);
        } catch (Error er) {
            System.err.println("Error in initializing crypto pro : " + er);
        }

        RestClient restClient = new RestClient();
//            HashMap<String, String> headers = new HashMap<>();
//            headers.put("Accept", "application/json");
//            RestRequest restRequest = RestRequest.builder()
//                    .httpMethod(RestMethod.GET)
//                    .requestUri(new URI("https://keylab/verify/"))
//                    .requestContent("")
//                    .httpHeader(headers)
//                    .build();
//            restClient.requestString(restRequest);
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        RestRequest restRequest = RestRequest.builder()
                .httpMethod(RestMethod.POST)
                .requestUri(new URI("https://keylab/vs/api/documents/reset/all"))
                .requestContent("")
                .httpHeader(headers)
                .build();
        restClient.requestString(restRequest);
        log.info("Connection was successfully");

    }

}