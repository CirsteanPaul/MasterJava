package lab3.di;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    /** 5th injection point: parameters of a {@code @Bean} factory method. */
    @Bean
    public ReportScheduler reportScheduler(ReportGenerator generator, @Value("${report.cron}") String cron) {
        return new ReportScheduler(generator, cron);
    }
}
