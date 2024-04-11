package su.opencode.digsigcik.synapi.dscp;

public enum ContainerTypeEnum implements HasCode<ContainerTypeEnum> {

	SIMPLE_DETACHED("simpleDetached", true),
	PDF_DETACHED("pdfDetached", true),
	DOCX_ATTACHED("docxAttached", false),
	PDF_ATTACHED("pdfAttached", false),
	XML_DSIG("xmlDsig", false),
	POA("poa", true);

	static {
		InvariantUtils.fillCodes(ContainerTypeEnum.class);
	}

	private final String code;
	private final boolean detached;

	ContainerTypeEnum(String code, boolean detached) {
		this.code = code;
		this.detached = detached;
	}

	@Override
	public String getCode() {
		return code;
	}

	public boolean isDetached() {
		return detached;
	}
}
