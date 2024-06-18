package su.opencode.digsigcik.synapi.controller;


import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import su.opencode.digsigcik.synapi.service.AcmeService;

import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@AllArgsConstructor
@Slf4j
public class AcmeController {
    private final AcmeService acmeService;


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



    @Operation(summary = "Выпустить новый сертификат в автоматическом режиме",
            description = "Выпускает новый сертификат в автоматическом режиме" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Сертификат успешно выпущен" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @PostMapping("/certificate")
    public ResponseEntity<Map>  certificate(HttpSession httpSession,
                                           @RequestParam(value = "domainName") String domainName,
                                           @RequestParam(value = "notAfter") String notAfter ) {
        return acmeService.certificate(httpSession,domainName, notAfter);
    }

    @Operation(summary = "Сформировать заявку на выпуск нового сертификата",
            description = "Формирует заявку на выпуск нового сертификата" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Заявка успешно сформирована" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @PostMapping("/certificate/order")
    public ResponseEntity<Map>  certificateOrder(HttpSession httpSession,
                                                 @RequestParam(value = "domainName") String domainName,
                                                 @RequestParam(value = "notAfter") String notAfter
    ) {
        return acmeService.certificateOrder(httpSession,domainName, notAfter);
    }

    @Operation(summary = "Получить сертификат по ранее сформированной заявке",
            description = "Получает сертификат по ранее сформированной заявке" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Сертификат получен" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @PostMapping("/certificate/{id}/release")
    public ResponseEntity<Map>  certificateRelease(HttpSession httpSession,
                                                   @PathVariable(value = "id") Long id
    ) {
        return acmeService.certificateRelease(httpSession,id);
    }


    @Operation(summary = "Выпустить новый сертификат в автоматическом режиме для указанного домена",
            description = "Выпускает новый сертификат в автоматическом режиме для указанного домена" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Сертификат успешно выпущен" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @PostMapping("/domains/{domainId}/certificate")
    public Map certificate(HttpSession httpSession,
                      @PathVariable(value = "domainId") Long domainId
                      ) {
        return acmeService.certificate(httpSession,domainId);
    }

    @Operation(summary = "Отозвать сертификат",
            description = "Отзывает сертификат" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Сертификат успешно отозван" ),
            @ApiResponse(code = 404, message = "Not found" )
    })
    @PostMapping("/certificates/{id}/revoke")
    public Map revoke(HttpSession httpSession,
                           @PathVariable(value = "id") Long id
    ) {
        return acmeService.revoke(httpSession,id);
    }


}
