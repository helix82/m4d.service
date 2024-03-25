package su.opencode.digsigcik.synapi.dscp;

public enum EnhanceTypeEnum implements HasCode<EnhanceTypeEnum>{

	ADES_BES("AdEsBes", "No enhance"),
	ADES_T("AdEsT", "With timestamp"),
	ADES_C("AdEsC", "Complete"),
	ADES_X("AdEsX", "Long type 1 and long type 2 enhance"),
	ADES_A("AdEsA", "Archive signature");

	static {
		InvariantUtils.fillCodes(EnhanceTypeEnum.class);
	}

	private final String code;
	private final String description;

	EnhanceTypeEnum(String code, String description) {
		this.code = code;
		this.description = description;
	}

	@Override
	public String getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}
}
