package su.opencode.digsigcik.m4d.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import su.opencode.digsigcik.common.exceptions.exception.custom.BadRequestException;
import su.opencode.digsigcik.m4d.dto.UserPermissionsRequestDto;
import su.opencode.digsigcik.m4d.dto.UserPermissionsResponseDto;
import su.opencode.digsigcik.m4d.mapper.UserPermissionsMapper;
import su.opencode.digsigcik.m4d.repository.UserPermissionsRepository;
import su.opencode.digsigcik.m4d.service.UserPermissionsService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UserPermissionsServiceImpl implements UserPermissionsService {
    private final UserPermissionsRepository repository;
    private final UserPermissionsMapper mapper;

    @Override
    public List<UserPermissionsResponseDto> findAll() {
        return repository.findAll().stream()
                .map(mapper::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserPermissionsResponseDto findById(Long id) {
        return repository.findById(id)
                .map(mapper::mapToResponseDto)
                .orElseThrow(() -> new BadRequestException("Сущность не найдена с id: " + id));
    }

    @Override
    public UserPermissionsResponseDto create(UserPermissionsRequestDto dto) {
        return Optional.of(dto)
                .map(mapper::createMap)
                .map(repository::save)
                .map(mapper::mapToResponseDto)
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
    public UserPermissionsResponseDto update(Long id, UserPermissionsRequestDto dto) {
        return repository.findById(id)
                .map(entity -> mapper.updateMap(entity, dto))
                .map(repository::saveAndFlush)
                .map(mapper::mapToResponseDto)
                .orElseThrow(() -> new BadRequestException("Сущность не найдена с id: " + id));
    }

    @Override
    public UserPermissionsResponseDto findByLogin(String login) {
        return repository.findByLogin(login)
                .map(mapper::mapToResponseDto)
                .orElse(new UserPermissionsResponseDto(null, null, 0, null));
    }
}
