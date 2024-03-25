package su.opencode.digsigcik.synapi.crypto;

import su.opencode.digsigcik.synapi.dscp.AttachedSignatureTypeEnum;

public interface AttachedSignCheckProvider extends SecurityProvider {
	CheckResult checkAttachedSignature(byte[] originalFileContent, AttachedSignatureTypeEnum attachedSignatureType);
}
