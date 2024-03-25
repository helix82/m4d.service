package su.opencode.digsigcik.synapi.dscp;

public enum AttachedSignatureTypeEnum implements HasCode<AttachedSignatureTypeEnum> {
	DOCX("docx"), PDF("pdf"), XML("xml");

	static {
		InvariantUtils.fillCodes(AttachedSignatureTypeEnum.class);
	}

	private final String code;

	AttachedSignatureTypeEnum(String code) {
		this.code = code;
	}

	@Override
	public String getCode() {
		return code;
	}
}
