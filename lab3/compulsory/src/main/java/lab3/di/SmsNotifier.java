package lab3.di;

import org.springframework.stereotype.Component;

/** Alternative notifier, selected explicitly with {@code @Qualifier("sms")}. */
@Component("sms")
public class SmsNotifier implements Notifier {
    @Override
    public String send(String message) {
        return "[SMS] " + message;
    }
}
