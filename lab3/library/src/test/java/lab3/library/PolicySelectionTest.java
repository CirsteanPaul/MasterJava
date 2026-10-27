package lab3.library;

import lab3.library.policy.BorrowingPolicy;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/** The custom property {@code library.borrowing.policy} decides which implementation is injected. */
class PolicySelectionTest {

    @ParameterizedTest
    @CsvSource({
            "fixed,      lab3.library.policy.FixedLimitBorrowingPolicy",
            "role-based, lab3.library.policy.RoleBasedBorrowingPolicy",
            "rules,      lab3.library.policy.rules.RuleEngineBorrowingPolicy"
    })
    void injectsConfiguredPolicy(String policy, Class<? extends BorrowingPolicy> expected) {
        try (ConfigurableApplicationContext ctx = start("--library.borrowing.policy=" + policy)) {
            assertThat(ctx.getBeansOfType(BorrowingPolicy.class)).hasSize(1);
            assertThat(ctx.getBean(BorrowingPolicy.class)).isInstanceOf(expected);
        }
    }

    private static ConfigurableApplicationContext start(String... args) {
        return new SpringApplicationBuilder(LibraryApplication.class)
                .properties("library.demo.enabled=false")
                .run(args);
    }
}
