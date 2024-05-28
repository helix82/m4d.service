package su.opencode.digsigcik.synapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.synapi.dto.JwtResponseDto;
import su.opencode.digsigcik.synapi.dto.JwtTokenDto;
import su.opencode.digsigcik.synapi.exception.UserAuthException;
import su.opencode.digsigcik.synapi.util.JwtTokenUtils;
import su.opencode.digsigcik.synapi.util.UsersInfoUtils;

import static su.opencode.digsigcik.synapi.util.Constant.LOGIN_ERROR_MESSAGE;
import static su.opencode.digsigcik.synapi.util.Constant.PASSWORD_ERROR_MESSAGE;
import static su.opencode.digsigcik.synapi.util.Constant.USER_NOT_FOUND;

//TODO @vitalik.sahnov Временное решение. Удалить после интеграции авторизации
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserService userService;
    private final JwtTokenUtils jwtTokenUtils;
    private final AuthenticationManager authenticationManager;
    private final UsersInfoUtils usersInfo;

    @Override
    public JwtResponseDto createAuthToken(String login, String password) throws UserAuthException {
        if (login == null || login.isEmpty()) {
            return createJwtResponseDto(LOGIN_ERROR_MESSAGE, HttpStatus.UNAUTHORIZED);
        }
        if (password == null || password.isEmpty()) {
            return createJwtResponseDto(PASSWORD_ERROR_MESSAGE, HttpStatus.UNAUTHORIZED);
        }
        if (usersInfo.getUserByUserLogin(login).isEmpty()) {
            return createJwtResponseDto(USER_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        if (!usersInfo.isUserPasswordCorrect(login, password)) {
            return createJwtResponseDto("User password incorrect!", HttpStatus.UNAUTHORIZED);
        }

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(login, password));

        UserDetails userDetails = userService.loadUserByUsername(login);
        String token = jwtTokenUtils.generateToken(userDetails, usersInfo.getUserByUserLogin(login).get());

        return JwtResponseDto.builder().token(new JwtTokenDto(token)).build();
    }

    private JwtResponseDto createJwtResponseDto(String responseMessage, HttpStatus responseStatus) {
        return JwtResponseDto.builder()
                .responseMessage(responseMessage)
                .responseStatus(responseStatus)
                .build();
    }
}
