package su.opencode.digsigcik.synapi.auth.util;

import lombok.Getter;
import su.opencode.digsigcik.synapi.auth.dto.UserDto;
import su.opencode.digsigcik.synapi.enums.PositionEnum;
import su.opencode.digsigcik.synapi.enums.RoleEnum;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Getter
public final class UsersInfoUtils {
    private final Set<UserDto> usersInfo = new HashSet<>();

    public UsersInfoUtils() {
        fillUsersInfo();
    }

    public Optional<UserDto> getUserByUsername(String username) {
        return usersInfo.stream()
                .filter(userDto -> userDto.getUsername().equals(username))
                .findFirst();
    }

    public boolean isUserPasswordCorrect(String username, String password) {
        return usersInfo.stream()
                .anyMatch(userDto -> userDto.getUsername().equals(username) && userDto.getPassword().equals(password));
    }
    private void fillUsersInfo() {
        UserDto ivanov = createUserDto("ivanov", "12345678", PositionEnum.WORKER, RoleEnum.USER);
        UserDto petrov = createUserDto("petrov", "123456789", PositionEnum.WORKER, RoleEnum.USER);
        UserDto sokolov = createUserDto("sokolov", "12345678", PositionEnum.DIRECTOR, RoleEnum.ADMIN);
        UserDto gudkov = createUserDto("gudkov", "123456789", PositionEnum.WORKER, RoleEnum.USER);

        usersInfo.add(addUserFullName(ivanov, "Иван", "Иванов", "Иванович"));
        usersInfo.add(addUserFullName(petrov, "Петр", "Петров", "Петрович"));
        usersInfo.add(addUserFullName(sokolov, "Юрий", "Соколов", "Александрович"));
        usersInfo.add(addUserFullName(gudkov, "Владимир", "Гудков", "Алексеевич"));
    }

    private UserDto createUserDto(String username, String password, PositionEnum position, RoleEnum role) {
        String email = String.format("%s@gmail.com", username);
        return UserDto.builder()
                .username(username)
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
