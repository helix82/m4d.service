package su.opencode.digsigcik.synapi.dscp;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Container {

	private static final char ZIP_PATH_DELIMITER = '/';
	private static final Log LOG = LogFactory.getLog(Container.class);
	private static final String DEFAULT_HASH_ALG = "SHA256";
	private static final Set<ContainerFileEnum> NEED_HISTORY_ADD = Collections.unmodifiableSet(
		EnumSet.of(ContainerFileEnum.LAST_SIGN_CHECK, ContainerFileEnum.LAST_CERT_DATA, ContainerFileEnum.LAST_POA_CHECK));

	private final Map<ContainerFileEnum, byte[]> mainFiles = new EnumMap<>(ContainerFileEnum.class);
	private final Map<String, byte[]> extraFiles = new LinkedHashMap<>();
	private ContainerProperties properties;

	Container(InputStream in) throws DscpContainerException {
		//Fill container content from file
		try {
			fillFilesContent(in, mainFiles, extraFiles);
		} catch (IOException e) {
			throw DscpContainerException.ioCause(e, true);
		}

		//Check mandatory container files
		checkMandatoryFiles(mainFiles);

		//Fill container meta
		byte[] metaContent = mainFiles.get(ContainerFileEnum.META);
		assert metaContent != null;
		properties = new ContainerProperties(metaContent);

		//Check main file is matched
		checkContainerMatch(mainFiles, properties, Collections.singletonList(ContainerFileEnum.MAIN_FILE),
			MetaAttribute.MAIN_FILE_SIZE, MetaAttribute.MAIN_FILE_HASH_TYPE, MetaAttribute.MAIN_FILE_HASH,
			"Main file do not match: ");

		checkContainerMatch(mainFiles, properties, null,
			MetaAttribute.CONTAINER_SIZE, MetaAttribute.CONTAINER_HASH_TYPE, MetaAttribute.CONTAINER_HASH,
			"Container content do not match: ");


	}

	public ContainerProperties getProperties() {
		return properties;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		if (!mainFiles.isEmpty()) {
			str.append("\nMain files in container:");
			mainFiles.forEach((k,v)->
				str.append('\n').append('\t').append(k.name())
					.append(" of a ").append(v.length).append(" bytes size;"));
		}

		if (!extraFiles.isEmpty()) {
			str.append("\nExtra files in container:");
			extraFiles.forEach((k, v) ->
				str.append("\n\t'").append(k)
					.append("' of a ").append(v.length).append(" bytes size;"));
		}

		return str.length() > 0 ? str.substring(1) : "EMPTY container";
	}

	//=================================package==========================================================================

	byte[] getMainFile(ContainerFileEnum fileType) {
		return mainFiles.get(fileType);
	}

	void write(OutputStream out) throws DscpContainerException {
		Set<String> directories = new HashSet<>();
		mainFiles.keySet().stream()
			.map(ContainerFileEnum::getPath)
			.map(Container::getDirectoryPath)
			.filter(Objects::nonNull)
			.forEach(directories::add);
		extraFiles.keySet().stream()
			.map(Container::getDirectoryPath)
			.filter(Objects::nonNull)
			.forEach(directories::add);

		try(ZipOutputStream zipOut = new ZipOutputStream(out)) {

			//Create directories
			for (String dirPath : directories) {
				zipOut.putNextEntry(new ZipEntry(dirPath));
				zipOut.closeEntry();
			}

			//Create main files
			for(Map.Entry<ContainerFileEnum, byte[]> file : mainFiles.entrySet()) {
				zipOut.putNextEntry(new ZipEntry(file.getKey().getPath()));
				IOUtils.writeChunked(file.getValue(), zipOut);
				zipOut.closeEntry();
			}

			//Create extra files
			for(Map.Entry<String, byte[]> file : extraFiles.entrySet()) {
				zipOut.putNextEntry(new ZipEntry(file.getKey()));
				IOUtils.writeChunked(file.getValue(), zipOut);
				zipOut.closeEntry();
			}

			zipOut.finish();
		} catch (IOException e) {
			throw DscpContainerException.ioCause(e, false);
		}
	}

	void fillContent(Map<ContainerFileEnum, byte[]> fileContentMap, Map<MetaAttribute, String> metaDataMap)
		throws DscpContainerException {

		checkFillContentParams(fileContentMap, metaDataMap);

		JSONArray history = getCheckHistory();

		//Fill files content
		boolean fileChanged = false;
		for (Map.Entry<ContainerFileEnum, byte[]> fileEntry : fileContentMap.entrySet()) {
			if (fileEntry.getValue() == null || fileEntry.getValue().length <= 0) {
				//Delete content
				byte[] oldValue = mainFiles.remove(fileEntry.getKey());
				if (oldValue != null && oldValue.length > 0) {
					fileChanged |= fileEntry.getKey().isCalculateDigest();
					LOG.debug(String.format("File type %s content of length %d was removed", fileEntry.getKey(), oldValue.length));
				}
			} else {
				//Insert or change content
				byte[] oldValue = mainFiles.put(fileEntry.getKey(), fileEntry.getValue());
				fileChanged |= fileEntry.getKey().isCalculateDigest();
				if (oldValue != null && oldValue.length > 0) {
					LOG.debug(String.format("File type %s content of length %d was replaced with content of length %d", fileEntry.getKey(), oldValue.length, fileEntry.getValue().length));
				} else {
					LOG.debug(String.format("File type %s content of length %d is added", fileEntry.getKey(), fileEntry.getValue().length));
				}

				if (NEED_HISTORY_ADD.contains(fileEntry.getKey())) {
					//Add check result is set to history
					JSONObject lastCheck = getJsonObject(fileEntry.getKey(), false);
					history.put(lastCheck);
				}
			}
		}

		//Fill history if needed
		if (history.length() > 0) {
			String historyStr = history.toString();
			byte[] historyContent = historyStr.getBytes(StandardCharsets.UTF_8);
			assert historyContent.length > 0;
			mainFiles.put(ContainerFileEnum.CHECK_HISTORY, historyContent);
			fileChanged |= ContainerFileEnum.CHECK_HISTORY.isCalculateDigest();
		}

		//Rebuild container checksum if needed
		if (fileChanged) {
			HashResult rebuildResult = calculateHash(mainFiles, properties, MetaAttribute.CONTAINER_HASH_TYPE, null);
			metaDataMap = ensureMutableMap(metaDataMap);
			metaDataMap.put(MetaAttribute.CONTAINER_SIZE, String.valueOf(rebuildResult.size));
			metaDataMap.put(MetaAttribute.CONTAINER_HASH_TYPE, String.valueOf(rebuildResult.hashType));
			metaDataMap.put(MetaAttribute.CONTAINER_HASH, String.valueOf(rebuildResult.hash));
		}

		//Rebuild meta
		if (!metaDataMap.isEmpty()) {
			try {
				JSONObject metaJson = properties.getMetaContentJson();
				for (Map.Entry<MetaAttribute, String> metaEntry : metaDataMap.entrySet()) {
					MetaAttribute attr = metaEntry.getKey();
					JSONObject part = metaJson.getJSONObject(attr.getObject().getCode());
					if (part == null || part.isEmpty()) {
						throw DscpContainerException.internalFormatFileProblem(
							String.format("Cannot get mandatory meta json part '%s'", attr.getObject().getCode()),
							ContainerFileEnum.META);
					} else if (StringUtils.isBlank(metaEntry.getValue())) {
						//Remove metadata
						Object oldValue = part.remove(attr.getAttribute());
						if (oldValue != null)
							LOG.debug(String.format("Meta data with oldValue='%s' was removed for key %s", oldValue, attr.getPropertyKey()));
					} else {
						//Fill metadata
						part.put(attr.getAttribute(), metaEntry.getValue());
						LOG.debug(String.format("Meta data for key=%s is set to '%s'", attr.getPropertyKey(), metaEntry.getValue()));
					}
				}
				String metaJsonStr = metaJson.toString();
				byte[] metaJsonContent = metaJsonStr.getBytes(StandardCharsets.UTF_8);
				assert metaJsonContent.length > 0;
				mainFiles.put(ContainerFileEnum.META, metaJsonContent);
				properties = new ContainerProperties(metaJson);
			} catch (JSONException e) {
				throw DscpContainerException.internalFormatFileProblem("Internal JSON format problemm for container metadata", e, ContainerFileEnum.META);
			}
		}
	}

	//=================================private==========================================================================

	private JSONArray getCheckHistory() {
		try {
			byte[] historyContent = mainFiles.get(ContainerFileEnum.CHECK_HISTORY);
			return historyContent == null || historyContent.length <= 0
				? new JSONArray()
				: new JSONArray(new String(historyContent, StandardCharsets.UTF_8));
		} catch (JSONException e) {
			throw DscpContainerException.internalFormatFileProblem("Check history is of invalid format", e, ContainerFileEnum.CHECK_HISTORY);
		}
	}

	private JSONObject getJsonObject(ContainerFileEnum fileType, boolean createNew) {
		byte[] jsonContent = mainFiles.get(fileType);
		if (jsonContent == null || jsonContent.length <= 0) {
			if (createNew) return new JSONObject();
			else
				throw DscpContainerException.internalFormatFileProblem("There is no mandatory JSON content for fileType=" + fileType, fileType);
		}
		try {
			return new JSONObject(new String(jsonContent, StandardCharsets.UTF_8));
		} catch (JSONException e) {
			throw DscpContainerException.internalFormatFileProblem("Invalid JSON format of fileType=" + fileType, e, fileType);
		}
	}

	private static Map<MetaAttribute, String> ensureMutableMap(Map<MetaAttribute, String> source) {
		if (source instanceof HashMap || source instanceof TreeMap) return source;

		return new HashMap<>(source);
	}

	private static void checkFillContentParams(Map<ContainerFileEnum, byte[]> fileContentMap, Map<MetaAttribute, String> metaDataMap) throws DscpContainerException {
		if (fileContentMap.containsKey(ContainerFileEnum.META)) {
			byte[] content = fileContentMap.get(ContainerFileEnum.META);
			throw DscpContainerException.fillContentProblem("Cannot fill meta file content. It is filled automatically",
				ContainerFileEnum.META, content);
		}

		if (fileContentMap.containsKey(ContainerFileEnum.CHECK_HISTORY)) {
			byte[] content = fileContentMap.get(ContainerFileEnum.CHECK_HISTORY);
			throw DscpContainerException.fillContentProblem("Cannot fill check history file content. It is filled automatically",
				ContainerFileEnum.CHECK_HISTORY, content);
		}

		if (metaDataMap.containsKey(MetaAttribute.CONTAINER_HASH)
			|| metaDataMap.containsKey(MetaAttribute.CONTAINER_HASH_TYPE)
			|| metaDataMap.containsKey(MetaAttribute.CONTAINER_SIZE)) {
			throw DscpContainerException.fillContentProblem("Cannot file meta attribute for container hash or size. It is calculated automatically", null);
		}
	}

	private static String getDirectoryPath(String filePath) {
		int delimIndex = filePath.lastIndexOf(ZIP_PATH_DELIMITER);
		if (delimIndex <= 0) return null;
		else return filePath.substring(0, delimIndex + 1); //Directory path should contain delimiter
	}

	private static void checkContainerMatch(Map<ContainerFileEnum, byte[]> mainFiles,
	                                        ContainerProperties properties,
	                                        List<ContainerFileEnum> filesToCheck,
	                                        MetaAttribute sizeMeta, MetaAttribute hashTypeMeta, MetaAttribute hashMeta,
	                                        String messagePrefix) throws DscpContainerException {


		HashResult hashResult = calculateHash(mainFiles, properties, hashTypeMeta, filesToCheck);

		long expectedLength = properties.getMetaAsLong(sizeMeta);
		if (hashResult.size != expectedLength) {
			throw DscpContainerException.contentSecurityException(String.format("%sexpected size is %d bytes, current size is %d bytes", messagePrefix, expectedLength, hashResult.size));
		}
		String expectedHash = properties.getMetaAsString(hashMeta);
		if (!hashResult.hash.equals(expectedHash)) {
			throw DscpContainerException.contentSecurityException(String.format("%shash of type %s was not matched", messagePrefix, hashResult.hashType));
		}
	}

	private static HashResult calculateHash(Map<ContainerFileEnum, byte[]> mainFiles, ContainerProperties properties,
	                                        MetaAttribute hashTypeMeta, List<ContainerFileEnum> filesToCheck) throws DscpContainerException {
		String hashType = properties.getMetaAsString(hashTypeMeta);
		if (filesToCheck == null) {
			filesToCheck = Arrays.stream(ContainerFileEnum.values())
				.filter(ContainerFileEnum::isCalculateDigest)
				.collect(Collectors.toUnmodifiableList());
		}
		long size = 0;
		try {
			MessageDigest digest = MessageDigest.getInstance(hashType);
			try (OutputStream out = new DigestOutputStream(NullOutputStream.nullOutputStream(), digest)) {
				for (ContainerFileEnum fileToCheck : filesToCheck) {
					byte[] fileContent = mainFiles.get(fileToCheck);
					if (fileContent != null && fileContent.length > 0) {
						IOUtils.write(fileContent, out);
						size += fileContent.length;
					}
				}
			} catch (IOException e) {
				throw new IllegalStateException("Not expected error here while writing into null output stream", e);
			}
			return new HashResult(hashType, Hex.encodeHexString(digest.digest()), size);
		} catch (NoSuchAlgorithmException e) {
			throw DscpContainerException.contentSecurityException("There are problems with hashType algorithm " + hashType, e);
		}
	}

	private static void checkMandatoryFiles(Map<ContainerFileEnum, byte[]> mainFiles) throws DscpContainerException {
		for (ContainerFileEnum fileType : ContainerFileEnum.values()) {
			if (!fileType.isMandatory()) continue;
			byte[] content = mainFiles.get(fileType); //Mandatory content
			if (content == null || content.length <= 0) {
				throw DscpContainerException.internalFormatFileProblem("Mandatory container part " + fileType + " was not found", fileType);
			}
		}
	}

	private static void fillFilesContent(
		InputStream in, Map<ContainerFileEnum, byte[]> mainFiles, Map<String, byte[]> extraFiles)
		throws IOException, DscpContainerException {

		try (ZipInputStream zipIn = new ZipInputStream(in)) {
			ZipEntry entry;
			while ((entry = zipIn.getNextEntry()) != null) {
				if (entry.isDirectory()) continue;

				String fileName = entry.getName();
				ContainerFileEnum typedFile = null;
				for (ContainerFileEnum fileType : ContainerFileEnum.values()) {
					if (fileType.getPath().equalsIgnoreCase(fileName)) {
						typedFile = fileType;
						break;
					}
				}

				byte[] content;
				try {
					content = IOUtils.toByteArray(zipIn);
				} catch (IOException e) {
					throw DscpContainerException.containerFileProblem(e, typedFile, fileName);
				}

				if (typedFile == null) extraFiles.put(fileName, content);
				else mainFiles.put(typedFile, content);
			}
		}
	}

	//=============================private static class=================================================================

	private static final class HashResult {
		private final String hashType;
		private final String hash;
		private final long size;

		private HashResult(String hashType, String hash, long size) {
			this.hashType = hashType;
			this.hash = hash;
			this.size = size;
		}
	}
}
