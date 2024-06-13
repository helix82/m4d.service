package su.opencode.digsigcik.synapi.acme;

import java.util.Map;

public class InMemoryChallengeStore implements ChallengeStore {

    private static final long FIFTEEN_MINS = 15 * 60 * 1000;

    private Map<String, String> challenges = new SelfExpiringHashMap<>(FIFTEEN_MINS);

    public String get (String aToken) {
        return challenges.get(aToken);
    }

    public void put (String aToken, String aAuthorization) {
        challenges.put(aToken, aAuthorization);
    }

}