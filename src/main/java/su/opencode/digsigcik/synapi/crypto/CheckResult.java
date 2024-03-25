package su.opencode.digsigcik.synapi.crypto;

import java.nio.charset.StandardCharsets;

public final class CheckResult {

	private final boolean successful;
	private final byte[] checkContent;

	public CheckResult(boolean successful, byte[] checkContent) {
		this.successful = successful;
		this.checkContent = checkContent;
	}

	public boolean isSuccessful() {
		return successful;
	}

	public byte[] getCheckContent() {
		return checkContent;
	}

	public String getCheckResult() {
		return checkContent == null || checkContent.length <= 0 ? null : new String(checkContent, StandardCharsets.UTF_8);
	}
}
