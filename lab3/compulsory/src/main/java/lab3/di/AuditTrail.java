package lab3.di;

import org.springframework.stereotype.Component;

/** Collaborator injected through an arbitrary (non-setter) configuration method. */
@Component
public class AuditTrail {
    public String audit(String action) {
        return "audit: " + action;
    }
}
