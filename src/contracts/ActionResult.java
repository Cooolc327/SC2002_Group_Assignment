package contracts;

import java.util.Objects;

/** Rejected player choices must leave state and staff capacity unchanged. */
public record ActionResult(boolean accepted, String message) {
    public ActionResult {
        Objects.requireNonNull(message, "message");
    }

    public static ActionResult success(String message) {
        return new ActionResult(true, message);
    }

    public static ActionResult rejected(String message) {
        return new ActionResult(false, message);
    }
}
