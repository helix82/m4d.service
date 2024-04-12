package su.opencode.digsigcik.synapi.dscp;

import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;

public class ResultFileDescription {
	private final ResultTypeEnum resultType;
	private final String fileName;
	private final byte[] content;
	private final MediaType contentType;

	public ResultFileDescription(ResultTypeEnum resultType, String fileName, MediaType contentType, byte[] content) {
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

	public MediaType getContentType() {
		return contentType;
	}

	public String getMetaValue() {
		return resultType.getFileType() != null ? null : new String(content, StandardCharsets.UTF_8);
	}
}
