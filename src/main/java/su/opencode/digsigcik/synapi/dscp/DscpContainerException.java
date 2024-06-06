package su.opencode.digsigcik.synapi.dscp;

import org.json.JSONException;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class DscpContainerException extends RuntimeException {

	private final Type type;
	private final ContainerFileEnum problemWithFile;
	private final ResultTypeEnum resultType;
	private final String problemWithMetaKey;
	private final String problemWithFileName;

	public static DscpContainerException ioCause(IOException cause, boolean reading) {
		String action = reading ? "reading" : "writing";
		return new DscpContainerException("IOException while " + action + " container", cause, Type.IO, null, null, null, null);
	}

	static DscpContainerException fillContentProblem(String message, ContainerFileEnum fileType, byte[] content) {
		message = String.format("Problems with fill %s for fileType %s. %s",
			content == null || content.length <= 0 ? " null content" : " content of " + content.length + " length",
			fileType, message);
		return fillContentProblem(message, fileType);
	}

	static DscpContainerException fillContentProblem(String message, ContainerFileEnum fileType) {
		String fileName = fileType == null ? null : fileType.getPath();
		return new DscpContainerException(message, null, Type.INPUT, fileType, fileName, null, null);
	}

	static DscpContainerException containerFileProblem(IOException cause, ContainerFileEnum fileType, String fileName) {
		return new DscpContainerException("IOException while reading container with file " + fileType + " path='" + fileName + "'", cause, Type.IO, fileType, fileName, null, null);
	}

	static DscpContainerException internalFormatFileProblem(String message, JSONException cause, ContainerFileEnum fileType) {
		return new DscpContainerException(message, cause, Type.FORMAT, fileType, fileType.getPath(), null, null);
	}

	static DscpContainerException internalFormatFileProblem(String message, ContainerFileEnum fileType) {
		return internalFormatFileProblem(message, null, fileType);
	}

	static DscpContainerException metaKeyProblem(String message, String metaKey) {
		return new DscpContainerException(message, null, Type.FORMAT, ContainerFileEnum.META, ContainerFileEnum.META.getPath(), metaKey, null);
	}

	static DscpContainerException contentSecurityException(String message) {
		return contentSecurityException(message, null);
	}

	static DscpContainerException contentSecurityException(String message, GeneralSecurityException cause) {
		return new DscpContainerException(message, cause, Type.SECURITY, null, null, null, null);
	}

	static DscpContainerException resultCauseException(ResultTypeEnum resultType, Exception cause) {
		return resultCauseException("Error while extracting result of resultType '" + resultType + "'", resultType, cause);
	}

	static DscpContainerException resultCauseException(String message, ResultTypeEnum resultType, Exception cause) {
		return new DscpContainerException(message, cause, Type.RESULT, null, null, null, resultType);
	}

	private DscpContainerException(String message, Throwable cause, Type type,
	                               ContainerFileEnum problemWithFile, String problemWithFileName,
	                               String problemWithMetaKey, ResultTypeEnum resultType) {
		super(message, cause);
		this.type = type;
		this.problemWithFile = problemWithFile;
		this.problemWithFileName = problemWithFileName;
		this.problemWithMetaKey = problemWithMetaKey;
		this.resultType = resultType;
	}

	public Type getType() {
		return type;
	}

	public ResultTypeEnum getResultType() {
		return resultType;
	}

	public String getProblemWithFileName() {
		return problemWithFileName;
	}

	public ContainerFileEnum getProblemWithFile() {
		return problemWithFile;
	}

	public String getProblemWithMetaKey() {
		return problemWithMetaKey;
	}

	public IOException getIOCause() {
		return type == Type.IO ? (IOException)getCause() : null;
	}

	public GeneralSecurityException getSecurityCause() {
		return type == Type.SECURITY ? (GeneralSecurityException)getCause() : null;
	}

	public JSONException getFormatException() {
		return type == Type.FORMAT ? (JSONException)getCause() : null;
	}

	public enum Type {
		IO, FORMAT, SECURITY, RESULT, INPUT
	}
}
