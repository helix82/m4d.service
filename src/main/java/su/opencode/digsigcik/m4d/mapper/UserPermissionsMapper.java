package su.opencode.digsigcik.m4d.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import su.opencode.digsigcik.m4d.dto.UserPermissionsRequestDto;
import su.opencode.digsigcik.m4d.model.UserPermissions;

@Mapper(componentModel = "spring")
public interface UserPermissionsMapper {

    @Mapping(target = "id", ignore = true)
    UserPermissions updateMap(@MappingTarget UserPermissions userPermissions, UserPermissionsRequestDto dto);

    @Mapping(target = "id", ignore = true)
    UserPermissions createMap(UserPermissionsRequestDto dto);
}
