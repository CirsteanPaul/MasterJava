package lab3.di;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Records, in order, every injection step performed on {@link ReportGenerator}.
 * It is itself injected through the constructor, so it is available from the very first step.
 */
@Component
public class InjectionLog {

    public enum Step { CONSTRUCTOR, FIELD, SETTER, METHOD, POST_CONSTRUCT }

    public record Entry(int order, Step step, String detail) {
        @Override
        public String toString() {
            return "%d. %-14s %s".formatted(order, step, detail);
        }
    }

    private final List<Entry> entries = Collections.synchronizedList(new ArrayList<>());

    public void record(Step step, String detail) {
        entries.add(new Entry(entries.size() + 1, step, detail));
    }

    public List<Entry> entries() {
        return List.copyOf(entries);
    }

    public List<Step> steps() {
        return entries().stream().map(Entry::step).toList();
    }
}
