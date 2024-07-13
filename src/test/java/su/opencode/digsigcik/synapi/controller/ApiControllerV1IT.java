package su.opencode.digsigcik.synapi.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import su.opencode.digsigcik.synapi.api.model.ContainerSelector;
import su.opencode.digsigcik.synapi.dscp.Container;
import su.opencode.digsigcik.synapi.dscp.ContainerSelectorCode;
import su.opencode.digsigcik.synapi.dscp.ContainerUtils;
import su.opencode.digsigcik.synapi.dscp.ResultTypeEnum;
import su.opencode.digsigcik.synapi.util.StubUtils;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
	webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class ApiControllerV1IT {

	@LocalServerPort
	private int port;

	private final Log log = LogFactory.getLog(ApiControllerV1IT.class);

	@Autowired
	private TestRestTemplate restTemplate;

	@Disabled
	@Test
	void createPdfContainer() throws Exception {

		Resource srcFile = StubUtils.getStubFileResource("source/Тестовый документ для подписи.pdf");
		Resource sigFile = StubUtils.getStubFileResource("source/pkcs7file.sig");

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add("srcFile", srcFile);
		body.add("sigFile", sigFile);
		body.add("createSigStamp", false);
		var req = new HttpEntity<>(body, headers);
		ResponseEntity<Resource> response = restTemplate.postForEntity(url(EndpointPaths.CREATE_PDF_CONTAINER), req, Resource.class);

		assertEquals(HttpStatus.OK, response.getStatusCode(), "unexpected HTTP status");
		MediaType contentType = response.getHeaders().getContentType();
		assertEquals(MediaType.APPLICATION_OCTET_STREAM, contentType, "content type mismatch");

		assertNotNull(response.getBody(), "response body is null");

		byte[] bodyBytes = response.getBody().getInputStream().readAllBytes();
		Container container = ContainerUtils.readContainer(bodyBytes);
		assertNotNull(container, "container is null");

		for (ResultTypeEnum resType : ResultTypeEnum.values()) {

			var res = ContainerUtils.getFromContainer(container, List.of(resType), false);
			assertNotNull(res, "getFromContainer result is null");
			assertTrue(res.size() <= 1, "getFromContainer result cannot be more than 1");
			var fd = res.get(resType);
			if (fd == null) {
				log.debug("There is no content for resultType=" + resType);
			} else {
				log.debug(String.format("For resultType=%s found string content '%s' and byte content of length=%d with contentType=%s%n",
					resType, fd.getMetaValue(), fd.getContent().length, fd.getContentType()));
			}
		}
	}

	@Disabled
	@Test
	void readPdfConainer() throws Exception {
		byte[] bodyBytes = StubUtils.getStubFileContent("dscp/No_01_01_41_20_02_2024.zip");
		Container container = ContainerUtils.readContainer(bodyBytes);
		assertNotNull(container, "container is null");

		for (ResultTypeEnum resType : ResultTypeEnum.values()) {

			var res = ContainerUtils.getFromContainer(container, List.of(resType), false);
			assertNotNull(res, "getFromContainer result is null");
			assertTrue(res.size() <= 1, "getFromContainer result cannot be more than 1");
			var fd = res.get(resType);
			if (fd == null) {
				log.debug("There is no content for resultType=" + resType);
			} else {
				log.debug(String.format("For resultType=%s found string content '%s' and byte content of length=%d with contentType=%s%n",
					resType, fd.getMetaValue(), fd.getContent().length, fd.getContentType()));
			}
		}
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
                ContainerSelectorCode.SOURCE_FILE,
        };
        for (var selectorCode : selectorCodes) {
            var selector = new ContainerSelector().code(selectorCode);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("container", containerFile);
            body.add("selector", selector);
            var req = new HttpEntity<>(body, headers);
            ResponseEntity<Resource> response = restTemplate.postForEntity(
                    url(EndpointPaths.GET_CONTAINER_PART), req, Resource.class);

            if (response.getStatusCode().isError()) {
                System.out.printf("Body: %s\n", response.getBody());
            }
            assertEquals(HttpStatus.OK, response.getStatusCode(), "unexpected HTTP status");
            MediaType contentType = response.getHeaders().getContentType();
            assertEquals(MediaType.APPLICATION_PDF, contentType, "content type mismatch");

            assertNotNull(response.getBody(), "response body is null");
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