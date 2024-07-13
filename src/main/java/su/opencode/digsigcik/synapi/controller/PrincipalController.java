package su.opencode.digsigcik.synapi.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import su.opencode.digsigcik.synapi.annotation.MchdExceptionHandler;
import su.opencode.digsigcik.synapi.dto.PrincipalDTO;
import su.opencode.digsigcik.synapi.exception.BadRequestException;
import su.opencode.digsigcik.synapi.model.Principal;
import su.opencode.digsigcik.synapi.service.PrincipalService;

import javax.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static su.opencode.digsigcik.synapi.util.Constants.ID_IS_NULL;
import static su.opencode.digsigcik.synapi.util.Constants.*;

@RestController
@MchdExceptionHandler
@RequestMapping("/api/v1/principal")
@AllArgsConstructor
public class PrincipalController {
    private final PrincipalService service;
    private final ModelMapper modelMapper;

    @Operation(summary = CONTROLLER_PRINCIPAL_FIND_ALL_SUMMARY, description = CONTROLLER_PRINCIPAL_FIND_ALL_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @GetMapping()
    public ResponseEntity<?> findAllPrincipal() {
        List<Principal> response;
        try {
            response = service.findAllPrincipal();
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(PRINCIPAL_IS_NULL);
        }
        return new ResponseEntity<>(response.stream().map(this::convertToDto).collect(Collectors.toList()), HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_PRINCIPAL_SAVE_SUMMARY, description = CONTROLLER_PRINCIPAL_SAVE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @PostMapping("save")
    public ResponseEntity<?> savePrincipal(@RequestBody PrincipalDTO principalDTO) {
        Principal principal = convertToEntity(principalDTO);
        Principal response;
        try {
            response = service.savePrincipal(principal);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(PRINCIPAL_IS_NULL);
        }
        return new ResponseEntity<>(convertToDto(response), HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_PRINCIPAL_FIND_BY_ID_SUMMARY, description = CONTROLLER_PRINCIPAL_FIND_BY_ID_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> findByPrincipalId(@PathVariable @Parameter(name = "id",
            description = PARAMETER_PRINCIPAL_ID_DESCRIPTION, example = "1" ) Long id) {
        Principal response;
        try {
            response = service.findByPrincipalId(id);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(PRINCIPAL_IS_NULL);
        }
        return new ResponseEntity<>(convertToDto(response), HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_PRINCIPAL_UPDATE_SUMMARY, description = CONTROLLER_PRINCIPAL_UPDATE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @PutMapping("update")
    public ResponseEntity<?> updatePrincipal(@RequestBody PrincipalDTO principalDTO) {
        Principal principal = convertToEntity(principalDTO);
        Principal response;
        try {
            response = service.updatePrincipal(principal);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(PRINCIPAL_IS_NULL);
        }
        return new ResponseEntity<>(convertToDto(response), HttpStatus.OK);
    }


    @Operation(summary = CONTROLLER_PRINCIPAL_DELETE_SUMMARY, description = CONTROLLER_PRINCIPAL_DELETE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @DeleteMapping("delete/{id}")
    public ResponseEntity<?> deletePrincipal(@PathVariable @Parameter(name = "id",
            description = PARAMETER_PRINCIPAL_ID_DESCRIPTION, example = "1" ) Long id) {
        try {
            service.deletePrincipal(id);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(ID_IS_NULL);
        }
        Map<String, String> body = new HashMap<>();
        body.put("result", "true");

        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    private PrincipalDTO convertToDto(Principal principal){
        PrincipalDTO principalDTO = modelMapper.map(principal, PrincipalDTO.class);
        return principalDTO;
    }

    private Principal convertToEntity(PrincipalDTO principalDTO){
        Principal principal = modelMapper.map(principalDTO, Principal.class);
        return principal;
    }
}
