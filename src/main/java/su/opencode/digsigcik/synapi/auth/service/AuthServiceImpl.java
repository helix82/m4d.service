package su.opencode.digsigcik.synapi.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.synapi.auth.dto.JwtRequestDto;
import su.opencode.digsigcik.synapi.auth.dto.JwtResponseDto;
import su.opencode.digsigcik.synapi.auth.exception.UserAuthException;
import su.opencode.digsigcik.synapi.auth.util.JwtTokenUtils;
import su.opencode.digsigcik.synapi.auth.util.UsersInfoUtils;

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
        String username = authRequest.getUsername();
        String password = authRequest.getPassword();

        if (username == null || username.isEmpty()) {
            throw new UserAuthException(String.format("Username %s", IS_NULL_OR_EMPTY_ERROR_MESSAGE));
        }
        if (password == null || password.isEmpty()) {
            throw new UserAuthException(String.format("Password %s", IS_NULL_OR_EMPTY_ERROR_MESSAGE));
        }
        if (usersInfo.getUserByUsername(username).isEmpty()) {
            throw new UserAuthException(String.format("User with username %s not found!", username));
        }
        if (!usersInfo.isUserPasswordCorrect(username, password)) {
            throw new UserAuthException(String.format("User password %s incorrect!", password));
        }

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

        UserDetails userDetails = userService.loadUserByUsername(username);
        String token = jwtTokenUtils.generateToken(userDetails);
        return JwtResponseDto.builder().token(token).build();
    }
}
