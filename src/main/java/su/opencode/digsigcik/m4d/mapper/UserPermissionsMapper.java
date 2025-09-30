package su.opencode.digsigcik.m4d.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import su.opencode.digsigcik.m4d.dto.UserPermissionsRequestDto;
import su.opencode.digsigcik.m4d.dto.UserPermissionsResponseDto;
import su.opencode.digsigcik.m4d.dto.response.UserPermissionsFindAllResponseDto;
import su.opencode.digsigcik.m4d.model.UserPermissions;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserPermissionsMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "payload", expression = "java(dto.getPayload() != null ? dto.getPayload().toString() : null)")
    UserPermissions updateMap(@MappingTarget UserPermissions userPermissions, UserPermissionsRequestDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "payload", expression = "java(dto.getPayload() != null ? dto.getPayload().toString() : null)")
    UserPermissions createMap(UserPermissionsRequestDto dto);

    @Mapping(target = "payload", expression = "java(payloadStringToJsonNode(userPermissions.getPayload()))")
    @Mapping(target = "listTofk", expression ="java(listTofk)")
    UserPermissionsResponseDto mapToResponseDto(UserPermissions userPermissions, List<String> listTofk);

    @Mapping(target = "payload", expression = "java(payloadStringToJsonNode(userPermissions.getPayload()))")
    UserPermissionsFindAllResponseDto mapToFindAllResponseDto(UserPermissions userPermissions);

    default JsonNode payloadStringToJsonNode(String payload) {
        if (payload == null) return null;
        try {
            return new ObjectMapper().readTree(payload);
        } catch (Exception e) {
            throw new RuntimeException("Invalid JSON", e);
        }
    }
}
