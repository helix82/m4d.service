package su.opencode.digsigcik.synapi.dscp;

enum MetaObjectEnum implements HasCode<MetaObjectEnum> {

	CONTAINER_TYPE("containerType"),
	SIGNATURE_TYPE("signatureType"),
	MAIN_FILE("mainFile"),
	CONTAINER_DESCRIPTION("containerDescription"),
	STAMP("stamp");

	static {
		InvariantUtils.fillCodes(MetaObjectEnum.class);
	}

	private final String code;

	MetaObjectEnum(String code) {
		this.code = code;
	}

	@Override
	public String getCode() {
		return code;
	}
}
