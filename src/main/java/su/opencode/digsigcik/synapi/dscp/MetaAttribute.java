package su.opencode.digsigcik.synapi.dscp;

final class MetaAttribute {

	private static final String PROPERTY_DELIMITER = ".";

	//Main file meta attributes
	static final MetaAttribute MAIN_FILE_NAME = new MetaAttribute(MetaObjectEnum.MAIN_FILE, "name");
	static final MetaAttribute MAIN_FILE_EXTENSION = new MetaAttribute(MetaObjectEnum.MAIN_FILE, "extension");
	static final MetaAttribute MAIN_FILE_HASH_TYPE = new MetaAttribute(MetaObjectEnum.MAIN_FILE, "hashType");
	static final MetaAttribute MAIN_FILE_HASH = new MetaAttribute(MetaObjectEnum.MAIN_FILE, "hash");
	static final MetaAttribute MAIN_FILE_CONTENT_TYPE = new MetaAttribute(MetaObjectEnum.MAIN_FILE, "mimeType");
	static final MetaAttribute MAIN_FILE_FULL_NAME = new MetaAttribute(MetaObjectEnum.MAIN_FILE, "fullName");
	static final MetaAttribute MAIN_FILE_SIZE = new MetaAttribute(MetaObjectEnum.MAIN_FILE, "size");
	//Container description meta attributes
	static final MetaAttribute CONTAINER_HASH_TYPE = new MetaAttribute(MetaObjectEnum.CONTAINER_DESCRIPTION, "hashType");
	static final MetaAttribute CONTAINER_HASH = new MetaAttribute(MetaObjectEnum.CONTAINER_DESCRIPTION, "hash");
	static final MetaAttribute CONTAINER_SIZE = new MetaAttribute(MetaObjectEnum.CONTAINER_DESCRIPTION, "size");
	static final MetaAttribute CONTAINER_DESCRIPTION = new MetaAttribute(MetaObjectEnum.CONTAINER_DESCRIPTION, "description");
	//Container type meta attributes
	static final MetaAttribute IS_DETACHED = new MetaAttribute(MetaObjectEnum.CONTAINER_TYPE, "isDetached");
	static final MetaAttribute HAS_PREVIEW = new MetaAttribute(MetaObjectEnum.CONTAINER_TYPE, "hasPreview");
	static final MetaAttribute HAS_POA = new MetaAttribute(MetaObjectEnum.CONTAINER_TYPE, "hasPoa");
	static final MetaAttribute CONTAINER_TYPE = new MetaAttribute(MetaObjectEnum.CONTAINER_TYPE, "code");
	//Signature type
	static final MetaAttribute SIGNATURE_TYPE = new MetaAttribute(MetaObjectEnum.SIGNATURE_TYPE, "code");
	static final MetaAttribute ENHANCE_TYPE = new MetaAttribute(MetaObjectEnum.SIGNATURE_TYPE, "enhance");
	static final MetaAttribute ATTACHED_TYPE = new MetaAttribute(MetaObjectEnum.SIGNATURE_TYPE, "attachedType");
	static final MetaAttribute SIGNATURE_TIMESTAMP = new MetaAttribute(MetaObjectEnum.SIGNATURE_TYPE, "timestamp");
	static final MetaAttribute SIGNATURE_ALGORITHM = new MetaAttribute(MetaObjectEnum.SIGNATURE_TYPE, "signatureAlgorithm");
	//Stamp
	static final MetaAttribute STAMP_SIGN_PAGE = new MetaAttribute(MetaObjectEnum.STAMP, "signPage");
	static final MetaAttribute STAMP_SIGN_X = new MetaAttribute(MetaObjectEnum.STAMP, "signX");
	static final MetaAttribute STAMP_SIGN_Y = new MetaAttribute(MetaObjectEnum.STAMP, "signY");
	static final MetaAttribute STAMP_SIGN_DIMENSION_W = new MetaAttribute(MetaObjectEnum.STAMP, "signDimensionW");
	static final MetaAttribute STAMP_SIGN_DIMENSION_H = new MetaAttribute(MetaObjectEnum.STAMP, "signDimensionH");
	static final MetaAttribute STAMP_REG_PAGE = new MetaAttribute(MetaObjectEnum.STAMP, "regPage");
	static final MetaAttribute STAMP_REG_X = new MetaAttribute(MetaObjectEnum.STAMP, "regX");
	static final MetaAttribute STAMP_REG_Y = new MetaAttribute(MetaObjectEnum.STAMP, "regY");
	static final MetaAttribute STAMP_REG_DIMENSION_W = new MetaAttribute(MetaObjectEnum.STAMP, "regDimensionW");
	static final MetaAttribute STAMP_REG_DIMENSION_H = new MetaAttribute(MetaObjectEnum.STAMP, "regDimensionH");
	// Signature data
	static final MetaAttribute USER_LOGIN = new MetaAttribute(MetaObjectEnum.SIGNATURE_DATA, "userLogin");
	static final MetaAttribute USER_FIRSTNAME = new MetaAttribute(MetaObjectEnum.SIGNATURE_DATA, "userFirstname");
	static final MetaAttribute USER_LASTNAME = new MetaAttribute(MetaObjectEnum.SIGNATURE_DATA, "userLastname");
	static final MetaAttribute USER_PATRONYMIC = new MetaAttribute(MetaObjectEnum.SIGNATURE_DATA, "userPatronymic");




	private final MetaObjectEnum object;
	private final String attribute;

	MetaAttribute(MetaObjectEnum object, String attribute) {
		this.object = object;
		this.attribute = attribute;
	}

	public MetaObjectEnum getObject() {
		return object;
	}

	public String getAttribute() {
		return attribute;
	}

	public String getPropertyKey() {
		return object.getCode() + PROPERTY_DELIMITER + attribute;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof MetaAttribute)) return false;

		MetaAttribute that = (MetaAttribute) o;

		if (object != that.object) return false;
		return attribute.equals(that.attribute);
	}

	@Override
	public int hashCode() {
		int result = object.hashCode();
		result = 31 * result + attribute.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return getPropertyKey();
	}
}
