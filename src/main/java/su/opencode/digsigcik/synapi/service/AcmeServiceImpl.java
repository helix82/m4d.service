package su.opencode.digsigcik.synapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.shredzone.acme4j.*;
import org.shredzone.acme4j.challenge.Challenge;
import org.shredzone.acme4j.challenge.Http01Challenge;
import org.shredzone.acme4j.exception.AcmeException;
import org.shredzone.acme4j.util.KeyPairUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import su.opencode.digsigcik.synapi.acme.ChallengeStore;
import su.opencode.digsigcik.synapi.controller.CertificateController;
import su.opencode.digsigcik.synapi.model.AcmeAccount;
import su.opencode.digsigcik.synapi.model.AcmeCertificate;
import su.opencode.digsigcik.synapi.model.AcmeDomain;
import su.opencode.digsigcik.synapi.model.AcmeOrder;
import su.opencode.digsigcik.synapi.model.AcmeProvider;
import su.opencode.digsigcik.synapi.repository.AccountRepository;
import su.opencode.digsigcik.synapi.repository.CertificateRepository;
import su.opencode.digsigcik.synapi.repository.DomainRepository;
import su.opencode.digsigcik.synapi.repository.OrderRepository;
import su.opencode.digsigcik.synapi.repository.ProviderRepository;
import su.opencode.digsigcik.synapi.util.DateUtils;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.KeyPair;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Supplier;


@Service
@RequiredArgsConstructor
@Slf4j
public class AcmeServiceImpl implements AcmeService {
    @Value("${tls.ca.uri}")
    private String CA_URI;
    @Value("${tls.account.name}")
    private String ACME_ACCOUNT_NAME;
    @Value("${tls.account.mail}")
    private String ACME_ACCOUNT_EMAIL;
    @Value("${tls.account.key}")
    private  String ACME_ACCOUNT_KEY;
    @Value("${tls.max_attempts}")
    private int MAX_ATTEMPTS;
    @Value("${tls.external_acknowledge}")
    private boolean EXTERNAL_ACKNOWLEDGE;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    DomainRepository domainRepository;

    @Autowired
    ProviderRepository providerRepository;

    @Autowired
    CertificateRepository certificateRepository;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    ChallengeStore challengeStore;

    public Map orderInfo(AcmeOrder order) {
        HashMap<String, Object> ret = new HashMap<>();

        ret.put("id", order.getId());
        ret.put("deleted", order.getDeleted());
        AcmeProvider provider = order.getProvider();
        if(provider!=null) {
            ret.put("provider", String.valueOf(provider.getName()));
            ret.put("providerId", String.valueOf(provider.getId()));
        }
        AcmeDomain domain = order.getDomain();
        ret.put("domain", String.valueOf(domain.getDomainUrl()));
        if(order.getNotAfter()!=null){
            ret.put("notAfter", String.valueOf(new SimpleDateFormat("yyyy-MM-dd").format(order.getNotAfter())));
        }
        if(order.getNotBefore()!=null) {
            ret.put("notBefore", String.valueOf(new SimpleDateFormat("yyyy-MM-dd").format(order.getNotBefore())));
        }
        ret.put("challengeToken", order.getChallengeToken());
        ret.put("challengeContent", order.getChallengeContent());

        Map<String, String[]> data = order.getData();
        if(data != null && !data.isEmpty()) {
            ret.put("data", order.getData());
        }
        if(!certificateRepository.findByOrderId(order.getId()).isEmpty()){
            ret.put("status", "released");
        } else {
            ret.put("status", "challenge");
        }
        if(order.getDeleted()) {
            ret.put("status", "deleted");
        }
        return ret;
    }


    public ResponseEntity<Map> login(HttpSession httpSession, Long id) {
        HashMap<String, String> ret = new HashMap<>();
        AcmeAccount acmeAccount = accountRepository.findAccountById(id);
        if(acmeAccount != null) {
            Session session = new Session(CA_URI);
            try {
                if(acmeAccount.getAccountPrivateKey()==null) {
                    StringWriter sw = new StringWriter();
                    KeyPairUtils.writeKeyPair(KeyPairUtils.createKeyPair(), sw);
                    acmeAccount.setAccountPrivateKey(sw.toString());
                    accountRepository.save(acmeAccount);
                }
                KeyPair userKeyPair = makeKeyPair(acmeAccount.getAccountPrivateKey());
                Login login = new AccountBuilder()
                        .onlyExisting()         // Do not create a new acmeAccount
                        .agreeToTermsOfService()
                        .useKeyPair(userKeyPair)
                        .createLogin(session);
                httpSession.setAttribute("login", login);
                ret.put("result", "done");
                return new ResponseEntity<>(ret, HttpStatus.OK);
            } catch (IOException e) {
                ret.put("error", "io exception");
                ret.put("exception", e.getMessage());
                return new ResponseEntity<>(ret, HttpStatus.BAD_REQUEST);
            } catch (AcmeException e) {
                ret.put("error", "acme exception");
                ret.put("exception", e.getMessage());
                return new ResponseEntity<>(ret, HttpStatus.BAD_REQUEST);
            }
        } else {
            ret.put("error", "no account");
            return new ResponseEntity<>(ret, HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<Map> register(HttpSession httpSession,Long id) {
        HashMap<String, String> ret = new HashMap<>();
        AcmeAccount acmeAccount = accountRepository.findAccountById(id);
        if(acmeAccount != null) {
            Session session = new Session(CA_URI);
            try {
                if(acmeAccount.getAccountPrivateKey()==null) {
                    StringWriter sw = new StringWriter();
                    KeyPairUtils.writeKeyPair(KeyPairUtils.createKeyPair(), sw);
                    acmeAccount.setAccountPrivateKey(sw.toString());
                    accountRepository.save(acmeAccount);
                }
                KeyPair userKeyPair = makeKeyPair(acmeAccount.getAccountPrivateKey());
                Account acct = new AccountBuilder()
                        .addContact("mailto:"+acmeAccount.getEmail())
                        .agreeToTermsOfService()
                        .useKeyPair(userKeyPair)
                        .create(session);
                Login login = session.login(acct.getLocation(), userKeyPair);
                httpSession.setAttribute("login", login);
                acmeAccount.setAccountUrl(acct.getLocation().toString());
                accountRepository.save(acmeAccount);
                ret.put("result", "done");
                return new ResponseEntity<>(ret, HttpStatus.OK);
            } catch (IOException e) {
                ret.put("error", "io exception");
                ret.put("exception", e.getMessage());
                return new ResponseEntity<>(ret, HttpStatus.BAD_REQUEST);
            } catch (AcmeException e) {
                ret.put("error", "acme exception");
                ret.put("exception", e.getMessage());
                return new ResponseEntity<>(ret, HttpStatus.BAD_REQUEST);
            }
        } else {
            ret.put("error", "no account");
            return new ResponseEntity<>(ret, HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<Map> logout(HttpSession httpSession, Long id) {
        HashMap<String, String> ret = new HashMap<>();
        AcmeAccount acmeAccount = accountRepository.findAccountById(id);
        if(acmeAccount != null) {
            httpSession.removeAttribute("login");
            ret.put("result", "done");
            return new ResponseEntity<>(ret, HttpStatus.OK);
        } else {
            ret.put("error", "no account");
            return new ResponseEntity<>(ret, HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<Map> certificate(HttpSession httpSession,String domain, String notAfter, Long providerId, Map data) {
        HashMap<String, String> ret = new HashMap<>();
        AcmeAccount acmeAccount = accountRepository.findAccountByExternalKey(ACME_ACCOUNT_KEY);
        if(acmeAccount==null) {
            acmeAccount = new AcmeAccount(ACME_ACCOUNT_NAME, ACME_ACCOUNT_KEY);
            acmeAccount.setEmail(ACME_ACCOUNT_EMAIL);
            accountRepository.save(acmeAccount);
        }
        AcmeDomain acmeDomain= domainRepository.findDomainByDomainUrl(domain);
        if(acmeDomain==null) {
            acmeDomain = new AcmeDomain(acmeAccount, domain);
            domainRepository.save(acmeDomain);
        }
        AcmeProvider acmeProvider = providerRepository.findById(providerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found AcmeProvider with id = " + providerId));

        Date notAfterDate = null;
        try {
            notAfterDate = DateUtils.parse(notAfter);
        } catch (ParseException e) {
            notAfterDate = new Date();
        }

        if(loginDefaultAccount(httpSession)){
            Login login = (Login)httpSession.getAttribute("login");
            try {
                Account account = login.getAccount();
                Order order = account
                        .newOrder()
                        .domain(acmeDomain.getDomainUrl())
                        .notAfter(notAfterDate.toInstant())
                        .create();

                Http01Challenge challenge = challenge(order.getAuthorizations().get(0));
                if(challenge==null) {
                    // авторизация уже прошла или испытание выполнено
                    ret.put("result", "done");
                    ret.put("description", "Auth and challenge done");
                    return new ResponseEntity<>(ret, HttpStatus.OK);
                }

                AcmeOrder acmeOrder = new AcmeOrder(acmeDomain);
                acmeOrder.setOrderUrl(order.getLocation().toString());
                acmeOrder.setProvider(acmeProvider);
                acmeOrder.setDomain(acmeDomain);
                acmeOrder.setNotAfter(notAfterDate);
                acmeOrder.setChallengeUrl(challenge.getLocation().toString());
                acmeOrder.setChallengeToken(challenge.getToken());
                acmeOrder.setChallengeContent(challenge.getAuthorization());
                acmeOrder.setData(data);
                orderRepository.save(acmeOrder);

                doChallenge(acmeDomain.getDomainUrl(), "/upload", challenge);

                challenge.trigger();

                if(acmeDomain.getDomainPrivateKey()==null) {
                    StringWriter sw = new StringWriter();
                    KeyPairUtils.writeKeyPair(KeyPairUtils.createKeyPair(4096), sw);
                    acmeDomain.setDomainPrivateKey(sw.toString());
                    domainRepository.save(acmeDomain);
                }
                KeyPair domainKeyPair = makeKeyPair(acmeDomain.getDomainPrivateKey());

                order.execute(domainKeyPair);

                // Wait for the order to complete
                Status status = waitForCompletion(order::getStatus, order::fetch);
                if (status != Status.VALID) {
                    throw new AcmeException("Order failed... Giving up.");
                }
                if (EXTERNAL_ACKNOWLEDGE) {
                    clearChallenge(acmeDomain.getDomainUrl(), "/upload", challenge);
                }

                Certificate certificate = order.getCertificate();

                StringWriter writer = new StringWriter();
                certificate.writeCertificate(writer);

                AcmeCertificate acmeCertificate = new AcmeCertificate(acmeDomain, writer.toString());
                acmeCertificate.setOrder(acmeOrder);
                acmeCertificate.setNotAfter(acmeOrder.getNotAfter());
                acmeCertificate.setProvider(acmeOrder.getProvider());
                acmeCertificate.setCertificateUrl(certificate.getLocation().toString());
                certificateRepository.save(acmeCertificate);

                return new ResponseEntity<>(CertificateController.certInfo(acmeCertificate), HttpStatus.OK);
            } catch (AcmeException e) {
                ret.put("error", "acme exception");
                ret.put("exception", e.getMessage());
                return new ResponseEntity<>(ret, HttpStatus.BAD_REQUEST);
            } catch (IOException e) {
                ret.put("error", "io exception");
                ret.put("exception", e.getMessage());
                return new ResponseEntity<>(ret, HttpStatus.BAD_REQUEST);
            }
        } else {
            ret.put("error", "no login");
            return new ResponseEntity<>(ret, HttpStatus.UNAUTHORIZED);
        }
    }

    public ResponseEntity<Map> certificateOrder(HttpSession httpSession,String domain, String notAfter, Long providerId, Map data) {
        HashMap<String, String> ret = new HashMap<>();
        AcmeAccount acmeAccount = accountRepository.findAccountByExternalKey(ACME_ACCOUNT_KEY);
        if(acmeAccount==null) {
            acmeAccount = new AcmeAccount(ACME_ACCOUNT_NAME, ACME_ACCOUNT_KEY);
            acmeAccount.setEmail(ACME_ACCOUNT_EMAIL);
            accountRepository.save(acmeAccount);
        }
        AcmeDomain acmeDomain= domainRepository.findDomainByDomainUrl(domain);
        if(acmeDomain==null) {
            acmeDomain = new AcmeDomain(acmeAccount, domain);
            domainRepository.save(acmeDomain);
        }
        Date notAfterDate = null;
        try {
            notAfterDate = DateUtils.parse(notAfter);
        } catch (ParseException e) {
            notAfterDate = new Date();
        }

        AcmeProvider acmeProvider = providerRepository.findById(providerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found AcmeProvider with id = " + providerId));

        if(loginDefaultAccount(httpSession)){
            Login login = (Login)httpSession.getAttribute("login");
            try {
                Account account = login.getAccount();
                Order order = account
                        .newOrder()
                        .domain(acmeDomain.getDomainUrl())
                        .notAfter(notAfterDate.toInstant())
                        .create();

                Http01Challenge challenge = challenge(order.getAuthorizations().get(0));
                if(challenge==null) {
                    // авторизация уже прошла или испытание выполнено
                    ret.put("result", "done");
                    ret.put("description", "Auth and challenge done");
                    return new ResponseEntity<>(ret, HttpStatus.OK);
                }
                ret.put("token", challenge.getToken());
                ret.put("authorization", challenge.getAuthorization());

                AcmeOrder acmeOrder = new AcmeOrder(acmeDomain);
                acmeOrder.setOrderUrl(order.getLocation().toString());
                acmeOrder.setProvider(acmeProvider);
                acmeOrder.setDomain(acmeDomain);
                acmeOrder.setNotAfter(notAfterDate);
                acmeOrder.setChallengeUrl(challenge.getLocation().toString());
                acmeOrder.setChallengeToken(challenge.getToken());
                acmeOrder.setChallengeContent(challenge.getAuthorization());
                acmeOrder.setData(data);
                orderRepository.save(acmeOrder);
                return new ResponseEntity<>(orderInfo(acmeOrder), HttpStatus.OK);
            } catch (AcmeException e) {
                ret.put("error", "acme exception");
                ret.put("exception", e.getMessage());
                return new ResponseEntity<>(ret, HttpStatus.BAD_REQUEST);

            }
        } else {
            ret.put("error", "no login");
            return new ResponseEntity<>(ret, HttpStatus.UNAUTHORIZED);
        }
    }

    public ResponseEntity<String> downloadChallengeFile(HttpServletResponse httpResponse, Long id) {
        AcmeOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found AcmeOrder with id = " + id));
        httpResponse.setContentType ("text/plain");
        httpResponse.setHeader ("Content-Disposition", "attachment; filename="+order.getChallengeToken());
        return new ResponseEntity<>(order.getChallengeContent(), HttpStatus.OK);
    }

    public ResponseEntity<Map> certificateRelease(HttpSession httpSession, Long orderId) {
        HashMap<String, String> ret = new HashMap<>();
        AcmeOrder acmeOrder = orderRepository.findOrderById(orderId);

        if(acmeOrder!=null) {
            if(!certificateRepository.findByOrderId(orderId).isEmpty()) {
                ret.put("error", "certificate already released");
                return new ResponseEntity<>(ret, HttpStatus.BAD_REQUEST);
            } else {
                if(loginDefaultAccount(httpSession)){
                    Login login = (Login)httpSession.getAttribute("login");
                    if(login!=null) {
                        try {
                            AcmeDomain acmeDomain = acmeOrder.getDomain();

                            Challenge challenge = login.bindChallenge(new URL(acmeOrder.getChallengeUrl()));
                            Order order = login.bindOrder(new URL(acmeOrder.getOrderUrl()));

                            challenge.trigger();

                            if(acmeDomain.getDomainPrivateKey()==null) {
                                StringWriter sw = new StringWriter();
                                KeyPairUtils.writeKeyPair(KeyPairUtils.createKeyPair(4096), sw);
                                acmeDomain.setDomainPrivateKey(sw.toString());
                                domainRepository.save(acmeDomain);
                            }
                            KeyPair domainKeyPair = makeKeyPair(acmeDomain.getDomainPrivateKey());

                            order.execute(domainKeyPair);

                            // Wait for the order to complete
                            Status status = waitForCompletion(order::getStatus, order::fetch);
                            if (status != Status.VALID) {
                                throw new AcmeException("Order failed... Giving up.");
                            }

                            Certificate certificate = order.getCertificate();

                            StringWriter writer = new StringWriter();
                            certificate.writeCertificate(writer);

                            AcmeCertificate acmeCertificate = new AcmeCertificate(acmeDomain, writer.toString());
                            acmeCertificate.setOrder(acmeOrder);
                            acmeCertificate.setNotAfter(acmeOrder.getNotAfter());
                            acmeCertificate.setProvider(acmeOrder.getProvider());
                            acmeCertificate.setCertificateUrl(certificate.getLocation().toString());
                            certificateRepository.save(acmeCertificate);
                            return new ResponseEntity<>(CertificateController.certInfo(acmeCertificate), HttpStatus.OK);
                        } catch (AcmeException e) {
                            ret.put("error", "acme exception");
                            ret.put("exception", e.getMessage());
                            return new ResponseEntity<>(ret, HttpStatus.BAD_REQUEST);
                        } catch (IOException e) {
                            ret.put("error", "io exception");
                            ret.put("exception", e.getMessage());
                            return new ResponseEntity<>(ret, HttpStatus.BAD_REQUEST);
                        }
                    } else {
                        ret.put("error", "no login");
                        return new ResponseEntity<>(ret, HttpStatus.UNAUTHORIZED);
                    }
                } else {
                    ret.put("error", "no login");
                    return new ResponseEntity<>(ret, HttpStatus.UNAUTHORIZED);
                }
            }
        } else {
            ret.put("error", "no order");
            return new ResponseEntity<>(ret, HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<Map> certificate(HttpSession httpSession, Long domainId) {
        HashMap<String, String> ret = new HashMap<>();
        AcmeAccount acmeAccount = accountRepository.findAccountByExternalKey(ACME_ACCOUNT_KEY);
        if(acmeAccount==null) {
            acmeAccount = new AcmeAccount(ACME_ACCOUNT_NAME, ACME_ACCOUNT_KEY);
            acmeAccount.setEmail(ACME_ACCOUNT_EMAIL);
            accountRepository.save(acmeAccount);
        }
        AcmeDomain acmeDomain= domainRepository.findDomainById(domainId);
        if(acmeDomain==null) {
            ret.put("error", "no domain");
            return new ResponseEntity<>(ret, HttpStatus.BAD_REQUEST);
        }

        if(loginDefaultAccount(httpSession)){
            Login login = (Login)httpSession.getAttribute("login");
            Account account = login.getAccount();
            if(account.getStatus() != Status.VALID){
                ret.put("error", "invalid status");
                return new ResponseEntity<>(ret, HttpStatus.BAD_REQUEST);
            } else {

                try {
                    Order order = account
                            .newOrder()
                            .domain(acmeDomain.getDomainUrl())
                            .create();

                    Http01Challenge challenge = challenge(order.getAuthorizations().get(0));
                    if(challenge==null) {
                        // авторизация уже прошла или испытание выполнено
                        ret.put("result", "done");
                        ret.put("description", "Auth and challenge done");
                        return new ResponseEntity<>(ret, HttpStatus.OK);
                    }
                    doChallenge(acmeDomain.getDomainUrl(), "/upload", challenge);

                    challenge.trigger();

                    if(acmeDomain.getDomainPrivateKey()==null) {
                        StringWriter sw = new StringWriter();
                        KeyPairUtils.writeKeyPair(KeyPairUtils.createKeyPair(4096), sw);
                        acmeDomain.setDomainPrivateKey(sw.toString());
                        domainRepository.save(acmeDomain);
                    }
                    KeyPair domainKeyPair = makeKeyPair(acmeDomain.getDomainPrivateKey());

                    order.execute(domainKeyPair);

                    // Wait for the order to complete
                    Status status = waitForCompletion(order::getStatus, order::fetch);
                    if (status != Status.VALID) {
                        throw new AcmeException("Order failed... Giving up.");
                    }
                    if (EXTERNAL_ACKNOWLEDGE) {
                        clearChallenge(acmeDomain.getDomainUrl(), "/upload", challenge);
                    }
                    Certificate certificate = order.getCertificate();

                    StringWriter writer = new StringWriter();
                    certificate.writeCertificate(writer);

                    AcmeCertificate acmeCertificate = new AcmeCertificate(acmeDomain, writer.toString());
                    acmeCertificate.setCertificateUrl(certificate.getLocation().toString());
                    certificateRepository.save(acmeCertificate);

                    ret.put("url", certificate.getLocation().toString());
                    return new ResponseEntity<>(ret, HttpStatus.OK);
                } catch (AcmeException e) {
                    ret.put("error", "acme exception");
                    ret.put("exception", e.getMessage());
                    return new ResponseEntity<>(ret, HttpStatus.BAD_REQUEST);
                } catch (IOException e) {
                    ret.put("error", "io exception");
                    ret.put("exception", e.getMessage());
                    return new ResponseEntity<>(ret, HttpStatus.BAD_REQUEST);
                }
            }
        } else {
            ret.put("error", "no login");
            return new ResponseEntity<>(ret, HttpStatus.UNAUTHORIZED);
        }
    }

    public ResponseEntity<Map> revoke(HttpSession httpSession, Long id
    ) {
        HashMap<String, String> ret = new HashMap<>();
        AcmeCertificate acmeCertificate= certificateRepository.findCertificateById(id);

        AcmeAccount acmeAccount = accountRepository.findAccountByExternalKey(ACME_ACCOUNT_KEY);
        if(acmeAccount==null) {
            acmeAccount = new AcmeAccount(ACME_ACCOUNT_NAME, ACME_ACCOUNT_KEY);
            acmeAccount.setEmail(ACME_ACCOUNT_EMAIL);
            accountRepository.save(acmeAccount);
        }

        if(loginDefaultAccount(httpSession)){
            Login login = (Login)httpSession.getAttribute("login");
            Account account = login.getAccount();
            if(account.getStatus() != Status.VALID){
                ret.put("error", "invalid status");
                return new ResponseEntity<>(ret, HttpStatus.NOT_FOUND);
            } else {
                try {
                    CertificateFactory fac = null;
                    fac = CertificateFactory.getInstance("X509");
                    X509Certificate cert = (X509Certificate) fac.generateCertificate(new ByteArrayInputStream(acmeCertificate.getContent().getBytes()));

                    Certificate.revoke(login, cert, RevocationReason.KEY_COMPROMISE);

                    acmeCertificate.setRevoked(true);
                    certificateRepository.save(acmeCertificate);
                    ret.put("result", "done");
                    return new ResponseEntity<>(ret, HttpStatus.OK);
                } catch (AcmeException e) {
                    ret.put("error", "acme exception");
                    ret.put("exception", e.getMessage());
                    return new ResponseEntity<>(ret, HttpStatus.BAD_REQUEST);
                } catch (CertificateException e) {
                    ret.put("error", "cert exception");
                    ret.put("exception", e.getMessage());
                    return new ResponseEntity<>(ret, HttpStatus.BAD_REQUEST);
                }
            }
        } else {
            ret.put("error", "no login");
            return new ResponseEntity<>(ret, HttpStatus.UNAUTHORIZED);

        }
    }


    private KeyPair makeKeyPair(MultipartFile userFile) throws IOException {
        return KeyPairUtils.readKeyPair(new BufferedReader(new InputStreamReader(userFile.getInputStream())));
    }
    private KeyPair makeKeyPair(String key) throws IOException {
        return KeyPairUtils.readKeyPair(new StringReader(key));
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
        log.info(challenge.getToken());
        log.info(challenge.getAuthorization());

        if (EXTERNAL_ACKNOWLEDGE) {
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
        } else {
            challengeStore.put(challenge.getToken(), challenge.getAuthorization());
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


    private boolean loginDefaultAccount(HttpSession httpSession) {
        boolean ret = false;
        AcmeAccount acmeAccount = accountRepository.findAccountByExternalKey(ACME_ACCOUNT_KEY);
        if(acmeAccount != null) {
            Session session = new Session(CA_URI);
            try {
                if(acmeAccount.getAccountPrivateKey()==null) {
                    StringWriter sw = new StringWriter();
                    KeyPairUtils.writeKeyPair(KeyPairUtils.createKeyPair(), sw);
                    acmeAccount.setAccountPrivateKey(sw.toString());
                    accountRepository.save(acmeAccount);
                }
                KeyPair userKeyPair = makeKeyPair(acmeAccount.getAccountPrivateKey());
                if(acmeAccount.getAccountUrl()==null) {
                    // register on acme server
                    Account acct = new AccountBuilder()
                            .addContact("mailto:"+acmeAccount.getEmail())
                            .agreeToTermsOfService()
                            .useKeyPair(userKeyPair)
                            .create(session);
                    Login login = session.login(acct.getLocation(), userKeyPair);
                    httpSession.setAttribute("login", login);
                    acmeAccount.setAccountUrl(acct.getLocation().toString());
                    accountRepository.save(acmeAccount);
                    ret = true;

                } else {
                    // login on acme server
                    Login login = new AccountBuilder()
                            .onlyExisting()         // Do not create a new acmeAccount
                            .agreeToTermsOfService()
                            .useKeyPair(userKeyPair)
                            .createLogin(session);
                    httpSession.setAttribute("login", login);
                    ret = true;
                }
            } catch (IOException | AcmeException ignored) {
                throw new RuntimeException(ignored);
            }
        }
        return ret;
    }

}
