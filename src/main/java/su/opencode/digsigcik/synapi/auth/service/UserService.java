package su.opencode.digsigcik.synapi.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.synapi.auth.dto.UserDto;
import su.opencode.digsigcik.synapi.auth.exception.UserAuthException;
import su.opencode.digsigcik.synapi.auth.util.UsersInfoUtils;

import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {
    private final PasswordEncoder passwordEncoder;
    private final UsersInfoUtils usersInfo = new UsersInfoUtils();

    @Autowired
    public UserService(@Lazy PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UserAuthException {
        Optional<UserDto> user = usersInfo.getUserByUsername(username);

        if (user.isPresent()) {
            UserDto userDto = user.get();
            return new User(
                    username,
                    passwordEncoder.encode(userDto.getPassword()),
                    List.of(new SimpleGrantedAuthority(userDto.getRole().name()))
            );
        } else {
            String message = String.format("User with username %s not found!", username);
            throw new UserAuthException(message);
        }
    }
}
