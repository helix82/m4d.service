package su.opencode.digsigcik.m4d.service;

import su.opencode.digsigcik.m4d.dto.UserPermissionsRequestDto;
import su.opencode.digsigcik.m4d.model.UserPermissions;

import java.util.List;

public interface UserPermissionsService {
    List<UserPermissions> findAll();
    UserPermissions findById(Long id);
    UserPermissions create(UserPermissionsRequestDto dto);
    boolean delete(Long id);
    UserPermissions update(Long id, UserPermissionsRequestDto dto);
    UserPermissions findByLogin(String login);
}
