package lab3.di;

/** Not a component: created by a {@code @Bean} method whose parameters are injected by Spring. */
public record ReportScheduler(ReportGenerator generator, String cron) {
}
