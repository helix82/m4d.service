package su.opencode.digsigcik.m4d.service.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class Producer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void send(String topic, String key, Object event) {
        kafkaTemplate.send(topic, key, event);
    }
}
