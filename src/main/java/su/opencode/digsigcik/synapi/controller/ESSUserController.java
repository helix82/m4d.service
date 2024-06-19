package su.opencode.digsigcik.synapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import su.opencode.digsigcik.synapi.annotation.SesExceptionHandler;
import su.opencode.digsigcik.synapi.dto.UserDto;
import su.opencode.digsigcik.synapi.model.ESSUser;
import su.opencode.digsigcik.synapi.service.ESUserService;
import su.opencode.digsigcik.synapi.util.HashUtils;

import java.nio.charset.StandardCharsets;

@RestController
@SesExceptionHandler
@RequiredArgsConstructor
@RequestMapping("/api/v1/ess_user")
public class ESSUserController {
    private final ESUserService esUserService;

    @Operation(summary = "Сохранить пользователя в БД", description = "Возвращает созданного пользователя")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно сохранено"),
            @ApiResponse(code = 404, message = "Not found")
    })
    @PostMapping(value = "create", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> createESSUser(@RequestBody UserDto userDto) {
        ESSUser essUser = new ObjectMapper().convertValue(userDto, ESSUser.class);
        if (esUserService.getESSUserByLogin(userDto.getLogin()) == null) {
            essUser.setPinCodeHash(HashUtils.getSha256Hash(userDto.getPinCode().getBytes(StandardCharsets.UTF_8)));
            esUserService.createESSUser(essUser);
        }
        return new ResponseEntity<>(essUser, HttpStatus.OK);
    }
}
