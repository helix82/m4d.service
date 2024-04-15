package su.opencode.digsigcik.synapi.util;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import su.opencode.digsigcik.synapi.api.model.StampPosition;

import java.io.IOException;

@Deprecated
public final class StubUtils {
	private StubUtils() {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	public static Resource getStubFileResource(String path) {
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		path = ("file:files/" + path.replace("\\", "/")).replace("//", "/");
		return resolver.getResource(path);
	}

	@Deprecated
	public static byte[] getStubFileContent(String path) {
		Resource resource = getStubFileResource(path);
		try {
			return IOUtils.toByteArray(resource.getInputStream());
		} catch (IOException e) {
			throw new IllegalStateException("Cannot read content of resource '" + path + "'");
		}
	}

	@Deprecated
	public static StampPosition createStampPosition() {
		return new StampPosition().page(0).topLeftX(43).topLeftY(83);
	}
}
