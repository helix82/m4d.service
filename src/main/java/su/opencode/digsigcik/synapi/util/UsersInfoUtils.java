package su.opencode.digsigcik.synapi.util;

import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import su.opencode.digsigcik.synapi.dto.SigHashDto;
import su.opencode.digsigcik.synapi.dto.UserDto;
import su.opencode.digsigcik.synapi.enums.PositionEnum;
import su.opencode.digsigcik.synapi.enums.RoleEnum;
import su.opencode.digsigcik.synapi.exception.SesException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static su.opencode.digsigcik.synapi.util.Constant.EMPTY_BYTE_ARRAY_ERROR;

//TODO @vitalik.sahnov Временное решение. Удалить все, что касается авторизации после интеграции авторизации
/*TODO @vitalik.sahnov переделать все что касается ПЭП:
   1 - создать таблицу Operation (id, fileHash, login, operationDate)
   2 - добавить в БД для пользоваателя хэш пинкода и хэш файла
* */
@Getter
@Component
public class UsersInfoUtils {
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

    public boolean isUserWithPinCodeHashNotExist(String pinCodeHash) {
        return getUserByUserPinCodeHash(pinCodeHash).isEmpty();
    }

    public boolean isUserPasswordCorrect(String login, String password) {
        return usersInfo.stream()
                .anyMatch(userDto -> userDto.getLogin().equals(login) && userDto.getPassword().equals(password));
    }

    public SigHashDto generateHashForPinCodeAndFile(String pinCode, MultipartFile file) {
        SigHashDto sigHashDto;
        try {
            String pinCodeHash = HashUtils.getSha256Hash(pinCode.getBytes(StandardCharsets.UTF_8));
            String fileHash = HashUtils.getSha256Hash(file.getBytes());

            sigHashDto = SigHashDto.builder()
                    .pinCodeHash(pinCodeHash)
                    .fileHash(fileHash)
                    .build();
        } catch (IOException e) {
            throw new SesException(EMPTY_BYTE_ARRAY_ERROR, e.getCause());
        }
        return sigHashDto;
    }

    public void setSignature(String userLogin, String signature) {
        for (UserDto userDto : usersInfo) {
            if (userDto.getLogin().equals(userLogin)) {
                userDto.setSignature(signature);
                break;
            }
        }
    }

    public void setFileHash(String userLogin, String fileHash) {
        for (UserDto userDto : usersInfo) {
            if (userDto.getLogin().equals(userLogin)) {
                userDto.setFileHash(fileHash);
                break;
            }
        }
    }

    public void setUserOperationDate(String operationDate, String userLogin) {
        for (UserDto userDto : usersInfo) {
            if (userDto.getLogin().equals(userLogin)) {
                userDto.setOperationDate(operationDate);
                break;
            }
        }
    }

    private void fillUsersInfo() {
        final String ivanovPinCode = "89265390";
        final String petrovPinCode = "02531485";
        final String sokolovPinCode = "91159207";
        final String gudkovPinCode = "01421652";
        UserDto ivanov = createUserDto("ivanov", "12345678", PositionEnum.SEP_USER,
                RoleEnum.USER, HashUtils.getSha256Hash(ivanovPinCode.getBytes(StandardCharsets.UTF_8)));

        UserDto petrov = createUserDto("petrov", "12345678", PositionEnum.SEP_ADMIN,
                RoleEnum.USER, HashUtils.getSha256Hash(petrovPinCode.getBytes(StandardCharsets.UTF_8)));

        UserDto sokolov = createUserDto("sokolov", "12345678", PositionEnum.TLS_ADMIN,
                RoleEnum.ADMIN, HashUtils.getSha256Hash(sokolovPinCode.getBytes(StandardCharsets.UTF_8)));

        UserDto gudkov = createUserDto("gudkov", "12345678", PositionEnum.IOK_OBJECTS_ADMIN,
                RoleEnum.ADMIN, HashUtils.getSha256Hash(gudkovPinCode.getBytes(StandardCharsets.UTF_8)));

        usersInfo.add(addUserFullNameAndOperationId(ivanov, "Иван", "Иванов",
                "Иванович"));
        usersInfo.add(addUserFullNameAndOperationId(petrov, "Петр", "Петров",
                "Петрович"));
        usersInfo.add(addUserFullNameAndOperationId(sokolov, "Юрий", "Соколов",
                "Александрович"));
        usersInfo.add(addUserFullNameAndOperationId(gudkov, "Владимир", "Гудков",
                "Алексеевич"));
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
                                                  String patronymic) {
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPatronymic(patronymic);
        return user;
    }
}
