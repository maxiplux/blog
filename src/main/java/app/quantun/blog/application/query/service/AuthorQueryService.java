package app.quantun.blog.application.query.service;

import app.quantun.blog.application.port.out.AuthorRepositoryPort;
import app.quantun.blog.application.query.port.in.GetAuthorQuery;
import app.quantun.blog.domain.model.Author;
import app.quantun.blog.domain.model.AuthorId;
import app.quantun.blog.shared.exception.AuthorNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthorQueryService implements GetAuthorQuery {

    private final AuthorRepositoryPort authorRepositoryPort;

    public AuthorQueryService(AuthorRepositoryPort authorRepositoryPort) {
        this.authorRepositoryPort = authorRepositoryPort;
    }

    @Override
    public Author getAuthorById(AuthorId authorId) {
        return authorRepositoryPort.findById(authorId)
                .orElseThrow(() -> new AuthorNotFoundException("Author not found: " + authorId.value()));
    }
}
