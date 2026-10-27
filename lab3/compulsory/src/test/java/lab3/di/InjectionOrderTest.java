package lab3.di;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static lab3.di.InjectionLog.Step.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class InjectionOrderTest {

    @Autowired
    InjectionLog log;

    @Autowired
    ReportScheduler scheduler;

    @Test
    void constructorThenFieldThenSetterAndMethodThenPostConstruct() {
        List<InjectionLog.Step> steps = log.steps();

        assertThat(steps.getFirst()).isEqualTo(CONSTRUCTOR);
        assertThat(steps.get(1)).isEqualTo(FIELD);
        assertThat(steps.subList(2, steps.size() - 1)).containsExactlyInAnyOrder(SETTER, SETTER, METHOD);
        assertThat(steps.getLast()).isEqualTo(POST_CONSTRUCT);
    }

    @Test
    void constructorSeesNothingElseInjected() {
        assertThat(log.entries().getFirst().detail()).contains("field=-, setter=-, method=-");
    }

    @Test
    void everyInjectionPointIsUsable() {
        String report = scheduler.generator().generate();

        assertThat(report)
                .contains("Monthly sales report: [120, 95, 143, 170]")
                .contains("[EMAIL]")
                .contains("[SMS]")
                .contains("2 notifiers known");
        assertThat(scheduler.cron()).isEqualTo("0 0 8 1 * *");
    }
}
