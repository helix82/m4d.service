package su.opencode.digsigcik.synapi.dscp;

public enum SignatureTypeEnum implements HasCode<SignatureTypeEnum>{

	SIMPLE("simple"),
	UNQUALIFIED("unqualified"),
	QUALIFIED("qualified");

	static {
		InvariantUtils.fillCodes(SignatureTypeEnum.class);
	}

	private final String code;

	SignatureTypeEnum(String code) {
		this.code = code;
	}

	@Override
	public String getCode() {
		return code;
	}
}
