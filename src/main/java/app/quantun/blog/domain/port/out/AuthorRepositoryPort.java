package app.quantun.blog.domain.port.out;

import app.quantun.blog.domain.model.Author;
import app.quantun.blog.shared.valueobject.Email;

import java.util.Optional;

public interface AuthorRepositoryPort {
    Author save(Author author);
    Optional<Author> findById(String id);
    Optional<Author> findByEmail(Email email);
    boolean existsByEmail(Email email);
    void deleteById(String id);
}
