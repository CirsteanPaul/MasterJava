package lab3.di;

import org.springframework.stereotype.Service;

import java.util.List;

/** Mandatory collaborator -> constructor injection. */
@Service
public class DataSourceService {
    public List<Integer> monthlySales() {
        return List.of(120, 95, 143, 170);
    }
}
