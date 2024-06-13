package su.opencode.digsigcik.synapi.controller;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.shredzone.acme4j.Account;
import org.shredzone.acme4j.AccountBuilder;
import org.shredzone.acme4j.Authorization;
import org.shredzone.acme4j.Certificate;
import org.shredzone.acme4j.Login;
import org.shredzone.acme4j.Order;
import org.shredzone.acme4j.RevocationReason;
import org.shredzone.acme4j.Session;
import org.shredzone.acme4j.Status;
import org.shredzone.acme4j.challenge.Http01Challenge;
import org.shredzone.acme4j.exception.AcmeException;
import org.shredzone.acme4j.util.KeyPairUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.acme.ChallengeStore;
import su.opencode.digsigcik.synapi.config.PropertyConfig;
import su.opencode.digsigcik.synapi.model.AcmeAccount;
import su.opencode.digsigcik.synapi.model.AcmeCertificate;
import su.opencode.digsigcik.synapi.model.AcmeDomain;
import su.opencode.digsigcik.synapi.repository.AccountRepository;
import su.opencode.digsigcik.synapi.repository.CertificateRepository;
import su.opencode.digsigcik.synapi.repository.DomainRepository;
import su.opencode.digsigcik.synapi.service.AcmeService;
import su.opencode.digsigcik.synapi.service.ContainerService;

import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.KeyPair;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

@RestController
@RequestMapping("/api/v1")
@AllArgsConstructor
@Slf4j
public class AcmeController {
    private final AcmeService acmeService;


    @GetMapping("/accounts/{id}/login")
    public Map login(HttpSession httpSession,
                    @PathVariable Long id) {
        return acmeService.login(httpSession,id);
    }

    @PostMapping("/accounts/{id}/register")
    public Map register(HttpSession httpSession,
                        @PathVariable Long id) {
        return acmeService.register(httpSession,id);
    }

    @GetMapping("/accounts/{id}/logout")
    public Map logout(HttpSession httpSession, @PathVariable Long id) {
        return acmeService.logout(httpSession,id);
    }

    @PostMapping("/certificate")
    public ResponseEntity<Map>  certificate(HttpSession httpSession,
                                           @RequestParam(value = "domainName") String domainName,
                                           @RequestParam(value = "notAfter") String notAfter
    ) {
        return acmeService.certificate(httpSession,domainName, notAfter);
    }

    @PostMapping("/domains/{domainId}/certificate")
    public Map certificate(HttpSession httpSession,
                      @PathVariable(value = "domainId") Long domainId
                      ) {
        return acmeService.certificate(httpSession,domainId);
    }

    @PostMapping("/certificates/{id}/revoke")
    public Map revoke(HttpSession httpSession,
                           @PathVariable(value = "id") Long id
    ) {
        return acmeService.revoke(httpSession,id);
    }


/*

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
    @PostMapping("/register")
    public Map register(HttpSession httpSession,
                     @RequestPart(value = "externalKey") String externalKey) {
        HashMap<String, String> ret = new HashMap<>();
        AcmeAccount acmeAccount = accountService.findByExternalKey(externalKey);
        if(acmeAccount != null) {
            Session session = new Session(CA_URI);
            try {
                KeyPair userKeyPair = makeKeyPair(acmeAccount.getAccountPrivateKey());
                Account acct = new AccountBuilder()
                        .addContact("mailto:acme@example.com")
                        .agreeToTermsOfService()
                        .useKeyPair(userKeyPair)
                        .create(session);
                Login login = session.login(acct.getLocation(), userKeyPair);
                httpSession.setAttribute("login", login);
                acmeAccount.setAccountUrl(acct.getLocation().toString());
                accountService.saveAccount(acmeAccount);
                ret.put("result", "done");
            } catch (IOException e) {
                ret.put("error", "io exception");
                ret.put("exception", e.getMessage());
            } catch (AcmeException e) {
                ret.put("error", "acme exception");
                ret.put("exception", e.getMessage());
            }
        }
        return ret;
    }


    @GetMapping("/orders")
    public Map orders(HttpSession httpSession) {
        HashMap<String, Object> ret = new HashMap<>();
        Login login = (Login)httpSession.getAttribute("login");
        if(login!=null) {
            Account account = login.getAccount();
            if(account.getStatus() != Status.VALID){
                ret.put("error", "invalid status");
            } else {
//                try {
                //login.bin
                    ArrayList<Order> orders = new ArrayList<>();
                    Iterator<Order> orderIt = account.getOrders();
                    while(orderIt.hasNext()){
                        Order o = orderIt.next();
                        orders.add(o);
                    }
//                } catch (AcmeException e) {
//                    ret.put("error", "acme exception");
//                    ret.put("exception", e.getMessage());
//                } catch (IOException e) {
//                    ret.put("error", "io exception");
//                    ret.put("exception", e.getMessage());
//                }
                ret.put("orders",orders);
            }
        } else {
            ret.put("error", "no login");
        }
        return ret;
    }

    @PostMapping("/certificate")
    public Map auto(HttpSession httpSession,
                     @RequestParam("domain") String domain,
                     @RequestPart("domainKey") MultipartFile domainKey) {
        HashMap<String, String> ret = new HashMap<>();
        Login login = (Login)httpSession.getAttribute("login");
        if(login!=null) {
            Account account = login.getAccount();
            if(account.getStatus() != Status.VALID){
                ret.put("error", "invalid status");
            } else {

                try {
                    Order order = account
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
                } catch (AcmeException e) {
                    ret.put("error", "acme exception");
                    ret.put("exception", e.getMessage());
                } catch (IOException e) {
                    ret.put("error", "io exception");
                    ret.put("exception", e.getMessage());
                }
            }
        } else {
            ret.put("error", "no login");
        }
        return ret;
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



    @PostMapping("/certificate1")
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
    }*/
}
