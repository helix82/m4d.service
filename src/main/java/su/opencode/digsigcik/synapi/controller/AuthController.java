package su.opencode.digsigcik.synapi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import su.opencode.digsigcik.synapi.dto.JwtRequestDto;
import su.opencode.digsigcik.synapi.dto.JwtResponseDto;
import su.opencode.digsigcik.synapi.service.AuthService;

//TODO @vitalik.sahnov Временное решение. Удалить после интеграции авторизации
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/auth")
    public ResponseEntity<?> createAuthToken(@RequestBody JwtRequestDto authRequest) {
        if (authRequest == null) {
            return new ResponseEntity<>("Request body is empty!", HttpStatus.BAD_REQUEST);
        }
        String login = authRequest.getLogin();
        String password = authRequest.getPassword();
        JwtResponseDto response = authService.createAuthToken(login, password);
        return response.getToken() == null ?
                new ResponseEntity<>(response.getResponseMessage(), response.getResponseStatus()) :
                new ResponseEntity<>(response.getToken(), HttpStatus.OK);
    }
}