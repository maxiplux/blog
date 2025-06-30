package app.quantun.blog.content.domain.model;

import app.quantun.blog.shared.domain.ValueObject;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

@EqualsAndHashCode(callSuper = true)
@Value
public class Slug extends ValueObject {
    String value;

    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    public Slug(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Slug cannot be null or empty");
        }
        this.value = createSlug(value);
    }

    public static Slug fromTitle(Title title) {
        return new Slug(title.getValue());
    }

    private String createSlug(String input) {
        String noWhitespace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(noWhitespace, Normalizer.Form.NFD);
        String slug = NON_LATIN.matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH);
    }
}
