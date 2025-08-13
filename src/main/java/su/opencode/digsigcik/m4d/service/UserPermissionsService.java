package su.opencode.digsigcik.m4d.service;

import su.opencode.digsigcik.m4d.dto.UserPermissionsRequestDto;
import su.opencode.digsigcik.m4d.dto.UserPermissionsResponseDto;

import java.util.List;

public interface UserPermissionsService {
    List<UserPermissionsResponseDto> findAll();
    UserPermissionsResponseDto findById(Long id);
    UserPermissionsResponseDto create(UserPermissionsRequestDto dto);
    boolean delete(Long id);
    UserPermissionsResponseDto update(Long id, UserPermissionsRequestDto dto);
    UserPermissionsResponseDto findByLogin(String login);
}
