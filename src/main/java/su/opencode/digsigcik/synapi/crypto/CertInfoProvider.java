package su.opencode.digsigcik.synapi.crypto;

import su.opencode.digsigcik.synapi.dscp.AttachedSignatureTypeEnum;

public interface CertInfoProvider extends SecurityProvider {
	byte[] getCertInfo(byte[] contentWithCertData, AttachedSignatureTypeEnum attachedSignatureType);
}
