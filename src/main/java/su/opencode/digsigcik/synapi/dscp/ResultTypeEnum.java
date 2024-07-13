package su.opencode.digsigcik.synapi.dscp;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;

public enum ResultTypeEnum implements HasCode<ResultTypeEnum> {

	SIGN_CHECK("signCheck", MediaType.APPLICATION_JSON, ContainerFileEnum.LAST_SIGN_CHECK, "_sign_check", "json"),
	CERT_DATA("certData", MediaType.APPLICATION_JSON, ContainerFileEnum.LAST_CERT_DATA, "_cert_data", "json"),
	POA_CHECK("poaCheck", MediaType.APPLICATION_JSON, ContainerFileEnum.LAST_POA_CHECK, "_poa_check", "json"),
	CHECK_HISTORY("checkHistory", MediaType.APPLICATION_JSON, ContainerFileEnum.CHECK_HISTORY, "_check_history", "json"),
	SOURCE_FILE("sourceFile", MediaType.APPLICATION_OCTET_STREAM, ContainerFileEnum.MAIN_FILE, null, null),
	SOURCE_FILE_NAME ("sourceFileName", MetaAttribute.MAIN_FILE_NAME),
	SOURCE_FILE_EXTENSION ("sourceFileExtension", MetaAttribute.MAIN_FILE_EXTENSION),
	SOURCE_FILE_HASH_TYPE ("sourceFileHashType", MetaAttribute.MAIN_FILE_HASH_TYPE),
	SOURCE_FILE_HASH ("sourceFileHash", MetaAttribute.MAIN_FILE_HASH),
	SOURCE_FILE_CONTENT_TYPE  ("sourceFileContentType",MetaAttribute.MAIN_FILE_CONTENT_TYPE),
	SOURCE_FILE_FULL_NAME  ("sourceFileFullName",MetaAttribute.MAIN_FILE_FULL_NAME ),
	SOURCE_FILE_SIZE  ("sourceFileSize",MetaAttribute.MAIN_FILE_SIZE),
	PREVIEW_PDF("previewPdf", MediaType.APPLICATION_PDF, ContainerFileEnum.PREVIEW_PDF, "_preview", "pdf"),
	PREVIEW_PNG("previewPng", MediaType.IMAGE_PNG, ContainerFileEnum.PREVIEW_PNG, "_preview", "png"),
	DETACHED_SIGN("detachedSign", MediaType.APPLICATION_OCTET_STREAM, ContainerFileEnum.DETACHED_BES, null, "sig"),
	ATTACHED_SIGN("attachedSign", MediaType.APPLICATION_OCTET_STREAM, ContainerFileEnum.ATTACHED_BES, null, null),
	HASH_TYPE("hashType", MetaAttribute.MAIN_FILE_HASH_TYPE),
	HASH("hash", MetaAttribute.MAIN_FILE_HASH),
	CONTENT_TYPE("contentType", MetaAttribute.MAIN_FILE_CONTENT_TYPE),
	POA_CONTAINER("poaContainer", MediaType.APPLICATION_OCTET_STREAM, ContainerFileEnum.POA_DSCP, "_poa", "dscp"),
	POA_DESCRIPTION("poaDescription", MediaType.APPLICATION_JSON, ContainerFileEnum.POA_DESCRIPTION, "_poa", "json"),
	DESCRIPTION("description", MetaAttribute.CONTAINER_DESCRIPTION),
	STAMP_SIGN_PAGE("stampSignPage", MetaAttribute.STAMP_SIGN_PAGE),
	STAMP_SIGN_X("stampSignX", MetaAttribute.STAMP_SIGN_X),
	STAMP_SIGN_Y("stampSignY", MetaAttribute.STAMP_SIGN_Y),
	STAMP_SIGN_DIM_W("stampSignDimensionW", MetaAttribute.STAMP_SIGN_DIMENSION_W),
	STAMP_SIGN_DIM_H("stampSignDimensionH", MetaAttribute.STAMP_SIGN_DIMENSION_H),
	STAMP_REG_PAGE("stampRegPage", MetaAttribute.STAMP_REG_PAGE),
	STAMP_REG_X("stampRegX", MetaAttribute.STAMP_REG_X),
	STAMP_REG_Y("stampRegY", MetaAttribute.STAMP_REG_Y),
	STAMP_REG_DIMENSION_W("stampSignDimensionW", MetaAttribute.STAMP_REG_DIMENSION_W),
	STAMP_REG_DIMENSION_H("stampSignDimensionH", MetaAttribute.STAMP_REG_DIMENSION_H),
	STAMP_SIGN_IMG("stampSignImage", MediaType.IMAGE_PNG, ContainerFileEnum.SIGN_STAMP, "_sign_stamp", "png"),
	STAMP_REG_IMG("stampSignImage", MediaType.IMAGE_PNG, ContainerFileEnum.REG_STAMP, "_reg_stamp", "png"),
	FIRSTNAME("userFirstname", MetaAttribute.USER_FIRSTNAME),
	LASTNAME("userLastname", MetaAttribute.USER_LASTNAME),
	PATRONYMIC("userPatronymic", MetaAttribute.USER_PATRONYMIC),
	TIMESTAMP("timestamp", MetaAttribute.SIGNATURE_TIMESTAMP),
	LOGIN("userLogin", MetaAttribute.USER_LOGIN),
	SIGNATURE_ALGORITHM("signatureAlgorithm", MetaAttribute.SIGNATURE_ALGORITHM),
	SIGNATURE_TYPE("signatureType", MetaAttribute.SIGNATURE_TYPE),
	CONTAINER_TYPE("containerType", MetaAttribute.CONTAINER_TYPE);

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
	 * If result is provided from meta information of container
	 */
	private final MetaAttribute contentMeta;

	/**
	 * If result is provided like a direct file from a container
	 */
	private final ContainerFileEnum fileType;

	private final String fileName;

	private final String fileNamePostfix;

	private final String fileNameExtension;

	ResultTypeEnum(String code, MetaAttribute contentFromMeta) {
		this(code, MediaType.TEXT_PLAIN, contentFromMeta, null, code, null, "txt");
	}

	ResultTypeEnum(String code, MediaType contentType, ContainerFileEnum fileType, String fileNamePostfix, String extension) {
		this(code, contentType, null, fileType, null, fileNamePostfix, extension);
	}

	ResultTypeEnum(String code, MediaType contentType, MetaAttribute contentMeta, ContainerFileEnum fileType,
	               String fileName, String fileNamePostfix, String fileNameExtension) {
		this.code = code;
		this.contentType = contentType;
		this.contentMeta = contentMeta;
		this.fileType = fileType;
		this.fileName = StringUtils.isBlank(fileName) ? null : fileName;
		this.fileNamePostfix = StringUtils.isBlank(fileNamePostfix) ? "" : fileNamePostfix;
		this.fileNameExtension = StringUtils.isBlank(fileNameExtension) ? null : fileNameExtension;
	}

	public String getCode() {
		return code;
	}

	public boolean isFromMeta() {
		return contentMeta != null;
	}

	public boolean isDirectFile() {
		return fileType != null;
	}

	ContainerFileEnum getFileType() {
		return fileType;
	}

	MetaAttribute getContentMeta() {
		return contentMeta;
	}

	MediaType getContentType() {
		return contentType;
	}

	String getFileName() {
		return fileName;
	}

	String getFileNamePostfix() {
		return fileNamePostfix;
	}

	String getFileNameExtension() {
		return fileNameExtension;
	}
}
