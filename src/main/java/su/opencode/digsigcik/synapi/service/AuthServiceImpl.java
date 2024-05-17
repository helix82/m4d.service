package su.opencode.digsigcik.synapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.synapi.dto.JwtRequestDto;
import su.opencode.digsigcik.synapi.dto.JwtResponseDto;
import su.opencode.digsigcik.synapi.exception.UserAuthException;
import su.opencode.digsigcik.synapi.util.JwtTokenUtils;
import su.opencode.digsigcik.synapi.util.UsersInfoUtils;

//TODO @vitalik.sahnov Временное решение. Удалить после интеграции авторизации
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private static final String IS_NULL_OR_EMPTY_ERROR_MESSAGE = "is null or empty!";
    private final UserService userService;
    private final JwtTokenUtils jwtTokenUtils;
    private final AuthenticationManager authenticationManager;
    private final UsersInfoUtils usersInfo = new UsersInfoUtils();

    @Override
    public JwtResponseDto createAuthToken(JwtRequestDto authRequest) throws UserAuthException {
        if (authRequest == null) {
            throw new UserAuthException("Request body is empty!");
        }
        String login = authRequest.getLogin();
        String password = authRequest.getPassword();

        if (login == null || login.isEmpty()) {
            throw new UserAuthException(String.format("Login %s", IS_NULL_OR_EMPTY_ERROR_MESSAGE));
        }
        if (password == null || password.isEmpty()) {
            throw new UserAuthException(String.format("Password %s", IS_NULL_OR_EMPTY_ERROR_MESSAGE));
        }
        if (usersInfo.getUserByUserLogin(login).isEmpty()) {
            throw new UserAuthException(String.format("User with login %s not found!", login));
        }
        if (!usersInfo.isUserPasswordCorrect(login, password)) {
            throw new UserAuthException(String.format("User password %s incorrect!", password));
        }

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(login, password));

        UserDetails userDetails = userService.loadUserByUsername(login);
        String token = jwtTokenUtils.generateToken(userDetails);
        return JwtResponseDto.builder().token(token).build();
    }
}
