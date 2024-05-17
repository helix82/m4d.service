package su.opencode.digsigcik.synapi.service;

import su.opencode.digsigcik.synapi.dto.JwtRequestDto;
import su.opencode.digsigcik.synapi.dto.JwtResponseDto;
//TODO @vitalik.sahnov Временное решение. Удалить после интеграции авторизации
public interface AuthService {
    JwtResponseDto createAuthToken(JwtRequestDto authRequest);
}
