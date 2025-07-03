package app.quantun.blog.application.command.port.out;

import app.quantun.blog.domain.model.Comment;

public interface CommentCommandRepositoryPort {
    Comment save(Comment comment);

    void deleteById(String id);
}