package lab3.library.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
@EnableConfigurationProperties(LibraryProperties.class)
public class LibraryConfig {

    @Bean
    Clock clock() {
        return Clock.systemDefaultZone();
    }
}
