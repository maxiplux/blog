package app.quantun.blog.application.query.port.out;

import app.quantun.blog.domain.model.Author;
import app.quantun.blog.domain.model.AuthorId;
import app.quantun.blog.shared.valueobject.Email;

import java.util.Optional;

public interface AuthorQueryRepositoryPort {
    Optional<Author> findById(AuthorId id);

    Optional<Author> findByEmail(Email email);

    boolean existsByEmail(Email email);
}