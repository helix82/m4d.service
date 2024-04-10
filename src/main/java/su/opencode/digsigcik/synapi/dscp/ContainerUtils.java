package su.opencode.digsigcik.synapi.dscp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import su.opencode.digsigcik.synapi.crypto.BasicSecurityProvider;
import su.opencode.digsigcik.synapi.crypto.SecurityProvider;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class ContainerUtils {

	private static final Log LOG = LogFactory.getLog(ContainerUtils.class);
	private static final BasicSecurityProvider BASIC_SECURITY_PROVIDER = new SimpleSecurityProvider();

	private ContainerUtils() {
		throw new UnsupportedOperationException();
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

	public static List<ResultFileDescription> getFromContainer(
		Container container, List<ResultTypeEnum> returnParts,
		boolean mandatory, SecurityProvider provider) throws DscpContainerException {

		if (returnParts == null || returnParts.isEmpty()) return Collections.emptyList();
		List<ResultFileDescription> res = new ArrayList<>(returnParts.size());
		for (ResultTypeEnum returnPart : returnParts) {
			if (returnPart == null) {
				if (mandatory) throw new IllegalStateException("This is an empty returnPart (maybe there is no such a code)");
				res.add(null);
				continue;
			}

			ResultFileDescription resFile = BASIC_SECURITY_PROVIDER.provideSecurityAction(container, returnPart, provider);
			if (resFile != null) {
				res.add(resFile);
			} else if (!mandatory) {
				res.add(null);
			} else {
				throw DscpContainerException.resultCauseException("Cannot find mandatory part of result " + returnPart, returnPart, null);
			}
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

	public static void fillContainer(Container container, Map<ContainerFileEnum, byte[]> filContentMap) throws DscpContainerException {
		fillContainer(container, filContentMap, Collections.emptyMap());
	}

	public static void fillContainer(Container container,
	                                 Map<ContainerFileEnum, byte[]> fileContentMap,
	                                 Map<MetaAttribute, String> metaDataMap) throws DscpContainerException {
		container.fillContent(fileContentMap, metaDataMap);
	}

	public static void fillContainer(Container container, ContainerFileEnum fileType, byte[] fileContent) throws DscpContainerException {
		fillContainer(container, Collections.singletonMap(fileType, fileContent));
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

}
