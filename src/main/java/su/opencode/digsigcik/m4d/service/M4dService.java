package su.opencode.digsigcik.m4d.service;

import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import su.opencode.digsigcik.m4d.dto.*;
import su.opencode.digsigcik.m4d.dto.response.*;
import su.opencode.digsigcik.m4d.model.*;

import javax.validation.constraints.NotNull;
import java.text.ParseException;
import java.util.List;

public interface M4dService {
    List<M4dResponseDto> findAllMrpaRegistry();

    M4d saveMrpaRegistry(@NotNull M4d m4d);

    @Transactional(rollbackFor = {Exception.class}, isolation = Isolation.READ_UNCOMMITTED)
    M4d saveAllDataMrpaRegistry(M4d m4d, OrganisationResponse organisation, PrincipalResponse principal, RepresentativeResponse representative, RepresentativePassportResponse representativePassport, List<CredentialResponse> credentialList, List<Long> credentials);

    @Transactional(rollbackFor = {Exception.class}, isolation = Isolation.READ_UNCOMMITTED)
    M4d updateAllDataMrpaRegistry(M4d m4d, OrganisationResponse organisation, PrincipalResponse principal, RepresentativeResponse representative, RepresentativePassportResponse representativePassport, List<CredentialResponse> credentialList, List<Long> credentials);

    Integer findMrpaRegistryByGuid(@NotNull String guid);

    M4dResponseDto findMrpaRegistryFullById(Long id);

    M4d findById(@NotNull Long id);

    void deleteMrpaRegistry(@NotNull Long id);

    M4dResponseDto findByMrpaRegistryPrincipalInn(String inn);

    void recallMrpaRegistryById(@NotNull Long id);

    List<M4dResponseDto.CredentialDTO> getCredentialByMrpaRegistryId(Long mrpaId);

    M4dFullXML ConvertMrpaRegistryFullDtoToFullXml(M4dResponseDto m4dFullDTO) throws ParseException;

    /*
     * Берет ИННы сертификатов пользователя, потом по ИНН через ID представителей
     * и заявителей находит все МЧД пользователя (т.е. по всем ИНН (как в Представителях, так и в Заявителях) принадлежащим пользователю находит все МЧД)
     *
     * @param login Логин пользователя. Берется из JWT.
     * @return Возвращает все МЧД пользователя
     */
    @Transactional(readOnly = true)
    List<M4dResponseDto> findAllMrpaRegistryByLogin(String login);
}
