package su.opencode.digsigcik.synapi.util;

import org.apache.tika.Tika;
import org.springframework.http.MediaType;

public final class CommonUtils {

	private static final Tika TIKA = new Tika();

	private CommonUtils() {
		throw new UnsupportedOperationException();
	}

	public static MediaType toMediaType(String contentType) {
		int pos = contentType.indexOf('/');
		if (contentType.length() < 3 || pos < 1 || pos >= contentType.length() - 1)
			throw new IllegalArgumentException("Invalid content type " + contentType);
		return new MediaType(contentType.substring(0, pos), contentType.substring(pos + 1));
	}

	public static MediaType toMediaType(byte[] content) {
		String contentType = TIKA.detect(content);
		return toMediaType(contentType);
	}
}
