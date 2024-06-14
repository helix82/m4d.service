package su.opencode.digsigcik.synapi.acme;


public interface ChallengeStore {

    String get (String aToken);

    void put (String aToken, String aAuthorization);

}