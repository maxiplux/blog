package app.quantun.blog.application.service;

import app.quantun.blog.application.port.in.CreateAuthorUseCase;
import app.quantun.blog.application.port.in.GetAuthorUseCase;
import app.quantun.blog.application.port.out.AuthorRepositoryPort;
import app.quantun.blog.domain.model.Author;
import app.quantun.blog.domain.model.AuthorId;
import app.quantun.blog.shared.exception.AuthorNotFoundException;
import app.quantun.blog.shared.valueobject.Email;
import org.springframework.stereotype.Service;

@Service
public class AuthorApplicationService implements CreateAuthorUseCase, GetAuthorUseCase {

    private final AuthorRepositoryPort authorRepositoryPort;

    public AuthorApplicationService(AuthorRepositoryPort authorRepositoryPort) {
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
    public Author getAuthorById(AuthorId authorId) {
        return authorRepositoryPort.findById(authorId)
                .orElseThrow(() -> new AuthorNotFoundException("Author not found: " + authorId.value()));
    }
}