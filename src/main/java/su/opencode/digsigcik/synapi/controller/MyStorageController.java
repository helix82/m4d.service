package su.opencode.digsigcik.synapi.controller;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import su.opencode.digsigcik.synapi.annotation.SettingsExceptionHandler;
import su.opencode.digsigcik.synapi.dto.SettingsEntityRequestDto;
import su.opencode.digsigcik.synapi.dto.SettingsEntityResponseDto;
import su.opencode.digsigcik.synapi.service.SettingsService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.notFound;
import static su.opencode.digsigcik.synapi.util.Constants.*;
import static su.opencode.digsigcik.synapi.util.JwtTokenUtils.getAuthenticationEmail;

@RestController
@SettingsExceptionHandler
@RequiredArgsConstructor
@RequestMapping("/api/v1/my-storage")
public class MyStorageController {
    private final SettingsService settingsService;

    @Operation(summary = CONTROLLER_MY_STORAGE_FIND_ALL_SUMMARY, description = CONTROLLER_MY_STORAGEE_FIND_ALL_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = API_RESPONSE_CODE_204_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer token for authorization", required = true, dataType = "string", paramType = "header")
    })
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> findAll(Authentication authentication) {
        List<SettingsEntityResponseDto> list
                = settingsService.findAllByLogin(getAuthenticationEmail(authentication));
        return list.isEmpty() ?
                noContent().build() :
                new ResponseEntity<>(list, HttpStatus.OK);
    }

    @Operation(summary = CONTROLLER_MY_STORAGE_FIND_BY_ID_SUMMARY, description = CONTROLLER_MY_STORAGEE_FIND_BY_ID_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer token for authorization", required = true, dataType = "string", paramType = "header")
    })
    @GetMapping("/{key}")
    @ResponseStatus(HttpStatus.OK)
    public SettingsEntityResponseDto findByKey(@PathVariable String key, Authentication authentication) {
        return settingsService.findSettingsByKey(key, getAuthenticationEmail(authentication));
    }

    @Operation(summary = CONTROLLER_MY_STORAGEE_SAVE_FOR_USER_SUMMARY, description = CONTROLLER_MY_STORAGEE_SAVE_FOR_USER_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = API_RESPONSE_CODE_201_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer token for authorization", required = true, dataType = "string", paramType = "header")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SettingsEntityResponseDto create(@RequestBody SettingsEntityRequestDto dto, Authentication authentication) {
        return settingsService.createForUser(dto, getAuthenticationEmail(authentication));
    }

    @Operation(summary = CONTROLLER_MY_STORAGEE_DELETE_SUMMARY, description = CONTROLLER_MY_STORAGEE_DELETE_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(code = 404, message = API_RESPONSE_CODE_404_MESSAGE)
    })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer token for authorization", required = true, dataType = "string", paramType = "header")
    })
    @DeleteMapping("/{key}")
    public ResponseEntity<?> deleteByKey(@PathVariable String key, Authentication authentication) {
        Map<String, String> body = new HashMap<>();
        body.put("result", "true");

        return settingsService.deletePublicKeyStatementForUserByKey(key,getAuthenticationEmail(authentication)) ?
                new ResponseEntity<>(body, HttpStatus.OK) :
                notFound().build();
    }
}
