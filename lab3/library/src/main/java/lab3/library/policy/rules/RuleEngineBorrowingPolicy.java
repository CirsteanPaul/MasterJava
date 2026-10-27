package lab3.library.policy.rules;

import lab3.library.config.LibraryProperties;
import lab3.library.policy.BorrowingDecision;
import lab3.library.policy.BorrowingPolicy;
import lab3.library.policy.BorrowingRequest;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Advanced policy: the decision (eligibility, limit, loan period) is delegated to Drools rules.
 * <p>
 * Rules live in external DRL files ({@code library.rules.location}), so they can be added or changed
 * without touching Java code. With {@code library.rules.auto-reload=true} edited files are picked up at runtime.
 */
@Component
@ConditionalOnProperty(name = "library.borrowing.policy", havingValue = "rules")
public class RuleEngineBorrowingPolicy implements BorrowingPolicy {

    private static final Logger log = LoggerFactory.getLogger(RuleEngineBorrowingPolicy.class);
    private static final int MAX_RULE_FIRINGS = 1_000;

    private final ResourcePatternResolver resolver;
    private final LibraryProperties.Rules config;

    private volatile KieBase kieBase;
    private volatile String fingerprint;

    public RuleEngineBorrowingPolicy(ResourcePatternResolver resolver, LibraryProperties properties) {
        this.resolver = resolver;
        this.config = properties.rules();
        reload();
    }

    @Override
    public String name() {
        return "rule engine (" + config.location() + ")";
    }

    @Override
    public BorrowingDecision evaluate(BorrowingRequest request) {
        if (config.autoReload() && !fingerprint(resources()).equals(fingerprint)) {
            reload();
        }
        BorrowingFacts facts = new BorrowingFacts(
                request.member().name(), request.member().membershipType(), request.currentlyBorrowed(),
                request.book().getTitle(), request.book().getType(), request.book().isAvailable());

        KieSession session = kieBase.newKieSession();
        try {
            session.insert(facts);
            session.fireAllRules(MAX_RULE_FIRINGS);
        } finally {
            session.dispose();
        }
        return toDecision(facts);
    }

    private static BorrowingDecision toDecision(BorrowingFacts facts) {
        List<String> reasons = new ArrayList<>(facts.getDenialReasons());
        // Safe defaults: if the rules forget to define something, the request is rejected.
        if (facts.getMaxBooks() == null) {
            reasons.add("no rule defines a borrowing limit for " + facts.getMembershipType());
        }
        if (facts.getLoanDays() == null) {
            reasons.add("no rule defines a loan period for " + facts.getMembershipType() + "/" + facts.getBookType());
        }
        int maxBooks = facts.getMaxBooks() == null ? 0 : facts.getMaxBooks();
        return reasons.isEmpty()
                ? BorrowingDecision.allow(maxBooks, facts.getLoanDays())
                : BorrowingDecision.deny(maxBooks, reasons);
    }

    /** (Re)compiles all DRL files found at the configured location. */
    public synchronized void reload() {
        Resource[] resources = resources();
        KieServices ks = KieServices.Factory.get();
        KieFileSystem kfs = ks.newKieFileSystem();
        for (Resource resource : resources) {
            try {
                kfs.write(ks.getResources()
                        .newInputStreamResource(resource.getInputStream())
                        .setSourcePath("src/main/resources/lab3/library/rules/" + resource.getFilename())
                        .setResourceType(ResourceType.DRL));
            } catch (IOException e) {
                throw new UncheckedIOException("Cannot read rule file " + resource, e);
            }
        }
        KieBuilder builder = ks.newKieBuilder(kfs).buildAll();
        List<Message> errors = builder.getResults().getMessages(Message.Level.ERROR);
        if (!errors.isEmpty()) {
            throw new IllegalStateException("Invalid borrowing rules: " + errors);
        }
        kieBase = ks.newKieContainer(builder.getKieModule().getReleaseId()).getKieBase();
        fingerprint = fingerprint(resources);
        log.info("Loaded {} borrowing rule(s) from {} file(s) at {}",
                kieBase.getKiePackages().stream().mapToInt(p -> p.getRules().size()).sum(),
                resources.length, config.location());
    }

    private Resource[] resources() {
        try {
            return resolver.getResources(config.location());
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot resolve rules at " + config.location(), e);
        }
    }

    private static String fingerprint(Resource[] resources) {
        return Arrays.stream(resources).map(r -> {
            try {
                return r.getDescription() + "@" + r.lastModified() + "#" + r.contentLength();
            } catch (IOException e) {
                return r.getDescription();
            }
        }).sorted().toList().toString();
    }
}
