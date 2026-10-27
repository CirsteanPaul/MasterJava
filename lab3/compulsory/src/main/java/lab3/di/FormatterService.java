package lab3.di;

import org.springframework.stereotype.Service;

/** Collaborator injected directly into a field. */
@Service
public class FormatterService {
    public String format(String title, Iterable<Integer> values) {
        return title + ": " + values;
    }
}
