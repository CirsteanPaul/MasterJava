package lab3.library.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class HeavyBorrowerListener {

    private static final Logger log = LoggerFactory.getLogger(HeavyBorrowerListener.class);

    private final List<HeavyBorrowerEvent> received = new CopyOnWriteArrayList<>();

    @EventListener
    public void onHeavyBorrower(HeavyBorrowerEvent event) {
        received.add(event);
        log.warn("[EVENT] {} now holds {} books (threshold {}) - notifying the librarian",
                event.member().name(), event.booksBorrowed(), event.threshold());
    }

    public List<HeavyBorrowerEvent> received() {
        return List.copyOf(received);
    }
}
