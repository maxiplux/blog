package app.quantun.blog.content.domain.model;

import app.quantun.blog.shared.domain.ValueObject;
import lombok.EqualsAndHashCode;
import lombok.Value;

@EqualsAndHashCode(callSuper = true)
@Value
public class Title extends ValueObject {
    String value;

    public Title(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
        if (value.length() > 200) {
            throw new IllegalArgumentException("Title cannot exceed 200 characters");
        }
        this.value = value.trim();
    }
}
