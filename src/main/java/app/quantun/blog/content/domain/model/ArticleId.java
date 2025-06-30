package app.quantun.blog.content.domain.model;

import app.quantun.blog.shared.domain.ValueObject;

import lombok.EqualsAndHashCode;
import lombok.Value;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Value
public class ArticleId extends ValueObject {
    String value;

    public static ArticleId generate() {
        return new ArticleId(UUID.randomUUID().toString());
    }

    public static ArticleId of(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("ArticleId cannot be null or empty");
        }
        return new ArticleId(value);
    }
}