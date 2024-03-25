package su.opencode.digsigcik.synapi.dscp;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import su.opencode.digsigcik.synapi.crypto.SecurityProvider;

import java.io.*;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Container {

	private static final char ZIP_PATH_DELIMITER = '/';

	private final Map<ContainerFileEnum, byte[]> mainFiles = new EnumMap<>(ContainerFileEnum.class);
	private final Map<String, byte[]> extraFiles = new LinkedHashMap<>();
	private final ContainerProperties properties;

	Container(InputStream in) throws DscpContainerException {
		//Fill container content from file
		try {
			doFillFiles(in, mainFiles, extraFiles);
		} catch (IOException e) {
			throw DscpContainerException.ioCause(e, true);
		}

		//Check mandatory container files
		doCheckMandatoryFiles(mainFiles);

		//Fill container meta
		byte[] metaContent = mainFiles.get(ContainerFileEnum.META);
		assert metaContent != null;
		properties = new ContainerProperties(metaContent);

		//Check main file is matched
		doCheckMatch(mainFiles, properties, Collections.singletonList(ContainerFileEnum.MAIN_FILE),
			MetaAttribute.MAIN_FILE_SIZE, MetaAttribute.MAIN_FILE_HASH_TYPE, MetaAttribute.MAIN_FILE_HASH,
			"Main file do not match: ");

		//Check container is matched
		List<ContainerFileEnum> files = new ArrayList<>(ContainerFileEnum.values().length);
		for (ContainerFileEnum file : ContainerFileEnum.values()) {
			if (file.isCalculateDigest()) files.add(file);
		}
		doCheckMatch(mainFiles, properties, files,
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
			extraFiles.forEach((k,v)->
				str.append("\n\t'").append(k)
					.append("' of a ").append(v.length).append(" bytes size;"));
		}

		return str.length() > 0 ? str.substring(1) : "EMPTY container";
	}

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

	void rebuild(ContainerFileEnum fileType, byte[] newContent, SecurityProvider provider) {
		//TODO: make container rebuild
	}

	//=================================private==========================================================================

	private static String getDirectoryPath(String filePath) {
		int delimIndex = filePath.lastIndexOf(ZIP_PATH_DELIMITER);
		if (delimIndex <= 0) return null;
		else return filePath.substring(0, delimIndex+1); //Directory path should contain delimiter
	}

	private static void doCheckMatch(Map<ContainerFileEnum, byte[]> mainFiles, ContainerProperties properties, List<ContainerFileEnum> filesToCheck,
	                                 MetaAttribute sizeMeta, MetaAttribute hashTypeMeta, MetaAttribute hashMeta,
	                                 String messagePrefix) throws DscpContainerException {
		doCheckSize(mainFiles, properties, filesToCheck, sizeMeta, messagePrefix);
		doCheckHash(mainFiles, properties, filesToCheck, hashTypeMeta, hashMeta, messagePrefix);
	}

	private static void doCheckHash(Map<ContainerFileEnum, byte[]> mainFiles, ContainerProperties properties, List<ContainerFileEnum> filesToCheck, MetaAttribute hashTypeMeta, MetaAttribute hashMeta, String messagePrefix) throws DscpContainerException {
		//Check hash
		String hashType = properties.getMetaAsString(hashTypeMeta);
		try {
			MessageDigest digest = MessageDigest.getInstance(hashType);
			try(OutputStream out = new DigestOutputStream(NullOutputStream.nullOutputStream(), digest)) {
				for (ContainerFileEnum fileToCheck : filesToCheck) {
					byte[] fileContent = mainFiles.get(fileToCheck);
					if (fileContent != null) IOUtils.write(fileContent, out);
				}
			} catch (IOException e) {
				throw new IllegalStateException("Not expected error here while writing into null output stream", e);
			}
			String hash = Hex.encodeHexString(digest.digest());
			String expectedHash = properties.getMetaAsString(hashMeta);
			if (!hash.equals(expectedHash))
				throw DscpContainerException.contentSecurityException(messagePrefix + "hash of type " + hashType + " was not matched");
		} catch (NoSuchAlgorithmException e) {
			throw DscpContainerException.contentSecurityException("There are problems with hashType algorithm " + hashType, e);
		}
	}

	private static void doCheckSize(Map<ContainerFileEnum, byte[]> mainFiles, ContainerProperties properties, List<ContainerFileEnum> filesToCheck, MetaAttribute sizeMeta, String messagePrefix) throws DscpContainerException {
		//Check size
		long length = 0;
		for (ContainerFileEnum fileToCheck : filesToCheck) {
			byte[] fileContent = mainFiles.get(fileToCheck);
			if (fileContent != null) length += fileContent.length;
		}

		long expectedLength = properties.getMetaAsLong(sizeMeta);

		if (length != expectedLength) {
			throw DscpContainerException.contentSecurityException(messagePrefix + "expected size is " + expectedLength + " bytes, current size is " + length + " bytes");
		}
	}

	private static void doCheckMandatoryFiles(Map<ContainerFileEnum, byte[]> mainFiles) throws DscpContainerException {
		for (ContainerFileEnum fileType : ContainerFileEnum.values()) {
			if (!fileType.isMandatory()) continue;
			byte[] content = mainFiles.get(fileType); //Mandatory content
			if (content == null || content.length <= 0) {
				throw DscpContainerException.internalFormatFileProblem("Mandatory container part " + fileType + " was not found", fileType);
			}
		}
	}

	private static void doFillFiles(InputStream in, Map<ContainerFileEnum, byte[]> mainFiles, Map<String, byte[]> extraFiles)
		throws IOException, DscpContainerException {

		try(ZipInputStream zipIn = new ZipInputStream(in)) {
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
}
