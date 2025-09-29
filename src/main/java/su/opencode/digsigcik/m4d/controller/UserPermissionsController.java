package su.opencode.digsigcik.m4d.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import su.opencode.digsigcik.common.exceptions.exception.custom.BadRequestException;
import su.opencode.digsigcik.m4d.dto.UserPermissionsRequestDto;
import su.opencode.digsigcik.m4d.dto.UserPermissionsResponseDto;
import su.opencode.digsigcik.m4d.service.UserPermissionsService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static su.opencode.digsigcik.m4d.util.Constants.API_RESPONSE_CODE_200_MESSAGE;
import static su.opencode.digsigcik.m4d.util.Constants.API_RESPONSE_CODE_400_MESSAGE;

@RestController
@RequiredArgsConstructor
@Tag(name = "User Permissions Controller")
@RequestMapping("/api/v1/m4d/user-permissions")
public class UserPermissionsController {
    private final UserPermissionsService userPermissionsService;

    @Operation(
            summary = "Отдать запись",
            description = "Отдает запись по id"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @GetMapping("/{id}")
    public UserPermissionsResponseDto getUserPermissionsById(@PathVariable Long id) {
        return userPermissionsService.findById(id);
    }

    @Operation(
            summary = "Отдать запись",
            description = "Отдает запись по login"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @GetMapping("/by-login/{login}")
    public UserPermissionsResponseDto getUserPermissionsByLogin(@PathVariable String login) {
        return userPermissionsService.findByLogin(login);
    }

    @Operation(
            summary = "Отдать все записи",
            description = "Отдает все записи"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @GetMapping
    public List<UserPermissionsResponseDto> getAllUserPermissions() {
        return userPermissionsService.findAll();
    }

    @Operation(
            summary = "Создать новую запись",
            description = "Создаёт новую запись в хранилище"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @PostMapping
    public UserPermissionsResponseDto createUserPermissions(@RequestBody UserPermissionsRequestDto dto) {
        UserPermissionsResponseDto response = userPermissionsService.findByLogin(dto.getLogin());
        if (response != null) {
            throw new BadRequestException("Запись с таким логином уже существует в ПЭП. Внесите изменения в неё или укажите другой логин.");
        }
        return userPermissionsService.create(dto);
    }

    @Operation(
            summary = "Полное обновление записи",
            description = "Обновляет запись полностью"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @PutMapping("/{id}")
    public UserPermissionsResponseDto updateUserPermissionsById(@PathVariable Long id, @RequestBody UserPermissionsRequestDto dto) {
        return userPermissionsService.update(id, dto);
    }

    @Operation(
            summary = "Удаление записи",
            description = "Удаляет запись по id"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE),
            @ApiResponse(responseCode = "400", description = API_RESPONSE_CODE_400_MESSAGE)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUserPermissionsById(@PathVariable Long id) {
        Map<String, String> body = new HashMap<>();
        body.put("result", String.valueOf(userPermissionsService.delete(id)));

        return ResponseEntity.ok(body);
    }
}
