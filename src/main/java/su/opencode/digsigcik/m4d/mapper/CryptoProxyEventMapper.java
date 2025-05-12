package su.opencode.digsigcik.m4d.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import su.opencode.digsigcik.dtos.cryptoproxy.sign.CAdESSignEvent;
import su.opencode.digsigcik.dtos.cryptoproxy.sign.SignType;
import su.opencode.digsigcik.m4d.dto.CryptoProCloudBufferDto;

@Mapper(componentModel = "spring", imports = {SignType.class})
public interface CryptoProxyEventMapper {

    @Mapping(target = "content", expression = "java(content)")
    @Mapping(target = "type", expression = "java(mapToSigType(sigType))")
    CAdESSignEvent mapToCAdESSignEvent(CryptoProCloudBufferDto dto, String sigType, String content);

    default SignType mapToSigType(String type) {
        for (SignType signType : SignType.values()) {
            if (signType.name().equalsIgnoreCase(type)) {
                return signType;
            }
        }
        throw new IllegalArgumentException("Неизвестный тип подписи: " + type);
    }
}
