package su.opencode.digsigcik.synapi.client;

import lombok.Builder;
import lombok.NonNull;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Builder
public class RestRequest {
    @NonNull
    RestMethod httpMethod;
    @NonNull
    URI requestUri;
    HashMap<String, String> httpHeader;
    String requestContent;
    File file;
    String fileName;
    InputStream inputStream;
    byte[] bytes;
    String mimeType;
    Map<String, String> requestForm;
}
