package su.opencode.digsigcik.synapi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import su.opencode.digsigcik.synapi.dto.PublicKeyUserCertificateAllResponseDto;
import su.opencode.digsigcik.synapi.dto.PublicKeyUserCertificateRequestDto;
import su.opencode.digsigcik.synapi.dto.PublicKeyUserCertificateResponseDto;
import su.opencode.digsigcik.synapi.dto.PublicKeyUserCertificateUpdateRequestDto;
import su.opencode.digsigcik.synapi.model.PublicKeyUserCertificate;

@Mapper(componentModel = "spring")
public interface PublicKeyUserCertificateMapper {

    @Mapping(target = "login", expression = "java(login)")
    @Mapping(target = "id", ignore = true)
    PublicKeyUserCertificate mapToEntity(PublicKeyUserCertificateRequestDto dto, String login);

    @Mapping(target = "id", ignore = true)
    PublicKeyUserCertificate updateMap(@MappingTarget PublicKeyUserCertificate certificate, PublicKeyUserCertificateUpdateRequestDto dto);

    PublicKeyUserCertificateResponseDto mapToResponseDto(PublicKeyUserCertificate certificate);
    PublicKeyUserCertificateAllResponseDto mapToAllResponseDto(PublicKeyUserCertificate certificate);
}
