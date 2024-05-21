package su.opencode.digsigcik.synapi.util;

import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.dto.SigHashDto;
import su.opencode.digsigcik.synapi.dto.UserDto;
import su.opencode.digsigcik.synapi.enums.PositionEnum;
import su.opencode.digsigcik.synapi.enums.RoleEnum;
import su.opencode.digsigcik.synapi.exception.SesException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

//TODO @vitalik.sahnov Временное решение. Удалить все, что касается авторизации после интеграции авторизации
/*TODO @vitalik.sahnov переделать все что касается ПЭП:
   1 - создать таблицу Operation (id, fileHash, login, operationDate)
   2 - добавить в БД для пользоваателя хэш пинкода и хэш файла
* */
@Getter
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

    public Optional<UserDto> getUserByUserPinCodeHash(String pinCodeHash) {
        return usersInfo.stream()
                .filter(userDto -> userDto.getPinCodeHash().equals(pinCodeHash))
                .findFirst();
    }

    public boolean isUserPasswordCorrect(String login, String password) {
        return usersInfo.stream()
                .anyMatch(userDto -> userDto.getLogin().equals(login) && userDto.getPassword().equals(password));
    }

    public SigHashDto generateHashForPinCodeAndFile(String pinCode, MultipartFile file) {
        SigHashDto sigHashDto;
        try {
            sigHashDto = SigHashDto.builder()
                    .pinCodeHash(HashUtils.getSha1Hash(pinCode.getBytes(StandardCharsets.UTF_8)))
                    .fileHash(HashUtils.getSha1Hash(file.getBytes()))
                    .build();
        } catch (IOException e) {
            throw new SesException("Empty byte array!", e.getCause());
        }
        return sigHashDto;
    }

    public void setUserFileHashWithPinCodeHash(String pinCodeHash, String fileHash) {
        Optional<UserDto> userDto = getUserByUserPinCodeHash(pinCodeHash);
        if (userDto.isPresent()) {
            UserDto user = userDto.get();
            user.setFileHash(user.getPinCodeHash().concat(fileHash));
        }
    }

    public void setUserOperationDate(Date operationDate, String userLogin) {
        getUserByUserLogin(userLogin).get().setOperationDate(operationDate);
    }

    private void fillUsersInfo() {
        final String ivanovPinCode = "89265390";
        final String petrovPinCode = "02531485";
        final String sokolovPinCode = "91159207";
        final String gudkovPinCode = "01421652";
        UserDto ivanov = createUserDto("ivanov", "12345678", PositionEnum.SEP_USER,
                RoleEnum.USER, HashUtils.getSha1Hash(ivanovPinCode.getBytes(StandardCharsets.UTF_8)));
        UserDto petrov = createUserDto("petrov", "123456789", PositionEnum.SEP_ADMIN,
                RoleEnum.USER, HashUtils.getSha1Hash(petrovPinCode.getBytes(StandardCharsets.UTF_8)));
        UserDto sokolov = createUserDto("sokolov", "12345678", PositionEnum.TLS_ADMIN,
                RoleEnum.ADMIN, HashUtils.getSha1Hash(sokolovPinCode.getBytes(StandardCharsets.UTF_8)));
        UserDto gudkov = createUserDto("gudkov", "123456789", PositionEnum.IOK_OBJECTS_ADMIN,
                RoleEnum.ADMIN, HashUtils.getSha1Hash(gudkovPinCode.getBytes(StandardCharsets.UTF_8)));

        usersInfo.add(addUserFullNameAndOperationId(ivanov, "Иван", "Иванов",
                "Иванович", 1L));
        usersInfo.add(addUserFullNameAndOperationId(petrov, "Петр", "Петров",
                "Петрович", 2L));
        usersInfo.add(addUserFullNameAndOperationId(sokolov, "Юрий", "Соколов",
                "Александрович", 3L));
        usersInfo.add(addUserFullNameAndOperationId(gudkov, "Владимир", "Гудков",
                "Алексеевич", 4L));
    }

    private UserDto createUserDto(String login, String password, PositionEnum position,
                                  RoleEnum role, String pinCodeHash) {
        String email = String.format("%s@gmail.com", login);
        return UserDto.builder()
                .login(login)
                .password(password)
                .email(email)
                .position(position)
                .role(role)
                .pinCodeHash(pinCodeHash)
                .build();
    }

    private UserDto addUserFullNameAndOperationId(UserDto user, String firstName, String lastName,
                                                  String patronymic, Long operationId) {
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPatronymic(patronymic);
        user.setOperationId(operationId);
        return user;
    }
}
