package lab3.library.config;

import lab3.library.domain.MembershipType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.Map;

/**
 * Custom configuration of a library deployment ({@code library.*} in application.yml).
 */
@ConfigurationProperties("library")
public record LibraryProperties(@DefaultValue Borrowing borrowing,
                                @DefaultValue Events events,
                                @DefaultValue Rules rules) {

    public enum PolicyType { FIXED, ROLE_BASED, RULES }

    /**
     * @param policy         which {@code BorrowingPolicy} Spring injects: fixed | role-based | rules
     * @param fixedLimit     max books per member for the fixed policy (and fallback for unknown roles)
     * @param loanDays       default loan period in days
     * @param roleLimits     max books per membership type for the role-based policy
     * @param roleLoanDays   loan period per membership type for the role-based policy
     */
    public record Borrowing(@DefaultValue("fixed") PolicyType policy,
                            @DefaultValue("3") int fixedLimit,
                            @DefaultValue("14") int loanDays,
                            Map<MembershipType, Integer> roleLimits,
                            Map<MembershipType, Integer> roleLoanDays) {

        public Borrowing {
            roleLimits = roleLimits == null ? Map.of() : Map.copyOf(roleLimits);
            roleLoanDays = roleLoanDays == null ? Map.of() : Map.copyOf(roleLoanDays);
        }
    }

    /** @param heavyBorrowerThreshold an event is published when a member holds more books than this */
    public record Events(@DefaultValue("3") int heavyBorrowerThreshold) {
    }

    /**
     * @param location   Spring resource pattern of the DRL files (e.g. {@code file:./rules/*.drl})
     * @param autoReload rebuild the rule base when a DRL file changes on disk
     */
    public record Rules(@DefaultValue("classpath*:rules/*.drl") String location,
                        @DefaultValue("false") boolean autoReload) {
    }
}
