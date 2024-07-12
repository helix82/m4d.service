package su.opencode.digsigcik.synapi.util;

import lombok.experimental.UtilityClass;
import org.apache.tika.Tika;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.io.Resource;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@UtilityClass
public final class CommonUtils {

	private static final Tika TIKA = new Tika();

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

	@SuppressWarnings("SameParameterValue")
	public static String filename(@NotNull Resource resource,
								   @NotNull String fallback) {
		return Optional.ofNullable(resource.getFilename())
				.orElse(fallback);
	}

	public static String filename(@NotNull MultipartFile file,
								   @Nullable String prefix) {
		String s = Optional.ofNullable(file.getOriginalFilename())
				.orElse(file.getName());
		return addPrefix(s, prefix);
	}

	public static String addPrefix(@NotNull String s,
									@Nullable String prefix) {
		return prefix == null ? s : String.join("_", prefix, s);
	}

	public static MediaType mediaType(@NotNull String type) {
		try {
			return MediaType.valueOf(type);
		} catch (InvalidMediaTypeException e) {
			throw new IllegalStateException("Invalid media type: " + type);
		}
	}
}
