package app.quantun.blog.infrastructure.adapter.in.web;

import app.quantun.blog.domain.model.Author;

public interface GetAuthorUseCase {
    Author getAuthorById(String authorId);
}
