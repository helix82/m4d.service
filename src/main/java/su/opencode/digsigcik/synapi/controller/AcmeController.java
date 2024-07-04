package su.opencode.digsigcik.synapi.controller;


import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import su.opencode.digsigcik.synapi.service.AcmeService;

import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@AllArgsConstructor
@Slf4j
public class AcmeController {

    @Autowired
    private AcmeService acmeService;

    @Operation(summary = "Выполнить авторизацию на ACME-сервере",
            description = "Выполняет авторизацию на ACME-сервере" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Авторизация успешная" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @GetMapping("/accounts/{id}/login")
    public Map login(HttpSession httpSession,
                    @PathVariable Long id ) {
        return acmeService.login(httpSession, id);
    }

    @Operation(summary = "Выполнить регистрацию на ACME-сервере",
            description = "Выполняет регистрацию на ACME-сервере" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Регистрация успешная" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @PostMapping("/accounts/{id}/register")
    public Map register(HttpSession httpSession,
                        @PathVariable Long id) {
        return acmeService.register(httpSession,id);
    }

    @Operation(summary = "Завершить сессию на ACME-сервере",
            description = "Завершает сессию на ACME-сервере" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Сессия завершена успешно" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @GetMapping("/accounts/{id}/logout")
    public Map logout(HttpSession httpSession, @PathVariable Long id) {
        return acmeService.logout(httpSession,id);
    }

}
