package app.quantun.blog.application.port.in;

import app.quantun.blog.domain.model.Author;
import app.quantun.blog.domain.model.AuthorId;

public interface GetAuthorUseCase {
    Author getAuthorById(AuthorId authorId);
}