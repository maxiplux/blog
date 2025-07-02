package app.quantun.blog.domain.model;

import app.quantun.blog.shared.valueobject.Email;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Builder(toBuilder = true)
public class Author {
    private final AuthorId id;
    private final String name;
    private final Email email;
    private final String bio;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private Author(AuthorId id, String name, Email email, String bio,
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
                .id(AuthorId.generate())
                .name(name)
                .email(email)
                .bio(bio)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public Author updateBio(String newBio) {
        return this.toBuilder()
                .bio(newBio)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public Author updateName(String newName) {
        Objects.requireNonNull(newName, "Name cannot be null");
        
        return this.toBuilder()
                .name(newName)
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    public Author updateEmail(Email newEmail) {
        Objects.requireNonNull(newEmail, "Email cannot be null");
        
        return this.toBuilder()
                .email(newEmail)
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
