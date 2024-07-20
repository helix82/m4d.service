package su.opencode.digsigcik.synapi.dscp;

public enum ContainerTypeEnum implements HasCode<ContainerTypeEnum> {

	SIMPLE_DETACHED("simpleDetached", true),
	SIMPLE_ATTACHED("simpleAttached", false),
	PDF_DETACHED("pdfDetached", true),
	PDF_ATTACHED("pdfAttached", false),
	DOCX_DETACHED("docxDetached", true),
	DOCX_ATTACHED("docxAttached", false),
	XML_DSIG("xmlDsig", false),
	POA_DETACHED("poa", true),
	POA_ATTACHED("poa", false);

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
