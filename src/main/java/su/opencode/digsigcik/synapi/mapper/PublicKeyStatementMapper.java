package su.opencode.digsigcik.synapi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import su.opencode.digsigcik.synapi.dto.PublicKeyStatementDto;
import su.opencode.digsigcik.synapi.dto.PublicKeyStatementRequestDto;
import su.opencode.digsigcik.synapi.dto.PublicKeyStatementUpdateRequestDto;
import su.opencode.digsigcik.synapi.model.PublicKeyStatement;

@Mapper(componentModel = "spring")
public interface PublicKeyStatementMapper {
    @Mapping(target = "login", expression = "java(login)")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", expression = "java(su.opencode.digsigcik.synapi.enums.StatusStatementEnum.IN_PROGRESS)")
    @Mapping(target = "dtCreate", expression = "java(su.opencode.digsigcik.synapi.util.DateUtils.getCreatedDate())")
    PublicKeyStatement map(PublicKeyStatementRequestDto dto, String login);

    @Mapping(target = "id", ignore = true)
    PublicKeyStatement updateMap(@MappingTarget PublicKeyStatement certificate, PublicKeyStatementUpdateRequestDto dto);

    PublicKeyStatementDto mapToDto(PublicKeyStatement statement);

}
