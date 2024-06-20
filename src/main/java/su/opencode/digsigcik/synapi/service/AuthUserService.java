package su.opencode.digsigcik.synapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.synapi.exception.UserAuthException;
import su.opencode.digsigcik.synapi.model.ESSUser;
import su.opencode.digsigcik.synapi.repository.ESUserRepository;

import java.util.List;

import static su.opencode.digsigcik.synapi.util.Constant.USER_NOT_FOUND;

//TODO @vitalik.sahnov Временное решение. Удалить после интеграции авторизации
@Service
public class AuthUserService implements UserDetailsService {
    private final PasswordEncoder passwordEncoder;
    private final ESUserRepository esUserRepository;

    @Autowired
    public AuthUserService(@Lazy PasswordEncoder passwordEncoder, ESUserRepository esUserRepository) {
        this.passwordEncoder = passwordEncoder;
        this.esUserRepository = esUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String userLogin) throws UserAuthException {
        ESSUser user = esUserRepository.findESSUserByLogin(userLogin);
        if (user != null) {
            return new User(
                    userLogin,
                    passwordEncoder.encode(user.getPassword()),
                    List.of(new SimpleGrantedAuthority(user.getPosition().name()))
            );
        } else {
            throw new UserAuthException(USER_NOT_FOUND);
        }
    }
}
