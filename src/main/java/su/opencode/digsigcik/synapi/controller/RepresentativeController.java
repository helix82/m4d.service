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
import su.opencode.digsigcik.synapi.dto.RepresentativeDTO;
import su.opencode.digsigcik.synapi.exception.BadRequestException;
import su.opencode.digsigcik.synapi.model.Representative;
import su.opencode.digsigcik.synapi.service.RepresentativeService;

import javax.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static su.opencode.digsigcik.synapi.util.Constant.ID_IS_NULL;
import static su.opencode.digsigcik.synapi.util.Constants.*;

@RestController
@MchdExceptionHandler
@RequestMapping("/api/v1/representative")
@AllArgsConstructor
public class RepresentativeController {
    private final RepresentativeService service;
    private final ModelMapper modelMapper;

    @Operation(summary = CONTROLLER_REPRESENTATIVE_FIND_ALL_SUMMARY, description = CONTROLLER_REPRESENTATIVE_FIND_ALL_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @GetMapping()
    public ResponseEntity<?> findAllRepresentative() {
        List<Representative> response;
        try {
            response = service.findAllRepresentative();
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(REPRESENTATIVE_IS_NULL);
        }
        return new ResponseEntity<>(response.stream().map(this::convertToDto).collect(Collectors.toList()), HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_REPRESENTATIVE_SAVE_SUMMARY, description = CONTROLLER_REPRESENTATIVE_SAVE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @PostMapping("save")
    public ResponseEntity<?> saveRepresentative(@RequestBody RepresentativeDTO representativeDTO) {
        Representative representative = convertToEntity(representativeDTO);
        Representative response;
        try {
            response = service.saveRepresentative(representative);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(REPRESENTATIVE_IS_NULL);
        }
        return new ResponseEntity<>(convertToDto(response), HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_REPRESENTATIVE_FIND_BY_ID_SUMMARY, description = CONTROLLER_REPRESENTATIVE_FIND_BY_ID_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> findByRepresentativeId(@PathVariable @Parameter(name = "id",
            description = PARAMETER_REPRESENTATIVE_ID_DESCRIPTION, example = "1" ) Long id) {
        Representative response;
        try {
            response = service.findByRepresentativeId(id);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(REPRESENTATIVE_IS_NULL);
        }
        return new ResponseEntity<>(convertToDto(response), HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_REPRESENTATIVE_UPDATE_SUMMARY, description = CONTROLLER_REPRESENTATIVE_UPDATE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @PutMapping("update")
    public ResponseEntity<?> updateRepresentative(@RequestBody RepresentativeDTO representativeDTO) {
        Representative representative = convertToEntity(representativeDTO);
        Representative response;
        try {
            response = service.updateRepresentative(representative);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(REPRESENTATIVE_IS_NULL);
        }
        return new ResponseEntity<>(convertToDto(response), HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_REPRESENTATIVE_DELETE_SUMMARY, description = CONTROLLER_REPRESENTATIVE_DELETE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @DeleteMapping("delete/{id}")
    public ResponseEntity<?> deleteRepresentative(@PathVariable @Parameter(name = "id",
            description = PARAMETER_REPRESENTATIVE_ID_DESCRIPTION, example = "1" ) Long id) {
        try {
            service.deleteRepresentative(id);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException(ID_IS_NULL);
        }
        Map<String, String> body = new HashMap<>();
        body.put("result", "true");

        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    private RepresentativeDTO convertToDto(Representative representative){
        RepresentativeDTO representativeDTO = modelMapper.map(representative, RepresentativeDTO.class);
        return representativeDTO;
    }

    private Representative convertToEntity(RepresentativeDTO representativeDTO){
        Representative representative = modelMapper.map(representativeDTO, Representative.class);
        return representative;
    }
}
