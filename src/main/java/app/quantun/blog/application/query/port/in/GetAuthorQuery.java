package app.quantun.blog.application.query.port.in;

import app.quantun.blog.domain.model.Author;
import app.quantun.blog.domain.model.AuthorId;

public interface GetAuthorQuery {
    Author getAuthorById(AuthorId authorId);
}