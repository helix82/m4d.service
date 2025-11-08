package su.opencode.digsigcik.m4d.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import su.opencode.digsigcik.audit.AuditLogEventEnum;
import su.opencode.digsigcik.audit.AuditLogThis;
import su.opencode.digsigcik.common.exceptions.exception.custom.BadRequestException;
import su.opencode.digsigcik.m4d.dto.UserPermissionsRequestDto;
import su.opencode.digsigcik.m4d.dto.UserPermissionsResponseDto;
import su.opencode.digsigcik.m4d.dto.response.TrustedPersonFkByLoginResponseDto;
import su.opencode.digsigcik.m4d.dto.response.UserPermissionsFindAllResponseDto;
import su.opencode.digsigcik.m4d.mapper.UserPermissionsMapper;
import su.opencode.digsigcik.m4d.repository.UserPermissionsRepository;
import su.opencode.digsigcik.m4d.service.SynapiClientService;
import su.opencode.digsigcik.m4d.service.UserPermissionsService;
import su.opencode.digsigcik.m4d.util.JwtTokenUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UserPermissionsServiceImpl implements UserPermissionsService {
    private final SynapiClientService synapiClientService;
    private final UserPermissionsRepository repository;
    private final UserPermissionsMapper mapper;

    @Override
    public List<UserPermissionsFindAllResponseDto> findAll() {
        return repository.findAll().stream()
                .map(mapper::mapToFindAllResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserPermissionsResponseDto findById(Authentication authentication, Long id) {
        return repository.findById(id)
                .map(entity -> mapper.mapToResponseDto(entity, getAllTofkByLogin(authentication, entity.getLogin())))
                .orElseThrow(() -> new BadRequestException("Сущность не найдена с id: " + id));
    }

    @AuditLogThis(event = AuditLogEventEnum.USER_PERMISSIONS_CREATE)
    @Override
    public UserPermissionsResponseDto create(Authentication authentication, UserPermissionsRequestDto dto) {
        return Optional.of(dto)
                .map(mapper::createMap)
                .map(repository::save)
                .map(entity -> mapper.mapToResponseDto(entity, getAllTofkByLogin(authentication, entity.getLogin())))
                .orElseThrow(() -> new BadRequestException("Ошибка при сохранении"));
    }

    @AuditLogThis(event = AuditLogEventEnum.USER_PERMISSIONS_DELETE, deleteByIdRepository = UserPermissionsRepository.class)
    @Override
    public boolean delete(Long id) {
        return repository.findById(id)
                .map(entity -> {
                    repository.delete(entity);
                    repository.flush();
                    return true;
                }).orElse(false);
    }

    @AuditLogThis(event = AuditLogEventEnum.USER_PERMISSIONS_UPDATE)
    @Override
    public UserPermissionsResponseDto update(Authentication authentication,
                                             Long id,
                                             UserPermissionsRequestDto dto) {
        return repository.findById(id)
                .map(entity -> mapper.updateMap(entity, dto))
                .map(repository::saveAndFlush)
                .map(entity -> mapper.mapToResponseDto(entity, getAllTofkByLogin(authentication, entity.getLogin())))
                .orElseThrow(() -> new BadRequestException("Сущность не найдена с id: " + id));
    }

    @Override
    public UserPermissionsResponseDto findByLogin(Authentication authentication, String login) {
        return repository.findByLogin(login)
                .map(entity -> mapper.mapToResponseDto(entity, getAllTofkByLogin(authentication, login)))
                .orElseGet(() -> new UserPermissionsResponseDto(null, null, 0, null, getAllTofkByLogin(authentication, login)));
    }

    private List<String> getAllTofkByLogin(Authentication authentication,
                                           String login) {
        return synapiClientService.getAllTrustedPersonFkByLogin(JwtTokenUtils.getTokenValue(authentication), login).stream()
                .map(TrustedPersonFkByLoginResponseDto::getCodeTofk)
                .collect(Collectors.toList());
    }
}
