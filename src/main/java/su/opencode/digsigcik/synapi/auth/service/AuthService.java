package su.opencode.digsigcik.synapi.auth.service;

import su.opencode.digsigcik.synapi.auth.dto.JwtRequestDto;
import su.opencode.digsigcik.synapi.auth.dto.JwtResponseDto;

public interface AuthService {
    JwtResponseDto createAuthToken(JwtRequestDto authRequest);
}
