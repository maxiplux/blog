package app.quantun.blog.application.command.service;

import app.quantun.blog.application.command.port.in.CreateAuthorCommand;
import app.quantun.blog.application.command.port.out.AuthorCommandRepositoryPort;
import app.quantun.blog.application.query.port.out.AuthorQueryRepositoryPort;
import app.quantun.blog.domain.model.Author;
import org.springframework.stereotype.Service;

@Service
public class AuthorCommandService implements CreateAuthorCommand {

    private final AuthorCommandRepositoryPort authorCommandRepositoryPort;
    private final AuthorQueryRepositoryPort authorQueryRepositoryPort;

    public AuthorCommandService(AuthorCommandRepositoryPort authorCommandRepositoryPort,
                                AuthorQueryRepositoryPort authorQueryRepositoryPort) {
        this.authorCommandRepositoryPort = authorCommandRepositoryPort;
        this.authorQueryRepositoryPort = authorQueryRepositoryPort;
    }

    @Override
    public Author createAuthor(CreateAuthorCommandData command) {
        if (authorQueryRepositoryPort.existsByEmail(command.email())) {
            throw new IllegalArgumentException("Author with email already exists: " + command.email().value());
        }

        Author author = Author.create(command.name(), command.email(), command.bio());
        return authorCommandRepositoryPort.save(author);
    }
}
