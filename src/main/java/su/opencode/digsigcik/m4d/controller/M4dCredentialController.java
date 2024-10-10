package su.opencode.digsigcik.m4d.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import su.opencode.digsigcik.m4d.annotation.M4dExceptionHandler;
import su.opencode.digsigcik.m4d.dto.CredentialDto;
import su.opencode.digsigcik.m4d.model.Credential;
import su.opencode.digsigcik.m4d.service.CredentialService;

import javax.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static su.opencode.digsigcik.m4d.util.Constants.*;

@RestController
@M4dExceptionHandler
@RequestMapping("/api/v1/m4d/m4d-credential")
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
    @PostMapping("save")
    public ResponseEntity<?> saveCredential(@RequestBody CredentialDto credentialDTO) {
        Credential credential = convertToEntity(credentialDTO);
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
    @GetMapping("find_by_code_or_name/{codeOrName}")
    public ResponseEntity<?> findCredentialByCodeOrName(@PathVariable @Parameter(name = "codeOrName",
            description = PARAMETER_CREDENTIAL_CODE_OR_NAME_DESCRIPTION, example = "EMPOWER") String codeOrName) {
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
    @GetMapping("mrpaId/{mrpaId}")
    public ResponseEntity<?> findCredentialByMrpaId(@PathVariable @Parameter(name = "mrpaId") Long mrpaId) {
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
    @PutMapping("update")
    public ResponseEntity<?> updateCredential(@RequestBody CredentialDto credentialDTO) {
        Credential credential = convertToEntity(credentialDTO);
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
    @DeleteMapping("delete/{id}")
    public ResponseEntity<?> deleteCredential(@PathVariable @Parameter(name = "id",
            description = PARAMETER_CREDENTIAL_ID_DESCRIPTION, example = "1") Long id) {
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
        CredentialDto credentialDto = modelMapper.map(credential, CredentialDto.class);
        return credentialDto;
    }

    private Credential convertToEntity(CredentialDto credentialDto) {
        Credential credential = modelMapper.map(credentialDto, Credential.class);
        return credential;
    }

    public Map<String, String> getCustomErrorJsonFromString(String text) {
        Map<String, String> body = new HashMap<>();
        body.put("error", text);
        return body;
    }
}
