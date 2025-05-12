package su.opencode.digsigcik.m4d.service;

import su.opencode.digsigcik.dtos.cryptoproxy.sign.CAdESSignEvent;

public interface CryptoProxyClientService {
    String requestFileCadesSignature(String bearerToken, CAdESSignEvent event);
}
