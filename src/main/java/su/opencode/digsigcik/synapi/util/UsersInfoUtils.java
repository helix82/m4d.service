package su.opencode.digsigcik.synapi.util;

import lombok.Getter;
import org.springframework.stereotype.Component;
import su.opencode.digsigcik.synapi.dto.UserDto;
import su.opencode.digsigcik.synapi.enums.PositionEnum;
import su.opencode.digsigcik.synapi.enums.RoleEnum;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
//TODO @vitalik.sahnov Временное решение. Удалить после интеграции авторизации
@Getter
@Component
public final class UsersInfoUtils {
    private final Set<UserDto> usersInfo = new HashSet<>();

    public UsersInfoUtils() {
        fillUsersInfo();
    }

    public Optional<UserDto> getUserByUserLogin(String login) {
        return usersInfo.stream()
                .filter(userDto -> userDto.getLogin().equals(login))
                .findFirst();
    }

    public boolean isUserPasswordCorrect(String login, String password) {
        return usersInfo.stream()
                .anyMatch(userDto -> userDto.getLogin().equals(login) && userDto.getPassword().equals(password));
    }
    private void fillUsersInfo() {
        UserDto ivanov = createUserDto("ivanov", "12345678", PositionEnum.SEP_USER, RoleEnum.USER);
        UserDto petrov = createUserDto("petrov", "123456789", PositionEnum.SEP_ADMIN, RoleEnum.USER);
        UserDto sokolov = createUserDto("sokolov", "12345678", PositionEnum.TLS_ADMIN, RoleEnum.ADMIN);
        UserDto gudkov = createUserDto("gudkov", "123456789", PositionEnum.IOK_OBJECTS_ADMIN, RoleEnum.ADMIN);

        usersInfo.add(addUserFullName(ivanov, "Иван", "Иванов", "Иванович"));
        usersInfo.add(addUserFullName(petrov, "Петр", "Петров", "Петрович"));
        usersInfo.add(addUserFullName(sokolov, "Юрий", "Соколов", "Александрович"));
        usersInfo.add(addUserFullName(gudkov, "Владимир", "Гудков", "Алексеевич"));
    }

    private UserDto createUserDto(String login, String password, PositionEnum position, RoleEnum role) {
        String email = String.format("%s@gmail.com", login);
        return UserDto.builder()
                .login(login)
                .password(password)
                .email(email)
                .position(position)
                .role(role)
                .build();
    }

    private UserDto addUserFullName(UserDto user, String firstName, String lastName, String patronymic) {
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPatronymic(patronymic);
        return user;
    }
}
