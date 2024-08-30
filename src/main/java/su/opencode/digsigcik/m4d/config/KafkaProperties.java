package su.opencode.digsigcik.m4d.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@Data
public class KafkaProperties {
    @Value("${spring.kafka.topics.test}")
    private String topicTest;
    @Value("${spring.kafka.bootstrap-servers}")
    private List<String> bootstrapAddresses;
}
