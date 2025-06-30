package app.quantun.blog.shared.valueobject;

import java.util.Objects;

public record Slug(String value) {
    public Slug {
        Objects.requireNonNull(value, "Slug cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Slug cannot be blank");
        }
    }

    public static Slug fromTitle(String title) {
        String slugValue = title.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
        return new Slug(slugValue);
    }

    public static Slug of(String value) {
        return new Slug(value);
    }
}
