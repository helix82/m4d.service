package su.opencode.digsigcik.synapi.dscp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import su.opencode.digsigcik.synapi.crypto.BasicSecurityProvider;
import su.opencode.digsigcik.synapi.crypto.SecurityProvider;

/**
 * This provider only gets result from a container file
 */
class SimpleSecurityProvider implements BasicSecurityProvider {

	private final Log log = LogFactory.getLog(SimpleSecurityProvider.class);

	@Override
	public ResultFileDescription provideSecurityAction(Container container, ResultTypeEnum resultType, SecurityProvider provider) throws DscpContainerException {
		return null;
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
