package app.quantun.blog.application.command.port.out;

import app.quantun.blog.domain.model.Author;
import app.quantun.blog.domain.model.AuthorId;

public interface AuthorCommandRepositoryPort {
    Author save(Author author);

    void deleteById(AuthorId id);
}