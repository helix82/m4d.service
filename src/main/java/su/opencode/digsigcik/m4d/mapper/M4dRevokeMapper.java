package su.opencode.digsigcik.m4d.mapper;

import org.mapstruct.Mapper;
import su.opencode.digsigcik.m4d.dto.response.M4dRevokeResponseDto;
import su.opencode.digsigcik.m4d.model.M4dRecall;

@Mapper(componentModel = "spring")
public interface M4dRevokeMapper {

    M4dRevokeResponseDto mapToRevokeResponseDto(M4dRecall m4dRecall);
}
