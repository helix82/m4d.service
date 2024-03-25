package su.opencode.digsigcik.synapi.dscp;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class ContainerProperties {
	private final JSONObject metaContentJson;

	private final String sourceFileName;
	private final String sourceFileContentType;
	private final String containerDescription;
	private final boolean detached;
	private final boolean hasPreview;
	private final boolean hasPoa;
	private final ContainerTypeEnum containerType;
	private final SignatureTypeEnum signatureType;
	private final EnhanceTypeEnum enhanceType;
	private final AttachedSignatureTypeEnum attachedSignatureType;

	ContainerProperties(byte[] metaContentByte) throws DscpContainerException {

		try {
			String metaContentStr = new String(metaContentByte, StandardCharsets.UTF_8);
			metaContentJson = new JSONObject(metaContentStr);
			this.sourceFileName = doGetMetaAsString(metaContentJson, MetaAttribute.MAIN_FILE_FULL_NAME, false);
			this.sourceFileContentType = doGetMetaAsString(metaContentJson, MetaAttribute.MAIN_FILE_CONTENT_TYPE, false);
			this.containerDescription = doGetMetaAsString(metaContentJson, MetaAttribute.CONTAINER_DESCRIPTION, true);
			this.detached = doGetMetaAsBoolean(metaContentJson, MetaAttribute.IS_DETACHED);
			this.hasPoa = doGetMetaAsBoolean(metaContentJson, MetaAttribute.HAS_POA);
			this.hasPreview = doGetMetaAsBoolean(metaContentJson, MetaAttribute.HAS_PREVIEW);
			this.containerType = doGetMetaAsEnum(metaContentJson, MetaAttribute.CONTAINER_TYPE, ContainerTypeEnum.class);
			this.signatureType = doGetMetaAsEnum(metaContentJson, MetaAttribute.SIGNATURE_TYPE, SignatureTypeEnum.class);
			this.enhanceType = doGetMetaAsEnum(metaContentJson, MetaAttribute.ENHANCE_TYPE, EnhanceTypeEnum.class);
			this.attachedSignatureType = doGetMetaAsEnum(metaContentJson, MetaAttribute.ATTACHED_TYPE, AttachedSignatureTypeEnum.class, true);
		} catch (JSONException e) {
			//Not a valid JSON format of meta.json
			throw DscpContainerException.internalFormatFileProblem("Not valid metadata json format", e, ContainerFileEnum.META);
		}
	}

	//==================================public==========================================================================

	public String getSourceFileName() {
		return sourceFileName;
	}

	public String getSourceFileContentType() {
		return sourceFileContentType;
	}

	public String getContainerDescription() {
		return containerDescription;
	}

	public boolean isDetached() {
		return detached;
	}

	public boolean hasPreview() {
		return hasPreview;
	}

	public boolean hasPoa() {
		return hasPoa;
	}

	public ContainerTypeEnum getContainerType() {
		return containerType;
	}

	public SignatureTypeEnum getSignatureType() {
		return signatureType;
	}

	public EnhanceTypeEnum getEnhanceType() {
		return enhanceType;
	}

	public AttachedSignatureTypeEnum getAttachedSignatureType() {
		return attachedSignatureType;
	}

	//==================================package=========================================================================

	String getMetaAsString(MetaAttribute attribute, boolean tolerant) throws DscpContainerException {
		return doGetMetaAsString(metaContentJson, attribute, tolerant);
	}

	String getMetaAsString(MetaAttribute attribute) throws DscpContainerException {
		return getMetaAsString(attribute, false);
	}

	long getMetaAsLong(MetaAttribute attribute) throws DscpContainerException {
		return doGetMetaAsLong(metaContentJson, attribute);
	}

	//=============================private==============================================================================

	private static long doGetMetaAsLong(JSONObject json, MetaAttribute attribute) throws DscpContainerException {
		try {
			JSONObject obj = json.getJSONObject(attribute.getObject().getCode());
			return obj.getLong(attribute.getAttribute());
		} catch (JSONException e) {
			throw DscpContainerException.metaKeyProblem("Cannot find meta key '" + attribute.getPropertyKey() + "' value", attribute.getPropertyKey());
		}
	}

	private static String doGetMetaAsString(JSONObject json, MetaAttribute attribute, boolean tolerant) throws DscpContainerException {
		JSONObject obj = json.getJSONObject(attribute.getObject().getCode());
		Object res = obj.has(attribute.getAttribute()) ? obj.get(attribute.getAttribute()) : null;
		if (res != null) return String.valueOf(res);
		if (tolerant) return null;
		else throw DscpContainerException.metaKeyProblem("Cannot find meta key '" + attribute.getPropertyKey() + "' value", attribute.getPropertyKey());
	}

	private static boolean doGetMetaAsBoolean(JSONObject json, MetaAttribute attribute) throws DscpContainerException {
		try {
			JSONObject obj = json.getJSONObject(attribute.getObject().getCode());
			return obj.getBoolean(attribute.getAttribute());
		} catch (JSONException e) {
			throw DscpContainerException.metaKeyProblem("Cannot find meta key '" + attribute.getPropertyKey() + "' value", attribute.getPropertyKey());
		}
	}

	private static <E extends Enum<E>> E doGetMetaAsEnum(
		JSONObject json, MetaAttribute attribute, Class<E> enumClass) throws DscpContainerException {
		return doGetMetaAsEnum(json, attribute, enumClass, false);
	}

	private static <E extends Enum<E>> E doGetMetaAsEnum(
		JSONObject json, MetaAttribute attribute, Class<E> enumClass, boolean tolerant) throws DscpContainerException {
		try {
			String code = doGetMetaAsString(json, attribute, tolerant);
			return InvariantUtils.getValueByCode(code, enumClass, tolerant);
		} catch (IllegalArgumentException e) {
			String message = e.getMessage() + " for meta key '" + attribute.getPropertyKey() + "'";
			throw DscpContainerException.metaKeyProblem(message, attribute.getPropertyKey());
		}
	}
}
