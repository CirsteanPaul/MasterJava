package lab3.di;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/** Default notifier: chosen whenever a single {@link Notifier} is requested without a qualifier. */
@Primary
@Component("email")
public class EmailNotifier implements Notifier {
    @Override
    public String send(String message) {
        return "[EMAIL] " + message;
    }
}
