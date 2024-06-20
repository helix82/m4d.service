package su.opencode.digsigcik.synapi.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import su.opencode.digsigcik.synapi.annotation.MchdExceptionHandler;
import su.opencode.digsigcik.synapi.exception.BadRequestException;
import su.opencode.digsigcik.synapi.model.Organisation;
import su.opencode.digsigcik.synapi.service.OrganisationService;

import javax.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static su.opencode.digsigcik.synapi.util.Constant.ID_IS_NULL;
import static su.opencode.digsigcik.synapi.util.Constants.*;

@RestController
@MchdExceptionHandler
@RequestMapping("/api/v1/organisation" )
@AllArgsConstructor
public class OrganisationController {

    private final OrganisationService service;

    @Operation(summary = CONTROLLER_ORGANISATION_FIND_ALL_SUMMARY, description = CONTROLLER_ORGANISATION_FIND_ALL_DESCRIPTION )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @GetMapping()
    public ResponseEntity<?> findAllOrganisation() {
        List<Organisation> response;
        try {
            response = service.findAllOrganisation();
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(ORGANISATION_IS_NULL);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_ORGANISATION_SAVE_SUMMARY, description = CONTROLLER_ORGANISATION_SAVE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @PostMapping("save" )
    public ResponseEntity<?> saveOrganisation(@RequestBody Organisation organisation) {
        Organisation existsOrganisation = service.findByOrganisationInn(organisation.getInn());
        if (existsOrganisation != null) {
            return new ResponseEntity<>(existsOrganisation, HttpStatus.OK);
        }
        Organisation response;
        try {
            response = service.saveOrganisation(organisation);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(ORGANISATION_IS_NULL);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_ORGANISATION_FIND_BY_ID_SUMMARY, description = CONTROLLER_ORGANISATION_FIND_BY_ID_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @GetMapping("/{id}" )
    public ResponseEntity<?> findByOrganisationId(@PathVariable @Parameter(name = "id",
            description = PARAMETER_ORGANISATION_ID_DESCRIPTION, example = "1" ) Long id) {
        Organisation response;
        try {
            response = service.findByOrganisationId(id);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(ORGANISATION_IS_NULL);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_ORGANISATION_UPDATE_SUMMARY, description = CONTROLLER_ORGANISATION_UPDATE_DESCRIPTION )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @PutMapping("update" )
    public ResponseEntity<?> updateOrganisation(@RequestBody Organisation organisation) {
        Organisation response;
        try {
            response = service.updateOrganisation(organisation);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(ORGANISATION_IS_NULL);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_ORGANISATION_DELETE_SUMMARY, description = CONTROLLER_ORGANISATION_DELETE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @DeleteMapping("delete/{id}" )
    public ResponseEntity<?> deleteOrganisation(@PathVariable @Parameter(name = "id",
            description = PARAMETER_ORGANISATION_ID_DESCRIPTION, example = "1" ) Long id) {
        try {
            service.deleteOrganisation(id);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(ID_IS_NULL);
        }
        Map<String, String> body = new HashMap<>();
        body.put("result", "true");

        return new ResponseEntity<>(body, HttpStatus.OK);
    }
}
