package su.opencode.digsigcik.synapi.dscp;

import java.util.Arrays;

public enum ContainerFileEnum {

	META("meta.json", true, false, null),
	PREVIEW_PDF("preview/preview.pdf", false, true, null),
	PREVIEW_PNG("preview/preview.png", false, true, null),
	MAIN_FILE("main/mainFile", true, true, null),
	DETACHED_BES("detached/sha256", false, true, EnhanceTypeEnum.ADES_BES),
	DETACHED_T("detached/pkcs7t", false, true, EnhanceTypeEnum.ADES_T),
	DETACHED_C("detached/pkcs7c", false, true, EnhanceTypeEnum.ADES_C),
	DETACHED_A("detached/pkcs7a", false, true, EnhanceTypeEnum.ADES_A),
	ATTACHED_BES("attached/file", false, true, EnhanceTypeEnum.ADES_BES),
	ATTACHED_T("attached/filet", false, true, EnhanceTypeEnum.ADES_T),
	ATTACHED_C("attached/filec", false, true, EnhanceTypeEnum.ADES_C),
	ATTACHED_A("attached/filea", false, true, EnhanceTypeEnum.ADES_A),
	REG_STAMP("stamp/regNum_stamp.png", false, true, null),
	SIGN_STAMP("stamp/sign_stamp.png", false, true, null),
	POA_DSCP("poa/poa.dscp", false, true, null),
	POA_DESCRIPTION("poa/poa.json", false, true, null),
	LAST_SIGN_CHECK("check/lastSignCheck.json", false, true, null),
	LAST_SIGN_REPORT("check/lastSignReport.pdf", false, true, null),
	LAST_CERT_DATA("check/lastCertData.json", false, true, null),
	LAST_POA_CHECK("check/lastPoaCheck.json", false, true, null),
	CHECK_HISTORY("check/checkHistory.json", false, true, null);

	public static ContainerFileEnum byEnhanceType(EnhanceTypeEnum enhanceType) {
		return Arrays.stream(values())
			.filter(item -> item.enhanceType == enhanceType)
			.findAny()
			.orElseThrow();
	}

	private final String path;
	private final boolean mandatory;
	private final boolean calculateDigest;
	private final EnhanceTypeEnum enhanceType;

	ContainerFileEnum(String path, boolean mandatory, boolean calculateDigest, EnhanceTypeEnum enhanceType) {
		this.path = path;
		this.mandatory = mandatory;
		this.calculateDigest = calculateDigest;
		this.enhanceType = enhanceType;
	}

	public String getPath() {
		return path;
	}

	public boolean isMandatory() {
		return mandatory;
	}

	public boolean isCalculateDigest() {
		return calculateDigest;
	}
}
