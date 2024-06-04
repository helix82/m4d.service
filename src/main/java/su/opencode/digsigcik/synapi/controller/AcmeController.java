package su.opencode.digsigcik.synapi.controller;


import lombok.AllArgsConstructor;
import org.shredzone.acme4j.Account;
import org.shredzone.acme4j.AccountBuilder;
import org.shredzone.acme4j.Authorization;
import org.shredzone.acme4j.Certificate;
import org.shredzone.acme4j.Order;
import org.shredzone.acme4j.Problem;
import org.shredzone.acme4j.Session;
import org.shredzone.acme4j.Status;
import org.shredzone.acme4j.challenge.Challenge;
import org.shredzone.acme4j.challenge.Http01Challenge;
import org.shredzone.acme4j.exception.AcmeException;
import org.shredzone.acme4j.util.KeyPairUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.http.HttpResponse;
import java.security.KeyPair;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

@RestController
@RequestMapping("/api/v1/acme")
@AllArgsConstructor
public class AcmeController {
    private static final String CA_URI = "https://acme.server:4433/acme/acme/directory";
    private static final int MAX_ATTEMPTS = 50;


    private KeyPair makeKeyPair(MultipartFile userFile) throws IOException {
        return KeyPairUtils.readKeyPair(new BufferedReader(new InputStreamReader(userFile.getInputStream())));
    }
    private Http01Challenge challenge(Authorization auth) throws AcmeException {
        if (auth.getStatus() == Status.VALID) {
            // авторизация уже выполнена
            return null;
        }
        Http01Challenge challenge = auth.findChallenge(Http01Challenge.class)
                .orElseThrow(() -> new AcmeException("Found no " + Http01Challenge.TYPE
                        + " challenge, don't know what to do..."));

        // If the challenge is already verified, there's no need to execute it again.
        if (challenge.getStatus() == Status.VALID) {
            // испытание же пройдено
            return null;
        }
        return challenge;
    }
    @FunctionalInterface
    private interface UpdateMethod {
        Optional<Instant> updateAndGetRetryAfter() throws AcmeException;
    }

    private Status waitForCompletion(Supplier<Status> statusSupplier, UpdateMethod statusUpdater)
            throws AcmeException {
        // A set of terminating status values
        Set<Status> acceptableStatus = EnumSet.of(Status.VALID, Status.INVALID);

        // Limit the number of checks, to avoid endless loops
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            Instant now = Instant.now();
            // Update the status property
            Instant retryAfter = statusUpdater.updateAndGetRetryAfter()
                    .orElse(now.plusSeconds(3L));
            // Check the status
            Status currentStatus = statusSupplier.get();
            if (acceptableStatus.contains(currentStatus)) {
                // Reached VALID or INVALID, we're done here
                return currentStatus;
            }
            // Wait before checking again
            try {
                Thread.sleep(now.until(retryAfter, ChronoUnit.MILLIS));
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new AcmeException("interrupted");
            }
        }

        throw new AcmeException("Too many update attempts, status did not change");
    }

    private void doChallenge(String domain, String uploadPath, Http01Challenge challenge ) {
        try {
            URL url = new URL("http://"+domain + uploadPath + "/" + challenge.getToken());
            HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
            httpCon.setDoOutput(true);
            httpCon.setRequestMethod("PUT");
            OutputStreamWriter out = new OutputStreamWriter(httpCon.getOutputStream());
            out.write(challenge.getAuthorization());
            out.close();
            httpCon.getInputStream();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void clearChallenge(String domain, String uploadPath, Http01Challenge challenge) {
        try {
            URL url = new URL("http://"+domain + uploadPath + "/" + challenge.getToken());
            HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
            httpCon.setDoOutput(true);
            httpCon.setRequestMethod("DELETE");
            httpCon.getInputStream();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @PostMapping("/orders")
    public String orders(HttpSession httpSession,
                      @RequestParam("domain") String domain,
                      @RequestPart(value = "userKey") MultipartFile userKey) {
String ret = "1:";
        try {
            Session session = new Session(CA_URI);
            KeyPair userKeyPair = null;
            userKeyPair = makeKeyPair(userKey);
            Account acct = new AccountBuilder()
                    .useKeyPair(userKeyPair)
                    .create(session);


            Iterator<Order> it = acct.getOrders();
            while(it.hasNext()) {
                Order o = it.next();
                ret += o.toString();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (AcmeException e) {
            e.printStackTrace();
        }
return ret;
    }



    @PostMapping("/auto")
    public Map auto(HttpSession httpSession,
                     @RequestParam("domain") String domain,
                     @RequestPart(value = "userKey") MultipartFile userKey,
                     @RequestPart("domainKey") MultipartFile domainKey) {
        HashMap<String, String> ret = new HashMap<>();
        try {
            Session session = new Session(CA_URI);
            KeyPair userKeyPair = makeKeyPair(userKey);
            Account acct = new AccountBuilder()
                    .useKeyPair(userKeyPair)
                    .create(session);
            Order order = acct
                    .newOrder()
                    .domain(domain)
                    .create();

            Http01Challenge challenge = challenge(order.getAuthorizations().get(0));
            if(challenge==null) {
                // авторизация уже прошла или испытание выполнено
                ret.put("result", "done");
                ret.put("description", "Auth and challenge done");
                return ret;
            }
            doChallenge(domain, "/upload", challenge);

            challenge.trigger();

            KeyPair domainKeyPair = makeKeyPair(domainKey);

            order.execute(domainKeyPair);

            // Wait for the order to complete
            Status status = waitForCompletion(order::getStatus, order::fetch);
            if (status != Status.VALID) {
                throw new AcmeException("Order failed... Giving up.");
            }

            clearChallenge(domain, "/upload", challenge);


            Certificate certificate = order.getCertificate();
            StringWriter writer = new StringWriter();
            certificate.writeCertificate(writer);
            ret.put("url", certificate.getLocation().toString());
            ret.put("content", writer.toString());
            return ret;
        } catch (IOException e) {
            ret.put("error", "io exception");
            return ret;
        } catch (AcmeException e) {
            ret.put("error", "acme exception");
            return ret;
        }
    }


    @PostMapping("/order")
    public Map order(HttpSession httpSession,
                    @RequestParam("domain") String domain,
                    @RequestPart(value = "userKey") MultipartFile userKey) {
        HashMap<String, String> ret = new HashMap<>();
        try {
            Session session = new Session(CA_URI);
            KeyPair userKeyPair = makeKeyPair(userKey);
            Account acct = new AccountBuilder()
                    .useKeyPair(userKeyPair)
                    .create(session);
            Order order = acct
                    .newOrder()
                    .domain(domain)
                    .create();

            Http01Challenge challenge = challenge(order.getAuthorizations().get(0));
            if(challenge==null) {
                // авторизация уже прошла или испытание выполнено
                ret.put("result", "done");
                ret.put("description", "Auth and challenge done");
                return ret;
            }
            httpSession.setAttribute("order", order);
            ret.put("token", challenge.getToken());
            ret.put("authorization", challenge.getAuthorization());
            return ret;
        } catch (IOException e) {
            ret.put("error", "io exception");
            return ret;
        } catch (AcmeException e) {
            ret.put("error", "acme exception");
            return ret;
        }
    }



    @PostMapping("/certificate")
    public Map certificate(HttpSession httpSession,
                      @RequestParam("domain") String domain,
                      @RequestPart("domainKey") MultipartFile domainKey) {
        HashMap<String, String> ret = new HashMap<>();

        Order order = (Order)httpSession.getAttribute("order");
        if(order == null) {
            ret.put("error", "no order");
            return ret;
        }
        try {
            Optional<Http01Challenge> challenge = order.getAuthorizations().get(0).findChallenge(Http01Challenge.class);
            if(challenge.isEmpty()){
                ret.put("error", "no challenge");
                return ret;
            }
            challenge.get().trigger();

            KeyPair domainKeyPair = makeKeyPair(domainKey);

            order.execute(domainKeyPair);

            // Wait for the order to complete
            Status status = waitForCompletion(order::getStatus, order::fetch);
            if (status != Status.VALID) {
                throw new AcmeException("Order failed... Giving up.");
            }
            Certificate certificate = order.getCertificate();
            StringWriter writer = new StringWriter();
            certificate.writeCertificate(writer);
            ret.put("url", certificate.getLocation().toString());
            ret.put("content", writer.toString());
            return ret;
        } catch (IOException e) {
            ret.put("error", "io exception");
            return ret;
        } catch (AcmeException e) {
            ret.put("error", "acme exception");
            return ret;
        }
    }

    @PostMapping("/download")
    public String download(HttpSession httpSession,
                      HttpServletResponse httpResponse,
                      @RequestParam(value = "domain", required = false) String domain,
                      @RequestPart(value = "domainKey", required = false) MultipartFile domainKey) throws IOException {
        Order order = (Order)httpSession.getAttribute("order");
        if(order == null) {
            httpResponse.sendError(400,  "no order");
            return "";
        }
        try {
            Optional<Http01Challenge> challenge = order.getAuthorizations().get(0).findChallenge(Http01Challenge.class);
            if(challenge.isEmpty()){
                httpResponse.sendError(400,  "no challenge");
                return "";
            }
            challenge.get().trigger();

            KeyPair domainKeyPair = makeKeyPair(domainKey);

            order.execute(domainKeyPair);

            // Wait for the order to complete
            Status status = waitForCompletion(order::getStatus, order::fetch);
            if (status != Status.VALID) {
                throw new AcmeException("Order failed... Giving up.");
            }
            Certificate certificate = order.getCertificate();
            StringWriter writer = new StringWriter();
            certificate.writeCertificate(writer);
            httpResponse.setContentType ("application/x-x509-user-cert");
            httpResponse.setHeader ("Content-Disposition", "attachment; filename="+domain+"-chain.crt");
            return writer.toString();
        } catch (IOException e) {
            httpResponse.sendError(400,  "io exception");
        } catch (AcmeException e) {
            httpResponse.sendError(400,  "acme exception");
        }
        return "";
    }
}
