package app.quantun.blog.domain.model;

import app.quantun.blog.shared.valueobject.Email;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Builder(toBuilder = true)
public class Author {
    private final String id;
    private String name;
    private Email email;
    private String bio;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Author(String id, String name, Email email, String bio,
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = Objects.requireNonNull(id, "Author id cannot be null");
        this.name = Objects.requireNonNull(name, "Author name cannot be null");
        this.email = Objects.requireNonNull(email, "Author email cannot be null");
        this.bio = bio;
        this.createdAt = Objects.requireNonNull(createdAt, "Created date cannot be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "Updated date cannot be null");
    }

    public static Author create(String name, Email email, String bio) {
        LocalDateTime now = LocalDateTime.now();
        return Author.builder()
                .id(java.util.UUID.randomUUID().toString())
                .name(name)
                .email(email)
                .bio(bio)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public Author updateBio(String newBio) {
        this.bio = newBio;
        this.updatedAt = LocalDateTime.now();
        return this;
    }

    public Author updateName(String newName) {
        this.name = Objects.requireNonNull(newName, "Name cannot be null");
        this.updatedAt = LocalDateTime.now();
        return this;
    }
}
