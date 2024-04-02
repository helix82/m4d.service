package su.opencode.digsigcik.synapi.dscp;

import java.util.Arrays;

enum ContainerFileEnum {

	META("meta.json", true, false, null, null),
	PREVIEW("preview/preview.pdf", false, true, null, null),
	MAIN_FILE("main/mainFile", true, true, null, null),
	DETACHED_PKCS7("detached/pkcs7", true, true, null, EnhanceTypeEnum.ADES_BES),
	DETACHED_PKCS7_T("detached/pkcs7t", false, true, null, EnhanceTypeEnum.ADES_T),
	DETACHED_PKCS7_C("detached/pkcs7c", false, true, null, EnhanceTypeEnum.ADES_C),
	DETACHED_PKCS7_X("detached/pkcs7x", false, true, null, EnhanceTypeEnum.ADES_X),
	DETACHED_PKCS7_A("detached/pkcs7a", false, true, null, EnhanceTypeEnum.ADES_A),
	ATTACHED_WORD("attached/wordFile.docx", false, true, AttachedSignatureTypeEnum.DOCX, null),
	ATTACHED_PDF("attached/pdfFile.pdf", false, true, AttachedSignatureTypeEnum.PDF, null),
	ATTACHED_XML("attached/xmlFile.xml", false, true, AttachedSignatureTypeEnum.XML, null),
	MEDO_COORDINATE("medo/coordinate.json", false, true, null, null),
	MEDO_REG_NUM_STAMP("medo/regNum_stamp.png", false, true, null, null),
	MEDO_SIGN_STAMP("medo/sign_stamp.png", false, true, null, null),
	POA_DSCP("poa/poa.dscp", false, true, null, null),
	POA_DESCRIPTION("poa/poa.json", false, true, null, null),
	LAST_SIGN_CHECK("check/lastSignCheck.json", false, true, null, null),
	LAST_CERT_DATA("check/lastCertData.json", false, true, null, null),
	LAST_POA_CHECK("check/lastPoaCheck.json", false, true, null, null),
	CHECK_HISTORY("check/checkHistory.json", false, true, null, null);

	public static ContainerFileEnum byAttachedSignatureType(AttachedSignatureTypeEnum attachedSignatureType) {
		return Arrays.stream(values())
			.filter(item->item.attachedSignatureType == attachedSignatureType)
			.findAny()
			.orElseThrow();
	}

	public static ContainerFileEnum byEnhanceType(EnhanceTypeEnum enhanceType) {
		return Arrays.stream(values())
			.filter(item->item.enhanceType == enhanceType)
			.findAny()
			.orElseThrow();
	}

	private final String path;
	private final boolean mandatory;
	private final boolean calculateDigest;
	private final AttachedSignatureTypeEnum attachedSignatureType;
	private final EnhanceTypeEnum enhanceType;

	ContainerFileEnum(String path, boolean mandatory, boolean calculateDigest,
	                  AttachedSignatureTypeEnum attachedSignatureType,
	                  EnhanceTypeEnum enhanceType) {
		this.path = path;
		this.mandatory = mandatory;
		this.calculateDigest = calculateDigest;
		this.attachedSignatureType = attachedSignatureType;
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
