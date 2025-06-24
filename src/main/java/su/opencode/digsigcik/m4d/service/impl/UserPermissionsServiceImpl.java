package su.opencode.digsigcik.m4d.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import su.opencode.digsigcik.m4d.dto.UserPermissionsRequestDto;
import su.opencode.digsigcik.common.exception.custom.BadRequestException;
import su.opencode.digsigcik.m4d.mapper.UserPermissionsMapper;
import su.opencode.digsigcik.m4d.model.UserPermissions;
import su.opencode.digsigcik.m4d.repository.UserPermissionsRepository;
import su.opencode.digsigcik.m4d.service.UserPermissionsService;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserPermissionsServiceImpl implements UserPermissionsService {
    private final UserPermissionsRepository repository;
    private final UserPermissionsMapper mapper;

    @Override
    public List<UserPermissions> findAll() {
        return repository.findAll();
    }

    @Override
    public UserPermissions findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BadRequestException("Сущность не найдена с id: " + id));
    }

    @Override
    public UserPermissions create(UserPermissionsRequestDto dto) {
        return Optional.of(dto)
                .map(mapper::createMap)
                .map(repository::save)
                .orElseThrow(() -> new BadRequestException("Ошибка при сохранении"));
    }

    @Override
    public boolean delete(Long id) {
        return repository.findById(id)
                .map(entity -> {
                    repository.delete(entity);
                    repository.flush();
                    return true;
                }).orElse(false);
    }

    @Override
    public UserPermissions update(Long id, UserPermissionsRequestDto dto) {
        return repository.findById(id)
                .map(entity -> mapper.updateMap(entity, dto))
                .map(repository::saveAndFlush)
                .orElseThrow(() -> new BadRequestException("Сущность не найдена с id: " + id));
    }

    @Override
    public UserPermissions findByLogin(String login) {
        return repository.findByLogin(login)
                .orElse(new UserPermissions(null,null,0));
    }
}
