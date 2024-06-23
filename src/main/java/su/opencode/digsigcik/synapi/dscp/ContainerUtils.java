package su.opencode.digsigcik.synapi.dscp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.MediaType;
import su.opencode.digsigcik.synapi.util.CommonUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public final class ContainerUtils {

	private static final Log LOG = LogFactory.getLog(ContainerUtils.class);

	private ContainerUtils() {
		throw new UnsupportedOperationException();
	}

	public static Container createContainer(
		ContainerTypeEnum type, byte[] content, String fileName) throws DscpContainerException {
		return new Container(type, content, fileName, null);
	}

	public static Container createContainer(
		ContainerTypeEnum type, byte[] content, String fileName, MediaType contentType, String containerHashType) throws DscpContainerException {

		return new Container(type, content, fileName,
			contentType == null ? CommonUtils.toMediaType(content) : contentType,containerHashType);
	}

	public static Container readContainer(File filePath) throws DscpContainerException {
		try (InputStream in = new FileInputStream(filePath)) {
			return new Container(in);
		} catch (IOException e) {
			throw DscpContainerException.ioCause(e, true);
		}
	}

	public static Container readContainer(String filePath) throws DscpContainerException {
		return readContainer(new File(filePath));
	}

	public static Container readContainer(byte[] containerContent) throws DscpContainerException {
		try (InputStream in = new ByteArrayInputStream(containerContent)) {
			return readContainer(in);
		} catch (IOException e) {
			throw DscpContainerException.ioCause(e, true);
		}
	}

	public static Container readContainer(InputStream in) throws DscpContainerException {
		return new Container(in);
	}

	public static Map<ResultTypeEnum, ResultFileDescription> getFromContainer(Container container, Collection<ResultTypeEnum> returnParts) throws DscpContainerException {
		return getFromContainer(container, returnParts, true);
	}

	public static Map<ResultTypeEnum, ResultFileDescription> getFromContainer(
		Container container, Collection<ResultTypeEnum> returnParts, boolean mandatory) throws DscpContainerException {

		if (returnParts == null || returnParts.isEmpty()) return Collections.emptyMap();

		Map<ResultTypeEnum, ResultFileDescription> res = new EnumMap<>(ResultTypeEnum.class);
		for (ResultTypeEnum resType : returnParts) {
			if (resType == null || res.containsKey(resType)) continue;
			ResultFileDescription resItem = container.getResultPart(resType);
			if (resItem != null) res.put(resType, resItem);
			else if (mandatory) throw DscpContainerException.resultCauseException(resType, null);
			else
				LOG.debug(String.format("No content found in container for '%s' for resultType=%s", container.getProperties().getSourceFileName(), resType));
		}
		return res;
	}

	public static void writeContainer(OutputStream out, Container container) throws DscpContainerException {
		container.write(out);
	}

	public static byte[] containerContent(Container container) throws DscpContainerException {
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			writeContainer(out, container);
			return out.toByteArray();
		} catch (IOException e) {
			throw DscpContainerException.ioCause(e, false);
		}
	}

	public static void fillContainerByResult(Container container, Map<ResultTypeEnum, byte[]> resultContent) throws DscpContainerException {
		Map<ContainerFileEnum, byte[]> fileContentMap = new EnumMap<>(ContainerFileEnum.class);
		Map<MetaAttribute, String> metaDataMap = new HashMap<>(resultContent.size());
		ContainerFileEnum file;
		MetaAttribute metaAttribute;
		for (Map.Entry<ResultTypeEnum, byte[]> entry : resultContent.entrySet()) {
			byte[] content = entry.getValue() == null || entry.getValue().length <= 0 ? null : entry.getValue();
			if ((file = entry.getKey().getFileType()) != null) {
				fileContentMap.put(file, content);
			} else if ((metaAttribute = entry.getKey().getContentMeta()) != null) {
				String metaValue = content == null ? null : new String(content, StandardCharsets.UTF_8);
				metaDataMap.put(metaAttribute, metaValue);
			} else {
				String length = content == null ? null : String.valueOf(content.length);
				LOG.warn(String.format("Invalid result type given %s, for set content with length of %s", entry.getKey(), length));
			}
		}
		fillContainer(container, fileContentMap, metaDataMap);
	}

	static void fillContainer(Container container, Map<ContainerFileEnum, byte[]> filContentMap) throws DscpContainerException {
		fillContainer(container, filContentMap, Collections.emptyMap());
	}

	static void fillContainer(Container container,
	                          Map<ContainerFileEnum, byte[]> fileContentMap,
	                          Map<MetaAttribute, String> metaDataMap) throws DscpContainerException {
		container.fillContent(fileContentMap, metaDataMap);
	}

	static void fillContainer(Container container, ContainerFileEnum fileType, byte[] fileContent) throws DscpContainerException {
		fillContainer(container, Collections.singletonMap(fileType, fileContent));
	}

}
