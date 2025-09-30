package su.opencode.digsigcik.m4d.service;

import org.springframework.security.core.Authentication;
import su.opencode.digsigcik.m4d.dto.UserPermissionsRequestDto;
import su.opencode.digsigcik.m4d.dto.UserPermissionsResponseDto;
import su.opencode.digsigcik.m4d.dto.response.UserPermissionsFindAllResponseDto;

import java.util.List;

public interface UserPermissionsService {
    List<UserPermissionsFindAllResponseDto> findAll();

    UserPermissionsResponseDto findById(Authentication authentication, Long id);

    UserPermissionsResponseDto create(Authentication authentication, UserPermissionsRequestDto dto);

    boolean delete(Long id);

    UserPermissionsResponseDto update(Authentication authentication, Long id, UserPermissionsRequestDto dto);

    UserPermissionsResponseDto findByLogin(Authentication authentication, String login);
}
