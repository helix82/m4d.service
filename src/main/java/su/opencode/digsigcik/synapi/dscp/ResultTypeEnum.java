package su.opencode.digsigcik.synapi.dscp;

public enum ResultTypeEnum implements HasCode<ResultTypeEnum>{

	SIGN_CHECK("signCheck", "text/plain"),
	CERT_DATA("certData", "text/plain"),
	POA_CHECK("poaCheck", "text/plain"),
	CHECK_HISTORY("certData", "text/plain", ContainerFileEnum.CHECK_HISTORY, "_check_history", "txt"),
	SOURCE_FILE("sourceFile", MetaAttribute.MAIN_FILE_CONTENT_TYPE, ContainerFileEnum.MAIN_FILE, "", MetaAttribute.MAIN_FILE_EXTENSION),
	PREVIEW("preview", "application/pdf", ContainerFileEnum.PREVIEW, "_preview", "pdf"),
	DETACHED_SIGN("detachedSign", "application/octet-stream"),
	ATTACHED_SIGN("attachedSign", MetaAttribute.MAIN_FILE_CONTENT_TYPE),
	HASH_TYPE("hashType", "text/plain", MetaAttribute.MAIN_FILE_HASH_TYPE),
	HASH("hash", "text/plain", MetaAttribute.MAIN_FILE_HASH),
	POA_CONTAINER("poaContainer", "application/octet-stream", ContainerFileEnum.POA_DSCP, "_poa", "dscp"),
	POA_DESCRIPTION("poaDescription", "application/json", ContainerFileEnum.POA_DESCRIPTION, "_poa", "json"),
	MEDO("medo", "application/zip"),
	DESCRIPTION("description", "text/plain", MetaAttribute.CONTAINER_DESCRIPTION);

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
	private final String contentType;

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

	ResultTypeEnum(String code, String contentType) {
		this(code, contentType, (MetaAttribute)null, null, null);
	}

	ResultTypeEnum(String code, String contentType, MetaAttribute contentFromMeta) {
		this(code, contentType, null, contentFromMeta, null);
	}

	ResultTypeEnum(String code, MetaAttribute contentTypeMeta) {
		this(code, null, contentTypeMeta, null, null);
	}

	ResultTypeEnum(String code, MetaAttribute contentTypeMeta, ContainerFileEnum fileType, String fileNamePostfix, MetaAttribute extensionMeta) {
		this(code, null, contentTypeMeta, null, new File(fileType, fileNamePostfix, extensionMeta));
	}

	ResultTypeEnum(String code, String contentType, ContainerFileEnum fileType, String fileNamePostfix, String extension) {
		this(code, contentType, null, null, new File(fileType, fileNamePostfix, extension));
	}

	ResultTypeEnum(String code, String contentType, MetaAttribute contentTypeMeta, MetaAttribute contentMeta, File file) {
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

	String getContentType(Container container) throws DscpContainerException {
		if (contentType != null) return contentType;
		else if (contentTypeMeta != null) return container.getProperties().getMetaAsString(contentTypeMeta);
		else return null;
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

	String getMeta(Container container) throws DscpContainerException {
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
