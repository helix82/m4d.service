package su.opencode.digsigcik.synapi.crypto;

import su.opencode.digsigcik.synapi.dscp.Container;

public interface PoaCheckProvider {
	CheckResult checkPoa(byte[] certData, Container poa, SecurityProvider provider);
}
