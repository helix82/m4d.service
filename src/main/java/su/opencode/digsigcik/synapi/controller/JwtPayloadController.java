package su.opencode.digsigcik.synapi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import su.opencode.digsigcik.synapi.dto.JwtPayloadDto;
import su.opencode.digsigcik.synapi.util.JwtTokenUtils;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/jwtPayload")
public class JwtPayloadController {
    private final JwtTokenUtils jwtTokenUtils;
    @GetMapping("/get")
    public ResponseEntity<?> createSes(@RequestHeader(HttpHeaders.AUTHORIZATION) String authToken) {
        JwtPayloadDto jwtPayloadDto = jwtTokenUtils.parseJwtPayload(authToken);
        return jwtPayloadDto == null ?
                new ResponseEntity<>("Token is null!", HttpStatus.NOT_FOUND) :
                new ResponseEntity<>(jwtPayloadDto, HttpStatus.OK);
    }
}
