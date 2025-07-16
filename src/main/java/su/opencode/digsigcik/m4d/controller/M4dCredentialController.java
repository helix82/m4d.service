package su.opencode.digsigcik.m4d.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.m4d.dto.CredentialDto;
import su.opencode.digsigcik.m4d.model.Credential;
import su.opencode.digsigcik.m4d.service.CredentialService;

import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static su.opencode.digsigcik.m4d.util.Constants.*;

@RestController
@RequestMapping("/api/v1/m4d/credential")
@AllArgsConstructor
@Tag(name = "M4d Credential Controller")
public class M4dCredentialController {
    private final CredentialService service;
    private final ModelMapper modelMapper;

    @Operation(summary = CONTROLLER_CREDENTIAL_FIND_ALL_SUMMARY, description = CONTROLLER_CREDENTIAL_FIND_ALL_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @GetMapping()
    public ResponseEntity<?> findAllCredential() {
        List<Credential> response;
        try {
            response = service.findAllCredential();
        } catch (ConstraintViolationException e) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(response.stream().map(this::convertToDto).collect(Collectors.toList()), HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_CREDENTIAL_SAVE_SUMMARY, description = CONTROLLER_CREDENTIAL_SAVE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @PostMapping()
    public ResponseEntity<?> saveCredential(@RequestBody CredentialDto credentialDTO) {
        Credential credential = convertToEntity(credentialDTO);
        if ((credential.getName() == null || credential.getName().isEmpty()) && (credential.getCode() == null || credential.getCode().isEmpty())) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(""), HttpStatus.BAD_REQUEST);
        }
        try {
            List<Credential> credentials = service.findCredentialByCode(credential.getCode());
            if (credentials != null && !credentials.isEmpty()) {
                return new ResponseEntity<>(getCustomErrorJsonFromString(CREDENTIAL_CODE_EXISTS), HttpStatus.BAD_REQUEST);
                }
            } catch (ConstraintViolationException e) {
                return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
            }
        Credential response;
        try {
            response = service.saveCredential(credential);
        } catch (ConstraintViolationException e) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(convertToDto(response), HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_CREDENTIAL_FIND_BY_ID_SUMMARY, description = CONTROLLER_CREDENTIAL_FIND_BY_ID_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> findByCredentialId(@PathVariable @Parameter(name = "id",
            description = PARAMETER_CREDENTIAL_ID_DESCRIPTION, example = "1") Long id) {
        if (id == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(ID_IS_NULL), HttpStatus.BAD_REQUEST);
        }
        Credential response;
        try {
            response = service.findByCredentialId(id);
        } catch (ConstraintViolationException e) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(convertToDto(response), HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_CREDENTIAL_FIND_BY_CODE_OR_NAME_SUMMARY, description = CONTROLLER_CREDENTIAL_FIND_BY_CODE_OR_NAME_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @GetMapping("code-or-name/{codeOrName}")
    public ResponseEntity<?> findCredentialByCodeOrName(@PathVariable @Parameter(name = "codeOrName",
            description = PARAMETER_CREDENTIAL_CODE_OR_NAME_DESCRIPTION, example = "EMPOWER") String codeOrName) {
        if (codeOrName == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(STRING_FIND_IS_NULL), HttpStatus.BAD_REQUEST);
        }
        List<Credential> response;
        try {
            response = service.findCredentialByCodeOrName(codeOrName);
        } catch (ConstraintViolationException e) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(response.stream().map(this::convertToDto).collect(Collectors.toList()), HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_CREDENTIAL_FIND_BY_MRPA_ID_SUMMARY, description = CONTROLLER_CREDENTIAL_FIND_BY_MRPA_ID_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @GetMapping("mrpa-id/{mrpaId}")
    public ResponseEntity<?> findCredentialByMrpaId(@PathVariable @Parameter(name = "mrpaId") Long mrpaId) {
        if (mrpaId == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(ID_IS_NULL), HttpStatus.BAD_REQUEST);
        }
        List<Credential> response;
        try {
            response = service.findCredentialByMrpaId(mrpaId);
        } catch (ConstraintViolationException e) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(response.stream().map(this::convertToDto).collect(Collectors.toList()), HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_CREDENTIAL_UPDATE_SUMMARY, description = CONTROLLER_CREDENTIAL_UPDATE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @PutMapping()
    public ResponseEntity<?> updateCredential(@RequestBody CredentialDto credentialDTO) {
        Credential credential = convertToEntity(credentialDTO);
        if (credential == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(CREDENTIAL_IS_NULL), HttpStatus.BAD_REQUEST);
        }
        if (credential.getId() == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(CREDENTIAL_ID_IS_NULL), HttpStatus.BAD_REQUEST);
        }
        Credential response;
        try {
            response = service.updateCredential(credential);
        } catch (ConstraintViolationException e) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(convertToDto(response), HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_CREDENTIAL_DELETE_SUMMARY, description = CONTROLLER_CREDENTIAL_DELETE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @DeleteMapping("{id}")
    public ResponseEntity<?> deleteCredential(@PathVariable @Parameter(name = "id",
            description = PARAMETER_CREDENTIAL_ID_DESCRIPTION, example = "1") Long id) {
        if (id == null) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(ID_IS_NULL), HttpStatus.BAD_REQUEST);
        }
        try {
            service.deleteCredential(id);
        } catch (ConstraintViolationException e) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
        Map<String, String> body = new HashMap<>();
        body.put("result", "true");

        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    private CredentialDto convertToDto(Credential credential) {
        return modelMapper.map(credential, CredentialDto.class);
    }

    private Credential convertToEntity(CredentialDto credentialDto) {
        return modelMapper.map(credentialDto, Credential.class);
    }

    public Map<String, String> getCustomErrorJsonFromString(String text) {
        Map<String, String> body = new HashMap<>();
        body.put("error", text);
        return body;
    }

    @Operation(summary = CONTROLLER_CREDENTIAL_UPLOAD_SUMMARY, description = CONTROLLER_CREDENTIAL_UPLOAD_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @PostMapping(value = "upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> upload(@RequestPart("srcFile") MultipartFile sourceFile) throws IOException {

        if (sourceFile == null || sourceFile.isEmpty()){
            return new ResponseEntity<>(getCustomErrorJsonFromString(FILE_IS_NULL), HttpStatus.BAD_REQUEST);
        }
        if (!Objects.equals(sourceFile.getContentType(), "application/json")) {
            return new ResponseEntity<>(getCustomErrorJsonFromString(FILE_WRONG_FORMAT), HttpStatus.BAD_REQUEST);
        }

        Integer credentials_import = this.service.uploadFromJson(sourceFile);

        Map<String, String> body = new HashMap<>();
        body.put("imported", Integer.toString(credentials_import));

        return new ResponseEntity<>(body, HttpStatus.OK);
    }
}
