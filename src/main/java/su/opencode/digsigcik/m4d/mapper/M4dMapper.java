package su.opencode.digsigcik.m4d.mapper;

import org.mapstruct.Mapper;
import su.opencode.digsigcik.m4d.dto.M4dRegistryResponseDto;
import su.opencode.digsigcik.m4d.model.M4d;

@Mapper(componentModel = "spring")
public interface M4dMapper {

    M4dRegistryResponseDto mapToM4dResponseDto(M4d m4d);
}
