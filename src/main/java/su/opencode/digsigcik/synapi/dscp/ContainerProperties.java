package su.opencode.digsigcik.synapi.dscp;

import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import su.opencode.digsigcik.synapi.util.CommonUtils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

public class ContainerProperties {
	private final JSONObject metaContentJson;

	private final String sourceFileName;
	private final MediaType sourceFileContentType;
	private final String containerDescription;
	private final boolean detached;
	private final boolean hasPreview;
	private final boolean hasPoa;
	private final ContainerTypeEnum containerType;
	private final SignatureTypeEnum signatureType;
	private final EnhanceTypeEnum enhanceType;
	private final AttachedSignatureTypeEnum attachedSignatureType;

	ContainerProperties(byte[] metaContentByte) throws DscpContainerException {
		this(doMakeJsonObject(metaContentByte));
	}

	ContainerProperties(JSONObject metaContentJson) throws DscpContainerException {
		this.metaContentJson = metaContentJson;
		this.sourceFileName = doGetMetaAsString(metaContentJson, MetaAttribute.MAIN_FILE_FULL_NAME, false);
		String sourceFileContentTypeStr = doGetMetaAsString(metaContentJson, MetaAttribute.MAIN_FILE_CONTENT_TYPE, false);
		assert sourceFileContentTypeStr != null;

		this.sourceFileContentType = CommonUtils.toMediaType(sourceFileContentTypeStr);
		this.containerDescription = doGetMetaAsString(metaContentJson, MetaAttribute.CONTAINER_DESCRIPTION, true);
		this.detached = doGetMetaAsBoolean(metaContentJson, MetaAttribute.IS_DETACHED);
		this.hasPoa = doGetMetaAsBoolean(metaContentJson, MetaAttribute.HAS_POA);
		this.hasPreview = doGetMetaAsBoolean(metaContentJson, MetaAttribute.HAS_PREVIEW);
		this.containerType = doGetMetaAsEnum(metaContentJson, MetaAttribute.CONTAINER_TYPE, ContainerTypeEnum.class);
		this.signatureType = doGetMetaAsEnum(metaContentJson, MetaAttribute.SIGNATURE_TYPE, SignatureTypeEnum.class);
		this.enhanceType = doGetMetaAsEnum(metaContentJson, MetaAttribute.ENHANCE_TYPE, EnhanceTypeEnum.class);
		this.attachedSignatureType = doGetMetaAsEnum(metaContentJson, MetaAttribute.ATTACHED_TYPE, AttachedSignatureTypeEnum.class, true);
	}

	ContainerProperties(
		ContainerTypeEnum containerType, String srcFileFullName, MediaType srcFileContentType,
		long srcFileSize, String hashType, String hash) throws DscpContainerException {

		this.metaContentJson = new JSONObject();
		this.containerType = containerType;
		this.sourceFileName = srcFileFullName;
		this.sourceFileContentType = srcFileContentType;
		this.detached = containerType.isDetached();
		this.hasPoa = false; //By default is false
		this.hasPreview = false; //By default is false
		this.signatureType = SignatureTypeEnum.UNKNOWN;
		this.enhanceType = EnhanceTypeEnum.ADES_BES; //By default no enhance
		this.containerDescription = null; //No description by default
		if (this.containerType == ContainerTypeEnum.PDF_ATTACHED)
			this.attachedSignatureType = AttachedSignatureTypeEnum.PDF;
		else if (this.containerType == ContainerTypeEnum.DOCX_ATTACHED)
			this.attachedSignatureType = AttachedSignatureTypeEnum.DOCX;
		else if (this.containerType == ContainerTypeEnum.XML_DSIG)
			this.attachedSignatureType = AttachedSignatureTypeEnum.XML;
		else this.attachedSignatureType = null;

		Map<MetaObjectEnum, JSONObject> parts = new EnumMap<>(MetaObjectEnum.class);
		Arrays.stream(MetaObjectEnum.values()).forEach(item -> {
			JSONObject part = new JSONObject();
			this.metaContentJson.put(item.getCode(), part);
			parts.put(item, part);
		});


		doSetMeta(parts, MetaAttribute.CONTAINER_TYPE, this.containerType);
		doSetMeta(parts, MetaAttribute.MAIN_FILE_FULL_NAME, this.sourceFileName);
		int dotPos = srcFileFullName.lastIndexOf('.');
		if (dotPos < 0) {
			doSetMeta(parts, MetaAttribute.MAIN_FILE_NAME, this.sourceFileName);
			doSetMeta(parts, MetaAttribute.MAIN_FILE_EXTENSION, "");
		} else {
			doSetMeta(parts, MetaAttribute.MAIN_FILE_NAME, this.sourceFileName.substring(0, dotPos));
			doSetMeta(parts, MetaAttribute.MAIN_FILE_EXTENSION, this.sourceFileName.substring(dotPos + 1).toLowerCase());
		}
		doSetMeta(parts, MetaAttribute.MAIN_FILE_CONTENT_TYPE, this.sourceFileContentType.toString());
		doSetMeta(parts, MetaAttribute.IS_DETACHED, this.detached);
		doSetMeta(parts, MetaAttribute.HAS_POA, this.hasPoa);
		doSetMeta(parts, MetaAttribute.HAS_PREVIEW, this.hasPreview);
		doSetMeta(parts, MetaAttribute.ATTACHED_TYPE, this.attachedSignatureType);
		doSetMeta(parts, MetaAttribute.SIGNATURE_TYPE, SignatureTypeEnum.UNKNOWN);
		doSetMeta(parts, MetaAttribute.ENHANCE_TYPE, EnhanceTypeEnum.ADES_BES);
		doSetMeta(parts, MetaAttribute.MAIN_FILE_SIZE, srcFileSize);
		doSetMeta(parts, MetaAttribute.MAIN_FILE_HASH_TYPE, hashType);
		doSetMeta(parts, MetaAttribute.MAIN_FILE_HASH, hash);
		doSetMeta(parts, MetaAttribute.CONTAINER_SIZE, srcFileSize);
		doSetMeta(parts, MetaAttribute.CONTAINER_HASH_TYPE, hashType);
		doSetMeta(parts, MetaAttribute.CONTAINER_HASH, hash);
	}

	@SuppressWarnings("rawtypes")
	private static void doSetMeta(Map<MetaObjectEnum, JSONObject> parts, MetaAttribute attr, @Nullable HasCode value) {
		doSetMeta(parts, attr, value == null ? null : value.getCode());
	}

	private static void doSetMeta(Map<MetaObjectEnum, JSONObject> parts, MetaAttribute attr, @Nullable Object value) {
		JSONObject obj = parts.get(attr.getObject());
		assert obj != null;
		obj.put(attr.getAttribute(), value);
	}

	private static JSONObject doMakeJsonObject(byte[] metaContentByte) {
		try {
			String metaContentStr = new String(metaContentByte, StandardCharsets.UTF_8);
			return new JSONObject(metaContentStr);
		} catch (JSONException e) {
			//Not a valid JSON format of meta.json
			throw DscpContainerException.internalFormatFileProblem("Not valid metadata json format", e, ContainerFileEnum.META);
		}
	}

	//==================================public==========================================================================

	public String getSourceFileName() {
		return sourceFileName;
	}

	public MediaType getSourceFileContentType() {
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

	JSONObject getMetaContentJson() {
		return metaContentJson;
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
