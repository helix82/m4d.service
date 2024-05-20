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
//TODO @vitalik.sahnov Временное решение. Удалить после интеграции авторизации
@Service
public class UserService implements UserDetailsService {
    private final PasswordEncoder passwordEncoder;
    private final UsersInfoUtils usersInfo = new UsersInfoUtils();

    @Autowired
    public UserService(@Lazy PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
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
            String message = String.format("User with login %s not found!", userLogin);
            throw new UserAuthException(message);
        }
    }
}
