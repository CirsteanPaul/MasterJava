package lab3.di;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DiDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DiDemoApplication.class, args);
    }

    @Bean
    CommandLineRunner demo(InjectionLog log, ReportScheduler scheduler) {
        return args -> {
            System.out.println("\n=== Injection order for ReportGenerator ===");
            log.entries().forEach(System.out::println);
            System.out.println("\n=== Using the bean (scheduled at '" + scheduler.cron() + "') ===");
            System.out.println(scheduler.generator().generate());
        };
    }
}
