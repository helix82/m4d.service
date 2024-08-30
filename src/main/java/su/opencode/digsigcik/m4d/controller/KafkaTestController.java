package su.opencode.digsigcik.m4d.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import su.opencode.digsigcik.m4d.service.impl.KafkaProducerTopicTestMessagingServiceImpl;

import static su.opencode.digsigcik.m4d.util.Constants.*;
import static su.opencode.digsigcik.m4d.util.JwtTokenUtils.getAuthenticationUsername;

@RestController
@RequestMapping("/api/v1/test-kafka")
@RequiredArgsConstructor
@Log4j2
public class KafkaTestController {
    private final KafkaProducerTopicTestMessagingServiceImpl kafkaProducerMessagingServiceImpl;

    @Operation(summary = CONTROLLER_TEST_KAFKA_SUMMARY,
            description = CONTROLLER_TEST_KAFKA_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = API_RESPONSE_CODE_200_MESSAGE)
    })
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public String test(Authentication authentication) {
        return kafkaProducerMessagingServiceImpl.sendUserProjectionEvent(getAuthenticationUsername(authentication));
    }
}
