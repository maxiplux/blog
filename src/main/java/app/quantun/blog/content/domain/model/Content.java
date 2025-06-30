package app.quantun.blog.content.domain.model;

import app.quantun.blog.shared.domain.ValueObject;
import lombok.EqualsAndHashCode;
import lombok.Value;

@EqualsAndHashCode(callSuper = true)
@Value
public class Content extends ValueObject {
    String value;

    public Content(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Content cannot be null or empty");
        }
        if (value.length() < 10) {
            throw new IllegalArgumentException("Content must have at least 10 characters");
        }
        this.value = value.trim();
    }

    public int wordCount() {
        return value.split("\\s+").length;
    }
}
