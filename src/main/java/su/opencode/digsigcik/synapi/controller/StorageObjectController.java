package su.opencode.digsigcik.synapi.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import su.opencode.digsigcik.synapi.dto.OrderDto;
import su.opencode.digsigcik.synapi.model.AcmeOrder;
import su.opencode.digsigcik.synapi.model.AcmeProvider;
import su.opencode.digsigcik.synapi.model.StorageObject;
import su.opencode.digsigcik.synapi.repository.CertificateRepository;
import su.opencode.digsigcik.synapi.repository.DomainRepository;
import su.opencode.digsigcik.synapi.repository.OrderRepository;
import su.opencode.digsigcik.synapi.repository.ProviderRepository;
import su.opencode.digsigcik.synapi.repository.StorageObjectRepository;
import su.opencode.digsigcik.synapi.service.AcmeService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/")
@AllArgsConstructor
public class StorageObjectController {
    @Autowired
    private StorageObjectRepository storageObjectRepository;

    @Autowired
    private ProviderRepository providerRepository;

    @Operation(summary = "Получить список вложений",
            description = "Возвращает список вложений" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно получено" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @GetMapping("/storageobjects")
    public ResponseEntity<List<StorageObject>> getAllStorageObjects() {
        List<StorageObject> storageObjects = storageObjectRepository.findAll();
        return new ResponseEntity<>(storageObjects, HttpStatus.OK);
    }

    @Operation(summary = "Получить список вложений указанного провайдера",
            description = "Возвращает список вложений указанного провайдера" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно получено" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @GetMapping("/providers/{providerId}/storageobjects")
    public ResponseEntity<List<StorageObject>> getAllStorageObjectByProviderId(@PathVariable(value = "providerId") Long providerId) {
        AcmeProvider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found AcmeProvider with id = " + providerId));
        List<StorageObject> storageObjects = storageObjectRepository.findByProviderId(provider.getId());
        return new ResponseEntity<>(storageObjects, HttpStatus.OK);
    }

    @Operation(summary = "Создать вложение",
            description = "Создаёт вложение" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Вложение создано" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @PostMapping(value = "/storageobjects/upload")
    public ResponseEntity<StorageObject> uploadStorageObject( @RequestPart("content") @Parameter(name = "content", description="Файл", example = "", required = true) MultipartFile content) {
        try {
            StorageObject storageObject = new StorageObject(content.getOriginalFilename(), content.getBytes());
            storageObjectRepository.save(storageObject);
            return new ResponseEntity<>(storageObject, HttpStatus.OK);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid content");
        }
    }

    @Operation(summary = "Создать вложение для указанного провайдера",
            description = "Создаёт вложение для указанного провайдера" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Вложение создано" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @PostMapping(value = "/providers/{providerId}/storageobjects/upload")
    public ResponseEntity<StorageObject> uploadStorageObjectForProvider( @PathVariable(value = "providerId") Long providerId,
                                                                         @RequestPart("content") @Parameter(name = "content", description="Файл", example = "", required = true) MultipartFile content) {
        try {
            AcmeProvider provider = providerRepository.findById(providerId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found AcmeProvider with id = " + providerId));
            StorageObject storageObject = new StorageObject(content.getOriginalFilename(), content.getBytes());
            storageObject.setProvider(provider);
            storageObjectRepository.save(storageObject);
            return new ResponseEntity<>(storageObject, HttpStatus.OK);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid content");
        }
    }


    @Operation(summary = "Получить вложение",
            description = "Возвращает вложение" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно получено" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @GetMapping("/storageobjects/{id}/download")
    public ResponseEntity<byte[]> downloadStorageObject(HttpServletResponse httpResponse,
                                                           @PathVariable(value = "id") Long id) {
        StorageObject storageObject = storageObjectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found StorageObject with id = " + id));
        HttpHeaders headers = new HttpHeaders();

        String contentType = "application/octet-stream";
        Map<String, Object> data = storageObject.getData();
        if(data!=null && data.containsKey("mimeType")){
            contentType = String.valueOf(data.get("mimeType"));
        }
        headers.add(HttpHeaders.CONTENT_TYPE, contentType);
        String fileName = "storageobject_"+storageObject.getId();
        if(data!=null && data.containsKey("fileName")){
            fileName = String.valueOf(data.get("fileName"));
        }
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename="+fileName);

        return new ResponseEntity<>(storageObject.getContent(), headers, HttpStatus.OK);
    }

}
