package su.opencode.digsigcik.synapi.dscp;

import org.springframework.http.MediaType;

public enum ResultTypeEnum implements HasCode<ResultTypeEnum> {

	SIGN_CHECK("signCheck", MediaType.APPLICATION_JSON, ContainerFileEnum.LAST_SIGN_CHECK, "_sign_check", "json"),
	CERT_DATA("certData", MediaType.APPLICATION_JSON, ContainerFileEnum.LAST_CERT_DATA, "_cert_data", "json"),
	POA_CHECK("poaCheck", MediaType.APPLICATION_JSON, ContainerFileEnum.LAST_POA_CHECK, "_poa_check", "json"),
	CHECK_HISTORY("checkHistory", MediaType.APPLICATION_JSON, ContainerFileEnum.CHECK_HISTORY, "_check_history", "json"),
	SOURCE_FILE("sourceFile", MetaAttribute.MAIN_FILE_CONTENT_TYPE, ContainerFileEnum.MAIN_FILE, "", MetaAttribute.MAIN_FILE_EXTENSION),
	PREVIEW("preview", MediaType.APPLICATION_PDF, ContainerFileEnum.PREVIEW, "_preview", "pdf"),
	DETACHED_SIGN("detachedSign", MediaType.APPLICATION_OCTET_STREAM),
	ATTACHED_SIGN("attachedSign", MetaAttribute.MAIN_FILE_CONTENT_TYPE),
	HASH_TYPE("hashType", MediaType.TEXT_PLAIN, MetaAttribute.MAIN_FILE_HASH_TYPE),
	HASH("hash", MediaType.TEXT_PLAIN, MetaAttribute.MAIN_FILE_HASH),
	POA_CONTAINER("poaContainer", MediaType.APPLICATION_OCTET_STREAM, ContainerFileEnum.POA_DSCP, "_poa", "dscp"),
	POA_DESCRIPTION("poaDescription", MediaType.APPLICATION_JSON, ContainerFileEnum.POA_DESCRIPTION, "_poa", "json"),
	DESCRIPTION("description", MediaType.TEXT_PLAIN, MetaAttribute.CONTAINER_DESCRIPTION),
	STAMP_SIGN_PAGE("stampSignPage", MediaType.TEXT_PLAIN, MetaAttribute.STAMP_SIGN_PAGE),
	STAMP_SIGN_X("stampSignX", MediaType.TEXT_PLAIN, MetaAttribute.STAMP_SIGN_X),
	STAMP_SIGN_Y("stampSignY", MediaType.TEXT_PLAIN, MetaAttribute.STAMP_SIGN_Y),
	STAMP_SIGN_DIM_W("stampSignDimensionW", MediaType.TEXT_PLAIN, MetaAttribute.STAMP_SIGN_DIMENSION_W),
	STAMP_SIGN_DIM_H("stampSignDimensionH", MediaType.TEXT_PLAIN, MetaAttribute.STAMP_SIGN_DIMENSION_H),
	STAMP_REG_PAGE("stampRegPage", MediaType.TEXT_PLAIN, MetaAttribute.STAMP_REG_PAGE),
	STAMP_REG_X("stampRegX", MediaType.TEXT_PLAIN, MetaAttribute.STAMP_REG_X),
	STAMP_REG_Y("stampRegY", MediaType.TEXT_PLAIN, MetaAttribute.STAMP_REG_Y),
	STAMP_REG_DIMENSION_W("stampSignDimensionW", MediaType.TEXT_PLAIN, MetaAttribute.STAMP_REG_DIMENSION_W),
	STAMP_REG_DIMENSION_H("stampSignDimensionH", MediaType.TEXT_PLAIN, MetaAttribute.STAMP_REG_DIMENSION_H),
	STAMP_SIGN_IMG("stampSignImage", MediaType.IMAGE_PNG, ContainerFileEnum.SIGN_STAMP, "_sign_stamp", "png"),
	STAMP_REG_IMG("stampSignImage", MediaType.IMAGE_PNG, ContainerFileEnum.REG_STAMP, "_reg_stamp", "png");

	static {
		InvariantUtils.fillCodes(ResultTypeEnum.class);
	}

	/**
	 * API code of result got from container
	 */
	private final String code;

	/**
	 * Static content type of the result
	 */
	private final MediaType contentType;

	/**
	 * From wich metadata content type should be got if not static
	 */
	private final MetaAttribute contentTypeMeta;

	/**
	 * If result is provided from meta information of container
	 */
	private final MetaAttribute contentMeta;

	/**
	 * If result is provided like a direct file from a container
	 */
	private final File file;

	ResultTypeEnum(String code, MediaType contentType) {
		this(code, contentType, (MetaAttribute)null, null, null);
	}

	ResultTypeEnum(String code, MediaType contentType, MetaAttribute contentFromMeta) {
		this(code, contentType, null, contentFromMeta, null);
	}

	ResultTypeEnum(String code, MetaAttribute contentTypeMeta) {
		this(code, null, contentTypeMeta, null, null);
	}

	ResultTypeEnum(String code, MetaAttribute contentTypeMeta, ContainerFileEnum fileType, String fileNamePostfix, MetaAttribute extensionMeta) {
		this(code, null, contentTypeMeta, null, new File(fileType, fileNamePostfix, extensionMeta));
	}

	ResultTypeEnum(String code, MediaType contentType, ContainerFileEnum fileType, String fileNamePostfix, String extension) {
		this(code, contentType, null, null, new File(fileType, fileNamePostfix, extension));
	}

	ResultTypeEnum(String code, MediaType contentType, MetaAttribute contentTypeMeta, MetaAttribute contentMeta, File file) {
		this.code = code;
		this.contentType = contentType;
		this.contentTypeMeta = contentTypeMeta;
		this.contentMeta = contentMeta;
		this.file = file;
	}

	public String getCode() {
		return code;
	}

	public boolean isFromMeta() {
		return contentMeta != null;
	}

	public boolean isDirectFile() {
		return file != null;
	}

	public MediaType getContentType(Container container) throws DscpContainerException {
		if (contentType != null) {
			return contentType;
		} else if (contentTypeMeta != null) {
			String contentTypeStr = container.getProperties().getMetaAsString(contentTypeMeta);
			return new MediaType(contentTypeStr);
		} else {
			return null;
		}
	}

	ContainerFileEnum getFileType() {
		return file == null ? null : file.content;
	}

	String getFileName(Container container) throws DscpContainerException {
		if (file == null) return "text.txt"; //TODO
		else {
			ContainerProperties properties = container.getProperties();
			String name = properties.getMetaAsString(file.fileNameMeta);
			String extension = file.extension != null ? file.extension : properties.getMetaAsString(file.extensionMeta);
			return name + file.fileNamePostfix + "." + extension;
		}
	}

	MetaAttribute getContentMeta() {
		return contentMeta;
	}

	String getContentMetaValue(Container container) throws DscpContainerException {
		if (contentMeta == null) return null;
		else return container.getProperties().getMetaAsString(contentMeta);
	}

	private final static class File {

		private final ContainerFileEnum content;
		private final MetaAttribute fileNameMeta = MetaAttribute.MAIN_FILE_NAME;
		private final String fileNamePostfix;
		private final String extension;
		private final MetaAttribute extensionMeta;

		private File(ContainerFileEnum content, String fileNamePostfix, MetaAttribute extensionMeta) {
			this(content, fileNamePostfix, null, extensionMeta);
		}

		private File(ContainerFileEnum content, String fileNamePostfix, String extension) {
			this(content, fileNamePostfix, extension, null);
		}

		private File(ContainerFileEnum content, String fileNamePostfix, String extension, MetaAttribute extensionMeta) {
			this.content = content;
			this.fileNamePostfix = fileNamePostfix;
			this.extension = extension;
			this.extensionMeta = extensionMeta;
		}
	}
}
