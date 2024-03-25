package su.opencode.digsigcik.synapi.crypto;

public interface DetachedSignCheckProvider extends SecurityProvider {
	CheckResult checkDetachedSignature(byte[] originalFileContent, byte[] detachedSignatureContent);
}
