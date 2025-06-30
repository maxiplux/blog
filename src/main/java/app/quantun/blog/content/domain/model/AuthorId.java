package app.quantun.blog.content.domain.model;

import app.quantun.blog.shared.domain.ValueObject;
import lombok.EqualsAndHashCode;
import lombok.Value;

@EqualsAndHashCode(callSuper = true)
@Value
public class AuthorId extends ValueObject {
    String value;

    public static AuthorId of(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("AuthorId cannot be null or empty");
        }
        return new AuthorId(value);
    }
}
