package su.opencode.digsigcik.m4d.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.dtos.kafka.UserProjectionEvent;
import su.opencode.digsigcik.m4d.config.KafkaProperties;
import su.opencode.digsigcik.m4d.service.KafkaProducerTopicTestMessagingService;
import su.opencode.digsigcik.m4d.service.kafka.producer.Producer;

@Service
@RequiredArgsConstructor
@Log4j2
public class KafkaProducerTopicTestMessagingServiceImpl implements KafkaProducerTopicTestMessagingService {
    private final KafkaProperties kafkaProperties;
    private final Producer producer;

    @Override
    public String sendUserProjectionEvent(String login) {
        UserProjectionEvent event = new UserProjectionEvent(login);
        producer.send(kafkaProperties.getTopicTest(),event.getLogin(),event);
        log.info("Send userProjection from producer service {}", event);
        return event.getLogin();
    }
}
