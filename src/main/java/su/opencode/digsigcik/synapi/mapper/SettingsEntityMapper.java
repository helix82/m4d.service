package su.opencode.digsigcik.synapi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import su.opencode.digsigcik.synapi.dto.SettingsEntityRequestDto;
import su.opencode.digsigcik.synapi.dto.SettingsEntityResponseDto;
import su.opencode.digsigcik.synapi.model.SettingsEntity;

@Mapper(componentModel = "spring")
public interface SettingsEntityMapper {

    @Mapping(target = "login", expression = "java(login)")
    SettingsEntity map(SettingsEntityRequestDto dto, String login);

    SettingsEntityResponseDto mapToResponseDto(SettingsEntity settingsEntity);
}
