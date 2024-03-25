package su.opencode.digsigcik.synapi.dscp;

public class ResultFileDescription {
	private final ResultTypeEnum resultType;
	private final String fileName;
	private final byte[] content;
	private final String contentType;

	public ResultFileDescription(ResultTypeEnum resultType, String fileName, String contentType, byte[] content) {
		this.resultType = resultType;
		this.fileName = fileName;
		this.content = content;
		this.contentType = contentType;
	}

	public ResultTypeEnum getResultType() {
		return resultType;
	}

	public String getFileName() {
		return fileName;
	}

	public byte[] getContent() {
		return content;
	}

	public String getResultCode() {
		return resultType.getCode();
	}

	public String getContentType() {
		return contentType;
	}
}
