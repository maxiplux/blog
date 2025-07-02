package app.quantun.blog.domain.model;

import java.util.Objects;
import java.util.UUID;

public record AuthorId(String value) {
    
    public AuthorId {
        Objects.requireNonNull(value, "Author ID cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Author ID cannot be blank");
        }
    }

    public static AuthorId generate() {
        return new AuthorId(UUID.randomUUID().toString());
    }

    public static AuthorId of(String value) {
        return new AuthorId(value);
    }
}
