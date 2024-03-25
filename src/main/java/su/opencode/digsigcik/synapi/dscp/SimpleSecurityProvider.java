package su.opencode.digsigcik.synapi.dscp;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import su.opencode.digsigcik.synapi.crypto.*;

import java.nio.charset.StandardCharsets;

/**
 * This provider only gets result from a container file
 */
class SimpleSecurityProvider implements BasicSecurityProvider {

	private final Log log = LogFactory.getLog(SimpleSecurityProvider.class);

	@Override
	public ResultFileDescription provideSecurityAction(Container container, ResultTypeEnum resultType, SecurityProvider provider) throws DscpContainerException {
		try {
			String contentType = resultType.getContentType(container);
			String fileName = resultType.getFileName(container);

			if (resultType.isDirectFile()) {
				//Directly file from container
				return doGetDirectFile(resultType, contentType, fileName, container, resultType.getFileType());
			}

			if (resultType.isFromMeta()) {
				//Is directly from metadata file
				String metaData = resultType.getMeta(container);
				if (StringUtils.isBlank(metaData)) return null;
				byte[] content = metaData.getBytes(StandardCharsets.UTF_8);
				log.debug(String.format("Result type %s is a returning of direct meta data string with symbols length of %d chars",
					resultType, metaData.length()));
				return new ResultFileDescription(resultType, fileName, contentType, content);
			}

			if (resultType == ResultTypeEnum.SIGN_CHECK) {
				if (container.getProperties().isDetached() && provider instanceof DetachedSignCheckProvider) {
					log.debug(String.format("Container is detached signature and signature security provider of class %s is given, " +
						"it will provide signature check", provider.getClass().getName()));
					ResultFileDescription srcFile = provideSecurityAction(container, ResultTypeEnum.SOURCE_FILE, provider);
					ResultFileDescription detachedSign = provideSecurityAction(container, ResultTypeEnum.DETACHED_SIGN, provider);
					DetachedSignCheckProvider detachedSignCheckProvider = (DetachedSignCheckProvider) provider;
					CheckResult checkResult = detachedSignCheckProvider.checkDetachedSignature(srcFile.getContent(), detachedSign.getContent());
					byte[] content = checkResult.getCheckContent();
					container.rebuild(ContainerFileEnum.LAST_SIGN_CHECK, content, provider);
					return new ResultFileDescription(resultType, fileName, contentType, content);
				} else if (container.getProperties().getAttachedSignatureType() != null && provider instanceof AttachedSignCheckProvider) {
					log.debug(String.format("Container is attached signature of type %s and signature security provider " +
						"of class %s is given, it will provide signature check",
						container.getProperties().getAttachedSignatureType(), provider.getClass().getName()));
					ResultFileDescription attachedSign = provideSecurityAction(container, ResultTypeEnum.ATTACHED_SIGN, provider);
					AttachedSignCheckProvider attachedSignCheckProvider = (AttachedSignCheckProvider) provider;
					CheckResult checkResult = attachedSignCheckProvider.checkAttachedSignature(attachedSign.getContent(), container.getProperties().getAttachedSignatureType());
					byte[] content = checkResult.getCheckContent();
					container.rebuild(ContainerFileEnum.LAST_SIGN_CHECK, content, provider);
					return new ResultFileDescription(resultType, fileName, contentType, content);
				} else {
					return doGetDirectFile(resultType, contentType, fileName, container, ContainerFileEnum.LAST_SIGN_CHECK);
				}

			}

			if (resultType == ResultTypeEnum.CERT_DATA) {
				if (container.getProperties().isDetached() && provider instanceof CertInfoProvider) {
					log.debug(String.format("Container is detached signature and signature security provider of class %s is given, " +
						"it will provide cert data extraction", provider.getClass().getName()));
					ResultFileDescription detachedSign = provideSecurityAction(container, ResultTypeEnum.DETACHED_SIGN, provider);
					CertInfoProvider certInfoProvider = (CertInfoProvider) provider;
					byte[] content = certInfoProvider.getCertInfo(detachedSign.getContent(), null);
					container.rebuild(ContainerFileEnum.LAST_CERT_DATA, content, provider);
					return new ResultFileDescription(resultType, fileName, contentType, content);
				} else if (container.getProperties().getAttachedSignatureType() != null && provider instanceof CertInfoProvider) {
					log.debug(String.format("Container is attached signature of type %s and signature security provider " +
							"of class %s is given, it will provide cert data extraction",
						container.getProperties().getAttachedSignatureType(), provider.getClass().getName()));
					ResultFileDescription attachedSign = provideSecurityAction(container, ResultTypeEnum.ATTACHED_SIGN, provider);
					CertInfoProvider certInfoProvider = (CertInfoProvider) provider;
					byte[] content = certInfoProvider.getCertInfo(attachedSign.getContent(), container.getProperties().getAttachedSignatureType());
					container.rebuild(ContainerFileEnum.LAST_CERT_DATA, content, provider);
					return new ResultFileDescription(resultType, fileName, contentType, content);
				} else {
					return doGetDirectFile(resultType, contentType, fileName, container, ContainerFileEnum.LAST_CERT_DATA);
				}
			}

			if (resultType == ResultTypeEnum.POA_CHECK) {
				if (!container.getProperties().hasPoa()) return null;

				if (provider instanceof PoaCheckProvider) {
					log.debug(String.format("Container is a signature with POA and signature security provider of class %s is given, " +
						"it will provide POA check", provider.getClass().getName()));
					ResultFileDescription poa = provideSecurityAction(container, ResultTypeEnum.POA_CONTAINER, provider);
					assert poa != null && poa.getContent() != null && poa.getContent().length > 0;
					Container poaContainer = ContainerUtils.createContainer(poa.getContent());
					ResultFileDescription certData = provideSecurityAction(container, ResultTypeEnum.CERT_DATA, provider);
					PoaCheckProvider poaCheckProvider = (PoaCheckProvider) provider;
					CheckResult checkResult = poaCheckProvider.checkPoa(certData.getContent(), poaContainer, provider);
					byte[] content = checkResult.getCheckContent();
					container.rebuild(ContainerFileEnum.LAST_POA_CHECK, content, provider);
					return new ResultFileDescription(resultType, fileName, contentType, content);
				} else {
					return doGetDirectFile(resultType, contentType, fileName, container, ContainerFileEnum.LAST_POA_CHECK);
				}
			}

			if (resultType == ResultTypeEnum.ATTACHED_SIGN) {
				if (container.getProperties().isDetached()
					|| container.getProperties().getAttachedSignatureType() == null) return null;
				ContainerFileEnum fileType = ContainerFileEnum.byAttachedSignatureType(container.getProperties().getAttachedSignatureType());
				return doGetDirectFile(resultType, contentType, fileName, container, fileType);
			}

			if (resultType == ResultTypeEnum.DETACHED_SIGN) {
				if (!container.getProperties().isDetached()) return null;
				ContainerFileEnum fileType = ContainerFileEnum.byEnhanceType(container.getProperties().getEnhanceType());
				return doGetDirectFile(resultType, contentType, fileName, container, fileType);
			}

			log.warn("Cannot find resultType=" + resultType);
			return null;
		} catch (Exception e) {
			throw DscpContainerException.resultCauseException(resultType, e);
		}
	}

	private ResultFileDescription doGetDirectFile(ResultTypeEnum resultType, String contentType, String fileName, Container container, ContainerFileEnum fileType) {
		if (fileType == null) return null; //TODO: maybe throw exception
		byte[] content = container.getMainFile(fileType);
		if (content == null || content.length <= 0) return null;
		log.debug(String.format("Result type %s is a returning of container file %s of contentType=%s with length of %d bytes",
			resultType, fileType, contentType, content.length));
		return new ResultFileDescription(resultType, fileName, contentType, content);
	}
}
