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
import su.opencode.digsigcik.synapi.model.RepresentativePassport;
import su.opencode.digsigcik.synapi.service.RepresentativePassportService;

import javax.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static su.opencode.digsigcik.synapi.util.Constant.ID_IS_NULL;
import static su.opencode.digsigcik.synapi.util.Constants.*;

@RestController
@MchdExceptionHandler
@RequestMapping("/api/v1/representative_passport")
@AllArgsConstructor
public class RepresentativePassportController {
    private final RepresentativePassportService service;

    @Operation(summary = CONTROLLER_REPRESENTATIVE_PASSPORT_FIND_ALL_SUMMARY, description = CONTROLLER_REPRESENTATIVE_PASSPORT_FIND_ALL_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @GetMapping()
    public ResponseEntity<?> findAllRepresentativePassport() {
        List<RepresentativePassport> response;
        try {
            response = service.findAllRepresentativePassport();
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(REPRESENTATIVE_PASSPORT_IS_NULL);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_REPRESENTATIVE_PASSPORT_SAVE_SUMMARY, description = CONTROLLER_REPRESENTATIVE_PASSPORT_SAVE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @PostMapping("save")
    public ResponseEntity<?> saveRepresentativePassport(@RequestBody RepresentativePassport representativePassport) {
        RepresentativePassport response;
        try {
            response = service.saveRepresentativePassport(representativePassport);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(REPRESENTATIVE_PASSPORT_IS_NULL);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_REPRESENTATIVE_PASSPORT_FIND_BY_ID_SUMMARY, description = CONTROLLER_REPRESENTATIVE_PASSPORT_FIND_BY_ID_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> findByRepresentativePassportId(@PathVariable @Parameter(name = "id",
            description = PARAMETER_REPRESENTATIVE_PASSPORT_ID_DESCRIPTION, example = "1" ) Long id) {
        RepresentativePassport response;
        try {
            response = service.findByRepresentativePassportId(id);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(REPRESENTATIVE_PASSPORT_IS_NULL);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_REPRESENTATIVE_PASSPORT_UPDATE_SUMMARY, description = CONTROLLER_REPRESENTATIVE_PASSPORT_UPDATE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @PutMapping("update")
    public ResponseEntity<?> updateRepresentativePassport(@RequestBody RepresentativePassport representativePassport) {
        RepresentativePassport response;
        try {
            response = service.updateRepresentativePassport(representativePassport);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(REPRESENTATIVE_PASSPORT_IS_NULL);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_REPRESENTATIVE_PASSPORT_DELETE_SUMMARY, description = CONTROLLER_REPRESENTATIVE_PASSPORT_DELETE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @DeleteMapping("delete/{id}")
    public ResponseEntity<?> deleteRepresentativePassport(@PathVariable @Parameter(name = "id",
            description = PARAMETER_REPRESENTATIVE_PASSPORT_ID_DESCRIPTION, example = "1" ) Long id) {
        try {
            service.deleteRepresentativePassport(id);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(ID_IS_NULL);
        }
        Map<String, String> body = new HashMap<>();
        body.put("result", "true");

        return new ResponseEntity<>(body, HttpStatus.OK);
    }
}
