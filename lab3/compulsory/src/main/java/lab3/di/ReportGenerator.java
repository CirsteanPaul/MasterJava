package lab3.di;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

import static lab3.di.InjectionLog.Step.*;

/**
 * A bean that uses every major injection point Spring offers:
 * <ol>
 *   <li>constructor injection (mandatory, immutable dependencies),</li>
 *   <li>field injection ({@code @Autowired} and {@code @Value} on fields),</li>
 *   <li>setter injection (JavaBean setters, incl. an optional dependency),</li>
 *   <li>method injection (any method annotated with {@code @Autowired}, with several parameters).</li>
 * </ol>
 * Each step writes a snapshot of which dependencies are already present into the {@link InjectionLog}.
 * The snapshots prove the order <b>constructor → field → setter &amp; method</b>, followed by {@code @PostConstruct}.
 */
@Component
public class ReportGenerator {

    // 1. constructor injection
    private final DataSourceService dataSource;
    private final InjectionLog log;

    // 2. field injection
    @Autowired
    private FormatterService formatter;

    @Value("${report.title:Monthly sales}")
    private String title;

    // 3. setter injection
    private Notifier notifier;
    private Notifier backupNotifier;

    // 4. method injection
    private AuditTrail auditTrail;
    private List<Notifier> allNotifiers;
    private ObjectProvider<MissingService> missing;

    public ReportGenerator(DataSourceService dataSource, InjectionLog log) {
        this.dataSource = dataSource;
        this.log = log;
        log.record(CONSTRUCTOR, "dataSource injected; " + snapshot());
    }

    @Autowired
    public void setNotifier(Notifier notifier) { // @Primary -> EmailNotifier
        this.notifier = notifier;
        log.record(SETTER, "notifier=" + notifier.getClass().getSimpleName() + "; " + snapshot());
    }

    @Autowired
    public void setBackupNotifier(@Qualifier("sms") Notifier backupNotifier) {
        this.backupNotifier = backupNotifier;
        log.record(SETTER, "backupNotifier=" + backupNotifier.getClass().getSimpleName() + "; " + snapshot());
    }

    @Autowired
    public void prepare(AuditTrail auditTrail, List<Notifier> allNotifiers, ObjectProvider<MissingService> missing) {
        this.auditTrail = auditTrail;
        this.allNotifiers = allNotifiers;
        this.missing = missing;
        log.record(METHOD, "auditTrail + %d notifiers + optional MissingService (present=%s); %s"
                .formatted(allNotifiers.size(), missing.getIfAvailable() != null, snapshot()));
    }

    @PostConstruct
    void init() {
        log.record(POST_CONSTRUCT, "bean fully initialised; " + snapshot());
    }

    /** Spring gives no callback for field injection, so we detect it: the first step that sees the field set. */
    private String snapshot() {
        if (formatter != null && !fieldSeen) {
            fieldSeen = true;
            log.record(FIELD, "formatter=FormatterService, title='" + title + "' (set after constructor, before this step)");
        }
        return "[constructor=%s, field=%s, setter=%s, method=%s]".formatted(
                mark(dataSource), mark(formatter), mark(notifier), mark(auditTrail));
    }

    private boolean fieldSeen;

    private static String mark(Object o) {
        return o == null ? "-" : "x";
    }

    public String generate() {
        String report = formatter.format(title, dataSource.monthlySales());
        return String.join(System.lineSeparator(),
                report,
                notifier.send(report),
                backupNotifier.send(report),
                auditTrail.audit("report generated, " + allNotifiers.size() + " notifiers known"));
    }

    /** Never registered as a bean: shows optional injection with {@link ObjectProvider}. */
    public interface MissingService {
    }
}
