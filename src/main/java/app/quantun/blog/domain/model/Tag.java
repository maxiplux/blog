package app.quantun.blog.domain.model;

import java.util.Objects;

public class Tag {
    private final String name;
    private final String slug;

    public Tag(String name, String slug) {
        this.name = Objects.requireNonNull(name, "Tag name cannot be null");
        this.slug = Objects.requireNonNull(slug, "Tag slug cannot be null");
    }

    public static Tag create(String name) {
        String slug = name.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
        return new Tag(name, slug);
    }

    public String getName() { return name; }
    public String getSlug() { return slug; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tag tag = (Tag) o;
        return Objects.equals(slug, tag.slug);
    }

    @Override
    public int hashCode() {
        return Objects.hash(slug);
    }
}
