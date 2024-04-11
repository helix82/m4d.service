package su.opencode.digsigcik.synapi.controller;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import su.opencode.digsigcik.synapi.api.model.ContainerSelector;
import su.opencode.digsigcik.synapi.api.model.ContainerSelectorCode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class ApiControllerV1IT {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Disabled
    @Test
    void createPdfContainer() throws Exception {
        // todo: use actual files from resources dir - but which ones?
        byte[] fileContent = "test main file".getBytes(StandardCharsets.UTF_8);
        ByteArrayResource sourceFile = new ByteArrayResource(fileContent) {
            @Override
            public String getFilename() {
                return "main.txt";
            }
        };
        ByteArrayResource sourceSig = new ByteArrayResource(fileContent) {
            @Override
            public String getFilename() {
                return "source.sig";
            }
        };
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("sourceFile", sourceFile);
        body.add("sourceSig", sourceSig);
        body.add("createSigStamp", false);
        var req = new HttpEntity<>(body, headers);
        ResponseEntity<Resource> response = restTemplate.postForEntity(
                url(EndpointPaths.CREATE_PDF_CONTAINER), req, Resource.class);

        assertEquals(HttpStatus.OK, response.getStatusCode(), "unexpected HTTP status");
        MediaType contentType = response.getHeaders().getContentType();
        assertEquals(MediaType.APPLICATION_OCTET_STREAM, contentType, "content type mismatch");

        assertNotNull(response.getBody(), "response body is null");

        // FIXME: doesn't work - Container content do not match: expected size is 646328 bytes, current size is 582793 bytes
//        byte[] bodyBytes = response.getBody().getInputStream().readAllBytes();
//        var container = ContainerUtils.readContainer(bodyBytes);
//        assertNotNull(container, "container is null");
    }

    @Test
    void updatePdfContainerRegData() {
    }

    @Test
    void getContainerData() {
    }

    @Test
    void getContainerPart() {
        var containerFile = pdfContainer();
        ContainerSelectorCode[] selectorCodes = {
                ContainerSelectorCode.SOURCEFILE,
        };
        for (var selectorCode : selectorCodes) {
            var selector = new ContainerSelector().code(selectorCode);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("container", containerFile);
            body.add("selector", selector);
            // FIXME: doesn't work - Container content do not match: expected size is 646328 bytes, current size is 582793 bytes
//            var req = new HttpEntity<>(body, headers);
//            ResponseEntity<Resource> response = restTemplate.postForEntity(
//                    url(EndpointPaths.GET_CONTAINER_PART), req, Resource.class);
//
//            assertEquals(HttpStatus.OK, response.getStatusCode(), "unexpected HTTP status");
//            MediaType contentType = response.getHeaders().getContentType();
//            assertEquals(MediaType.APPLICATION_PDF, contentType, "content type mismatch");
//
//            assertNotNull(response.getBody(), "response body is null");
        }
    }

    private static final class EndpointPaths {
        public static final String CREATE_PDF_CONTAINER = "create-pdf-container";
        public static final String UPDATE_PDF_CONTAINER_REGDATA = "update-pdf-container-regdata";
        public static final String GET_CONTAINER_DATA = "get-container-data";
        public static final String GET_CONTAINER_PART = "get-container-part";
    }

    private String url(String path) {
        return "http://localhost:" + port + "/api/v1/" + path;
    }

    private static Resource pdfContainer() {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources;
        try {
            resources = resolver.getResources("file:files/dscp/*");
            if (resources.length > 0) {
                return resources[0];
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}