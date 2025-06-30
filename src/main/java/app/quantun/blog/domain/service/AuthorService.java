package app.quantun.blog.domain.service;

import app.quantun.blog.domain.model.Author;
import app.quantun.blog.domain.port.in.CreateAuthorUseCase;
import app.quantun.blog.domain.port.out.AuthorRepositoryPort;
import app.quantun.blog.infrastructure.adapter.in.web.GetAuthorUseCase;
import app.quantun.blog.shared.exception.AuthorNotFoundException;
import app.quantun.blog.shared.valueobject.Email;

public class AuthorService implements CreateAuthorUseCase, GetAuthorUseCase {

    private final AuthorRepositoryPort authorRepositoryPort;

    public AuthorService(AuthorRepositoryPort authorRepositoryPort) {
        this.authorRepositoryPort = authorRepositoryPort;
    }

    @Override
    public Author createAuthor(CreateAuthorCommand command) {
        Email email = Email.of(command.email());

        if (authorRepositoryPort.existsByEmail(email)) {
            throw new IllegalArgumentException("Author with email already exists: " + command.email());
        }

        Author author = Author.create(command.name(), email, command.bio());
        return authorRepositoryPort.save(author);
    }

    @Override
    public Author getAuthorById(String authorId) {
        return authorRepositoryPort.findById(authorId)
                .orElseThrow(() -> new AuthorNotFoundException("Author not found: " + authorId));
    }
}
