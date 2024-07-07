package su.opencode.digsigcik.synapi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import su.opencode.digsigcik.synapi.exception.SesException;
import su.opencode.digsigcik.synapi.model.ContainerFile;
import su.opencode.digsigcik.synapi.repository.ContainerFileRepository;
import su.opencode.digsigcik.synapi.util.FileUtils;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/sesFiles")
public class ContainerFileController {
    private final ContainerFileRepository containerFileRepository;

    @GetMapping("/getContainerPath")
    public ResponseEntity<?> getContainerPathByUUID(@RequestParam String uuid) {
        if (uuid == null || uuid.isEmpty()) {
            new ResponseEntity<>("UUID is null or empty!", HttpStatus.BAD_REQUEST);
        }
        String filePath = String.format("http://localhost:8080/api/v1/sesFiles/getContainer?uuid=%s", uuid);

        return new ResponseEntity<>(filePath, HttpStatus.OK);
    }

    @GetMapping(value = "/getContainer", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<?> getContainer(@RequestParam(name = "uuid") String uuid) {
        ContainerFile containerFile = containerFileRepository.getContainerFileByUuid(uuid);
        String containerPath = containerFile.getFilePath();
        if (containerPath == null || containerPath.isEmpty()) {
            throw new SesException("Container path is null!");
        }
        byte[] fileContent = FileUtils.getFileContentByPath(containerPath);
        Resource resource = new ByteArrayResource(fileContent);
        var contentType = MediaType.APPLICATION_OCTET_STREAM;
        String filename = getContainerName(containerPath);

        try {
            var disposition = ContentDisposition.attachment()
                    .filename(filename)
                    .build();

            return ResponseEntity.ok()
                    .contentLength(resource.contentLength())
                    .contentType(contentType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                    .body(resource);
        } catch (IOException e) {
            return new ResponseEntity<>("Could not create octet-stream from resource: " +
                    "resource cannot be resolved", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String getContainerName(String containerPath) {
        Pattern pattern = Pattern.compile("container.+?dscp");
        Matcher matcher = pattern.matcher(containerPath);

        return matcher.find() ?
                containerPath.substring(matcher.start(), matcher.end()) :
                "container.dscp";
    }

}
