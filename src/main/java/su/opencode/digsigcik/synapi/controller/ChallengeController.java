package su.opencode.digsigcik.synapi.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.acme.ChallengeStore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@RestController
public class ChallengeController {

    private static final Logger logger = LoggerFactory.getLogger(ChallengeController.class);

    private final ChallengeStore challengeStore;

    public ChallengeController (ChallengeStore aChallengeStore) {
        challengeStore = aChallengeStore;
    }

    @GetMapping("/.well-known/acme-challenge/{token}")
    public String challenge (@PathVariable("token") String aToken) {
        logger.info("Received challenge for {}", aToken);
        return challengeStore.get(aToken);
    }

    @PostMapping("/.well-known/acme-challenge/")
    public void uploadChallenge (@RequestPart("tokenFile") MultipartFile tokenFile) {
        logger.info("Upload challenge for {}", tokenFile.getOriginalFilename());
        StringBuilder content = new StringBuilder();
        try (InputStream inputStream = tokenFile.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        challengeStore.put(tokenFile.getOriginalFilename(),content.toString());
    }

}