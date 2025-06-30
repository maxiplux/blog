package app.quantun.blog.domain.model;

import java.util.Objects;
import java.util.UUID;

public record PostId(String value) {
    public PostId {
        Objects.requireNonNull(value, "PostId cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("PostId cannot be blank");
        }
    }

    public static PostId generate() {
        return new PostId(UUID.randomUUID().toString());
    }

    public static PostId of(String value) {
        return new PostId(value);
    }
}