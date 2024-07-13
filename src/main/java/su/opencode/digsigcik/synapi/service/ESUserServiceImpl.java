package su.opencode.digsigcik.synapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.synapi.dto.UserDto;
import su.opencode.digsigcik.synapi.exception.BadRequestException;
import su.opencode.digsigcik.synapi.exception.MchdException;
import su.opencode.digsigcik.synapi.model.ESSUser;
import su.opencode.digsigcik.synapi.repository.ESUserRepository;

import javax.validation.constraints.NotNull;

import static su.opencode.digsigcik.synapi.util.Constants.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ESUserServiceImpl implements ESUserService {
    private final ESUserRepository esUserRepository;


    @Override
    public boolean isESSUserPasswordCorrect(String login, String password) {
        ESSUser user = esUserRepository.findESSUserByLogin(login);
        return user != null && password.equals(user.getPassword());
    }

    @Override
    public UserDto getESSUserByLogin(String login) {
        return new ObjectMapper().convertValue(this.esUserRepository.findESSUserByLogin(login), UserDto.class);
    }


    @Override
    public ESSUser createESSUser(@NotNull ESSUser essUser) {
        return this.esUserRepository.save(essUser);
    }

    @Override
    public ESSUser updateESSUser(@NotNull ESSUser essUser) {
        if (this.esUserRepository.findESSUserByLogin(essUser.getLogin()) == null) {
            throw new BadRequestException("Can`t update ESS user because ESS user id null!");
        }
        Long essUserId = essUser.getId();

        if (essUserId == null) {
            throw new BadRequestException("Can`t update ESS user because id null!");
        }

        ESSUser existESSUser = this.esUserRepository.findESSUserById(essUserId);
        if (existESSUser == null) {
            throw new MchdException(String.format("Can`t update. ESS user with id=%d %s", essUserId, NOT_FOUND));
        }

        return this.esUserRepository.save(existESSUser);
    }

    @Override
    public void deleteESSUser(Long id) {
        ESSUser essUser = this.esUserRepository.findESSUserById(id);
        if (essUser == null) {
            throw new MchdException(String.format("Can`t delete ESS user because ESS user %s", NOT_FOUND));
        }

        this.esUserRepository.deleteById(id);
    }
}
