package su.opencode.digsigcik.synapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.synapi.dto.UserDto;
import su.opencode.digsigcik.synapi.exception.UserAuthException;
import su.opencode.digsigcik.synapi.util.UsersInfoUtils;

import java.util.List;
import java.util.Optional;

import static su.opencode.digsigcik.synapi.util.Constant.USER_NOT_FOUND;

//TODO @vitalik.sahnov Временное решение. Удалить после интеграции авторизации
@Service
public class UserService implements UserDetailsService {
    private final PasswordEncoder passwordEncoder;
    private final UsersInfoUtils usersInfo;

    @Autowired
    public UserService(@Lazy PasswordEncoder passwordEncoder, UsersInfoUtils usersInfo) {
        this.passwordEncoder = passwordEncoder;
        this.usersInfo = usersInfo;
    }

    @Override
    public UserDetails loadUserByUsername(String userLogin) throws UserAuthException {
        Optional<UserDto> user = usersInfo.getUserByUserLogin(userLogin);

        if (user.isPresent()) {
            UserDto userDto = user.get();
            return new User(
                    userLogin,
                    passwordEncoder.encode(userDto.getPassword()),
                    List.of(new SimpleGrantedAuthority(userDto.getPosition().name()))
            );
        } else {
            String message = String.format(USER_NOT_FOUND, userLogin);
            throw new UserAuthException(message);
        }
    }
}
