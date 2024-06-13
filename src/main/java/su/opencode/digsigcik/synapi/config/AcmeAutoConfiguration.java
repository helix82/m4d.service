package su.opencode.digsigcik.synapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import su.opencode.digsigcik.synapi.acme.InMemoryChallengeStore;


@Configuration
@ComponentScan(basePackages="su.opencode.digsigcik.synapi.acme")
public class AcmeAutoConfiguration  {

    @Bean
    InMemoryChallengeStore inMemoryChallengeStore () {
        return new InMemoryChallengeStore();
    }

}