package su.opencode.digsigcik.synapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.synapi.dto.JwtErrorDto;
import su.opencode.digsigcik.synapi.dto.JwtResponseDto;
import su.opencode.digsigcik.synapi.dto.JwtResponseErrorDto;
import su.opencode.digsigcik.synapi.dto.JwtTokenDto;
import su.opencode.digsigcik.synapi.exception.UserAuthException;
import su.opencode.digsigcik.synapi.util.JwtTokenUtils;

import static su.opencode.digsigcik.synapi.util.Constants.AUTH_ERROR;

//TODO @vitalik.sahnov Временное решение. Удалить после интеграции авторизации
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthUserService authUserService;
    private final JwtTokenUtils jwtTokenUtils;
    private final AuthenticationManager authenticationManager;
    private final ESUserService esUserService;

    @Override
    public JwtResponseDto createAuthToken(String login, String password) throws UserAuthException {
        if (login == null || login.isEmpty()) {
            return createJwtResponseDto(HttpStatus.UNAUTHORIZED);
        }
        if (password == null || password.isEmpty()) {
            return createJwtResponseDto(HttpStatus.UNAUTHORIZED);
        }
        if (esUserService.getESSUserByLogin(login) == null) {
            return createJwtResponseDto(HttpStatus.NOT_FOUND);
        }
        if (!esUserService.isESSUserPasswordCorrect(login, password)) {
            return createJwtResponseDto(HttpStatus.UNAUTHORIZED);
        }

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(login, password));

        UserDetails userDetails = authUserService.loadUserByUsername(login);
        String token = jwtTokenUtils.generateToken(userDetails, esUserService.getESSUserByLogin(login));

        return JwtResponseDto.builder().token(new JwtTokenDto(token)).build();
    }

    private JwtResponseDto createJwtResponseDto(HttpStatus responseStatus) {
        return JwtResponseDto.builder()
                .errorDto(JwtErrorDto.builder()
                        .error(new JwtResponseErrorDto(AUTH_ERROR, responseStatus.value()))
                        .build())
                .status(responseStatus)
                .build();
    }
}
